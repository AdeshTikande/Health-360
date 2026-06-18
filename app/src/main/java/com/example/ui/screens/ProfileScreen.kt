package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HealthViewModel
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
    val historyState by viewModel.allActivitiesHistory.collectAsStateWithLifecycle()

    val profile = profileState ?: com.example.data.UserProfile()

    // Form states
    var editName by remember(profile.name) { mutableStateOf(profile.name) }
    var editAge by remember(profile.age) { mutableStateOf(profile.age.toString()) }
    var editHeight by remember(profile.heightCm) { mutableStateOf(profile.heightCm.toString()) }
    var editWeight by remember(profile.weightKg) { mutableStateOf(profile.weightKg.toString()) }
    
    var editGoal by remember(profile.goal) { mutableStateOf(profile.goal) }
    var editActivity by remember(profile.activityLevel) { mutableStateOf(profile.activityLevel) }

    var isEditStateEdited by remember { mutableStateOf(false) }

    // Validation Errors
    var profileNameError by remember { mutableStateOf<String?>(null) }
    var profileAgeError by remember { mutableStateOf<String?>(null) }
    var profileHeightError by remember { mutableStateOf<String?>(null) }
    var profileWeightError by remember { mutableStateOf<String?>(null) }

    // Notification Toggles
    var waterReminder by remember { mutableStateOf(true) }
    var mealReminder by remember { mutableStateOf(true) }
    var workoutReminder by remember { mutableStateOf(true) }
    var sleepReminder by remember { mutableStateOf(true) }
    var scoreReminder by remember { mutableStateOf(false) }

    // Dialog & Simulation states
    var showPlanDialog by remember { mutableStateOf(false) }
    var showPdfDialog by remember { mutableStateOf(false) }
    var pdfProcessing by remember { mutableStateOf(false) }
    var showAddFamilyDialog by remember { mutableStateOf(false) }
    var newFamilyMemberName by remember { mutableStateOf("") }
    
    val familyMembers = remember { mutableStateListOf("Adesh Tikande", "Priya K. (Sister)", "Shankar T. (Father)") }
    var wearableSynced by remember { mutableStateOf(false) }
    var wearableSyncing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Summary Card with BMI
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (profile.name.isNotEmpty()) profile.name.take(1).uppercase() else "A",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (profile.name.isNotEmpty()) profile.name else "Adesh Tikande",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val tierText = when (profile.subscriptionTier) {
                                    1 -> "PREMIUM"
                                    2 -> "PRO"
                                    else -> "FREE"
                                }
                                val tierBg = when (profile.subscriptionTier) {
                                    1 -> SleepPurple
                                    2 -> StreakOrangeRed
                                    else -> Color.Gray
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(tierBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(tierText, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "Age: ${profile.age} • ${profile.goal}",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFF1F1F1))
                    Spacer(modifier = Modifier.height(12.dp))

                    // BMI Diagnostic row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Body Mass Index (BMI)", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                "%.1f kg/m²".format(profile.bmi),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.DarkGray
                            )
                        }
                        
                        // Status indicator badge
                        val cat = profile.bmiCategory
                        val badgeBg = when (cat) {
                            "Normal (Healthy)" -> HealthGreen.copy(alpha = 0.12f)
                            "Underweight" -> SnackOrange.copy(alpha = 0.12f)
                            else -> FitnessRed.copy(alpha = 0.12f)
                        }
                        val badgeText = when (cat) {
                            "Normal (Healthy)" -> HealthGreen
                            "Underweight" -> SnackOrange
                            else -> FitnessRed
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeBg)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                color = badgeText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // WEEKLY STATUS ANALYSIS REPORT SECTION (Badges and aggregates)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Unified Weekly Health Analysis",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Your aggregate stats performance.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Historic performance stats
                    val daysTracked = historyState.size
                    val totalStepsSum = historyState.sumOf { it.steps }
                    val avgWater = if (daysTracked > 0) historyState.sumOf { it.waterGlasses }.toFloat() / daysTracked else 4f
                    val averageSleep = if (daysTracked > 0) historyState.sumOf { it.sleepDurationHours.toDouble() }.toFloat() / daysTracked else 8f

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Steps", fontSize = 11.sp, color = Color.Gray)
                            Text(String.format("%,d", totalStepsSum), fontWeight = FontWeight.Bold, color = DarkBlue, fontSize = 15.sp)
                        }
                        Column {
                            Text("Avg Hydration", fontSize = 11.sp, color = Color.Gray)
                            Text("%.1f glasses".format(avgWater), fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 15.sp)
                        }
                        Column {
                            Text("Avg Sleep", fontSize = 11.sp, color = Color.Gray)
                            Text("%.1f hrs/night".format(averageSleep), fontWeight = FontWeight.Bold, color = SleepPurple, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFFF1F1F1))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Achievement Badges Section
                    Text(
                        text = "Achievement Badges Unlocked",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray,
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BadgeItem(
                            badgeName = "7-Day Streak",
                            icon = Icons.Outlined.EmojiEvents,
                            iconTint = StreakOrangeRed,
                            isUnlocked = totalStepsSum > 5000 || daysTracked >= 1
                        )
                        BadgeItem(
                            badgeName = "Hydration Hero",
                            icon = Icons.Default.Opacity,
                            iconTint = PrimaryBlue,
                            isUnlocked = avgWater >= 6f
                        )
                        BadgeItem(
                            badgeName = "Sleep Master",
                            icon = Icons.Default.Bedtime,
                            iconTint = SleepPurple,
                            isUnlocked = averageSleep >= 7f
                        )
                    }
                }
            }
        }

        // Editable Form fields
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Edit Profile & Metric Parameters",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it; isEditStateEdited = true; profileNameError = null },
                        label = { Text("Display Name*") },
                        isError = profileNameError != null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("profile_name_edit"),
                        shape = RoundedCornerShape(10.dp),
                        supportingText = {
                            if (profileNameError != null) {
                                Text(text = profileNameError!!, color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                            }
                        }
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = editAge,
                                onValueChange = { editAge = it; isEditStateEdited = true; profileAgeError = null },
                                label = { Text("Age*") },
                                isError = profileAgeError != null,
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("profile_age_edit"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            if (profileAgeError != null) {
                                Text(text = profileAgeError!!, color = MaterialTheme.colorScheme.error, fontSize = 9.sp)
                            }
                        }
                        Column(modifier = Modifier.weight(1.3f)) {
                            OutlinedTextField(
                                value = editHeight,
                                onValueChange = { editHeight = it; isEditStateEdited = true; profileHeightError = null },
                                label = { Text("Height (cm)*") },
                                isError = profileHeightError != null,
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("profile_height_edit"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            if (profileHeightError != null) {
                                Text(text = profileHeightError!!, color = MaterialTheme.colorScheme.error, fontSize = 9.sp)
                            }
                        }
                        Column(modifier = Modifier.weight(1.3f)) {
                            OutlinedTextField(
                                value = editWeight,
                                onValueChange = { editWeight = it; isEditStateEdited = true; profileWeightError = null },
                                label = { Text("Weight (kg)*") },
                                isError = profileWeightError != null,
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("profile_weight_edit"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            if (profileWeightError != null) {
                                Text(text = profileWeightError!!, color = MaterialTheme.colorScheme.error, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isEditStateEdited) {
                        Button(
                            onClick = {
                                val cleanName = editName.trim()
                                val parsedAge = editAge.trim().toIntOrNull()
                                val parsedHeight = editHeight.trim().toFloatOrNull()
                                val parsedWeight = editWeight.trim().toFloatOrNull()
                                
                                profileNameError = if (cleanName.isEmpty()) "Required" else null
                                profileAgeError = if (editAge.trim().isEmpty()) "Required" else if (parsedAge == null || parsedAge !in 1..120) "1 to 120" else null
                                profileHeightError = if (editHeight.trim().isEmpty()) "Required" else if (parsedHeight == null || parsedHeight !in 50f..250f) "50 to 250" else null
                                profileWeightError = if (editWeight.trim().isEmpty()) "Required" else if (parsedWeight == null || parsedWeight !in 10f..300f) "10 to 300" else null
                                
                                if (profileNameError == null && profileAgeError == null && profileHeightError == null && profileWeightError == null) {
                                    viewModel.saveUserProfile(
                                        name = cleanName,
                                        age = parsedAge!!,
                                        height = parsedHeight!!,
                                        weight = parsedWeight!!,
                                        goal = editGoal,
                                        activityLevel = editActivity
                                    )
                                    isEditStateEdited = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_save_changes_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Physical Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Notification reminders settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Local Reminder Settings",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Toggle client triggers for health objectives.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ReminderSettingRow("Water Hydration Reminders", waterReminder) { waterReminder = it }
                    ReminderSettingRow("Structured Meal Reminders", mealReminder) { mealReminder = it }
                    ReminderSettingRow("Active Workout Reminders", workoutReminder) { workoutReminder = it }
                    ReminderSettingRow("Sleep Schedule Trackers", sleepReminder) { sleepReminder = it }
                    ReminderSettingRow("Daily Health Score Updates", scoreReminder) { scoreReminder = it }
                }
            }
        }

        // Subscriptions monetization upgrade tier Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (profile.subscriptionTier) {
                        1 -> SleepPurple.copy(alpha = 0.95f)
                        2 -> DarkBlue
                        else -> Color(0xFF1E293B)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Health360 Plan Status", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            val statusSub = when (profile.subscriptionTier) {
                                1 -> "Premium Active • Rs. 199/month"
                                2 -> "Pro Active • Rs. 499/month"
                                else -> "Free Plan • Limited AI consultations"
                            }
                            Text(statusSub, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (profile.subscriptionTier > 0) HealthGreen else StreakOrangeRed)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            val badgeLbl = when (profile.subscriptionTier) {
                                1 -> "PREMIUM"
                                2 -> "PRO"
                                else -> "UPGRADE"
                            }
                            Text(badgeLbl, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Plan Features Active:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (profile.subscriptionTier == 0) {
                        Text("• 3-Day Diet planning views only.", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                        Text("• Standard Workout routines.", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                        Text("• AI Chatbot restricted to 10 queries/day.", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                    } else if (profile.subscriptionTier == 1) {
                        Text("• Full 30-Day Diet Planner calendar unlocked.", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                        Text("• Standard, Intermediate & Advanced Workout levels.", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                        Text("• Unlimited conversational health coach interactions.", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                        Text("• Ad-Free application interface enabled.", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                    } else {
                        Text("• Everything in Premium Plan packages.", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                        Text("• AI Disease Risk predictions unlocked.", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                        Text("• Multi-account Family Plan support unlocked.", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                        Text("• Apple Watch / Fitbit hardware synchronization active.", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showPlanDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if (profile.isPremium) Color.White.copy(alpha = 0.2f) else HealthGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("toggle_premium_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (profile.isPremium) "Change / Manage Memberships" else "Upgrade Plans (Rs. 199 or 499)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // PRO EXCLUSIVE FEATURE: Family plan accounts
        item {
            val isPro = profile.subscriptionTier >= 2
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    if (!isPro) {
                        showPlanDialog = true
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Groups, contentDescription = null, tint = if (isPro) StreakOrangeRed else Color.Gray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Family Member Accounts (Pro)", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 14.sp)
                                Text("Add and keep track of up to 5 family members", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        if (!isPro) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        } else {
                            IconButton(onClick = { showAddFamilyDialog = true }) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Add Family Member", tint = StreakOrangeRed)
                            }
                        }
                    }

                    if (isPro) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFFF1F1F1))
                        Spacer(modifier = Modifier.height(8.dp))
                        familyMembers.forEach { name ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(name, fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = { familyMembers.remove(name) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = FitnessRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    } else {
                        Text(
                            "Upgrade to the Pro Plan to sync, track and optimize profiles for up to 5 family members under one unified pricing.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // PRO EXCLUSIVE FEATURE: Wearables device sync
        item {
            val isPro = profile.subscriptionTier >= 2
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    if (!isPro) {
                        showPlanDialog = true
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Watch, contentDescription = null, tint = if (isPro) PrimaryBlue else Color.Gray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Wearable Devices Integration", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 14.sp)
                                Text("Sync steps, sleep and pulse from hardware", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        if (!isPro) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }

                    if (isPro) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFFF1F1F1))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Linked Device: Google Fit Cloud Sync", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Text(
                                    text = if (wearableSynced) "Last synchronized: Just Now" else "Not synchronized today",
                                    fontSize = 11.sp,
                                    color = if (wearableSynced) HealthGreen else Color.Gray
                                )
                            }
                            
                            Button(
                                onClick = {
                                    wearableSyncing = true
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        wearableSyncing = false
                                        wearableSynced = true
                                    }, 1200)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !wearableSyncing
                            ) {
                                if (wearableSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 1.5.dp)
                                } else {
                                    Text("Sync Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Text(
                            "Sync with Google Fit, Fitbit, Garmin or Apple Health. Automatically imports logs into Health360 engines seamlessly.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // PRO EXCLUSIVE FEATURE: Export report as PDF
        item {
            val isPro = profile.subscriptionTier >= 2
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    if (isPro) {
                        showPdfDialog = true
                        pdfProcessing = true
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            pdfProcessing = false
                        }, 1300)
                    } else {
                        showPlanDialog = true
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, tint = if (isPro) HealthGreen else Color.Gray)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Export Health Dossier (PDF)", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 14.sp)
                            Text("Download clinical aggregate charts as PDF reports", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                    if (!isPro) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Developers & Institutional details
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "App version 1.0 (PROTOTYPE)", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    Text(text = "Developer Contact: Adeshtikande56@gmail.com", fontSize = 10.sp, color = Color.LightGray)
                    Text(text = "College Ref: KJEI's Trinity College, Pune", fontSize = 10.sp, color = Color.LightGray)
                }
            }
        }

        // Log out session
        item {
            Button(
                onClick = {
                    viewModel.logoutUserProfile()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("profile_logout_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Log out")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out Profile Session", fontWeight = FontWeight.Bold, color = Color.White)
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

    if (showPdfDialog) {
        Dialog(onDismissRequest = { showPdfDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(24.dp)),
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (pdfProcessing) {
                        Text("Exporting Health Portfolio PDF", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Compiling user profiles, weekly performance matrix and Indian recipe sets...", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("📄 Health360 PDF Report", fontWeight = FontWeight.Black, fontSize = 18.sp, color = DarkBlue)
                            IconButton(onClick = { showPdfDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        
                        // Fake PDF representation box
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("HEALTH360 MEDICAL DOSSIER", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.Gray, letterSpacing = 1.sp)
                            Text("• Patient: ${profile.name.ifEmpty { "Adesh Tikande" }}", fontSize = 12.sp, color = Color.DarkGray)
                            Text("• Diagnostics BMI: %.1f".format(profile.bmi), fontSize = 12.sp, color = Color.DarkGray)
                            Text("• Goal Prescription: ${profile.goal}", fontSize = 12.sp, color = Color.DarkGray)
                            Text("• Hydration Rating: Excellent (Avg: 7.2 glasses/day)", fontSize = 12.sp, color = Color.DarkGray)
                            Text("• Quality Sleep: Optimal (Avg: 8.1 hours/night)", fontSize = 12.sp, color = Color.DarkGray)
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HealthGreen.copy(alpha = 0.12f))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✅ APPROVED BY AI HEALTH COACH", color = HealthGreen, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showPdfDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Download PDF Copy", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAddFamilyDialog) {
        Dialog(onDismissRequest = { showAddFamilyDialog = false }) {
            var familyMemberError by remember { mutableStateOf<String?>(null) }
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(20.dp)),
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add Family Account (Pro Member)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkBlue)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newFamilyMemberName,
                        onValueChange = { newFamilyMemberName = it; familyMemberError = null },
                        placeholder = { Text("e.g. Priyanshi Tikande (Mother)") },
                        label = { Text("Name & Relation*") },
                        isError = familyMemberError != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            if (familyMemberError != null) {
                                Text(text = familyMemberError!!, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showAddFamilyDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val trimmed = newFamilyMemberName.trim()
                                if (trimmed.isEmpty()) {
                                    familyMemberError = "Family member name & relation is required"
                                } else {
                                    familyMembers.add(trimmed)
                                    newFamilyMemberName = ""
                                    showAddFamilyDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StreakOrangeRed),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Add Member")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeItem(
    badgeName: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    isUnlocked: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isUnlocked) Color(0xFFF6F8FA) else Color(0xFFF1F1F1))
            .border(1.dp, if (isUnlocked) iconTint.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) iconTint.copy(alpha = 0.12f) else Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isUnlocked) iconTint else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = badgeName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) Color.DarkGray else Color.Gray
        )
        Text(
            text = if (isUnlocked) "UNLOCKED" else "LOCKED",
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isUnlocked) iconTint else Color.LightGray,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun ReminderSettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryBlue,
                checkedTrackColor = Color(0xFFD0E0FF)
            )
        )
    }
}
