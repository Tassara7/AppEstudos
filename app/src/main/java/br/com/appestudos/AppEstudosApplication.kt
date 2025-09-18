package br.com.appestudos

import android.app.Application
import br.com.appestudos.data.ai.AIManager
import br.com.appestudos.data.ai.GoogleAIService
import br.com.appestudos.data.ai.GroqService
import br.com.appestudos.data.local.AppDatabase
import br.com.appestudos.data.repository.AppRepository
import br.com.appestudos.data.repository.AppRepositoryImpl
import br.com.appestudos.data.service.HybridMediaSyncService

class AppEstudosApplication : Application() {

    private val database by lazy { AppDatabase.getInstance(this) }

    val aiManager: AIManager by lazy { 
        AIManager(services = listOf(
            GoogleAIService(apiKey = "your_google_ai_api_key"), // TODO: Move to BuildConfig
            GroqService(apiKey = "your_groq_api_key") // TODO: Move to BuildConfig
        ))
    }

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