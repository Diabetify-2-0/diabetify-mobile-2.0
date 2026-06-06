package com.itb.diabetify.data.repository

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.itb.diabetify.data.remote.chatbot.ChatbotApiService
import com.itb.diabetify.data.remote.chatbot.request.ChatRequest
import com.itb.diabetify.data.remote.chatbot.request.SessionStartRequest
import com.itb.diabetify.data.remote.chatbot.response.RecommendationResponse
import com.itb.diabetify.data.remote.chatbot.response.XaiFeatureResponse
import com.itb.diabetify.data.remote.chatbot.response.XaiProfileResponse
import com.itb.diabetify.domain.model.ChatMessage
import com.itb.diabetify.domain.model.ChatRecommendation
import com.itb.diabetify.domain.model.ChatStreamEvent
import com.itb.diabetify.domain.model.XaiFeature
import com.itb.diabetify.domain.model.XaiProfile
import com.itb.diabetify.domain.repository.ChatbotRepository
import com.itb.diabetify.util.Constants.CHATBOT_BASE_URL
import com.itb.diabetify.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class ChatbotRepositoryImpl(
    private val chatbotApiService: ChatbotApiService,
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
) : ChatbotRepository {

    private val streamClient: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
    }

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

    override fun sendMessageStream(userId: String, message: String): Flow<ChatStreamEvent> {
        return flow {
            val bodyJson = gson.toJson(ChatRequest(userId = userId, message = message.trim()))
            val request = Request.Builder()
                .url("${CHATBOT_BASE_URL}api/v1/chat/stream")
                .post(bodyJson.toRequestBody(JSON_MEDIA_TYPE))
                .header("Accept", "text/event-stream")
                .header("Cache-Control", "no-cache")
                .build()

            val call = streamClient.newCall(request)
            try {
                val response = try {
                    call.execute()
                } catch (e: IOException) {
                    emit(ChatStreamEvent.Error(e.message ?: "Tidak dapat terhubung ke layanan chatbot"))
                    return@flow
                }

                try {
                    if (shouldFallbackToRest(response.code)) {
                        emitRestFallback(userId, message)
                        return@flow
                    }

                    if (!response.isSuccessful) {
                        emit(ChatStreamEvent.Error(parseStreamHttpError(response.code, response.message)))
                        return@flow
                    }

                    val contentType = response.header("Content-Type").orEmpty()
                    if (!contentType.contains("text/event-stream")) {
                        emitRestFallback(userId, message)
                        return@flow
                    }

                    val source = response.body?.source()
                    if (source == null) {
                        emit(ChatStreamEvent.Error("Respons chatbot kosong"))
                        return@flow
                    }

                    var receivedChunk = false
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: continue
                        val payload = parseSseDataPayload(line) ?: continue

                        val json = runCatching { JsonParser.parseString(payload).asJsonObject }.getOrNull()
                            ?: continue

                        json.get("error")?.asString?.let { error ->
                            emit(ChatStreamEvent.Error(error))
                            return@flow
                        }

                        if (json.get("done")?.asBoolean == true) {
                            emit(ChatStreamEvent.Done)
                            return@flow
                        }

                        json.get("delta")?.asString?.let { delta ->
                            if (delta.isNotEmpty()) {
                                receivedChunk = true
                                emit(ChatStreamEvent.Chunk(delta))
                            }
                        }
                    }

                    if (!receivedChunk) {
                        emitRestFallback(userId, message)
                    } else {
                        emit(ChatStreamEvent.Done)
                    }
                } finally {
                    response.close()
                }
            } finally {
                call.cancel()
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun parseSseDataPayload(line: String): String? {
        val trimmed = line.trim()
        if (!trimmed.startsWith(SSE_DATA_PREFIX)) return null
        return trimmed.removePrefix(SSE_DATA_PREFIX).trim()
    }

    private suspend fun FlowCollector<ChatStreamEvent>.emitRestFallback(
        userId: String,
        message: String,
    ) {
        when (val result = sendMessage(userId, message)) {
            is Resource.Success -> {
                val text = result.data.orEmpty()
                if (text.isNotEmpty()) {
                    emit(ChatStreamEvent.Chunk(text))
                }
                emit(ChatStreamEvent.Done)
            }
            is Resource.Error -> emit(ChatStreamEvent.Error(result.message ?: "Terjadi kesalahan pada layanan chatbot"))
            is Resource.Loading -> Unit
        }
    }

    private fun shouldFallbackToRest(httpCode: Int): Boolean {
        return httpCode == 404 || httpCode == 405
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
                entry.response?.takeIf { it.isNotBlank() }?.let { response ->
                    messages.add(
                        ChatMessage(
                            id = "bot-${entry.id}",
                            text = response,
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

    override suspend fun getXaiProfile(userId: String): Resource<XaiProfile> {
        return try {
            val response = chatbotApiService.getXaiProfile(userId)
            Resource.Success(response.toDomain())
        } catch (e: IOException) {
            Resource.Error(e.message ?: "Tidak dapat memuat profil XAI")
        } catch (e: HttpException) {
            if (e.code() == 404) {
                Resource.Error("not_found")
            } else {
                Resource.Error(parseHttpError(e))
            }
        }
    }

    private fun RecommendationResponse.toDomain(): ChatRecommendation {
        val questions = listOf(question1, question2)
            .map { it.trim() }
            .filter { it.isNotBlank() }
        return ChatRecommendation(questions = questions)
    }

    private fun XaiProfileResponse.toDomain(): XaiProfile {
        return XaiProfile(
            userId = userId,
            riskScore = riskScore,
            features = features.map { it.toDomain() },
            xaiSummary = xaiSummary,
        )
    }

    private fun XaiFeatureResponse.toDomain(): XaiFeature {
        return XaiFeature(
            feature = feature,
            alias = alias,
            value = value,
            shap = shap,
            contribution = contribution,
            impact = impact,
            explanation = explanation,
        )
    }

    private fun parseHttpError(exception: HttpException): String {
        return when (exception.code()) {
            422 -> "Pesan terlalu panjang (maks. 500 karakter)"
            503 -> "Chatbot sedang mempersiapkan layanan, coba lagi"
            else -> exception.message() ?: "Terjadi kesalahan pada layanan chatbot"
        }
    }

    private fun parseStreamHttpError(code: Int, fallback: String): String {
        return when (code) {
            422 -> "Pesan terlalu panjang (maks. 500 karakter)"
            503 -> "Chatbot sedang mempersiapkan layanan, coba lagi"
            else -> fallback.ifBlank { "Terjadi kesalahan pada layanan chatbot" }
        }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val SSE_DATA_PREFIX = "data:"
    }
}
