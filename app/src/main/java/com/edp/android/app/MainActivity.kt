package com.edp.android.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        supportActionBar?.hide()
        
        findViewById<Button>(R.id.btnGetStarted).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}
