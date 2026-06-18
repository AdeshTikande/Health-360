package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    /**
     * Sends a health question to Gemini 3.5 Flash and returns the text response.
     * Incorporates user profiles in the system instructions for a highly custom Health Coach persona.
     */
    suspend fun getHealthAdvice(
        userMessage: String,
        history: List<Pair<String, String>>, // list of sender to message (e.g. "user" or "ai")
        profile: UserProfile?
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key is missing! Please configure GEMINI_API_KEY in the AI Studio Secrets panel."
        }

        val url = "$BASE_URL?key=$apiKey"

        val profileContext = if (profile != null) {
            "User Profile context: Name: ${profile.name}, Age: ${profile.age}, Height: ${profile.heightCm} cm, Weight: ${profile.weightKg} kg (BMI: %.1f, Category: ${profile.bmiCategory}), Health Goal: ${profile.goal}, Activity Level: ${profile.activityLevel}."
                .format(profile.bmi)
        } else {
            "User Profile: None set (default/anonymous user)."
        }

        val systemPrompt = """
            You are 'Health360 AI Coach', a highly professional, encouraging, and knowledgeable personal wellness helper.
            $profileContext
            
            Guidelines:
            1. Provide practical, accurate, and scientifically-grounded feedback on diet, workouts, sleep, and overall wellness.
            2. Tailor your recommendations specifically to the user's details above (like Indian vegetarian choices, goals, and BMI category).
            3. Do not diagnose diseases or act as a primary caregiver, but offer clear, constructive lifestyle guidance.
            4. Keep answers motivating, neat, concise, and structured with bullet points where appropriate.
        """.trimIndent()

        try {
            // Build request JSON manually to avoid complex class hierarchies
            val jsonBody = JSONObject()
            
            // System instructions
            val systemInstruction = JSONObject()
            val systemInstructionParts = JSONArray()
            systemInstructionParts.put(JSONObject().put("text", systemPrompt))
            systemInstruction.put("parts", systemInstructionParts)
            jsonBody.put("systemInstruction", systemInstruction)

            // Contents (history + current messaging)
            val contentsArray = JSONArray()

            // Map history
            for (turn in history) {
                val turnObj = JSONObject()
                val role = if (turn.first == "user") "user" else "model"
                turnObj.put("role", role)
                val parts = JSONArray()
                parts.put(JSONObject().put("text", turn.second))
                turnObj.put("parts", parts)
                contentsArray.put(turnObj)
            }

            // Current message
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", userMessage))
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            jsonBody.put("contents", contentsArray)

            // Make request
            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Request failed: ${response.code}, body: $errBody")
                    return@withContext "Unable to communicate with Health360 AI. Please check network/key permissions. Status code: ${response.code}"
                }

                val responseBody = response.body?.string() ?: return@withContext "Error: Empty response description"
                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No text generated.")
                    }
                }
                return@withContext "No response text was generated by the AI."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in advice request", e)
            return@withContext "Connection Error: ${e.localizedMessage ?: "Please try again later."}"
        }
    }

    /**
     * Structured Diet Plan definition for parser convenience
     */
    data class GeminiMeal(
        val mealType: String,
        val mealName: String,
        val calories: Int,
        val proteinGrams: Float,
        val carbsGrams: Float,
        val fatGrams: Float
    )

    data class GeminiDietResponse(
        val meals: List<GeminiMeal>
    )

    /**
     * Generates a structured Indian Vegetarian Diet Plan using JSON response schema.
     */
    suspend fun generateIndianDietPlan(
        profile: UserProfile?,
        selectedDay: String
    ): List<Meal> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Missing key in diet generation")
            return@withContext emptyList()
        }

        val url = "$BASE_URL?key=$apiKey"

        val profileContext = if (profile != null) {
            "User Details: Age: ${profile.age}, Height: ${profile.heightCm} cm, Weight: ${profile.weightKg} kg (BMI Category: ${profile.bmiCategory}), Goal: ${profile.goal}, Activity Level: ${profile.activityLevel}."
        } else {
            "User Details: Default healthy adult."
        }

        val promptText = """
            Generate an Indian Vegetarian Meal Plan for $selectedDay.
            $profileContext
            Provide exactly 5 meals: 'Breakfast', 'Mid Morning', 'Lunch', 'Evening Snack', and 'Dinner'.
            Return them in a valid JSON schema that aligns with standard calorie standards for the user's goals.
        """.trimIndent()

        try {
            val jsonBody = JSONObject()
            
            // Content setup
            val contentsArray = JSONArray()
            val userTurn = JSONObject()
            val userParts = JSONArray()
            userParts.put(JSONObject().put("text", promptText))
            userTurn.put("parts", userParts)
            contentsArray.put(userTurn)
            jsonBody.put("contents", contentsArray)

            // Configuration for JSON schema
            val generationConfig = JSONObject()
            generationConfig.put("responseMimeType", "application/json")
            
            // Build schema
            val responseSchema = JSONObject()
            responseSchema.put("type", "OBJECT")
            
            val properties = JSONObject()
            val mealsArray = JSONObject()
            mealsArray.put("type", "ARRAY")
            
            val mealItem = JSONObject()
            mealItem.put("type", "OBJECT")
            
            val itemProperties = JSONObject()
            itemProperties.put("mealType", JSONObject().put("type", "STRING").put("description", "Breakfast, Mid Morning, Lunch, Evening Snack, Dinner"))
            itemProperties.put("mealName", JSONObject().put("type", "STRING").put("description", "Appetizing names of Indian vegetarian dishes"))
            itemProperties.put("calories", JSONObject().put("type", "INTEGER"))
            itemProperties.put("proteinGrams", JSONObject().put("type", "NUMBER"))
            itemProperties.put("carbsGrams", JSONObject().put("type", "NUMBER"))
            itemProperties.put("fatGrams", JSONObject().put("type", "NUMBER"))
            
            mealItem.put("properties", itemProperties)
            mealItem.put("required", JSONArray().put("mealType").put("mealName").put("calories").put("proteinGrams").put("carbsGrams").put("fatGrams"))
            
            mealsArray.put("items", mealItem)
            properties.put("meals", mealsArray)
            responseSchema.put("properties", properties)
            responseSchema.put("required", JSONArray().put("meals"))
            
            generationConfig.put("responseSchema", responseSchema)
            jsonBody.put("generationConfig", generationConfig)

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Diet Generation failed: ${response.code} - ${response.body?.string()}")
                    return@withContext emptyList()
                }

                val responseStr = response.body?.string() ?: return@withContext emptyList()
                val responseJson = JSONObject(responseStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val textJson = parts.getJSONObject(0).optString("text")
                        
                        // Parse JSON text using Moshi
                        val adapter = moshi.adapter(GeminiDietResponse::class.java)
                        val parsed = adapter.fromJson(textJson)
                        
                        if (parsed != null) {
                            return@withContext parsed.meals.map {
                                Meal(
                                    date = "", // Filled by caller
                                    mealType = it.mealType,
                                    mealName = it.mealName,
                                    calories = it.calories,
                                    proteinGrams = it.proteinGrams,
                                    carbsGrams = it.carbsGrams,
                                    fatGrams = it.fatGrams,
                                    isCompleted = false
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating diet", e)
        }
        return@withContext emptyList()
    }
}
