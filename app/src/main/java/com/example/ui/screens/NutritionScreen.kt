package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
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
import com.example.data.NutritionLog
import com.example.ui.ForgeViewModel
import com.example.ui.theme.ForgeCarbon
import com.example.ui.theme.ForgeFire
import com.example.ui.theme.ForgeGreen
import com.example.ui.theme.ForgeSteel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    viewModel: ForgeViewModel
) {
    val profile by viewModel.userProfile.collectAsState()
    val nutritionLogs by viewModel.nutrition.collectAsState()
    val waterLogs by viewModel.waterLogs.collectAsState()
    val focusManager = LocalFocusManager.current

    // Form States
    var foodName by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf("Breakfast") }
    var caloriesText by remember { mutableStateOf("") }
    var proteinText by remember { mutableStateOf("") }
    var carbsText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }

    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    // Computations
    val calorieBudget = profile?.dailyCalorieTarget ?: 2500
    val totalCaloriesConsumed = nutritionLogs.sumOf { it.calories }
    val remainingCalories = calorieBudget - totalCaloriesConsumed

    val totalProtein = nutritionLogs.sumOf { it.proteinGrams.toDouble() }.toFloat()
    val totalCarbs = nutritionLogs.sumOf { it.carbsGrams.toDouble() }.toFloat()
    val totalFat = nutritionLogs.sumOf { it.fatGrams.toDouble() }.toFloat()

    // Targets (simple standard percentages based on calorie budget)
    val targetProtein = (calorieBudget * 0.3f / 4f) // 30% of energy from protein (4 kcal/g)
    val targetCarbs = (calorieBudget * 0.45f / 4f) // 45% of energy from carbs (4 kcal/g)
    val targetFat = (calorieBudget * 0.25f / 9f) // 25% of energy from fat (9 kcal/g)

    val totalWaterMl = waterLogs.sumOf { it.amountMl }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Nutrition",
                            tint = ForgeFire,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "METRIC & FUEL LOG",
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
            // CALORIE COUNTER HUD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "FUEL HUD",
                            color = ForgeFire,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$totalCaloriesConsumed",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ForgeGreen
                                )
                                Text("kcal consumed", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }

                            // Circular visual center or divider
                            Box(
                                modifier = Modifier
                                    .size(1.dp, 40.dp)
                                    .background(ForgeSteel)
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$remainingCalories",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (remainingCalories >= 0) Color.White else ForgeFire
                                )
                                Text(
                                    text = if (remainingCalories >= 0) "kcal remaining" else "kcal over budget",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Calorie Progress bar
                        val progressFraction = if (calorieBudget > 0) (totalCaloriesConsumed.toFloat() / calorieBudget).coerceIn(0f, 1f) else 0f
                        LinearProgressIndicator(
                            progress = progressFraction,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (remainingCalories >= 0) ForgeGreen else ForgeFire,
                            trackColor = ForgeSteel
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Macros Row: Protein, Carbs, Fats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroBar("PROTEIN", totalProtein, targetProtein, ForgeFire, Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(16.dp))
                            MacroBar("CARBS", totalCarbs, targetCarbs, Color.Yellow, Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(16.dp))
                            MacroBar("FAT", totalFat, targetFat, Color.Cyan, Modifier.weight(1f))
                        }
                    }
                }
            }

            // WATER LOGGER HUD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "HYDRATION CORE",
                                    color = ForgeFire,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$totalWaterMl / 3000 ml",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Cyan
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = Color.Cyan,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Water log buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(250, 500, 750).forEach { amount ->
                                OutlinedButton(
                                    onClick = { viewModel.logWater(amount) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("water_${amount}_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Cyan),
                                    border = ButtonDefaults.outlinedButtonBorder.let { ButtonDefaults.outlinedButtonColors(contentColor = Color.Cyan).let { BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.5f)) } }
                                ) {
                                    Text("+$amount ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // MEAL ADDER FORM
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "LOG A MEAL / SNACK",
                            color = ForgeFire,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = foodName,
                            onValueChange = { foodName = it },
                            label = { Text("Food / Meal Name") },
                            placeholder = { Text("e.g., Protein Shake, Oats, Salmon & Rice") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("food_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Meal Type Selection Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            mealTypes.forEach { type ->
                                val isSelected = selectedMealType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ForgeFire else ForgeSteel)
                                        .clickable { selectedMealType = type }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color.White else Color.LightGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Macros entry row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = caloriesText,
                                onValueChange = { caloriesText = it },
                                label = { Text("kcal") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("kcal_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = proteinText,
                                onValueChange = { proteinText = it },
                                label = { Text("P (g)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("protein_input_field"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = carbsText,
                                onValueChange = { carbsText = it },
                                label = { Text("C (g)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("carbs_input_field"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = fatText,
                                onValueChange = { fatText = it },
                                label = { Text("F (g)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("fat_input_field"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val cals = caloriesText.toIntOrNull() ?: 0
                                val prot = proteinText.toFloatOrNull() ?: 0f
                                val carbs = carbsText.toFloatOrNull() ?: 0f
                                val fat = fatText.toFloatOrNull() ?: 0f

                                if (foodName.isNotBlank() && cals > 0) {
                                    viewModel.logNutrition(
                                        mealType = selectedMealType,
                                        foodName = foodName.trim(),
                                        calories = cals,
                                        protein = prot,
                                        carbs = carbs,
                                        fat = fat
                                    )
                                    // Reset inputs
                                    foodName = ""
                                    caloriesText = ""
                                    proteinText = ""
                                    carbsText = ""
                                    fatText = ""
                                    focusManager.clearFocus()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("log_meal_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ForgeFire),
                            shape = RoundedCornerShape(12.dp),
                            enabled = foodName.isNotBlank() && caloriesText.isNotBlank()
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SAVE MEAL TO TRACKER", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // RECENT NUTRITION LOGS LIST
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.NoFood, contentDescription = null, tint = ForgeFire)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RECENT INTAKE LOG",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            if (nutritionLogs.isEmpty()) {
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
                            Text(
                                "No nutritional items recorded today.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                items(nutritionLogs, key = { it.id }) { log ->
                    MealRow(
                        log = log,
                        onDelete = { viewModel.deleteNutrition(log.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun MacroBar(
    name: String,
    current: Float,
    target: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) (current / target).coerceIn(0f, 1f) else 0f
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
            Text("${current.toInt()}g / ${target.toInt()}g", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = ForgeSteel
        )
    }
}

@Composable
fun MealRow(
    log: NutritionLog,
    onDelete: () -> Unit
) {
    val dateString = DateFormat.format("hh:mm a", Date(log.timestamp)).toString()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nutrition_log_item_${log.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = log.foodName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "[${log.mealType}]",
                        color = ForgeGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${log.calories} kcal  |  P: ${log.proteinGrams.toInt()}g  C: ${log.carbsGrams.toInt()}g  F: ${log.fatGrams.toInt()}g",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateString,
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Log",
                    tint = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
