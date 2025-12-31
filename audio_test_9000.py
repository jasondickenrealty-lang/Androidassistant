import os
import requests

# Use port 9000 for local FastAPI server
OMI_AUDIO_URL = os.getenv("OMI_AUDIO_URL", "http://127.0.0.1:9000/omi/audio")

params = {
    "session_id": "script-session",
    "sample_rate": 16000,
    "uid": "script-user"
}

audio_bytes = bytes([1, 2, 3, 4, 5])  # Example audio bytes

headers = {
    "Content-Type": "application/octet-stream"
}

try:
    response = requests.post(OMI_AUDIO_URL, params=params, data=audio_bytes, headers=headers)
    print("Status Code:", response.status_code)
    print("Response JSON:", response.json())
except Exception as e:
    print("Request failed:", e)
