"""Push-up (팔굽혀펴기) metric computation from a sequence of pose frames."""
from __future__ import annotations

from typing import List

from .angles import angle_deg, line_deviation_deg
from .extractor import Frame
from .landmarks import (
    LEFT_ANKLE,
    LEFT_EAR,
    LEFT_ELBOW,
    LEFT_HIP,
    LEFT_SHOULDER,
    LEFT_WRIST,
    RIGHT_ANKLE,
    RIGHT_EAR,
    RIGHT_ELBOW,
    RIGHT_HIP,
    RIGHT_SHOULDER,
    RIGHT_WRIST,
    pick_side,
)

DOWN_PHASE_ELBOW_ANGLE_DEG = 140.0
NECK_ALIGNED_DEVIATION_DEG = 15.0
MIN_REP_GAP_SEC = 0.8


def compute_metrics(frames: List[Frame]) -> dict:
    per_frame = [_frame_metrics(f) for f in frames]
    per_frame = [m for m in per_frame if m is not None]

    reps = _segment_reps(per_frame)
    body_line_series = [
        {"sec": round(m["sec"], 2), "deg": round(m["bodyLineDeviationDeg"], 1)}
        for m in per_frame[:: max(1, len(per_frame) // 30 or 1)]
    ]

    return {
        "repCount": len(reps),
        "reps": reps,
        "bodyLineDeviationSeries": body_line_series,
    }


def _frame_metrics(frame: Frame):
    shoulder = pick_side(frame.landmarks, LEFT_SHOULDER, RIGHT_SHOULDER)
    elbow = pick_side(frame.landmarks, LEFT_ELBOW, RIGHT_ELBOW)
    wrist = pick_side(frame.landmarks, LEFT_WRIST, RIGHT_WRIST)
    hip = pick_side(frame.landmarks, LEFT_HIP, RIGHT_HIP)
    ankle = pick_side(frame.landmarks, LEFT_ANKLE, RIGHT_ANKLE)
    ear = pick_side(frame.landmarks, LEFT_EAR, RIGHT_EAR)

    if None in (shoulder, elbow, wrist, hip, ankle):
        return None

    elbow_angle = angle_deg(shoulder, elbow, wrist)
    body_line_deviation = line_deviation_deg(shoulder, hip, ankle)
    neck_deviation = line_deviation_deg(ear, shoulder, hip) if ear is not None else None

    return {
        "sec": frame.sec,
        "elbowAngleDeg": elbow_angle,
        "bodyLineDeviationDeg": body_line_deviation,
        "neckDeviationDeg": neck_deviation,
    }


def _segment_reps(per_frame: List[dict]) -> List[dict]:
    reps: List[dict] = []
    in_down_phase = False
    current: List[dict] = []

    for m in per_frame:
        if m["elbowAngleDeg"] <= DOWN_PHASE_ELBOW_ANGLE_DEG:
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

    # Merge reps that are too close together in time (noise splitting one rep in two)
    merged: List[dict] = []
    for rep in reps:
        if merged and rep["startSec"] - merged[-1]["endSec"] < MIN_REP_GAP_SEC:
            merged[-1] = _merge_reps(merged[-1], rep)
        else:
            merged.append(rep)
    return merged


def _summarize_rep(frames: List[dict]) -> dict:
    neck_values = [f["neckDeviationDeg"] for f in frames if f["neckDeviationDeg"] is not None]
    aligned = [v for v in neck_values if v <= NECK_ALIGNED_DEVIATION_DEG]
    return {
        "startSec": round(frames[0]["sec"], 2),
        "endSec": round(frames[-1]["sec"], 2),
        "minElbowAngleDeg": round(min(f["elbowAngleDeg"] for f in frames), 1),
        "maxHipSagDeg": round(max(f["bodyLineDeviationDeg"] for f in frames), 1),
        "neckAlignedRatio": round(len(aligned) / len(neck_values), 2) if neck_values else None,
    }


def _merge_reps(a: dict, b: dict) -> dict:
    return {
        "startSec": a["startSec"],
        "endSec": b["endSec"],
        "minElbowAngleDeg": min(a["minElbowAngleDeg"], b["minElbowAngleDeg"]),
        "maxHipSagDeg": max(a["maxHipSagDeg"], b["maxHipSagDeg"]),
        "neckAlignedRatio": _avg_ignore_none(a["neckAlignedRatio"], b["neckAlignedRatio"]),
    }


def _avg_ignore_none(a, b):
    values = [v for v in (a, b) if v is not None]
    return round(sum(values) / len(values), 2) if values else None
