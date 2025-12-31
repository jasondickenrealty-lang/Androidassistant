package com.edp.android.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_login) // Uncomment and create layout as needed

        val email = "user@email.com" // Replace with actual input
        val pass = "password123"     // Replace with actual input

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseAuth", "Sign-in successful for: $email")
                }
            }
    }
}
