package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ForgeViewModel
import com.example.ui.theme.ForgeCarbon
import com.example.ui.theme.ForgeFire
import com.example.ui.theme.ForgeGreen
import com.example.ui.theme.ForgeSteel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: ForgeViewModel
) {
    val workouts by viewModel.workouts.collectAsState()
    val nutritionLogs by viewModel.nutrition.collectAsState()
    val waterLogs by viewModel.waterLogs.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    // 1. Calculations
    val totalWorkouts = workouts.size
    val totalDuration = workouts.sumOf { it.durationMinutes }
    val totalCalories = nutritionLogs.sumOf { it.calories }
    val totalWaterMl = waterLogs.sumOf { it.amountMl }

    // Categories Distribution for Canvas Chart
    val categoryCounts = remember(workouts) {
        val counts = mutableMapOf("Strength" to 0f, "Cardio" to 0f, "Yoga" to 0f, "Core" to 0f)
        workouts.forEach {
            val cat = it.category
            if (counts.containsKey(cat)) {
                counts[cat] = (counts[cat] ?: 0f) + 1f
            } else {
                counts["Strength"] = (counts["Strength"] ?: 0f) + 1f // fallback
            }
        }
        counts
    }

    val totalCategorized = categoryCounts.values.sum()

    val strengthColor = ForgeFire
    val cardioColor = ForgeGreen
    val yogaColor = Color.Cyan
    val coreColor = Color.Yellow

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Stats",
                            tint = ForgeFire,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PERFORMANCE ANALYTICS",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = ForgeFire
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // METRIC GRID CARDS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Workouts count
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("WORKOUTS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalWorkouts", fontSize = 26.sp, fontWeight = FontWeight.Black, color = ForgeFire)
                        Text("completed", fontSize = 10.sp, color = Color.LightGray)
                    }
                }

                // Card 2: Workout Duration
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("ACTIVE MINUTES", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalDuration", fontSize = 26.sp, fontWeight = FontWeight.Black, color = ForgeGreen)
                        Text("total minutes", fontSize = 10.sp, color = Color.LightGray)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 3: Calories Consumed
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("ENERGY LOGGED", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalCalories", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Yellow)
                        Text("kcal input", fontSize = 10.sp, color = Color.LightGray)
                    }
                }

                // Card 4: Water consumed
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("HYDRATION INTAKE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalWaterMl", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Cyan)
                        Text("ml total", fontSize = 10.sp, color = Color.LightGray)
                    }
                }
            }

            // DYNAMIC CATEGORY DONUT CHART (Canvas)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "WORKOUT FOCUS DISTRIBUTION",
                        color = ForgeFire,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (totalCategorized == 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No workouts recorded yet to chart focus split.",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // Donut Chart Canvas drawing
                            Canvas(modifier = Modifier.size(120.dp)) {
                                val strokeWidth = 24.dp.toPx()
                                val sizeDouble = size.width
                                val radius = (sizeDouble - strokeWidth) / 2f
                                val centerOffset = Offset(sizeDouble / 2f, sizeDouble / 2f)

                                val strengthAngle = (categoryCounts["Strength"] ?: 0f) / totalCategorized * 360f
                                val cardioAngle = (categoryCounts["Cardio"] ?: 0f) / totalCategorized * 360f
                                val yogaAngle = (categoryCounts["Yoga"] ?: 0f) / totalCategorized * 360f
                                val coreAngle = (categoryCounts["Core"] ?: 0f) / totalCategorized * 360f

                                var startAngle = -90f

                                if (strengthAngle > 0f) {
                                    drawArc(
                                        color = strengthColor,
                                        startAngle = startAngle,
                                        sweepAngle = strengthAngle,
                                        useCenter = false,
                                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                                        size = Size(radius * 2, radius * 2),
                                        style = Stroke(width = strokeWidth)
                                    )
                                    startAngle += strengthAngle
                                }

                                if (cardioAngle > 0f) {
                                    drawArc(
                                        color = cardioColor,
                                        startAngle = startAngle,
                                        sweepAngle = cardioAngle,
                                        useCenter = false,
                                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                                        size = Size(radius * 2, radius * 2),
                                        style = Stroke(width = strokeWidth)
                                    )
                                    startAngle += cardioAngle
                                }

                                if (yogaAngle > 0f) {
                                    drawArc(
                                        color = yogaColor,
                                        startAngle = startAngle,
                                        sweepAngle = yogaAngle,
                                        useCenter = false,
                                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                                        size = Size(radius * 2, radius * 2),
                                        style = Stroke(width = strokeWidth)
                                    )
                                    startAngle += yogaAngle
                                }

                                if (coreAngle > 0f) {
                                    drawArc(
                                        color = coreColor,
                                        startAngle = startAngle,
                                        sweepAngle = coreAngle,
                                        useCenter = false,
                                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                                        size = Size(radius * 2, radius * 2),
                                        style = Stroke(width = strokeWidth)
                                    )
                                }
                            }

                            // Donut Chart Legends
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                LegendItem("Strength", categoryCounts["Strength"]?.toInt() ?: 0, strengthColor)
                                LegendItem("Cardio", categoryCounts["Cardio"]?.toInt() ?: 0, cardioColor)
                                LegendItem("Yoga", categoryCounts["Yoga"]?.toInt() ?: 0, yogaColor)
                                LegendItem("Core", categoryCounts["Core"]?.toInt() ?: 0, coreColor)
                            }
                        }
                    }
                }
            }

            // ATHLETIC HEALTH FEEDBACK CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ForgeFire)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COACH'S DAILY FEEDBACK",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val coachFeedback = when {
                        totalWorkouts == 0 -> "You haven't logged any workouts yet. A true master is forged through routine effort. Open the logger and submit your initial set!"
                        totalCalories == 0 -> "Water and fuel tracking is empty today. Consuming high-density protein and maintaining cell hydration are critical blocks of physical growth."
                        totalWaterMl < 1500 -> "Your water intake is sub-optimal. Aim for at least 3000ml to optimize recovery, joint lubrication, and cognitive discipline."
                        else -> "Stellar discipline, Athlete! You are fueling correctly and keeping physical activity high. Keep burning in the forge!"
                    }

                    Text(
                        text = coachFeedback,
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LegendItem(
    label: String,
    count: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = "$label ($count logs)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
