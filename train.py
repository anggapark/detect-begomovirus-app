import argparse
from pathlib import Path
import clearml
from clearml import Task
import dataset
from ultralytics import YOLO
from dataset import get_dataset
from kfold_crossval import (
    get_images_and_labels,
    get_class_labels,
    count_labels,
    generate_kfolds,
    create_folds_df,
    create_dataset_splits,
    create_dataset_directories,
)


def train_model(batch, epochs, img_size=640):
    """
    Trains the YOLO model using K-Fold cross-validation.

    Args:
        dataset: Path to the dataset directory.
        batch: Batch size for training (list of options).
        epochs: Number of training epochs.
        img_size: Image size for training (list of options).
    """
    ksplit = 5

    results = {}

    for k in range(1, ksplit + 1):
        print("")
        print("#" * 70)
        print(f"Train at Fold {k}")
        print("#" * 70)

        # Define parameters
        dataset_yaml = (
            f"{dataset.location}/5-Fold_Cross-val/fold_{k}/fold_{k}_dataset.yaml"
        )
        project = f"yolov8_begomo_batch{batch}_epochs{epochs}_img{img_size}"
        name = f"fold{k}"
        lr = 0.001
        optimizer = "SGD"

        # create ClearML Task
        hyp_task = Task.init(project_name=project, task_name=name)
        model_variant = "yolov8s.pt"
        hyp_task.set_parameter("model_variant", model_variant)
        model = YOLO(model_variant, task="detect")

        # put all argument in the dict and pass it to ClearML
        args = dict(
            data=dataset_yaml,
            epochs=epochs,
            batch=batch,
            project=project,
            name=name,
            lr0=lr,
            imgsz=img_size,
            optimizer=optimizer,
            cache=True,
        )
        hyp_task.connect(args)

        model.train(**args)
        results[k] = model.metrics

        hyp_task.close()


if __name__ == "__main__":
    dataset = get_dataset(version=1)
    dataset_path = Path(dataset.location)
    images, labels = get_images_and_labels(dataset_path)

    classes = get_class_labels(dataset)
    labels_df = count_labels(labels, classes)
    kfolds = generate_kfolds(labels_df, ksplit=5)
    folds_df = create_folds_df(kfolds, labels_df)
    create_dataset_splits(dataset_path, folds_df, kfolds)
    create_dataset_directories(images, labels, dataset_path, kfolds, folds_df)

    # Define Argument Parser
    parser = argparse.ArgumentParser(description="Train YOLOv8 model with K-Fold CV")
    parser.add_argument("--batch", type=int, default=64, help="Batch size for training")
    parser.add_argument(
        "--epochs", type=int, default=60, help="Number of training epochs"
    )
    parser.add_argument(
        "--img_size", type=int, default=640, help="Image size for training"
    )

    # Parse arguments
    args = parser.parse_args()

    # Call train_model function with parsed arguments
    train_model(args)
