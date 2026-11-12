package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AddaBuzzRepository
import com.example.model.Agent
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class AgentMenuOption(val title: String, val emoji: String) {
    AGENT("Agent", "👥"),
    ADD_HOST("Add Host", "🎤"),
    INVITE_AGENT("Invite Agent", "➕"),
    COINS_TRADING("Coins Trading", "🪙"),
    AGENT_REWARDS("Agent Rewards", "💰"),
    AGENT_RANKING("Agent Ranking", "📈"),
    DIAMOND_AGENT("Diamond Agent", "💎"),
    SUPER_AGENT_ADMIN("Super Agent & Admin", "🏛️")
}

private val agentCompactOptions = listOf(
    AgentMenuOption.AGENT,
    AgentMenuOption.ADD_HOST,
    AgentMenuOption.INVITE_AGENT,
    AgentMenuOption.COINS_TRADING
)

private val agentWideOptions = listOf(
    AgentMenuOption.AGENT_REWARDS,
    AgentMenuOption.AGENT_RANKING,
    AgentMenuOption.DIAMOND_AGENT,
    AgentMenuOption.SUPER_AGENT_ADMIN
)

@Composable
fun AgentManagementSection(
    selected: AgentMenuOption?,
    onSelect: (AgentMenuOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Agent Management",
            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )
        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            agentCompactOptions.forEach { option ->
                AgentCompactTile(
                    option = option,
                    selected = selected == option,
                    onClick = { onSelect(option) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        agentWideOptions.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { option ->
                    AgentWideTile(
                        option = option,
                        selected = selected == option,
                        onClick = { onSelect(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AgentCompactTile(
    option: AgentMenuOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) VioletBright else SurfaceCardBorder,
                shape = RoundedCornerShape(20.dp)
            )
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
            style = Typography.labelSmall.copy(color = TextSecondary, fontSize = 12.sp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AgentWideTile(
    option: AgentMenuOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) VioletBright else SurfaceCardBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 20.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(PurplePrimary.copy(alpha = 0.45f), NeonPink.copy(alpha = 0.25f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = option.emoji, fontSize = 26.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = option.title,
            style = Typography.bodyMedium.copy(color = TextSecondary),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AgentRow(
    agent: Agent,
    rank: Int? = null,
    trailing: @Composable (() -> Unit)? = null
) {
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
            if (rank != null) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(SurfaceGlassHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$rank",
                        style = Typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(PurplePrimary.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = agent.name.take(1).uppercase(),
                    style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = agent.name,
                    style = Typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "@${agent.username} • ${agent.agentCode}",
                    style = Typography.labelSmall.copy(color = TextSecondary)
                )
                if (agent.status != "Active") {
                    Text(
                        text = agent.status,
                        style = Typography.labelSmall.copy(color = GoldCoin, fontWeight = FontWeight.Bold)
                    )
                }
            }
            if (trailing != null) {
                trailing()
            }
        }
    }
}

@Composable
fun AgentScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val agents by repository.agents.collectAsState()
    val user by repository.sessionManager.currentUser.collectAsState()

    MenuScaffold(title = "Agent Network", onBack = onBack) {
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Agents", style = Typography.labelSmall.copy(color = TextSecondary))
                    Text(
                        text = "${agents.size}",
                        style = Typography.headlineSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Your Agent Code", style = Typography.labelSmall.copy(color = TextSecondary))
                    Text(
                        text = (user?.id ?: "guest").takeLast(6).uppercase(),
                        style = Typography.headlineSmall.copy(color = GoldCoin, fontWeight = FontWeight.Bold)
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
            items(agents) { agent ->
                AgentRow(agent = agent) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${agent.earnings} 🪙",
                            style = Typography.titleSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${agent.referredUsers} referred • Lv.${agent.level}",
                            style = Typography.labelSmall.copy(color = TextSecondary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddHostScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val addedHosts by repository.addedHosts.collectAsState()
    val agents by repository.agents.collectAsState()
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    MenuScaffold(title = "Add Host", onBack = onBack, snackbarHostState = snackbarHostState) {
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Onboard a new host", style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Host name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    leadingIcon = { Text("@", color = TextSecondary, modifier = Modifier.padding(start = 12.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (repository.addHost(username, displayName)) {
                            message = "@${username.trim().removePrefix("@")} added as host"
                            username = ""
                            displayName = ""
                            scope.launch { snackbarHostState.showSnackbar("Host added to your network") }
                        } else {
                            message = "Enter a valid, unused username"
                            scope.launch { snackbarHostState.showSnackbar("Username is empty or already a host") }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Add host", style = Typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                }
                message?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, style = Typography.labelSmall.copy(color = TextSecondary))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Your hosts (${addedHosts.size})", style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(agents.filter { it.status == "Host" }) { agent ->
                AgentRow(agent = agent) {
                    Text(
                        text = agent.referralCode,
                        style = Typography.labelMedium.copy(color = GoldCoin, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun InviteAgentScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val user by repository.sessionManager.currentUser.collectAsState()
    val pending by repository.pendingAgentInvites.collectAsState()
    var username by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val referralCode = (user?.id ?: "guest").takeLast(6).uppercase()
    val referralLink = "https://addabuzz.live/agent/$referralCode"

    MenuScaffold(title = "Invite Agent", onBack = onBack, snackbarHostState = snackbarHostState) {
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
                Text("🛡️", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Your Referral Code", style = Typography.labelMedium.copy(color = TextSecondary))
                Text(
                    text = referralCode,
                    style = Typography.headlineMedium.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(referralLink, style = Typography.bodySmall.copy(color = TextSecondary))
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(referralLink))
                            scope.launch { snackbarHostState.showSnackbar("Referral link copied") }
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
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Join the AddaBuzz Live agent team with my code $referralCode — $referralLink"
                                )
                            }
                            context.startActivity(Intent.createChooser(intent, "Invite agent"))
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

        Text("Invite an agent", style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Agent username") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = SurfaceCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (repository.inviteAgent(username)) {
                        username = ""
                        scope.launch { snackbarHostState.showSnackbar("Invitation sent") }
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Invalid or duplicate username") }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Invite", style = Typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Pending invitations (${pending.size})", style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        if (pending.isEmpty()) {
            Text("No pending invitations yet.", style = Typography.bodyMedium.copy(color = TextSecondary))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(pending) { invite ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("@$invite", style = Typography.bodyLarge.copy(color = TextPrimary))
                            Surface(shape = RoundedCornerShape(8.dp), color = GoldCoin.copy(alpha = 0.2f)) {
                                Text(
                                    text = "Pending",
                                    style = Typography.labelSmall.copy(color = GoldCoin, fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CoinsTradingScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val user by repository.sessionManager.currentUser.collectAsState()
    val transactions by repository.transactions.collectAsState()
    var amount by remember { mutableStateOf("100") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val trades = transactions.filter { it.type == "TRADE" }

    MenuScaffold(title = "Coins Trading", onBack = onBack, snackbarHostState = snackbarHostState) {
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(GoldCoin, ElectricCyan)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🪙 → 💎 Live Exchange", style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                Text(
                    text = "100 coins = 8 diamonds. Diamonds are withdrawable, coins are not.",
                    style = Typography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() } },
                    label = { Text("Coins to trade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val coins = amount.toLongOrNull() ?: 0
                        val diamonds = repository.tradeCoinsForDiamonds(coins)
                        if (diamonds > 0) {
                            scope.launch { snackbarHostState.showSnackbar("Traded $coins coins for $diamonds diamonds") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Trade failed — check the amount and balance") }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldCoin),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Trade now",
                        style = Typography.titleSmall.copy(color = Color(0xFF070510), fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Balance: 🪙 ${user?.coins ?: 0}   •   💎 ${user?.diamonds ?: 0}",
                    style = Typography.labelMedium.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Recent trades", style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        if (trades.isEmpty()) {
            Text("No trades yet.", style = Typography.bodyMedium.copy(color = TextSecondary))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(trades) { tx ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(tx.title, style = Typography.bodyMedium.copy(color = TextPrimary))
                                Text(tx.date, style = Typography.labelSmall.copy(color = TextSecondary))
                            }
                            Text(tx.amount, style = Typography.titleSmall.copy(color = GoldCoin, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgentRewardsScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val claimed by repository.claimedRewards.collectAsState()
    val user by repository.sessionManager.currentUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val tasks = listOf(
        AgentRewardTask("agent_checkin", "Agent Check-in", "Daily agent sign-in", 100L, "📅"),
        AgentRewardTask("agent_refer", "Refer an Agent", "Invite a new agent to your team", 500L, "👥"),
        AgentRewardTask("agent_host", "Onboard a Host", "Add a host to your network", 750L, "🎤"),
        AgentRewardTask("agent_stream", "Host a Live Room", "Stream for 30 minutes", 1_000L, "📺"),
        AgentRewardTask("agent_trade", "Trade Coins", "Perform your first coin trade", 300L, "🪙")
    )

    MenuScaffold(title = "Agent Rewards", onBack = onBack, snackbarHostState = snackbarHostState) {
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(PurplePrimary, GoldCoin)))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Agent Earnings", style = Typography.labelSmall.copy(color = TextSecondary))
                    Text(
                        text = "🪙 ${user?.coins ?: 0}",
                        style = Typography.headlineSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Tasks claimed", style = Typography.labelSmall.copy(color = TextSecondary))
                    Text(
                        text = "${claimed.count { it.startsWith("agent_") }}/${tasks.size}",
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

private data class AgentRewardTask(
    val id: String,
    val title: String,
    val description: String,
    val coins: Long,
    val emoji: String
)

@Composable
fun AgentRankingScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val ranking = repository.agentRanking()

    MenuScaffold(title = "Agent Ranking", onBack = onBack) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Agents ranked by total earnings",
            style = Typography.bodyMedium.copy(color = TextSecondary)
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(ranking) { agent ->
                val rank = ranking.indexOf(agent) + 1
                AgentRow(agent = agent, rank = rank) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${agent.earnings} 🪙",
                            style = Typography.titleSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${agent.referredUsers} referrals",
                            style = Typography.labelSmall.copy(color = TextSecondary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiamondAgentScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val diamondAgents = repository.diamondAgents()

    MenuScaffold(title = "Diamond Agent", onBack = onBack) {
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(ElectricCyan, VioletBright)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💎 Diamond Agent Program", style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "Earn 50,000+ diamonds from your network",
                    "10,000+ coins traded per month",
                    "Maintain a 90% active host retention",
                    "Get the Diamond Agent badge + priority payouts"
                ).forEach { requirement ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("•", color = ElectricCyan, style = Typography.bodyMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(requirement, style = Typography.bodyMedium.copy(color = TextSecondary))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Top Diamond Agents", style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(diamondAgents) { agent ->
                AgentRow(agent = agent) {
                    Text(
                        text = "${agent.earnings * 8 / 100} 💎",
                        style = Typography.titleSmall.copy(color = GoldCoin, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun SuperAgentAdminScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val agents by repository.agents.collectAsState()
    val sessionManager = repository.sessionManager
    var programEnabled by remember { mutableStateOf(sessionManager.isAgentProgramEnabled()) }
    var autoPayout by remember { mutableStateOf(sessionManager.isAgentAutoPayoutEnabled()) }

    val leadership = agents.filter { it.isAdmin || it.isSuperAgent }

    MenuScaffold(title = "Super Agent & Admin", onBack = onBack) {
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(GoldCoin, NeonPink)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🏛️ Program Control", style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Agent Program", style = Typography.bodyLarge.copy(color = TextPrimary))
                        Text("Accept new agent registrations", style = Typography.labelSmall.copy(color = TextSecondary))
                    }
                    Switch(
                        checked = programEnabled,
                        onCheckedChange = {
                            programEnabled = it
                            sessionManager.setAgentProgramEnabled(it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan, checkedTrackColor = PurplePrimary)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Auto Payout", style = Typography.bodyLarge.copy(color = TextPrimary))
                        Text("Release agent earnings every Friday", style = Typography.labelSmall.copy(color = TextSecondary))
                    }
                    Switch(
                        checked = autoPayout,
                        onCheckedChange = {
                            autoPayout = it
                            sessionManager.setAgentAutoPayoutEnabled(it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan, checkedTrackColor = PurplePrimary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Leadership team", style = Typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(leadership) { agent ->
                AgentRow(agent = agent) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (agent.isAdmin) LiveRed.copy(alpha = 0.2f) else GoldCoin.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (agent.isAdmin) "Admin" else "Super Agent",
                            style = Typography.labelSmall.copy(
                                color = if (agent.isAdmin) LiveRed else GoldCoin,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
