import math

from pose.angles import angle_deg, euclidean_distance, line_deviation_deg
from pose.extractor import Frame
from pose.landmarks import (
    LEFT_ANKLE,
    LEFT_EAR,
    LEFT_ELBOW,
    LEFT_HIP,
    LEFT_KNEE,
    LEFT_SHOULDER,
    LEFT_WRIST,
    RIGHT_ANKLE,
    RIGHT_ELBOW,
    RIGHT_HIP,
    RIGHT_KNEE,
    RIGHT_SHOULDER,
    RIGHT_WRIST,
)
from pose import curlup, pushup


def lm(x, y, visibility=1.0):
    return (x, y, visibility)


def test_angle_deg_right_angle():
    a = (0.0, 1.0)
    b = (0.0, 0.0)
    c = (1.0, 0.0)
    assert math.isclose(angle_deg(a, b, c), 90.0, abs_tol=1e-6)


def test_angle_deg_straight_line():
    a = (-1.0, 0.0)
    b = (0.0, 0.0)
    c = (1.0, 0.0)
    assert math.isclose(angle_deg(a, b, c), 180.0, abs_tol=1e-6)


def test_line_deviation_deg_straight_line_is_zero():
    a = (-1.0, 0.0)
    b = (0.0, 0.0)
    c = (1.0, 0.0)
    assert math.isclose(line_deviation_deg(a, b, c), 0.0, abs_tol=1e-6)


def test_line_deviation_deg_bent_line():
    a = (-1.0, 0.0)
    b = (0.0, 0.0)
    c = (0.0, 1.0)  # 90 degree bend at b
    assert math.isclose(line_deviation_deg(a, b, c), 90.0, abs_tol=1e-6)


def test_euclidean_distance():
    assert math.isclose(euclidean_distance((0.0, 0.0), (3.0, 4.0)), 5.0, abs_tol=1e-6)


def _pushup_frame(sec, elbow_angle_deg, hip_sag_deg=0.0, neck_aligned=True):
    """Build a synthetic push-up frame with a controlled elbow angle and body-line deviation.

    Shoulder/elbow/wrist are placed so angle_deg(shoulder, elbow, wrist) == elbow_angle_deg.
    Shoulder/hip/ankle are placed so line_deviation_deg(shoulder, hip, ankle) == hip_sag_deg.
    """
    elbow = (0.0, 0.0)
    shoulder = (0.0, -1.0)
    theta = math.radians(elbow_angle_deg)
    wrist = (math.sin(theta), -math.cos(theta))

    hip = (0.0, 1.0)
    ankle_angle = math.radians(180.0 - hip_sag_deg)
    ankle = (math.sin(ankle_angle) * 1.0, 1.0 + math.cos(ankle_angle) * -1.0 * -1.0)
    # Simpler: place ankle along the shoulder->hip direction extended, then rotate by hip_sag_deg.
    direction = math.atan2(hip[1] - shoulder[1], hip[0] - shoulder[0])
    rotated = direction + math.radians(hip_sag_deg)
    ankle = (hip[0] + math.cos(rotated) * 1.0, hip[1] + math.sin(rotated) * 1.0)

    ear = (0.0, -1.2) if neck_aligned else (0.6, -1.2)

    return Frame(
        sec=sec,
        landmarks={
            LEFT_SHOULDER: lm(*shoulder), RIGHT_SHOULDER: lm(*shoulder),
            LEFT_ELBOW: lm(*elbow), RIGHT_ELBOW: lm(*elbow),
            LEFT_WRIST: lm(*wrist), RIGHT_WRIST: lm(*wrist),
            LEFT_HIP: lm(*hip), RIGHT_HIP: lm(*hip),
            LEFT_ANKLE: lm(*ankle), RIGHT_ANKLE: lm(*ankle),
            LEFT_EAR: lm(*ear),
        },
    )


