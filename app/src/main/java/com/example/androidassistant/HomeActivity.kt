package com.example.androidassistant

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        
        supportActionBar?.hide()
        
        // Quick Action Cards
        findViewById<CardView>(R.id.cardBrowse).setOnClickListener {
            startActivity(Intent(this, PropertyListActivity::class.java))
        }
        
        findViewById<CardView>(R.id.cardChat).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        
        findViewById<CardView>(R.id.cardProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        
        // View All button
        findViewById<TextView>(R.id.btnViewAll).setOnClickListener {
            startActivity(Intent(this, PropertyListActivity::class.java))
        }
        
        // Property Cards - navigate to detail
        findViewById<CardView>(R.id.propertyCard1).setOnClickListener {
            val intent = Intent(this, PropertyDetailActivity::class.java)
            intent.putExtra("property_id", 1)
            intent.putExtra("price", "$425,000")
            intent.putExtra("address", "123 Oak Street, Austin, TX 78701")
            intent.putExtra("beds", 3)
            intent.putExtra("baths", 2)
            intent.putExtra("sqft", 1850)
            startActivity(intent)
        }
        
        findViewById<CardView>(R.id.propertyCard2).setOnClickListener {
            val intent = Intent(this, PropertyDetailActivity::class.java)
            intent.putExtra("property_id", 2)
            intent.putExtra("price", "$2,200/mo")
            intent.putExtra("address", "456 Maple Ave, Austin, TX 78702")
            intent.putExtra("beds", 2)
            intent.putExtra("baths", 1)
            intent.putExtra("sqft", 950)
            startActivity(intent)
        }
        
        findViewById<CardView>(R.id.propertyCard3).setOnClickListener {
            val intent = Intent(this, PropertyDetailActivity::class.java)
            intent.putExtra("property_id", 3)
            intent.putExtra("price", "$675,000")
            intent.putExtra("address", "789 Downtown Blvd, Austin, TX 78703")
            intent.putExtra("beds", 4)
            intent.putExtra("baths", 3)
            intent.putExtra("sqft", 2400)
            startActivity(intent)
        }
        
        // Bottom Navigation
        findViewById<LinearLayout>(R.id.navProperties).setOnClickListener {
            startActivity(Intent(this, PropertyListActivity::class.java))
        }
        
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
