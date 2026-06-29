package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.ui.ForgeViewModel
import com.example.ui.theme.ForgeCarbon
import com.example.ui.theme.ForgeFire
import com.example.ui.theme.ForgeGreen
import com.example.ui.theme.ForgeSteel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ForgeViewModel
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isTyping by viewModel.isCoachTyping.collectAsState()
    val focusManager = LocalFocusManager.current

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom of conversation
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Coach",
                            tint = ForgeFire,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "FORGE COACH AI",
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "High-Intensity Advisor • Active",
                                color = ForgeGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = ForgeFire
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Chat Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(msg = msg)
                }

                if (isTyping) {
                    item {
                        CoachTypingIndicator()
                    }
                }
            }

            // Input Bar
            Surface(
                color = ForgeCarbon,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask Coach about split routines, macro targets, or posture...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForgeFire,
                            unfocusedBorderColor = ForgeSteel,
                            focusedContainerColor = ForgeSteel,
                            unfocusedContainerColor = ForgeSteel
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessageToCoach(inputText.trim())
                                inputText = ""
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(ForgeFire, RoundedCornerShape(22.dp))
                            .testTag("send_chat_button"),
                        enabled = inputText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send message",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.sender == "User"

    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) ForgeFire else ForgeSteel
    val contentColor = Color.White
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        // Optional Sender Tag
        Text(
            text = if (isUser) "YOU (ATHLETE)" else "COACH",
            color = if (isUser) Color.Gray else ForgeFire,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )

        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(shape)
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Column {
                // Parse basic double-asterisk bold characters "**"
                val rawText = msg.text
                val parts = rawText.split("**")
                if (parts.size > 1) {
                    FlowRowText(parts = parts, color = contentColor)
                } else {
                    Text(
                        text = rawText,
                        color = contentColor,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FlowRowText(parts: List<String>, color: Color) {
    // Basic Markdown helper to render bold blocks correctly inline or block-based
    Column {
        var isBold = false
        parts.forEach { part ->
            if (part.isNotBlank()) {
                val cleanedText = part.trim()
                if (cleanedText.startsWith("-") || cleanedText.startsWith("*")) {
                    // Line bullet item
                    Text(
                        text = "• ${cleanedText.substring(1).trim()}",
                        color = color,
                        fontSize = 13.sp,
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                } else {
                    Text(
                        text = part,
                        color = if (isBold) ForgeFire else color,
                        fontSize = 13.sp,
                        fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Normal,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
            isBold = !isBold // Alternate bold state
        }
    }
}

@Composable
fun CoachTypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp))
                .background(ForgeSteel)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    color = ForgeFire,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Coach is reviewing targets...",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
