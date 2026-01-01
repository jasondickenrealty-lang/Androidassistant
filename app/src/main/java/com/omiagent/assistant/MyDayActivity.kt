package com.omiagent.assistant

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

data class Task(
    val id: String = "",
    val text: String = "",
    val completed: Boolean = false,
    val createdAt: String = ""
)

data class DayEntry(
    val date: String = "",
    val tasks: List<Map<String, Any>> = emptyList(),
    val notes: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

class MyDayActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var tasksContainer: LinearLayout
    private lateinit var newTaskInput: EditText
    private lateinit var notesEditText: EditText
    private lateinit var dateTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    
    private val tasks = mutableListOf<Task>()
    private var currentDate: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_day)
        
        supportActionBar?.hide()
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        if (auth.currentUser == null) {
            finish()
            return
        }
        
        // Initialize views
        tasksContainer = findViewById(R.id.tasksContainer)
        newTaskInput = findViewById(R.id.newTaskInput)
        notesEditText = findViewById(R.id.notesEditText)
        dateTextView = findViewById(R.id.dateTextView)
        progressBar = findViewById(R.id.progressBar)
        emptyStateText = findViewById(R.id.emptyStateText)
        
        // Set current date
        currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        updateDateDisplay()
        
        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
        
        // Add task button
        findViewById<Button>(R.id.btnAddTask).setOnClickListener {
            addTask()
        }
        
        // Enter key on task input
        newTaskInput.setOnEditorActionListener { _, _, _ ->
            addTask()
            true
        }
        
        // Save notes button
        findViewById<Button>(R.id.btnSaveNotes).setOnClickListener {
            saveNotes()
        }
        
        loadDayEntry()
    }
    
    private fun updateDateDisplay() {
        val formatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(currentDate)
        dateTextView.text = formatter.format(date ?: Date())
    }
    
    private fun loadDayEntry() {
        progressBar.visibility = View.VISIBLE
        
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("my-days").document(currentDate)
            .get()
            .addOnSuccessListener { document ->
                progressBar.visibility = View.GONE
                
                if (document.exists()) {
                    val tasksList = document.get("tasks") as? List<Map<String, Any>> ?: emptyList()
                    tasks.clear()
                    tasksList.forEach { taskMap ->
                        tasks.add(Task(
                            id = taskMap["id"] as? String ?: "",
                            text = taskMap["text"] as? String ?: "",
                            completed = taskMap["completed"] as? Boolean ?: false,
                            createdAt = taskMap["createdAt"] as? String ?: ""
                        ))
                    }
                    
                    val notes = document.getString("notes") ?: ""
                    notesEditText.setText(notes)
                    
                    updateTasksUI()
                } else {
                    updateTasksUI()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun addTask() {
        val taskText = newTaskInput.text.toString().trim()
        if (taskText.isEmpty()) return
        
        val newTask = Task(
            id = System.currentTimeMillis().toString(),
            text = taskText,
            completed = false,
            createdAt = Date().toString()
        )
        
        tasks.add(newTask)
        newTaskInput.text.clear()
        updateTasksUI()
        saveDayEntry()
    }
    
    private fun toggleTask(position: Int) {
        if (position >= 0 && position < tasks.size) {
            val task = tasks[position]
            tasks[position] = task.copy(completed = !task.completed)
            updateTasksUI()
            saveDayEntry()
        }
    }
    
    private fun deleteTask(position: Int) {
        if (position >= 0 && position < tasks.size) {
            tasks.removeAt(position)
            updateTasksUI()
            saveDayEntry()
        }
    }
    
    private fun updateTasksUI() {
        tasksContainer.removeAllViews()
        
        if (tasks.isEmpty()) {
            emptyStateText.visibility = View.VISIBLE
        } else {
            emptyStateText.visibility = View.GONE
            
            tasks.forEachIndexed { index, task ->
                val taskView = layoutInflater.inflate(R.layout.item_task, tasksContainer, false)
                
                val checkbox = taskView.findViewById<CheckBox>(R.id.taskCheckbox)
                val taskTextView = taskView.findViewById<TextView>(R.id.taskText)
                val deleteButton = taskView.findViewById<ImageButton>(R.id.btnDeleteTask)
                
                checkbox.isChecked = task.completed
                taskTextView.text = task.text
                
                if (task.completed) {
                    taskTextView.paintFlags = taskTextView.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                    taskTextView.alpha = 0.5f
                } else {
                    taskTextView.paintFlags = taskTextView.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    taskTextView.alpha = 1.0f
                }
                
                checkbox.setOnCheckedChangeListener { _, _ ->
                    toggleTask(index)
                }
                
                deleteButton.setOnClickListener {
                    deleteTask(index)
                }
                
                tasksContainer.addView(taskView)
            }
        }
    }
    
    private fun saveDayEntry() {
        val uid = auth.currentUser?.uid ?: return
        
        val tasksMap = tasks.map { task ->
            mapOf(
                "id" to task.id,
                "text" to task.text,
                "completed" to task.completed,
                "createdAt" to task.createdAt
            )
        }
        
        val data = hashMapOf(
            "date" to currentDate,
            "tasks" to tasksMap,
            "notes" to notesEditText.text.toString(),
            "updatedAt" to Date().toString()
        )
        
        db.collection("users").document(uid)
            .collection("my-days").document(currentDate)
            .set(data, SetOptions.merge())
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun saveNotes() {
        saveDayEntry()
        Toast.makeText(this, "Notes saved!", Toast.LENGTH_SHORT).show()
    }
}
