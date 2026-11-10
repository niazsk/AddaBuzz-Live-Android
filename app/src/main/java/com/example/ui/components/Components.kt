package com.example.ui.components

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.model.Gift
import com.example.model.GuestSeat
import com.example.model.LiveRoom
import com.example.ui.theme.*
import kotlin.random.Random

@Composable
fun AddaBuzzTopBar(
    diamonds: Long,
    unreadNotifications: Int,
    onWalletClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceDark.copy(alpha = 0.95f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrandGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "AddaBuzz Logo",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AddaBuzz",
                        style = Typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "LIVE",
                        style = Typography.labelSmall.copy(
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }

            // Right Actions: Diamonds Pill, Search, Notification
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Diamond Balance Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceCard,
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(PurplePrimary, ElectricCyan))),
                    modifier = Modifier
                        .clickable(onClick = onWalletClick)
                        .height(34.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "💎", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = diamonds.toString(),
                            style = Typography.labelLarge.copy(
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Recharge",
                            tint = PurplePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextPrimary
                    )
                }

                Box {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = TextPrimary
                        )
                    }
                    if (unreadNotifications > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(LiveRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unreadNotifications.toString(),
                                style = Typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveStreamCard(
    room: LiveRoom,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), spotColor = PurplePrimary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AsyncImage(
                model = room.coverUrl,
                contentDescription = room.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0x60070510),
                                Color(0xE6070510)
                            )
                        )
                    )
            )

            // Top Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live indicator with animated pulse
                LiveBadge()

                // Viewer counter
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Viewers",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = room.viewerCount.toString(),
                            style = Typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Category tag
            Surface(
                color = SurfaceGlassHigh,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(PurplePrimary, ElectricCyan))),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 42.dp, start = 10.dp)
            ) {
                Text(
                    text = room.category,
                    style = Typography.labelSmall.copy(color = ElectricCyan, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Bottom Info: Host & Title
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = room.host.avatarUrl,
                    contentDescription = room.host.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, ElectricCyan, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = room.title,
                        style = Typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = room.host.displayName,
                        style = Typography.labelSmall.copy(color = TextSecondary),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun LiveBadge(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = LiveRed.copy(alpha = alpha),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "LIVE",
                style = Typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

// 8 Multi-Guest Seats Grid
@Composable
fun GuestSeatsGrid(
    seats: List<GuestSeat>,
    isHost: Boolean,
    onSeatClick: (GuestSeat) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark.copy(alpha = 0.85f))
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🎙️ Guest Stage",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PurplePrimary.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "8 Seats",
                        style = Typography.labelSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            if (isHost) {
                Text(
                    text = "Tap seat to manage",
                    style = Typography.labelSmall.copy(color = TextSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2 rows of 4 seats
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(seats) { seat ->
                GuestSeatItem(
                    seat = seat,
                    onClick = { onSeatClick(seat) }
                )
            }
        }
    }
}

@Composable
fun GuestSeatItem(
    seat: GuestSeat,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voicePulse")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            // Equalizer glowing ring if speaking
            if (seat.guest != null && seat.isSpeaking && !seat.isMuted) {
                Box(
                    modifier = Modifier
                        .size(54.dp * waveScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(ElectricCyan.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                )
            }

            // Seat Avatar / Status Icon
            when {
                seat.guest != null -> {
                    AsyncImage(
                        model = seat.guest.avatarUrl,
                        contentDescription = seat.guest.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (seat.isSpeaking) 2.dp else 1.dp,
                                color = if (seat.isSpeaking) ElectricCyan else PurplePrimary,
                                shape = CircleShape
                            )
                    )

                    // Mute Badge overlay
                    if (seat.isMuted) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(LiveRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MicOff,
                                contentDescription = "Muted",
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
                seat.isLocked -> {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                            .border(1.dp, Color(0xFF4B5563), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                else -> {
                    // Empty seat with "+" invite icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SurfaceGlass)
                            .border(1.dp, SurfaceCardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Empty Seat",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = seat.guest?.displayName ?: "Seat ${seat.index + 1}",
            style = Typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (seat.guest != null) FontWeight.Bold else FontWeight.Normal,
                color = if (seat.guest != null) TextPrimary else TextSecondary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Interactive Gift Catalog Bottom Sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftBottomSheet(
    gifts: List<Gift>,
    diamonds: Long,
    onSendGift: (Gift) -> Unit,
    onRechargeClick: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedGift by remember { mutableStateOf(gifts.firstOrNull()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Header with Diamonds balance & Recharge button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎁 Gift Catalog",
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💎 $diamonds", style = Typography.titleMedium.copy(color = ElectricCyan, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onRechargeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(text = "Recharge", style = Typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid of Gifts
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(gifts) { gift ->
                    val isSelected = selectedGift?.id == gift.id
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) SurfaceCard else SurfaceDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedGift = gift }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) ElectricCyan else SurfaceCardBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = gift.iconEmoji, fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = gift.name,
                                style = Typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "💎 ${gift.diamondPrice}",
                                style = Typography.labelSmall.copy(
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Send Action Button
            Button(
                onClick = {
                    selectedGift?.let { onSendGift(it) }
                },
                enabled = selectedGift != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurplePrimary,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                selectedGift?.let { g ->
                    Text(
                        text = "Send ${g.name} (${g.diamondPrice} 💎)",
                        style = Typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                } ?: Text("Select a Gift", style = Typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Floating Hearts Animation Component
data class FloatingHeart(
    val id: Long,
    val xOffsetFactor: Float,
    val color: Color
)

@Composable
fun FloatingHeartsBurst(
    triggerCount: Long,
    modifier: Modifier = Modifier
) {
    val hearts = remember { mutableStateListOf<FloatingHeart>() }

    LaunchedEffect(triggerCount) {
        if (triggerCount > 0) {
            val colors = listOf(LiveRed, NeonPink, PurplePrimary, ElectricCyan, GoldCoin)
            hearts.add(
                FloatingHeart(
                    id = System.nanoTime(),
                    xOffsetFactor = Random.nextFloat(),
                    color = colors.random()
                )
            )
            if (hearts.size > 15) {
                hearts.removeAt(0)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        hearts.forEach { heart ->
            KeyedFloatingHeart(heart = heart) {
                hearts.remove(heart)
            }
        }
    }
}

@Composable
fun KeyedFloatingHeart(
    heart: FloatingHeart,
    onFinished: () -> Unit
) {
    val animatedY = remember { Animatable(1f) }
    val animatedAlpha = remember { Animatable(1f) }

    LaunchedEffect(heart.id) {
        animatedY.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
        )
        onFinished()
    }

    LaunchedEffect(heart.id) {
        animatedAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 2000, delayMillis = 500, easing = LinearEasing)
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val yPos = maxHeight * animatedY.value
        val xPos = maxWidth * heart.xOffsetFactor

        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = heart.color.copy(alpha = animatedAlpha.value),
            modifier = Modifier
                .offset(x = xPos - 16.dp, y = yPos)
                .size(32.dp)
        )
    }
}

// CameraX Local Camera Preview Surface
@Composable
fun CameraPreview(
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = if (isFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                } catch (e: Exception) {
                    // Camera binding fallback
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        update = { previewView ->
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val cameraSelector = if (isFrontCamera) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                    } catch (e: Exception) {
                        // ignore
                    }
                }, ContextCompat.getMainExecutor(context))
            } catch (e: Exception) {
                // ignore
            }
        },
        modifier = modifier
    )
}
