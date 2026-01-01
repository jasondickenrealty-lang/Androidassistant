package com.omiagent.assistant

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class Recording(
    val id: String = "",
    val title: String = "",
    val duration: String = "",
    val date: String = "",
    val audioUrl: String = "",
    val transcript: String = "",
    val createdAt: String = ""
)

class MyRecordingsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recordingsContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    
    private val recordings = mutableListOf<Recording>()
    
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
        
        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
        
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
                recordings.clear()
                
                for (document in documents) {
                    val recording = Recording(
                        id = document.id,
                        title = document.getString("title") ?: "Untitled Recording",
                        duration = document.getString("duration") ?: "0:00",
                        date = document.getString("date") ?: "",
                        audioUrl = document.getString("audioUrl") ?: "",
                        transcript = document.getString("transcript") ?: "",
                        createdAt = document.getString("createdAt") ?: ""
                    )
                    recordings.add(recording)
                }
                
                updateRecordingsUI()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading recordings: ${e.message}", Toast.LENGTH_SHORT).show()
                updateRecordingsUI()
            }
    }
    
    private fun updateRecordingsUI() {
        recordingsContainer.removeAllViews()
        
        if (recordings.isEmpty()) {
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
