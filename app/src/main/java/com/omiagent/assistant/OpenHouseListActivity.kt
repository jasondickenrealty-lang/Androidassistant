package com.omiagent.assistant

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class OpenHouse(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val date: String = "",
    val price: String = "",
    val notes: String = "",
    val imageUrl: String = "",
    val createdAt: String = ""
)

class OpenHouseListActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var container: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var searchInput: EditText
    private lateinit var filterButton: ImageButton
    
    private val originalOpenHouses = mutableListOf<OpenHouse>()
    private val openHouses = mutableListOf<OpenHouse>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_house_list)
        
        supportActionBar?.hide()
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        container = findViewById(R.id.openHousesContainer)
        progressBar = findViewById(R.id.progressBar)
        emptyStateText = findViewById(R.id.emptyStateText)
        searchInput = findViewById(R.id.searchOpenHouses)
        filterButton = findViewById(R.id.btnFilter)
        
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        
        findViewById<FloatingActionButton>(R.id.fabAddOpenHouse).setOnClickListener {
            startActivity(Intent(this, NewOpenHouseActivity::class.java))
        }
        
        findViewById<Button>(R.id.btnNewOpenHouseTop).setOnClickListener {
            startActivity(Intent(this, NewOpenHouseActivity::class.java))
        }
        
        // Search Listener
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterOpenHouses(s.toString())
            }
        })
        
        // Filter Button
        filterButton.setOnClickListener {
            showFilterDialog()
        }
        
        loadOpenHouses()
    }
    
    override fun onResume() {
        super.onResume()
        loadOpenHouses()
    }
    
    private fun loadOpenHouses() {
        val uid = auth.currentUser?.uid ?: return
        progressBar.visibility = View.VISIBLE
        container.removeAllViews()
        
        db.collection("users").document(uid)
            .collection("open_houses")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE
                originalOpenHouses.clear()
                
                for (document in documents) {
                    val openHouse = OpenHouse(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        address = document.getString("address") ?: "",
                        date = document.getString("date") ?: "",
                        price = document.getString("price") ?: "",
                        notes = document.getString("notes") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        createdAt = document.getString("createdAt") ?: ""
                    )
                    originalOpenHouses.add(openHouse)
                }
                
                // Initial filter (show all)
                filterOpenHouses("")
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading open houses", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun updateOpenHousesUI() {
        container.removeAllViews()
        
        if (openHouses.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
        } else {
            emptyStateText.visibility = View.GONE
            openHouses.forEach { openHouse ->
                addOpenHouseView(openHouse)
            }
        }
    }
    
    private fun filterOpenHouses(query: String) {
        openHouses.clear()
        if (query.isEmpty()) {
            openHouses.addAll(originalOpenHouses)
        } else {
            val lowerQuery = query.lowercase(Locale.getDefault())
            originalOpenHouses.forEach { openHouse ->
                if (openHouse.address.lowercase(Locale.getDefault()).contains(lowerQuery) ||
                    openHouse.name.lowercase(Locale.getDefault()).contains(lowerQuery) ||
                    openHouse.date.lowercase(Locale.getDefault()).contains(lowerQuery)) {
                    openHouses.add(openHouse)
                }
            }
        }
        updateOpenHousesUI()
    }

    private fun showFilterDialog() {
        val options = arrayOf("All", "Last 7 Days", "Last 30 Days", "This Year")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Filter by Date")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> filterOpenHouses(searchInput.text.toString()) // All
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
        
        openHouses.clear()
        val currentSearch = searchInput.text.toString().lowercase(Locale.getDefault())
        
        originalOpenHouses.forEach { openHouse ->
            // Check search query first
            val matchesSearch = currentSearch.isEmpty() || 
                openHouse.address.lowercase(Locale.getDefault()).contains(currentSearch) ||
                openHouse.name.lowercase(Locale.getDefault()).contains(currentSearch)
            
            if (matchesSearch) {
                try {
                    val openHouseDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(openHouse.date)
                    if (openHouseDate != null && openHouseDate.after(cutoffDate)) {
                        openHouses.add(openHouse)
                    }
                } catch (e: Exception) {
                    // Try parsing createdAt if date field fails or is different format
                     try {
                        val createdDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(openHouse.createdAt)
                        if (createdDate != null && createdDate.after(cutoffDate)) {
                            openHouses.add(openHouse)
                        }
                     } catch (e2: Exception) {
                        openHouses.add(openHouse) // Include if parsing fails
                     }
                }
            }
        }
        updateOpenHousesUI()
    }
    
    private fun addOpenHouseView(openHouse: OpenHouse) {
        val view = layoutInflater.inflate(R.layout.item_recording, container, false) // Reusing item_recording for now, or create item_open_house
        
        // Customizing the reused layout or creating a new one would be better. 
        // Let's create a simple card view programmatically or reuse item_recording and adapt it.
        // Adapting item_recording:
        val titleText = view.findViewById<TextView>(R.id.recordingTitle)
        val dateText = view.findViewById<TextView>(R.id.recordingDate)
        val durationText = view.findViewById<TextView>(R.id.recordingDuration)
        val playButton = view.findViewById<ImageButton>(R.id.btnPlayRecording)
        
        titleText.text = openHouse.address
        dateText.text = openHouse.date
        durationText.text = openHouse.price
        playButton.visibility = View.GONE // Hide play button
        
        view.setOnClickListener {
            val intent = Intent(this, OpenHouseDetailActivity::class.java)
            intent.putExtra("open_house_id", openHouse.id)
            startActivity(intent)
        }
        
        container.addView(view)
    }
}
