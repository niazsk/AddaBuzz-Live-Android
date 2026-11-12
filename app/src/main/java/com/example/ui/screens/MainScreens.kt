package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.model.LiveRoom
import com.example.model.RoomType
import com.example.model.User
import com.example.ui.components.AddaBuzzTopBar
import com.example.ui.components.LiveBadge
import com.example.ui.components.LiveStreamCard
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    repository: AddaBuzzRepository,
    onJoinRoom: (LiveRoom) -> Unit,
    onStartLive: (roomType: RoomType, title: String, category: String) -> Unit,
    onWalletClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNavigateCategories: () -> Unit
) {
    val currentUser by repository.sessionManager.currentUser.collectAsState()
    val allRooms by repository.allRooms.collectAsState()
    val notifications by repository.notifications.collectAsState()
    val unreadNotifs = remember(notifications) { notifications.count { !it.isRead } }

    var selectedCategory by remember { mutableStateOf("All") }
    var showStartLiveDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Music & Adda", "Audio Party", "Gaming Arena", "Multi-Guest", "Education & Tech")

    val filteredRooms = remember(allRooms, selectedCategory) {
        if (selectedCategory == "All") allRooms
        else allRooms.filter { it.category.contains(selectedCategory, ignoreCase = true) }
    }

    val featuredRoom = allRooms.firstOrNull()

    Scaffold(
        topBar = {
            AddaBuzzTopBar(
                coins = currentUser?.coins ?: 0L,
                unreadNotifications = unreadNotifs,
                onWalletClick = onWalletClick,
                onNotificationsClick = onNotificationsClick,
                onSearchClick = onSearchClick
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showStartLiveDialog = true },
                containerColor = PurplePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(ElectricCyan, NeonPink)),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Icon(Icons.Default.Videocam, contentDescription = "Go Live")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Go Live",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Featured Hero Stream Banner
            if (featuredRoom != null) {
                item {
                    FeaturedHeroBanner(
                        room = featuredRoom,
                        onJoin = { onJoinRoom(featuredRoom) }
                    )
                }
            }

            // Categories horizontal bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Streams",
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = "Explore All >",
                        style = Typography.labelMedium.copy(color = ElectricCyan, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.clickable { onNavigateCategories() }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) PurplePrimary else SurfaceCard,
                            border = if (!isSelected) CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(PurplePrimary.copy(alpha = 0.3f), ElectricCyan.copy(alpha = 0.3f)))) else null,
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                style = Typography.labelLarge.copy(
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Grid / List of live streams
            items(filteredRooms.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { room ->
                        Box(modifier = Modifier.weight(1f)) {
                            LiveStreamCard(
                                room = room,
                                onClick = { onJoinRoom(room) }
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // Modal to start a Live stream
    if (showStartLiveDialog) {
        StartLiveDialog(
            onDismiss = { showStartLiveDialog = false },
            onConfirm = { type, title, cat ->
                showStartLiveDialog = false
                onStartLive(type, title, cat)
            }
        )
    }
}

@Composable
fun FeaturedHeroBanner(
    room: LiveRoom,
    onJoin: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(210.dp)
            .clickable(onClick = onJoin)
            .border(
                1.5.dp,
                Brush.horizontalGradient(listOf(PurpleAccent, ElectricCyan)),
                RoundedCornerShape(20.dp)
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = room.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Deep gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0x99070510), Color(0xF2070510))
                        )
                    )
            )

            // Top Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PurplePrimary.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "⭐ FEATURED STREAM",
                        style = Typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                LiveBadge()
            }

            // Bottom Info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = room.host.avatarUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, ElectricCyan, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = room.title,
                            style = Typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${room.host.displayName} • ${room.viewerCount} Viewers",
                            style = Typography.bodyMedium.copy(color = ElectricCyan)
                        )
                    }
                    Button(
                        onClick = onJoin,
                        colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Watch", style = Typography.labelLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
fun StartLiveDialog(
    onDismiss: () -> Unit,
    onConfirm: (RoomType, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(RoomType.VIDEO) }
    var category by remember { mutableStateOf("Music & Adda") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Start Live Broadcast 🎙️",
                style = Typography.headlineMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Stream Title") },
                    placeholder = { Text("e.g. Late night acoustic adda 🎸") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Room Type",
                    style = Typography.labelLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf(
                        Triple(RoomType.VIDEO, "📹 Video", "Camera live"),
                        Triple(RoomType.AUDIO, "🎙️ Audio", "Voice party"),
                        Triple(RoomType.MULTI_GUEST, "👥 8-Seat", "Multi-Guest")
                    )

                    types.forEach { (type, label, _) ->
                        val isSelected = selectedType == type
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PurplePrimary else SurfaceCard,
                            border = if (!isSelected) CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(PurplePrimary, ElectricCyan))) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = type }
                        ) {
                            Text(
                                text = label,
                                style = Typography.labelSmall.copy(
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedType, title, category)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start Now", style = Typography.labelLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = Typography.labelLarge.copy(color = TextSecondary))
            }
        }
    )
}

@Composable
fun DiscoverScreen(
    repository: AddaBuzzRepository,
    onJoinRoom: (LiveRoom) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allRooms by repository.allRooms.collectAsState()
    val following by repository.followingUsers.collectAsState()

    val hashtags = listOf("#AddaParty", "#BanglaBeats", "#LateNightTalk", "#GamingZone", "#SingWithMe", "#TechAdda")

    val searchResults = remember(allRooms, searchQuery) {
        if (searchQuery.isBlank()) allRooms
        else allRooms.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.host.displayName.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Discover",
                    style = Typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search streamers, rooms or tags...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricCyan) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Trending Hashtags
            item {
                Text(
                    text = "Trending Topics",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    hashtags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceGlassHigh,
                            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(PurplePrimary, ElectricCyan))),
                            modifier = Modifier.clickable { searchQuery = tag.removePrefix("#") }
                        ) {
                            Text(
                                text = tag,
                                style = Typography.labelMedium.copy(color = ElectricCyan, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Top Rising Streamers
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Popular Streamers",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    following.forEach { creator ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                AsyncImage(
                                    model = creator.avatarUrl,
                                    contentDescription = creator.displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, PurplePrimary, CircleShape)
                                )
                                if (creator.isLive) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .offset(y = 4.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(LiveRed)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "LIVE",
                                            style = Typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = creator.displayName,
                                style = Typography.labelSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Live Streams Results
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = if (searchQuery.isBlank()) "Recommended Streams" else "Search Results",
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(searchResults) { room ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    LiveStreamCard(room = room, onClick = { onJoinRoom(room) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveCategoriesScreen(
    repository: AddaBuzzRepository,
    onSelectCategory: (String) -> Unit,
    onBack: () -> Unit
) {
    val categoryList = listOf(
        Pair("Chit-Chat (Adda)", "🎙️ 1.2k Streams"),
        Pair("Music & Acoustic", "🎸 890 Streams"),
        Pair("Gaming Arena", "🎮 3.4k Streams"),
        Pair("8-Guest Audio Party", "👥 650 Streams"),
        Pair("PK Battles", "⚔️ 420 Streams"),
        Pair("Talents & Comedy", "🎭 510 Streams"),
        Pair("Late Night Talk", "🌙 380 Streams"),
        Pair("Education & Tech", "💻 230 Streams")
    )

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Live Categories", style = Typography.titleLarge.copy(color = TextPrimary)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categoryList) { (title, count) ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(PurplePrimary, ElectricCyan))),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clickable { onSelectCategory(title) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title,
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = count,
                            style = Typography.labelSmall.copy(color = ElectricCyan, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }
}
