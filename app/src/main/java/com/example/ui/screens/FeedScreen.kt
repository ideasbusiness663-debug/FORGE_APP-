package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.data.ChatMessage
import com.example.ui.FeedPost
import com.example.ui.ForgeViewModel
import com.example.ui.theme.ForgeCarbon
import com.example.ui.theme.ForgeFire
import com.example.ui.theme.ForgeGreen
import com.example.ui.theme.ForgeSteel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: ForgeViewModel,
    onNavigateToLog: () -> Unit
) {
    val feedList by viewModel.communityFeed.collectAsState(initial = emptyList())
    var selectedCategory by remember { mutableStateOf("All") }

    var activeChatPartner by remember { mutableStateOf<FeedPost?>(null) }
    var activeCallPartner by remember { mutableStateOf<FeedPost?>(null) }

    val categories = listOf("All", "Strength", "Cardio", "Yoga", "Core")

    val filteredFeed = if (selectedCategory == "All") {
        feedList
    } else {
        feedList.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Feed",
                            tint = ForgeFire,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "FORGE FEED",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = ForgeFire
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToLog,
                containerColor = ForgeFire,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_workout_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Workout", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Category Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) ForgeFire else ForgeSteel)
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (filteredFeed.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = ForgeSteel,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No workouts in this category yet.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Log a workout or complete one to start fueling the Forge community fire!",
                            fontSize = 12.sp,
                            color = Color.Gray.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredFeed, key = { it.id }) { post ->
                        FeedPostItem(
                            post = post,
                            onLikeToggle = { viewModel.toggleLike(post.id) },
                            onAddComment = { commentText -> viewModel.addComment(post.id, commentText) },
                            onChatClick = { activeChatPartner = post },
                            onCallClick = { activeCallPartner = post }
                        )
                    }
                }
            }
        }
    }

    // Overlay Screens
    activeChatPartner?.let { post ->
        PartnerChatDialog(
            post = post,
            onDismiss = { activeChatPartner = null }
        )
    }

    activeCallPartner?.let { post ->
        PartnerCallOverlay(
            post = post,
            onDismiss = { activeCallPartner = null }
        )
    }
}

