package com.omiagent.assistant

import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PairDevicesActivity : AppCompatActivity() {
    
    private lateinit var checkboxSmartGlasses: CheckBox
    private lateinit var checkboxSmartAssistant: CheckBox
    private lateinit var btnPairDevices: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_devices)
        
        supportActionBar?.hide()
        
        // Initialize views
        checkboxSmartGlasses = findViewById(R.id.checkboxSmartGlasses)
        checkboxSmartAssistant = findViewById(R.id.checkboxSmartAssistant)
        btnPairDevices = findViewById(R.id.btnPairDevices)
        
        // Back button
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }
        
        // Pair button
        btnPairDevices.setOnClickListener {
            pairSelectedDevices()
        }
        
        // Update button state when checkboxes change
        checkboxSmartGlasses.setOnCheckedChangeListener { _, _ ->
            updatePairButtonState()
        }
        
        checkboxSmartAssistant.setOnCheckedChangeListener { _, _ ->
            updatePairButtonState()
        }
        
        // Initial button state
        updatePairButtonState()
    }
    
    private fun updatePairButtonState() {
        btnPairDevices.isEnabled = checkboxSmartGlasses.isChecked || checkboxSmartAssistant.isChecked
    }
    
    private fun pairSelectedDevices() {
        val devicesToPair = mutableListOf<String>()
        
        if (checkboxSmartGlasses.isChecked) {
            devicesToPair.add("Smart Glasses")
        }
        
        if (checkboxSmartAssistant.isChecked) {
            devicesToPair.add("Smart Assistant")
        }
        
        if (devicesToPair.isEmpty()) {
            Toast.makeText(this, "Please select at least one device", Toast.LENGTH_SHORT).show()
            return
        }
        
        // TODO: Implement actual pairing logic here
        // For now, just show a success message
        val message = "Pairing ${devicesToPair.joinToString(" and ")}..."
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        
        // Simulate pairing success
        btnPairDevices.postDelayed({
            Toast.makeText(this, "Devices paired successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }, 2000)
    }
}
