package br.com.appestudos.data.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GoogleAIService(private val apiKey: String) : AIService {

    // 🔹 Atualizado para modelo válido
    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash", // ou "gemini-1.5-pro"
            apiKey = apiKey
        )
    }

    override fun isAvailable(): Boolean = apiKey.isNotBlank()

    override fun getProvider(): AIProvider = AIProvider.GoogleAI

    override suspend fun generateText(request: AIRequest): AIResult<AIResponse> {
        return try {
            // Chamada com prompt direto
            val response: GenerateContentResponse = model.generateContent(request.prompt)

            val content = response.text ?: ""

            AIResult.Success(
                AIResponse(
                    content = content,
                    provider = AIProvider.GoogleAI,
                    model = model.modelName // retorna o nome do modelo realmente usado
                )
            )
        } catch (e: Exception) {
            AIResult.Error("Erro ao chamar Gemini: ${e.message}", e)
        }
    }

    override suspend fun generateTextStream(request: AIRequest): Flow<AIResult<String>> = flow {
        try {
            emit(AIResult.Loading)

            model.generateContentStream(request.prompt).collect { chunk ->
                chunk.text?.let { text ->
                    emit(AIResult.Success(text))
                }
            }
        } catch (e: Exception) {
            emit(AIResult.Error("Erro no streaming: ${e.message}", e))
        }
    }
}
