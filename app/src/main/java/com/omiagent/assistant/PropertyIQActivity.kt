package com.omiagent.assistant

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PropertyIQActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_iq)

        supportActionBar?.hide()

        // Back Button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Export Button
        findViewById<MaterialButton>(R.id.btnExport).setOnClickListener {
            showExportOptions()
        }
        
        // Search
        findViewById<EditText>(R.id.searchPropertyIQ).setOnEditorActionListener { v, actionId, event ->
            Toast.makeText(this, "Analyzing property: ${v.text}", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun showExportOptions() {
        val options = arrayOf("PDF", "CSV", "Email")
        AlertDialog.Builder(this)
            .setTitle("Export Property Report")
            .setItems(options) { _, which ->
                val selected = options[which]
                Toast.makeText(this, "Exporting to $selected...", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
