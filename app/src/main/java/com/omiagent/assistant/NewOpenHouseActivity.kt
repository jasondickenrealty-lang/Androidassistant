package com.omiagent.assistant

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NewOpenHouseActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_open_house)
        
        supportActionBar?.hide()
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }
        
        findViewById<LinearLayout>(R.id.btnTakePhoto).setOnClickListener {
            Toast.makeText(this, "Camera feature coming soon", Toast.LENGTH_SHORT).show()
            // TODO: Implement Camera Intent
        }
        
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveOpenHouse()
        }
        
        // Pre-fill date with today
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        findViewById<TextInputEditText>(R.id.inputDate).setText(today)
    }
    
    private fun saveOpenHouse() {
        val name = findViewById<TextInputEditText>(R.id.inputName).text.toString()
        val address = findViewById<TextInputEditText>(R.id.inputAddress).text.toString()
        val price = findViewById<TextInputEditText>(R.id.inputPrice).text.toString()
        val date = findViewById<TextInputEditText>(R.id.inputDate).text.toString()
        val notes = findViewById<TextInputEditText>(R.id.inputNotes).text.toString()
        
        if (address.isEmpty()) {
            Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show()
            return
        }
        
        val uid = auth.currentUser?.uid ?: return
        
        val openHouse = hashMapOf(
            "name" to name,
            "address" to address,
            "price" to price,
            "date" to date,
            "notes" to notes,
            "createdAt" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()),
            "imageUrl" to "" // Placeholder
        )
        
        db.collection("users").document(uid)
            .collection("open_houses")
            .add(openHouse)
            .addOnSuccessListener {
                Toast.makeText(this, "Open House saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
