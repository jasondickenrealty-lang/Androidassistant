

from fastapi import APIRouter, Request, Query
from fastapi.responses import JSONResponse
from datetime import datetime, timezone
import uuid
import datetime as dt
import time
import json
from app.firebase_client import get_db

_last_write = {}  # uid -> unix timestamp

router = APIRouter()

@router.post("/webhook")
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

    # Throttled Firestore session update
    now = time.time()
    key = uid or "unknown"
    last = _last_write.get(key, 0)
    # write at most once every 2 seconds per uid
    if now - last >= 2:
        _last_write[key] = now
        try:
            db = get_db()
            db.collection("sessions").document(key).set({
                "uid": uid,
                "session_id": session_id,
                "sample_rate": sample_rate,
                "last_bytes": len(body),
                "content_type": ctype,
                "updated_at": datetime.now(timezone.utc).isoformat(),
            }, merge=True)
            print(f"FIRESTORE SESSION UPDATE OK: uid={key} bytes={len(body)}")
        except Exception as e:
            print(f"FIRESTORE SESSION UPDATE FAILED: {type(e).__name__}: {e}")

    # Firestore write with logging
    try:
        db = get_db()
        chunk_id = str(uuid.uuid4())
        doc = {
            "uid": uid,
            "session_id": session_id,
            "sample_rate": sample_rate,
            "bytes": len(body),
            "content_type": ctype,
            "received_at": datetime.now(timezone.utc).isoformat(),
        }
        db.collection("sessions").document(uid or "unknown").collection("audio_chunks").document(chunk_id).set(doc)
        print(f"FIRESTORE AUDIO WRITE OK: uid={uid} chunk_id={chunk_id} bytes={len(body)}")
    except Exception as e:
        print(f"FIRESTORE AUDIO WRITE FAILED: {type(e).__name__}: {e}")

    return JSONResponse({"ok": True, "uid": uid, "session_id": session_id, "sample_rate": sample_rate, "bytes": len(body)}, status_code=200)
