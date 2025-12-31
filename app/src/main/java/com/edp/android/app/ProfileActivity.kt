package com.edp.android.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {
    private lateinit var nameText: TextView
    private lateinit var emailText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.hide()

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Reference to TextViews in the profile card
        nameText = findViewById(R.id.profileName)
        emailText = findViewById(R.id.profileEmail)

        fetchProfile()
    }

    private fun fetchProfile() {
        // Example: fetch the first profile in the collection
        val db = Firebase.firestore
        db.collection("profiles")
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents[0]
                    nameText.text = doc.getString("name") ?: "No Name"
                    emailText.text = doc.getString("email") ?: "No Email"
                }
            }
            .addOnFailureListener {
                nameText.text = "Error loading profile"
                emailText.text = ""
            }
    }
}
