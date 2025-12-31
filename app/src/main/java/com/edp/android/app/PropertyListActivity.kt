package com.edp.android.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class PropertyListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_propertylist)
        
        supportActionBar?.hide()
        
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }
        
        // Property click handlers
        val propertyClickListener = { id: Int, price: String, address: String, beds: Int, baths: Int, sqft: Int ->
            val intent = Intent(this, PropertyDetailActivity::class.java)
            intent.putExtra("property_id", id)
            intent.putExtra("price", price)
            intent.putExtra("address", address)
            intent.putExtra("beds", beds)
            intent.putExtra("baths", baths)
            intent.putExtra("sqft", sqft)
            startActivity(intent)
        }
        
        findViewById<CardView>(R.id.property1).setOnClickListener {
            propertyClickListener(1, "$425,000", "123 Oak Street, Austin, TX", 3, 2, 1850)
        }
        
        findViewById<CardView>(R.id.property2).setOnClickListener {
            propertyClickListener(2, "$2,200/mo", "456 Maple Ave, Austin, TX", 2, 1, 950)
        }
        
        findViewById<CardView>(R.id.property3).setOnClickListener {
            propertyClickListener(3, "$1,800/mo", "789 Pine Dr, Austin, TX", 2, 2, 1100)
        }
    }
}
