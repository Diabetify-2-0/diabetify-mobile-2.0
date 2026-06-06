package com.itb.diabetify.presentation.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.itb.diabetify.R
import com.itb.diabetify.domain.model.ChatMessage
import com.itb.diabetify.presentation.chatbot.components.RecommendationChips
import com.itb.diabetify.presentation.chatbot.components.XaiInfoSheet
import com.itb.diabetify.presentation.common.ErrorNotification
import com.itb.diabetify.ui.theme.poppinsFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    navController: NavController,
    viewModel: ChatbotViewModel = hiltViewModel(),
) {
    val messages = viewModel.messagesList
    val streamTexts = viewModel.streamTexts
    val inputText by viewModel.inputText
    val isSending by viewModel.isSending
    val isLoadingHistory by viewModel.isLoadingHistory
    val isLoadingRecommendations by viewModel.isLoadingRecommendations
    val recommendedQuestions by viewModel.recommendedQuestions
    val errorMessage by viewModel.errorMessage
    val xaiProfile by viewModel.xaiProfile
    val isLoadingXai by viewModel.isLoadingXai
    val showXaiSheet by viewModel.showXaiSheet
    val listState = rememberLazyListState()

    if (showXaiSheet) {
        XaiInfoSheet(
            xaiProfile = xaiProfile,
            isLoading = isLoadingXai,
            onDismiss = viewModel::onXaiDismiss,
        )
    }

    val lastMessage = messages.lastOrNull()
    val lastDisplayText = lastMessage?.let { message ->
        if (message.isStreaming) streamTexts[message.id].orEmpty() else message.text
    }
    LaunchedEffect(messages.size, lastDisplayText, isSending) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.white))
                .imePadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                colorResource(id = R.color.primary),
                                colorResource(id = R.color.primary).copy(alpha = 0.8f),
                            )
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White,
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Asisten Diabetify",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White,
                    )
                    Text(
                        text = "Didukung data XAI & RAG medis",
                        fontFamily = poppinsFontFamily,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }

                IconButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = viewModel::onXaiButtonClick,
                ) {
                    Icon(
                        imageVector = Icons.Filled.BarChart,
                        contentDescription = "Lihat analisis XAI",
                        tint = Color.White,
                    )
                }
            }

            if (isLoadingHistory) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorResource(id = R.color.primary))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(
                        count = messages.size,
                        key = { index -> messages[index].id },
                    ) { index ->
                        val message = messages[index]
                        val displayText = if (message.isStreaming) {
                            streamTexts[message.id].orEmpty()
                        } else {
                            message.text
                        }
                        ChatBubble(
                            message = message,
                            displayText = displayText,
                        )
                    }
                    val showTypingIndicator = isSending &&
                        messages.none { !it.isFromUser && it.isStreaming }
                    if (showTypingIndicator) {
                        item(key = "typing") {
                            TypingIndicator()
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7F8F8)),
            ) {
                if (!isLoadingHistory) {
                    RecommendationChips(
                        questions = recommendedQuestions,
                        isLoading = isLoadingRecommendations,
                        enabled = !isSending,
                        onQuestionClick = viewModel::onRecommendationClicked,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = viewModel::onInputChanged,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    placeholder = {
                        Text(
                            text = "Tulis pertanyaan Anda...",
                            fontFamily = poppinsFontFamily,
                            fontSize = 14.sp,
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    enabled = !isSending && !isLoadingHistory,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.primary),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                )

                IconButton(
                    onClick = { viewModel.sendMessage() },
                    enabled = inputText.isNotBlank() && !isSending && !isLoadingHistory,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    colorResource(id = R.color.primary),
                                    colorResource(id = R.color.primary).copy(alpha = 0.85f),
                                )
                            )
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_enter),
                        contentDescription = "Kirim",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
                }
            }
        }

        ErrorNotification(
            showError = errorMessage != null,
            errorMessage = errorMessage,
            onDismiss = { viewModel.onErrorShown() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1000f)
        )
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    displayText: String,
) {
    val alignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (message.isFromUser) {
        colorResource(id = R.color.primary)
    } else {
        Color(0xFFE8F0F3)
    }
    val textColor = if (message.isFromUser) Color.White else colorResource(id = R.color.primary)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment,
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp,
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                if (displayText.isNotEmpty()) {
                    Text(
                        text = displayText,
                        fontFamily = poppinsFontFamily,
                        fontSize = 14.sp,
                        color = textColor,
                        lineHeight = 20.sp,
                    )
                } else if (message.isStreaming) {
                    StreamingPlaceholder(color = textColor)
                }
                if (message.isStreaming && displayText.isNotEmpty()) {
                    StreamingCursor(color = textColor)
                }
            }
        }
    }
}

@Composable
private fun StreamingPlaceholder(color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = color,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Mengetik...",
            fontFamily = poppinsFontFamily,
            fontSize = 13.sp,
            color = color,
        )
    }
}

@Composable
private fun StreamingCursor(color: Color) {
    val transition = rememberInfiniteTransition(label = "stream_cursor")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cursor_alpha",
    )
    Text(
        text = "▍",
        modifier = Modifier.padding(start = 2.dp),
        fontFamily = poppinsFontFamily,
        fontSize = 14.sp,
        color = color.copy(alpha = alpha),
    )
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0F3)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = colorResource(id = R.color.primary),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mengetik...",
                    fontFamily = poppinsFontFamily,
                    fontSize = 13.sp,
                    color = colorResource(id = R.color.primary),
                )
            }
        }
    }
}
