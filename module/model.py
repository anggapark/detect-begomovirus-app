import torch
import torchvision

from torchvision.models.detection.ssd import SSDClassificationHead
from torchvision.models.detection import (
    _utils,
    ssdlite320_mobilenet_v3_large,
    SSDLite320_MobileNet_V3_Large_Weights,
)


def create_model(num_classes=2, size=320):
    # load torchvision pretrained model
    model = torchvision.models.detection.ssdlite320_mobilenet_v3_large(
        weights=SSDLite320_MobileNet_V3_Large_Weights.DEFAULT
    )

    # Retrieve the list of input channel
    in_channel = _utils.retrieve_out_channels(model.backbone, (size, size))

    # list containing number of anchors based on aspect ratio
    num_anchors = model.anchor_generator.num_anchors_per_location()

    # classification head
    model.head.classification_head = SSDClassificationHead(
        in_channels=in_channel, num_anchors=num_anchors, num_classes=num_classes
    )

    # images size to transform
    model.transform.min_size = (size,)
    model.transform.max_size = size

    print(model)

    # total params and trainable params
    total_params = sum(p.numel() for p in model.parameters())
    print(f"{total_params:,} total parameters")

    total_trainable_params = sum(
        p.numel() for p in model.parameters() if p.requires_grad
    )
    print(f"{total_trainable_params:,} training parameters")

    return model


if __name__ == "__main__":
    model = create_model(2, 320)
