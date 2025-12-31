import os
import requests

# Test the deployed Railway /omi/audio endpoint
OMI_AUDIO_URL = os.getenv("OMI_AUDIO_URL", "https://web-production-0ddab3.up.railway.app/omi/audio")

params = {
    "session_id": "railway-test-session",
    "sample_rate": 16000,
    "uid": "railway-user"
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
