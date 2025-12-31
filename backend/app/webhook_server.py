from fastapi import APIRouter, Request, Query
from fastapi.responses import JSONResponse
import datetime

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
    return JSONResponse({"ok": True, "uid": uid, "session_id": session_id, "sample_rate": sample_rate, "bytes": len(body)}, status_code=200)
