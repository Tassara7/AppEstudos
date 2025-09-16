package br.com.appestudos.data.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GoogleAIService(private val apiKey: String) : AIService {
    
    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-pro",
            apiKey = apiKey
        )
    }

    override suspend fun generateText(request: AIRequest): AIResult<AIResponse> {
        return try {
            if (!isAvailable()) {
                return AIResult.Error("Google AI service not available")
            }

            val prompt = buildPrompt(request)
            val response = model.generateContent(prompt)
            
            val content = response.text ?: ""
            
            AIResult.Success(
                AIResponse(
                    content = content,
                    provider = AIProvider.GoogleAI,
                    model = "gemini-pro",
                    usage = extractUsage(response)
                )
            )
        } catch (e: Exception) {
            AIResult.Error("Google AI error: ${e.message}", e)
        }
    }

    override suspend fun generateTextStream(request: AIRequest): Flow<AIResult<String>> = flow {
        try {
            if (!isAvailable()) {
                emit(AIResult.Error("Google AI service not available"))
                return@flow
            }

            emit(AIResult.Loading)
            
            val prompt = buildPrompt(request)
            model.generateContentStream(prompt).collect { response ->
                response.text?.let { text ->
                    emit(AIResult.Success(text))
                }
            }
        } catch (e: Exception) {
            emit(AIResult.Error("Google AI streaming error: ${e.message}", e))
        }
    }

    override fun isAvailable(): Boolean {
        return apiKey.isNotBlank()
    }

    override fun getProvider(): AIProvider = AIProvider.GoogleAI

    private fun buildPrompt(request: AIRequest): String {
        return if (request.systemMessage != null) {
            "${request.systemMessage}\n\nUser: ${request.prompt}\nAssistant:"
        } else {
            request.prompt
        }
    }

    private fun extractUsage(response: GenerateContentResponse): TokenUsage? {
        return try {
            response.usageMetadata?.let { metadata ->
                TokenUsage(
                    promptTokens = metadata.promptTokenCount,
                    completionTokens = metadata.candidatesTokenCount,
                    totalTokens = metadata.totalTokenCount
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}