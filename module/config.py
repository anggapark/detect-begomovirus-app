import os
import torch

BATCH_SIZE = 32  # according to GPU memory
IMG_SIZE = 320  # input size 320x320 according to ssdlite320_mobilenet_v3_large model
NUM_EPOCHS = 4  # number of epochs to train
# NUM_WORKERS = os.cpu_count()  # equal to number of cpu
NUM_WORKERS = 2

# use cuda if cuda is available, else use CPU
DEVICE = torch.device("cuda") if torch.cuda.is_available() else torch.device("cpu")

# define dataset path
TRAIN_DIR = "data/train"
VAL_DIR = "data/valid"
TEST_DIR = "data/test"

CLASSES = ["positif_begomovirus", "negatif_begomovirus"]

NUM_CLASSES = len(CLASSES)

VISUALIZE_TRANSFORMED_IMGS = False

# location to save training outputs
OUTPUT_DIR = "outputs"
