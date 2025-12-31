import os
import requests

# Use port 9000 for local FastAPI server
OMI_HEALTH_URL = os.getenv("OMI_HEALTH_URL", "http://127.0.0.1:9000/health")

try:
    response = requests.get(OMI_HEALTH_URL, timeout=3)
    print("Status Code:", response.status_code)
    print("Response JSON:", response.json())
except requests.ConnectionError:
    print("Could not connect to the FastAPI server. Make sure it is running on port 9000.")
except Exception as e:
    print("Request failed:", e)
