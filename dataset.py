import os
from dotenv import dotenv_values, load_dotenv
from roboflow import Roboflow


def get_dataset(version=1):
    """_summary_"""

    load_dotenv()
    api_key = os.environ.get("API_KEY")
    workspace = os.environ.get("WORKSPACE")

    rf = Roboflow(api_key=api_key)
    project = rf.workspace(workspace).project("dataset-vnozn")
    version = project.version(version)
    dataset = version.download("yolov8")

    return dataset


if __name__ == "__main__":
    get_dataset(version=1)
