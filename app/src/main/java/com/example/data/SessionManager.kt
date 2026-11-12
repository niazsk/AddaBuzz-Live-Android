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
        const val KEY_USER_DISPLAY_NAME = "user_display_name"
        const val KEY_USER_BIO = "user_bio"
        const val KEY_USER_COINS = "user_coins"
        const val KEY_USER_DIAMONDS = "user_diamonds"
        const val KEY_USER_LEVEL = "user_level"
        const val KEY_USER_FOLLOWERS = "user_followers"
        const val KEY_USER_FOLLOWING = "user_following"
        const val KEY_USER_IS_VIP = "user_is_vip"
        const val KEY_USER_BADGE = "user_badge"
        const val KEY_SERVER_URL = "server_url"
        const val KEY_FIREBASE_API_KEY = "firebase_api_key"
        const val KEY_FIREBASE_PROJECT_ID = "firebase_project_id"
        const val KEY_RESOLUTION = "stream_resolution"
        const val KEY_MIC_MUTED = "stream_mic_muted"
        const val KEY_CAM_ENABLED = "stream_cam_enabled"
        const val KEY_AGENT_PROGRAM = "agent_program_enabled"
        const val KEY_AGENT_AUTO_PAYOUT = "agent_auto_payout"
        const val KEY_ALLOW_NOTIFICATIONS = "allow_notifications"
        const val KEY_PRIVATE_MODE = "private_mode"
        const val KEY_IS_DEMO = "is_demo_session"

        const val DEFAULT_SERVER_URL =
            "https://ais-dev-5rvojij5ycow7bw5erv6dc-218062420511.asia-southeast1.run.app"
        const val DEMO_TOKEN = "token_demo_quick"
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
                username = prefs.getString(KEY_USER_NAME, "") ?: "",
                displayName = prefs.getString(KEY_USER_DISPLAY_NAME, null)
                    ?: prefs.getString(KEY_USER_NAME, "") ?: "",
                email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
                avatarUrl = prefs.getString(KEY_USER_AVATAR, "") ?: "",
                bio = prefs.getString(KEY_USER_BIO, "") ?: "",
                followersCount = prefs.getInt(KEY_USER_FOLLOWERS, 0),
                followingCount = prefs.getInt(KEY_USER_FOLLOWING, 0),
                coins = prefs.getLong(KEY_USER_COINS, 0L),
                diamonds = prefs.getLong(KEY_USER_DIAMONDS, 0L),
                level = prefs.getInt(KEY_USER_LEVEL, 1),
                isVip = prefs.getBoolean(KEY_USER_IS_VIP, false),
                badge = prefs.getString(KEY_USER_BADGE, "") ?: ""
            )
        }
    }

    fun saveUserSession(user: User, token: String) {
        prefs.edit().apply {
            putString(KEY_AUTH_TOKEN, token)
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.username)
            putString(KEY_USER_DISPLAY_NAME, user.displayName)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_AVATAR, user.avatarUrl)
            putString(KEY_USER_BIO, user.bio)
            putLong(KEY_USER_COINS, user.coins)
            putLong(KEY_USER_DIAMONDS, user.diamonds)
            putInt(KEY_USER_LEVEL, user.level)
            putInt(KEY_USER_FOLLOWERS, user.followersCount)
            putInt(KEY_USER_FOLLOWING, user.followingCount)
            putBoolean(KEY_USER_IS_VIP, user.isVip)
            putString(KEY_USER_BADGE, user.badge)
            putBoolean(KEY_IS_DEMO, false)
            apply()
        }
        _currentUser.value = user
    }

    /**
     * Signs in a locally generated demo account. Demo balances are not tied to a
     * real account, so they must not be inherited by a later real sign-in.
     */
    fun saveDemoSession(user: User) {
        prefs.edit().apply {
            putString(KEY_AUTH_TOKEN, DEMO_TOKEN)
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.username)
            putString(KEY_USER_DISPLAY_NAME, user.displayName)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_AVATAR, user.avatarUrl)
            putString(KEY_USER_BIO, user.bio)
            putLong(KEY_USER_COINS, user.coins)
            putLong(KEY_USER_DIAMONDS, user.diamonds)
            putInt(KEY_USER_LEVEL, user.level)
            putInt(KEY_USER_FOLLOWERS, user.followersCount)
            putInt(KEY_USER_FOLLOWING, user.followingCount)
            putBoolean(KEY_USER_IS_VIP, user.isVip)
            putString(KEY_USER_BADGE, user.badge)
            putBoolean(KEY_IS_DEMO, true)
            apply()
        }
        _currentUser.value = user
    }

    fun isDemoSession(): Boolean = prefs.getBoolean(KEY_IS_DEMO, false)

    fun clearSession() {
        prefs.edit().apply {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_USER_DISPLAY_NAME)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_AVATAR)
            remove(KEY_USER_BIO)
            remove(KEY_USER_COINS)
            remove(KEY_USER_DIAMONDS)
            remove(KEY_USER_LEVEL)
            remove(KEY_USER_FOLLOWERS)
            remove(KEY_USER_FOLLOWING)
            remove(KEY_USER_IS_VIP)
            remove(KEY_USER_BADGE)
            remove(KEY_IS_DEMO)
            apply()
        }
        _currentUser.value = null
        runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().signOut() }
    }

    fun updateCoins(newBalance: Long) {
        val updated = _currentUser.value?.copy(coins = newBalance) ?: return
        prefs.edit().putLong(KEY_USER_COINS, newBalance).apply()
        _currentUser.value = updated
    }

    fun updateDiamonds(newBalance: Long) {
        val updated = _currentUser.value?.copy(diamonds = newBalance) ?: return
        prefs.edit().putLong(KEY_USER_DIAMONDS, newBalance).apply()
        _currentUser.value = updated
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

    fun storedCoins(): Long? = if (prefs.contains(KEY_USER_COINS)) prefs.getLong(KEY_USER_COINS, 0L) else null

    fun storedDiamonds(): Long? = if (prefs.contains(KEY_USER_DIAMONDS)) prefs.getLong(KEY_USER_DIAMONDS, 0L) else null

    fun isAgentProgramEnabled(): Boolean = prefs.getBoolean(KEY_AGENT_PROGRAM, true)

    fun setAgentProgramEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AGENT_PROGRAM, enabled).apply()
    }

    fun isAgentAutoPayoutEnabled(): Boolean = prefs.getBoolean(KEY_AGENT_AUTO_PAYOUT, false)

    fun setAgentAutoPayoutEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AGENT_AUTO_PAYOUT, enabled).apply()
    }

    fun isAllowNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_ALLOW_NOTIFICATIONS, true)

    fun setAllowNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALLOW_NOTIFICATIONS, enabled).apply()
    }

    fun isPrivateModeEnabled(): Boolean = prefs.getBoolean(KEY_PRIVATE_MODE, false)

    fun setPrivateModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVATE_MODE, enabled).apply()
    }
}
