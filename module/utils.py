import numpy as numpy
import matplotlib.pyplot as plt
import albumentations as A
import cv2
import torch

from albumentations.pytorch import ToTensorV2

# from config import DEVICE, CLASSES

plt.style.use("ggplot")


class Averager:
    """
    keep track of the training and validation loss values
    and helps to get the average for each epochs
    """

    # define initial values and iterations
    def __init__(self):
        self.current_total = 0.0
        self.iterations = 0.0

    def send(self, value):
        self.current_total += value
        self.iterations += 1

    @property
    def value(self):
        if self.iterations == 0:
            return 0
        else:
            return 1.0 * self.current_total / self.iterations

    def reset(self):
        self.current_total = 0.0
        self.iterations = 0.0


class SaveBestModel:
    """
    Save the best model while training. If the current epoch's
    validation mAP @0.5:0.95 IoU higher than the previous highest,
    then save the model
    """

    def __init__(self, best_valid_map=float(0)):
        self.best_valid_map = best_valid_map

    def __call__(self, model, current_valid_map, epoch, OUTPUT_DIR):
        if current_valid_map > self.best_valid_map:
            self.best_valid_map = current_valid_map
            print(f"\nBest Validation mAP: {self.best_valid_map}\n")
            print(f"Saving Best Model for Epoch: {epoch + 1}")
            torch.save(
                {
                    "epoch": epoch + 1,
                    "model_state_dict": model.state_dict(),
                },
                f"{OUTPUT_DIR}/best_model.pth",
            )


def collate_fn(batch):
    """
    Handle the data loading as different images may have
    different numbers of objects and to handle varying size tensors
    """
    return tuple(zip(*batch))


def get_transform():
    return A.Compose(
        [
            ToTensorV2(p=1.0),
        ],
        bbox_params={"format": "pascal_voc", "label_fields": ["labels"]},
    )


def save_model(epoch, model, optimizer):
    """
    save the trained model until current epochs

    Args:
        epochs (int): path to save the graphs
        model: weight model after training
        optimizer: optimizer used for model training
    """
    torch.save(
        {
            "epoch": epoch + 1,
            "model_state_dict": model.state_dict(),
            "optimizer_state_dict": optimizer.state_dict(),
        },
        "outputs/model.pth",
    )


def save_loss_plot(
    OUTPUT_DIR,
    train_loss_list,
    x_label="iterations",
    y_label="train_loss",
    save_name="train_loss",
):
    """
    save both train loss graph

    Args:
        OUTPUT_DIR: Path to save the graphs.
        train_loss_list: List containing the training loss values.
    """
    fig_1 = plt.figure(figsize=(10, 7), num=1, clear=True)
    train_ax = fig_1.add_subplot()
    train_ax.plot(train_loss_list, color="tab:blue")
    train_ax.set_xlabel(x_label)
    train_ax.set_ylabel(y_label)
    fig_1.savefig(f"{OUTPUT_DIR}/{save_name}")
    print("Saving Plots Complete...")


def save_precision(OUTPUT_DIR):
    pass


def save_recall(OUTPUT_DIR):
    pass


def save_mAP(OUTPUT_DIR, mAP_05, mAP_05_095):
    """
    save the mAP@0.5 and mAP@0.5:0.95 per epochs.

    Args:
        OUTPUT_DIR (str): path to save the graphs
        mAP_05 (list): list containing mAP values at 0.5 IoU
        mAP (list): list containing mAP values at 0.5:0.95 IoU
    """
    fig = plt.figure(figsize=(10, 7), num=1, clear=True)
    ax = fig.add_subplot()

    ax.plot(mAP_05, color="tab:orange", linestyle="-", label="mAP@0.5")
    ax.plot(mAP_05_095, color="tab:red", linestyle="-", label="mAP@0.5:0.95")
    ax.set_xlabel("Epochs")
    ax.set_ylabel("mAP")
    ax.legend()
    fig.savefig(f"{OUTPUT_DIR}/mAP.png")
