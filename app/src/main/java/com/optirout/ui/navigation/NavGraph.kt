package com.optirout.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.optirout.ui.screens.ErrorScreen
import com.optirout.ui.screens.HomeScreen
import com.optirout.ui.screens.LoadingScreen
import com.optirout.ui.screens.RouteMapScreen
import com.optirout.viewmodel.RouteViewModel

object Routes {
    const val HOME = "home"
    const val LOADING = "loading"
    const val ROUTE_MAP = "route_map"
    const val ERROR = "error"
}

private val enterForward = slideInHorizontally(initialOffsetX = { it }) + fadeIn()
private val exitForward = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
private val enterBack = slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
private val exitBack = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()

@Composable
fun OptiRoutNavGraph(navController: NavHostController) {
    val viewModel: RouteViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { enterForward },
        exitTransition = { exitForward },
        popEnterTransition = { enterBack },
        popExitTransition = { exitBack },
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToLoading = {
                    navController.navigate(Routes.LOADING)
                },
            )
        }

        composable(Routes.LOADING) {
            LoadingScreen(
                viewModel = viewModel,
                onNavigateToMap = {
                    navController.navigate(Routes.ROUTE_MAP) {
                        popUpTo(Routes.LOADING) { inclusive = true }
                    }
                },
                onError = {
                    navController.navigate(Routes.ERROR) {
                        popUpTo(Routes.LOADING) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.ROUTE_MAP) {
            RouteMapScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    viewModel.resetState()
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
            )
        }

        composable(Routes.ERROR) {
            val errorMessage by viewModel.errorMessage.collectAsState()
            ErrorScreen(
                message = errorMessage ?: "Erro desconhecido. Verifique sua conexão e tente novamente.",
                onRetry = {
                    viewModel.clearError()
                    viewModel.resetState()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    viewModel.clearError()
                    viewModel.resetState()
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
            )
        }
    }
}
