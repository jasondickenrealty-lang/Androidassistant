import os
import requests

OMI_AUDIO_URL = os.getenv("OMI_AUDIO_URL", "http://127.0.0.1:8000/omi/audio")

params = {
    "session_id": "test-session",
    "sample_rate": 16000,
    "uid": "user-001"
}

audio_bytes = bytes([0] * 100)  # Simulate 100 bytes of audio

headers = {
    "Content-Type": "application/octet-stream"
}

try:
    response = requests.post(OMI_AUDIO_URL, params=params, data=audio_bytes, headers=headers)
    print("Status Code:", response.status_code)
    print("Response JSON:", response.json())
except Exception as e:
    print("Request failed:", e)
