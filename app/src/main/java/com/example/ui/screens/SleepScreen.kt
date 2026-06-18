package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyActivity
import com.example.ui.HealthViewModel
import com.example.ui.theme.*
import java.time.LocalDate

@Composable
fun SleepScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val activityState by viewModel.dailyActivity.collectAsStateWithLifecycle()
    val historyState by viewModel.allActivitiesHistory.collectAsStateWithLifecycle()
    val userProfileState by viewModel.userProfile.collectAsStateWithLifecycle()

    val isFree = (userProfileState?.subscriptionTier ?: 0) == 0
    var showPlanDialog by remember { mutableStateOf(false) }

    val currentSleep = activityState ?: DailyActivity("Today")

    // Picker values
    var bedTimeHours by remember(currentSleep.sleepBedTime) { 
        mutableStateOf(currentSleep.sleepBedTime.split(":").getOrNull(0) ?: "22") 
    }
    var bedTimeMinutes by remember(currentSleep.sleepBedTime) { 
        mutableStateOf(currentSleep.sleepBedTime.split(":").getOrNull(1) ?: "00") 
    }
    var wakeTimeHours by remember(currentSleep.sleepWakeTime) { 
        mutableStateOf(currentSleep.sleepWakeTime.split(":").getOrNull(0) ?: "06") 
    }
    var wakeTimeMinutes by remember(currentSleep.sleepWakeTime) { 
        mutableStateOf(currentSleep.sleepWakeTime.split(":").getOrNull(1) ?: "00") 
    }

    var isTimeEdited by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sleep Hero Title
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SleepPurple),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Sleep & Recovery",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Log sleeping schedules and track sleep qualities. Steady sleep cycles elevate muscle repair and mental scores.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Duration Auto-Calculator
        item {
            val loggedDuration = currentSleep.sleepDurationHours
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = "Sleep Duration Recorded",
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Based on bedtime & wake settings below.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Conditional smart sleep advice
                        val feedback = when {
                            loggedDuration < 6.0f -> "Under 6 hrs limit. Your muscles and nervous repair systems need longer rest stages. Aim for 7+ hours!"
                            loggedDuration in 6.0f..7.0f -> "Mildly low. Your circadian rhythms are solid, but an extra hour will boost daily cognitive speeds."
                            loggedDuration in 7.0f..9.0f -> "Ideal Sleep! Excellent repair. Your cardiovascular and metabolic indexes are fully optimized."
                            else -> "Prone to hypersomnia. Aim for continuous physical workouts and balanced sleeping consistency."
                        }
                        Text(
                            text = feedback,
                            color = SleepPurple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(SleepPurple.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Bedtime, contentDescription = null, tint = SleepPurple, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "%.1f hrs".format(loggedDuration),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        // Time log editor Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Modify Sleep Logs",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Click hours/minutes to adjust bedtime and wake cycles.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Bedtime settings Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Nightlight, contentDescription = null, tint = SnackOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bedtime", fontWeight = FontWeight.SemiBold, color = Color.DarkGray, fontSize = 14.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InputSlot(value = bedTimeHours, onIncrease = {
                                val hr = (bedTimeHours.toInt() + 1).coerceIn(0, 23)
                                bedTimeHours = "%02d".format(hr)
                                isTimeEdited = true
                            }, onDecrease = {
                                val hr = (bedTimeHours.toInt() - 1).coerceIn(0, 23)
                                bedTimeHours = "%02d".format(hr)
                                isTimeEdited = true
                            })
                            Text(":", modifier = Modifier.padding(horizontal = 4.dp), fontWeight = FontWeight.Bold)
                            InputSlot(value = bedTimeMinutes, onIncrease = {
                                var min = bedTimeMinutes.toInt() + 15
                                if (min >= 60) min = 0
                                bedTimeMinutes = "%02d".format(min)
                                isTimeEdited = true
                            }, onDecrease = {
                                var min = bedTimeMinutes.toInt() - 15
                                if (min < 0) min = 45
                                bedTimeMinutes = "%02d".format(min)
                                isTimeEdited = true
                            })
                        }
                    }

                    // Wake Time settings Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.WbSunny, contentDescription = null, tint = SnackOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Wake Time", fontWeight = FontWeight.SemiBold, color = Color.DarkGray, fontSize = 14.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InputSlot(value = wakeTimeHours, onIncrease = {
                                val hr = (wakeTimeHours.toInt() + 1).coerceIn(0, 23)
                                wakeTimeHours = "%02d".format(hr)
                                isTimeEdited = true
                            }, onDecrease = {
                                val hr = (wakeTimeHours.toInt() - 1).coerceIn(0, 23)
                                wakeTimeHours = "%02d".format(hr)
                                isTimeEdited = true
                            })
                            Text(":", modifier = Modifier.padding(horizontal = 4.dp), fontWeight = FontWeight.Bold)
                            InputSlot(value = wakeTimeMinutes, onIncrease = {
                                var min = wakeTimeMinutes.toInt() + 15
                                if (min >= 60) min = 0
                                wakeTimeMinutes = "%02d".format(min)
                                isTimeEdited = true
                            }, onDecrease = {
                                var min = wakeTimeMinutes.toInt() - 15
                                if (min < 0) min = 45
                                wakeTimeMinutes = "%02d".format(min)
                                isTimeEdited = true
                            })
                        }
                    }

                    // Save Button
                    if (isTimeEdited) {
                        Button(
                            onClick = {
                                viewModel.updateSleep(
                                    bedTime = "$bedTimeHours:$bedTimeMinutes",
                                    wakeTime = "$wakeTimeHours:$wakeTimeMinutes",
                                    quality = currentSleep.sleepQualityRating
                                )
                                isTimeEdited = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleepPurple),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .testTag("save_sleep_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Sleeping Hours", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quality rating stars
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sleep Quality Rating",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Rate your sleeping satisfaction from 1 to 5 stars.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (star in 1..5) {
                            val active = star <= currentSleep.sleepQualityRating
                            IconButton(
                                onClick = {
                                    viewModel.updateSleep(
                                        bedTime = currentSleep.sleepBedTime,
                                        wakeTime = currentSleep.sleepWakeTime,
                                        quality = star
                                    )
                                },
                                modifier = Modifier.testTag("sleep_star_$star")
                            ) {
                                Icon(
                                    imageVector = if (active) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Rating $star",
                                    tint = if (active) SnackOrange else Color.LightGray,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dynamic Chart: Sleep history bar chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Weekly Sleep History",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Your auto-calculated night durations (hrs) over dates.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (isFree) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                .clickable { showPlanDialog = true }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Premium Lock",
                                    tint = SleepPurple,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "📊 Unlock Weekly sleep analytics",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = DarkBlue
                                )
                                Text(
                                    text = "Analyze weekly sleep cycles & quality profiles. Click to upgrade.",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    } else {
                        SleepHistoryBarChart(historyState.takeLast(7))
                    }
                }
            }
        }

        if (isFree) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { showPlanDialog = true },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Ads", tint = StreakOrangeRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("HEALTH360 ADS • Free Tier Active", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                                Text("Remove ads & unlock full 30-day AI plans!", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                            }
                        }
                        Text("UPGRADE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = PrimaryBlue, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }

    if (showPlanDialog) {
        PlanSelectionDialog(
            currentTier = userProfileState?.subscriptionTier ?: 0,
            onDismiss = { showPlanDialog = false },
            onSelectPlan = { tier ->
                viewModel.setSubscriptionTier(tier)
            }
        )
    }
}

