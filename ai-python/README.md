# AI Python Pose Extraction Server

FastAPI server used by the Spring backend for posture metric extraction.

The Spring server forwards an exercise video to this service. This service runs MediaPipe Pose on sampled frames, computes exercise-specific metrics, and returns JSON. It does not call Gemini or any LLM. Long-form analysis and text generation remain the Spring server's responsibility.

## Runtime

- Python 3.11+
- FastAPI
- MediaPipe
- OpenCV

## Run Locally

```powershell
cd ai-python
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

## Docker

```sh
docker build -t health-ai-python ./ai-python
docker run --rm -p 8000:8000 health-ai-python
```

## API

### `GET /health`

```json
{
  "status": "ok"
}
```

### `POST /pose/extract`

Content-Type: `multipart/form-data`

Parts:

- `video`: mp4 or mov video file
- `exerciseType`: `PUSH_UP`, `SQUAT`, `LUNGE`, or `PLANK`

Success response:

```json
{
  "exerciseType": "PUSH_UP",
  "durationSec": 18.4,
  "sampledFps": 10,
  "personDetected": true,
  "metrics": {}
}
```

Errors:

- `400 {"detail":{"code":"UNSUPPORTED_EXERCISE_TYPE"}}`
- `422 {"detail":{"code":"NO_PERSON_DETECTED"}}`
- `422 {"detail":{"code":"VIDEO_DECODE_FAILED"}}`

## Storage Policy

Uploaded videos are written to a temporary file only while processing the request and are deleted in a `finally` block after success or failure.
