package com.omiagent.assistant

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*

data class Recording(
    val id: String = "",
    val title: String = "",
    val duration: String = "",
    val date: String = "",
    val audioUrl: String = "",
    val transcript: String = "",
    val createdAt: String = "",
    val propertyAddress: String = "",
    val clientName: String = ""
)

class MyRecordingsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recordingsContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var searchInput: EditText
    private lateinit var filterButton: ImageButton
    
    private val originalRecordings = mutableListOf<Recording>()
    private val recordings = mutableListOf<Recording>()
    private var currentTab = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_recordings)
        
        supportActionBar?.hide()
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        if (auth.currentUser == null) {
            finish()
            return
        }
        
        // Initialize views
        recordingsContainer = findViewById(R.id.recordingsContainer)
        progressBar = findViewById(R.id.progressBar)
        emptyStateText = findViewById(R.id.emptyStateText)
        searchInput = findViewById(R.id.searchRecordings)
        filterButton = findViewById(R.id.btnFilter)
        
        // Search Listener
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterRecordings(s.toString())
            }
        })
        
        // Filter Button
        filterButton.setOnClickListener {
            showFilterDialog()
        }
        
        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Tab Layout
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                updateRecordingsUI()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        
        loadRecordings()
    }
    
    private fun loadRecordings() {
        progressBar.visibility = View.VISIBLE
        emptyStateText.visibility = View.GONE
        
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("recordings")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE
                originalRecordings.clear()
                
                for (document in documents) {
                    val recording = Recording(
                        id = document.id,
                        title = document.getString("title") ?: "Untitled Recording",
                        duration = document.getString("duration") ?: "0:00",
                        date = document.getString("date") ?: "",
                        audioUrl = document.getString("audioUrl") ?: "",
                        transcript = document.getString("transcript") ?: "",
                        createdAt = document.getString("createdAt") ?: "",
                        propertyAddress = document.getString("propertyAddress") ?: "",
                        clientName = document.getString("clientName") ?: ""
                    )
                    originalRecordings.add(recording)
                }
                
                // Initial filter (show all)
                filterRecordings("")
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading recordings: ${e.message}", Toast.LENGTH_SHORT).show()
                updateRecordingsUI()
            }
    }
    
    private fun updateRecordingsUI() {
        recordingsContainer.removeAllViews()
        
        if (currentTab == 0) {
            // Audio
            if (recordings.isEmpty()) {
                emptyStateText.text = "No recordings found"
                emptyStateText.visibility = View.VISIBLE
            } else {
                emptyStateText.visibility = View.GONE
                
                recordings.forEach { recording ->
                    val recordingView = layoutInflater.inflate(R.layout.item_recording, recordingsContainer, false)
                    
                    val titleText = recordingView.findViewById<TextView>(R.id.recordingTitle)
                    val dateText = recordingView.findViewById<TextView>(R.id.recordingDate)
                    val durationText = recordingView.findViewById<TextView>(R.id.recordingDuration)
                    val playButton = recordingView.findViewById<ImageButton>(R.id.btnPlayRecording)
                    
                    titleText.text = recording.title
                    dateText.text = formatDate(recording.createdAt)
                    durationText.text = recording.duration
                    
                    playButton.setOnClickListener {
                        Toast.makeText(this, "Play recording: ${recording.title}", Toast.LENGTH_SHORT).show()
                        // TODO: Implement audio playback
                    }
                    
                    recordingView.setOnClickListener {
                        showRecordingDetails(recording)
                    }
                    
                    recordingsContainer.addView(recordingView)
                }
            }
        } else if (currentTab == 1) {
            // Video
            emptyStateText.text = "No videos found"
            emptyStateText.visibility = View.VISIBLE
        } else {
            // Photos
            emptyStateText.text = "No photos found"
            emptyStateText.visibility = View.VISIBLE
        }
    }
    
    private fun filterRecordings(query: String) {
        recordings.clear()
        if (query.isEmpty()) {
            recordings.addAll(originalRecordings)
        } else {
            val lowerQuery = query.lowercase(Locale.getDefault())
            originalRecordings.forEach { recording ->
                if (recording.title.lowercase(Locale.getDefault()).contains(lowerQuery) ||
                    recording.propertyAddress.lowercase(Locale.getDefault()).contains(lowerQuery) ||
                    recording.clientName.lowercase(Locale.getDefault()).contains(lowerQuery) ||
                    recording.date.lowercase(Locale.getDefault()).contains(lowerQuery)) {
                    recordings.add(recording)
                }
            }
        }
        updateRecordingsUI()
    }

    private fun showFilterDialog() {
        val options = arrayOf("All", "Last 7 Days", "Last 30 Days", "This Year")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Filter by Date")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> filterRecordings(searchInput.text.toString()) // All
                1 -> filterByDateRange(7)
                2 -> filterByDateRange(30)
                3 -> filterByDateRange(365)
            }
        }
        builder.show()
    }

    private fun filterByDateRange(days: Int) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val cutoffDate = calendar.time
        
        recordings.clear()
        val currentSearch = searchInput.text.toString().lowercase(Locale.getDefault())
        
        originalRecordings.forEach { recording ->
            // Check search query first
            val matchesSearch = currentSearch.isEmpty() || 
                recording.title.lowercase(Locale.getDefault()).contains(currentSearch) ||
                recording.propertyAddress.lowercase(Locale.getDefault()).contains(currentSearch) ||
                recording.clientName.lowercase(Locale.getDefault()).contains(currentSearch)
            
            if (matchesSearch) {
                try {
                    val recordingDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(recording.createdAt)
                    if (recordingDate != null && recordingDate.after(cutoffDate)) {
                        recordings.add(recording)
                    }
                } catch (e: Exception) {
                    // If date parsing fails, include it (or exclude, depending on preference)
                    recordings.add(recording)
                }
            }
        }
        updateRecordingsUI()
    }

    private fun formatDate(dateString: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dateString)
            SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
    
    private fun showRecordingDetails(recording: Recording) {
        Toast.makeText(this, "Recording details - Coming soon", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to recording details activity or show dialog
    }
}
