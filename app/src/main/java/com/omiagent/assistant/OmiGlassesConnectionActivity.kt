package com.omiagent.assistant

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class OmiDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val device: BluetoothDevice,
    val deviceType: String // "omi", "omi-glass", "friend"
)

class OmiGlassesConnectionActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    private lateinit var btnScan: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var devicesContainer: LinearLayout
    private lateinit var emptyStateText: TextView
    private lateinit var scanStatusText: TextView
    
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val discoveredDevices = mutableMapOf<String, OmiDevice>()
    private var isScanning = false
    
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD: Long = 10000 // 10 seconds
    
    // Omi device identifiers (based on Omi open-source repo)
    private val OMI_DEVICE_NAMES = listOf(
        "Friend", "Omi", "omi", "friend",
        "Friend DevKit 2", "Omi DevKit 2",
        "Omi Glass", "omiGlass", "OmiGlass"
    )
    
    // Standard Omi BLE Service UUIDs (from Omi documentation)
    private val OMI_SERVICE_UUID = "19B10000-E8F2-537E-4F6C-D104768A1214"
    private val OMI_AUDIO_DATA_UUID = "19B10001-E8F2-537E-4F6C-D104768A1214"
    
    companion object {
        private const val TAG = "OmiGlassesConnection"
        private const val REQUEST_BLUETOOTH_PERMISSIONS = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_omi_glasses_connection)
        
        supportActionBar?.hide()
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        if (auth.currentUser == null) {
            finish()
            return
        }
        
        initializeViews()
        initializeBluetooth()
        checkBluetoothPermissions()
    }
    
    private fun initializeViews() {
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            stopScanning()
            finish()
        }
        
        btnScan = findViewById(R.id.btnScanDevices)
        progressBar = findViewById(R.id.progressBar)
        devicesContainer = findViewById(R.id.devicesContainer)
        emptyStateText = findViewById(R.id.emptyStateText)
        scanStatusText = findViewById(R.id.scanStatusText)
        
        btnScan.setOnClickListener {
            if (isScanning) {
                stopScanning()
            } else {
                startScanning()
            }
        }
    }
    
    private fun initializeBluetooth() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        if (bluetoothAdapter?.isEnabled == false) {
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_LONG).show()
        }
        
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    }
    
    private fun checkBluetoothPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                REQUEST_BLUETOOTH_PERMISSIONS
            )
            return false
        }
        
        return true
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted. You can now scan for devices.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth permissions are required to scan for devices", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun startScanning() {
        if (!checkBluetoothPermissions()) {
            return
        }
        
        if (bluetoothAdapter?.isEnabled == false) {
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        
        discoveredDevices.clear()
        updateDevicesList()
        
        isScanning = true
        btnScan.text = "Stop Scan"
        progressBar.visibility = View.VISIBLE
        scanStatusText.visibility = View.VISIBLE
        scanStatusText.text = "Scanning for Omi devices..."
        emptyStateText.visibility = View.GONE
        
        // Use scan filters to find Omi devices more efficiently
        val scanFilters = mutableListOf<ScanFilter>()
        
        // Add filter for service UUID if available
        // Note: Some Omi devices may not advertise service UUIDs
        
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        try {
            bluetoothLeScanner?.startScan(scanFilters, scanSettings, leScanCallback)
            
            // Stop scanning after SCAN_PERIOD
            handler.postDelayed({
                stopScanning()
            }, SCAN_PERIOD)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting scan", e)
            Toast.makeText(this, "Failed to start scanning: ${e.message}", Toast.LENGTH_SHORT).show()
            isScanning = false
            btnScan.text = "Start Scan"
            progressBar.visibility = View.GONE
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun stopScanning() {
        if (!isScanning) return
        
        isScanning = false
        btnScan.text = "Start Scan"
        progressBar.visibility = View.GONE
        scanStatusText.visibility = View.GONE
        
        try {
            bluetoothLeScanner?.stopScan(leScanCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping scan", e)
        }
        
        if (discoveredDevices.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
            emptyStateText.text = "No Omi devices found. Make sure your device is turned on and in pairing mode."
        }
    }
    
    private val leScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            
            val device = result.device
            val deviceName = device.name ?: return
            
            // Check if this is an Omi device
            val isOmiDevice = OMI_DEVICE_NAMES.any { name ->
                deviceName.contains(name, ignoreCase = true)
            }
            
            if (isOmiDevice && !discoveredDevices.containsKey(device.address)) {
                val deviceType = when {
                    deviceName.contains("Glass", ignoreCase = true) -> "omi-glass"
                    deviceName.contains("Friend", ignoreCase = true) -> "friend"
                    else -> "omi"
                }
                
                val omiDevice = OmiDevice(
                    name = deviceName,
                    address = device.address,
                    rssi = result.rssi,
                    device = device,
                    deviceType = deviceType
                )
                
                discoveredDevices[device.address] = omiDevice
                
                runOnUiThread {
                    updateDevicesList()
                }
                
                Log.d(TAG, "Found Omi device: $deviceName (${device.address}), RSSI: ${result.rssi}")
            }
        }
        
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "Scan failed with error: $errorCode")
            
            runOnUiThread {
                isScanning = false
                btnScan.text = "Start Scan"
                progressBar.visibility = View.GONE
                scanStatusText.visibility = View.GONE
                
                val errorMessage = when (errorCode) {
                    SCAN_FAILED_ALREADY_STARTED -> "Scan already in progress"
                    SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "App registration failed"
                    SCAN_FAILED_FEATURE_UNSUPPORTED -> "BLE scanning not supported"
                    SCAN_FAILED_INTERNAL_ERROR -> "Internal error occurred"
                    else -> "Unknown error ($errorCode)"
                }
                
                Toast.makeText(this@OmiGlassesConnectionActivity, "Scan failed: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun updateDevicesList() {
        devicesContainer.removeAllViews()
        
        if (discoveredDevices.isEmpty()) {
            if (!isScanning) {
                emptyStateText.visibility = View.VISIBLE
            }
            return
        }
        
        emptyStateText.visibility = View.GONE
        
        discoveredDevices.values.sortedByDescending { it.rssi }.forEach { device ->
            val deviceView = layoutInflater.inflate(R.layout.item_omi_device, devicesContainer, false)
            
            val deviceIcon = deviceView.findViewById<TextView>(R.id.deviceIcon)
            val deviceNameText = deviceView.findViewById<TextView>(R.id.deviceName)
            val deviceTypeText = deviceView.findViewById<TextView>(R.id.deviceType)
            val deviceRssiText = deviceView.findViewById<TextView>(R.id.deviceRssi)
            val btnConnect = deviceView.findViewById<MaterialButton>(R.id.btnConnect)
            
            deviceIcon.text = when (device.deviceType) {
                "omi-glass" -> "👓"
                "friend" -> "🎧"
                else -> "📱"
            }
            
            deviceNameText.text = device.name
            deviceTypeText.text = when (device.deviceType) {
                "omi-glass" -> "Omi Glass"
                "friend" -> "Friend Device"
                else -> "Omi Device"
            }
            
            val signalStrength = when {
                device.rssi > -60 -> "Excellent"
                device.rssi > -70 -> "Good"
                device.rssi > -80 -> "Fair"
                else -> "Weak"
            }
            deviceRssiText.text = "$signalStrength (${device.rssi} dBm)"
            
            btnConnect.setOnClickListener {
                connectToDevice(device)
            }
            
            devicesContainer.addView(deviceView)
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: OmiDevice) {
        if (!checkBluetoothPermissions()) {
            return
        }
        
        // Show dialog to name the device
        showNameDeviceDialog(device)
    }
    
    private fun showNameDeviceDialog(device: OmiDevice) {
        val input = EditText(this)
        input.hint = "My Omi Glasses"
        input.setText(device.name)
        input.setPadding(50, 20, 50, 20)
        
        AlertDialog.Builder(this)
            .setTitle("Name Your Device")
            .setMessage("Give your ${device.name} a custom name:")
            .setView(input)
            .setPositiveButton("Connect") { _, _ ->
                val customName = input.text.toString().trim().ifEmpty { device.name }
                connectToDeviceWithName(device, customName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    @SuppressLint("MissingPermission")
    private fun connectToDeviceWithName(device: OmiDevice, customName: String) {
        // Show connection dialog
        val progressDialog = AlertDialog.Builder(this)
            .setTitle("Connecting")
            .setMessage("Connecting to $customName...")
            .setCancelable(false)
            .create()
        progressDialog.show()
        
        // Disconnect any existing connection
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        
        // Connect to the device
        bluetoothGatt = device.device.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                
                runOnUiThread {
                    progressDialog.dismiss()
                    
                    when (newState) {
                        BluetoothProfile.STATE_CONNECTED -> {
                            Log.d(TAG, "Connected to $customName")
                            Toast.makeText(
                                this@OmiGlassesConnectionActivity,
                                "Connected to $customName",
                                Toast.LENGTH_SHORT
                            ).show()
                            
                            // Discover services
                            gatt.discoverServices()
                            
                            // Save paired device to Firestore
                            savePairedDevice(device, customName)
                        }
                        BluetoothProfile.STATE_DISCONNECTED -> {
                            Log.d(TAG, "Disconnected from ${device.name}")
                            Toast.makeText(
                                this@OmiGlassesConnectionActivity,
                                "Disconnected from ${device.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                super.onServicesDiscovered(gatt, status)
                
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "Services discovered for ${device.name}")
                    
                    // List all discovered services
                    gatt.services.forEach { service ->
                        Log.d(TAG, "Service UUID: ${service.uuid}")
                        service.characteristics.forEach { char ->
                            Log.d(TAG, "  Characteristic UUID: ${char.uuid}")
                        }
                    }
                    
                    runOnUiThread {
                        Toast.makeText(
                            this@OmiGlassesConnectionActivity,
                            "Device ready! Services discovered.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                super.onCharacteristicChanged(gatt, characteristic)
                // Handle audio data or other notifications here
                Log.d(TAG, "Characteristic changed: ${characteristic.uuid}")
            }
        })
    }
    
    private fun savePairedDevice(device: OmiDevice, customName: String) {
        val uid = auth.currentUser?.uid ?: return
        
        TenantManager.getCurrentUserCompanyId { companyId ->
            val deviceData = hashMapOf(
                "name" to device.name,
                "customName" to customName,
                "address" to device.address,
                "type" to device.deviceType,
                "companyId" to companyId,
                "pairedAt" to System.currentTimeMillis(),
                "lastConnected" to System.currentTimeMillis()
            )
        
        db.collection("users").document(uid)
            .collection("pairedDevices")
            .document(device.address)
            .set(deviceData)
            .addOnSuccessListener {
                Log.d(TAG, "Device saved to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving device to Firestore", e)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopScanning()
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
    }
}
