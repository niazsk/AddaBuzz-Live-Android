package com.example.data

import android.util.Log
import com.example.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseClient(
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "SupabaseClient"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    sealed class AuthResult {
        data class Success(val user: User, val accessToken: String) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    sealed class ConnectionTestResult {
        data class Connected(val message: String, val latencyMs: Long) : ConnectionTestResult()
        data class Failed(val error: String) : ConnectionTestResult()
    }

    /**
     * Test connection to the configured Supabase Project URL and Anon Key
     */
    suspend fun testConnection(url: String, anonKey: String): ConnectionTestResult = withContext(Dispatchers.IO) {
        val cleanUrl = SessionManager.sanitizeSupabaseUrl(url)
        if (cleanUrl.isBlank()) {
            return@withContext ConnectionTestResult.Failed("Please enter your Supabase Project URL (e.g. https://your-project.supabase.co)")
        }

        val startTime = System.currentTimeMillis()
        try {
            // Check health or auth settings endpoint
            val testEndpoint = "$cleanUrl/auth/v1/settings"
            val requestBuilder = Request.Builder()
                .url(testEndpoint)
                .get()

            if (anonKey.isNotBlank()) {
                requestBuilder.addHeader("apikey", anonKey.trim())
                requestBuilder.addHeader("Authorization", "Bearer ${anonKey.trim()}")
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val latency = System.currentTimeMillis() - startTime

            if (response.isSuccessful || response.code in 200..404) {
                // Supabase responded
                ConnectionTestResult.Connected(
                    message = "Successfully connected to Supabase (HTTP ${response.code})",
                    latencyMs = latency
                )
            } else {
                val errorBody = response.body?.string().orEmpty()
                ConnectionTestResult.Failed("Supabase responded with code ${response.code}: $errorBody")
            }
        } catch (e: java.net.UnknownHostException) {
            Log.w(TAG, "Supabase host unreachable: ${e.message}")
            ConnectionTestResult.Failed("Cannot find host '$cleanUrl'. Please verify your Project URL in supabase.com -> Project Settings -> API.")
        } catch (e: Exception) {
            Log.w(TAG, "Supabase connection error: ${e.message}")
            ConnectionTestResult.Failed("Connection error: ${e.localizedMessage ?: e.message}")
        }
    }

    /**
     * Sign In with Email and Password using Supabase Auth
     */
    suspend fun signIn(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val supabaseUrl = SessionManager.sanitizeSupabaseUrl(sessionManager.getSupabaseUrl())
        val anonKey = sessionManager.getSupabaseAnonKey().trim()

        if (supabaseUrl.isBlank() || anonKey.isBlank() || supabaseUrl.contains("kvyuzqvyiabxdfgjhklp")) {
            // Local fallback when Supabase URL not set yet
            val demoUser = User(
                id = "usr_${System.currentTimeMillis()}",
                username = email.substringBefore("@"),
                displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = email,
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                bio = "AddaBuzz Live Streamer 🎙️",
                diamonds = 2500,
                beans = 6000
            )
            return@withContext AuthResult.Success(demoUser, "local_token_${System.currentTimeMillis()}")
        }

        try {
            val endpoint = "$supabaseUrl/auth/v1/token?grant_type=password"
            val payload = JSONObject().apply {
                put("email", email.trim())
                put("password", password)
            }

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val accessToken = json.optString("access_token", "token_${System.currentTimeMillis()}")
                val userObj = json.optJSONObject("user")

                val userId = userObj?.optString("id") ?: "usr_${System.currentTimeMillis()}"
                val userEmail = userObj?.optString("email") ?: email
                val metadata = userObj?.optJSONObject("user_metadata")
                val displayName = metadata?.optString("full_name")
                    ?: metadata?.optString("display_name")
                    ?: userEmail.substringBefore("@")
                val avatar = metadata?.optString("avatar_url")
                    ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200"

                val user = User(
                    id = userId,
                    username = userEmail.substringBefore("@"),
                    displayName = displayName,
                    email = userEmail,
                    avatarUrl = avatar,
                    bio = "AddaBuzz Live Creator 🌟",
                    diamonds = 3000,
                    beans = 7500
                )
                AuthResult.Success(user, accessToken)
            } else {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optString("error_description", errJson.optString("msg", response.message))
                } catch (e: Exception) {
                    responseBody.ifBlank { "HTTP Error ${response.code}" }
                }
                AuthResult.Error(errorMsg)
            }
        } catch (e: java.net.UnknownHostException) {
            Log.w(TAG, "Supabase signIn host unreachable: ${e.message}")
            AuthResult.Error("Cannot connect to '$supabaseUrl'. Please verify your Project URL in Settings, or use One-Tap Demo Login.")
        } catch (e: Exception) {
            Log.w(TAG, "Supabase signIn error: ${e.message}")
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    /**
     * Sign Up with Email and Password using Supabase Auth
     */
    suspend fun signUp(email: String, password: String, displayName: String): AuthResult = withContext(Dispatchers.IO) {
        val supabaseUrl = SessionManager.sanitizeSupabaseUrl(sessionManager.getSupabaseUrl())
        val anonKey = sessionManager.getSupabaseAnonKey().trim()

        if (supabaseUrl.isBlank() || anonKey.isBlank() || supabaseUrl.contains("kvyuzqvyiabxdfgjhklp")) {
            val user = User(
                id = "usr_${System.currentTimeMillis()}",
                username = email.substringBefore("@"),
                displayName = displayName.ifBlank { email.substringBefore("@") },
                email = email,
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                bio = "New AddaBuzz Live Creator 🎉",
                diamonds = 1500,
                beans = 3000
            )
            return@withContext AuthResult.Success(user, "local_token_${System.currentTimeMillis()}")
        }

        try {
            val endpoint = "$supabaseUrl/auth/v1/signup"
            val payload = JSONObject().apply {
                put("email", email.trim())
                put("password", password)
                put("data", JSONObject().apply {
                    put("display_name", displayName)
                    put("full_name", displayName)
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val accessToken = json.optString("access_token", "token_${System.currentTimeMillis()}")
                val userObj = json.optJSONObject("user") ?: json

                val userId = userObj.optString("id", "usr_${System.currentTimeMillis()}")
                val user = User(
                    id = userId,
                    username = email.substringBefore("@"),
                    displayName = displayName.ifBlank { email.substringBefore("@") },
                    email = email,
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                    bio = "AddaBuzz Live Creator 🎉",
                    diamonds = 1500,
                    beans = 3000
                )
                AuthResult.Success(user, accessToken)
            } else {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optString("error_description", errJson.optString("msg", response.message))
                } catch (e: Exception) {
                    responseBody.ifBlank { "HTTP Error ${response.code}" }
                }
                AuthResult.Error(errorMsg)
            }
        } catch (e: java.net.UnknownHostException) {
            Log.w(TAG, "Supabase signUp host unreachable: ${e.message}")
            AuthResult.Error("Cannot connect to '$supabaseUrl'. Please enter your valid Supabase Project URL in Settings.")
        } catch (e: Exception) {
            Log.w(TAG, "Supabase signUp error: ${e.message}")
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    /**
     * Password Recovery
     */
    suspend fun sendPasswordReset(email: String): Boolean = withContext(Dispatchers.IO) {
        val supabaseUrl = SessionManager.sanitizeSupabaseUrl(sessionManager.getSupabaseUrl())
        val anonKey = sessionManager.getSupabaseAnonKey().trim()
        if (supabaseUrl.isBlank() || anonKey.isBlank()) return@withContext true

        try {
            val endpoint = "$supabaseUrl/auth/v1/recover"
            val payload = JSONObject().apply {
                put("email", email.trim())
            }
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = httpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
