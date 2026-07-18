"""MediaPipe Pose landmark index constants (subset used by this app)."""

NOSE = 0
LEFT_EAR = 7
RIGHT_EAR = 8
LEFT_SHOULDER = 11
RIGHT_SHOULDER = 12
LEFT_ELBOW = 13
RIGHT_ELBOW = 14
LEFT_WRIST = 15
RIGHT_WRIST = 16
LEFT_HIP = 23
RIGHT_HIP = 24
LEFT_KNEE = 25
RIGHT_KNEE = 26
LEFT_ANKLE = 27
RIGHT_ANKLE = 28

MIN_VISIBILITY = 0.5


def pick_side(landmarks: dict, left_idx: int, right_idx: int):
    """Pick whichever side (left/right landmark) has higher visibility."""
    left = landmarks.get(left_idx)
    right = landmarks.get(right_idx)
    if left is None and right is None:
        return None
    if left is None:
        return right
    if right is None:
        return left
    return left if left[2] >= right[2] else right
