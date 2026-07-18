"""플랭크(PLANK) metric computation from a sequence of pose frames.

Plank is a static hold, not a repeated motion — there is no rep segmentation.
Instead this tracks how well the shoulder-hip-ankle line stayed straight over
the hold: positive offset means the hip sagged below the line, negative means
the hip piked above it.
"""
from __future__ import annotations

from typing import List

from .angles import line_deviation_deg
from .extractor import Frame
from .landmarks import (
    LEFT_ANKLE,
    LEFT_HIP,
    LEFT_SHOULDER,
    RIGHT_ANKLE,
    RIGHT_HIP,
    RIGHT_SHOULDER,
    pick_side,
)


def compute_metrics(frames: List[Frame]) -> dict:
    per_frame = [_frame_metrics(f) for f in frames]
    per_frame = [m for m in per_frame if m is not None]

    if not per_frame:
        return {
            "holdSec": 0.0,
            "maxHipSagDeg": 0.0,
            "maxHipPikeDeg": 0.0,
            "bodyLineDeviationSeries": [],
        }

    sag_values = [m["deviationDeg"] for m in per_frame if m["offset"] > 0]
    pike_values = [m["deviationDeg"] for m in per_frame if m["offset"] < 0]
    series = [
        {"sec": round(m["sec"], 2), "deg": round(m["deviationDeg"] if m["offset"] >= 0 else -m["deviationDeg"], 1)}
        for m in per_frame[:: max(1, len(per_frame) // 30 or 1)]
    ]

    return {
        "holdSec": round(per_frame[-1]["sec"] - per_frame[0]["sec"], 2),
        "maxHipSagDeg": round(max(sag_values), 1) if sag_values else 0.0,
        "maxHipPikeDeg": round(max(pike_values), 1) if pike_values else 0.0,
        "bodyLineDeviationSeries": series,
    }


def _frame_metrics(frame: Frame):
    shoulder = pick_side(frame.landmarks, LEFT_SHOULDER, RIGHT_SHOULDER)
    hip = pick_side(frame.landmarks, LEFT_HIP, RIGHT_HIP)
    ankle = pick_side(frame.landmarks, LEFT_ANKLE, RIGHT_ANKLE)

    if None in (shoulder, hip, ankle):
        return None

    return {
        "sec": frame.sec,
        "deviationDeg": line_deviation_deg(shoulder, hip, ankle),
        "offset": _hip_line_offset(shoulder, hip, ankle),
    }


def _hip_line_offset(shoulder, hip, ankle) -> float:
    """Signed vertical distance of hip from the shoulder-ankle line at hip's x.

    Positive means hip is below the line (sagging down); negative means hip
    is above it (piking up). Assumes y increases downward.
    """
    if ankle[0] == shoulder[0]:
        expected_y = (shoulder[1] + ankle[1]) / 2
    else:
        t = (hip[0] - shoulder[0]) / (ankle[0] - shoulder[0])
        expected_y = shoulder[1] + t * (ankle[1] - shoulder[1])
    return hip[1] - expected_y
