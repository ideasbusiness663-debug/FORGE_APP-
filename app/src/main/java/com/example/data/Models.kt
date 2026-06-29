package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val weightKg: Float,
    val heightCm: Float,
    val age: Int,
    val gender: String,
    val fitnessGoal: String,
    val dailyCalorieTarget: Int,
    val bio: String = ""
)

@Entity(tableName = "workouts")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseName: String,
    val category: String, // e.g., "Strength", "Cardio", "Yoga", "Core"
    val sets: Int,
    val reps: Int,
    val weightKg: Float,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "nutrition")
data class NutritionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    val foodName: String,
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "water")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "User", "AI_Coach", "Buddy_Alex"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "progress_photos")
data class ProgressPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekLabel: String,
    val weightKg: Float,
    val photoPath: String,
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
