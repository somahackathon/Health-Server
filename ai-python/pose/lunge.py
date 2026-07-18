"""런지(LUNGE) metric computation from a sequence of pose frames.

Tracks whichever leg is more bent in a given frame as the "front" (lunging)
leg — a 2D side-view pose can't reliably tell front from back by left/right
alone, but the front leg is the one that bends during the down phase.
"""
from __future__ import annotations

from typing import List

from .angles import angle_deg, vertical_deviation_deg
from .extractor import Frame
from .landmarks import (
    LEFT_ANKLE,
    LEFT_HIP,
    LEFT_KNEE,
    LEFT_SHOULDER,
    RIGHT_ANKLE,
    RIGHT_HIP,
    RIGHT_KNEE,
    RIGHT_SHOULDER,
)

DOWN_PHASE_KNEE_ANGLE_DEG = 140.0
MIN_REP_GAP_SEC = 0.5


def compute_metrics(frames: List[Frame]) -> dict:
    per_frame = [_frame_metrics(f) for f in frames]
    per_frame = [m for m in per_frame if m is not None]
    reps = _segment_reps(per_frame)
    return {
        "repCount": len(reps),
        "reps": reps,
    }


def _frame_metrics(frame: Frame):
    left_hip = frame.landmarks.get(LEFT_HIP)
    right_hip = frame.landmarks.get(RIGHT_HIP)
    left_knee = frame.landmarks.get(LEFT_KNEE)
    right_knee = frame.landmarks.get(RIGHT_KNEE)
    left_ankle = frame.landmarks.get(LEFT_ANKLE)
    right_ankle = frame.landmarks.get(RIGHT_ANKLE)
    shoulder = frame.landmarks.get(LEFT_SHOULDER) or frame.landmarks.get(RIGHT_SHOULDER)
    hip = left_hip or right_hip

    required = (left_hip, right_hip, left_knee, right_knee, left_ankle, right_ankle, shoulder)
    if any(p is None for p in required):
        return None

    left_knee_angle = angle_deg(left_hip, left_knee, left_ankle)
    right_knee_angle = angle_deg(right_hip, right_knee, right_ankle)
    front_knee_angle = min(left_knee_angle, right_knee_angle)
    trunk_lean_deg = vertical_deviation_deg(hip, shoulder)

    return {"sec": frame.sec, "frontKneeAngleDeg": front_knee_angle, "trunkLeanDeg": trunk_lean_deg}


def _segment_reps(per_frame: List[dict]) -> List[dict]:
    reps: List[dict] = []
    in_down_phase = False
    current: List[dict] = []

    for m in per_frame:
        if m["frontKneeAngleDeg"] <= DOWN_PHASE_KNEE_ANGLE_DEG:
            if not in_down_phase:
                in_down_phase = True
                current = []
            current.append(m)
        else:
            if in_down_phase and current:
                reps.append(_summarize_rep(current))
            in_down_phase = False
            current = []

    if in_down_phase and current:
        reps.append(_summarize_rep(current))

    merged: List[dict] = []
    for rep in reps:
        if merged and rep["startSec"] - merged[-1]["endSec"] < MIN_REP_GAP_SEC:
            merged[-1] = _merge_reps(merged[-1], rep)
        else:
            merged.append(rep)
    return merged


def _summarize_rep(frames: List[dict]) -> dict:
    return {
        "startSec": round(frames[0]["sec"], 2),
        "endSec": round(frames[-1]["sec"], 2),
        "minFrontKneeAngleDeg": round(min(f["frontKneeAngleDeg"] for f in frames), 1),
        "maxTrunkLeanDeg": round(max(f["trunkLeanDeg"] for f in frames), 1),
    }


def _merge_reps(a: dict, b: dict) -> dict:
    return {
        "startSec": a["startSec"],
        "endSec": b["endSec"],
        "minFrontKneeAngleDeg": min(a["minFrontKneeAngleDeg"], b["minFrontKneeAngleDeg"]),
        "maxTrunkLeanDeg": max(a["maxTrunkLeanDeg"], b["maxTrunkLeanDeg"]),
    }
