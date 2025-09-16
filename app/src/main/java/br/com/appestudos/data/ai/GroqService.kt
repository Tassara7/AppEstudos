package br.com.appestudos.data.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GroqService(private val apiKey: String) : AIService {
    
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            gson()
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("Groq HTTP: $message")
                }
            }
            level = LogLevel.INFO
        }
    }

    override suspend fun generateText(request: AIRequest): AIResult<AIResponse> {
        return try {
            if (!isAvailable()) {
                return AIResult.Error("Groq service not available")
            }

            val groqRequest = GroqRequest(
                model = request.model ?: "llama3-8b-8192",
                messages = buildMessages(request),
                max_tokens = request.maxTokens,
                temperature = request.temperature
            )

            val response = httpClient.post("https://api.groq.com/openai/v1/chat/completions") {
                headers {
                    append("Authorization", "Bearer $apiKey")
                    append("Content-Type", "application/json")
                }
                contentType(ContentType.Application.Json)
                setBody(groqRequest)
            }.body<GroqResponse>()

            val content = response.choices.firstOrNull()?.message?.content ?: ""
            
            AIResult.Success(
                AIResponse(
                    content = content,
                    provider = AIProvider.Groq,
                    model = groqRequest.model,
                    usage = response.usage?.let { usage ->
                        TokenUsage(
                            promptTokens = usage.prompt_tokens,
                            completionTokens = usage.completion_tokens,
                            totalTokens = usage.total_tokens
                        )
                    }
                )
            )
        } catch (e: Exception) {
            AIResult.Error("Groq error: ${e.message}", e)
        }
    }

    override suspend fun generateTextStream(request: AIRequest): Flow<AIResult<String>> = flow {
        emit(AIResult.Error("Streaming not implemented for Groq yet"))
    }

    override fun isAvailable(): Boolean {
        return apiKey.isNotBlank()
    }

    override fun getProvider(): AIProvider = AIProvider.Groq

    private fun buildMessages(request: AIRequest): List<GroqMessage> {
        val messages = mutableListOf<GroqMessage>()
        
        if (request.systemMessage != null) {
            messages.add(GroqMessage("system", request.systemMessage))
        }
        
        messages.add(GroqMessage("user", request.prompt))
        
        return messages
    }

    data class GroqRequest(
        val model: String,
        val messages: List<GroqMessage>,
        val max_tokens: Int,
        val temperature: Float,
        val stream: Boolean = false
    )

    data class GroqMessage(
        val role: String,
        val content: String
    )

    data class GroqResponse(
        val id: String,
        val choices: List<GroqChoice>,
        val usage: GroqUsage?,
        val model: String
    )

    data class GroqChoice(
        val message: GroqMessage,
        val finish_reason: String?
    )

    data class GroqUsage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int
    )
}