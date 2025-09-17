package br.com.appestudos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.appestudos.ui.ViewModelFactory
import br.com.appestudos.ui.screens.addeditdeck.AddEditDeckScreen
import br.com.appestudos.ui.screens.addeditdeck.AddEditDeckViewModel
import br.com.appestudos.ui.screens.addeditflashcard.AddEditFlashcardScreen
import br.com.appestudos.ui.screens.addeditflashcard.AddEditFlashcardViewModel
import br.com.appestudos.ui.screens.decklist.DeckListScreen
import br.com.appestudos.ui.screens.decklist.DeckListViewModel
import br.com.appestudos.ui.screens.flashcardlist.FlashcardListScreen
import br.com.appestudos.ui.screens.flashcardlist.FlashcardListViewModel
import br.com.appestudos.ui.screens.studysession.StudySessionScreen
import br.com.appestudos.ui.screens.studysession.StudySessionViewModel
import br.com.appestudos.ui.screens.importexport.ImportExportScreen
import br.com.appestudos.ui.screens.importexport.ImportExportViewModel
import br.com.appestudos.ui.screens.login.LoginScreen
import com.google.firebase.auth.FirebaseAuth

object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val DECK_LIST_ROUTE = "deck_list"
    const val ADD_EDIT_DECK_ROUTE = "add_edit_deck"
    const val FLASHCARD_LIST_ROUTE = "flashcard_list"
    const val ADD_EDIT_FLASHCARD_ROUTE = "add_edit_flashcard"
    const val STUDY_SESSION_ROUTE = "study_session"
    const val IMPORT_EXPORT_ROUTE = "import_export"
}

@Composable
fun AppNavigation(
    factory: ViewModelFactory
) {
    val navController = rememberNavController()

    // Tela inicial dinâmica: login ou lista de decks
    val startDestination =
        if (FirebaseAuth.getInstance().currentUser == null) {
            AppDestinations.LOGIN_ROUTE
        } else {
            AppDestinations.DECK_LIST_ROUTE
        }

    NavHost(navController = navController, startDestination = startDestination) {

        // Tela de Login
        composable(route = AppDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppDestinations.DECK_LIST_ROUTE) {
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        // Lista de Decks (com logout)
        composable(route = AppDestinations.DECK_LIST_ROUTE) {
            val viewModel: DeckListViewModel = factory.create(DeckListViewModel::class.java)
            DeckListScreen(
                viewModel = viewModel,
                onAddDeckClick = {
                    navController.navigate(AppDestinations.ADD_EDIT_DECK_ROUTE)
                },
                onDeckClick = { deckId ->
                    navController.navigate("${AppDestinations.FLASHCARD_LIST_ROUTE}/$deckId")
                },
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(AppDestinations.DECK_LIST_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        // Adicionar/Editar Deck
        composable(route = AppDestinations.ADD_EDIT_DECK_ROUTE) {
            val viewModel: AddEditDeckViewModel = factory.create(AddEditDeckViewModel::class.java)
            AddEditDeckScreen(
                viewModel = viewModel,
                onNavigateUp = { navController.navigateUp() },
                onSave = { navController.navigateUp() }
            )
        }

        // Lista de Flashcards
        composable(
            route = "${AppDestinations.FLASHCARD_LIST_ROUTE}/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            val viewModel: FlashcardListViewModel = factory.create(FlashcardListViewModel::class.java)
            FlashcardListScreen(
                viewModel = viewModel,
                deckId = deckId,
                onNavigateUp = { navController.navigateUp() },
                onAddFlashcardClick = {
                    navController.navigate("${AppDestinations.ADD_EDIT_FLASHCARD_ROUTE}/$deckId")
                },
                onStudyClick = {
                    navController.navigate("${AppDestinations.STUDY_SESSION_ROUTE}/$deckId")
                },
                onImportExportClick = { deckId, deckName ->
                    navController.navigate("${AppDestinations.IMPORT_EXPORT_ROUTE}/$deckId/$deckName")
                }
            )
        }

        // Adicionar/Editar Flashcard
        composable(
            route = "${AppDestinations.ADD_EDIT_FLASHCARD_ROUTE}/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            val viewModel: AddEditFlashcardViewModel = factory.create(AddEditFlashcardViewModel::class.java)
            AddEditFlashcardScreen(
                viewModel = viewModel,
                deckId = deckId,
                onNavigateUp = { navController.navigateUp() },
                onSave = { navController.navigateUp() }
            )
        }

        // Sessão de Estudo
        composable(
            route = "${AppDestinations.STUDY_SESSION_ROUTE}/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.LongType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            val viewModel: StudySessionViewModel = factory.create(StudySessionViewModel::class.java)
            StudySessionScreen(
                viewModel = viewModel,
                deckId = deckId,
                onNavigateUp = { navController.navigateUp() }
            )
        }

        // Import/Export de Flashcards
        composable(
            route = "${AppDestinations.IMPORT_EXPORT_ROUTE}/{deckId}/{deckName}",
            arguments = listOf(
                navArgument("deckId") { type = NavType.LongType },
                navArgument("deckName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
            val deckName = backStackEntry.arguments?.getString("deckName") ?: ""
            val viewModel: ImportExportViewModel = factory.create(ImportExportViewModel::class.java)
            ImportExportScreen(
                viewModel = viewModel,
                deckId = deckId,
                deckName = deckName,
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}
