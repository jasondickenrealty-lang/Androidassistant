import os, json
import time
import firebase_admin
from firebase_admin import credentials, firestore

def init_firebase():
    if firebase_admin._apps:
        return

    sa_json = os.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
    if sa_json:
        cred = credentials.Certificate(json.loads(sa_json))
        firebase_admin.initialize_app(cred)
        return

    # fallback to local file
    if os.path.exists("serviceAccount.json"):
        cred = credentials.Certificate("serviceAccount.json")
        firebase_admin.initialize_app(cred)
        return

    # fallback to GOOGLE_APPLICATION_CREDENTIALS
    gac = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
    if gac and os.path.exists(gac):
        cred = credentials.Certificate(gac)
        firebase_admin.initialize_app(cred)
        return

    raise RuntimeError(
        "No Firebase credentials found. Set FIREBASE_SERVICE_ACCOUNT_JSON, "
        "or place serviceAccount.json in the project root, "
        "or set GOOGLE_APPLICATION_CREDENTIALS to a service account file path."
    )

init_firebase()
db = firestore.client()
uid = "CBp6OtFeL4SOzo8sbJnYnCIQtWu1"  # Replace with your UID

def on_snapshot(doc_snapshot, changes, read_time):
    for doc in doc_snapshot:
        data = doc.to_dict()
        bytes_val = data.get("last_bytes", 0)
        updated_at = data.get("updated_at", "")
        print(f"Session update: bytes={bytes_val} at {updated_at}")

doc_ref = db.collection("sessions").document(uid)
doc_watch = doc_ref.on_snapshot(on_snapshot)

print("Listening for session updates. Press Ctrl+C to exit.")
try:
    while True:
        time.sleep(1)
except KeyboardInterrupt:
    print("Stopped listening.")
