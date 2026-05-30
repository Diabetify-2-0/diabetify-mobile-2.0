package com.itb.diabetify.data.repository

import com.itb.diabetify.data.remote.chatbot.ChatbotApiService
import com.itb.diabetify.data.remote.chatbot.request.ChatRequest
import com.itb.diabetify.data.remote.chatbot.request.SessionStartRequest
import com.itb.diabetify.data.remote.chatbot.response.RecommendationResponse
import com.itb.diabetify.domain.model.ChatMessage
import com.itb.diabetify.domain.model.ChatRecommendation
import com.itb.diabetify.domain.repository.ChatbotRepository
import com.itb.diabetify.util.Resource
import okio.IOException
import retrofit2.HttpException
class ChatbotRepositoryImpl(
    private val chatbotApiService: ChatbotApiService,
) : ChatbotRepository {

    override suspend fun sendMessage(userId: String, message: String): Resource<String> {
        return try {
            val response = chatbotApiService.sendMessage(
                ChatRequest(userId = userId, message = message.trim())
            )
            Resource.Success(response.response)
        } catch (e: IOException) {
            Resource.Error(e.message ?: "Tidak dapat terhubung ke layanan chatbot")
        } catch (e: HttpException) {
            Resource.Error(parseHttpError(e))
        }
    }

    override suspend fun loadHistory(userId: String): Resource<List<ChatMessage>> {
        return try {
            val response = chatbotApiService.getHistory(userId = userId, limit = 20)
            val messages = mutableListOf<ChatMessage>()
            response.entries.forEach { entry ->
                messages.add(
                    ChatMessage(
                        id = "user-${entry.id}",
                        text = entry.message,
                        isFromUser = true,
                    )
                )
                entry.summary?.takeIf { it.isNotBlank() }?.let { summary ->
                    messages.add(
                        ChatMessage(
                            id = "bot-${entry.id}",
                            text = summary,
                            isFromUser = false,
                        )
                    )
                }
            }
            Resource.Success(messages)
        } catch (e: IOException) {
            Resource.Error(e.message ?: "Tidak dapat memuat riwayat chat")
        } catch (e: HttpException) {
            if (e.code() == 404) {
                Resource.Success(emptyList())
            } else {
                Resource.Error(parseHttpError(e))
            }
        }
    }

    override suspend fun startRecommendationSession(userId: String): Resource<ChatRecommendation> {
        return try {
            val response = chatbotApiService.startRecommendationSession(
                SessionStartRequest(userId = userId)
            )
            Resource.Success(response.toDomain())
        } catch (e: IOException) {
            Resource.Error(e.message ?: "Tidak dapat memuat saran pertanyaan")
        } catch (e: HttpException) {
            Resource.Error(parseHttpError(e))
        }
    }

    override suspend fun getRecommendations(userId: String): Resource<ChatRecommendation> {
        return try {
            val response = chatbotApiService.getRecommendations(userId = userId)
            Resource.Success(response.toDomain())
        } catch (e: IOException) {
            Resource.Error(e.message ?: "Tidak dapat memuat saran pertanyaan")
        } catch (e: HttpException) {
            if (e.code() == 404) {
                Resource.Error("not_found")
            } else {
                Resource.Error(parseHttpError(e))
            }
        }
    }

    override suspend fun loadRecommendations(userId: String): Resource<ChatRecommendation> {
        return when (val existing = getRecommendations(userId)) {
            is Resource.Success -> existing
            is Resource.Error -> {
                if (existing.message == "not_found") {
                    startRecommendationSession(userId)
                } else {
                    existing
                }
            }
            is Resource.Loading -> existing
        }
    }

    private fun RecommendationResponse.toDomain(): ChatRecommendation {
        val questions = listOf(question1, question2)
            .map { it.trim() }
            .filter { it.isNotBlank() }
        return ChatRecommendation(questions = questions)
    }

    private fun parseHttpError(exception: HttpException): String {
        return when (exception.code()) {
            422 -> "Pesan terlalu panjang (maks. 500 karakter)"
            503 -> "Chatbot sedang mempersiapkan layanan, coba lagi"
            else -> exception.message() ?: "Terjadi kesalahan pada layanan chatbot"
        }
    }
}
