package com.omiagent.assistant

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class PairedDevice(
    val id: String = "",
    val name: String = "",
    val customName: String = "",
    val address: String = "",
    val type: String = "",
    val pairedAt: Long = 0,
    val lastConnected: Long = 0
)

class PairedDevicesActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var devicesContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    
    private val pairedDevices = mutableListOf<PairedDevice>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paired_devices)
        
        supportActionBar?.hide()
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        if (auth.currentUser == null) {
            finish()
            return
        }
        
        initializeViews()
        loadPairedDevices()
    }
    
    private fun initializeViews() {
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }
        
        devicesContainer = findViewById(R.id.devicesContainer)
        progressBar = findViewById(R.id.progressBar)
        emptyStateText = findViewById(R.id.emptyStateText)
    }
    
    private fun loadPairedDevices() {
        progressBar.visibility = View.VISIBLE
        emptyStateText.visibility = View.GONE
        
        val uid = auth.currentUser?.uid ?: return
        
        db.collection("users").document(uid)
            .collection("pairedDevices")
            .orderBy("lastConnected", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Toast.makeText(this, "Error loading devices: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                
                pairedDevices.clear()
                
                snapshot?.documents?.forEach { doc ->
                    val device = PairedDevice(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unknown Device",
                        customName = doc.getString("customName") ?: doc.getString("name") ?: "Unknown Device",
                        address = doc.getString("address") ?: "",
                        type = doc.getString("type") ?: "omi",
                        pairedAt = doc.getLong("pairedAt") ?: 0,
                        lastConnected = doc.getLong("lastConnected") ?: 0
                    )
                    pairedDevices.add(device)
                }
                
                updateDevicesList()
            }
    }
    
    private fun updateDevicesList() {
        devicesContainer.removeAllViews()
        
        if (pairedDevices.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
            emptyStateText.text = "No paired devices.\nPair your Omi glasses from Settings."
            return
        }
        
        emptyStateText.visibility = View.GONE
        
        pairedDevices.forEach { device ->
            val deviceView = layoutInflater.inflate(R.layout.item_paired_device, devicesContainer, false)
            
            val deviceIcon = deviceView.findViewById<TextView>(R.id.deviceIcon)
            val deviceName = deviceView.findViewById<TextView>(R.id.deviceName)
            val deviceAddress = deviceView.findViewById<TextView>(R.id.deviceAddress)
            val deviceLastConnected = deviceView.findViewById<TextView>(R.id.deviceLastConnected)
            val btnRename = deviceView.findViewById<MaterialButton>(R.id.btnRename)
            val btnDelete = deviceView.findViewById<MaterialButton>(R.id.btnDelete)
            
            deviceIcon.text = when (device.type) {
                "omi-glass" -> "👓"
                "friend" -> "🎧"
                else -> "📱"
            }
            
            deviceName.text = device.customName
            deviceAddress.text = device.address
            deviceLastConnected.text = "Last connected: ${formatDate(device.lastConnected)}"
            
            btnRename.setOnClickListener {
                showRenameDialog(device)
            }
            
            btnDelete.setOnClickListener {
                showDeleteConfirmation(device)
            }
            
            // Make the entire card clickable to open device details
            deviceView.setOnClickListener {
                openDeviceDetails(device)
            }
            
            devicesContainer.addView(deviceView)
        }
    }
    
    private fun openDeviceDetails(device: PairedDevice) {
        // Only open control screen for glasses type devices
        if (device.type == "omi-glass") {
            val intent = android.content.Intent(this, GlassesControlActivity::class.java).apply {
                putExtra("DEVICE_ID", device.id)
                putExtra("DEVICE_NAME", device.customName)
                putExtra("DEVICE_ADDRESS", device.address)
                putExtra("DEVICE_TYPE", device.type)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Control panel available for glasses only", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showRenameDialog(device: PairedDevice) {
        val input = EditText(this)
        input.setText(device.customName)
        input.setPadding(50, 20, 50, 20)
        input.selectAll()
        
        AlertDialog.Builder(this)
            .setTitle("Rename Device")
            .setMessage("Enter a new name for your device:")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    updateDeviceName(device, newName)
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
        
        // Show keyboard
        input.requestFocus()
    }
    
    private fun updateDeviceName(device: PairedDevice, newName: String) {
        val uid = auth.currentUser?.uid ?: return
        
        db.collection("users").document(uid)
            .collection("pairedDevices")
            .document(device.id)
            .update("customName", newName)
            .addOnSuccessListener {
                Toast.makeText(this, "Device renamed to \"$newName\"", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error renaming device: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun showDeleteConfirmation(device: PairedDevice) {
        AlertDialog.Builder(this)
            .setTitle("Remove Device")
            .setMessage("Are you sure you want to remove \"${device.customName}\"?")
            .setPositiveButton("Remove") { _, _ ->
                deleteDevice(device)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteDevice(device: PairedDevice) {
        val uid = auth.currentUser?.uid ?: return
        
        db.collection("users").document(uid)
            .collection("pairedDevices")
            .document(device.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "\"${device.customName}\" removed", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error removing device: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "Never"
        
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000} minutes ago"
            diff < 86400000 -> "${diff / 3600000} hours ago"
            diff < 604800000 -> "${diff / 86400000} days ago"
            else -> {
                val date = Date(timestamp)
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
            }
        }
    }
}
