package com.omiagent.assistant

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class GlassesControlActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    private var deviceId: String = ""
    private var deviceName: String = ""
    private var deviceAddress: String = ""
    private var deviceType: String = ""
    
    private var isRecording = false
    private var recordingStartTime: Long = 0
    private lateinit var recordingTimer: Timer
    
    private lateinit var deviceNameText: TextView
    private lateinit var deviceAddressText: TextView
    private lateinit var connectionStatusText: TextView
    private lateinit var btnStartRecording: MaterialButton
    private lateinit var recordingDurationText: TextView
    private lateinit var recordingIndicator: View
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glasses_control)
        
        supportActionBar?.hide()
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        // Get device info from intent
        deviceId = intent.getStringExtra("DEVICE_ID") ?: ""
        deviceName = intent.getStringExtra("DEVICE_NAME") ?: "Unknown Device"
        deviceAddress = intent.getStringExtra("DEVICE_ADDRESS") ?: ""
        deviceType = intent.getStringExtra("DEVICE_TYPE") ?: ""
        
        if (deviceId.isEmpty()) {
            finish()
            return
        }
        
        initializeViews()
        loadDeviceStatus()
    }
    
    private fun initializeViews() {
        deviceNameText = findViewById(R.id.deviceNameText)
        deviceAddressText = findViewById(R.id.deviceAddressText)
        connectionStatusText = findViewById(R.id.connectionStatusText)
        btnStartRecording = findViewById(R.id.btnStartRecording)
        recordingDurationText = findViewById(R.id.recordingDurationText)
        recordingIndicator = findViewById(R.id.recordingIndicator)
        
        deviceNameText.text = deviceName
        deviceAddressText.text = deviceAddress
        
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
        
        btnStartRecording.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
        
        findViewById<MaterialButton>(R.id.btnViewRecordings).setOnClickListener {
            openMyRecordings()
        }
        
        findViewById<MaterialButton>(R.id.btnConnectDevice)?.setOnClickListener {
            connectToDevice()
        }
    }
    
    private fun loadDeviceStatus() {
        // Check if device is connected
        // For now, assume it's connected
        connectionStatusText.text = "Connected"
        connectionStatusText.setTextColor(getColor(android.R.color.holo_green_dark))
    }
    
    private fun startRecording() {
        isRecording = true
        recordingStartTime = System.currentTimeMillis()
        
        // Update UI
        btnStartRecording.text = "Stop Recording"
        btnStartRecording.setBackgroundColor(getColor(android.R.color.holo_red_dark))
        recordingIndicator.visibility = View.VISIBLE
        recordingDurationText.visibility = View.VISIBLE
        
        // Start timer to update duration
        recordingTimer = Timer()
        recordingTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    updateRecordingDuration()
                }
            }
        }, 0, 1000)
        
        Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
    }
    
    private fun stopRecording() {
        isRecording = false
        recordingTimer.cancel()
        
        // Calculate duration
        val duration = System.currentTimeMillis() - recordingStartTime
        val durationSeconds = (duration / 1000).toInt()
        
        // Update UI
        btnStartRecording.text = "Start Recording"
        btnStartRecording.setBackgroundColor(getColor(com.google.android.material.R.color.design_default_color_primary))
        recordingIndicator.visibility = View.GONE
        recordingDurationText.visibility = View.GONE
        recordingDurationText.text = "00:00"
        
        // Save recording to Firestore
        saveRecording(durationSeconds)
    }
    
    private fun updateRecordingDuration() {
        val duration = System.currentTimeMillis() - recordingStartTime
        val seconds = (duration / 1000) % 60
        val minutes = (duration / 1000) / 60
        recordingDurationText.text = String.format("%02d:%02d", minutes, seconds)
    }
    
    private fun saveRecording(durationSeconds: Int) {
        val uid = auth.currentUser?.uid ?: return
        
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        val durationStr = String.format("%d:%02d", minutes, seconds)
        
        val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        
        val recording = hashMapOf(
            "title" to "Recording from $deviceName",
            "duration" to durationStr,
            "date" to currentDate,
            "deviceId" to deviceId,
            "deviceName" to deviceName,
            "createdAt" to System.currentTimeMillis(),
            "audioUrl" to "",
            "transcript" to ""
        )
        
        db.collection("users").document(uid)
            .collection("recordings")
            .add(recording)
            .addOnSuccessListener {
                Toast.makeText(this, "Recording saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving recording: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun openMyRecordings() {
        val intent = Intent(this, MyRecordingsActivity::class.java).apply {
            putExtra("FILTER_DEVICE_ID", deviceId)
            putExtra("FILTER_DEVICE_NAME", deviceName)
        }
        startActivity(intent)
    }
    
    private fun connectToDevice() {
        // Show connection dialog or initiate connection
        Toast.makeText(this, "Connecting to $deviceName...", Toast.LENGTH_SHORT).show()
        
        // Update connection status
        connectionStatusText.text = "Connecting..."
        connectionStatusText.setTextColor(getColor(android.R.color.holo_orange_dark))
        
        // Simulate connection (in real app, implement actual BLE connection)
        findViewById<ImageButton>(R.id.btnBack).postDelayed({
            connectionStatusText.text = "Connected"
            connectionStatusText.setTextColor(getColor(android.R.color.holo_green_dark))
            Toast.makeText(this, "Connected to $deviceName", Toast.LENGTH_SHORT).show()
        }, 2000)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) {
            stopRecording()
        }
    }
}
