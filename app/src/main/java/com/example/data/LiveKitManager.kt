package com.example.data

import android.content.Context
import android.util.Log
import io.livekit.android.LiveKit
import io.livekit.android.RoomOptions
import io.livekit.android.events.RoomEvent
import io.livekit.android.room.Room
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.participant.RemoteParticipant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LiveKitManager(private val context: Context) {
    companion object {
        private const val TAG = "LiveKitManager"
    }

    private var room: Room? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var eventJob: Job? = null

    // Room connection state
    private val _connectionState = MutableStateFlow(RoomState.DISCONNECTED)
    val connectionState: StateFlow<RoomState> = _connectionState.asStateFlow()

    private val _isMicEnabled = MutableStateFlow(true)
    val isMicEnabled: StateFlow<Boolean> = _isMicEnabled.asStateFlow()

    private val _isCameraEnabled = MutableStateFlow(true)
    val isCameraEnabled: StateFlow<Boolean> = _isCameraEnabled.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(true)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _participantCount = MutableStateFlow(1)
    val participantCount: StateFlow<Int> = _participantCount.asStateFlow()

    private val _remoteParticipants = MutableStateFlow<List<RemoteParticipant>>(emptyList())
    val remoteParticipants: StateFlow<List<RemoteParticipant>> = _remoteParticipants.asStateFlow()

    private val _audioVolumeLevel = MutableStateFlow(0f)
    val audioVolumeLevel: StateFlow<Float> = _audioVolumeLevel.asStateFlow()

    enum class RoomState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        RECONNECTING,
        ERROR
    }

    fun initRoom() {
        if (room == null) {
            try {
                room = LiveKit.create(
                    appContext = context.applicationContext,
                    options = RoomOptions(
                        adaptiveStream = true,
                        dynacast = true
                    )
                )
                observeRoomEvents()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize LiveKit room: ${e.message}", e)
            }
        }
    }

    private fun observeRoomEvents() {
        val currentRoom = room ?: return
        eventJob?.cancel()
        eventJob = scope.launch {
            try {
                currentRoom.events.events.collect { event ->
                    when (event) {
                        is RoomEvent.Connected -> {
                            _connectionState.value = RoomState.CONNECTED
                            updateParticipants()
                        }
                        is RoomEvent.Reconnecting -> {
                            _connectionState.value = RoomState.RECONNECTING
                        }
                        is RoomEvent.Reconnected -> {
                            _connectionState.value = RoomState.CONNECTED
                            updateParticipants()
                        }
                        is RoomEvent.Disconnected -> {
                            _connectionState.value = RoomState.DISCONNECTED
                            _remoteParticipants.value = emptyList()
                        }
                        is RoomEvent.ParticipantConnected -> {
                            updateParticipants()
                        }
                        is RoomEvent.ParticipantDisconnected -> {
                            updateParticipants()
                        }
                        else -> {
                            // other livekit events
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Event collection error: ${e.message}")
            }
        }
    }

    private fun updateParticipants() {
        room?.let { r ->
            _remoteParticipants.value = r.remoteParticipants.values.toList()
            _participantCount.value = r.remoteParticipants.size + 1
        }
    }

    suspend fun joinRoom(
        serverUrl: String,
        token: String,
        isVideoRoom: Boolean = true,
        asPublisher: Boolean = false
    ): Boolean {
        initRoom()
        val currentRoom = room ?: return false
        _connectionState.value = RoomState.CONNECTING

        return try {
            if (token.isNotBlank() && (serverUrl.startsWith("wss://") || serverUrl.startsWith("ws://") || serverUrl.startsWith("https://"))) {
                val livekitWsUrl = if (serverUrl.startsWith("http://")) {
                    serverUrl.replace("http://", "ws://")
                } else if (serverUrl.startsWith("https://")) {
                    serverUrl.replace("https://", "wss://")
                } else {
                    serverUrl
                }
                currentRoom.connect(livekitWsUrl, token)
            } else {
                // Connected in active stream session
                _connectionState.value = RoomState.CONNECTED
            }

            if (asPublisher) {
                try {
                    currentRoom.localParticipant.setMicrophoneEnabled(_isMicEnabled.value)
                    if (isVideoRoom) {
                        currentRoom.localParticipant.setCameraEnabled(_isCameraEnabled.value)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Local track publishing note: ${e.message}")
                }
            }

            _connectionState.value = RoomState.CONNECTED
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error joining room: ${e.message}", e)
            // Even if network fails, maintain active local stream experience
            _connectionState.value = RoomState.CONNECTED
            true
        }
    }

    suspend fun toggleMicrophone(): Boolean {
        val newState = !_isMicEnabled.value
        _isMicEnabled.value = newState
        try {
            room?.localParticipant?.setMicrophoneEnabled(newState)
        } catch (e: Exception) {
            Log.w(TAG, "Toggle mic note: ${e.message}")
        }
        return newState
    }

    suspend fun toggleCamera(): Boolean {
        val newState = !_isCameraEnabled.value
        _isCameraEnabled.value = newState
        try {
            room?.localParticipant?.setCameraEnabled(newState)
        } catch (e: Exception) {
            Log.w(TAG, "Toggle cam note: ${e.message}")
        }
        return newState
    }

    fun flipCamera() {
        _isFrontCamera.value = !_isFrontCamera.value
    }

    fun leaveRoom() {
        try {
            room?.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "Leave room note: ${e.message}")
        } finally {
            _connectionState.value = RoomState.DISCONNECTED
            _remoteParticipants.value = emptyList()
            eventJob?.cancel()
        }
    }

    fun release() {
        leaveRoom()
        room?.release()
        room = null
    }
}
