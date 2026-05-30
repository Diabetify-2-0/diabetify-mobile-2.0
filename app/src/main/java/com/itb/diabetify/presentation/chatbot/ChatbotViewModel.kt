package com.itb.diabetify.presentation.chatbot

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itb.diabetify.domain.model.ChatMessage
import com.itb.diabetify.domain.usecases.chatbot.ChatbotUseCases
import com.itb.diabetify.domain.usecases.user.UserUseCases
import com.itb.diabetify.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val chatbotUseCases: ChatbotUseCases,
    private val userUseCases: UserUseCases,
) : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    private val _inputText = mutableStateOf("")
    val inputText: State<String> = _inputText

    private val _isSending = mutableStateOf(false)
    val isSending: State<Boolean> = _isSending

    private val _isLoadingHistory = mutableStateOf(true)
    val isLoadingHistory: State<Boolean> = _isLoadingHistory

    private val _isLoadingRecommendations = mutableStateOf(false)
    val isLoadingRecommendations: State<Boolean> = _isLoadingRecommendations

    private val _recommendedQuestions = mutableStateOf<List<String>>(emptyList())
    val recommendedQuestions: State<List<String>> = _recommendedQuestions

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _userId = mutableStateOf<String?>(null)
    val userId: State<String?> = _userId

    private val welcomeMessage = ChatMessage(
        id = "welcome",
        text = "Halo! Saya asisten Diabetify. Tanyakan seputar diabetes, risiko, atau hasil XAI Anda — jawaban akan disesuaikan dengan profil risiko terbaru.",
        isFromUser = false,
    )

    init {
        _messages.add(welcomeMessage)
        resolveUserAndLoadHistory()
    }

    fun onInputChanged(value: String) {
        if (value.length <= 500) {
            _inputText.value = value
        }
    }

    fun onErrorShown() {
        _errorMessage.value = null
    }

    fun onRecommendationClicked(question: String) {
        if (_isSending.value || question.isBlank()) return
        sendMessage(question)
    }

    fun sendMessage(overrideText: String? = null) {
        val text = (overrideText ?: _inputText.value).trim()
        val currentUserId = _userId.value
        if (text.isBlank() || currentUserId.isNullOrBlank() || _isSending.value) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            isFromUser = true,
        )
        _messages.add(userMessage)
        _inputText.value = ""
        _isSending.value = true

        viewModelScope.launch {
            when (val result = chatbotUseCases.sendChatMessage(currentUserId, text)) {
                is Resource.Success -> {
                    _messages.add(
                        ChatMessage(
                            id = UUID.randomUUID().toString(),
                            text = result.data ?: "",
                            isFromUser = false,
                        )
                    )
                    refreshRecommendations(currentUserId)
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                }
                is Resource.Loading -> Unit
            }
            _isSending.value = false
        }
    }

    private fun resolveUserAndLoadHistory() {
        viewModelScope.launch {
            val fetchResult = userUseCases.getUser().result
            when (fetchResult) {
                is Resource.Success -> {
                    val cachedUser = userUseCases.getUserRepository().firstOrNull()
                    val resolvedId = cachedUser?.id?.toString()?.takeIf { it.isNotBlank() }
                        ?: cachedUser?.email?.takeIf { it.isNotBlank() }
                    _userId.value = resolvedId
                    if (!resolvedId.isNullOrBlank()) {
                        loadHistory(resolvedId)
                        loadRecommendations(resolvedId)
                    } else {
                        _isLoadingHistory.value = false
                        _errorMessage.value = "Profil pengguna tidak ditemukan"
                    }
                }
                is Resource.Error -> {
                    _isLoadingHistory.value = false
                    _errorMessage.value = fetchResult.message
                }
                else -> {
                    _isLoadingHistory.value = false
                    _errorMessage.value = "Terjadi kesalahan saat mengambil data pengguna"
                }
            }
        }
    }

    private suspend fun loadHistory(userId: String) {
        when (val result = chatbotUseCases.loadChatHistory(userId)) {
            is Resource.Success -> {
                val history = result.data.orEmpty()
                if (history.isNotEmpty()) {
                    _messages.clear()
                    _messages.addAll(history)
                }
            }
            is Resource.Error -> Unit
            is Resource.Loading -> Unit
        }
        _isLoadingHistory.value = false
    }

    private fun loadRecommendations(userId: String) {
        viewModelScope.launch {
            _isLoadingRecommendations.value = true
            when (val result = chatbotUseCases.loadRecommendations(userId)) {
                is Resource.Success -> {
                    _recommendedQuestions.value = result.data?.questions.orEmpty()
                }
                is Resource.Error -> {
                    _recommendedQuestions.value = emptyList()
                }
                is Resource.Loading -> Unit
            }
            _isLoadingRecommendations.value = false
        }
    }

    private fun refreshRecommendations(userId: String) {
        viewModelScope.launch {
            when (val result = chatbotUseCases.refreshRecommendations(userId)) {
                is Resource.Success -> {
                    val questions = result.data?.questions.orEmpty()
                    if (questions.isNotEmpty()) {
                        _recommendedQuestions.value = questions
                    }
                }
                is Resource.Error -> Unit
                is Resource.Loading -> Unit
            }
        }
    }
}
