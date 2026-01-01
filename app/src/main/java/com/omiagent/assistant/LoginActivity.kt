package com.omiagent.assistant

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signInButton: Button
    private lateinit var googleSignInButton: Button
    private lateinit var toggleSignUpTextView: TextView
    private lateinit var progressBar: ProgressBar
    private var isSignUpMode = false
    
    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        supportActionBar?.hide()
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        // Check if user is already signed in
        if (auth.currentUser != null) {
            navigateToHome()
            return
        }
        
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        
        // Initialize views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signInButton = findViewById(R.id.signInButton)
        googleSignInButton = findViewById(R.id.googleSignInButton)
        toggleSignUpTextView = findViewById(R.id.toggleSignUpTextView)
        progressBar = findViewById(R.id.progressBar)
        
        signInButton.setOnClickListener { handleEmailPasswordAuth() }
        googleSignInButton.setOnClickListener { signInWithGoogle() }
        toggleSignUpTextView.setOnClickListener { toggleSignUpMode() }
        
        updateUI()
    }
    
    private fun handleEmailPasswordAuth() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }
        
        setLoading(true)
        
        if (isSignUpMode) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        createUserProfile(auth.currentUser?.uid, email)
                    } else {
                        setLoading(false)
                        Toast.makeText(this, "Sign up failed: ${task.exception?.message}", 
                            Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    setLoading(false)
                    if (task.isSuccessful) {
                        navigateToHome()
                    } else {
                        Toast.makeText(this, "Sign in failed: ${task.exception?.message}", 
                            Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
    
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun firebaseAuthWithGoogle(idToken: String) {
        setLoading(true)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Check if user profile exists, if not create it
                    user?.uid?.let { uid ->
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    createUserProfile(uid, user.email)
                                } else {
                                    setLoading(false)
                                    navigateToHome()
                                }
                            }
                            .addOnFailureListener {
                                setLoading(false)
                                navigateToHome()
                            }
                    }
                } else {
                    setLoading(false)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
    
    private fun createUserProfile(uid: String?, email: String?) {
        if (uid == null) {
            setLoading(false)
            navigateToHome()
            return
        }
        
        // Default company for all new users
        val companyId = "euphoric-development"
        
        val userProfile = hashMapOf(
            "email" to (email ?: ""),
            "companyId" to companyId,
            "createdAt" to System.currentTimeMillis(),
            "isActive" to true
        )
        
        db.collection("users").document(uid)
            .set(userProfile)
            .addOnSuccessListener {
                setLoading(false)
                navigateToHome()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Failed to create profile: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
    }
    
    private fun toggleSignUpMode() {
        isSignUpMode = !isSignUpMode
        updateUI()
    }
    
    private fun updateUI() {
        if (isSignUpMode) {
            signInButton.text = "Sign Up"
            toggleSignUpTextView.text = "Already have an account? Sign In"
        } else {
            signInButton.text = "Sign In"
            toggleSignUpTextView.text = "Don't have an account? Sign Up"
        }
    }
    
    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        signInButton.isEnabled = !loading
        googleSignInButton.isEnabled = !loading
        emailEditText.isEnabled = !loading
        passwordEditText.isEnabled = !loading
    }
    
    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
