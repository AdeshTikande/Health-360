package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HealthViewModel
import com.example.ui.HealthViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Core HealthViewModel with standard Constructor factory
        val viewModel: HealthViewModel by viewModels {
            HealthViewModelFactory(application)
        }

        setContent {
            MyApplicationTheme {
                val profileState by viewModel.userProfile.collectAsStateWithLifecycle()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Crossfade(targetState = profileState, label = "AppTransition") { profile ->
                        if (profile == null) {
                            // First-time user onboarding
                            OnboardingScreen(
                                viewModel = viewModel,
                                onComplete = {
                                    // Trigger refresh dashboard if helpful
                                }
                            )
                        } else {
                            // Main Application Dashboard Container
                            HomeScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HealthViewModel) {
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val isPremium = profileState?.isPremium ?: false

    // Bottom Navigation tab item list
    val navItems = listOf(
        NavigationTabItem("Dashboard", Icons.Default.Dashboard, "dashboard_tab"),
        NavigationTabItem("Diet plan", Icons.Default.Restaurant, "diet_tab"),
        NavigationTabItem("Fitness", Icons.Default.FitnessCenter, "fitness_tab"),
        NavigationTabItem("Sleep", Icons.Default.Bedtime, "sleep_tab"),
        NavigationTabItem("AI Chat", Icons.Default.Chat, "chat_tab"),
        NavigationTabItem("Profile", Icons.Default.Person, "profile_tab")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (selectedIndex) {
                                0 -> "Health360 Dashboard"
                                1 -> "AI Indian Diet Plan"
                                2 -> "Fitness Tracker"
                                3 -> "Sleep Logging"
                                4 -> "Coach Consultations"
                                else -> "Health360 Parameters"
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = DarkBlue
                        )
                        if (isPremium) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(StreakOrangeRed)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("PRO", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    if (selectedIndex != 5) {
                        IconButton(
                            onClick = { selectedIndex = 5 },
                            modifier = Modifier.testTag("app_bar_profile_shortcut")
                        ) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                navItems.forEachIndexed { index, tabItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        label = { Text(tabItem.title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = tabItem.icon, contentDescription = tabItem.title) },
                        modifier = Modifier.testTag(tabItem.testTag),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = PrimaryBlue,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = PrimaryBlue
                        )
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (selectedIndex) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { selectedIndex = 4 },
                    onNavigateToReport = { selectedIndex = 5 },
                    onNavigateToProfile = { selectedIndex = 5 }
                )
                1 -> DietScreen(viewModel = viewModel)
                2 -> FitnessScreen(viewModel = viewModel)
                3 -> SleepScreen(viewModel = viewModel)
                4 -> ChatScreen(viewModel = viewModel)
                5 -> ProfileScreen(viewModel = viewModel)
            }
        }
    }
}

data class NavigationTabItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
