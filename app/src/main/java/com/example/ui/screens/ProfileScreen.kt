package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import android.graphics.Bitmap
import android.graphics.Paint
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.ProgressPhoto
import com.example.ui.ForgeViewModel
import com.example.ui.theme.ForgeCarbon
import com.example.ui.theme.ForgeFire
import com.example.ui.theme.ForgeGreen
import com.example.ui.theme.ForgeSteel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ForgeViewModel,
    onSetupComplete: () -> Unit = {}
) {
    val profile by viewModel.userProfile.collectAsState()
    val progressPhotos by viewModel.progressPhotos.collectAsState(initial = emptyList())
    val context = LocalContext.current

    var showCameraDialog by remember { mutableStateOf(false) }
    var selectedPhotoDetail by remember { mutableStateOf<ProgressPhoto?>(null) }

    val focusManager = LocalFocusManager.current

    // Local form states
    var name by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var heightText by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var fitnessGoal by remember { mutableStateOf("Build Muscle & Strength") }
    var calorieTargetText by remember { mutableStateOf("2500") }
    var bio by remember { mutableStateOf("") }

    var isEditMode by remember { mutableStateOf(false) }

    // Sync state with db profile once loaded
    LaunchedEffect(profile) {
        profile?.let {
            name = it.name
            weightText = it.weightKg.toString()
            heightText = it.heightCm.toString()
            ageText = it.age.toString()
            gender = it.gender
            fitnessGoal = it.fitnessGoal
            calorieTargetText = it.dailyCalorieTarget.toString()
            bio = it.bio
            isEditMode = false
        } ?: run {
            isEditMode = true // If no profile exists, open directly in edit/setup mode
        }
    }

    val goalsList = listOf(
        "Build Muscle & Strength",
        "Fat Loss & Conditioning",
        "Endurance & Marathon Prep",
        "Functional Fitness & Yoga",
        "Powerlifting Peak"
    )

    var showGoalMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (profile == null) "FORGE YOUR PROFILE" else "ATHLETE PROFILE",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
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
            if (!isEditMode && profile != null) {
                // VIEW MODE - Sleek, futuristic dashboard design for profile
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Athlete Avatar Placeholder
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(45.dp))
                                .background(ForgeSteel),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(2).uppercase(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForgeFire
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = fitnessGoal,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        if (bio.isNotEmpty()) {
                            Text(
                                text = "\"$bio\"",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Divider(color = ForgeSteel, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(20.dp))

                        // Stats Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("WEIGHT", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("$weightText kg", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = ForgeFire)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("HEIGHT", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("$heightText cm", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = ForgeFire)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("AGE", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("$ageText yrs", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = ForgeFire)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("GENDER", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(gender, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("DAILY CALORIE TARGET", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("$calorieTargetText kcal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ForgeGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { isEditMode = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("edit_profile_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ForgeSteel),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = ForgeFire)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EDIT ATTEMPTS / TARGETS", color = ForgeFire, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- WEEKLY PROGRESS GALLERY CARD ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = ForgeFire,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "WEEKLY PROGRESS GALLERY",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            
                            IconButton(
                                onClick = { showCameraDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ForgeSteel, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Capture progress photo",
                                    tint = ForgeFire,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = ForgeSteel, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        if (progressPhotos.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(ForgeSteel, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No progress photos recorded yet",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = "Dedication requires validation. Capture and store your weekly progress photos to visualize your transformation!",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showCameraDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ForgeFire),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("CAPTURE WEEKLY PROGRESS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        } else {
                            // Grid of progress photos
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp), // fixed height inside scrollable screen
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(progressPhotos) { photo ->
                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(0.8f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(ForgeCarbon)
                                            .clickable { selectedPhotoDetail = photo }
                                    ) {
                                        // Progress picture image
                                        AsyncImage(
                                            model = photo.photoPath,
                                            contentDescription = "Progress Photo ${photo.weekLabel}",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )

                                        // Dark gradient overlay at bottom
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            Color.Black.copy(alpha = 0.85f)
                                                        ),
                                                        startY = 180f
                                                    )
                                                )
                                        )

                                        // Overlaid metadata
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = photo.weekLabel.uppercase(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = ForgeFire
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${photo.weightKg} kg",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(photo.timestamp)),
                                                fontSize = 9.sp,
                                                color = Color.LightGray
                                            )
                                        }

                                        // Delete trigger
                                        IconButton(
                                            onClick = { viewModel.deleteProgressPhoto(photo.id) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(24.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete photo",
                                                tint = Color.LightGray,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // EDIT MODE (Or initial setup setup)
                Text(
                    text = "Specify your physical parameters and goals below. This adapts the AI Coach and calculations specifically for you.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("name_input"),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ForgeFire) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("weight_input"),
                        leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = ForgeFire) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Height (cm)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("height_input"),
                        leadingIcon = { Icon(Icons.Default.Height, contentDescription = null, tint = ForgeFire) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text("Age") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("age_input"),
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = ForgeFire) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Simple gender Selector
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    ) {
                        Column {
                            Text(
                                "Gender",
                                style = MaterialTheme.typography.bodySmall,
                                color = ForgeFire,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ForgeSteel, RoundedCornerShape(12.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf("Male", "Female").forEach { g ->
                                    val isSelected = gender == g
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) ForgeFire else Color.Transparent)
                                            .clickable { gender = g }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = g,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.White else Color.LightGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Goal Dropdown / Selector
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        "Primary Fitness Target",
                        style = MaterialTheme.typography.bodySmall,
                        color = ForgeFire,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ForgeSteel, RoundedCornerShape(12.dp))
                            .clickable { showGoalMenu = !showGoalMenu }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = fitnessGoal, color = Color.White, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = ForgeFire)
                        }

                        DropdownMenu(
                            expanded = showGoalMenu,
                            onDismissRequest = { showGoalMenu = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(ForgeCarbon)
                        ) {
                            goalsList.forEach { goalOption ->
                                DropdownMenuItem(
                                    text = { Text(goalOption, color = Color.White) },
                                    onClick = {
                                        fitnessGoal = goalOption
                                        showGoalMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = calorieTargetText,
                    onValueChange = { calorieTargetText = it },
                    label = { Text("Daily Calorie Intake Goal (kcal)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("calories_input"),
                    leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = ForgeFire) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio / Current Physical Focus") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("bio_input"),
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = ForgeFire) },
                    placeholder = { Text("e.g., preparing for powerlifting meet in November") },
                    shape = RoundedCornerShape(12.dp)
                )

                // Save Action
                Button(
                    onClick = {
                        val weight = weightText.toFloatOrNull() ?: 70f
                        val height = heightText.toFloatOrNull() ?: 175f
                        val age = ageText.toIntOrNull() ?: 25
                        val calories = calorieTargetText.toIntOrNull() ?: 2500

                        if (name.isNotBlank()) {
                            viewModel.saveProfile(
                                name = name,
                                weight = weight,
                                height = height,
                                age = age,
                                gender = gender,
                                goal = fitnessGoal,
                                calorieTarget = calories,
                                bio = bio
                            )
                            focusManager.clearFocus()
                            onSetupComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .testTag("save_profile_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ForgeFire),
                    shape = RoundedCornerShape(12.dp),
                    enabled = name.isNotBlank()
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save Profile", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SAVE ATHLETE CONFIG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                if (profile != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { isEditMode = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                    ) {
                        Text("CANCEL")
                    }
                }
            }
        }
    }

    // --- OVERLAY DIALOGS ---
    if (showCameraDialog) {
        ProgressCameraDialog(
            viewModel = viewModel,
            profileWeight = profile?.weightKg ?: 70f,
            gender = profile?.gender ?: "Male",
            onDismiss = { showCameraDialog = false }
        )
    }

    selectedPhotoDetail?.let { photo ->
        ProgressPhotoDetailDialog(
            photo = photo,
            viewModel = viewModel,
            onDismiss = { selectedPhotoDetail = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressCameraDialog(
    viewModel: ForgeViewModel,
    profileWeight: Float,
    gender: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var weekInput by remember { mutableStateOf("Week 1") }
    var weightInput by remember { mutableStateOf(profileWeight.toString()) }
    var captionInput by remember { mutableStateOf("") }
    var selectedPose by remember { mutableStateOf("Front Biceps") }
    var filterSelected by remember { mutableStateOf("Hyper-Pump Warmth") }

    val poses = listOf("Front Biceps", "Relaxed Front", "Side Chest", "Back Biceps", "Leg Definition")
    val filters = listOf("Forge Carbon Mono", "Hyper-Pump Warmth", "Raw Cinematic", "Active Steel")

    // File picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localPath = copyUriToLocalCache(context, it)
            viewModel.addProgressPhoto(
                weekLabel = weekInput,
                weightKg = weightInput.toFloatOrNull() ?: profileWeight,
                photoPath = localPath,
                caption = captionInput.trim().ifEmpty { "Selected progress pose: $selectedPose" }
            )
            onDismiss()
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = ForgeFire)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ATHLETE CAMERA INTERFACE",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = ForgeSteel, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                if (!hasCameraPermission) {
                    // Explain & Ask permission
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Camera,
                            contentDescription = null,
                            tint = ForgeFire,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera Access Required",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To record and preserve weekly progress snapshots of your physical transformation directly, the Forge requires Camera permission.",
                            fontSize = 13.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = ForgeFire),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("GRANT CAMERA PERMISSION", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { hasCameraPermission = true }, // Simulated/Fallback integrated camera
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ForgeGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ForgeGreen)
                        ) {
                            Text("USE HIGH-FI ATHLETIC VIEWFINDER SIMULATOR", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Custom Camera Viewfinder & Inputs
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Viewfinder Frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.2f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                                .border(2.dp, ForgeSteel, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Viewfinder Grid Canvas
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw third gridlines
                                val w = size.width
                                val h = size.height
                                val gridPaint = Color.White.copy(alpha = 0.2f)
                                
                                // Verticals
                                drawLine(gridPaint, start = androidx.compose.ui.geometry.Offset(w / 3, 0f), end = androidx.compose.ui.geometry.Offset(w / 3, h), strokeWidth = 1f)
                                drawLine(gridPaint, start = androidx.compose.ui.geometry.Offset(w * 2 / 3, 0f), end = androidx.compose.ui.geometry.Offset(w * 2 / 3, h), strokeWidth = 1f)
                                
                                // Horizontals
                                drawLine(gridPaint, start = androidx.compose.ui.geometry.Offset(0f, h / 3), end = androidx.compose.ui.geometry.Offset(w, h / 3), strokeWidth = 1f)
                                drawLine(gridPaint, start = androidx.compose.ui.geometry.Offset(0f, h * 2 / 3), end = androidx.compose.ui.geometry.Offset(w, h * 2 / 3), strokeWidth = 1f)
                                
                                // Target center bracket
                                drawCircle(ForgeFire.copy(alpha = 0.3f), radius = 40f, center = center, style = Stroke(width = 2f))
                                drawCircle(ForgeFire, radius = 4f, center = center)
                            }

                            // Dynamic Silhouette Guide Overlay
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .border(1.dp, ForgeFire.copy(alpha = 0.5f), CircleShape)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = ForgeFire.copy(alpha = 0.6f),
                                        modifier = Modifier.size(56.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ALIGN BODY: ${selectedPose.uppercase()}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForgeFire,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Active Filter/Lenses tag
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ISO 400 • $filterSelected",
                                    fontSize = 10.sp,
                                    color = ForgeGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Capture settings (Inputs)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = weekInput,
                                onValueChange = { weekInput = it },
                                label = { Text("Week Label") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                            )

                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                label = { Text("Weight (kg)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Pose selection
                        Text(
                            "Select Capture Posture Guide",
                            fontSize = 11.sp,
                            color = ForgeFire,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            poses.forEach { p ->
                                val isSelected = selectedPose == p
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ForgeFire else ForgeSteel)
                                        .clickable { selectedPose = p }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = p,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color.LightGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Filter selection
                        Text(
                            "Visual Filter Palette",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filters.forEach { f ->
                                val isSelected = filterSelected == f
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ForgeGreen else ForgeSteel)
                                        .clickable { filterSelected = f }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = f,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color.LightGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = captionInput,
                            onValueChange = { captionInput = it },
                            label = { Text("Annotation / Caption") },
                            placeholder = { Text("e.g. Quad separation is becoming noticeable! Fuel level high.") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Primary capture actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Import from storage
                            IconButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(ForgeSteel, RoundedCornerShape(12.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Pick from gallery",
                                    tint = ForgeGreen
                                )
                            }

                            // Trigger simulated high-fi camera snap and write to DB
                            Button(
                                onClick = {
                                    val generatedPath = generateProgressBitmap(
                                        context = context,
                                        weekLabel = weekInput,
                                        weightKg = weightInput.toFloatOrNull() ?: profileWeight,
                                        pose = selectedPose,
                                        gender = gender
                                    )
                                    viewModel.addProgressPhoto(
                                        weekLabel = weekInput,
                                        weightKg = weightInput.toFloatOrNull() ?: profileWeight,
                                        photoPath = generatedPath,
                                        caption = captionInput.trim().ifEmpty { "$selectedPose posture alignment recorded." }
                                    )
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForgeFire),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SNAP PHOTO & RECORD", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressPhotoDetailDialog(
    photo: ProgressPhoto,
    viewModel: ForgeViewModel,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = photo.weekLabel.uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = ForgeFire
                        )
                        Text(
                            text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date(photo.timestamp)),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = ForgeSteel, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Image display
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                ) {
                    AsyncImage(
                        model = photo.photoPath,
                        contentDescription = "Progress Photo ${photo.weekLabel}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats and Caption
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ForgeSteel),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ATHLETE WEIGHT",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${photo.weightKg} kg",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForgeFire
                            )
                        }
                        
                        if (photo.caption.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = ForgeCarbon, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = photo.caption,
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                    ) {
                        Text("CLOSE")
                    }

                    Button(
                        onClick = {
                            viewModel.deleteProgressPhoto(photo.id)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.6.dp))
                        Text("DELETE PHOTO", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun copyUriToLocalCache(context: android.content.Context, uri: Uri): String {
    val file = File(context.cacheDir, "progress_gallery_${System.currentTimeMillis()}.jpg")
    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return file.absolutePath
}

fun generateProgressBitmap(
    context: android.content.Context,
    weekLabel: String,
    weightKg: Float,
    pose: String,
    gender: String
): String {
    val width = 600
    val height = 800
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    
    val paint = Paint().apply {
        isAntiAlias = true
    }
    
    val gradient = android.graphics.LinearGradient(
        0f, 0f, 0f, height.toFloat(),
        0xFF1C1C1E.toInt(), 0xFF0E0E10.toInt(),
        android.graphics.Shader.TileMode.CLAMP
    )
    paint.shader = gradient
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    paint.shader = null
    
    paint.color = 0x1AFFFFFF.toInt()
    paint.strokeWidth = 1f
    for (i in 1..4) {
        val x = (width / 5) * i
        canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), paint)
    }
    for (i in 1..6) {
        val y = (height / 7) * i
        canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
    }
    
    paint.color = 0x33FF3D00.toInt()
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 2f
    canvas.drawCircle(width / 2f, height / 2.2f, 120f, paint)
    canvas.drawCircle(width / 2f, height / 2.2f, 6f, paint)
    canvas.drawLine(width / 2f - 160f, height / 2.2f, width / 2f + 160f, height / 2.2f, paint)
    canvas.drawLine(width / 2f, height / 2.2f - 160f, width / 2f, height / 2.2f + 160f, paint)
    
    val bodyPaint = Paint().apply {
        isAntiAlias = true
        color = 0xFF2C2C2E.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(width / 2f, height / 2.2f - 150f, 35f, bodyPaint)
    canvas.drawRect(width / 2f - 10f, height / 2.2f - 120f, width / 2f + 10f, height / 2.2f - 95f, bodyPaint)
    
    val torsoPath = android.graphics.Path().apply {
        moveTo(width / 2f - 60f, height / 2.2f - 95f)
        lineTo(width / 2f + 60f, height / 2.2f - 95f)
        lineTo(width / 2f + 40f, height / 2.2f + 50f)
        lineTo(width / 2f - 40f, height / 2.2f + 50f)
        close()
    }
    canvas.drawPath(torsoPath, bodyPaint)
    
    if (pose.contains("Biceps")) {
        val armsPath = android.graphics.Path().apply {
            moveTo(width / 2f - 60f, height / 2.2f - 95f)
            lineTo(width / 2f - 120f, height / 2.2f - 95f)
            lineTo(width / 2f - 130f, height / 2.2f - 140f)
            lineTo(width / 2f - 100f, height / 2.2f - 150f)
            lineTo(width / 2f - 90f, height / 2.2f - 120f)
            lineTo(width / 2f - 60f, height / 2.2f - 75f)
            close()
            
            moveTo(width / 2f + 60f, height / 2.2f - 95f)
            lineTo(width / 2f + 120f, height / 2.2f - 95f)
            lineTo(width / 2f + 130f, height / 2.2f - 140f)
            lineTo(width / 2f + 100f, height / 2.2f - 150f)
            lineTo(width / 2f + 90f, height / 2.2f - 120f)
            lineTo(width / 2f + 60f, height / 2.2f - 75f)
            close()
        }
        bodyPaint.color = 0xFF444446.toInt()
        canvas.drawPath(armsPath, bodyPaint)
    } else {
        val armsPath = android.graphics.Path().apply {
            moveTo(width / 2f - 60f, height / 2.2f - 95f)
            lineTo(width / 2f - 90f, height / 2.2f + 20f)
            lineTo(width / 2f - 75f, height / 2.2f + 20f)
            lineTo(width / 2f - 50f, height / 2.2f - 60f)
            close()
            
            moveTo(width / 2f + 60f, height / 2.2f - 95f)
            lineTo(width / 2f + 90f, height / 2.2f + 20f)
            lineTo(width / 2f + 75f, height / 2.2f + 20f)
            lineTo(width / 2f + 50f, height / 2.2f - 60f)
            close()
        }
        bodyPaint.color = 0xFF444446.toInt()
        canvas.drawPath(armsPath, bodyPaint)
    }
    
    val legsPath = android.graphics.Path().apply {
        moveTo(width / 2f - 40f, height / 2.2f + 50f)
        lineTo(width / 2f - 50f, height / 2.2f + 180f)
        lineTo(width / 2f - 20f, height / 2.2f + 180f)
        lineTo(width / 2f - 10f, height / 2.2f + 50f)
        close()
        
        moveTo(width / 2f + 40f, height / 2.2f + 50f)
        lineTo(width / 2f + 50f, height / 2.2f + 180f)
        lineTo(width / 2f + 20f, height / 2.2f + 180f)
        lineTo(width / 2f + 10f, height / 2.2f + 50f)
        close()
    }
    bodyPaint.color = 0xFF3A3A3C.toInt()
    canvas.drawPath(legsPath, bodyPaint)
    
    val badgePaint = Paint().apply {
        isAntiAlias = true
        color = 0xCCFF3D00.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(25f, height - 195f, width - 25f, height - 25f, 20f, 20f, badgePaint)
    
    val textPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        textSize = 28f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
    }
    canvas.drawText("FORGE ATHLETE EVOLUTION", 50f, height - 150f, textPaint)
    
    textPaint.textSize = 20f
    textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
    canvas.drawText("TARGET: $weekLabel  |  WEIGHT: $weightKg KG", 50f, height - 105f, textPaint)
    
    textPaint.textSize = 16f
    textPaint.color = 0xCCFFFFFF.toInt()
    textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
    val dateStr = SimpleDateFormat("dd MMMM yyyy • HH:mm", Locale.getDefault()).format(Date())
    canvas.drawText("POSE: $pose  |  RECORDED: $dateStr", 50f, height - 65f, textPaint)
    
    val file = File(context.cacheDir, "progress_${System.currentTimeMillis()}.jpg")
    try {
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        out.flush()
        out.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
    return file.absolutePath
}
