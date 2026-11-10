package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AddaBuzzRepository
import com.example.model.Gift
import com.example.model.RechargePackage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    val currentUser by repository.sessionManager.currentUser.collectAsState()
    val transactions by repository.transactions.collectAsState()

    var showRechargeSuccessDialog by remember { mutableStateOf<RechargePackage?>(null) }
    var showExchangeDialog by remember { mutableStateOf(false) }
    var beansToExchange by remember { mutableStateOf("1000") }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("AddaBuzz Wallet", style = Typography.titleLarge.copy(color = TextPrimary)) },
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
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Balance Cards (Diamonds & Beans)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Diamonds Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(PurplePrimary, ElectricCyan))),
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Diamonds", style = Typography.labelMedium.copy(color = TextSecondary))
                                Text("💎", fontSize = 18.sp)
                            }
                            Text(
                                text = "${currentUser?.diamonds ?: 0}",
                                style = Typography.headlineLarge.copy(
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text("Use for live gifts", style = Typography.labelSmall.copy(color = TextSecondary))
                        }
                    }

                    // Beans Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(GoldCoin, NeonPink))),
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Beans Earned", style = Typography.labelMedium.copy(color = TextSecondary))
                                Text("🫘", fontSize = 18.sp)
                            }
                            Text(
                                text = "${currentUser?.beans ?: 0}",
                                style = Typography.headlineLarge.copy(
                                    color = GoldCoin,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Exchange to 💎",
                                style = Typography.labelSmall.copy(color = NeonPink, fontWeight = FontWeight.Bold),
                                modifier = Modifier.clickable { showExchangeDialog = true }
                            )
                        }
                    }
                }
            }

            // Quick Exchange Beans Button
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { showExchangeDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(PurplePrimary, GoldCoin))
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🫘 Convert Beans into Diamonds / Rewards",
                        style = Typography.labelLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Diamond Recharge Packages Header
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Top-Up Diamonds",
                    style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Recharge Grid
            items(repository.rechargePackages.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { pkg ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = if (pkg.isHot) CardDefaults.outlinedCardBorder().copy(width = 1.5.dp, brush = Brush.horizontalGradient(listOf(NeonPink, ElectricCyan))) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    repository.rechargeDiamonds(pkg)
                                    showRechargeSuccessDialog = pkg
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (pkg.isHot) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(LivePulseGradient)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("🔥 POPULAR", style = Typography.labelSmall.copy(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                                Text("💎 ${pkg.diamonds}", style = Typography.titleLarge.copy(color = ElectricCyan, fontWeight = FontWeight.Bold))
                                if (pkg.bonusDiamonds > 0) {
                                    Text("+${pkg.bonusDiamonds} Bonus", style = Typography.labelSmall.copy(color = GoldCoin, fontWeight = FontWeight.Bold))
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        repository.rechargeDiamonds(pkg)
                                        showRechargeSuccessDialog = pkg
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("$${pkg.priceUsd}", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                                }
                            }
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Transactions History
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "Transaction History",
                    style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(transactions) { tx ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = tx.title, style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                            Text(text = tx.date, style = Typography.labelSmall.copy(color = TextSecondary))
                        }
                        Text(
                            text = tx.amount,
                            style = Typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (tx.isCredit) EmeraldGreen else LiveRed
                            )
                        )
                    }
                }
            }
        }
    }

    // Recharge Success Confirmation Dialog
    showRechargeSuccessDialog?.let { pkg ->
        AlertDialog(
            onDismissRequest = { showRechargeSuccessDialog = null },
            containerColor = SurfaceDark,
            title = {
                Text("Recharge Successful! 🎉", style = Typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
            },
            text = {
                Text(
                    text = "Added ${pkg.diamonds + pkg.bonusDiamonds} Diamonds to your AddaBuzz wallet! Enjoy gifting streamers.",
                    style = Typography.bodyMedium.copy(color = TextSecondary)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showRechargeSuccessDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Exchange Beans Dialog
    if (showExchangeDialog) {
        AlertDialog(
            onDismissRequest = { showExchangeDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text("Exchange Beans to 💎", style = Typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
            },
            text = {
                Column {
                    Text(
                        text = "Current Beans: ${currentUser?.beans ?: 0}\nExchange Rate: 2 Beans = 1 Diamond 💎",
                        style = Typography.bodyMedium.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = beansToExchange,
                        onValueChange = { beansToExchange = it.filter { char -> char.isDigit() } },
                        label = { Text("Beans to convert") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = beansToExchange.toLongOrNull() ?: 0L
                        repository.exchangeBeansToDiamonds(amount)
                        showExchangeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Exchange Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExchangeDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

// Standalone Gift Catalog Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftCatalogScreen(
    repository: AddaBuzzRepository,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Gift Catalog & Animations", style = Typography.titleLarge.copy(color = TextPrimary)) },
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
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(repository.availableGifts) { gift ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.horizontalGradient(listOf(PurplePrimary, ElectricCyan)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = gift.iconEmoji, fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = gift.name,
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = gift.description,
                            style = Typography.labelSmall.copy(color = TextSecondary),
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PurplePrimary.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = "💎 ${gift.diamondPrice}",
                                style = Typography.labelLarge.copy(color = ElectricCyan, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Settings Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: AddaBuzzRepository,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val sessionManager = repository.sessionManager
    var serverUrl by remember { mutableStateOf(sessionManager.getServerUrl()) }
    var resolution by remember { mutableStateOf(sessionManager.getStreamResolution()) }
    var allowNotifications by remember { mutableStateOf(true) }
    var privateMode by remember { mutableStateOf(false) }
    var showUrlSavedToast by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = Typography.titleLarge.copy(color = TextPrimary)) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Backend Integration Section
            Text(
                text = "Backend Server Integration",
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ElectricCyan)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("Shared Web & Mobile Backend URL") },
                supportingText = { Text("Default: Cloud Run AddaBuzz Backend") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = SurfaceCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    sessionManager.setServerUrl(serverUrl)
                    showUrlSavedToast = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Server URL", color = Color.White)
            }

            if (showUrlSavedToast) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Server URL updated successfully!", color = EmeraldGreen, style = Typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Streaming Configuration
            Text(
                text = "Live Streaming Quality",
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ElectricCyan)
            )
            Spacer(modifier = Modifier.height(10.dp))

            val resolutions = listOf("720p (HD 30fps)", "1080p (Full HD)", "480p (Data Saver)")
            resolutions.forEach { res ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            resolution = res
                            sessionManager.setStreamResolution(res)
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(res, style = Typography.bodyLarge.copy(color = TextPrimary))
                    RadioButton(
                        selected = resolution == res,
                        onClick = {
                            resolution = res
                            sessionManager.setStreamResolution(res)
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = ElectricCyan)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy & Preferences
            Text(
                text = "Preferences",
                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ElectricCyan)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Gift & Live Notifications", style = Typography.bodyLarge.copy(color = TextPrimary))
                Switch(
                    checked = allowNotifications,
                    onCheckedChange = { allowNotifications = it },
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
                Text("Anonymous Viewer Mode", style = Typography.bodyLarge.copy(color = TextPrimary))
                Switch(
                    checked = privateMode,
                    onCheckedChange = { privateMode = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan, checkedTrackColor = PurplePrimary)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Info
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AddaBuzz Live",
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = "Connect • Stream • Chat • Enjoy",
                        style = Typography.bodyMedium.copy(color = ElectricCyan)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Native Android Client v1.0.0 (Official Build)",
                        style = Typography.labelSmall.copy(color = TextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logout Button
            Button(
                onClick = {
                    sessionManager.clearSession()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", style = Typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
            }
        }
    }
}
