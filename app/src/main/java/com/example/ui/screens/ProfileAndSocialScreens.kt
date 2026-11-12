package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AddaBuzzRepository
import com.example.model.Conversation
import com.example.model.NotificationType
import com.example.model.User
import com.example.ui.components.LiveBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    repository: AddaBuzzRepository,
    onNavigateFollowers: () -> Unit,
    onNavigateFollowing: () -> Unit,
    onNavigateWallet: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateMenu: (ProfileMenuOption) -> Unit,
    onNavigateAgentMenu: (AgentMenuOption) -> Unit
) {
    val currentUser by repository.sessionManager.currentUser.collectAsState()
    val user = currentUser ?: return
    val followers by repository.followersUsers.collectAsState()
    val following by repository.followingUsers.collectAsState()
    var selectedAgentOption by remember { mutableStateOf<AgentMenuOption?>(null) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("My Profile", style = Typography.titleLarge.copy(color = TextPrimary)) },
                actions = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Profile Card Header
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(PurplePrimary, ElectricCyan))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = user.displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, BrandGradient, CircleShape)
                            )
                            // Level Badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 8.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PurplePrimary)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Lv. ${user.level}",
                                    style = Typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = user.displayName,
                            style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Text(
                            text = "@${user.username} • ${user.badge}",
                            style = Typography.bodyMedium.copy(color = ElectricCyan, fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = user.bio,
                            style = Typography.bodyMedium.copy(color = TextSecondary),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Followers & Following counters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onNavigateFollowers() }
                            ) {
                                Text(
                                    text = "${followers.size}",
                                    style = Typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Followers",
                                    style = Typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                            Divider(
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(1.dp),
                                color = SurfaceCardBorder
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onNavigateFollowing() }
                            ) {
                                Text(
                                    text = "${following.size}",
                                    style = Typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Following",
                                    style = Typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                        }
                    }
                }
            }

            // Profile Options Grid (Reward, Rank, Games, Store, ...)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ProfileOptionsGrid(onSelect = onNavigateMenu)
            }

            // Agent Management Section
            item {
                Spacer(modifier = Modifier.height(20.dp))
                AgentManagementSection(
                    selected = selectedAgentOption,
                    onSelect = { option ->
                        selectedAgentOption = option
                        onNavigateAgentMenu(option)
                    }
                )
            }

            // Coins & Diamonds Options
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Coins & Diamonds",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(PurplePrimary, ElectricCyan))),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateWallet() }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "🪙 Coins", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${user.coins}",
                                style = Typography.headlineSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Use for gifts & all in-app actions",
                                style = Typography.labelSmall.copy(color = TextSecondary)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Top Up →",
                                style = Typography.labelMedium.copy(color = NeonPink, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(GoldCoin, NeonPink))),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateWallet() }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "💎 Diamonds", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${user.diamonds}",
                                style = Typography.headlineSmall.copy(color = GoldCoin, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Withdraw only",
                                style = Typography.labelSmall.copy(color = TextSecondary)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Withdraw →",
                                style = Typography.labelMedium.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Wallet Quick Card
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateWallet() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = ElectricCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Open Wallet",
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Text(
                                    text = "Manage Coins top-up & Diamonds withdrawal",
                                    style = Typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                    }
                }
            }

            // Badges & Honors
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Badges & Honors",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val badges = listOf("👑 VIP Host", "🎙️ Audio Master", "🚀 Rising Star", "💎 Top Giver")
                    badges.forEach { b ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceGlassHigh,
                            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(PurplePrimary, ElectricCyan))),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = b,
                                style = Typography.labelSmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val followers by repository.followersUsers.collectAsState()
    val following by repository.followingUsers.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Followers (${followers.size})", style = Typography.titleLarge.copy(color = TextPrimary)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(followers) { user ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = user.displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = user.displayName,
                                    style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Text(
                                    text = "@${user.username}",
                                    style = Typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                        }
                        Button(
                            onClick = { repository.toggleFollow(user) },
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (following.any { it.id == user.id }) "Following" else "Follow",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingScreen(
    repository: AddaBuzzRepository,
    onJoinStream: (String) -> Unit,
    onBack: () -> Unit
) {
    val following by repository.followingUsers.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Following (${following.size})", style = Typography.titleLarge.copy(color = TextPrimary)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(following) { user ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(contentAlignment = Alignment.Center) {
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = user.displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, if (user.isLive) LiveRed else PurplePrimary, CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = user.displayName,
                                    style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Text(
                                    text = if (user.isLive) "🔴 Streaming live" else "@${user.username}",
                                    style = Typography.labelSmall.copy(color = if (user.isLive) LiveRed else TextSecondary)
                                )
                            }
                        }

                        if (user.isLive) {
                            Button(
                                onClick = { onJoinStream(user.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Join Live", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        } else {
                            OutlinedButton(
                                onClick = { repository.toggleFollow(user) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Following", style = Typography.labelSmall.copy(color = TextSecondary))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateMessagesScreen(
    repository: AddaBuzzRepository
) {
    val conversations by repository.conversations.collectAsState()
    val directMessages by repository.directMessages.collectAsState()

    var activeConversation by remember { mutableStateOf<Conversation?>(null) }
    var messageText by remember { mutableStateOf("") }

    if (activeConversation != null) {
        val conv = activeConversation!!
        val msgs = directMessages[conv.id] ?: emptyList()

        LaunchedEffect(conv.id) {
            repository.markConversationRead(conv.id)
        }

        Scaffold(
            containerColor = BackgroundDark,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = conv.peer.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = conv.peer.displayName, style = Typography.titleMedium.copy(color = TextPrimary))
                                Text(text = "Online", style = Typography.labelSmall.copy(color = EmeraldGreen))
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            activeConversation = null
                            messageText = ""
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(msgs) { msg ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (msg.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (msg.isFromMe) PurplePrimary else SurfaceCard,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = msg.content,
                                    style = Typography.bodyMedium.copy(color = Color.White),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                Surface(
                    color = SurfaceDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Write a message...", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = SurfaceCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = SurfaceCard,
                                unfocusedContainerColor = SurfaceCard
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    repository.sendDirectMessage(conv.id, messageText)
                                    messageText = ""
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(PurplePrimary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        }
    } else {
        Scaffold(
            containerColor = BackgroundDark,
            topBar = {
                TopAppBar(
                    title = { Text("Direct Messages", style = Typography.headlineMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conversations) { conv ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                messageText = ""
                                activeConversation = conv
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = conv.peer.avatarUrl,
                                contentDescription = conv.peer.displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = conv.peer.displayName,
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Text(
                                    text = conv.lastMessage,
                                    style = Typography.bodyMedium.copy(color = TextSecondary),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (conv.unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(LiveRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${conv.unreadCount}",
                                        style = Typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val notifications by repository.notifications.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        repository.markAllNotificationsRead()
    }

    val filteredList = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "Live" -> notifications.filter { it.type == NotificationType.LIVE_ALERT }
            "Gifts" -> notifications.filter { it.type == NotificationType.GIFT_RECEIVED }
            "System" -> notifications.filter { it.type == NotificationType.SYSTEM }
            else -> notifications
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Notifications", style = Typography.titleLarge.copy(color = TextPrimary)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Live", "Gifts", "System").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) PurplePrimary else SurfaceCard,
                        modifier = Modifier.clickable { selectedFilter = filter }
                    ) {
                        Text(
                            text = filter,
                            style = Typography.labelMedium.copy(
                                color = if (isSelected) Color.White else TextSecondary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { notif ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (notif.type) {
                                    NotificationType.LIVE_ALERT -> "🔴"
                                    NotificationType.GIFT_RECEIVED -> "🎁"
                                    NotificationType.FOLLOW -> "👤"
                                    NotificationType.SYSTEM -> "📢"
                                },
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.title,
                                    style = Typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Text(
                                    text = notif.description,
                                    style = Typography.bodyMedium.copy(color = TextSecondary)
                                )
                                Text(
                                    text = notif.timeAgo,
                                    style = Typography.labelSmall.copy(color = ElectricCyan, fontSize = 10.sp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
