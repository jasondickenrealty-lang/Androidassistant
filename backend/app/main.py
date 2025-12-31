
import os
from fastapi import FastAPI
from . import webhook_server
from datetime import datetime, timezone
import uuid
from app.firebase_client import get_db
from fastapi.responses import JSONResponse


app = FastAPI()

@app.on_event("startup")
async def startup_check():
    has_sa = bool(os.getenv("FIREBASE_SERVICE_ACCOUNT_JSON"))
    print(f"STARTUP: FIREBASE_SERVICE_ACCOUNT_JSON present={has_sa}")

app.include_router(webhook_server.router)


@app.get("/health")
def health():
    return {"ok": True}

@app.post("/debug/firebase")
async def debug_firebase():
    try:
        db = get_db()
        doc_id = str(uuid.uuid4())
        doc = {
            "ok": True,
            "source": "railway-debug",
            "created_at": datetime.now(timezone.utc).isoformat(),
        }
        db.collection("debug_writes").document(doc_id).set(doc)
        print(f"FIRESTORE DEBUG WRITE OK: doc_id={doc_id}")
        return JSONResponse({"ok": True, "doc_id": doc_id})
    except Exception as e:
        print(f"FIRESTORE DEBUG WRITE FAILED: {type(e).__name__}: {e}")
        return JSONResponse({"ok": False, "error": f"{type(e).__name__}: {e}"}, status_code=500)
