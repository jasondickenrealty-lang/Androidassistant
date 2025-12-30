package com.example.androidassistant

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        supportActionBar?.hide()
        
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }
        
        findViewById<MaterialButton>(R.id.btnSend).setOnClickListener {
            val messageInput = findViewById<EditText>(R.id.messageInput)
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                Toast.makeText(this, "Message sent: $message", Toast.LENGTH_SHORT).show()
                messageInput.text.clear()
            }
        }
    }
}
