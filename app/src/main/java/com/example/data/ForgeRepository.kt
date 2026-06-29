package com.example.data

import kotlinx.coroutines.flow.Flow

class ForgeRepository(private val db: AppDatabase) {

    val userProfile: Flow<UserProfile?> = db.userProfileDao().getProfile()
    val allWorkouts: Flow<List<WorkoutLog>> = db.workoutDao().getAllWorkouts()
    val allNutrition: Flow<List<NutritionLog>> = db.nutritionDao().getAllNutrition()
    val allWater: Flow<List<WaterLog>> = db.waterDao().getAllWater()
    val allMessages: Flow<List<ChatMessage>> = db.chatMessageDao().getAllMessages()
    val allPhotos: Flow<List<ProgressPhoto>> = db.progressPhotoDao().getAllPhotos()

    suspend fun saveProfile(profile: UserProfile) {
        db.userProfileDao().insertProfile(profile)
    }

    suspend fun insertWorkout(workout: WorkoutLog) {
        db.workoutDao().insertWorkout(workout)
    }

    suspend fun deleteWorkout(id: Long) {
        db.workoutDao().deleteWorkout(id)
    }

    suspend fun insertNutrition(nutrition: NutritionLog) {
        db.nutritionDao().insertNutrition(nutrition)
    }

    suspend fun deleteNutrition(id: Long) {
        db.nutritionDao().deleteNutrition(id)
    }

    suspend fun insertWater(water: WaterLog) {
        db.waterDao().insertWater(water)
    }

    suspend fun insertMessage(message: ChatMessage) {
        db.chatMessageDao().insertMessage(message)
    }

    suspend fun insertPhoto(photo: ProgressPhoto) {
        db.progressPhotoDao().insertPhoto(photo)
    }

    suspend fun deletePhoto(id: Long) {
        db.progressPhotoDao().deletePhoto(id)
    }
}