@Composable
fun InputSlot(
    value: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .background(Color(0xFFF9FBFD))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        IconButton(onClick = onDecrease, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
        }
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            fontSize = 15.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        IconButton(onClick = onIncrease, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun SleepHistoryBarChart(history: List<DailyActivity>) {
    // Canvas drawn chart
    val sleepDays = if (history.isEmpty()) {
        listOf(
            DailyActivity("Mon", sleepBedTime = "23:00", sleepWakeTime = "06:00"),
            DailyActivity("Tue", sleepBedTime = "22:00", sleepWakeTime = "06:30"),
            DailyActivity("Wed", sleepBedTime = "00:00", sleepWakeTime = "06:00"),
            DailyActivity("Thu", sleepBedTime = "22:30", sleepWakeTime = "06:00"),
            DailyActivity("Fri", sleepBedTime = "22:00", sleepWakeTime = "06:00"),
            DailyActivity("Sat", sleepBedTime = "21:00", sleepWakeTime = "06:00"),
            DailyActivity("Sun", sleepBedTime = "22:00", sleepWakeTime = "06:12")
        )
    } else {
        history
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(horizontal = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barCount = sleepDays.size
            val spacing = 20.dp.toPx()
            val totalSpaces = barCount - 1
            val barWidth = (canvasWidth - (spacing * totalSpaces)) / barCount

            val maxSleepVal = 10f // Peak hours shown on chart

            sleepDays.forEachIndexed { index, activity ->
                val duration = activity.sleepDurationHours.coerceIn(0f, maxSleepVal)
                val barHeight = (duration / maxSleepVal) * (canvasHeight - 24.dp.toPx())
                
                val x = index * (barWidth + spacing)
                val y = canvasHeight - 24.dp.toPx() - barHeight

                // Draw Bar
                drawRoundRect(
                    color = SleepPurple,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Label below
                val dayLabel = try {
                    val date = LocalDate.parse(activity.date)
                    date.dayOfWeek.name.take(3)
                } catch (e: Exception) {
                    activity.date.take(3)
                }

                drawContext.canvas.nativeCanvas.drawText(
                    dayLabel,
                    x + (barWidth / 2f) - 10.dp.toPx(),
                    canvasHeight - 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 10.sp.toPx()
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                    }
                )

                // Write duration text above bar
                drawContext.canvas.nativeCanvas.drawText(
                    "%.1f".format(activity.sleepDurationHours),
                    x + (barWidth / 2f) - 8.dp.toPx(),
                    y - 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 9.sp.toPx()
                        typeface = android.graphics.Typeface.DEFAULT
                    }
                )
            }
        }
    }
}
