package br.com.appestudos

import android.app.Application
import br.com.appestudos.data.local.AppDatabase
import br.com.appestudos.data.repository.AppRepository
import br.com.appestudos.data.repository.AppRepositoryImpl

class AppEstudosApplication : Application() {

    private val database by lazy { AppDatabase.getInstance(this) }

    val repository: AppRepository by lazy {
        AppRepositoryImpl(
            deckDao = database.deckDao(),
            flashcardDao = database.flashcardDao()
        )
    }
}