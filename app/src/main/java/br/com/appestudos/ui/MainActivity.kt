package br.com.appestudos.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.appestudos.AppEstudosApplication
import br.com.appestudos.ui.navigation.AppNavigation
import br.com.appestudos.ui.theme.AppEstudosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = application as AppEstudosApplication
        val viewModelFactory = ViewModelFactory(application.repository, application.aiManager)

        enableEdgeToEdge()
        setContent {
            AppEstudosTheme {
                AppNavigation(factory = viewModelFactory)
            }
        }
    }
}