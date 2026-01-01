package com.omiagent.assistant

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        
        supportActionBar?.hide()
        
        auth = FirebaseAuth.getInstance()
        
        // Check if user is authenticated
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        // Display user info
        findViewById<TextView>(R.id.userNameText).text = 
            auth.currentUser?.displayName ?: auth.currentUser?.email ?: "User"
        
        // Sign Out button
        findViewById<Button>(R.id.btnSignOut).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        
        // Dashboard Cards - Match web app
        findViewById<CardView>(R.id.cardMyDay).setOnClickListener {
            startActivity(Intent(this, MyDayActivity::class.java))
        }
        findViewById<Button>(R.id.btnViewMyDay).setOnClickListener {
            startActivity(Intent(this, MyDayActivity::class.java))
        }
        
        findViewById<CardView>(R.id.cardMyRecordings).setOnClickListener {
            startActivity(Intent(this, MyRecordingsActivity::class.java))
        }
        findViewById<Button>(R.id.btnViewRecordings).setOnClickListener {
            startActivity(Intent(this, MyRecordingsActivity::class.java))
        }
        
        findViewById<CardView>(R.id.cardMyPeople).setOnClickListener {
            Toast.makeText(this, "My People - Coming Soon", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnViewPeople).setOnClickListener {
            Toast.makeText(this, "My People - Coming Soon", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<CardView>(R.id.cardMyProperties).setOnClickListener {
            startActivity(Intent(this, PropertyListActivity::class.java))
        }
        findViewById<Button>(R.id.btnViewProperties).setOnClickListener {
            startActivity(Intent(this, PropertyListActivity::class.java))
        }
        
        // Bottom Navigation
        findViewById<LinearLayout>(R.id.navChat).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
