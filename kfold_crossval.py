import os
import datetime
import shutil
from pathlib import Path
from collections import Counter
from tqdm import tqdm

import yaml
import numpy as np
import pandas as pd
from sklearn.model_selection import KFold
from dataset import get_dataset


def get_images_and_labels(dataset_path):
    # get images from each dataset split
    data_splits = ["train", "valid"]
    # Initialize an empty list to store image file paths
    images = []

    # Loop through supported extensions and gather image files
    for s in data_splits:
        images.extend(sorted((dataset_path / s / "images").rglob(f"*.jpg")))

    labels = sorted(dataset_path.rglob("*labels/*.txt"))

    return images, labels


def get_class_labels(dataset_path):
    # read yaml file
    yaml_file = os.path.join(dataset_path, "data.yaml")

    with open(yaml_file, "r", encoding="utf8") as file:
        classes = yaml.safe_load(file)["names"]

    return classes


def count_labels(labels, classes):
    """
    Retrieves labels from annotation files and stores them in a DataFrame.

    Args:
        dataset_path: Path object representing the dataset directory.

    Returns:
        A pandas DataFrame with class labels as columns and image filenames as index.
    """
    idx = {i: item for i, item in enumerate(classes)}
    cls_idx = sorted(idx)
    indx = [l.stem for l in labels]

    labels_df = pd.DataFrame([], columns=cls_idx, index=indx)

    for label in tqdm(labels):
        label_counter = Counter()

        with open(label, "r") as lf:
            lines = lf.readlines()

        for l in lines:
            # get the integer at first position representing classes for YOLO labels
            label_counter[int(l.split(" ")[0])] += 1

        labels_df.loc[label.stem] = label_counter

    labels_df = labels_df.fillna(0.0)

    return labels_df


def generate_kfolds(labels_df, ksplit=5):
    """
    Generates k-folds splits using scikit-learn's KFold.

    Args:
        labels_df: Pandas DataFrame containing label counts.
        n_splits: Number of folds (default: 5).

    Returns:
        A list of tuples representing train and validation indices for each fold.
    """
    kf = KFold(n_splits=ksplit, shuffle=True, random_state=42)
    kfolds = list(kf.split(labels_df))

    return kfolds


def create_folds_df(kfolds, labels_df):
    """
    Create a pandas dataFrame to represent the data splits.

    Args:
        kfolds: List of tuples representing train and validation indices for each fold.
        labels_df: Pandas DataFrame containing label counts.
    """
    # create dataframe to display the results
    ksplit = len(kfolds)
    folds = [f"fold_{n}" for n in range(1, ksplit + 1)]
    folds_df = pd.DataFrame(index=labels_df.index, columns=folds)

    # fill df values with train and val split
    for idx, (train, val) in enumerate(kfolds, start=1):
        folds_df[f"fold_{idx}"].loc[labels_df.iloc[train].index] = "train"
        folds_df[f"fold_{idx}"].loc[labels_df.iloc[val].index] = "valid"

    return folds_df


def create_dataset_splits(dataset_path, folds_df, kfolds):
    """
    Creates directories and dataset YAML files for each fold split.

    Args:
        dataset_path: Path object representing the dataset directory.
        folds_df: A pandas DataFrame with train/validation split information.
        kfolds: List of tuples representing train and validation indices.
    """
    ksplit = len(kfolds)
    # Create the necessary directories and dataset YAML files (unchanged)
    save_path = Path(dataset_path / f"{ksplit}-Fold_Cross-val")
    save_path.mkdir(parents=True, exist_ok=True)
    ds_yamls = []

    for split in folds_df.columns:
        # Create directories
        fold_dir = save_path / split
        fold_dir.mkdir(parents=True, exist_ok=True)
        (fold_dir / "train" / "images").mkdir(parents=True, exist_ok=True)
        (fold_dir / "train" / "labels").mkdir(parents=True, exist_ok=True)
        (fold_dir / "valid" / "images").mkdir(parents=True, exist_ok=True)
        (fold_dir / "valid" / "labels").mkdir(parents=True, exist_ok=True)

        # Create dataset YAML files
        dataset_yaml = fold_dir / f"{split}_dataset.yaml"
        ds_yamls.append(dataset_yaml)

        with open(dataset_yaml, "w") as ds_y:
            yaml.safe_dump(
                {
                    "path": f"../{fold_dir.as_posix()}",
                    "train": "train",
                    "val": "valid",
                    "names": get_class_labels(dataset_path),
                },
                ds_y,
            )


def create_dataset_directories(images, labels, dataset_path, kfolds, folds_df):
    """
    Creates directories for each fold (train/valid) and dataset YAML files.

    Args:
        kfolds: List of tuples representing train and validation indices.
        images: List of images data
        folds_df: Pandas DataFrame containing data split information.
    """
    ksplit = len(kfolds)
    save_path = Path(dataset_path / f"{ksplit}-Fold_Cross-val")
    save_path.mkdir(parents=True, exist_ok=True)

    # fill the directories with data
    for image, label in tqdm(zip(images, labels)):
        for split, k_split in folds_df.loc[image.stem].items():
            # Destination directory
            img_to_path = save_path / split / k_split / "images"
            lbl_to_path = save_path / split / k_split / "labels"

            # Copy image and label files to new directory (SamefileError if file already exists)
            shutil.copy(image, img_to_path / image.name)
            shutil.copy(label, lbl_to_path / label.name)


# def main():
#     dataset = get_dataset(version=1)
#     dataset_path = Path(dataset.location)
#     # images = sorted(dataset_path.rglob('*images/*.jpg'))

#     # get images from each dataset split
#     data_splits = ["train", "valid"]
#     # Initialize an empty list to store image file paths
#     images = []

#     # Loop through supported extensions and gather image files
#     for s in data_splits:
#         images.extend(sorted((dataset_path / s / "images").rglob(f"*.jpg")))

#     labels = sorted(dataset_path.rglob("*labels/*.txt"))

#     classes = get_class_labels(dataset_path)
#     labels_df = count_labels(labels, classes)
#     kfolds = generate_kfolds(labels_df, ksplit=5)
#     folds_df = create_folds_df(kfolds, labels_df)

#     create_dataset_splits(dataset_path, folds_df, kfolds)
#     create_dataset_directories(images, labels, dataset_path, kfolds, folds_df)


# if __name__ == "__main__":
#     main()
