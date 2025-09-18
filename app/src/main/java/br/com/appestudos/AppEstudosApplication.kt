package br.com.appestudos

import android.app.Application
import br.com.appestudos.data.ai.AIManager
import br.com.appestudos.data.ai.GoogleAIService
import br.com.appestudos.data.local.AppDatabase
import br.com.appestudos.data.repository.AppRepository
import br.com.appestudos.data.repository.AppRepositoryImpl
import br.com.appestudos.data.service.HybridMediaSyncService

class AppEstudosApplication : Application() {

    // Banco de dados
    private val database by lazy { AppDatabase.getInstance(this) }

    // ?? Gerenciador de IA (somente Google Gemini)
    val aiManager: AIManager by lazy {
        AIManager(
            services = listOf(
                GoogleAIService(
                    apiKey = BuildConfig.GOOGLE_AI_API_KEY // ?? chave injetada via build.gradle
                )
            )
        )
    }

    // ?? Repositório central
    val repository: AppRepository by lazy {
        AppRepositoryImpl(
            deckDao = database.deckDao(),
            flashcardDao = database.flashcardDao(),
            mediaContentDao = database.mediaContentDao()
        )
    }

    val hybridMediaSyncService: HybridMediaSyncService by lazy {
        HybridMediaSyncService(this, repository)
    }
}