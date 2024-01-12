from module.config import (
    DEVICE,
    NUM_CLASSES,
    NUM_EPOCHS,
    OUTPUT_DIR,
    NUM_WORKERS,
    IMG_SIZE,
    TRAIN_DIR,
    VAL_DIR,
)
from module.model import create_model
from module.utils import (
    Averager,
    SaveBestModel,
    save_model,
    save_loss_plot,
    save_mAP,
)
from tqdm.auto import tqdm
from module.datasets import (
    create_train_dataset,
    create_valid_dataset,
    create_train_loader,
    create_valid_loader,
)

import os
import time
import numpy as np
import matplotlib.pyplot as plt
import torch
from torch.optim.lr_scheduler import StepLR

from torchmetrics import Precision, Recall
from torchmetrics.classification import BinaryPrecision, BinaryRecall
from torchmetrics.detection.mean_ap import MeanAveragePrecision


plt.style.use("ggplot")

seed = 42
torch.manual_seed(seed)
torch.cuda.manual_seed(seed)


# running training iteration
def train_step(train_loader, model, train_loss_hist):
    """
    Train a pytorch model for a single epoch

    Turns a target PyTorch model to training mode and then
    runs through all of the required training steps (forward
    pass, loss calculation, optimizer step).

    Args:
        train_loader (torch.utils.data.DataLoader): _description_
        model (torch.nn.Module): _description_

    Returns:
        _type_: _description_
    """
    print("Training!")
    model.train()

    # initialize progress bar
    progress_bar = tqdm(train_loader, total=len(train_loader))

    for i, data in enumerate(progress_bar):
        images, targets = data

        # send data to target device
        images = list(image.to(DEVICE) for image in images)
        targets = [{k: v.to(DEVICE) for k, v in t.items()} for t in targets]

        # forward pass
        loss_dict = model(images, targets)

        # calculate and accumulate loss
        losses = sum(loss for loss in loss_dict.values())
        loss_value = losses.item()

        train_loss_hist.send(loss_value)

        # zero grad
        optimizer.zero_grad()

        # loss backward
        losses.backward()

        # optimizer step
        optimizer.step()

        # update the loss value beside the progress bar for each iter
        progress_bar.set_description(desc=f"Loss: {loss_value:.4f}")

    return loss_value


# running validation iterations
def validate_step(valid_loader, model):
    """
    Validate pytorch model for a single epoch

    Turns a target PyTorch model to "eval" mode and
    then performs a forward pass on a validating dataset.

    Args:
        valid_loader (Dataloader): A valid dataloader instance for the model to be validate on
        model (Module): A pytorch model to be validate

    Returns:
        Dict: A dictionary of metric summary consist of prediction results and ground truth
    """
    print("Validating!")
    model.eval()

    # initialize progress bar
    progress_bar = tqdm(valid_loader, total=len(valid_loader))
    target = []
    preds = []

    for i, data in enumerate(progress_bar):
        images, targets = data

        # send data to device
        images = list(image.to(DEVICE) for image in images)
        targets = [{k: v.to(DEVICE) for k, v in t.items()} for t in targets]

        with torch.inference_mode():
            outputs = model(images, targets)

            ## mAP calculation using Torchmatrics
            for i in range(len(images)):
                true_dict = dict()
                preds_dict = dict()

                true_dict["boxes"] = targets[i]["boxes"].detach().cpu()
                true_dict["labels"] = targets[i]["labels"].detach().cpu()
                preds_dict["boxes"] = outputs[i]["boxes"].detach().cpu()
                preds_dict["scores"] = outputs[i]["scores"].detach().cpu()
                preds_dict["labels"] = outputs[i]["labels"].detach().cpu()
                preds.append(preds_dict)
                target.append(true_dict)

    metric = MeanAveragePrecision()
    metric.update(preds, target)
    metric_summary = metric.compute()

    return metric_summary


def train(model, train_loader, val_loader, optimizer):
    """
    Train and test a PyTorch model

    Passed a target model through train_step and validation_stap
    functions for the number of epochs, training and validating
    the model in the same loop.

    Calculated

    Args:
        model (Module): _description_
        train_loader (Dataloader): _description_
        val_loader (Dataloader): _description_
        optimizer (Optimizer): _description_
    """
    # monitor training loss
    train_loss_hist = Averager()

    # store training loss and mAP values
    train_loss_list = []
    map_50_list = []
    map_list = []

    # initiate precision and recall
    precision = Precision(num_class=NUM_CLASSES)

    # name the model
    MODEL_NAME = "model"

    # save best model
    save_best_model = SaveBestModel()

    # train loop
    for epoch in range(NUM_EPOCHS):
        print(f"\nEpoch {epoch+1} of {NUM_EPOCHS}")

        # reset the training loss histories for the current epoch
        train_loss_hist.reset()

        # start timer and carry out training and validation
        start = time.time()
        train_loss = train_step(train_loader, model, train_loss_hist)
        metric_summary = validate_step(val_loader, model)

        print(
            f"Epoch #{epoch+1}\ntrain loss: {train_loss_hist.value:.3f} | mAP: {metric_summary['map']}"
        )

        # end timer and show training duration
        end = time.time()
        print(f"Took {((end - start) / 60):.3f} minutes for epoch {epoch}")

        # collect train result
        train_loss_list.append(train_loss)
        map_50_list.append(metric_summary["map_50"])
        map_list.append(metric_summary["map"])

        # save the best model
        save_best_model(model, float(metric_summary["map"]), epoch, "outputs")
        # save the current epoch model
        save_model(epoch, model, optimizer)
        # save loss plot
        save_loss_plot(OUTPUT_DIR, train_loss_list)
        # save mAP plot
        save_mAP(OUTPUT_DIR, map_50_list, map_list)
        scheduler.step()


if __name__ == "__main__":
    os.makedirs("outputs", exist_ok=True)

    train_dataset = create_train_dataset(TRAIN_DIR)
    val_dataset = create_valid_dataset(VAL_DIR)
    train_loader = create_train_loader(train_dataset, NUM_WORKERS)
    val_loader = create_valid_loader(val_dataset, NUM_WORKERS)

    print(f"Number of training samples: {len(train_dataset)}")
    print(f"Number of validation samples: {len(val_dataset)}")

    # initialize the model
    model = create_model(num_classes=NUM_CLASSES, size=IMG_SIZE)
    model = model.to(DEVICE)

    params = [p for p in model.parameters() if p.requires_grad]

    optimizer = torch.optim.SGD(params, lr=0.001, momentum=0.9, nesterov=True)
    scheduler = StepLR(optimizer=optimizer, step_size=15, gamma=0.1, verbose=True)

    # train model
    train(model, train_loader, val_loader, optimizer)
