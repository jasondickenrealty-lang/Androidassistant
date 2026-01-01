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

async function addPasswordsToUsers() {
  try {
    const users = [
      'andrew@edprealty.com',
      'jasondickenrealty@gmail.com'
    ];
    
    const password = 'password';
    
    console.log('Adding password authentication to users...\n');
    
    for (const email of users) {
      try {
        // Get user by email
        const user = await getAuth().getUserByEmail(email);
        
        // Update user to add password
        await getAuth().updateUser(user.uid, {
          password: password
        });
        
        console.log(`✅ Successfully added password to: ${email}`);
        console.log(`   UID: ${user.uid}`);
        console.log(`   Password: ${password}\n`);
      } catch (error) {
        console.error(`❌ Error updating ${email}:`, error.message);
      }
    }
    
    console.log('\n' + '='.repeat(60));
    console.log('Password update complete!');
    console.log('Users can now sign in with email/password');
    console.log('='.repeat(60));
    
  } catch (error) {
    console.error('Error:', error);
  }
}

addPasswordsToUsers();
