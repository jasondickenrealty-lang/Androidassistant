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
        
        // Dashboard Buttons
        findViewById<LinearLayout>(R.id.btnMyDay).setOnClickListener {
            startActivity(Intent(this, MyDayActivity::class.java))
        }
        
        findViewById<LinearLayout>(R.id.btnMyRecordings).setOnClickListener {
            startActivity(Intent(this, MyRecordingsActivity::class.java))
        }
        
        findViewById<LinearLayout>(R.id.btnMyPeople).setOnClickListener {
            Toast.makeText(this, "My People - Coming Soon", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<LinearLayout>(R.id.btnMyProperties).setOnClickListener {
            startActivity(Intent(this, PropertyListActivity::class.java))
        }
        
        findViewById<LinearLayout>(R.id.btnOpenHouses).setOnClickListener {
            startActivity(Intent(this, OpenHouseListActivity::class.java))
        }
        
        findViewById<LinearLayout>(R.id.btnSafetyWord).setOnClickListener {
            startActivity(Intent(this, SafetyWordActivity::class.java))
        }
        
        findViewById<LinearLayout>(R.id.btnInspectionPunchList).setOnClickListener {
            startActivity(Intent(this, InspectionPunchListActivity::class.java))
        }
        
        findViewById<LinearLayout>(R.id.btnPropertyIQ).setOnClickListener {
            startActivity(Intent(this, PropertyIQActivity::class.java))
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
