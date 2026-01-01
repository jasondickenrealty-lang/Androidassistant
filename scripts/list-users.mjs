import { initializeApp, cert } from 'firebase-admin/app';
import { getAuth } from 'firebase-admin/auth';
import { readFileSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Initialize Firebase Admin
const serviceAccount = JSON.parse(
  readFileSync(join(__dirname, 'service-account-key.json'), 'utf8')
);

initializeApp({
  credential: cert(serviceAccount)
});

async function listAllUsers() {
  try {
    console.log('Fetching all users from Firebase Authentication...\n');
    
    const listUsersResult = await getAuth().listUsers(1000);
    
    console.log(`Total users: ${listUsersResult.users.length}\n`);
    console.log('='.repeat(60));
    
    listUsersResult.users.forEach((userRecord, index) => {
      console.log(`\n${index + 1}. UID: ${userRecord.uid}`);
      console.log(`   Email: ${userRecord.email || 'N/A'}`);
      console.log(`   Display Name: ${userRecord.displayName || 'N/A'}`);
      console.log(`   Email Verified: ${userRecord.emailVerified}`);
      console.log(`   Created: ${userRecord.metadata.creationTime}`);
      console.log(`   Last Sign In: ${userRecord.metadata.lastSignInTime || 'Never'}`);
      console.log(`   Provider: ${userRecord.providerData.map(p => p.providerId).join(', ')}`);
    });
    
    console.log('\n' + '='.repeat(60));
    
    // Check for specific user
    const targetEmail = 'jasondickenrealty@gmail.com';
    const targetUser = listUsersResult.users.find(u => u.email === targetEmail);
    
    if (targetUser) {
      console.log(`\n✅ Found user: ${targetEmail}`);
      console.log(`   UID: ${targetUser.uid}`);
      console.log(`   Email Verified: ${targetUser.emailVerified}`);
    } else {
      console.log(`\n❌ User ${targetEmail} NOT found in Firebase Authentication`);
    }
    
  } catch (error) {
    console.error('Error listing users:', error);
  }
}

listAllUsers();
