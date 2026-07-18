"""FastAPI pose-extraction server.

Receives an exercise video from the Spring backend, runs MediaPipe Pose over
sampled frames, computes exercise-specific angle/alignment metrics, and
returns them as JSON. The uploaded video is written to a temporary file only
for the duration of processing and is always deleted afterward. This process
never persists video content to disk beyond a single request.
"""
from __future__ import annotations

import os
import tempfile

from fastapi import FastAPI, Form, HTTPException, UploadFile

from pose import lunge, plank, pushup, squat
from pose.extractor import NoPersonDetectedError, VideoDecodeError, extract_frames

app = FastAPI(title="ai-python pose extraction server")

SAMPLE_FPS = 10
METRIC_COMPUTERS = {
    "PUSH_UP": pushup.compute_metrics,
    "SQUAT": squat.compute_metrics,
    "LUNGE": lunge.compute_metrics,
    "PLANK": plank.compute_metrics,
}
SUPPORTED_EXERCISE_TYPES = set(METRIC_COMPUTERS)


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/pose/extract")
async def extract(video: UploadFile, exerciseType: str = Form(...)):
    exercise_type = exerciseType.strip().upper()
    if exercise_type not in SUPPORTED_EXERCISE_TYPES:
        raise HTTPException(status_code=400, detail={"code": "UNSUPPORTED_EXERCISE_TYPE"})

    tmp_path = None
    try:
        contents = await video.read()
        with tempfile.NamedTemporaryFile(suffix=".mp4", delete=False) as tmp:
            tmp.write(contents)
            tmp_path = tmp.name

        frames, duration_sec = extract_frames(tmp_path, sample_fps=SAMPLE_FPS)

        metrics = METRIC_COMPUTERS[exercise_type](frames)

        return {
            "exerciseType": exercise_type,
            "durationSec": round(duration_sec, 2),
            "sampledFps": SAMPLE_FPS,
            "personDetected": True,
            "metrics": metrics,
        }
    except VideoDecodeError:
        raise HTTPException(status_code=422, detail={"code": "VIDEO_DECODE_FAILED"})
    except NoPersonDetectedError:
        raise HTTPException(status_code=422, detail={"code": "NO_PERSON_DETECTED"})
    finally:
        if tmp_path is not None and os.path.exists(tmp_path):
            os.remove(tmp_path)
