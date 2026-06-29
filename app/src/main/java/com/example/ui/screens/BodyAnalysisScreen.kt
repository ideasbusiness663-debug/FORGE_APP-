package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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
fun BodyAnalysisScreen(
    viewModel: ForgeViewModel,
    onNavigateToProfile: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisResult by viewModel.bodyAnalysisResult.collectAsState()

    var extraDetails by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Troubleshoot,
                            contentDescription = "Analysis",
                            tint = ForgeFire,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AI BODY SCAN",
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
                .padding(16.dp)
        ) {
            if (profile == null) {
                // If profile is missing, guide them to configure it first.
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = ForgeCarbon),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ForgeFire,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "PROFILE DETECTED AS VACANT",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "We need your height, weight, gender, age, and goals to process an accurate body scan. Setup your parameters first.",
                            fontSize = 13.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateToProfile,
                            colors = ButtonDefaults.buttonColors(containerColor = ForgeFire),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CONFIGURE INITIAL PROFILE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Main Analysis Screen
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ForgeCarbon),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CURRENT MEASUREMENT METRICS",
                            color = ForgeFire,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Brief display of core profile info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("WEIGHT", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("${profile?.weightKg} kg", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("HEIGHT", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("${profile?.heightCm} cm", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("AGE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("${profile?.age} yrs", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("GOAL", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(profile?.fitnessGoal?.split(" ")?.firstOrNull() ?: "Build", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ForgeGreen)
                            }
                        }
                    }
                }

                if (analysisResult == null) {
                    // FORM INPUT
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ForgeCarbon),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ADD EXTRA SCAN DETAILS",
                                color = ForgeFire,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Text(
                                text = "Provide extra insights to improve scan fidelity (e.g., \"Body fat roughly 18%\", \"stiff knees\", \"wanting to prioritize abdominal cuts\").",
                                fontSize = 12.sp,
                                color = Color.LightGray.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            OutlinedTextField(
                                value = extraDetails,
                                onValueChange = { extraDetails = it },
                                placeholder = { Text("e.g. Muscle fatigue, energy levels are high, eating clean...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .testTag("body_extra_input"),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            if (isAnalyzing) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = ForgeFire, strokeWidth = 3.dp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "ARCHITECT IS ANALYZING ANTHROPOMETRICS...",
                                        color = ForgeFire,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.runBodyAnalysis(extraDetails) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("analyze_body_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = ForgeFire),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.QueryStats, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("INITIATE COMPREHENSIVE AI SCAN", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // DISPLAY COMPLETED ANALYSIS RESULT
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ForgeCarbon),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI SCAN ASSESSMENT SUMMARY",
                                    color = ForgeFire,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )

                                IconButton(
                                    onClick = { viewModel.clearBodyAnalysis() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Re-analyze", tint = ForgeGreen)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Custom Parsed Text Output (Simulating Markdown display)
                            val cleanLines = analysisResult?.split("\n") ?: emptyList()
                            cleanLines.forEach { line ->
                                when {
                                    line.startsWith("#") -> {
                                        val headerText = line.replace("#", "").trim()
                                        Text(
                                            text = headerText,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ForgeFire,
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                        )
                                    }
                                    line.startsWith("-") || line.startsWith("*") -> {
                                        val itemText = line.substring(1).trim()
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text("• ", color = ForgeGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(
                                                text = itemText,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                    line.isNotBlank() -> {
                                        Text(
                                            text = line,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 13.sp,
                                            lineHeight = 19.sp,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
