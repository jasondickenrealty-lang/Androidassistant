
from fastapi import APIRouter, Request, Query
from fastapi.responses import JSONResponse
from datetime import datetime, timezone
import uuid
import datetime as dt
from app.firebase_client import get_db

router = APIRouter()

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

    # Firestore write
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

    return JSONResponse({"ok": True, "uid": uid, "session_id": session_id, "sample_rate": sample_rate, "bytes": len(body)}, status_code=200)
