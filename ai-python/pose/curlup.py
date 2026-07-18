"""윗몸일으키기(CURL_UP) metric computation from a sequence of pose frames."""
from __future__ import annotations

from typing import List

from .angles import angle_deg, line_deviation_deg
from .extractor import Frame
from .landmarks import (
    LEFT_ANKLE,
    LEFT_EAR,
    LEFT_HIP,
    LEFT_KNEE,
    LEFT_SHOULDER,
    RIGHT_ANKLE,
    RIGHT_EAR,
    RIGHT_HIP,
    RIGHT_KNEE,
    RIGHT_SHOULDER,
    pick_side,
)

UP_PHASE_TRUNK_LIFT_DEG = 30.0
MIN_REP_GAP_SEC = 0.5
KNEE_ANGLE_TARGET_DEG = 90.0
KNEE_ANGLE_STABLE_TOLERANCE_DEG = 20.0


def compute_metrics(frames: List[Frame]) -> dict:
    per_frame = [_frame_metrics(f) for f in frames]
    per_frame = [m for m in per_frame if m is not None]

    reps = _segment_reps(per_frame)

    knee_angles = [m["kneeAngleDeg"] for m in per_frame]
    stable_count = sum(
        1 for a in knee_angles if abs(a - KNEE_ANGLE_TARGET_DEG) <= KNEE_ANGLE_STABLE_TOLERANCE_DEG
    )

    return {
        "repCount": len(reps),
        "reps": reps,
        "kneeAngleStableRatio": round(stable_count / len(knee_angles), 2) if knee_angles else None,
    }


def _frame_metrics(frame: Frame):
    shoulder = pick_side(frame.landmarks, LEFT_SHOULDER, RIGHT_SHOULDER)
    hip = pick_side(frame.landmarks, LEFT_HIP, RIGHT_HIP)
    knee = pick_side(frame.landmarks, LEFT_KNEE, RIGHT_KNEE)
    ankle = pick_side(frame.landmarks, LEFT_ANKLE, RIGHT_ANKLE)
    ear = pick_side(frame.landmarks, LEFT_EAR, RIGHT_EAR)

    if None in (shoulder, hip, knee, ankle):
        return None

    knee_angle = angle_deg(hip, knee, ankle)
    trunk_lift_deg = 180.0 - angle_deg(shoulder, hip, knee)
    neck_pull_deg = line_deviation_deg(ear, shoulder, hip) if ear is not None else None

    return {
        "sec": frame.sec,
        "kneeAngleDeg": knee_angle,
        "trunkLiftDeg": trunk_lift_deg,
        "neckPullDeg": neck_pull_deg,
    }


def _segment_reps(per_frame: List[dict]) -> List[dict]:
    reps: List[dict] = []
    in_up_phase = False
    current: List[dict] = []

    for m in per_frame:
        if m["trunkLiftDeg"] >= UP_PHASE_TRUNK_LIFT_DEG:
            if not in_up_phase:
                in_up_phase = True
                current = []
            current.append(m)
        else:
            if in_up_phase and current:
                reps.append(_summarize_rep(current))
            in_up_phase = False
            current = []

    if in_up_phase and current:
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
    neck_values = [f["neckPullDeg"] for f in frames if f["neckPullDeg"] is not None]
    return {
        "startSec": round(frames[0]["sec"], 2),
        "endSec": round(frames[-1]["sec"], 2),
        "maxTrunkLiftDeg": round(max(f["trunkLiftDeg"] for f in frames), 1),
        "minKneeAngleDeg": round(min(f["kneeAngleDeg"] for f in frames), 1),
        "maxNeckPullDeg": round(max(neck_values), 1) if neck_values else None,
    }


def _merge_reps(a: dict, b: dict) -> dict:
    return {
        "startSec": a["startSec"],
        "endSec": b["endSec"],
        "maxTrunkLiftDeg": max(a["maxTrunkLiftDeg"], b["maxTrunkLiftDeg"]),
        "minKneeAngleDeg": min(a["minKneeAngleDeg"], b["minKneeAngleDeg"]),
        "maxNeckPullDeg": _max_ignore_none(a["maxNeckPullDeg"], b["maxNeckPullDeg"]),
    }


def _max_ignore_none(a, b):
    values = [v for v in (a, b) if v is not None]
    return max(values) if values else None
