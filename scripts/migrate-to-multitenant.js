// Firestore Multi-Tenant Migration Script
// Run this in Firebase Console or using Node.js with Firebase Admin SDK

import admin from 'firebase-admin';
import { readFileSync } from 'fs';

const serviceAccount = JSON.parse(
  readFileSync('./service-account-key.json', 'utf8')
);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: `https://${serviceAccount.project_id}.firebaseio.com`
});

const db = admin.firestore();
// Ensure database is initialized
db.settings({ ignoreUndefinedProperties: true });

async function migrateToMultiTenant() {
  console.log('Starting multi-tenant migration...');
  
  // Step 1: Create Euphoric Development company
  const companyId = 'euphoric-development';
  const companyData = {
    id: companyId,
    name: 'Euphoric Development',
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    isActive: true,
    plan: 'enterprise',
    settings: {
      maxUsers: 100,
      features: ['omi-glasses', 'recordings', 'ai-assistant']
    }
  };
  
  try {
    await db.collection('companies').doc(companyId).set(companyData);
    console.log('✓ Created Euphoric Development company');
  } catch (error) {
    console.error('Error creating company:', error);
  }
  
  // Step 2: Get all existing users
  const usersSnapshot = await db.collection('users').get();
  console.log(`Found ${usersSnapshot.size} users to migrate`);
  
  // Step 3: Update each user with companyId
  const batch = db.batch();
  let updateCount = 0;
  
  for (const doc of usersSnapshot.docs) {
    const userRef = db.collection('users').doc(doc.id);
    batch.update(userRef, {
      companyId: companyId,
      migratedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    updateCount++;
    
    // Commit in batches of 500 (Firestore limit)
    if (updateCount % 500 === 0) {
      await batch.commit();
      console.log(`✓ Migrated ${updateCount} users...`);
    }
  }
  
  // Commit remaining
  if (updateCount % 500 !== 0) {
    await batch.commit();
  }
  
  console.log(`✓ Successfully migrated ${updateCount} users to Euphoric Development`);
  
  // Step 4: Migrate paired devices
  console.log('Migrating paired devices...');
  let deviceCount = 0;
  
  for (const userDoc of usersSnapshot.docs) {
    const devicesSnapshot = await db.collection('users')
      .doc(userDoc.id)
      .collection('pairedDevices')
      .get();
    
    const deviceBatch = db.batch();
    
    for (const deviceDoc of devicesSnapshot.docs) {
      const deviceRef = db.collection('users')
        .doc(userDoc.id)
        .collection('pairedDevices')
        .doc(deviceDoc.id);
      
      deviceBatch.update(deviceRef, {
        companyId: companyId
      });
      deviceCount++;
    }
    
    if (devicesSnapshot.size > 0) {
      await deviceBatch.commit();
    }
  }
  
  console.log(`✓ Migrated ${deviceCount} paired devices`);
  
  // Step 5: Migrate recordings
  console.log('Migrating recordings...');
  let recordingCount = 0;
  
  for (const userDoc of usersSnapshot.docs) {
    const recordingsSnapshot = await db.collection('users')
      .doc(userDoc.id)
      .collection('recordings')
      .get();
    
    const recordingBatch = db.batch();
    
    for (const recordingDoc of recordingsSnapshot.docs) {
      const recordingRef = db.collection('users')
        .doc(userDoc.id)
        .collection('recordings')
        .doc(recordingDoc.id);
      
      recordingBatch.update(recordingRef, {
        companyId: companyId
      });
      recordingCount++;
    }
    
    if (recordingsSnapshot.size > 0) {
      await recordingBatch.commit();
    }
  }
  
  console.log(`✓ Migrated ${recordingCount} recordings`);
  
  // Step 6: Migrate myDays data
  console.log('Migrating myDays...');
  let myDaysCount = 0;
  
  for (const userDoc of usersSnapshot.docs) {
    const myDaysSnapshot = await db.collection('users')
      .doc(userDoc.id)
      .collection('myDays')
      .get();
    
    const myDaysBatch = db.batch();
    
    for (const myDayDoc of myDaysSnapshot.docs) {
      const myDayRef = db.collection('users')
        .doc(userDoc.id)
        .collection('myDays')
        .doc(myDayDoc.id);
      
      myDaysBatch.update(myDayRef, {
        companyId: companyId
      });
      myDaysCount++;
    }
    
    if (myDaysSnapshot.size > 0) {
      await myDaysBatch.commit();
    }
  }
  
  console.log(`✓ Migrated ${myDaysCount} myDays entries`);
  
  console.log('\n=== Migration Complete ===');
  console.log(`Company: Euphoric Development (${companyId})`);
  console.log(`Users: ${updateCount}`);
  console.log(`Devices: ${deviceCount}`);
  console.log(`Recordings: ${recordingCount}`);
  console.log(`MyDays: ${myDaysCount}`);
}

// Run the migration
migrateToMultiTenant()
  .then(() => {
    console.log('Migration completed successfully!');
    process.exit(0);
  })
  .catch((error) => {
    console.error('Migration failed:', error);
    process.exit(1);
  });
