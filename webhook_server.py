from fastapi import FastAPI, APIRouter, Request, Query
from fastapi.responses import JSONResponse
import datetime

router = APIRouter()

app = FastAPI()

@app.get("/health")
def health():
    return {"ok": True, "ts": datetime.datetime.utcnow().isoformat()}

import json

@app.post("/webhook")
async def webhook(request: Request):
    # Optional: verify a shared secret
    expected = "MY_SHARED_SECRET"
    got = request.headers.get("X-Webhook-Secret", "")
    if got != expected:
        return JSONResponse(content="unauthorized", status_code=401)

    try:
        data = await request.json()
    except Exception:
        data = {}

    print("Webhook received:", json.dumps(data))
    return JSONResponse(content="ok", status_code=200)

@router.post("/omi/audio")
async def omi_audio(
    request: Request,
    session_id: str = Query("default"),
    sample_rate: int | None = Query(None),
    uid: str | None = Query(None),
):
    body = await request.body()
    ctype = (request.headers.get("content-type") or "").lower()

    if sample_rate is None and "sample_rate=" in session_id:
        try:
            left, right = session_id.split("?", 1)
            session_id = left
            if right.startswith("sample_rate="):
                sample_rate = int(right.split("=", 1)[1])
        except Exception:
            pass

    print(f"OMI AUDIO: uid={uid} session_id={session_id} sample_rate={sample_rate} ctype={ctype} bytes={len(body)}")
    return JSONResponse({"ok": True, "uid": uid, "session_id": session_id, "sample_rate": sample_rate, "bytes": len(body)}, status_code=200)

app.include_router(router)
