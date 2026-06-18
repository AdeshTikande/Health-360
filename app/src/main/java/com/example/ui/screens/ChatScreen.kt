package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HealthViewModel
import com.example.ui.theme.*

@Composable
fun ChatScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val isFree = (userProfile?.subscriptionTier ?: 0) == 0
    val userMessagesCount = messages.count { it.sender == "user" }
    val chatLimitReached = isFree && userMessagesCount >= 10
    var showPlanDialog by remember { mutableStateOf(false) }

    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when messages load
    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat Header Informational banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Unified AI Wellness Coach",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    Text(
                        text = "Ask health, exercise, sleep, or Indian vegetarian recipe guidelines.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Conversational list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No prior advice sessions",
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                            Text(
                                "Hi ${userProfile?.name ?: "Adesh"}! Ask me anything regarding your wellness goal of '${userProfile?.goal ?: "Stay Healthy"}'.",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                            )
                        }
                    }
                }
            } else {
                items(messages) { msg ->
                    ChatBubble(message = msg)
                }
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("chatbot_typing_spinner"),
                            strokeWidth = 2.dp,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Health360 Coach is typing...",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Bottom Input Toolbar
        if (isFree) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
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

        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            if (chatLimitReached) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .imePadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "🔒 AI Chat Limit Reached (10/10 messages)",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )
                    Text(
                        "You have compiled 10 user chat messages. Upgrade to Premium for continuous, unlimited access to your personal AI Coach!",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { showPlanDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Upgrade to Premium", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Clear History Option
                    IconButton(
                        onClick = { viewModel.clearChatHistory() },
                        modifier = Modifier
                            .background(Color(0xFFF1F1F1), CircleShape)
                            .testTag("chatbot_clear_history"),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.DarkGray)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear Chat History")
                    }

                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("Ask your health coach...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chatbot_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            val txt = inputMessage
                            inputMessage = ""
                            viewModel.sendUserChatMessage(txt)
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                            .testTag("chatbot_send_button"),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send Message")
                    }
                }
            }
        }
    }

    if (showPlanDialog) {
        PlanSelectionDialog(
            currentTier = userProfile?.subscriptionTier ?: 0,
            onDismiss = { showPlanDialog = false },
            onSelectPlan = { tier ->
                viewModel.setSubscriptionTier(tier)
            }
        )
    }
}

@Composable
fun ChatBubble(message: com.example.data.ChatMessage) {
    val isUser = message.sender == "user"
    
    Row(
        modifier = Modifier.fillMaxWidth().testTag("chat_bubble_${message.sender}"),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 270.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) PrimaryBlue else Color(0xFFF1F4F9))
                .padding(12.dp)
        ) {
            Text(
                text = message.message,
                color = if (isUser) Color.White else Color.DarkGray,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(HealthGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
