package com.omiagent.assistant

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        supportActionBar?.hide()
        
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }
        
        findViewById<LinearLayout>(R.id.btnPairDevices).setOnClickListener {
            val intent = Intent(this, PairDevicesActivity::class.java)
            startActivity(intent)
        }
    }
}
