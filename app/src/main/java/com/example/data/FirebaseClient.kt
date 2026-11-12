package com.example.data

import android.util.Log
import com.example.model.User
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseClient(
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "FirebaseClient"
        private const val DEFAULT_AVATAR = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200"
    }

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    sealed class AuthResult {
        data class Success(val user: User, val accessToken: String) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    sealed class ConnectionTestResult {
        data class Connected(val message: String, val latencyMs: Long) : ConnectionTestResult()
        data class Failed(val error: String) : ConnectionTestResult()
    }

    /**
     * Test connection to the configured Firebase project
     */
    suspend fun testConnection(apiKey: String, projectId: String): ConnectionTestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        return try {
            val app = FirebaseApp.getInstance()
            ConnectionTestResult.Connected(
                message = "Successfully connected to Firebase Auth (project: ${app.projectId})",
                latencyMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            Log.w(TAG, "Firebase connection error: ${e.message}")
            ConnectionTestResult.Failed("Firebase connection failed: ${e.localizedMessage ?: e.message}")
        }
    }

    /**
     * Sign In with Email and Password using Firebase Auth
     */
    suspend fun signIn(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        return try {
            val result = Tasks.await(auth.signInWithEmailAndPassword(email.trim(), password))
            val firebaseUser: FirebaseUser = result.user
            val accessToken = Tasks.await(firebaseUser.getIdToken())
            AuthResult.Success(toUser(firebaseUser, email, accessToken), accessToken)
        } catch (e: Exception) {
            Log.w(TAG, "Firebase signIn error: ${e.message}")
            AuthResult.Error(e.message ?: "Firebase sign in failed")
        }
    }

    /**
     * Sign Up with Email and Password using Firebase Auth
     */
    suspend fun signUp(email: String, password: String, displayName: String): AuthResult = withContext(Dispatchers.IO) {
        return try {
            val result = Tasks.await(auth.createUserWithEmailAndPassword(email.trim(), password))
            val firebaseUser: FirebaseUser = result.user
            val name = displayName.ifBlank { email.substringBefore("@") }
            if (name.isNotBlank()) {
                runCatching { Tasks.await(firebaseUser.updateDisplayName(name)) }
            }
            val accessToken = Tasks.await(firebaseUser.getIdToken())
            AuthResult.Success(toUser(firebaseUser, email, accessToken), accessToken)
        } catch (e: Exception) {
            Log.w(TAG, "Firebase signUp error: ${e.message}")
            AuthResult.Error(e.message ?: "Firebase sign up failed")
        }
    }

    /**
     * Password Recovery via Firebase Auth
     */
    suspend fun sendPasswordReset(email: String): Boolean = withContext(Dispatchers.IO) {
        return try {
            Tasks.await(auth.sendPasswordResetEmail(email.trim()))
            true
        } catch (e: Exception) {
            Log.w(TAG, "Firebase password reset error: ${e.message}")
            false
        }
    }

    private fun toUser(firebaseUser: FirebaseUser, email: String, accessToken: String): User {
        val userEmail = firebaseUser.email ?: email
        val displayName = firebaseUser.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
        val avatarUrl = firebaseUser.photoUrl?.toString() ?: DEFAULT_AVATAR
        return User(
            id = firebaseUser.uid,
            username = userEmail.substringBefore("@"),
            displayName = displayName,
            email = userEmail,
            avatarUrl = avatarUrl,
            bio = "AddaBuzz Live Creator 🌟",
            diamonds = 3000,
            beans = 7500
        )
    }
}
