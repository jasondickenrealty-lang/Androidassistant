package com.example.androidassistant

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PropertyDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_propertydetail)
        
        supportActionBar?.hide()
        
        // Get property data from intent
        val price = intent.getStringExtra("price") ?: "$425,000"
        val address = intent.getStringExtra("address") ?: "123 Oak Street, Austin, TX 78701"
        val beds = intent.getIntExtra("beds", 3)
        val baths = intent.getIntExtra("baths", 2)
        val sqft = intent.getIntExtra("sqft", 1850)
        
        // Populate views
        findViewById<TextView>(R.id.propertyPrice).text = price
        findViewById<TextView>(R.id.propertyAddress).text = address
        findViewById<TextView>(R.id.bedsCount).text = "$beds Beds"
        findViewById<TextView>(R.id.bathsCount).text = "$baths Baths"
        findViewById<TextView>(R.id.sqftCount).text = "${String.format("%,d", sqft)} sqft"
        
        // Back button
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }
        
        // Save button
        findViewById<TextView>(R.id.btnSave).setOnClickListener {
            Toast.makeText(this, "Property saved to favorites!", Toast.LENGTH_SHORT).show()
        }
        
        // Contact Agent button
        findViewById<MaterialButton>(R.id.btnContactAgent).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        
        // Schedule Tour button
        findViewById<MaterialButton>(R.id.btnScheduleTour).setOnClickListener {
            Toast.makeText(this, "Tour scheduling coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}
