package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AddaBuzzRepository
import com.example.model.LiveRoom
import com.example.model.RoomType
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Discover : Screen("discover")
    object Categories : Screen("categories")
    data class LiveRoomView(val room: LiveRoom, val isHost: Boolean) : Screen("live_room")
    data class MultiGuestRoomView(val room: LiveRoom, val isHost: Boolean) : Screen("multiguest_room")
    object Profile : Screen("profile")
    object Followers : Screen("followers")
    object Following : Screen("following")
    object Messages : Screen("messages")
    object Notifications : Screen("notifications")
    object Wallet : Screen("wallet")
    object GiftCatalog : Screen("gift_catalog")
    object Settings : Screen("settings")
    object Terminal : Screen("terminal")
    data class ProfileMenu(val option: ProfileMenuOption) : Screen("profile_menu")
    data class AgentMenu(val option: AgentMenuOption) : Screen("agent_menu")
}

enum class BottomTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    DISCOVER("Discover", Icons.Filled.Explore, Icons.Outlined.Explore),
    MESSAGES("Chat", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.PersonOutline)
}

@Composable
fun AddaBuzzApp(repository: AddaBuzzRepository) {
    val coroutineScope = rememberCoroutineScope()
    val currentUser by repository.sessionManager.currentUser.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    val backStack = remember { mutableStateListOf<Screen>() }
    var homeCategory by remember { mutableStateOf("All") }

    fun navigateTo(screen: Screen) {
        backStack.add(currentScreen)
        currentScreen = screen
    }

    fun popBack() {
        if (backStack.isNotEmpty()) {
            currentScreen = backStack.removeAt(backStack.size - 1)
        } else {
            currentScreen = Screen.Home
            selectedTab = BottomTab.HOME
        }
    }

    // Determine if bottom navigation should be visible
    val showBottomBar = currentScreen is Screen.Home ||
            currentScreen is Screen.Discover ||
            currentScreen is Screen.Messages ||
            currentScreen is Screen.Profile

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    color = SurfaceDark.copy(alpha = 0.95f),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = Brush.verticalGradient(listOf(SurfaceCardBorder, Color.Transparent))),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(64.dp)
                    ) {
                        BottomTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    selectedTab = tab
                                    backStack.clear()
                                    when (tab) {
                                        BottomTab.HOME -> currentScreen = Screen.Home
                                        BottomTab.DISCOVER -> currentScreen = Screen.Discover
                                        BottomTab.MESSAGES -> currentScreen = Screen.Messages
                                        BottomTab.PROFILE -> currentScreen = Screen.Profile
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) ElectricCyan else TextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        style = Typography.labelSmall.copy(
                                            color = if (isSelected) ElectricCyan else TextSecondary,
                                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = PurplePrimary.copy(alpha = 0.35f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            when (val screen = currentScreen) {
                is Screen.Splash -> {
                    SplashScreen(
                        onNavigateNext = {
                            currentScreen = if (currentUser != null) Screen.Home else Screen.Login
                        }
                    )
                }

                is Screen.Login -> {
                    LoginScreen(
                        repository = repository,
                        onLoginSuccess = {
                            backStack.clear()
                            currentScreen = Screen.Home
                            selectedTab = BottomTab.HOME
                        },
                        onNavigateRegister = {
                            navigateTo(Screen.Register)
                        }
                    )
                }

                is Screen.Register -> {
                    BackHandler { popBack() }
                    RegisterScreen(
                        repository = repository,
                        onRegisterSuccess = {
                            backStack.clear()
                            currentScreen = Screen.Home
                            selectedTab = BottomTab.HOME
                        },
                        onNavigateLogin = {
                            popBack()
                        }
                    )
                }

                is Screen.Home -> {
                    HomeScreen(
                        repository = repository,
                        onJoinRoom = { room ->
                            coroutineScope.launch {
                                repository.joinRoom(room, asHost = false)
                                if (room.roomType == RoomType.MULTI_GUEST || room.roomType == RoomType.AUDIO) {
                                    navigateTo(Screen.MultiGuestRoomView(room, isHost = false))
                                } else {
                                    navigateTo(Screen.LiveRoomView(room, isHost = false))
                                }
                            }
                        },
                        onStartLive = { type, title, category ->
                            coroutineScope.launch {
                                val room = repository.createLiveRoom(title, category, type)
                                repository.joinRoom(room, asHost = true)
                                if (type == RoomType.MULTI_GUEST || type == RoomType.AUDIO) {
                                    navigateTo(Screen.MultiGuestRoomView(room, isHost = true))
                                } else {
                                    navigateTo(Screen.LiveRoomView(room, isHost = true))
                                }
                            }
                        },
                        onWalletClick = { navigateTo(Screen.Wallet) },
                        onNotificationsClick = { navigateTo(Screen.Notifications) },
                        onSearchClick = {
                            selectedTab = BottomTab.DISCOVER
                            currentScreen = Screen.Discover
                        },
                        onNavigateCategories = { navigateTo(Screen.Categories) },
                        selectedCategory = homeCategory,
                        onSelectCategory = { homeCategory = it }
                    )
                }

                is Screen.Discover -> {
                    DiscoverScreen(
                        repository = repository,
                        onJoinRoom = { room ->
                            coroutineScope.launch {
                                repository.joinRoom(room, asHost = false)
                                if (room.roomType == RoomType.MULTI_GUEST || room.roomType == RoomType.AUDIO) {
                                    navigateTo(Screen.MultiGuestRoomView(room, isHost = false))
                                } else {
                                    navigateTo(Screen.LiveRoomView(room, isHost = false))
                                }
                            }
                        }
                    )
                }

                is Screen.Categories -> {
                    BackHandler { popBack() }
                    LiveCategoriesScreen(
                        repository = repository,
                        onSelectCategory = { cat ->
                            homeCategory = cat
                            backStack.clear()
                            selectedTab = BottomTab.HOME
                            currentScreen = Screen.Home
                        },
                        onBack = { popBack() }
                    )
                }

                is Screen.LiveRoomView -> {
                    LiveRoomScreen(
                        room = screen.room,
                        isHost = screen.isHost,
                        repository = repository,
                        onLeaveRoom = { popBack() },
                        onNavigateWallet = { navigateTo(Screen.Wallet) }
                    )
                }

                is Screen.MultiGuestRoomView -> {
                    MultiGuestRoomScreen(
                        room = screen.room,
                        isHost = screen.isHost,
                        repository = repository,
                        onLeaveRoom = { popBack() },
                        onNavigateWallet = { navigateTo(Screen.Wallet) }
                    )
                }

                is Screen.Profile -> {
                    UserProfileScreen(
                        repository = repository,
                        onNavigateFollowers = { navigateTo(Screen.Followers) },
                        onNavigateFollowing = { navigateTo(Screen.Following) },
                        onNavigateWallet = { navigateTo(Screen.Wallet) },
                        onNavigateSettings = { navigateTo(Screen.Settings) },
                        onNavigateMenu = { navigateTo(Screen.ProfileMenu(it)) },
                        onNavigateAgentMenu = { navigateTo(Screen.AgentMenu(it)) }
                    )
                }

                is Screen.AgentMenu -> {
                    BackHandler { popBack() }
                    val option = screen.option
                    when (option) {
                        AgentMenuOption.AGENT -> AgentScreen(repository = repository, onBack = { popBack() })
                        AgentMenuOption.ADD_HOST -> AddHostScreen(repository = repository, onBack = { popBack() })
                        AgentMenuOption.INVITE_AGENT -> InviteAgentScreen(repository = repository, onBack = { popBack() })
                        AgentMenuOption.COINS_TRADING -> CoinsTradingScreen(repository = repository, onBack = { popBack() })
                        AgentMenuOption.AGENT_REWARDS -> AgentRewardsScreen(repository = repository, onBack = { popBack() })
                        AgentMenuOption.AGENT_RANKING -> AgentRankingScreen(repository = repository, onBack = { popBack() })
                        AgentMenuOption.DIAMOND_AGENT -> DiamondAgentScreen(repository = repository, onBack = { popBack() })
                        AgentMenuOption.SUPER_AGENT_ADMIN -> SuperAgentAdminScreen(repository = repository, onBack = { popBack() })
                    }
                }

                is Screen.ProfileMenu -> {
                    BackHandler { popBack() }
                    val option = screen.option
                    when (option) {
                        ProfileMenuOption.REWARD -> RewardScreen(repository = repository, onBack = { popBack() })
                        ProfileMenuOption.RANK -> RankScreen(repository = repository, onBack = { popBack() })
                        ProfileMenuOption.GAMES -> GamesScreen(repository = repository, onBack = { popBack() })
                        ProfileMenuOption.STORE -> StoreScreen(repository = repository, onBack = { popBack() })
                        ProfileMenuOption.INVITE -> InviteScreen(repository = repository, onBack = { popBack() })
                        ProfileMenuOption.HONOR_LEVEL -> HonorLevelScreen(repository = repository, onBack = { popBack() })
                        ProfileMenuOption.FAN_CLUB -> FanClubScreen(repository = repository, onBack = { popBack() })
                        ProfileMenuOption.MEDAL_WALL -> MedalWallScreen(repository = repository, onBack = { popBack() })
                    }
                }

                is Screen.Followers -> {
                    BackHandler { popBack() }
                    FollowersScreen(
                        repository = repository,
                        onBack = { popBack() }
                    )
                }

                is Screen.Following -> {
                    BackHandler { popBack() }
                    FollowingScreen(
                        repository = repository,
                        onJoinStream = { creatorId ->
                            val room = repository.allRooms.value.firstOrNull { it.host.id == creatorId }
                            if (room != null) {
                                coroutineScope.launch {
                                    repository.joinRoom(room, asHost = false)
                                    if (room.roomType == RoomType.MULTI_GUEST || room.roomType == RoomType.AUDIO) {
                                        navigateTo(Screen.MultiGuestRoomView(room, isHost = false))
                                    } else {
                                        navigateTo(Screen.LiveRoomView(room, isHost = false))
                                    }
                                }
                            }
                        },
                        onBack = { popBack() }
                    )
                }

                is Screen.Messages -> {
                    PrivateMessagesScreen(
                        repository = repository
                    )
                }

                is Screen.Notifications -> {
                    BackHandler { popBack() }
                    NotificationsScreen(
                        repository = repository,
                        onBack = { popBack() }
                    )
                }

                is Screen.Wallet -> {
                    BackHandler { popBack() }
                    WalletScreen(
                        repository = repository,
                        onBack = { popBack() }
                    )
                }

                is Screen.GiftCatalog -> {
                    BackHandler { popBack() }
                    GiftCatalogScreen(
                        repository = repository,
                        onBack = { popBack() }
                    )
                }

                is Screen.Settings -> {
                    BackHandler { popBack() }
                    SettingsScreen(
                        repository = repository,
                        onLogout = {
                            currentScreen = Screen.Login
                            backStack.clear()
                        },
                        onOpenTerminal = { navigateTo(Screen.Terminal) },
                        onBack = { popBack() }
                    )
                }

                is Screen.Terminal -> {
                    BackHandler { popBack() }
                    TerminalScreen(
                        repository = repository,
                        onBack = { popBack() }
                    )
                }
            }
        }
    }
}
