package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("addabuzz_session", Context.MODE_PRIVATE)

    companion object {
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_USER_AVATAR = "user_avatar"
        const val KEY_SERVER_URL = "server_url"
        const val KEY_FIREBASE_API_KEY = "firebase_api_key"
        const val KEY_FIREBASE_PROJECT_ID = "firebase_project_id"
        const val KEY_RESOLUTION = "stream_resolution"
        const val KEY_MIC_MUTED = "stream_mic_muted"
        const val KEY_CAM_ENABLED = "stream_cam_enabled"
        const val KEY_AGENT_PROGRAM = "agent_program_enabled"
        const val KEY_AGENT_AUTO_PAYOUT = "agent_auto_payout"

        const val DEFAULT_SERVER_URL =
            "https://ais-dev-5rvojij5ycow7bw5erv6dc-218062420511.asia-southeast1.run.app"
        const val DEFAULT_FIREBASE_API_KEY = ""
        const val DEFAULT_FIREBASE_PROJECT_ID = "addabuzzlive"
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _serverUrl = MutableStateFlow(getServerUrl())
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    init {
        val userId = prefs.getString(KEY_USER_ID, null)
        if (userId != null) {
            _currentUser.value = User(
                id = userId,
                username = prefs.getString(KEY_USER_NAME, "fayselsk") ?: "fayselsk",
                displayName = "Faysel SK",
                email = prefs.getString(KEY_USER_EMAIL, "fayselsk@gmail.com") ?: "fayselsk@gmail.com",
                avatarUrl = prefs.getString(KEY_USER_AVATAR, "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200") ?: "",
                bio = "AddaBuzz Official Streamer 🎙️ | Chit-Chat & Music lover",
                followersCount = 1420,
                followingCount = 380,
                coins = 2500,
                diamonds = 7200,
                level = 24,
                isVip = true,
                badge = "VIP Host"
            )
        } else {
            // Default active demo user session for immediate live streaming test
            val demoUser = User(
                id = "usr_fayselsk_01",
                username = "fayselsk",
                displayName = "Faysel SK",
                email = "fayselsk@gmail.com",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                bio = "Connecting & Streaming on AddaBuzz Live 🚀",
                followersCount = 2150,
                followingCount = 412,
                coins = 3500,
                diamonds = 8900,
                level = 28,
                isVip = true,
                badge = "Elite Host"
            )
            saveUserSession(demoUser, "token_demo_authenticated_fayselsk")
        }
    }

    fun saveUserSession(user: User, token: String) {
        prefs.edit().apply {
            putString(KEY_AUTH_TOKEN, token)
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.username)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_AVATAR, user.avatarUrl)
            apply()
        }
        _currentUser.value = user
    }

    fun clearSession() {
        prefs.edit().apply {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_AVATAR)
            apply()
        }
        _currentUser.value = null
    }

    fun updateCoins(newBalance: Long) {
        _currentUser.value = _currentUser.value?.copy(coins = newBalance)
    }

    fun updateDiamonds(newBalance: Long) {
        _currentUser.value = _currentUser.value?.copy(diamonds = newBalance)
    }

    fun getServerUrl(): String {
        return prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    }

    fun setServerUrl(url: String) {
        prefs.edit().putString(KEY_SERVER_URL, url).apply()
        _serverUrl.value = url
    }

    fun getStreamResolution(): String {
        return prefs.getString(KEY_RESOLUTION, "720p (HD 30fps)") ?: "720p (HD 30fps)"
    }

    fun setStreamResolution(res: String) {
        prefs.edit().putString(KEY_RESOLUTION, res).apply()
    }

    fun getFirebaseApiKey(): String {
        val key = prefs.getString(KEY_FIREBASE_API_KEY, DEFAULT_FIREBASE_API_KEY)
        return if (key.isNullOrBlank()) DEFAULT_FIREBASE_API_KEY else key
    }

    fun setFirebaseApiKey(key: String) {
        prefs.edit().putString(KEY_FIREBASE_API_KEY, key.trim()).apply()
    }

    fun getFirebaseProjectId(): String {
        val projectId = prefs.getString(KEY_FIREBASE_PROJECT_ID, DEFAULT_FIREBASE_PROJECT_ID)
        return if (projectId.isNullOrBlank()) DEFAULT_FIREBASE_PROJECT_ID else projectId.trim()
    }

    fun setFirebaseProjectId(projectId: String) {
        prefs.edit().putString(KEY_FIREBASE_PROJECT_ID, projectId.trim()).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun isAgentProgramEnabled(): Boolean = prefs.getBoolean(KEY_AGENT_PROGRAM, true)

    fun setAgentProgramEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AGENT_PROGRAM, enabled).apply()
    }

    fun isAgentAutoPayoutEnabled(): Boolean = prefs.getBoolean(KEY_AGENT_AUTO_PAYOUT, false)

    fun setAgentAutoPayoutEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AGENT_AUTO_PAYOUT, enabled).apply()
    }
}
