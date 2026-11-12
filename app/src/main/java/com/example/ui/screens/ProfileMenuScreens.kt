package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AddaBuzzRepository
import com.example.model.GameResult
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class ProfileMenuOption(val title: String, val emoji: String) {
    REWARD("Reward", "🎁"),
    RANK("Rank", "🏆"),
    GAMES("Games", "🎲"),
    STORE("Store", "🛍️"),
    INVITE("Invite", "👥"),
    HONOR_LEVEL("Honor Level", "🛡️"),
    FAN_CLUB("Fan Club", "💖"),
    MEDAL_WALL("Medal Wall", "🎖️")
}

@Composable
fun ProfileOptionsGrid(
    onSelect: (ProfileMenuOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProfileMenuOption.entries.toList().chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { option ->
                    ProfileOptionTile(
                        option = option,
                        onClick = { onSelect(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ProfileOptionTile(
    option: ProfileMenuOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(PurplePrimary.copy(alpha = 0.45f), NeonPink.copy(alpha = 0.25f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = option.emoji, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = option.title,
            style = Typography.labelSmall.copy(
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuScaffold(
    title: String,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = {
            snackbarHostState?.let { SnackbarHost(it) }
        },
        topBar = {
            TopAppBar(
                title = { Text(title, style = Typography.titleLarge.copy(color = TextPrimary)) },
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
                .padding(horizontal = 16.dp),
            content = content
        )
    }
}

@Composable
fun RewardScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val claimed by repository.claimedRewards.collectAsState()
    val user by repository.sessionManager.currentUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val tasks = listOf(
        RewardTask("daily_checkin", "Daily Check-in", "Sign in once every day", 50L, "📅"),
        RewardTask("watch_stream", "Watch a Live Room", "Stay in a live room for 10 minutes", 100L, "📺"),
        RewardTask("send_gift", "Send a Gift", "Send any gift to a streamer", 150L, "🎁"),
        RewardTask("follow_creator", "Follow a Creator", "Follow one new creator", 200L, "👤"),
        RewardTask("invite_friend", "Invite a Friend", "Share your invite link", 500L, "📨")
    )

    MenuScaffold(title = "Reward Tasks", onBack = onBack, snackbarHostState = snackbarHostState) {
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(PurplePrimary, ElectricCyan)))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Your Coin Balance", style = Typography.labelSmall.copy(color = TextSecondary))
                    Text(
                        text = "🪙 ${user?.coins ?: 0}",
                        style = Typography.headlineSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Tasks claimed", style = Typography.labelSmall.copy(color = TextSecondary))
                    Text(
                        text = "${claimed.size}/${tasks.size}",
                        style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(tasks) { task ->
                val isClaimed = claimed.contains(task.id)
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
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PurplePrimary.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(task.emoji, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = Typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = task.description,
                                style = Typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isClaimed) {
                            Surface(shape = RoundedCornerShape(10.dp), color = EmeraldGreen.copy(alpha = 0.2f)) {
                                Text(
                                    text = "✓ Claimed",
                                    style = Typography.labelSmall.copy(color = EmeraldGreen, fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (repository.claimReward(task.id, task.coins)) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("+${task.coins} coins added to your wallet")
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "+${task.coins} 🪙",
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

private data class RewardTask(
    val id: String,
    val title: String,
    val description: String,
    val coins: Long,
    val emoji: String
)

@Composable
fun RankScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val currentUser by repository.sessionManager.currentUser.collectAsState()
    val ranked = remember(repository.allRooms.value, repository.followingUsers.value, repository.followersUsers.value) {
        (repository.allRooms.value.map { it.host } +
            repository.followingUsers.value +
            repository.followersUsers.value).distinctBy { it.id }
            .sortedByDescending { it.followersCount }
    }

    MenuScaffold(title = "Creator Ranking", onBack = onBack) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Top creators this week",
            style = Typography.bodyMedium.copy(color = TextSecondary)
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(ranked) { creator ->
                val rank = ranked.indexOf(creator) + 1
                val isMe = creator.id == currentUser?.id
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = if (isMe) CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(ElectricCyan, NeonPink))
                    ) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when (rank) {
                                        1 -> GoldCoin.copy(alpha = 0.25f)
                                        2 -> TextSecondary.copy(alpha = 0.25f)
                                        3 -> NeonPink.copy(alpha = 0.25f)
                                        else -> SurfaceGlassHigh
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (rank <= 3) listOf("🥇", "🥈", "🥉")[rank - 1] else "$rank",
                                style = Typography.labelMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        AsyncImage(
                            model = creator.avatarUrl,
                            contentDescription = creator.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .border(
                                    1.5.dp,
                                    if (creator.isLive) LiveRed else PurplePrimary,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isMe) "${creator.displayName} (You)" else creator.displayName,
                                style = Typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "@${creator.username} • Lv.${creator.level}",
                                style = Typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${creator.followersCount}",
                                style = Typography.titleSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "followers",
                                style = Typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GamesScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val user by repository.sessionManager.currentUser.collectAsState()
    var wager by remember { mutableStateOf(10L) }
    var pick by remember { mutableStateOf(1) }
    var lastResult by remember { mutableStateOf<GameResult?>(null) }

    MenuScaffold(title = "Dice Duel", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(PurplePrimary, NeonPink)))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "🎲 Dice Duel",
                    style = Typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Pick a number from 1 to 6. If the dice lands on it you win double your wager.",
                    style = Typography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text("Wager", style = Typography.labelMedium.copy(color = TextSecondary))
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10L, 50L, 100L).forEach { amount ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (wager == amount) PurplePrimary else SurfaceGlassHigh,
                            modifier = Modifier.clickable { wager = amount }
                        ) {
                            Text(
                                text = "$amount 🪙",
                                style = Typography.labelMedium.copy(
                                    color = if (wager == amount) Color.White else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Your pick", style = Typography.labelMedium.copy(color = TextSecondary))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..6).forEach { number ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (pick == number) ElectricCyan else SurfaceGlassHigh)
                                .clickable { pick = number },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$number",
                                style = Typography.titleMedium.copy(
                                    color = if (pick == number) Color(0xFF070510) else TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val result = repository.playDiceGame(wager, pick)
                        if (result != null) {
                            lastResult = result
                        }
                    },
                    enabled = (user?.coins ?: 0) >= wager,
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Roll the dice", style = Typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Last roll", style = Typography.labelMedium.copy(color = TextSecondary))
                Spacer(modifier = Modifier.height(6.dp))
                val result = lastResult
                if (result == null) {
                    Text("No roll yet — place your first wager.", style = Typography.bodyMedium.copy(color = TextSecondary))
                } else {
                    Text(
                        text = "Dice: ${result.roll}   •   Your pick: ${result.pick}",
                        style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (result.won) "🎉 You won ${result.payout} coins!" else "😔 No luck this time. Wager refunded as 0.",
                        style = Typography.bodyMedium.copy(
                            color = if (result.won) EmeraldGreen else LiveRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Balance: 🪙 ${user?.coins ?: 0}",
                    style = Typography.labelMedium.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun StoreScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val user by repository.sessionManager.currentUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    MenuScaffold(title = "Store", onBack = onBack, snackbarHostState = snackbarHostState) {
        Spacer(modifier = Modifier.height(12.dp))

        Text("Coin Top-Up", style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(repository.rechargePackages) { pkg ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = if (pkg.isHot) CardDefaults.outlinedCardBorder().copy(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(listOf(NeonPink, ElectricCyan))
                    ) else null
                ) {
                    Column(
                        modifier = Modifier
                            .width(132.dp)
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🪙 ${pkg.coins}", style = Typography.titleMedium.copy(color = ElectricCyan, fontWeight = FontWeight.Bold))
                        if (pkg.bonusCoins > 0) {
                            Text("+${pkg.bonusCoins} bonus", style = Typography.labelSmall.copy(color = GoldCoin))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                repository.rechargeCoins(pkg)
                                scope.launch {
                                    snackbarHostState.showSnackbar("${pkg.coins + pkg.bonusCoins} coins added")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("$${pkg.priceUsd}", style = Typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Gift Store", style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
            Text("🪙 ${user?.coins ?: 0}", style = Typography.titleSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(repository.availableGifts) { gift ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(gift.iconEmoji, fontSize = 34.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = gift.name,
                            style = Typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${gift.coinPrice} 🪙",
                            style = Typography.labelSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                if (repository.buyGift(gift)) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("${gift.name} added to your inventory")
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Not enough coins")
                                    }
                                }
                            },
                            enabled = (user?.coins ?: 0) >= gift.coinPrice,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Buy", style = Typography.labelSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InviteScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val user by repository.sessionManager.currentUser.collectAsState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val inviteCode = (user?.id ?: "guest").takeLast(6).uppercase()
    val inviteLink = "https://addabuzz.live/invite/$inviteCode"

    MenuScaffold(title = "Invite Friends", onBack = onBack, snackbarHostState = snackbarHostState) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(ElectricCyan, PurplePrimary)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("👥", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your Invite Code",
                    style = Typography.labelMedium.copy(color = TextSecondary)
                )
                Text(
                    text = inviteCode,
                    style = Typography.headlineMedium.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = inviteLink,
                    style = Typography.bodySmall.copy(color = TextSecondary),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(inviteLink))
                            scope.launch { snackbarHostState.showSnackbar("Invite link copied") }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy", style = Typography.labelMedium.copy(color = TextPrimary))
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Join me on AddaBuzz Live! Use my code $inviteCode — $inviteLink")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share invite"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", style = Typography.labelMedium.copy(color = Color.White))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Invite Rewards", style = Typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "Invite 1 friend → 500 coins",
                    "Invite 5 friends → 3,000 coins + Exclusive badge",
                    "Invite 20 friends → 15,000 coins + Fan Club for a month"
                ).forEach { reward ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎁", fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(reward, style = Typography.bodyMedium.copy(color = TextSecondary))
                    }
                }
            }
        }
    }
}

@Composable
fun HonorLevelScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val user by repository.sessionManager.currentUser.collectAsState()
    val level = user?.level ?: 1
    val currentExp = level * 120
    val nextLevel = level + 1
    val nextExp = nextLevel * 120
    val progress = (currentExp.toFloat() / nextExp.toFloat()).coerceIn(0f, 1f)

    MenuScaffold(title = "Honor Level", onBack = onBack) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(GoldCoin, NeonPink)))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Level", style = Typography.labelSmall.copy(color = TextSecondary))
                        Text(
                            text = "Level $level",
                            style = Typography.headlineMedium.copy(color = GoldCoin, fontWeight = FontWeight.Bold)
                        )
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = PurplePrimary.copy(alpha = 0.3f)) {
                        Text(
                            text = user?.badge ?: "VIP Host",
                            style = Typography.labelMedium.copy(color = ElectricCyan, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    color = ElectricCyan,
                    trackColor = SurfaceGlassHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$currentExp / $nextExp EXP to reach Level $nextLevel",
                    style = Typography.labelSmall.copy(color = TextSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Level Benefits", style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        val benefits = listOf(
            "Level 5 — Unlock 10% extra gift coin earnings" to (level >= 5),
            "Level 10 — Exclusive chat badge & priority room entry" to (level >= 10),
            "Level 15 — Unlock multi-guest host tools" to (level >= 15),
            "Level 20 — 20% coin back on every recharge" to (level >= 20),
            "Level 25 — Golden Crown profile frame" to (level >= 25)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(benefits) { (text, unlocked) ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (unlocked) "✅" else "🔒",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = text,
                            style = Typography.bodyMedium.copy(
                                color = if (unlocked) TextPrimary else TextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FanClubScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val user by repository.sessionManager.currentUser.collectAsState()
    val memberships by repository.fanClubMemberships.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val creators = remember(repository.allRooms.value, repository.followingUsers.value) {
        (repository.allRooms.value.map { it.host } + repository.followingUsers.value).distinctBy { it.id }
    }
    val joinCost = 500L

    MenuScaffold(title = "Fan Clubs", onBack = onBack, snackbarHostState = snackbarHostState) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Join a creator's fan club for ${joinCost} coins and unlock exclusive perks.",
            style = Typography.bodyMedium.copy(color = TextSecondary)
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(creators) { creator ->
                val joined = memberships.contains(creator.id)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = if (joined) CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(NeonPink, ElectricCyan))
                    ) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = creator.avatarUrl,
                            contentDescription = creator.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = creator.displayName,
                                style = Typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "@${creator.username} • ${creator.followersCount / 3} members",
                                style = Typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                        if (joined) {
                            OutlinedButton(
                                onClick = { repository.leaveFanClub(creator.id) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Leave", style = Typography.labelSmall.copy(color = TextSecondary))
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (repository.joinFanClub(creator.id, joinCost)) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Joined ${creator.displayName}'s fan club")
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Not enough coins")
                                        }
                                    }
                                },
                                enabled = (user?.coins ?: 0) >= joinCost,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("${joinCost} 🪙", style = Typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedalWallScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val user by repository.sessionManager.currentUser.collectAsState()
    val transactions by repository.transactions.collectAsState()

    val medals = listOf(
        Medal("🌱", "First Steps", "Joined AddaBuzz Live", true),
        Medal("👑", "VIP Host", "VIP badge unlocked", user?.isVip == true),
        Medal("🔥", "Rising Star", "Reach level 10", (user?.level ?: 0) >= 10),
        Medal("💎", "Top Giver", "Hold 1,000 coins", (user?.coins ?: 0) >= 1000),
        Medal("👥", "Social Butterfly", "Get 100 followers", (user?.followersCount ?: 0) >= 100),
        Medal("🎲", "Gamer", "Play a game in the app", transactions.any { it.type == "GAME" }),
        Medal("🎬", "Streamer", "Start your own live room", repository.allRooms.value.any { it.host.id == user?.id }),
        Medal("🏆", "Legend", "Reach level 25", (user?.level ?: 0) >= 25)
    )
    val earned = medals.count { it.earned }

    MenuScaffold(title = "Medal Wall", onBack = onBack) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "$earned of ${medals.size} medals earned",
            style = Typography.bodyMedium.copy(color = TextSecondary)
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(medals) { medal ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = if (medal.earned) CardDefaults.outlinedCardBorder().copy(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(GoldCoin, NeonPink))
                    ) else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (medal.earned) GoldCoin.copy(alpha = 0.2f) else SurfaceGlassHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (medal.earned) medal.emoji else "🔒",
                                fontSize = 26.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = medal.name,
                            style = Typography.titleSmall.copy(
                                color = if (medal.earned) TextPrimary else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = medal.description,
                            style = Typography.labelSmall.copy(color = TextSecondary),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private data class Medal(
    val emoji: String,
    val name: String,
    val description: String,
    val earned: Boolean
)
