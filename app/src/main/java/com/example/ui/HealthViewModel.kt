package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HealthViewModel(private val application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = HealthRepository(database.healthDao())

    // Date tracking
    private val _selectedDate = MutableStateFlow(LocalDate.now().toString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Observables
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current date activity is observed reactively based on selectedDate
    val dailyActivity: StateFlow<DailyActivity?> = _selectedDate
        .flatMapLatest { date ->
            repository.getDailyActivity(date).map { activity ->
                activity ?: createDefaultActivityForDate(date)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current date meals are observed reactively based on selectedDate
    val meals: StateFlow<List<Meal>> = _selectedDate
        .flatMapLatest { date ->
            repository.getMealsForDate(date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allActivitiesHistory: StateFlow<List<DailyActivity>> = repository.allActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state loaders
    private val _isGeneratingDiet = MutableStateFlow(false)
    val isGeneratingDiet: StateFlow<Boolean> = _isGeneratingDiet.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Calculated Health Score (0-100) reactively computed
    val healthScore: StateFlow<Int> = combine(dailyActivity, meals) { activity, mealList ->
        if (activity == null) return@combine 50
        
        var score = 0
        
        // 1. Steps tracking (up to 30 points)
        // Target: 10,000 steps
        val targetSteps = 10000
        val stepRatio = (activity.steps.toFloat() / targetSteps.toFloat()).coerceAtMost(1.0f)
        score += (stepRatio * 30).toInt()

        // 2. Water intake (up to 20 points)
        // Target: 8 glasses
        val targetWater = 8
        val waterRatio = (activity.waterGlasses.toFloat() / targetWater.toFloat()).coerceAtMost(1.0f)
        score += (waterRatio * 20).toInt()

        // 3. Sleep (up to 25 points)
        // Optimal sleep: 7.0 to 9.0 hours with rating 3+
        val sleepHrs = activity.sleepDurationHours
        val sleepScore = when {
            sleepHrs >= 7.0f && sleepHrs <= 9.0f -> 20
            sleepHrs >= 6.0f && sleepHrs < 7.0f -> 15
            sleepHrs > 9.0f && sleepHrs <= 10.0f -> 15
            else -> 10
        }
        val qualityBonus = ((activity.sleepQualityRating / 5.0f) * 5f).toInt()
        score += sleepScore + qualityBonus

        // 4. Mood Check-in (up to 15 points)
        val moodScore = when (activity.mood) {
            "Excellent" -> 15
            "Good" -> 13
            "Neutral" -> 10
            "Tired" -> 8
            "Stressed" -> 5
            else -> 8 // default prior to check-in
        }
        score += moodScore

        // 5. Diet completion status check (up to 10 points)
        if (mealList.isNotEmpty()) {
            val completed = mealList.count { it.isCompleted }
            val completedRatio = completed.toFloat() / mealList.size.toFloat()
            score += (completedRatio * 10).toInt()
        } else {
            score += 5 // half points if no meal plan generated yet
        }

        score.coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50)

    // Action Methods
    fun changeSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun selectDateOffset(days: Int) {
        val current = LocalDate.parse(_selectedDate.value)
        val newDate = current.plusDays(days.toLong())
        _selectedDate.value = newDate.toString()
    }

    private fun createDefaultActivityForDate(date: String): DailyActivity {
        val newActivity = DailyActivity(date = date)
        viewModelScope.launch {
            if (repository.getDailyActivityDirect(date) == null) {
                repository.insertDailyActivity(newActivity)
            }
        }
        return newActivity
    }

    // PROFILE OPERATION
    fun saveUserProfile(name: String, age: Int, height: Float, weight: Float, goal: String, activityLevel: String, phoneNumber: String? = null) {
        viewModelScope.launch {
            val current = repository.getUserProfileDirect() ?: UserProfile()
            val updated = current.copy(
                name = name,
                phoneNumber = phoneNumber ?: current.phoneNumber,
                age = age,
                heightCm = height,
                weightKg = weight,
                goal = goal,
                activityLevel = activityLevel
            )
            repository.insertUserProfile(updated)
        }
    }

    fun logoutUserProfile() {
        viewModelScope.launch {
            repository.deleteUserProfile()
            repository.clearChatMessages()
        }
    }

    fun setSubscriptionTier(tier: Int) {
        viewModelScope.launch {
            val current = repository.getUserProfileDirect() ?: UserProfile()
            val updated = current.copy(
                subscriptionTier = tier,
                isPremium = tier >= 1
            )
            repository.insertUserProfile(updated)
        }
    }

    fun togglePremiumStatus() {
        viewModelScope.launch {
            val current = repository.getUserProfileDirect() ?: UserProfile()
            val nextTier = if (current.subscriptionTier > 0) 0 else 1
            val updated = current.copy(
                subscriptionTier = nextTier,
                isPremium = nextTier >= 1
            )
            repository.insertUserProfile(updated)
        }
    }

    // ACTIVITY INTERACTIONS
    fun incrementWater() {
        viewModelScope.launch {
            val date = _selectedDate.value
            val current = repository.getDailyActivityDirect(date) ?: DailyActivity(date = date)
            val updated = current.copy(waterGlasses = current.waterGlasses + 1)
            repository.insertDailyActivity(updated)
        }
    }

    fun decrementWater() {
        viewModelScope.launch {
            val date = _selectedDate.value
            val current = repository.getDailyActivityDirect(date) ?: DailyActivity(date = date)
            if (current.waterGlasses > 0) {
                val updated = current.copy(waterGlasses = current.waterGlasses - 1)
                repository.insertDailyActivity(updated)
            }
        }
    }

    fun addSteps(amount: Int) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val current = repository.getDailyActivityDirect(date) ?: DailyActivity(date = date)
            val updated = current.copy(steps = current.steps + amount)
            repository.insertDailyActivity(updated)
        }
    }

    fun setMood(mood: String) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val current = repository.getDailyActivityDirect(date) ?: DailyActivity(date = date)
            val updated = current.copy(mood = mood)
            repository.insertDailyActivity(updated)
        }
    }

    fun updateSleep(bedTime: String, wakeTime: String, quality: Int) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val current = repository.getDailyActivityDirect(date) ?: DailyActivity(date = date)
            val updated = current.copy(
                sleepBedTime = bedTime,
                sleepWakeTime = wakeTime,
                sleepQualityRating = quality
            )
            repository.insertDailyActivity(updated)
        }
    }

    fun burnCalories(calories: Int) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val current = repository.getDailyActivityDirect(date) ?: DailyActivity(date = date)
            val updated = current.copy(caloriesBurned = current.caloriesBurned + calories)
            repository.insertDailyActivity(updated)
        }
    }

    // DIET / MEAL PLAN OPTIONS
    fun generateDietPlan() {
        val date = _selectedDate.value
        val dayString = LocalDate.parse(date).dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        
        viewModelScope.launch {
            _isGeneratingDiet.value = true
            try {
                val profile = repository.getUserProfileDirect()
                val apiMeals = GeminiService.generateIndianDietPlan(profile, dayString)
                if (apiMeals.isNotEmpty()) {
                    // Clear existing meals for this date
                    repository.deleteMealsForDate(date)
                    // Save new meals
                    for (meal in apiMeals) {
                        repository.insertMeal(meal.copy(date = date))
                    }
                } else {
                    // Populate offline sample contingency meals if service/network fails
                    generateFallbackMeals(date)
                }
            } catch (e: Exception) {
                generateFallbackMeals(date)
            } finally {
                _isGeneratingDiet.value = false
            }
        }
    }

    private suspend fun generateFallbackMeals(date: String) {
        repository.deleteMealsForDate(date)
        val fallbacks = listOf(
            Meal(date = date, mealType = "Breakfast", mealName = "Idli (2 pcs) with Sambar & Coconut chutney", calories = 310, proteinGrams = 8f, carbsGrams = 52f, fatGrams = 6f),
            Meal(date = date, mealType = "Mid Morning", mealName = "A bowl of Fresh Mixed Fruits & Roasted Almonds", calories = 150, proteinGrams = 3f, carbsGrams = 24f, fatGrams = 5f),
            Meal(date = date, mealType = "Lunch", mealName = "Roti (2) with Mixed Veg Curry, Dal Tadka & Curd", calories = 480, proteinGrams = 18f, carbsGrams = 68f, fatGrams = 12f),
            Meal(date = date, mealType = "Evening Snack", mealName = "Spiced Masala Chai with Roasted Chana", calories = 120, proteinGrams = 5f, carbsGrams = 15f, fatGrams = 3f),
            Meal(date = date, mealType = "Dinner", mealName = "Paneer Bhurji with 1 Chapati & Cucumber Salad", calories = 420, proteinGrams = 22f, carbsGrams = 36f, fatGrams = 14f)
        )
        for (meal in fallbacks) {
            repository.insertMeal(meal)
        }
    }

    fun toggleMealCompletion(meal: Meal) {
        viewModelScope.launch {
            repository.updateMeal(meal.copy(isCompleted = !meal.isCompleted))
        }
    }

    // CHATBOT OPERATIONS
    fun sendUserChatMessage(messageText: String) {
        if (messageText.trim().isEmpty()) return
        
        viewModelScope.launch {
            // Save user message in db
            val userMsg = ChatMessage(sender = "user", message = messageText)
            repository.insertChatMessage(userMsg)
            
            _isAiThinking.value = true
            
            try {
                val profile = repository.getUserProfileDirect()
                
                // Fetch recent conversational logs
                val currentHistory = repository.chatMessages.first().takeLast(10).map {
                    Pair(it.sender, it.message)
                }
                
                // Contact Gemini Coach
                val response = GeminiService.getHealthAdvice(messageText, currentHistory, profile)
                
                // Save AI response
                val aiMsg = ChatMessage(sender = "ai", message = response)
                repository.insertChatMessage(aiMsg)
                
            } catch (e: Exception) {
                val errorMsg = ChatMessage(sender = "ai", message = "Sorry, I am facing a connection issue: ${e.localizedMessage}. Please retry.")
                repository.insertChatMessage(errorMsg)
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatMessages()
            // Add a friendly welcome message
            repository.insertChatMessage(ChatMessage(sender = "ai", message = "Hi! I'm your Health360 AI Coach. How can I help you today with your fitness, diet, or sleep goals?"))
        }
    }

    // Quick helper to format selectedDate
    fun getFormattedSelectedDate(): String {
        return try {
            val localDate = LocalDate.parse(_selectedDate.value)
            val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
            localDate.format(formatter)
        } catch (e: Exception) {
            _selectedDate.value
        }
    }
}

class HealthViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HealthViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
