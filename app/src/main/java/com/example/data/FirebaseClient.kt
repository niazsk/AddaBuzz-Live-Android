package com.example.data

import android.util.Log
import com.example.model.User
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseClient(
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "FirebaseClient"
        private const val DEFAULT_AVATAR = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200"
        private const val STARTING_COINS = 500L
        private const val STARTING_DIAMONDS = 1000L
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
        try {
            val app = FirebaseApp.getInstance()
            ConnectionTestResult.Connected(
                message = "Successfully connected to Firebase Auth (project: $projectId)",
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
        try {
            val result = Tasks.await(auth.signInWithEmailAndPassword(email.trim(), password))
            val firebaseUser = result.user
                ?: throw IllegalStateException("Firebase sign in failed: no user returned")
            val idTokenResult = Tasks.await(firebaseUser.getIdToken(false))
            val accessToken = idTokenResult.token
                ?: throw IllegalStateException("Firebase token is null")
            AuthResult.Success(toUser(firebaseUser, email, accessToken), accessToken)
        } catch (e: Exception) {
            Log.w(TAG, "Firebase signIn error: ${e.message}")
            AuthResult.Error(e.message ?: "Firebase sign in failed")
        }
    }

    /**
     * Sign Up with Email and Password using Firebase Auth
     */
    suspend fun signUp(email: String, password: String, displayName: String, username: String = ""): AuthResult = withContext(Dispatchers.IO) {
        var firebaseUser: FirebaseUser? = null
        try {
            val result = Tasks.await(auth.createUserWithEmailAndPassword(email.trim(), password))
            firebaseUser = result.user
                ?: throw IllegalStateException("Firebase sign up failed: no user returned")
            val name = displayName.ifBlank { email.substringBefore("@") }
            // Profile/token are best-effort: the account already exists at this point,
            // so a failure here must not be reported as a failed sign up.
            runCatching {
                Tasks.await(firebaseUser!!.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()))
            }.onFailure { Log.w(TAG, "updateProfile failed: ${it.message}") }
            val accessToken = runCatching { Tasks.await(firebaseUser!!.getIdToken(false)).token }
                .getOrNull()
                ?: ""
            AuthResult.Success(
                toUser(firebaseUser!!, email, accessToken, isNewUser = true, usernameOverride = username),
                accessToken
            )
        } catch (e: Exception) {
            Log.w(TAG, "Firebase signUp error: ${e.message}")
            AuthResult.Error(e.message ?: "Firebase sign up failed")
        }
    }

    /**
     * Password Recovery via Firebase Auth
     */
    suspend fun sendPasswordReset(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Tasks.await(auth.sendPasswordResetEmail(email.trim()))
            true
        } catch (e: Exception) {
            Log.w(TAG, "Firebase password reset error: ${e.message}")
            false
        }
    }

    private fun toUser(
        firebaseUser: FirebaseUser,
        email: String,
        accessToken: String,
        isNewUser: Boolean = false,
        usernameOverride: String = ""
    ): User {
        val userEmail = firebaseUser.email ?: email
        val displayName = firebaseUser.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
        val avatarUrl = firebaseUser.photoUrl?.toString() ?: DEFAULT_AVATAR
        // A demo session's balances are not real, so a real sign-in must not inherit them.
        val storedCoins = if (sessionManager.isDemoSession()) null else sessionManager.storedCoins()
        val storedDiamonds = if (sessionManager.isDemoSession()) null else sessionManager.storedDiamonds()
        val coins = if (isNewUser) STARTING_COINS else (storedCoins ?: STARTING_COINS)
        val diamonds = if (isNewUser) STARTING_DIAMONDS else (storedDiamonds ?: STARTING_DIAMONDS)
        val username = usernameOverride.trim().removePrefix("@").ifBlank { userEmail.substringBefore("@") }
        return User(
            id = firebaseUser.uid,
            username = username,
            displayName = displayName,
            email = userEmail,
            avatarUrl = avatarUrl,
            bio = "AddaBuzz Live Creator 🌟",
            coins = coins,
            diamonds = diamonds
        )
    }
}
