# Multi-Tenant Migration Script

This script migrates your Omi Assistant app to a multi-tenant SAAS architecture.

## Setup

1. **Install dependencies:**
   ```bash
   cd scripts
   npm install
   ```

2. **Copy your Firebase service account key:**
   - Go to Firebase Console → Project Settings → Service Accounts
   - Click "Generate New Private Key"
   - Save as `service-account-key.json` in this directory

3. **Run the migration:**
   ```bash
   npm run migrate
   ```

## What it does

1. Creates a "companies" collection with "Euphoric Development" as the first company
2. Updates all existing users with `companyId: "euphoric-development"`
3. Adds `companyId` to all user sub-collections:
   - pairedDevices
   - recordings
   - myDays

## After Migration

Update your Android app to use the new multi-tenant structure:
- All data queries should filter by `companyId`
- New users should be assigned to a company on registration
- User profiles should include `companyId`

## Rollback

If needed, you can remove the `companyId` field from all documents using a similar script.
