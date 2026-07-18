"""윗몸앞으로굽히기(SIT_AND_REACH) metric computation from a sequence of pose frames."""
from __future__ import annotations

from typing import List

from .angles import angle_deg, euclidean_distance
from .extractor import Frame
from .landmarks import (
    LEFT_ANKLE,
    LEFT_HIP,
    LEFT_KNEE,
    LEFT_SHOULDER,
    LEFT_WRIST,
    RIGHT_ANKLE,
    RIGHT_HIP,
    RIGHT_KNEE,
    RIGHT_SHOULDER,
    RIGHT_WRIST,
)

KNEE_BENT_THRESHOLD_DEG = 160.0
HOLD_VELOCITY_THRESHOLD = 0.01  # normalized-distance units per frame
BOUNCE_MIN_AMPLITUDE = 0.02  # normalized-distance units


def compute_metrics(frames: List[Frame]) -> dict:
    per_frame = [_frame_metrics(f) for f in frames]
    per_frame = [m for m in per_frame if m is not None]

    if not per_frame:
        return {
            "maxTrunkFlexionDeg": None,
            "minKneeAngleDeg": None,
            "kneeBentRatio": None,
            "bounceDetected": False,
            "holdSec": 0.0,
            "leftRightAsymmetryDeg": None,
        }

    trunk_flexions = [m["trunkFlexionDeg"] for m in per_frame]
    knee_angles = [m["kneeAngleDeg"] for m in per_frame]
    reach_distances = [m["reachDistance"] for m in per_frame]

    bent_count = sum(1 for a in knee_angles if a < KNEE_BENT_THRESHOLD_DEG)

    asymmetries = [m["asymmetryDeg"] for m in per_frame if m["asymmetryDeg"] is not None]

    return {
        "maxTrunkFlexionDeg": round(max(trunk_flexions), 1),
        "minKneeAngleDeg": round(min(knee_angles), 1),
        "kneeBentRatio": round(bent_count / len(knee_angles), 2),
        "bounceDetected": _detect_bounce(reach_distances),
        "holdSec": _hold_duration_sec(per_frame, reach_distances),
        "leftRightAsymmetryDeg": round(sum(asymmetries) / len(asymmetries), 1) if asymmetries else None,
    }


def _frame_metrics(frame: Frame):
    left_shoulder = frame.landmarks.get(LEFT_SHOULDER)
    right_shoulder = frame.landmarks.get(RIGHT_SHOULDER)
    left_hip = frame.landmarks.get(LEFT_HIP)
    right_hip = frame.landmarks.get(RIGHT_HIP)
    left_knee = frame.landmarks.get(LEFT_KNEE)
    right_knee = frame.landmarks.get(RIGHT_KNEE)
    left_ankle = frame.landmarks.get(LEFT_ANKLE)
    right_ankle = frame.landmarks.get(RIGHT_ANKLE)
    wrist = frame.landmarks.get(LEFT_WRIST) or frame.landmarks.get(RIGHT_WRIST)
    ankle = left_ankle or right_ankle

    required = (left_shoulder, right_shoulder, left_hip, right_hip, left_knee, right_knee, left_ankle, right_ankle)
    if any(p is None for p in required):
        return None

    left_knee_angle = angle_deg(left_hip, left_knee, left_ankle)
    right_knee_angle = angle_deg(right_hip, right_knee, right_ankle)
    knee_angle = min(left_knee_angle, right_knee_angle)

    left_trunk_flexion = 180.0 - angle_deg(left_shoulder, left_hip, left_knee)
    right_trunk_flexion = 180.0 - angle_deg(right_shoulder, right_hip, right_knee)
    trunk_flexion = max(left_trunk_flexion, right_trunk_flexion)

    reach_distance = euclidean_distance(wrist, ankle) if wrist is not None and ankle is not None else None

    return {
        "sec": frame.sec,
        "kneeAngleDeg": knee_angle,
        "trunkFlexionDeg": trunk_flexion,
        "reachDistance": reach_distance,
        "asymmetryDeg": abs(left_knee_angle - right_knee_angle),
    }


def _detect_bounce(reach_distances: List[float]) -> bool:
    values = [d for d in reach_distances if d is not None]
    if len(values) < 5:
        return False
    direction_changes = 0
    last_direction = 0
    for i in range(1, len(values)):
        delta = values[i] - values[i - 1]
        if abs(delta) < BOUNCE_MIN_AMPLITUDE / 5:
            continue
        direction = 1 if delta > 0 else -1
        if last_direction != 0 and direction != last_direction:
            direction_changes += 1
        last_direction = direction
    return direction_changes >= 3


def _hold_duration_sec(per_frame: List[dict], reach_distances: List[float]) -> float:
    values = [d for d in reach_distances if d is not None]
    if len(values) < 2:
        return 0.0
    min_distance = min(values)  # closest wrist-to-ankle distance == maximum reach
    hold_frames = []
    for i, m in enumerate(per_frame):
        d = m["reachDistance"]
        if d is None:
            continue
        if d - min_distance <= BOUNCE_MIN_AMPLITUDE:
            hold_frames.append(m["sec"])
    if not hold_frames:
        return 0.0
    return round(max(hold_frames) - min(hold_frames), 2)
