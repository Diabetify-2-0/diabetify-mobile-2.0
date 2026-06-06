package com.itb.diabetify.presentation.chatbot

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itb.diabetify.domain.model.ChatMessage
import com.itb.diabetify.domain.model.ChatStreamEvent
import com.itb.diabetify.domain.model.XaiProfile
import com.itb.diabetify.domain.usecases.chatbot.ChatbotUseCases
import com.itb.diabetify.domain.usecases.user.UserUseCases
import com.itb.diabetify.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.yield
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val chatbotUseCases: ChatbotUseCases,
    private val userUseCases: UserUseCases,
) : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessage>()
    val messagesList: SnapshotStateList<ChatMessage> get() = _messages

    /** Live token buffer per streaming bot message id — drives Compose recomposition. */
    private val _streamTexts = mutableStateMapOf<String, String>()
    val streamTexts: SnapshotStateMap<String, String> get() = _streamTexts

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

    private val _xaiProfile = mutableStateOf<XaiProfile?>(null)
    val xaiProfile: State<XaiProfile?> = _xaiProfile

    private val _isLoadingXai = mutableStateOf(false)
    val isLoadingXai: State<Boolean> = _isLoadingXai

    private val _showXaiSheet = mutableStateOf(false)
    val showXaiSheet: State<Boolean> = _showXaiSheet

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

    fun onXaiButtonClick() {
        _showXaiSheet.value = true
        val currentUserId = _userId.value ?: return
        if (_xaiProfile.value == null && !_isLoadingXai.value) {
            loadXaiProfile(currentUserId)
        }
    }

    fun onXaiDismiss() {
        _showXaiSheet.value = false
    }

    private fun loadXaiProfile(userId: String) {
        viewModelScope.launch {
            _isLoadingXai.value = true
            when (val result = chatbotUseCases.getXaiProfile(userId)) {
                is Resource.Success -> {
                    _xaiProfile.value = result.data
                }
                is Resource.Error -> {
                    // silently fail — XAI may not exist yet (no prediction done)
                }
                is Resource.Loading -> Unit
            }
            _isLoadingXai.value = false
        }
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
        _recommendedQuestions.value = emptyList()
        _isLoadingRecommendations.value = true

        val botMessageId = UUID.randomUUID().toString()
        _messages.add(
            ChatMessage(
                id = botMessageId,
                text = "",
                isFromUser = false,
                isStreaming = true,
            ),
        )

        viewModelScope.launch {
            chatbotUseCases.sendChatMessageStream(currentUserId, text)
                .flowOn(Dispatchers.IO)
                .catch { throwable ->
                    clearStreamText(botMessageId)
                    removeBotPlaceholder(botMessageId)
                    _errorMessage.value =
                        throwable.message ?: "Terjadi kesalahan pada layanan chatbot"
                    _isSending.value = false
                }
                .collect { event ->
                    when (event) {
                        is ChatStreamEvent.Chunk -> {
                            appendStreamDelta(botMessageId, event.delta)
                            yield()
                        }
                        is ChatStreamEvent.Done -> {
                            finishBotMessage(botMessageId)
                            _isSending.value = false
                            refreshRecommendations(currentUserId)
                        }
                        is ChatStreamEvent.Error -> {
                            clearStreamText(botMessageId)
                            removeBotPlaceholder(botMessageId)
                            _errorMessage.value = event.message
                            _isSending.value = false
                            _isLoadingRecommendations.value = false
                        }
                    }
                }
        }
    }

    private fun appendStreamDelta(messageId: String, delta: String) {
        if (delta.isEmpty()) return
        _streamTexts[messageId] = _streamTexts.getOrDefault(messageId, "") + delta
        val index = _messages.indexOfFirst { it.id == messageId }
        if (index < 0) return
        val current = _messages[index]
        val updatedText = _streamTexts[messageId].orEmpty()
        _messages[index] = current.copy(text = updatedText)
    }

    private fun finishBotMessage(messageId: String) {
        val index = _messages.indexOfFirst { it.id == messageId }
        if (index < 0) return
        val finalText = _streamTexts.remove(messageId)
            ?: _messages[index].text
        _messages[index] = _messages[index].copy(
            text = finalText,
            isStreaming = false,
        )
    }

    private fun clearStreamText(messageId: String) {
        _streamTexts.remove(messageId)
    }

    private fun removeBotPlaceholder(messageId: String) {
        clearStreamText(messageId)
        val index = _messages.indexOfFirst { it.id == messageId }
        if (index >= 0) {
            _messages.removeAt(index)
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
                        loadXaiProfile(resolvedId)
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
            _isLoadingRecommendations.value = true
            when (val result = chatbotUseCases.refreshRecommendations(userId)) {
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
}
