package cat.insbaixcamp.llistadelacompra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cat.insbaixcamp.llistadelacompra.data.FirebaseAuthRepository
import cat.insbaixcamp.llistadelacompra.data.FirestoreListRepository
import cat.insbaixcamp.llistadelacompra.data.ListRepository
import cat.insbaixcamp.llistadelacompra.ui.screens.ListDetailScreen
import cat.insbaixcamp.llistadelacompra.ui.screens.ListOverviewScreen
import cat.insbaixcamp.llistadelacompra.ui.screens.LoginScreen
import cat.insbaixcamp.llistadelacompra.ui.theme.LlistaDeLaCompraTheme
import cat.insbaixcamp.llistadelacompra.viewmodel.AppViewModelFactory
import cat.insbaixcamp.llistadelacompra.viewmodel.AuthUiState
import cat.insbaixcamp.llistadelacompra.viewmodel.AuthViewModel
import cat.insbaixcamp.llistadelacompra.viewmodel.DetailViewModel
import cat.insbaixcamp.llistadelacompra.viewmodel.ListViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authRepository = FirebaseAuthRepository()
        val listRepository = FirestoreListRepository()

        setContent {
            LlistaDeLaCompraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        authRepository = authRepository,
                        listRepository = listRepository
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    authRepository: FirebaseAuthRepository,
    listRepository: ListRepository
) {
    val navController = rememberNavController()

    val factory = AppViewModelFactory(authRepository, listRepository)
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val listViewModel: ListViewModel = viewModel(factory = factory)

    val authUiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(authUiState) {
        when (authUiState) {
            is AuthUiState.SignedIn -> {
                if (navController.currentDestination?.route != "lists") {
                    navController.navigate("lists") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthUiState.SignedOut -> {
                if (navController.currentDestination?.route != "login") {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    when (authUiState) {
        is AuthUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        else -> {
            val startDestination = if (authUiState is AuthUiState.SignedIn) "lists" else "login"

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable("login") {
                    LoginScreen(
                        authViewModel = authViewModel
                    )
                }

                composable("lists") {
                    ListOverviewScreen(
                        authViewModel = authViewModel,
                        listViewModel = listViewModel,
                        onNavigateToListDetail = { listId ->
                            navController.navigate("detail/$listId")
                        }
                    )
                }

                composable(
                    route = "detail/{listId}",
                    arguments = listOf(navArgument("listId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val listId = backStackEntry.arguments?.getString("listId") ?: ""
                    val detailFactory = AppViewModelFactory(authRepository, listRepository, listId)
                    val detailViewModel: DetailViewModel = viewModel(
                        key = "DetailViewModel_$listId",
                        factory = detailFactory
                    )

                    ListDetailScreen(
                        authViewModel = authViewModel,
                        detailViewModel = detailViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
