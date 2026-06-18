package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val phoneNumber: String = "",
    val age: Int = 24,
    val heightCm: Float = 175f,
    val weightKg: Float = 70f,
    val goal: String = "Stay Fit and Healthy", // "Lose Weight", "Gain Weight", "Stay Fit and Healthy"
    val activityLevel: String = "Moderate", // "Sedentary", "Moderate", "Active"
    val isPremium: Boolean = false,
    val subscriptionTier: Int = 0, // 0 = Free, 1 = Premium, 2 = Pro
    val createdAt: Long = System.currentTimeMillis()
) {
    val isPro: Boolean
        get() = subscriptionTier >= 2
    // Calculate BMI
    val bmi: Float
        get() = if (heightCm > 0) weightKg / ((heightCm / 100f) * (heightCm / 100f)) else 0f

    val bmiCategory: String
        get() {
            val value = bmi
            return when {
                value < 18.5f -> "Underweight"
                value < 25.0f -> "Normal (Healthy)"
                value < 30.0f -> "Overweight"
                else -> "Obese"
            }
        }
}

@Entity(tableName = "daily_activity", primaryKeys = ["date"])
data class DailyActivity(
    val date: String, // "YYYY-MM-DD"
    val steps: Int = 0,
    val waterGlasses: Int = 0, // e.g. 0 to 8 or more
    val mood: String = "", // "Excellent" 😄, "Good" 😊, "Neutral" 😐, "Tired" 😴, "Stressed" 😫
    val sleepBedTime: String = "22:00",
    val sleepWakeTime: String = "06:00",
    val sleepQualityRating: Int = 3, // 1 to 5 stars
    val caloriesBurned: Int = 0,
    val note: String = ""
) {
    // Calculates sleep duration in hours from times "HH:MM"
    val sleepDurationHours: Float
        get() {
            try {
                val bedParts = sleepBedTime.split(":")
                val wakeParts = sleepWakeTime.split(":")
                if (bedParts.size != 2 || wakeParts.size != 2) return 8.0f
                val bedMin = bedParts[0].toInt() * 60 + bedParts[1].toInt()
                val wakeMin = wakeParts[0].toInt() * 60 + wakeParts[1].toInt()
                
                return if (wakeMin >= bedMin) {
                    (wakeMin - bedMin) / 60.0f
                } else {
                    // Overnight sleep
                    ((1440 - bedMin) + wakeMin) / 60.0f
                }
            } catch (e: Exception) {
                return 8.0f
            }
        }
}

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "YYYY-MM-DD"
    val mealType: String, // "Breakfast", "Mid Morning", "Lunch", "Evening Snack", "Dinner"
    val mealName: String,
    val calories: Int = 0,
    val proteinGrams: Float = 0f,
    val carbsGrams: Float = 0f,
    val fatGrams: Float = 0f,
    val isCompleted: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val sender: String, // "user" or "ai"
    val message: String
)
