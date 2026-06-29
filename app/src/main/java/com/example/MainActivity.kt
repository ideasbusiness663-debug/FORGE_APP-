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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.ForgeViewModel
import com.example.ui.ForgeViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ForgeCarbon
import com.example.ui.theme.ForgeFire
import com.example.ui.theme.ForgeSteel

class MainActivity : ComponentActivity() {
    private val viewModel: ForgeViewModel by viewModels {
        ForgeViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainNavigation(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainNavigation(viewModel: ForgeViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("splash") {
            SplashScreen(
                viewModel = viewModel,
                onNavigateToSetup = {
                    navController.navigate("profile_setup") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate("main_hub") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("profile_setup") {
            ProfileScreen(
                viewModel = viewModel,
                onSetupComplete = {
                    navController.navigate("main_hub") {
                        popUpTo("profile_setup") { inclusive = true }
                    }
                }
            )
        }

        composable("main_hub") {
            MainHub(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHub(viewModel: ForgeViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        TabItem("Feed", Icons.Default.Groups, "feed_tab"),
        TabItem("Train", Icons.Default.FitnessCenter, "train_tab"),
        TabItem("Fuel", Icons.Default.LocalFireDepartment, "fuel_tab"),
        TabItem("AI Core", Icons.Default.SmartToy, "ai_tab"),
        TabItem("Stats", Icons.Default.Analytics, "stats_tab"),
        TabItem("Profile", Icons.Default.Person, "profile_tab")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF5EBE8), // Sleek Nav Background (#F5EBE8)
                tonalElevation = 8.dp,
                modifier = Modifier
                    .height(80.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (isSelected) Color(0xFF201A18) else Color(0xFF735C54),
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF201A18) else Color(0xFF735C54)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFFFDBD1) // Peach pill indicator (#FFDBD1)
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> FeedScreen(
                    viewModel = viewModel,
                    onNavigateToLog = { selectedTab = 1 } // Redirect tab to Train
                )
                1 -> LogWorkoutScreen(viewModel = viewModel)
                2 -> NutritionScreen(viewModel = viewModel)
                3 -> AICoreTabScreen(viewModel = viewModel, onNavigateToProfile = { selectedTab = 5 })
                4 -> StatsScreen(viewModel = viewModel)
                5 -> ProfileScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AICoreTabScreen(
    viewModel: ForgeViewModel,
    onNavigateToProfile: () -> Unit
) {
    var selectedSubTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Simple Top Tab Bar for AICore
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = Color(0xFFF5EBE8), // Sleek Nav Background (#F5EBE8)
            contentColor = Color(0xFF8E4D34),    // Terracotta indicator color (#8E4D34)
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = Color(0xFF8E4D34)
                )
            }
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { 
                    Text(
                        text = "COACH CHAT", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 12.sp,
                        color = if (selectedSubTab == 0) Color(0xFF201A18) else Color(0xFF735C54)
                    ) 
                }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { 
                    Text(
                        text = "BODY ANALYZER", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 12.sp,
                        color = if (selectedSubTab == 1) Color(0xFF201A18) else Color(0xFF735C54)
                    ) 
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            if (selectedSubTab == 0) {
                ChatScreen(viewModel = viewModel)
            } else {
                BodyAnalysisScreen(viewModel = viewModel, onNavigateToProfile = onNavigateToProfile)
            }
        }
    }
}

data class TabItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
