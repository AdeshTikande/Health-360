package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HealthViewModel
import com.example.ui.theme.*

@Composable
fun FitnessScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val activityState by viewModel.dailyActivity.collectAsStateWithLifecycle()
    val totalCaloriesBurned = activityState?.caloriesBurned ?: 0
    val userProfileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val profile = userProfileState ?: com.example.data.UserProfile()

    var selectedLevel by remember { mutableStateOf("Beginner") } // Beginner, Intermediate, Advanced
    var showPlanDialog by remember { mutableStateOf(false) }

    // Keep track of which card indices are expanded
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }
    // Keep track of which workout names are completed (local state in case user completes multiple times or reset)
    val completedWorkouts = remember { mutableStateMapOf<String, Boolean>() }

    val calorieMultiplier = when (selectedLevel) {
        "Intermediate" -> 1.5f
        "Advanced" -> 2.0f
        else -> 1.0f
    }

    val workouts = listOf(
        WorkoutTemplate(
            id = "morning_cardio",
            title = "Morning Cardio Blast",
            icon = Icons.Default.DirectionsRun,
            durationMins = 20,
            baseCalories = 120,
            setsAndReps = "3 sets x 30s High Knees, Jumping Jacks & Burpees",
            description = "Ideal for morning routine to ramp up your cardiovascular health, improve lung volume, and accelerate daily calorie expenditure."
        ),
        WorkoutTemplate(
            id = "core_strength",
            title = "Core Strength & Stability",
            icon = Icons.Default.FitnessCenter,
            durationMins = 15,
            baseCalories = 100,
            setsAndReps = "3 sets x 12 Plank Shoulder Taps, Bicycle Crunches & Russian Twists",
            description = "Build solid postural support, stabilize lower lumbar muscles, and strengthen core abdominal walls."
        ),
        WorkoutTemplate(
            id = "yoga_vinyasa",
            title = "Vinyasa Yoga & Balance",
            icon = Icons.Default.SelfImprovement,
            durationMins = 25,
            baseCalories = 80,
            setsAndReps = "Hold for 5 breaths: Sun Salutations, Warrior II & Tree Pose",
            description = "Align spinal posture, cultivate mental clarity, improve balance coordinates, and deeply stretch tense muscles."
        ),
        WorkoutTemplate(
            id = "evening_walk",
            title = "Evening Recovery Walk",
            icon = Icons.Default.DirectionsWalk,
            durationMins = 30,
            baseCalories = 90,
            setsAndReps = "Steady brisk pacing with comfortable footwear",
            description = "Relieve joint stress, improve blood circulation after long sedentary shifts, and prepare your body for deep night sleep."
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FitnessRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Fitness Routines",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Complete pre-configured sessions to track active energy burned. Adjust difficulty scales dynamically below.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // LEVEL SELECTOR Segmented Control
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Adjust Workout Difficulty",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Changes calorie outcomes and set guidelines.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0F4F8))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Beginner", "Intermediate", "Advanced").forEach { level ->
                            val isSelected = selectedLevel == level
                            val isLocked = level != "Beginner" && profile.subscriptionTier == 0
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) FitnessRed else Color.Transparent)
                                    .clickable {
                                        if (isLocked) {
                                            showPlanDialog = true
                                        } else {
                                            selectedLevel = level
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = level,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color.White else Color.DarkGray
                                    )
                                    if (isLocked) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's burned card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = FitnessRed, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Total Burned Today", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 14.sp)
                            Text("Active exercises tracked", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                    Text(
                        text = "$totalCaloriesBurned kcal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = FitnessRed
                    )
                }
            }
        }

        // List Header
        item {
            Text(
                text = "Interactive Workout Library",
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Loop Workout cards
        items(workouts) { workout ->
            val isExpanded = expandedStates[workout.id] ?: false
            val isCompleted = completedWorkouts[workout.id] ?: false
            val calories = (workout.baseCalories * calorieMultiplier).toInt()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("workout_card_${workout.id}")
                    .clickable { expandedStates[workout.id] = !isExpanded },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCompleted) FitnessRed.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isCompleted) FitnessRed.copy(alpha = 0.3f) else Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCompleted) FitnessRed else Color(0xFFFFECEB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = workout.icon,
                                    contentDescription = null,
                                    tint = if (isCompleted) Color.White else FitnessRed
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = workout.title,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${workout.durationMins} mins • $selectedLevel",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$calories",
                                fontWeight = FontWeight.Bold,
                                color = FitnessRed,
                                fontSize = 18.sp
                            )
                            Text(text = "kcal", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    // Expanded Section
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Divider(color = Color(0xFFF1F1F1), modifier = Modifier.padding(bottom = 12.dp))
                            
                            Text(
                                text = "GUIDELINES",
                                fontWeight = FontWeight.SemiBold,
                                color = FitnessRed,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = workout.setsAndReps,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                            )

                            Text(
                                text = "ABOUT THIS ROUTINE",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = workout.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                            )

                            // Action completion Button
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Button(
                                    onClick = {
                                        completedWorkouts[workout.id] = !isCompleted
                                        if (!isCompleted) {
                                            viewModel.burnCalories(calories)
                                        } else {
                                            viewModel.burnCalories(-calories)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCompleted) Color(0xFFCCCCCC) else FitnessRed
                                    ),
                                    modifier = Modifier.testTag("workout_complete_button_${workout.id}"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCompleted) Icons.Default.Cancel else Icons.Default.Check,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isCompleted) "Completed (Tap to undo)" else "Mark Routine Complete",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPlanDialog) {
        PlanSelectionDialog(
            currentTier = profile.subscriptionTier,
            onDismiss = { showPlanDialog = false },
            onSelectPlan = { tier ->
                viewModel.setSubscriptionTier(tier)
            }
        )
    }
}

data class WorkoutTemplate(
    val id: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val durationMins: Int,
    val baseCalories: Int,
    val setsAndReps: String,
    val description: String
)
