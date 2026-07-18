"""Geometry helpers for joint-angle and alignment calculations.

All landmark points are expected as (x, y) tuples in normalized image
coordinates (as returned by MediaPipe Pose), or (x, y, z) — only x, y are
used here since posture judgments in this app are 2D-plane based.
"""
from __future__ import annotations

import math
from typing import Sequence


def angle_deg(a: Sequence[float], b: Sequence[float], c: Sequence[float]) -> float:
    """Angle at vertex b formed by points a-b-c, in degrees (0-180)."""
    ax, ay = a[0] - b[0], a[1] - b[1]
    cx, cy = c[0] - b[0], c[1] - b[1]
    dot = ax * cx + ay * cy
    mag_a = math.hypot(ax, ay)
    mag_c = math.hypot(cx, cy)
    if mag_a == 0 or mag_c == 0:
        return 0.0
    cos_theta = max(-1.0, min(1.0, dot / (mag_a * mag_c)))
    return math.degrees(math.acos(cos_theta))


def line_deviation_deg(a: Sequence[float], b: Sequence[float], c: Sequence[float]) -> float:
    """Deviation from a straight line a-b-c, in degrees (0 = perfectly straight).

    Equivalent to 180 - angle_deg(a, b, c): a straight line has an interior
    angle of 180 degrees at b; deviation is how far short of that it falls.
    """
    return 180.0 - angle_deg(a, b, c)


def euclidean_distance(a: Sequence[float], b: Sequence[float]) -> float:
    return math.hypot(a[0] - b[0], a[1] - b[1])
