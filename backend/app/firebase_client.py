import os
import json
import firebase_admin
from firebase_admin import credentials, firestore

_db = None

def get_db():
    global _db
    if _db is not None:
        return _db

    if not firebase_admin._apps:
        # Option A: service account JSON stored in env var (recommended for Railway)
        sa_json = os.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
        if not sa_json:
            raise RuntimeError("Missing FIREBASE_SERVICE_ACCOUNT_JSON env var")

        cred_dict = json.loads(sa_json)
        cred = credentials.Certificate(cred_dict)
        firebase_admin.initialize_app(cred)

    _db = firestore.client()
    return _db
