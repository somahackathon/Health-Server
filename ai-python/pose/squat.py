"""스쿼트(SQUAT) metric computation from a sequence of pose frames."""
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
    pick_side,
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
    shoulder = pick_side(frame.landmarks, LEFT_SHOULDER, RIGHT_SHOULDER)
    hip = pick_side(frame.landmarks, LEFT_HIP, RIGHT_HIP)
    knee = pick_side(frame.landmarks, LEFT_KNEE, RIGHT_KNEE)
    ankle = pick_side(frame.landmarks, LEFT_ANKLE, RIGHT_ANKLE)

    if None in (shoulder, hip, knee, ankle):
        return None

    knee_angle = angle_deg(hip, knee, ankle)
    trunk_lean_deg = vertical_deviation_deg(hip, shoulder)

    return {"sec": frame.sec, "kneeAngleDeg": knee_angle, "trunkLeanDeg": trunk_lean_deg}


def _segment_reps(per_frame: List[dict]) -> List[dict]:
    reps: List[dict] = []
    in_down_phase = False
    current: List[dict] = []

    for m in per_frame:
        if m["kneeAngleDeg"] <= DOWN_PHASE_KNEE_ANGLE_DEG:
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
        "minKneeAngleDeg": round(min(f["kneeAngleDeg"] for f in frames), 1),
        "maxTrunkLeanDeg": round(max(f["trunkLeanDeg"] for f in frames), 1),
    }


def _merge_reps(a: dict, b: dict) -> dict:
    return {
        "startSec": a["startSec"],
        "endSec": b["endSec"],
        "minKneeAngleDeg": min(a["minKneeAngleDeg"], b["minKneeAngleDeg"]),
        "maxTrunkLeanDeg": max(a["maxTrunkLeanDeg"], b["maxTrunkLeanDeg"]),
    }
