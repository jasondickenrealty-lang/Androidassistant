package com.omiagent.assistant

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class InspectionPunchListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspection_punch_list)

        supportActionBar?.hide()

        // Back Button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Export Button
        findViewById<MaterialButton>(R.id.btnExport).setOnClickListener {
            showExportOptions()
        }

        // Add Item FAB
        findViewById<FloatingActionButton>(R.id.fabAddPunchItem).setOnClickListener {
            Toast.makeText(this, "Add Item - Coming Soon", Toast.LENGTH_SHORT).show()
        }
        
        // Search
        findViewById<EditText>(R.id.searchPunchList).setOnEditorActionListener { v, actionId, event ->
            Toast.makeText(this, "Searching: ${v.text}", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun showExportOptions() {
        val options = arrayOf("PDF", "CSV", "Email")
        AlertDialog.Builder(this)
            .setTitle("Export Punch List")
            .setItems(options) { _, which ->
                val selected = options[which]
                Toast.makeText(this, "Exporting to $selected...", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
