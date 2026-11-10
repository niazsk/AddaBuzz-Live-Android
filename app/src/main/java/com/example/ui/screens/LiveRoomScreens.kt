package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.AddaBuzzRepository
import com.example.data.LiveKitManager
import com.example.model.GuestSeat
import com.example.model.LiveRoom
import com.example.model.RoomType
import com.example.model.User
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveRoomScreen(
    room: LiveRoom,
    isHost: Boolean,
    repository: AddaBuzzRepository,
    onLeaveRoom: () -> Unit,
    onNavigateWallet: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val currentUser by repository.sessionManager.currentUser.collectAsState()
    val liveMessages by repository.liveMessages.collectAsState()
    val reactionCount by repository.reactionCount.collectAsState()
    val connectionState by repository.liveKitManager.connectionState.collectAsState()
    val isMicEnabled by repository.liveKitManager.isMicEnabled.collectAsState()
    val isFrontCamera by repository.liveKitManager.isFrontCamera.collectAsState()

    var showGiftSheet by remember { mutableStateOf(false) }
    var chatInput by remember { mutableStateOf("") }
    var celebrationMessage by remember { mutableStateOf<String?>(null) }
    var isFollowingHost by remember { mutableStateOf(false) }

    // Camera & Mic Permissions launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // handled
    }

    LaunchedEffect(Unit) {
        val neededPermissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (neededPermissions.isNotEmpty()) {
            permissionLauncher.launch(neededPermissions.toTypedArray())
        }
    }

    // Auto-scroll chat to bottom
    LaunchedEffect(liveMessages.size) {
        if (liveMessages.isNotEmpty()) {
            listState.animateScrollToItem(liveMessages.size - 1)
        }
    }

    // Listen for gift celebration
    LaunchedEffect(Unit) {
        repository.giftCelebrationEvent.collect { event ->
            celebrationMessage = "${event.sender.displayName} sent ${event.giftName} ${event.giftIcon}!"
            kotlinx.coroutines.delay(3500)
            celebrationMessage = null
        }
    }

    BackHandler {
        repository.leaveCurrentRoom()
        onLeaveRoom()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                repository.addReaction()
            }
    ) {
        // Video Layer: Camera Preview (for Host) or Stream Visual (for Viewers)
        if (isHost) {
            CameraPreview(
                isFrontCamera = isFrontCamera,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = room.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Dark gradient scrims for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Floating Hearts Bursts
        FloatingHeartsBurst(triggerCount = reactionCount)

        // Celebration Banner
        AnimatedVisibility(
            visible = celebrationMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceDark.copy(alpha = 0.95f),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.5.dp, brush = Brush.horizontalGradient(listOf(GoldCoin, NeonPink))),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎉", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = celebrationMessage ?: "",
                        style = Typography.titleMedium.copy(color = GoldCoin, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Top Overlay: Host Pill, Viewer Counter, Close
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Host info pill
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.55f),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(PurplePrimary, ElectricCyan)))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = room.host.avatarUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = room.host.displayName,
                            style = Typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Text(
                            text = "${room.viewerCount} Viewers",
                            style = Typography.labelSmall.copy(color = ElectricCyan, fontSize = 10.sp)
                        )
                    }
                    if (!isHost) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                isFollowingHost = !isFollowingHost
                                repository.toggleFollow(room.host)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowingHost) SurfaceGlass else PurplePrimary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = if (isFollowingHost) "Following" else "+ Follow",
                                style = Typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // Connection state & Close Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (connectionState == LiveKitManager.RoomState.CONNECTED) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EmeraldGreen.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = "HD LIVE",
                            style = Typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        repository.leaveCurrentRoom()
                        onLeaveRoom()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Leave Stream",
                        tint = Color.White
                    )
                }
            }
        }

        // Live Chat Messages (lower left overlay)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.85f)
                .padding(start = 16.dp, bottom = 80.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
            ) {
                items(liveMessages) { msg ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier.padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = msg.sender.displayName,
                                style = Typography.labelMedium.copy(
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = msg.text,
                                style = Typography.bodyMedium.copy(
                                    color = if (msg.isGift) GoldCoin else Color.White,
                                    fontWeight = if (msg.isGift) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }
        }

        // Bottom Bar: Chat input & Action buttons
        Surface(
            color = Color.Black.copy(alpha = 0.75f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chat input
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Comment or send love...", color = TextSecondary, fontSize = 13.sp) },
                    trailingIcon = {
                        if (chatInput.isNotBlank()) {
                            IconButton(onClick = {
                                repository.sendLiveMessage(chatInput)
                                chatInput = ""
                            }) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = ElectricCyan)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = SurfaceCard.copy(alpha = 0.7f),
                        unfocusedContainerColor = SurfaceCard.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Host Controls: Flip Camera & Mic Toggle
                if (isHost) {
                    IconButton(
                        onClick = { repository.liveKitManager.flipCamera() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                    ) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { coroutineScope.launch { repository.liveKitManager.toggleMicrophone() } },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isMicEnabled) SurfaceCard else LiveRed)
                    ) {
                        Icon(
                            if (isMicEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Mic",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Gift Button (glowing gradient)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(PurplePrimary, NeonPink, GoldCoin))
                        )
                        .clickable { showGiftSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎁", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Heart reaction button
                IconButton(
                    onClick = { repository.addReaction() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NeonPink.copy(alpha = 0.85f))
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = "Love", tint = Color.White)
                }
            }
        }
    }

    // Gift sheet modal
    if (showGiftSheet) {
        GiftBottomSheet(
            gifts = repository.availableGifts,
            diamonds = currentUser?.diamonds ?: 0L,
            onSendGift = { gift ->
                val sent = repository.sendGift(gift)
                if (sent) showGiftSheet = false
            },
            onRechargeClick = {
                showGiftSheet = false
                onNavigateWallet()
            },
            onDismiss = { showGiftSheet = false }
        )
    }
}

// 8 Multi-Guest Room Screen (Full 8 seats display & management)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiGuestRoomScreen(
    room: LiveRoom,
    isHost: Boolean,
    repository: AddaBuzzRepository,
    onLeaveRoom: () -> Unit,
    onNavigateWallet: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUser by repository.sessionManager.currentUser.collectAsState()
    val guestSeats by repository.guestSeats.collectAsState()
    val pendingRequests by repository.pendingGuestRequests.collectAsState()
    val liveMessages by repository.liveMessages.collectAsState()
    val isMicEnabled by repository.liveKitManager.isMicEnabled.collectAsState()

    var showGiftSheet by remember { mutableStateOf(false) }
    var showGuestRequestsDialog by remember { mutableStateOf(false) }
    var selectedSeatForHostManage by remember { mutableStateOf<GuestSeat?>(null) }
    var chatInput by remember { mutableStateOf("") }
    var requestStatusMessage by remember { mutableStateOf<String?>(null) }

    val mySeat = remember(guestSeats, currentUser) {
        guestSeats.firstOrNull { it.guest?.id == currentUser?.id }
    }

    BackHandler {
        repository.leaveCurrentRoom()
        onLeaveRoom()
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = room.title,
                            style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "8-Guest Audio Stage • ${room.viewerCount} Viewers",
                            style = Typography.labelSmall.copy(color = ElectricCyan)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        repository.leaveCurrentRoom()
                        onLeaveRoom()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    if (isHost) {
                        BadgedBox(
                            badge = {
                                if (pendingRequests.isNotEmpty()) {
                                    Badge(containerColor = LiveRed) {
                                        Text("${pendingRequests.size}")
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = { showGuestRequestsDialog = true }) {
                                Icon(Icons.Default.GroupAdd, contentDescription = "Requests", tint = ElectricCyan)
                            }
                        }
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
            // Stage banner & Host Podium
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = room.host.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(2.dp, GoldCoin, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👑 Host: ", style = Typography.labelSmall.copy(color = GoldCoin, fontWeight = FontWeight.Bold))
                        Text(
                            text = room.host.displayName,
                            style = Typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Topic: Chit-Chat, open mic & storytelling",
                        style = Typography.labelSmall.copy(color = TextSecondary)
                    )
                }

                // Guest action: Request Seat / Leave Seat
                if (!isHost) {
                    if (mySeat == null) {
                        Button(
                            onClick = {
                                val ok = repository.requestSeat()
                                requestStatusMessage = if (ok) "Seat request sent to host!" else "Already requested!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Request Seat", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    } else {
                        Button(
                            onClick = { repository.leaveSeat() },
                            colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Leave Seat", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            if (requestStatusMessage != null) {
                Surface(
                    color = PurplePrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = requestStatusMessage!!,
                        style = Typography.labelSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // The 8 Guest Seats Grid
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                GuestSeatsGrid(
                    seats = guestSeats,
                    isHost = isHost,
                    onSeatClick = { seat ->
                        if (isHost) {
                            selectedSeatForHostManage = seat
                        } else if (seat.guest == null && !seat.isLocked) {
                            repository.requestSeat()
                            requestStatusMessage = "Requested to take Seat ${seat.index + 1}!"
                        }
                    }
                )
            }

            // Live Chat inside Multi-Guest Room
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Live Adda Chat",
                style = Typography.labelLarge.copy(color = TextSecondary, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(liveMessages) { msg ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${msg.sender.displayName}: ",
                            style = Typography.labelMedium.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = msg.text,
                            style = Typography.bodyMedium.copy(color = TextPrimary)
                        )
                    }
                }
            }

            // Bottom Bar: Chat input, Mic toggle, Gift
            Surface(
                color = SurfaceDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Chat in audio party...", color = TextSecondary, fontSize = 13.sp) },
                        trailingIcon = {
                            if (chatInput.isNotBlank()) {
                                IconButton(onClick = {
                                    repository.sendLiveMessage(chatInput)
                                    chatInput = ""
                                }) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", tint = ElectricCyan)
                                }
                            }
                        },
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
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Mic mute toggle if host or guest seated
                    if (isHost || mySeat != null) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    repository.liveKitManager.toggleMicrophone()
                                    mySeat?.let { repository.toggleGuestMute(it.index) }
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isMicEnabled) PurplePrimary else LiveRed)
                        ) {
                            Icon(
                                if (isMicEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Mic",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Gift launcher
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(BrandGradient)
                            .clickable { showGiftSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎁", fontSize = 20.sp)
                    }
                }
            }
        }
    }

    // Host Seat Management Modal
    selectedSeatForHostManage?.let { seat ->
        AlertDialog(
            onDismissRequest = { selectedSeatForHostManage = null },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "Manage Seat ${seat.index + 1}",
                    style = Typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    if (seat.guest != null) {
                        Text(
                            text = "Current Guest: ${seat.guest.displayName}",
                            style = Typography.bodyLarge.copy(color = ElectricCyan)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                repository.toggleGuestMute(seat.index)
                                selectedSeatForHostManage = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (seat.isMuted) "Unmute Guest" else "Mute Guest")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                repository.removeGuestFromSeat(seat.index)
                                selectedSeatForHostManage = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Remove from Seat")
                        }
                    } else {
                        Text("Seat is currently empty", style = Typography.bodyMedium.copy(color = TextSecondary))
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                repository.toggleSeatLock(seat.index)
                                selectedSeatForHostManage = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (seat.isLocked) "Unlock Seat" else "Lock Seat")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSeatForHostManage = null }) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }

    // Host Join Requests Dialog
    if (showGuestRequestsDialog) {
        AlertDialog(
            onDismissRequest = { showGuestRequestsDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text("Guest Mic Requests", style = Typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
            },
            text = {
                if (pendingRequests.isEmpty()) {
                    Text("No pending requests right now.", style = Typography.bodyMedium.copy(color = TextSecondary))
                } else {
                    LazyColumn {
                        items(pendingRequests) { reqUser ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = reqUser.avatarUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = reqUser.displayName,
                                        style = Typography.labelLarge.copy(color = TextPrimary)
                                    )
                                }
                                Row {
                                    IconButton(onClick = { repository.acceptGuestRequest(reqUser) }) {
                                        Icon(Icons.Default.Check, contentDescription = "Accept", tint = EmeraldGreen)
                                    }
                                    IconButton(onClick = { repository.rejectGuestRequest(reqUser) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Reject", tint = LiveRed)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGuestRequestsDialog = false }) {
                    Text("Done", color = ElectricCyan)
                }
            }
        )
    }

    if (showGiftSheet) {
        GiftBottomSheet(
            gifts = repository.availableGifts,
            diamonds = currentUser?.diamonds ?: 0L,
            onSendGift = { gift ->
                val sent = repository.sendGift(gift)
                if (sent) showGiftSheet = false
            },
            onRechargeClick = {
                showGiftSheet = false
                onNavigateWallet()
            },
            onDismiss = { showGiftSheet = false }
        )
    }
}
