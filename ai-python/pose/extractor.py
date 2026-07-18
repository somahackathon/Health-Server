"""Frame sampling and MediaPipe Pose landmark extraction."""
from __future__ import annotations

from dataclasses import dataclass
from typing import Dict, List, Tuple


class NoPersonDetectedError(Exception):
    pass


class VideoDecodeError(Exception):
    pass


@dataclass
class Frame:
    sec: float
    # landmark index -> (x, y, visibility), normalized image coordinates
    landmarks: Dict[int, Tuple[float, float, float]]


def extract_frames(video_path: str, sample_fps: int = 10) -> Tuple[List[Frame], float]:
    """Sample a video at roughly `sample_fps` and run MediaPipe Pose per frame.

    Returns (frames, duration_sec). Raises VideoDecodeError if the file can't
    be opened/decoded, NoPersonDetectedError if no frame yields a person.
    """
    try:
        import cv2
        import mediapipe as mp
    except ImportError as e:  # pragma: no cover - environment guard
        raise RuntimeError("opencv-python and mediapipe must be installed") from e

    capture = cv2.VideoCapture(video_path)
    if not capture.isOpened():
        raise VideoDecodeError("could not open video file")

    source_fps = capture.get(cv2.CAP_PROP_FPS) or 30.0
    frame_count = capture.get(cv2.CAP_PROP_FRAME_COUNT) or 0
    duration_sec = (frame_count / source_fps) if source_fps > 0 else 0.0
    step = max(1, round(source_fps / sample_fps))

    frames: List[Frame] = []
    pose = mp.solutions.pose.Pose(static_image_mode=False, model_complexity=1)
    try:
        index = 0
        while True:
            ok, image = capture.read()
            if not ok:
                break
            if index % step == 0:
                result = pose.process(_to_rgb(image, cv2))
                if result.pose_landmarks is not None:
                    landmarks = {
                        i: (lm.x, lm.y, lm.visibility)
                        for i, lm in enumerate(result.pose_landmarks.landmark)
                    }
                    frames.append(Frame(sec=index / source_fps, landmarks=landmarks))
            index += 1
    finally:
        pose.close()
        capture.release()

    if not frames:
        raise NoPersonDetectedError("no person detected in any sampled frame")

    return frames, duration_sec


def _to_rgb(image, cv2):
    return cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
