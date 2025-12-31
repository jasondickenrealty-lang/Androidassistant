from fastapi import FastAPI
from . import webhook_server

app = FastAPI()
app.include_router(webhook_server.router)

@app.get("/health")
def health():
    return {"ok": True}
