package com.omiagent.assistant

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OpenHouseDetailActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_house_detail)
        
        supportActionBar?.hide()
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }
        
        val openHouseId = intent.getStringExtra("open_house_id")
        if (openHouseId != null) {
            loadOpenHouseDetails(openHouseId)
        } else {
            Toast.makeText(this, "Error: No Open House ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun loadOpenHouseDetails(id: String) {
        val uid = auth.currentUser?.uid ?: return
        
        db.collection("users").document(uid)
            .collection("open_houses").document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    findViewById<TextView>(R.id.detailAddress).text = document.getString("address")
                    findViewById<TextView>(R.id.detailPrice).text = document.getString("price")
                    findViewById<TextView>(R.id.detailDate).text = document.getString("date")
                    findViewById<TextView>(R.id.detailNotes).text = document.getString("notes") ?: "No notes."
                    
                    // Bullet points - assuming stored as a list or string
                    // If it's a list in Firestore:
                    val bullets = document.get("conversationPoints") as? List<String>
                    if (bullets != null && bullets.isNotEmpty()) {
                        val bulletText = bullets.joinToString("\n• ", prefix = "• ")
                        findViewById<TextView>(R.id.detailBullets).text = bulletText
                    } else {
                        // Fallback or check for string field
                        findViewById<TextView>(R.id.detailBullets).text = document.getString("conversationPointsString") ?: "• No conversation points yet."
                    }
                } else {
                    Toast.makeText(this, "Open House not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
