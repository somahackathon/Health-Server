# ai-python — 자세 분석 관절 추출 서버

Spring 서버가 자세 분석 영상(팔굽혀펴기/윗몸앞으로굽히기)을 전달하면 MediaPipe Pose로
관절 좌표를 추출하고, 종목별 각도·정렬 지표(metrics)를 계산해 반환하는 내부 서브서버입니다.

이 서버는 Gemini API 키를 갖지 않습니다. LLM 분석/문장 생성은 Spring이 담당하고,
이 서버는 순수 컴퓨터 비전(MediaPipe) 계산만 수행합니다.

## 영상 처리 정책

업로드된 영상은 요청 처리 중에만 임시 파일로 존재하며, 처리 완료(성공/실패 무관) 즉시
`finally` 블록에서 삭제합니다. 디스크에 영구 저장하지 않습니다.

## 실행

```powershell
cd ai-python
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

## 테스트

```powershell
pytest
```

## API

### POST /pose/extract (multipart/form-data)

- `video`: 영상 파일 (mp4/mov)
- `exerciseType`: `PUSH_UP` | `SIT_AND_REACH`

성공 200 응답 예시는 `docs/ai-contract.md`를 참고하세요.

오류:
- 400 `{"code": "UNSUPPORTED_EXERCISE_TYPE"}`
- 422 `{"code": "NO_PERSON_DETECTED"}` — 사람 미검출
- 422 `{"code": "VIDEO_DECODE_FAILED"}` — 영상 디코딩 실패

### GET /health

`{"status": "ok"}`
