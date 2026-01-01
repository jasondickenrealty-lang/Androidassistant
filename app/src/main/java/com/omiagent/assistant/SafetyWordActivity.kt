package com.omiagent.assistant

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SafetyWordActivity : AppCompatActivity() {

    private lateinit var inputKeyWord: TextInputEditText
    private lateinit var inputPhoneNumber: TextInputEditText
    private lateinit var inputMessage: TextInputEditText
    private lateinit var btnSave: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safety_word)

        supportActionBar?.hide()

        // Initialize Views
        inputKeyWord = findViewById(R.id.inputKeyWord)
        inputPhoneNumber = findViewById(R.id.inputPhoneNumber)
        inputMessage = findViewById(R.id.inputMessage)
        btnSave = findViewById(R.id.btnSaveSafetySettings)

        // Back Button
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Load Saved Settings
        loadSettings()

        // Save Button Listener
        btnSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadSettings() {
        val sharedPref = getSharedPreferences("SafetySettings", Context.MODE_PRIVATE)
        inputKeyWord.setText(sharedPref.getString("KEY_WORD", ""))
        inputPhoneNumber.setText(sharedPref.getString("PHONE_NUMBER", ""))
        inputMessage.setText(sharedPref.getString("MESSAGE", ""))
    }

    private fun saveSettings() {
        val keyWord = inputKeyWord.text.toString().trim()
        val phoneNumber = inputPhoneNumber.text.toString().trim()
        val message = inputMessage.text.toString().trim()

        if (keyWord.isEmpty() || phoneNumber.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = getSharedPreferences("SafetySettings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("KEY_WORD", keyWord)
            putString("PHONE_NUMBER", phoneNumber)
            putString("MESSAGE", message)
            apply()
        }

        Toast.makeText(this, "Safety settings saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}
