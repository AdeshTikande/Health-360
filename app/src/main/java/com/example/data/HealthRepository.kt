package com.example.data

import kotlinx.coroutines.flow.Flow

class HealthRepository(private val healthDao: HealthDao) {

    val userProfile: Flow<UserProfile?> = healthDao.getUserProfile()
    val allActivities: Flow<List<DailyActivity>> = healthDao.getAllDailyActivity()
    val chatMessages: Flow<List<ChatMessage>> = healthDao.getChatMessages()

    suspend fun getUserProfileDirect(): UserProfile? {
        return healthDao.getUserProfileDirect()
    }

    suspend fun insertUserProfile(profile: UserProfile) {
        healthDao.insertUserProfile(profile)
    }

    suspend fun deleteUserProfile() {
        healthDao.deleteUserProfile()
    }

    fun getDailyActivity(date: String): Flow<DailyActivity?> {
        return healthDao.getDailyActivity(date)
    }

    suspend fun getDailyActivityDirect(date: String): DailyActivity? {
        return healthDao.getDailyActivityDirect(date)
    }

    suspend fun insertDailyActivity(activity: DailyActivity) {
        healthDao.insertDailyActivity(activity)
    }

    fun getMealsForDate(date: String): Flow<List<Meal>> {
        return healthDao.getMealsForDate(date)
    }

    suspend fun insertMeal(meal: Meal) {
        healthDao.insertMeal(meal)
    }

    suspend fun updateMeal(meal: Meal) {
        healthDao.updateMeal(meal)
    }

    suspend fun deleteMealsForDate(date: String) {
        healthDao.deleteMealsForDate(date)
    }

    suspend fun insertChatMessage(message: ChatMessage) {
        healthDao.insertChatMessage(message)
    }

    suspend fun clearChatMessages() {
        healthDao.clearChatMessages()
    }
}
