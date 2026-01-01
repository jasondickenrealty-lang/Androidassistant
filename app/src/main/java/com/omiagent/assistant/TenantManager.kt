package com.omiagent.assistant

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Helper class for multi-tenant operations
 * Ensures all data is scoped to the user's company
 */
object TenantManager {
    private const val DEFAULT_COMPANY_ID = "euphoric-development"
    
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    /**
     * Get the current user's company ID
     */
    suspend fun getCurrentUserCompanyId(): String {
        val uid = auth.currentUser?.uid ?: return DEFAULT_COMPANY_ID
        
        return try {
            val userDoc = db.collection("users").document(uid).get().await()
            userDoc.getString("companyId") ?: DEFAULT_COMPANY_ID
        } catch (e: Exception) {
            DEFAULT_COMPANY_ID
        }
    }
    
    /**
     * Get company ID synchronously (with callback)
     */
    fun getCurrentUserCompanyId(callback: (String) -> Unit) {
        val uid = auth.currentUser?.uid
        
        if (uid == null) {
            callback(DEFAULT_COMPANY_ID)
            return
        }
        
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val companyId = document.getString("companyId") ?: DEFAULT_COMPANY_ID
                callback(companyId)
            }
            .addOnFailureListener {
                callback(DEFAULT_COMPANY_ID)
            }
    }
    
    /**
     * Add company ID to data map
     */
    fun addCompanyId(data: HashMap<String, Any>, companyId: String): HashMap<String, Any> {
        data["companyId"] = companyId
        return data
    }
    
    /**
     * Create a query filter for company-scoped data
     */
    fun getCompanyQuery(collection: String, companyId: String) =
        db.collection(collection).whereEqualTo("companyId", companyId)
}
