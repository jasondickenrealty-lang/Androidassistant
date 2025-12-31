import { useEffect, useState } from "react";
import { auth, db } from "./lib/firebase";
import { onAuthStateChanged, signInWithEmailAndPassword } from "firebase/auth";
import { doc, getDoc } from "firebase/firestore";

export default function DebugFirebase() {
  const [msg, setMsg] = useState("Starting...");
  const [uid, setUid] = useState<string | null>(null);

  useEffect(() => {
    const projectId = import.meta.env.VITE_FIREBASE_PROJECT_ID;
    setMsg(`Loaded env projectId = ${projectId || "(undefined)"}`);

    return onAuthStateChanged(auth, (u) => {
      setUid(u?.uid ?? null);
    });
  }, []);

  async function login() {
    // put a DEV test user you created in Firebase Auth
    const email = "YOUR_TEST_EMAIL";
    const password = "YOUR_TEST_PASSWORD";
    const cred = await signInWithEmailAndPassword(auth, email, password);
    setUid(cred.user.uid);
  }

  async function readMyUserDoc() {
    if (!auth.currentUser) {
      setMsg("Not signed in yet.");
      return;
    }
    try {
      const ref = doc(db, "users", auth.currentUser.uid);
      const snap = await getDoc(ref);
      setMsg(
        snap.exists()
          ? `✅ Read users/${auth.currentUser.uid}:\n` + JSON.stringify(snap.data(), null, 2)
          : `⚠️ No doc at users/${auth.currentUser.uid} (Auth ok, Firestore ok, but profile not created yet)`
      );
    } catch (e: any) {
      setMsg(`❌ Firestore error: ${e?.message ?? String(e)}`);
    }
  }

  return (
    <div style={{ padding: 16, fontFamily: "system-ui" }}>
      <h2>DebugFirebase</h2>
      <div style={{ marginBottom: 8 }}>UID: {uid ?? "(none)"}</div>

      <button onClick={login} style={{ marginRight: 8 }}>Login</button>
      <button onClick={readMyUserDoc}>Read users/{`{uid}`}</button>

      <pre style={{ marginTop: 12, whiteSpace: "pre-wrap" }}>{msg}</pre>
    </div>
  );
}