@Composable
fun FeedPostItem(
    post: FeedPost,
    onLikeToggle: () -> Unit,
    onAddComment: (String) -> Unit,
    onChatClick: () -> Unit,
    onCallClick: () -> Unit
) {
    var isCommentSectionOpen by remember { mutableStateOf(false) }
    var commentInputText by remember { mutableStateOf("") }

    // Relative Time calculation helper
    val relativeTime = DateUtils.getRelativeTimeSpanString(
        post.timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("feed_post_${post.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Avatar, Name, Goal, Category Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular user avatar initials
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ForgeSteel),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.userName.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = ForgeFire,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = post.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Badge indicating category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ForgeFire.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = post.category.uppercase(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ForgeFire
                            )
                        }
                    }

                    Text(
                        text = post.userGoal,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Time
                Text(
                    text = relativeTime,
                    fontSize = 10.sp,
                    color = Color.Gray.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Workout Content Text
            Text(
                text = post.activityText,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp
            )

            // Duration details, if non-zero
            if (post.durationMinutes > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ForgeSteel)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = ForgeGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.durationMinutes} minutes duration",
                        fontSize = 11.sp,
                        color = ForgeGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = ForgeSteel, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Row: Like, Comment, Chat, Call
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Like Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onLikeToggle() }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) ForgeFire else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${post.likesCount}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (post.isLiked) ForgeFire else Color.LightGray
                        )
                    }

                    // Comment Trigger Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { isCommentSectionOpen = !isCommentSectionOpen }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${post.comments.size}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Chat Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ForgeSteel)
                            .clickable { onChatClick() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat",
                            tint = ForgeGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Chat",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Call Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ForgeFire)
                            .clickable { onCallClick() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Call",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Comments Panel
            if (isCommentSectionOpen) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = ForgeSteel, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // List existing comments
                post.comments.forEach { comment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "Athlete:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ForgeFire,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = comment,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Comment input box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentInputText,
                        onValueChange = { commentInputText = it },
                        placeholder = { Text("Write a comment...", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForgeFire,
                            unfocusedBorderColor = ForgeSteel,
                            focusedContainerColor = ForgeSteel,
                            unfocusedContainerColor = ForgeSteel
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (commentInputText.isNotBlank()) {
                                onAddComment(commentInputText.trim())
                                commentInputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(ForgeFire, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerChatDialog(
    post: FeedPost,
    onDismiss: () -> Unit
) {
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(sender = post.userName, text = "Hey gym buddy! Bol, kaisa rha aaj ka workout session? 💪")
            )
        )
    }
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ForgeSteel),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = post.userName.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = ForgeFire,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = post.userName,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Active • Gym Partner",
                                fontSize = 11.sp,
                                color = ForgeGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = ForgeSteel, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Messages area
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.sender == "User"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isUser) 16.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 16.dp
                                        )
                                    )
                                    .background(if (isUser) ForgeFire else ForgeSteel)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .widthIn(max = 240.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (isTyping) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(ForgeSteel)
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "${post.userName} is typing...",
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask your buddy anything...", fontSize = 13.sp, color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = Color.White),
                        shape = RoundedCornerShape(26.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForgeFire,
                            unfocusedBorderColor = ForgeSteel,
                            focusedContainerColor = ForgeSteel,
                            unfocusedContainerColor = ForgeSteel
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val userMsg = ChatMessage(sender = "User", text = inputText.trim())
                                messages = messages + userMsg
                                val sentText = inputText
                                inputText = ""
                                isTyping = true

                                coroutineScope.launch {
                                    delay(1000)
                                    val buddyReply = getBuddyReply(post.userName, sentText)
                                    messages = messages + ChatMessage(sender = post.userName, text = buddyReply)
                                    isTyping = false
                                }
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(ForgeFire, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

fun getBuddyReply(name: String, userMsg: String): String {
    val lower = userMsg.lowercase()
    return when {
        name.contains("Alex", ignoreCase = true) -> {
            when {
                lower.contains("hello") || lower.contains("hi") || lower.contains("hey") || lower.contains("yo") ->
                    "Yo bro! Bol, aaj heavy squats lagaye kya? Power mode humesha high rkhna!"
                lower.contains("diet") || lower.contains("protein") || lower.contains("eat") || lower.contains("kha") ->
                    "Bhai tagde bano! Oats, banana shake, and egg whites bulk me khao. Weight check rkhna!"
                lower.contains("tired") || lower.contains("thak") || lower.contains("pain") ->
                    "Pain is just weakness leaving the body! Warm up acche se karo, or heavy lifting start kr do!"
                else -> "Beast mode bro! Keep pushin'. Direct deadlift or squat pe heavy sets lga, backup ke baare m mat soch!"
            }
        }
        name.contains("Sarah", ignoreCase = true) -> {
            when {
                lower.contains("hello") || lower.contains("hi") || lower.contains("hey") || lower.contains("yo") ->
                    "Hi runner! Kaisa rha tera workout? Running and breathing sync m rkhna!"
                lower.contains("diet") || lower.contains("protein") || lower.contains("eat") || lower.contains("kha") ->
                    "Carbs backup jaruri hai long run k liye! Hydration or dry fruits check krte rho."
                lower.contains("tired") || lower.contains("thak") || lower.contains("pain") ->
                    "Bhai, 5 mins ka walking session kro, recovery dynamic stretching se hogi. Water intake badhao!"
                else -> "Perfect going, bro! Kal 10K running challenge sath m clear krenge. Let's get it!"
            }
        }
        name.contains("Marcus", ignoreCase = true) -> {
            when {
                lower.contains("hello") || lower.contains("hi") || lower.contains("hey") || lower.contains("yo") ->
                    "Yo gym buddy! Flex your muscles. Aaj biceps blast kiya ki chest press?"
                lower.contains("diet") || lower.contains("protein") || lower.contains("eat") || lower.contains("kha") ->
                    "Chicken breast, paneer, and whey protein hit kro! Hypertrophy needs high protein, bro!"
                lower.contains("tired") || lower.contains("thak") || lower.contains("pain") ->
                    "No pain, no gain bro! Muscles damage honge tabhi to recover hoke bade banenge! Muscle pump check kro!"
                else -> "Insane pump, bro! Consistency check kro. Flex your biceps right now, you are building a temple!"
            }
        }
        name.contains("Emma", ignoreCase = true) -> {
            when {
                lower.contains("hello") || lower.contains("hi") || lower.contains("hey") || lower.contains("yo") ->
                    "Namaste partner! Hope your mind and body are in deep harmony today."
                lower.contains("diet") || lower.contains("protein") || lower.contains("eat") || lower.contains("kha") ->
                    "Green veggies, healthy fats like almonds, and pure coconut water are best for cellular recovery."
                lower.contains("tired") || lower.contains("thak") || lower.contains("pain") ->
                    "Listen to your body. Take deep belly breaths and do child's pose. Healing takes time."
                else -> "Beautiful! Yoga is not about touching your toes, it's about what you learn on the way down. Flow strong!"
            }
        }
        else -> "Keep grinding gym brother! Target heavy lifting and strict discipline!"
    }
}

@Composable
fun PartnerCallOverlay(
    post: FeedPost,
    onDismiss: () -> Unit
) {
    var callState by remember { mutableStateOf("Ringing...") } // "Ringing...", "Connecting...", "Connected"
    var secondsElapsed by remember { mutableStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var callSubtitles by remember { mutableStateOf("[Ringing...]") }

    // Timer effect for call duration
    LaunchedEffect(callState) {
        if (callState == "Connected") {
            while (true) {
                delay(1000)
                secondsElapsed++
            }
        }
    }

    // Call state progression effect
    LaunchedEffect(Unit) {
        delay(1500)
        callState = "Connecting..."
        delay(1200)
        callState = "Connected"
    }

    // Update subtitles based on call seconds elapsed
    LaunchedEffect(secondsElapsed, callState) {
        if (callState != "Connected") {
            callSubtitles = if (callState == "Ringing...") "[Ringing...]" else "[Connecting...]"
        } else {
            val name = post.userName.split(" ").firstOrNull() ?: "Buddy"
            callSubtitles = when (secondsElapsed) {
                in 0..3 -> "$name: Yo gym bro! Kaisa h? Workout session heavy ja rha h na?"
                in 4..8 -> "$name: Sun, fatigue ko block kr! Ek step aur extra lgana h aaj!"
                in 9..14 -> "$name: Solid muscle connection build krna, no shortcut bro. Keep pushing!"
                in 15..20 -> "$name: Kal main or tu heavy leg press set sath m lgaenge! Taiyar rehna."
                in 21..27 -> "$name: Accha bro, coach bula rha h next set k liye. Grind mat rokna! Bye, let's get it!"
                else -> {
                    if (secondsElapsed > 28) {
                        onDismiss()
                    }
                    "$name: Stay strong, grind mode ON! Bye!"
                }
            }
        }
    }

    // Ripple wave animation effect for beautiful phone call visual UI
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0E0E10).copy(alpha = 0.95f))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                // Top Title
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 36.dp)
                ) {
                    Text(
                        text = "FORGE GYM GROUP CALL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ForgeFire,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = post.userName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (callState == "Connected") {
                            val mins = secondsElapsed / 60
                            val secs = secondsElapsed % 60
                            String.format("%02d:%02d", mins, secs)
                        } else {
                            callState
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (callState == "Connected") ForgeGreen else Color.LightGray
                    )
                }

                // Call Animated Avatar / Core
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing ripple ring
                    if (callState == "Connected" && !isMuted) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(ForgeFire.copy(alpha = pulseAlpha))
                        )
                    }

                    // Actual Avatar Core
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(ForgeCarbon),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = null,
                            tint = ForgeFire,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Subtitle Display for Partner Voice Captioning
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ForgeCarbon)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = ForgeGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = callSubtitles,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    // Mute Button
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(56.dp)
                            .background(if (isMuted) ForgeFire else ForgeSteel, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Hang up Button (Red)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFFE53935), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Hang Up",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Speaker Button
                    IconButton(
                        onClick = { isSpeakerOn = !isSpeakerOn },
                        modifier = Modifier
                            .size(56.dp)
                            .background(if (isSpeakerOn) ForgeGreen else ForgeSteel, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Speaker",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

