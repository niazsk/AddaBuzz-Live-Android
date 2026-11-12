package com.example.model

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String,
    val bio: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val coins: Long = 1250,
    val diamonds: Long = 4800,
    val level: Int = 18,
    val isVip: Boolean = true,
    val isLive: Boolean = false,
    val badge: String = "VIP Host"
)

enum class RoomType {
    VIDEO,
    AUDIO,
    MULTI_GUEST
}

data class LiveRoom(
    val id: String,
    val host: User,
    val title: String,
    val category: String,
    val coverUrl: String,
    val viewerCount: Int,
    val roomType: RoomType = RoomType.VIDEO,
    val tags: List<String> = emptyList(),
    val livekitRoomName: String = "",
    val livekitToken: String = "",
    val likesCount: Long = 3420L,
    val isPkWon: Boolean = false
)

data class GuestSeat(
    val index: Int,
    val guest: User? = null,
    val isLocked: Boolean = false,
    val isMuted: Boolean = false,
    val isSpeaking: Boolean = false
)

data class ChatMessage(
    val id: String,
    val sender: User,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isGift: Boolean = false,
    val giftName: String? = null,
    val giftIcon: String? = null,
    val giftAmount: Int? = null
)

data class Gift(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val coinPrice: Int,
    val animationType: String,
    val description: String
)

data class WalletTransaction(
    val id: String,
    val title: String,
    val amount: String,
    val date: String,
    val isCredit: Boolean,
    val type: String
)

data class RechargePackage(
    val id: String,
    val coins: Int,
    val bonusCoins: Int,
    val priceUsd: Double,
    val isHot: Boolean = false
)

data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val timeAgo: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val actionData: String? = null
)

enum class NotificationType {
    LIVE_ALERT,
    GIFT_RECEIVED,
    SYSTEM,
    FOLLOW
}

data class Conversation(
    val id: String,
    val peer: User,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0
)

data class PrivateMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val isFromMe: Boolean = true
)
