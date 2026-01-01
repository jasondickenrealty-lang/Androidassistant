#!/bin/bash

# Setup script for connecting to Google Cloud Project
# Project: omi-vituralassisant-app

set -e

echo "🔧 Google Cloud Project Connection Setup"
echo "========================================"
echo ""
echo "Project Name: OMI-VITURALASSISANT-APP"
echo "Project ID: omi-vituralassisant-app"
echo "Project Number: 9646598829912"
echo ""

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo "❌ Google Cloud SDK (gcloud) is not installed."
    echo ""
    echo "Install it from: https://cloud.google.com/sdk/docs/install"
    echo "Or run: brew install google-cloud-sdk"
    echo ""
    read -p "Would you like to install it now with brew? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        brew install google-cloud-sdk
    else
        exit 1
    fi
fi

echo ""
echo "📋 Step 1: Authenticating with Google Cloud"
echo "-------------------------------------------"
gcloud auth login

echo ""
echo "📋 Step 2: Setting active project"
echo "-------------------------------------------"
gcloud config set project omi-vituralassisant-app

echo ""
echo "📋 Step 3: Checking Firebase status"
echo "-------------------------------------------"

# Check if Firebase CLI is installed
if ! command -v firebase &> /dev/null; then
    echo "❌ Firebase CLI is not installed."
    echo ""
    read -p "Would you like to install it now with npm? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        npm install -g firebase-tools
    else
        echo "Install Firebase CLI: npm install -g firebase-tools"
        exit 1
    fi
fi

echo ""
echo "Logging into Firebase..."
firebase login

echo ""
echo "📋 Step 4: Checking if Firebase is enabled for this project"
echo "-------------------------------------------"
firebase projects:list

echo ""
echo "📋 Step 5: Setting up Firebase credentials"
echo "-------------------------------------------"
echo ""
echo "⚠️  MANUAL STEPS REQUIRED:"
echo ""
echo "1. Open Firebase Console: https://console.firebase.google.com/"
echo "2. If your project (omi-vituralassisant-app) is NOT listed above:"
echo "   - Click 'Add project'"
echo "   - Select 'omi-vituralassisant-app' from the dropdown"
echo "   - Complete Firebase setup"
echo ""
echo "3. Enable Firestore Database:"
echo "   - Go to Build → Firestore Database"
echo "   - Click 'Create database'"
echo "   - Choose test mode for development"
echo ""
echo "4. Get Service Account Key (for backend):"
echo "   - Go to Project Settings → Service Accounts"
echo "   - Click 'Generate new private key'"
echo "   - Download the JSON file"
echo "   - Save it as: ./backend/service-account-key.json"
echo ""
echo "5. Get Web App Config (for frontend):"
echo "   - Go to Project Settings → General"
echo "   - Add a Web App if not already added"
echo "   - Copy the firebaseConfig values"
echo ""

read -p "Press Enter when you've completed the Firebase setup..."

echo ""
echo "📋 Step 6: Configure service account"
echo "-------------------------------------------"

SERVICE_ACCOUNT_PATH="./backend/service-account-key.json"

if [ -f "$SERVICE_ACCOUNT_PATH" ]; then
    echo "✅ Service account key found!"
    echo ""
    echo "Setting GOOGLE_APPLICATION_CREDENTIALS..."
    export GOOGLE_APPLICATION_CREDENTIALS="$SERVICE_ACCOUNT_PATH"
    
    # Add to .env file if it exists, or create one
    if [ -f "./backend/.env" ]; then
        if grep -q "GOOGLE_APPLICATION_CREDENTIALS" ./backend/.env; then
            sed -i.bak "s|GOOGLE_APPLICATION_CREDENTIALS=.*|GOOGLE_APPLICATION_CREDENTIALS=$SERVICE_ACCOUNT_PATH|" ./backend/.env
        else
            echo "GOOGLE_APPLICATION_CREDENTIALS=$SERVICE_ACCOUNT_PATH" >> ./backend/.env
        fi
    else
        echo "GOOGLE_APPLICATION_CREDENTIALS=$SERVICE_ACCOUNT_PATH" > ./backend/.env
        echo "FIREBASE_SERVICE_ACCOUNT_JSON=" >> ./backend/.env
    fi
    
    echo "✅ Backend environment configured!"
else
    echo "⚠️  Service account key not found at: $SERVICE_ACCOUNT_PATH"
    echo "Please download it from Firebase Console and save it to that location."
fi

echo ""
echo "📋 Step 7: Configure Firebase for frontend"
echo "-------------------------------------------"
echo ""
echo "Please provide your Firebase web app configuration values:"
echo ""

read -p "API Key: " VITE_FIREBASE_API_KEY
read -p "Auth Domain: " VITE_FIREBASE_AUTH_DOMAIN
read -p "Storage Bucket: " VITE_FIREBASE_STORAGE_BUCKET
read -p "Messaging Sender ID: " VITE_FIREBASE_MESSAGING_SENDER_ID
read -p "App ID: " VITE_FIREBASE_APP_ID

# Update frontend .env.development
cat > ./frontend/.env.development << EOF
VITE_FIREBASE_API_KEY=$VITE_FIREBASE_API_KEY
VITE_FIREBASE_AUTH_DOMAIN=$VITE_FIREBASE_AUTH_DOMAIN
VITE_FIREBASE_PROJECT_ID=omi-vituralassisant-app
VITE_FIREBASE_STORAGE_BUCKET=$VITE_FIREBASE_STORAGE_BUCKET
VITE_FIREBASE_MESSAGING_SENDER_ID=$VITE_FIREBASE_MESSAGING_SENDER_ID
VITE_FIREBASE_APP_ID=$VITE_FIREBASE_APP_ID

VITE_API_BASE_URL=http://localhost:8000
EOF

echo ""
echo "✅ Frontend environment configured!"

echo ""
echo "=========================================="
echo "✅ Setup Complete!"
echo "=========================================="
echo ""
echo "Your project is now connected to:"
echo "  Project ID: omi-vituralassisant-app"
echo ""
echo "Next steps:"
echo "  1. Install backend dependencies: cd backend && pip install -r requirements.txt"
echo "  2. Install frontend dependencies: cd frontend && npm install"
echo "  3. Start backend: cd backend && python -m uvicorn app.main:app --reload"
echo "  4. Start frontend: cd frontend && npm run dev"
echo ""
