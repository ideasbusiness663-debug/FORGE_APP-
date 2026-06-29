package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WorkoutLog
import com.example.ui.ForgeViewModel
import com.example.ui.theme.ForgeCarbon
import com.example.ui.theme.ForgeFire
import com.example.ui.theme.ForgeGreen
import com.example.ui.theme.ForgeSteel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWorkoutScreen(
    viewModel: ForgeViewModel
) {
    val workoutsList by viewModel.workouts.collectAsState()
    val focusManager = LocalFocusManager.current

    // Form States
    var exerciseName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Strength") }
    var sets by remember { mutableStateOf(3) }
    var reps by remember { mutableStateOf(10) }
    var weightText by remember { mutableStateOf("60") }
    var durationMinutes by remember { mutableStateOf(45) }

    val categories = listOf("Strength", "Cardio", "Yoga", "Core")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Log",
                            tint = ForgeFire,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "LOG WORKOUTS",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // LOGGER SECTION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "FORGE A NEW ATTEMPT",
                            color = ForgeFire,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = exerciseName,
                            onValueChange = { exerciseName = it },
                            label = { Text("Exercise Name") },
                            placeholder = { Text("e.g., Bench Press, Deadlift, Running") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("exercise_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Tabs
                        Text(
                            "Category",
                            style = MaterialTheme.typography.bodySmall,
                            color = ForgeFire,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ForgeFire else ForgeSteel)
                                        .clickable { selectedCategory = cat }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White else Color.LightGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sets / Reps Stepper Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Sets Stepper
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Sets",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(ForgeSteel, RoundedCornerShape(12.dp))
                                        .padding(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (sets > 1) sets-- },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White)
                                    }
                                    Text(
                                        text = "$sets",
                                        modifier = Modifier.weight(1f),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    IconButton(
                                        onClick = { sets++ },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }

                            // Reps Stepper
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Reps",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(ForgeSteel, RoundedCornerShape(12.dp))
                                        .padding(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (reps > 1) reps-- },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White)
                                    }
                                    Text(
                                        text = "$reps",
                                        modifier = Modifier.weight(1f),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    IconButton(
                                        onClick = { reps++ },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Weight Input & Duration Slider Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Weight Input
                            OutlinedTextField(
                                value = weightText,
                                onValueChange = { weightText = it },
                                label = { Text("Weight (kg)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("workout_weight_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Duration selection
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Duration", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Text("$durationMinutes min", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForgeGreen)
                                }
                                Slider(
                                    value = durationMinutes.toFloat(),
                                    onValueChange = { durationMinutes = it.toInt() },
                                    valueRange = 5f..180f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = ForgeFire,
                                        activeTrackColor = ForgeFire,
                                        inactiveTrackColor = ForgeSteel
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Log Button
                        Button(
                            onClick = {
                                val weight = weightText.toFloatOrNull() ?: 0f
                                if (exerciseName.isNotBlank()) {
                                    viewModel.logWorkout(
                                        name = exerciseName.trim(),
                                        category = selectedCategory,
                                        sets = sets,
                                        reps = reps,
                                        weight = weight,
                                        duration = durationMinutes
                                    )
                                    // Reset inputs
                                    exerciseName = ""
                                    focusManager.clearFocus()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("log_workout_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ForgeFire),
                            shape = RoundedCornerShape(12.dp),
                            enabled = exerciseName.isNotBlank()
                        ) {
                            Icon(Icons.Default.AddTask, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("INSERT LOG INTO THE FORGE", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // HISTORY SECTION HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = ForgeFire)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "YOUR PHYSICAL TIMELINE",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            // HISTORY ITEMS LIST
            if (workoutsList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = ForgeSteel, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No physical workouts recorded yet.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            } else {
                items(workoutsList, key = { it.id }) { log ->
                    WorkoutHistoryRow(
                        log = log,
                        onDelete = { viewModel.deleteWorkout(log.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutHistoryRow(
    log: WorkoutLog,
    onDelete: () -> Unit
) {
    val dateString = DateFormat.format("MMM dd, yyyy - hh:mm a", Date(log.timestamp)).toString()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("workout_log_item_${log.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Indicator Dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (log.category.lowercase()) {
                            "strength" -> ForgeFire
                            "cardio" -> ForgeGreen
                            "yoga" -> Color.Cyan
                            else -> Color.Yellow
                        }
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = log.exerciseName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${log.category})",
                        color = Color.LightGray.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${log.sets} sets x ${log.reps} reps @ ${log.weightKg} kg  |  ${log.durationMinutes} min",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateString,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Log",
                    tint = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
