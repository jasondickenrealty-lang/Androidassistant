#!/usr/bin/env python3
"""
Upload APK to Firebase Storage and make it publicly accessible for download
"""
import os
import sys
import json
import firebase_admin
from firebase_admin import credentials, storage
from datetime import datetime

def upload_apk_to_storage(apk_path, destination_blob_name="android-app/OmiAssistant.apk"):
    """
    Upload APK to Firebase Storage and return the public download URL
    
    Args:
        apk_path: Path to the APK file
        destination_blob_name: Path in Firebase Storage bucket
    
    Returns:
        Public download URL
    """
    # Initialize Firebase Admin if not already done
    if not firebase_admin._apps:
        # Try to get service account from environment or file
        sa_json = os.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
        
        if sa_json:
            sa_data = json.loads(sa_json)
            cred = credentials.Certificate(sa_data)
            project_id = sa_data.get('project_id')
        else:
            # Try to load from file
            sa_path = os.path.join(os.path.dirname(__file__), "service-account-key.json")
            if not os.path.exists(sa_path):
                sa_path = os.path.join(os.path.dirname(__file__), "..", "backend", "service-account-key.json")
            
            if os.path.exists(sa_path):
                with open(sa_path, 'r') as f:
                    sa_data = json.load(f)
                cred = credentials.Certificate(sa_path)
                project_id = sa_data.get('project_id')
            else:
                raise RuntimeError("Firebase service account credentials not found")
        
        # Initialize without specifying bucket, will use default
        firebase_admin.initialize_app(cred)
    
    # Try different bucket name formats
    bucket_names = [
        f"{project_id}.appspot.com",
        f"{project_id}.firebasestorage.app",
        project_id
    ]
    
    bucket = None
    for bucket_name in bucket_names:
        try:
            bucket = storage.bucket(bucket_name)
            # Test if bucket is accessible
            list(bucket.list_blobs(max_results=1))
            print(f"Using bucket: {bucket_name}")
            break
        except Exception as e:
            print(f"Trying bucket {bucket_name}... not found")
            continue
    
    if bucket is None:
        # Use default bucket
        bucket = storage.bucket()
        print(f"Using default storage bucket")
    
    # Check if APK exists
    if not os.path.exists(apk_path):
        raise FileNotFoundError(f"APK file not found: {apk_path}")
    
    # Get file size
    file_size = os.path.getsize(apk_path)
    print(f"Uploading APK ({file_size / (1024*1024):.2f} MB)...")
    
    # Upload the file
    blob = bucket.blob(destination_blob_name)
    blob.upload_from_filename(apk_path, content_type='application/vnd.android.package-archive')
    
    # Make the blob publicly accessible
    blob.make_public()
    
    # Get the public URL
    public_url = blob.public_url
    
    print(f"✅ APK uploaded successfully!")
    print(f"📦 Storage path: {destination_blob_name}")
    print(f"🔗 Public URL: {public_url}")
    print(f"📅 Upload time: {datetime.now().isoformat()}")
    
    return public_url

if __name__ == "__main__":
    # Default APK path
    default_apk_path = os.path.join(
        os.path.dirname(__file__), 
        "..", 
        "app", 
        "build", 
        "outputs", 
        "apk", 
        "debug", 
        "app-debug.apk"
    )
    
    apk_path = sys.argv[1] if len(sys.argv) > 1 else default_apk_path
    
    try:
        url = upload_apk_to_storage(apk_path)
        print(f"\n✨ You can now use this URL for downloads:")
        print(url)
    except Exception as e:
        print(f"❌ Error: {e}")
        sys.exit(1)
