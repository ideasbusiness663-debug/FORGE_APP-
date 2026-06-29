package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FeedPost(
    val id: String,
    val userName: String,
    val userGoal: String,
    val activityText: String,
    val durationMinutes: Int,
    val timestamp: Long,
    val category: String,
    val likesCount: Int,
    val isLiked: Boolean = false,
    val comments: List<String> = emptyList()
)

class ForgeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ForgeRepository(db)

    // UI States observed by Composables
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val workouts: StateFlow<List<WorkoutLog>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nutrition: StateFlow<List<NutritionLog>> = repository.allNutrition
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val waterLogs: StateFlow<List<WaterLog>> = repository.allWater
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val progressPhotos: StateFlow<List<ProgressPhoto>> = repository.allPhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Interactive States
    private val _isCoachTyping = MutableStateFlow(false)
    val isCoachTyping: StateFlow<Boolean> = _isCoachTyping.asStateFlow()

    private val _feedLikes = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val _userLikes = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val _feedComments = MutableStateFlow<Map<String, List<String>>>(emptyMap())

    // Community Feed State, combining static mock posts with user logged workouts
    val communityFeed: Flow<List<FeedPost>> = combine(
        workouts,
        _feedLikes,
        _userLikes,
        _feedComments,
        userProfile
    ) { userWorkouts, likes, userLikesMap, commentsMap, profile ->
        val list = mutableListOf<FeedPost>()

        // 1. Add user's logged workouts
        userWorkouts.forEach { log ->
            val id = "user_workout_${log.id}"
            list.add(
                FeedPost(
                    id = id,
                    userName = profile?.name ?: "You (Recruit)",
                    userGoal = profile?.fitnessGoal ?: "Build Strength",
                    activityText = "completed ${log.exerciseName} (${log.sets} sets x ${log.reps} reps @ ${log.weightKg}kg)",
                    durationMinutes = log.durationMinutes,
                    timestamp = log.timestamp,
                    category = log.category,
                    likesCount = likes[id] ?: 3,
                    isLiked = userLikesMap[id] ?: false,
                    comments = commentsMap[id] ?: emptyList()
                )
            )
        }

        // 2. Add high-quality community mock workouts
        val mockPosts = listOf(
            FeedPost(
                id = "mock_1",
                userName = "Alex Mercer",
                userGoal = "Powerlifting",
                activityText = "smashed a new Squat PR of 180kg for 3 reps! Feeling unstoppable today.",
                durationMinutes = 45,
                timestamp = System.currentTimeMillis() - 120_000, // 2 mins ago
                category = "Strength",
                likesCount = likes["mock_1"] ?: 18,
                isLiked = userLikesMap["mock_1"] ?: false,
                comments = commentsMap["mock_1"] ?: listOf("Epic lift bro!", "Insane depth!")
            ),
            FeedPost(
                id = "mock_2",
                userName = "Sarah Chen",
                userGoal = "Marathon Prep",
                activityText = "completed a steady 10K outdoor run. Decent pace, legs felt great.",
                durationMinutes = 52,
                timestamp = System.currentTimeMillis() - 1_200_000, // 20 mins ago
                category = "Cardio",
                likesCount = likes["mock_2"] ?: 12,
                isLiked = userLikesMap["mock_2"] ?: false,
                comments = commentsMap["mock_2"] ?: listOf("Super clean run!", "Keep crushing it!")
            ),
            FeedPost(
                id = "mock_3",
                userName = "Marcus Vance",
                userGoal = "Hypertrophy",
                activityText = "destroyed an intense Shoulder & Arm burner. Absolute forge-fire pump.",
                durationMinutes = 60,
                timestamp = System.currentTimeMillis() - 7_200_000, // 2 hours ago
                category = "Strength",
                likesCount = likes["mock_3"] ?: 24,
                isLiked = userLikesMap["mock_3"] ?: false,
                comments = commentsMap["mock_3"] ?: listOf("What's the routine?", "Boulders!")
            ),
            FeedPost(
                id = "mock_4",
                userName = "Emma Watson",
                userGoal = "Mobility & Flow",
                activityText = "completed a 40-minute Vinyasa yoga session for active recovery.",
                durationMinutes = 40,
                timestamp = System.currentTimeMillis() - 28_800_000, // 8 hours ago
                category = "Yoga",
                likesCount = likes["mock_4"] ?: 9,
                isLiked = userLikesMap["mock_4"] ?: false,
                comments = commentsMap["mock_4"] ?: emptyList()
            )
        )

        list.addAll(mockPosts)
        list.sortByDescending { it.timestamp }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Body Analysis states
    private val _bodyAnalysisResult = MutableStateFlow<String?>(null)
    val bodyAnalysisResult: StateFlow<String?> = _bodyAnalysisResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    init {
        // Initialize pre-seeded chat with AI Coach greeting
        viewModelScope.launch {
            repository.allMessages.first().let { msgs ->
                if (msgs.isEmpty()) {
                    repository.insertMessage(
                        ChatMessage(
                            sender = "AI_Coach",
                            text = "Welcome to the **FORGE**, Recruit! I am your AI Coach. I'm here to push your limits, review your workouts, optimize your macros, and keep your discipline fire burning. Are you ready to forge a stronger version of yourself today? Tell me your current goal!"
                        )
                    )
                }
            }
        }
    }

    // --- Profile actions ---
    fun saveProfile(
        name: String,
        weight: Float,
        height: Float,
        age: Int,
        gender: String,
        goal: String,
        calorieTarget: Int,
        bio: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = UserProfile(
                id = 1,
                name = name,
                weightKg = weight,
                heightCm = height,
                age = age,
                gender = gender,
                fitnessGoal = goal,
                dailyCalorieTarget = calorieTarget,
                bio = bio
            )
            repository.saveProfile(profile)
        }
    }

    // --- Workout actions ---
    fun logWorkout(
        name: String,
        category: String,
        sets: Int,
        reps: Int,
        weight: Float,
        duration: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val workout = WorkoutLog(
                exerciseName = name,
                category = category,
                sets = sets,
                reps = reps,
                weightKg = weight,
                durationMinutes = duration
            )
            repository.insertWorkout(workout)
        }
    }

    fun deleteWorkout(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWorkout(id)
        }
    }

    // --- Nutrition & Water Actions ---
    fun logNutrition(
        mealType: String,
        foodName: String,
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val log = NutritionLog(
                mealType = mealType,
                foodName = foodName,
                calories = calories,
                proteinGrams = protein,
                carbsGrams = carbs,
                fatGrams = fat
            )
            repository.insertNutrition(log)
        }
    }

    fun deleteNutrition(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNutrition(id)
        }
    }

    fun logWater(amountMl: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertWater(WaterLog(amountMl = amountMl))
        }
    }

    // --- Social Interactions ---
    fun toggleLike(postId: String) {
        val currentLiked = _userLikes.value[postId] ?: false
        val currentLikesCount = _feedLikes.value[postId] ?: if (postId.startsWith("mock")) 10 else 0

        _userLikes.value = _userLikes.value + (postId to !currentLiked)
        _feedLikes.value = _feedLikes.value + (postId to if (currentLiked) currentLikesCount - 1 else currentLikesCount + 1)
    }

    fun addComment(postId: String, text: String) {
        if (text.isBlank()) return
        val currentComments = _feedComments.value[postId] ?: emptyList()
        _feedComments.value = _feedComments.value + (postId to (currentComments + text))
    }

    // --- Chat with AI Coach ---
    fun sendMessageToCoach(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Insert User Message
            repository.insertMessage(ChatMessage(sender = "User", text = text))

            // 2. Trigger AI response
            _isCoachTyping.value = true
            val profile = userProfile.value
            val profileContext = if (profile != null) {
                "User Profile: Name ${profile.name}, Age ${profile.age}, Weight ${profile.weightKg}kg, Height ${profile.heightCm}cm, Goal '${profile.fitnessGoal}', Calorie Target ${profile.dailyCalorieTarget}kcal."
            } else {
                "User has not set up their profile details yet."
            }

            val systemPrompt = """
                You are the FORGE AI Coach, an elite level personal trainer, functional movement specialist, and high-performance nutritionist. 
                Your coaching personality is intense, motivational, deeply supportive, and disciplined (like forging steel). 
                Address the user as 'Athlete' or by their name.
                Use bullet points and bold headers to structure your training tips. Keep your response brief but heavy-hitting.
                Context of the athlete: $profileContext
            """.trimIndent()

            val reply = GeminiClient.getCoachResponse(text, systemPrompt)
            repository.insertMessage(ChatMessage(sender = "AI_Coach", text = reply))
            _isCoachTyping.value = false
        }
    }

    // --- AI Body Analysis ---
    fun runBodyAnalysis(extraDetails: String) {
        val profile = userProfile.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isAnalyzing.value = true
            val heightM = profile.heightCm / 100f
            val bmi = profile.weightKg / (heightM * heightM)
            val bmiCategory = when {
                bmi < 18.5 -> "Underweight"
                bmi < 25.0 -> "Normal weight"
                bmi < 30.0 -> "Overweight"
                else -> "Obese"
            }

            val prompt = """
                Generate a comprehensive, expert physical body and athletic breakdown based on the following measurements:
                - Name: ${profile.name}
                - Age: ${profile.age}
                - Weight: ${profile.weightKg} kg
                - Height: ${profile.heightCm} cm
                - Calculated BMI: ${String.format("%.1f", bmi)} ($bmiCategory)
                - Primary Goal: ${profile.fitnessGoal}
                - Bio / Current Focus: ${profile.bio}
                - Additional notes from athlete: $extraDetails
                
                Please provide:
                1. A brief professional critique of their physical posture/weight index.
                2. Precise calorie and macro recommendations (Protein, Carbs, Fats) tailored specifically to their goal.
                3. A hyper-focused 3-step action plan to reach their physical target.
                
                Format with clean bold headers, list elements, and keep it incredibly motivating and clear!
            """.trimIndent()

            val systemPrompt = "You are the FORGE Body Architect, an expert in body composition, anthropometry, and physical transformation. Speak with authority, science, and extreme clarity."
            val result = GeminiClient.getCoachResponse(prompt, systemPrompt)
            _bodyAnalysisResult.value = result
            _isAnalyzing.value = false
        }
    }
    
    fun clearBodyAnalysis() {
        _bodyAnalysisResult.value = null
    }

    fun addProgressPhoto(weekLabel: String, weightKg: Float, photoPath: String, caption: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPhoto(
                ProgressPhoto(
                    weekLabel = weekLabel,
                    weightKg = weightKg,
                    photoPath = photoPath,
                    caption = caption
                )
            )
        }
    }

    fun deleteProgressPhoto(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePhoto(id)
        }
    }
}

class ForgeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForgeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ForgeViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
