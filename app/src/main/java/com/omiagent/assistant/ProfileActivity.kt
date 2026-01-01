package com.omiagent.assistant

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        supportActionBar?.hide()
        
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}
