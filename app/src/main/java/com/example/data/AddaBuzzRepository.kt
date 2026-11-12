package com.example.data

import android.content.Context
import com.example.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddaBuzzRepository(
    private val context: Context,
    val sessionManager: SessionManager,
    val liveKitManager: LiveKitManager
) {
    val firebaseClient = FirebaseClient(sessionManager)
    private val firestore: FirebaseFirestore? by lazy {
        runCatching {
            if (FirebaseApp.getApps(context).isEmpty()) null else FirebaseFirestore.getInstance()
        }.getOrNull()
    }
    private val scope = CoroutineScope(Dispatchers.Default)
    private var liveMessagesListener: ListenerRegistration? = null

    // Current Active Live Room
    private val _currentRoom = MutableStateFlow<LiveRoom?>(null)
    val currentRoom: StateFlow<LiveRoom?> = _currentRoom.asStateFlow()

    // 8 Multi-Guest Seats
    private val _guestSeats = MutableStateFlow<List<GuestSeat>>(
        List(8) { index -> GuestSeat(index = index) }
    )
    val guestSeats: StateFlow<List<GuestSeat>> = _guestSeats.asStateFlow()

    // Guest Join Requests (for Host to accept/reject)
    private val _pendingGuestRequests = MutableStateFlow<List<User>>(emptyList())
    val pendingGuestRequests: StateFlow<List<User>> = _pendingGuestRequests.asStateFlow()

    // Live Room Chat Messages
    private val _liveMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val liveMessages: StateFlow<List<ChatMessage>> = _liveMessages.asStateFlow()

    // Animated Gift Trigger Event (for full-screen celebrations)
    private val _giftCelebrationEvent = MutableSharedFlow<ChatMessage>(extraBufferCapacity = 5)
    val giftCelebrationEvent: SharedFlow<ChatMessage> = _giftCelebrationEvent.asSharedFlow()

    // Reactions counter
    private val _reactionCount = MutableStateFlow(3420L)
    val reactionCount: StateFlow<Long> = _reactionCount.asStateFlow()

    // All Live Rooms
    private val _allRooms = MutableStateFlow<List<LiveRoom>>(emptyList())
    val allRooms: StateFlow<List<LiveRoom>> = _allRooms.asStateFlow()

    // Following creators
    private val _followingUsers = MutableStateFlow<List<User>>(emptyList())
    val followingUsers: StateFlow<List<User>> = _followingUsers.asStateFlow()

    // Followers
    private val _followersUsers = MutableStateFlow<List<User>>(emptyList())
    val followersUsers: StateFlow<List<User>> = _followersUsers.asStateFlow()

    // Conversations / Direct Messages
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _directMessages = MutableStateFlow<Map<String, List<PrivateMessage>>>(emptyMap())
    val directMessages: StateFlow<Map<String, List<PrivateMessage>>> = _directMessages.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Wallet Transactions
    private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    // Claimed daily reward task ids
    private val _claimedRewards = MutableStateFlow<Set<String>>(emptySet())
    val claimedRewards: StateFlow<Set<String>> = _claimedRewards.asStateFlow()

    // Creator fan clubs the user has joined
    private val _fanClubMemberships = MutableStateFlow<Set<String>>(emptySet())
    val fanClubMemberships: StateFlow<Set<String>> = _fanClubMemberships.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        // Sample Creators
        val creator1 = User(
            id = "cr_1",
            username = "anika_stream",
            displayName = "Anika Rahman 🎶",
            email = "anika@addabuzz.live",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
            bio = "Live Acoustic Songs & Chill Adda | Daily 9 PM 🎸",
            followersCount = 14200,
            followingCount = 120,
            coins = 45000,
            diamonds = 98000,
            level = 42,
            isVip = true,
            isLive = true,
            badge = "Superstar"
        )
        val creator2 = User(
            id = "cr_2",
            username = "tanvir_gamer",
            displayName = "Tanvir Ahmed 🎮",
            email = "tanvir@addabuzz.live",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
            bio = "Mobile Legends & BGMI Pro streamer | Late night tournaments 🏆",
            followersCount = 28900,
            followingCount = 310,
            coins = 82000,
            diamonds = 154000,
            level = 56,
            isVip = true,
            isLive = true,
            badge = "Pro Gamer"
        )
        val creator3 = User(
            id = "cr_3",
            username = "roxy_adda",
            displayName = "Roxy & Friends 🎙️",
            email = "roxy@addabuzz.live",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
            bio = "8-Guest Audio Adda! Come join the mic & share funny stories!",
            followersCount = 9850,
            followingCount = 85,
            coins = 18000,
            diamonds = 45000,
            level = 31,
            isVip = true,
            isLive = true,
            badge = "Adda Queen"
        )
        val creator4 = User(
            id = "cr_4",
            username = "shahadat_voice",
            displayName = "Shahadat Hossain",
            email = "shahadat@addabuzz.live",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
            bio = "Tech talk, Flutter vs Native Android, AI coding session 💻",
            followersCount = 5600,
            followingCount = 90,
            coins = 12000,
            diamonds = 23000,
            level = 22,
            isVip = false,
            isLive = true,
            badge = "Tech Guru"
        )

        // Seed Live Rooms
        val rooms = listOf(
            LiveRoom(
                id = "room_01",
                host = creator1,
                title = "Late Night Acoustic Melodies & Adda 🎸✨",
                category = "Music & Adda",
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
                viewerCount = 1420,
                roomType = RoomType.VIDEO,
                tags = listOf("#Music", "#Acoustic", "#AddaBuzz"),
                livekitRoomName = "adda_music_room_01",
                likesCount = 12500L
            ),
            LiveRoom(
                id = "room_02",
                host = creator3,
                title = "Clubhouse Style 8-Guest Voice Adda! Open Mic 🎙️",
                category = "Audio Party",
                coverUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600",
                viewerCount = 890,
                roomType = RoomType.MULTI_GUEST,
                tags = listOf("#OpenMic", "#8Guests", "#FunTalk"),
                livekitRoomName = "adda_multiguest_02",
                likesCount = 8400L
            ),
            LiveRoom(
                id = "room_03",
                host = creator2,
                title = "Rank Push + Room Matches! Giving Gifts to MVPs 🔥",
                category = "Gaming Arena",
                coverUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=600",
                viewerCount = 3280,
                roomType = RoomType.VIDEO,
                tags = listOf("#Gaming", "#BGMI", "#Tournament"),
                livekitRoomName = "adda_gaming_03",
                likesCount = 28900L
            ),
            LiveRoom(
                id = "room_04",
                host = creator4,
                title = "AI Coding & Tech Chill Out with Coffee ☕",
                category = "Education & Tech",
                coverUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=600",
                viewerCount = 460,
                roomType = RoomType.AUDIO,
                tags = listOf("#Android", "#AI", "#TechTalk"),
                livekitRoomName = "adda_tech_04",
                likesCount = 4100L
            )
        )
        _allRooms.value = rooms
        _followingUsers.value = listOf(creator1, creator2, creator3)

        val follower1 = User(
            id = "fol_1",
            username = "sakib_99",
            displayName = "Sakib Hasan",
            email = "sakib@mail.com",
            avatarUrl = "https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=200",
            bio = "Big fan of acoustic music! 🎵",
            followersCount = 45,
            followingCount = 120
        )
        val follower2 = User(
            id = "fol_2",
            username = "meghla_roy",
            displayName = "Meghla Roy",
            email = "meghla@mail.com",
            avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200",
            bio = "Live streaming enthusiast 🌸",
            followersCount = 210,
            followingCount = 89
        )
        _followersUsers.value = listOf(follower1, follower2, creator4)

        // Seed Notifications
        _notifications.value = listOf(
            NotificationItem(
                id = "notif_1",
                title = "Anika Rahman is LIVE now! 🎶",
                description = "Started streaming: Late Night Acoustic Melodies & Adda 🎸✨",
                timeAgo = "5m ago",
                type = NotificationType.LIVE_ALERT,
                actionData = "room_01"
            ),
            NotificationItem(
                id = "notif_2",
                title = "Gift Received! 🚀",
                description = "Sakib Hasan sent you a Space Rocket (1,000 Coins) in your stream!",
                timeAgo = "1h ago",
                type = NotificationType.GIFT_RECEIVED
            ),
            NotificationItem(
                id = "notif_3",
                title = "New Follower",
                description = "Meghla Roy started following you.",
                timeAgo = "3h ago",
                type = NotificationType.FOLLOW
            ),
            NotificationItem(
                id = "notif_4",
                title = "Welcome to AddaBuzz Live! 🎉",
                description = "Enjoy seamless streaming, 8-seat audio rooms, and connect with creators.",
                timeAgo = "1d ago",
                type = NotificationType.SYSTEM
            )
        )

        // Seed Conversations & Messages
        _conversations.value = listOf(
            Conversation(
                id = "conv_1",
                peer = creator1,
                lastMessage = "Thanks for the rocket in my stream! You're awesome!",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 12,
                unreadCount = 1
            ),
            Conversation(
                id = "conv_2",
                peer = creator3,
                lastMessage = "Are you joining the multi-guest audio room tonight?",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                unreadCount = 0
            )
        )

        _directMessages.value = mapOf(
            "conv_1" to listOf(
                PrivateMessage(
                    id = "msg_1",
                    conversationId = "conv_1",
                    senderId = creator1.id,
                    content = "Hey Faysel! Welcome to AddaBuzz Live!",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60,
                    isFromMe = false
                ),
                PrivateMessage(
                    id = "msg_2",
                    conversationId = "conv_1",
                    senderId = "me",
                    content = "Love your acoustic songs Anika! Keep rocking 🎸",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                    isFromMe = true
                ),
                PrivateMessage(
                    id = "msg_3",
                    conversationId = "conv_1",
                    senderId = creator1.id,
                    content = "Thanks for the rocket in my stream! You're awesome!",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 12,
                    isFromMe = false
                )
            ),
            "conv_2" to listOf(
                PrivateMessage(
                    id = "msg_21",
                    conversationId = "conv_2",
                    senderId = creator3.id,
                    content = "Are you joining the multi-guest audio room tonight?",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                    isFromMe = false
                )
            )
        )

        // Seed Transactions
        val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        _transactions.value = listOf(
            WalletTransaction(
                id = "tx_1",
                title = "Coin Recharge (+2,500 🪙)",
                amount = "+2,500 🪙",
                date = dateFormat.format(Date(System.currentTimeMillis() - 1000 * 60 * 90)),
                isCredit = true,
                type = "RECHARGE"
            ),
            WalletTransaction(
                id = "tx_2",
                title = "Gift Sent: Space Rocket 🚀",
                amount = "-1,000 🪙",
                date = dateFormat.format(Date(System.currentTimeMillis() - 1000 * 60 * 180)),
                isCredit = false,
                type = "GIFT"
            ),
            WalletTransaction(
                id = "tx_3",
                title = "Diamonds Withdrawn (1,200 💎)",
                amount = "-1,200 💎",
                date = dateFormat.format(Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)),
                isCredit = false,
                type = "WITHDRAW"
            )
        )
    }

    // Firebase Firestore realtime listeners for live room chat
    fun startLiveRoomMessagesListener(roomId: String) {
        stopLiveRoomMessagesListener()
        val db = firestore ?: return
        liveMessagesListener = db
            .collection("live_rooms")
            .document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                val messages = snapshots?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val senderId = data["sender_id"] as? String ?: return@mapNotNull null
                    ChatMessage(
                        id = doc.id,
                        sender = User(
                            id = senderId,
                            username = (data["sender_username"] as? String) ?: "user",
                            displayName = (data["sender_name"] as? String) ?: "User",
                            email = (data["sender_email"] as? String) ?: "",
                            avatarUrl = (data["sender_avatar"] as? String) ?: ""
                        ),
                        text = (data["text"] as? String) ?: "",
                        timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis(),
                        isGift = (data["is_gift"] as? Boolean) ?: false,
                        giftName = data["gift_name"] as? String,
                        giftIcon = data["gift_icon"] as? String,
                        giftAmount = (data["gift_amount"] as? Long)?.toInt()
                    )
                } ?: emptyList()
                _liveMessages.value = messages
            }
    }

    fun stopLiveRoomMessagesListener() {
        liveMessagesListener?.remove()
        liveMessagesListener = null
    }

    // Gift Catalog (priced in Coins)
    val availableGifts = listOf(
        Gift(
            id = "gift_rose",
            name = "Red Rose",
            iconEmoji = "🌹",
            coinPrice = 10,
            animationType = "BURST",
            description = "Send a lovely rose"
        ),
        Gift(
            id = "gift_heart",
            name = "Heart Balloon",
            iconEmoji = "💖",
            coinPrice = 50,
            animationType = "FLOAT",
            description = "Floating neon hearts"
        ),
        Gift(
            id = "gift_car",
            name = "Cyber Sportscar",
            iconEmoji = "🏎️",
            coinPrice = 500,
            animationType = "DRIVE",
            description = "Electric neon supercar sweep"
        ),
        Gift(
            id = "gift_rocket",
            name = "Space Rocket",
            iconEmoji = "🚀",
            coinPrice = 1000,
            animationType = "ROCKET_LAUNCH",
            description = "Blasts off into the galaxy"
        ),
        Gift(
            id = "gift_galaxy",
            name = "Super Galaxy",
            iconEmoji = "🌌",
            coinPrice = 5000,
            animationType = "COSMIC_BURST",
            description = "Spectacular cosmic portal"
        ),
        Gift(
            id = "gift_crown",
            name = "Golden Crown",
            iconEmoji = "👑",
            coinPrice = 10000,
            animationType = "CROWN_CORONATION",
            description = "Coronate the host as royalty!"
        )
    )

    // Recharge Packages (buy Coins)
    val rechargePackages = listOf(
        RechargePackage("pkg_1", coins = 100, bonusCoins = 10, priceUsd = 0.99),
        RechargePackage("pkg_2", coins = 550, bonusCoins = 70, priceUsd = 4.99, isHot = true),
        RechargePackage("pkg_3", coins = 1200, bonusCoins = 200, priceUsd = 9.99),
        RechargePackage("pkg_4", coins = 2600, bonusCoins = 500, priceUsd = 19.99, isHot = true),
        RechargePackage("pkg_5", coins = 7000, bonusCoins = 1500, priceUsd = 49.99),
        RechargePackage("pkg_6", coins = 15000, bonusCoins = 4000, priceUsd = 99.99)
    )

    // Room operations
    suspend fun joinRoom(room: LiveRoom, asHost: Boolean = false) {
        _currentRoom.value = room
        _reactionCount.value = room.likesCount

        // Reset or populate 8 seats for multi-guest room
        if (room.roomType == RoomType.MULTI_GUEST || room.roomType == RoomType.AUDIO) {
            val updatedSeats = MutableList(8) { idx ->
                if (idx == 0) {
                    GuestSeat(index = 0, guest = room.host, isLocked = false, isMuted = false, isSpeaking = true)
                } else if (idx == 1 && !asHost) {
                    GuestSeat(
                        index = 1,
                        guest = User(
                            id = "gst_1",
                            username = "tahmid_dj",
                            displayName = "Tahmid 🎧",
                            email = "tahmid@mail.com",
                            avatarUrl = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=200",
                            bio = "DJ & Beats"
                        ),
                        isLocked = false,
                        isMuted = false,
                        isSpeaking = false
                    )
                } else {
                    GuestSeat(index = idx, guest = null, isLocked = false, isMuted = false, isSpeaking = false)
                }
            }
            _guestSeats.value = updatedSeats
        }

        // Connect LiveKit room
        liveKitManager.joinRoom(
            serverUrl = sessionManager.getServerUrl(),
            token = room.livekitToken.ifBlank { "token_${room.id}_${System.currentTimeMillis()}" },
            isVideoRoom = room.roomType != RoomType.AUDIO,
            asPublisher = asHost
        )

        // Listen to live room chat messages via Firebase Firestore (realtime)
        startLiveRoomMessagesListener(room.id)

        // Seed chat messages
        _liveMessages.value = listOf(
            ChatMessage(
                id = "cm_1",
                sender = User("u1", "samir", "Samir", "s@m.com", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200"),
                text = "Hello everyone! Loving the vibes tonight! ✨"
            ),
            ChatMessage(
                id = "cm_2",
                sender = User("u2", "faria", "Faria Noor", "f@m.com", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200"),
                text = "Welcome to AddaBuzz Live! 🎉"
            )
        )
    }

    fun leaveCurrentRoom() {
        stopLiveRoomMessagesListener()
        liveKitManager.leaveRoom()
        _currentRoom.value = null
        _liveMessages.value = emptyList()
        _pendingGuestRequests.value = emptyList()
    }

    fun sendLiveMessage(text: String) {
        val user = sessionManager.currentUser.value ?: return
        val msg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            sender = user,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        _liveMessages.value = _liveMessages.value + msg

        // Persist live room chat message to Firebase Firestore
        val roomId = _currentRoom.value?.id ?: "room_${System.currentTimeMillis()}"
        firestore?.collection("live_rooms")?.document(roomId)?.collection("messages")
            ?.add(
                hashMapOf(
                    "sender_id" to user.id,
                    "sender_username" to user.username,
                    "sender_name" to user.displayName,
                    "sender_avatar" to user.avatarUrl,
                    "sender_email" to user.email,
                    "text" to text,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "is_gift" to false
                )
            )

        // Add auto-reply from audience after slight delay for lively experience
        scope.launch {
            delay(2500)
            val audienceReplies = listOf(
                "Awesome stream! 🔥",
                "Can you sing that acoustic track again? 🎵",
                "Tap tap the screen for hearts! ❤️",
                "AddaBuzz Live is so smooth today! 🚀",
                "Big love from Dhaka! 💖"
            )
            val randomReply = audienceReplies.random()
            val peerMsg = ChatMessage(
                id = "reply_${System.currentTimeMillis()}",
                sender = User(
                    id = "aud_${System.currentTimeMillis()}",
                    username = "viewer_${(10..99).random()}",
                    displayName = "Buzz Fan ${(1..20).random()}",
                    email = "viewer@mail.com",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200"
                ),
                text = randomReply
            )
            _liveMessages.value = _liveMessages.value + peerMsg
        }
    }

    fun sendGift(gift: Gift): Boolean {
        val user = sessionManager.currentUser.value ?: return false
        if (user.coins < gift.coinPrice) {
            return false
        }

        val newCoinBalance = user.coins - gift.coinPrice
        sessionManager.updateCoins(newCoinBalance)

        val giftMessage = ChatMessage(
            id = "gift_${System.currentTimeMillis()}",
            sender = user,
            text = "Sent ${gift.name} ${gift.iconEmoji} (${gift.coinPrice} 🪙)",
            isGift = true,
            giftName = gift.name,
            giftIcon = gift.iconEmoji,
            giftAmount = gift.coinPrice
        )

        _liveMessages.value = _liveMessages.value + giftMessage
        _giftCelebrationEvent.tryEmit(giftMessage)

        // Persist gift message to Firebase Firestore
        val roomId = _currentRoom.value?.id ?: "room_${System.currentTimeMillis()}"
        firestore?.collection("live_rooms")?.document(roomId)?.collection("messages")
            ?.add(
                hashMapOf(
                    "sender_id" to user.id,
                    "sender_username" to user.username,
                    "sender_name" to user.displayName,
                    "sender_avatar" to user.avatarUrl,
                    "sender_email" to user.email,
                    "text" to "Sent ${gift.name} ${gift.iconEmoji} (${gift.coinPrice} 🪙)",
                    "timestamp" to FieldValue.serverTimestamp(),
                    "is_gift" to true,
                    "gift_name" to gift.name,
                    "gift_icon" to gift.iconEmoji,
                    "gift_amount" to gift.coinPrice.toLong()
                )
            )

        // Log transaction (coin spend)
        val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        val tx = WalletTransaction(
            id = "tx_${System.currentTimeMillis()}",
            title = "Sent Gift: ${gift.name} ${gift.iconEmoji}",
            amount = "-${gift.coinPrice} 🪙",
            date = dateFormat.format(Date()),
            isCredit = false,
            type = "GIFT"
        )
        _transactions.value = listOf(tx) + _transactions.value

        // Increase host withdrawable diamonds (earnings)
        _currentRoom.value?.host?.let { host ->
            val earnedDiamonds = (gift.coinPrice * 0.7).toLong()
            if (host.id == user.id) {
                sessionManager.updateDiamonds(user.diamonds + earnedDiamonds)
            }
        }

        return true
    }

    fun addReaction() {
        _reactionCount.value = _reactionCount.value + 1
    }

    // 8 Multi-Guest Room Seat Actions
    fun requestSeat(): Boolean {
        val user = sessionManager.currentUser.value ?: return false
        if (_pendingGuestRequests.value.none { it.id == user.id }) {
            _pendingGuestRequests.value = _pendingGuestRequests.value + user
            return true
        }
        return false
    }

    fun acceptGuestRequest(user: User, targetSeatIndex: Int? = null) {
        _pendingGuestRequests.value = _pendingGuestRequests.value.filter { it.id != user.id }
        val seats = _guestSeats.value.toMutableList()
        val emptyIndex = targetSeatIndex ?: seats.indexOfFirst { it.guest == null && !it.isLocked }
        if (emptyIndex != -1 && emptyIndex in seats.indices) {
            seats[emptyIndex] = seats[emptyIndex].copy(
                guest = user,
                isMuted = false,
                isSpeaking = false
            )
            _guestSeats.value = seats
        }
    }

    fun rejectGuestRequest(user: User) {
        _pendingGuestRequests.value = _pendingGuestRequests.value.filter { it.id != user.id }
    }

    fun toggleSeatLock(seatIndex: Int) {
        val seats = _guestSeats.value.toMutableList()
        if (seatIndex in seats.indices) {
            val current = seats[seatIndex]
            seats[seatIndex] = current.copy(isLocked = !current.isLocked)
            _guestSeats.value = seats
        }
    }

    fun toggleGuestMute(seatIndex: Int) {
        val seats = _guestSeats.value.toMutableList()
        if (seatIndex in seats.indices) {
            val current = seats[seatIndex]
            seats[seatIndex] = current.copy(isMuted = !current.isMuted)
            _guestSeats.value = seats
        }
    }

    fun removeGuestFromSeat(seatIndex: Int) {
        val seats = _guestSeats.value.toMutableList()
        if (seatIndex in seats.indices) {
            seats[seatIndex] = seats[seatIndex].copy(guest = null, isSpeaking = false)
            _guestSeats.value = seats
        }
    }

    fun leaveSeat() {
        val user = sessionManager.currentUser.value ?: return
        val seats = _guestSeats.value.toMutableList()
        val mySeatIndex = seats.indexOfFirst { it.guest?.id == user.id }
        if (mySeatIndex != -1) {
            seats[mySeatIndex] = seats[mySeatIndex].copy(guest = null, isSpeaking = false)
            _guestSeats.value = seats
        }
    }

    // Wallet Recharge & Withdraw
    fun rechargeCoins(pkg: RechargePackage): Boolean {
        val user = sessionManager.currentUser.value ?: return false
        val totalCoins = pkg.coins + pkg.bonusCoins
        val newBalance = user.coins + totalCoins
        sessionManager.updateCoins(newBalance)

        val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        val tx = WalletTransaction(
            id = "tx_${System.currentTimeMillis()}",
            title = "Coin Recharge +$totalCoins 🪙 ($${pkg.priceUsd})",
            amount = "+$totalCoins 🪙",
            date = dateFormat.format(Date()),
            isCredit = true,
            type = "RECHARGE"
        )
        _transactions.value = listOf(tx) + _transactions.value
        return true
    }

    fun withdrawDiamonds(amount: Long): Boolean {
        val user = sessionManager.currentUser.value ?: return false
        if (user.diamonds < amount || amount <= 0) return false

        sessionManager.updateDiamonds(user.diamonds - amount)

        val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        val tx = WalletTransaction(
            id = "tx_${System.currentTimeMillis()}",
            title = "Diamonds Withdrawn",
            amount = "-$amount 💎",
            date = dateFormat.format(Date()),
            isCredit = false,
            type = "WITHDRAW"
        )
        _transactions.value = listOf(tx) + _transactions.value
        return true
    }

    // Follow / Unfollow
    fun toggleFollow(user: User): Boolean {
        val isCurrentlyFollowing = _followingUsers.value.any { it.id == user.id }
        if (isCurrentlyFollowing) {
            _followingUsers.value = _followingUsers.value.filter { it.id != user.id }
            return false
        } else {
            _followingUsers.value = _followingUsers.value + user
            return true
        }
    }

    // Private Messaging
    fun sendDirectMessage(conversationId: String, text: String) {
        val newMsg = PrivateMessage(
            id = "dm_${System.currentTimeMillis()}",
            conversationId = conversationId,
            senderId = "me",
            content = text,
            timestamp = System.currentTimeMillis(),
            isFromMe = true
        )
        val currentList = _directMessages.value[conversationId] ?: emptyList()
        val updatedMap = _directMessages.value.toMutableMap()
        updatedMap[conversationId] = currentList + newMsg
        _directMessages.value = updatedMap

        // Update conversation preview
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == conversationId) {
                conv.copy(lastMessage = text, timestamp = System.currentTimeMillis())
            } else conv
        }

        // Persist direct message to Firebase Firestore
        firestore?.collection("conversations")?.document(conversationId)?.collection("messages")
            ?.add(
                hashMapOf(
                    "sender_id" to "me",
                    "text" to text,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "is_from_me" to true
                )
            )

        // Friendly auto-reply simulation
        scope.launch {
            delay(1500)
            val autoReply = PrivateMessage(
                id = "dm_reply_${System.currentTimeMillis()}",
                conversationId = conversationId,
                senderId = "peer",
                content = "Thanks for your message! Looking forward to chatting in the live room 🎙️✨",
                timestamp = System.currentTimeMillis(),
                isFromMe = false
            )
            val updatedMap2 = _directMessages.value.toMutableMap()
            updatedMap2[conversationId] = (updatedMap2[conversationId] ?: emptyList()) + autoReply
            _directMessages.value = updatedMap2
        }
    }

    // Reward tasks: claim once, coins are credited to the wallet
    fun claimReward(rewardId: String, coins: Long): Boolean {
        val user = sessionManager.currentUser.value ?: return false
        if (_claimedRewards.value.contains(rewardId)) return false
        _claimedRewards.value = _claimedRewards.value + rewardId
        sessionManager.updateCoins(user.coins + coins)
        recordTransaction("Reward: ${rewardId.replace('_', ' ')}", "+$coins 🪙", isCredit = true, type = "REWARD")
        return true
    }

    // Store purchase: spend coins on a gift item
    fun buyGift(gift: Gift): Boolean {
        val user = sessionManager.currentUser.value ?: return false
        if (user.coins < gift.coinPrice) return false
        sessionManager.updateCoins(user.coins - gift.coinPrice)
        recordTransaction("Bought ${gift.name} ${gift.iconEmoji}", "-${gift.coinPrice} 🪙", isCredit = false, type = "STORE")
        return true
    }

    // Fan club membership
    fun joinFanClub(hostId: String, costCoins: Long): Boolean {
        val user = sessionManager.currentUser.value ?: return false
        if (_fanClubMemberships.value.contains(hostId)) return true
        if (user.coins < costCoins) return false
        sessionManager.updateCoins(user.coins - costCoins)
        _fanClubMemberships.value = _fanClubMemberships.value + hostId
        recordTransaction("Joined fan club", "-$costCoins 🪙", isCredit = false, type = "FAN_CLUB")
        return true
    }

    fun leaveFanClub(hostId: String) {
        _fanClubMemberships.value = _fanClubMemberships.value - hostId
    }

    // Dice Duel: predict a number, win double the wager
    fun playDiceGame(wager: Long, pick: Int): GameResult? {
        val user = sessionManager.currentUser.value ?: return null
        if (pick !in 1..6 || wager <= 0 || user.coins < wager) return null
        val roll = (1..6).random()
        val won = roll == pick
        val payout = if (won) wager * 2 else 0L
        sessionManager.updateCoins(user.coins - wager + payout)
        recordTransaction(
            title = "Dice Duel — rolled $roll, picked $pick",
            amount = if (won) "+$payout 🪙" else "-$wager 🪙",
            isCredit = won,
            type = "GAME"
        )
        return GameResult(roll = roll, pick = pick, wager = wager, payout = payout, won = won)
    }

    private fun recordTransaction(title: String, amount: String, isCredit: Boolean, type: String) {
        val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        val tx = WalletTransaction(
            id = "tx_${System.currentTimeMillis()}",
            title = title,
            amount = amount,
            date = dateFormat.format(Date()),
            isCredit = isCredit,
            type = type
        )
        _transactions.value = listOf(tx) + _transactions.value
    }

    // Create a new stream
    fun createLiveRoom(
        title: String,
        category: String,
        roomType: RoomType
    ): LiveRoom {
        val user = sessionManager.currentUser.value ?: User(
            id = "usr_fayselsk_01",
            username = "fayselsk",
            displayName = "Faysel SK",
            email = "fayselsk@gmail.com",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
            bio = "Official AddaBuzz Streamer"
        )
        val newRoom = LiveRoom(
            id = "my_room_${System.currentTimeMillis()}",
            host = user,
            title = title.ifBlank { "Live with ${user.displayName} 🔥" },
            category = category,
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
            viewerCount = 1,
            roomType = roomType,
            tags = listOf("#AddaBuzz", "#LiveNow", "#BanglaStream"),
            livekitRoomName = "room_${user.username}_${System.currentTimeMillis()}",
            livekitToken = "token_publisher_${System.currentTimeMillis()}"
        )
        _allRooms.value = listOf(newRoom) + _allRooms.value
        return newRoom
    }
}
