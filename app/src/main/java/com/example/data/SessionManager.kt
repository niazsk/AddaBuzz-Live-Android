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
        const val KEY_SUPABASE_URL = "supabase_url"
        const val KEY_SUPABASE_ANON_KEY = "supabase_anon_key"
        const val KEY_RESOLUTION = "stream_resolution"
        const val KEY_MIC_MUTED = "stream_mic_muted"
        const val KEY_CAM_ENABLED = "stream_cam_enabled"

        const val DEFAULT_SERVER_URL =
            "https://ais-dev-5rvojij5ycow7bw5erv6dc-218062420511.asia-southeast1.run.app"
        const val DEFAULT_SUPABASE_URL = "https://citiaagytdkomcmkuhkk.supabase.co"
        const val DEFAULT_SUPABASE_ANON_KEY = "sb_publishable_84NpiaDV0dfaFOWFbTFdwA_AKFby-Lo"

        fun sanitizeSupabaseUrl(raw: String): String {
            var url = raw.trim()
            if (url.isBlank()) return ""
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }
            url = url.trimEnd('/')
            if (url.endsWith("/rest/v1")) {
                url = url.removeSuffix("/rest/v1")
            } else if (url.endsWith("/auth/v1")) {
                url = url.removeSuffix("/auth/v1")
            }
            return url.trimEnd('/')
        }
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _serverUrl = MutableStateFlow(getServerUrl())
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    init {
        // Clear old dummy placeholder URL if previously stored
        val cachedUrl = prefs.getString(KEY_SUPABASE_URL, "")
        if (cachedUrl?.contains("kvyuzqvyiabxdfgjhklp") == true || cachedUrl.isNullOrBlank()) {
            prefs.edit().putString(KEY_SUPABASE_URL, DEFAULT_SUPABASE_URL).apply()
        }

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
                diamonds = 2500,
                beans = 7200,
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
                diamonds = 3500,
                beans = 8900,
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

    fun updateDiamonds(newBalance: Long) {
        _currentUser.value = _currentUser.value?.copy(diamonds = newBalance)
    }

    fun updateBeans(newBalance: Long) {
        _currentUser.value = _currentUser.value?.copy(beans = newBalance)
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

    fun getSupabaseUrl(): String {
        val url = prefs.getString(KEY_SUPABASE_URL, DEFAULT_SUPABASE_URL)
        return if (url.isNullOrBlank() || url.contains("kvyuzqvyiabxdfgjhklp")) DEFAULT_SUPABASE_URL else sanitizeSupabaseUrl(url)
    }

    fun setSupabaseUrl(url: String) {
        val clean = sanitizeSupabaseUrl(url)
        prefs.edit().putString(KEY_SUPABASE_URL, clean).apply()
    }

    fun getSupabaseAnonKey(): String {
        val key = prefs.getString(KEY_SUPABASE_ANON_KEY, DEFAULT_SUPABASE_ANON_KEY)
        return if (key.isNullOrBlank()) DEFAULT_SUPABASE_ANON_KEY else key
    }

    fun setSupabaseAnonKey(key: String) {
        prefs.edit().putString(KEY_SUPABASE_ANON_KEY, key).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }
}
