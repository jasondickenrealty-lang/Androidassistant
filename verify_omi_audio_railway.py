import requests

RAILWAY_BASE = "https://web-production-0ddab3.up.railway.app"

# Check OpenAPI docs for /omi/audio
openapi_url = f"{RAILWAY_BASE}/openapi.json"
try:
    resp = requests.get(openapi_url, timeout=5)
    resp.raise_for_status()
    paths = resp.json().get("paths", {})
    print("Available endpoints:")
    for path in sorted(paths.keys()):
        print("-", path)
    if "/omi/audio" not in paths:
        print("\n❌ /omi/audio is NOT deployed. Check your Railway start command and code push.")
    else:
        print("\n✅ /omi/audio is present in OpenAPI and should be live.")
except Exception as e:
    print("Failed to fetch OpenAPI docs:", e)

# Try a POST to /omi/audio
try:
    url = f"{RAILWAY_BASE}/omi/audio"
    params = {"session_id": "test", "sample_rate": 16000, "uid": "test-uid"}
    audio_bytes = bytes([1, 2, 3, 4, 5])
    headers = {"Content-Type": "application/octet-stream"}
    resp = requests.post(url, params=params, data=audio_bytes, headers=headers, timeout=5)
    print(f"\nPOST /omi/audio status: {resp.status_code}")
    print("Response:", resp.text)
except Exception as e:
    print("POST to /omi/audio failed:", e)
