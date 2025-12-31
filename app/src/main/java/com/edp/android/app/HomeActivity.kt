package com.edp.android.app

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
    }
}
