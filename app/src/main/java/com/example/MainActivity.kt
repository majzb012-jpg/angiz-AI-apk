package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.admin.AdminDashboardScreen
import com.example.features.auth.AuthScreen
import com.example.features.chat.ChatScreen
import com.example.features.home.HomeScreen
import com.example.features.library.LibraryScreen
import com.example.features.onboarding.OnboardingScreen
import com.example.features.profile.ProfileScreen
import com.example.features.splash.SplashScreen
import com.example.features.tools.ToolsHubScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.AnjezPrimary
import com.example.ui.theme.MyApplicationTheme

enum class AppDestination {
    SPLASH,
    ONBOARDING,
    AUTH,
    MAIN_APP,
    ADMIN_DASHBOARD
}

enum class MainTab(val titleAr: String, val icon: ImageVector) {
    HOME("الرئيسية", Icons.Default.Home),
    CHAT("المحادثات", Icons.Default.Chat),
    TOOLS("الأدوات", Icons.Default.Build),
    LIBRARY("المكتبة", Icons.Default.Folder),
    PROFILE("حسابي", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            MyApplicationTheme(darkTheme = isDarkMode) {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: MainViewModel) {
    var destination by remember { mutableStateOf(AppDestination.SPLASH) }
    var currentTab by remember { mutableStateOf(MainTab.HOME) }

    val hasSeenOnboarding by viewModel.hasSeenOnboarding.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissStatusMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (destination) {
            AppDestination.SPLASH -> {
                SplashScreen(
                    onFinish = {
                        destination = if (!hasSeenOnboarding) {
                            AppDestination.ONBOARDING
                        } else if (currentUser == null) {
                            AppDestination.AUTH
                        } else {
                            AppDestination.MAIN_APP
                        }
                    }
                )
            }
            AppDestination.ONBOARDING -> {
                OnboardingScreen(
                    onCompleted = {
                        viewModel.completeOnboarding()
                        destination = if (currentUser == null) AppDestination.AUTH else AppDestination.MAIN_APP
                    }
                )
            }
            AppDestination.AUTH -> {
                AuthScreen(
                    viewModel = viewModel,
                    onSuccess = { destination = AppDestination.MAIN_APP }
                )
            }
            AppDestination.ADMIN_DASHBOARD -> {
                AdminDashboardScreen(
                    viewModel = viewModel,
                    onBack = { destination = AppDestination.MAIN_APP }
                )
            }
            AppDestination.MAIN_APP -> {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            MainTab.values().forEach { tab ->
                                val isSelected = currentTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.titleAr
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.titleAr,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AnjezPrimary,
                                        selectedTextColor = AnjezPrimary,
                                        indicatorColor = AnjezPrimary.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            MainTab.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToChat = { currentTab = MainTab.CHAT },
                                onNavigateToTools = { currentTab = MainTab.TOOLS }
                            )
                            MainTab.CHAT -> ChatScreen(
                                viewModel = viewModel,
                                onBack = { currentTab = MainTab.HOME }
                            )
                            MainTab.TOOLS -> ToolsHubScreen(
                                viewModel = viewModel,
                                onNavigateToChat = { currentTab = MainTab.CHAT }
                            )
                            MainTab.LIBRARY -> LibraryScreen(
                                viewModel = viewModel,
                                onOpenChat = { currentTab = MainTab.CHAT }
                            )
                            MainTab.PROFILE -> ProfileScreen(
                                viewModel = viewModel,
                                onNavigateToAdmin = { destination = AppDestination.ADMIN_DASHBOARD },
                                onLogout = {
                                    viewModel.userRepo.logout()
                                    destination = AppDestination.AUTH
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Preserve Greeting function for screenshot tests and template compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
