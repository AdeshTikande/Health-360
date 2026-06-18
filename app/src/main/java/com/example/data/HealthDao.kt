package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {

    // PROFILE
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Query("DELETE FROM user_profile WHERE id = 1")
    suspend fun deleteUserProfile()

    // DAILY ACTIVITY
    @Query("SELECT * FROM daily_activity WHERE date = :date LIMIT 1")
    fun getDailyActivity(date: String): Flow<DailyActivity?>

    @Query("SELECT * FROM daily_activity WHERE date = :date LIMIT 1")
    suspend fun getDailyActivityDirect(date: String): DailyActivity?

    @Query("SELECT * FROM daily_activity ORDER BY date DESC")
    fun getAllDailyActivity(): Flow<List<DailyActivity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyActivity(activity: DailyActivity)

    // MEALS/DIET
    @Query("SELECT * FROM meals WHERE date = :date ORDER BY id ASC")
    fun getMealsForDate(date: String): Flow<List<Meal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal)

    @Update
    suspend fun updateMeal(meal: Meal)

    @Query("DELETE FROM meals WHERE date = :date")
    suspend fun deleteMealsForDate(date: String)

    // HEALTH COACH CHAT
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()
}
