package br.com.appestudos.data.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay

class AIManager(
    private val services: List<AIService>
) {
    
    suspend fun generateText(request: AIRequest): AIResult<AIResponse> {
        val availableServices = services.filter { it.isAvailable() }
        
        if (availableServices.isEmpty()) {
            return AIResult.Error("No AI services available")
        }
        
        for (service in availableServices) {
            val result = try {
                service.generateText(request)
            } catch (e: Exception) {
                AIResult.Error("Service ${service.getProvider()} failed: ${e.message}", e)
            }
            
            when (result) {
                is AIResult.Success -> return result
                is AIResult.Error -> {
                    println("AI Service ${service.getProvider()} failed: ${result.message}")
                    continue
                }
                is AIResult.Loading -> continue
            }
        }
        
        return AIResult.Error("All AI services failed")
    }
    
    suspend fun generateTextStream(request: AIRequest): Flow<AIResult<String>> = flow {
        val availableServices = services.filter { it.isAvailable() }
        
        if (availableServices.isEmpty()) {
            emit(AIResult.Error("No AI services available"))
            return@flow
        }
        
        for (service in availableServices) {
            try {
                var hasEmittedSuccess = false
                service.generateTextStream(request).collect { result ->
                    when (result) {
                        is AIResult.Success -> {
                            hasEmittedSuccess = true
                            emit(result)
                        }
                        is AIResult.Error -> {
                            if (!hasEmittedSuccess) {
                                println("AI Service ${service.getProvider()} streaming failed: ${result.message}")
                            } else {
                                emit(result)
                            }
                        }
                        is AIResult.Loading -> emit(result)
                    }
                }
                
                if (hasEmittedSuccess) {
                    return@flow
                }
            } catch (e: Exception) {
                println("AI Service ${service.getProvider()} streaming exception: ${e.message}")
                continue
            }
        }
        
        emit(AIResult.Error("All AI services failed for streaming"))
    }
    
    fun getAvailableProviders(): List<AIProvider> {
        return services.filter { it.isAvailable() }.map { it.getProvider() }
    }
    
    fun isAnyServiceAvailable(): Boolean {
        return services.any { it.isAvailable() }
    }
}