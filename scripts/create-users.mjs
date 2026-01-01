#!/usr/bin/env node

// Script to create initial Firebase users
import admin from 'firebase-admin';
import { readFileSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Initialize Firebase Admin
const serviceAccount = JSON.parse(
  readFileSync(join(__dirname, '../backend/service-account-key.json'), 'utf8')
);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const users = [
  {
    email: 'andrew@edprealty.com',
    password: 'password',
    displayName: 'Andrew'
  },
  {
    email: 'jasondickenrealty@gmail.com',
    password: 'password',
    displayName: 'Jason Dicken'
  }
];

async function createUsers() {
  console.log('🔧 Creating Firebase users...\n');

  for (const userData of users) {
    try {
      const userRecord = await admin.auth().createUser({
        email: userData.email,
        password: userData.password,
        displayName: userData.displayName,
        emailVerified: false
      });

      console.log(`✅ Successfully created user: ${userData.email}`);
      console.log(`   UID: ${userRecord.uid}`);
      console.log(`   Display Name: ${userData.displayName}\n`);
    } catch (error) {
      if (error.code === 'auth/email-already-exists') {
        console.log(`⚠️  User already exists: ${userData.email}\n`);
      } else {
        console.error(`❌ Error creating user ${userData.email}:`, error.message, '\n');
      }
    }
  }

  console.log('✅ User creation complete!\n');
  console.log('Login credentials:');
  users.forEach(user => {
    console.log(`  Email: ${user.email}`);
    console.log(`  Password: ${user.password}\n`);
  });

  process.exit(0);
}

createUsers().catch(error => {
  console.error('Fatal error:', error);
  process.exit(1);
});