def test_pushup_rep_segmentation_counts_down_phases():
    frames = [
        _pushup_frame(0.0, 170.0),
        _pushup_frame(0.2, 150.0),
        _pushup_frame(0.4, 95.0),   # down phase 1
        _pushup_frame(0.6, 100.0),  # down phase 1
        _pushup_frame(0.8, 150.0),
        _pushup_frame(1.0, 170.0),
        _pushup_frame(1.2, 170.0),
        _pushup_frame(1.4, 150.0),
        _pushup_frame(1.6, 90.0),   # down phase 2
        _pushup_frame(1.8, 170.0),
    ]
    metrics = pushup.compute_metrics(frames)
    assert metrics["repCount"] == 2
    assert metrics["reps"][0]["minElbowAngleDeg"] <= 100.0
    assert metrics["reps"][1]["minElbowAngleDeg"] <= 95.0


def test_pushup_hip_sag_detected():
    frames = [_pushup_frame(0.0, 170.0, hip_sag_deg=0.0), _pushup_frame(0.2, 95.0, hip_sag_deg=25.0)]
    metrics = pushup.compute_metrics(frames)
    assert metrics["reps"][0]["maxHipSagDeg"] >= 20.0


def _curlup_frame(sec, knee_angle_deg, trunk_lift_deg, neck_pull_deg=0.0):
    """Build a synthetic curl-up frame with a controlled knee angle, trunk lift, and neck-pull deviation.

    Knee/hip/ankle are placed so angle_deg(hip, knee, ankle) == knee_angle_deg.
    Shoulder/hip/knee are placed so (180 - angle_deg(shoulder, hip, knee)) == trunk_lift_deg.
    Ear/shoulder/hip are placed so line_deviation_deg(ear, shoulder, hip) == neck_pull_deg.
    """
    knee = (0.0, 0.0)
    theta = math.radians(knee_angle_deg)
    ankle = (math.sin(theta), -math.cos(theta))

    hip = (0.0, -1.0)
    phi = math.radians(180.0 - trunk_lift_deg)
    shoulder = (hip[0] + math.sin(phi), hip[1] + math.cos(phi))

    w0 = (math.sin(phi), math.cos(phi))  # opposite of the hip->shoulder direction
    delta = math.radians(neck_pull_deg)
    w = (
        w0[0] * math.cos(delta) - w0[1] * math.sin(delta),
        w0[0] * math.sin(delta) + w0[1] * math.cos(delta),
    )
    ear = (shoulder[0] + w[0], shoulder[1] + w[1])

    return Frame(
        sec=sec,
        landmarks={
            LEFT_SHOULDER: lm(*shoulder), RIGHT_SHOULDER: lm(*shoulder),
            LEFT_HIP: lm(*hip), RIGHT_HIP: lm(*hip),
            LEFT_KNEE: lm(*knee), RIGHT_KNEE: lm(*knee),
            LEFT_ANKLE: lm(*ankle), RIGHT_ANKLE: lm(*ankle),
            LEFT_EAR: lm(*ear),
        },
    )


def test_curlup_rep_segmentation_counts_up_phases():
    frames = [
        _curlup_frame(0.0, 90.0, 5.0),
        _curlup_frame(0.2, 90.0, 15.0),
        _curlup_frame(0.4, 90.0, 45.0),   # up phase 1
        _curlup_frame(0.6, 90.0, 50.0),   # up phase 1
        _curlup_frame(0.8, 90.0, 15.0),
        _curlup_frame(1.0, 90.0, 5.0),
        _curlup_frame(1.2, 90.0, 5.0),
        _curlup_frame(1.4, 90.0, 15.0),
        _curlup_frame(1.6, 90.0, 45.0),   # up phase 2
        _curlup_frame(1.8, 90.0, 5.0),
    ]
    metrics = curlup.compute_metrics(frames)
    assert metrics["repCount"] == 2
    assert metrics["reps"][0]["maxTrunkLiftDeg"] >= 45.0
    assert metrics["reps"][1]["maxTrunkLiftDeg"] >= 45.0


def test_curlup_neck_pull_and_knee_angle_instability_detected():
    frames = [
        _curlup_frame(0.0, 90.0, 5.0, neck_pull_deg=0.0),
        _curlup_frame(0.2, 170.0, 45.0, neck_pull_deg=25.0),  # knee straightened + neck pulled, up phase
    ]
    metrics = curlup.compute_metrics(frames)
    assert metrics["reps"][0]["maxNeckPullDeg"] >= 20.0
    assert metrics["kneeAngleStableRatio"] < 1.0
