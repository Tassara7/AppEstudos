package br.com.appestudos.data.ai

import kotlinx.coroutines.flow.Flow

sealed class AIProvider {
    object GoogleAI : AIProvider()
    object Groq : AIProvider()
    object OpenAI : AIProvider()
    object Ollama : AIProvider()
}

data class AIRequest(
    val prompt: String,
    val systemMessage: String? = null,
    val maxTokens: Int = 1000,
    val temperature: Float = 0.7f,
    val model: String? = null
)

data class AIResponse(
    val content: String,
    val provider: AIProvider,
    val model: String,
    val usage: TokenUsage? = null
)

data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

sealed class AIResult<out T> {
    data class Success<T>(val data: T) : AIResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : AIResult<Nothing>()
    object Loading : AIResult<Nothing>()
}

interface AIService {
    suspend fun generateText(request: AIRequest): AIResult<AIResponse>
    suspend fun generateTextStream(request: AIRequest): Flow<AIResult<String>>
    fun isAvailable(): Boolean
    fun getProvider(): AIProvider
}