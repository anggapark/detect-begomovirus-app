import torch
import cv2
import numpy as np
import os
import glob as glob

from xml.etree import ElementTree as ET
from torch.utils.data import Dataset, DataLoader
from .config import CLASSES, IMG_SIZE, TRAIN_DIR, BATCH_SIZE
from .utils import collate_fn, get_transform


# The dataset class.
class CustomDataset(Dataset):
    def __init__(self, dir_path, width, height, classes, transforms=None):
        self.transforms = transforms
        self.dir_path = dir_path
        self.height = height
        self.width = width
        self.classes = classes
        self.image_file_types = ["*.jpg", "*.jpeg", "*.png", "*.ppm", "*.JPG"]
        self.all_image_paths = []

        # get all the image path in sorted order
        for file_type in self.image_file_types:
            if file_type in self.image_file_types:
                self.all_image_paths.extend(
                    glob.glob(os.path.join(self.dir_path, file_type))
                )

        self.all_images = [
            image_path.split(os.path.sep)[-1] for image_path in self.all_image_paths
        ]
        self.all_images = sorted(self.all_images)

    def __len__(self):
        return len(self.all_images)

    def __getitem__(self, idx):
        # get the image name and its full path
        image_name = self.all_images[idx]
        image_path = os.path.join(self.dir_path, image_name)

        # Read and preprocess the image.
        image = cv2.imread(image_path)
        image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB).astype(np.float32)
        image_resized = cv2.resize(image, (self.width, self.height))
        image_resized /= 255.0

        # get corresponding XML file for the annotatino
        annot_filename = os.path.splitext(image_name)[0] + ".xml"
        annot_file_path = os.path.join(self.dir_path, annot_filename)

        boxes = []
        labels = []
        tree = ET.parse(annot_file_path)
        root = tree.getroot()

        # original image width and height
        image_width = image.shape[1]
        image_height = image.shape[0]

        # box coordinates for xml files are extracted
        #   and corrected for given image size
        for member in root.findall("object"):
            # get label and map the 'classes'
            labels.append(self.classes.index(member.find("name").text))

            # Left corner x-coordinates.
            xmin = int(member.find("bndbox").find("xmin").text)
            # Right corner x-coordinates.
            xmax = int(member.find("bndbox").find("xmax").text)
            # Left corner y-coordinates.
            ymin = int(member.find("bndbox").find("ymin").text)
            # Right corner y-coordinates.
            ymax = int(member.find("bndbox").find("ymax").text)

            # Resize the bounding boxes according
            #   to resized image 'width' and 'height'
            xmin_final = (xmin / image_width) * self.width
            xmax_final = (xmax / image_width) * self.width
            ymin_final = (ymin / image_height) * self.height
            ymax_final = (ymax / image_height) * self.height

            # check that all coordinates are within the image
            if xmax_final > self.width:
                xmax_final = self.width
            if ymax_final > self.height:
                ymax_final = self.height

            boxes.append([xmin_final, ymin_final, xmax_final, ymax_final])

        # bounding bo to tensor
        boxes = torch.as_tensor(boxes, dtype=torch.float32)

        # area of bounding box
        area = (
            (boxes[:, 3] - boxes[:, 1]) * (boxes[:, 2] - boxes[:, 0])
            if len(boxes) > 0
            else torch.as_tensor(boxes, dtype=torch.float32)
        )

        # no crowd instances
        iscrowd = torch.zeros((boxes.shape[0],), dtype=torch.int64)
        # labels to tensor
        labels = torch.as_tensor(labels, dtype=torch.int64)

        # prepare the final target dictionary
        target = {}
        target["boxes"] = boxes
        target["labels"] = labels
        target["area"] = area
        target["iscrowd"] = iscrowd

        image_id = torch.tensor([idx])
        target["image_id"] = image_id

        # apply the image transforms
        if self.transforms:
            sample = self.transforms(
                image=image_resized, bboxes=target["boxes"], labels=labels
            )
            image_resized = sample["image"]
            target["boxes"] = torch.Tensor(sample["bboxes"])

        if np.isnan((target["boxes"]).numpy()).any() or target[
            "boxes"
        ].shape == torch.Size([0]):
            target["boxes"] = torch.zeros((0, 4), dtype=torch.int64)

        return image_resized, target


# prepare the final datasets and data loaders
def create_train_dataset(DIR):
    train_dataset = CustomDataset(DIR, IMG_SIZE, IMG_SIZE, CLASSES, get_transform())

    return train_dataset


def create_valid_dataset(DIR):
    valid_dataset = CustomDataset(DIR, IMG_SIZE, IMG_SIZE, CLASSES, get_transform())

    return valid_dataset


def create_train_loader(train_dataset, num_workers=0):
    train_loader = DataLoader(
        train_dataset,
        batch_size=BATCH_SIZE,
        shuffle=True,
        num_workers=num_workers,
        collate_fn=collate_fn,
        drop_last=True,
    )

    return train_loader


def create_valid_loader(valid_dataset, num_workers=0):
    valid_loader = DataLoader(
        valid_dataset,
        batch_size=BATCH_SIZE,
        shuffle=False,
        num_workers=num_workers,
        collate_fn=collate_fn,
        drop_last=True,
    )

    return valid_loader


# Execute 'datasetl.py' using python command in terminal
# USE: python dataset.py
if __name__ == "__main__":
    # sanity check of the dataset pipeline with sample visualization
    dataset = CustomDataset(TRAIN_DIR, IMG_SIZE, IMG_SIZE, CLASSES)
    print(f"Number of training images: {len(dataset)}")

    # visualize sample image
    def visualize_sample(image, target):
        # Define a color map for different classes
        class_colors = [
            (255, 0, 0),
            (0, 0, 255),
        ]

        for box_num in range(len(target["boxes"])):
            box = target["boxes"][box_num]
            label = CLASSES[target["labels"][box_num]]
            color = class_colors[target["labels"][box_num]]

            # image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)
            cv2.rectangle(
                image,
                (int(box[0]), int(box[1])),
                (int(box[2]), int(box[3])),
                color,
                2,
            )
            cv2.putText(
                image,
                label,
                (int(box[0]), int(box[1] - 5)),
                cv2.FONT_HERSHEY_SIMPLEX,
                0.4,
                color,
                2,
            )

        cv2.imshow("Image", image[..., ::-1].copy())
        # cv2.imshow("Image", image)
        cv2.waitKey(0)

    # Example usage
    NUM_SAMPLES_TO_VISUALIZE = 10
    for i in range(NUM_SAMPLES_TO_VISUALIZE):
        image, target = dataset[i]
        visualize_sample(image, target)
