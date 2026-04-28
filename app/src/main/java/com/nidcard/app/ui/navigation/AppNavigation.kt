package com.nidcard.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nidcard.app.ui.screens.admin.AdminScreen
import com.nidcard.app.ui.screens.create.CreateNIDScreen
import com.nidcard.app.ui.screens.home.HomeScreen
import com.nidcard.app.ui.screens.search.SearchScreen
import com.nidcard.app.ui.screens.view.ViewNIDScreen
import com.nidcard.app.viewmodel.NIDViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("create_nid") {
            CreateNIDScreen(navController = navController)
        }
        composable(
            route = "view_nid/{cardId}",
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) {
            val cardId = it.arguments?.getLong("cardId") ?: 0L
            val viewModel: NIDViewModel = viewModel()
            viewModel.loadCardById(cardId)
            ViewNIDScreen(navController = navController, viewModel = viewModel)
        }
        composable("search") {
            SearchScreen(navController = navController)
        }
        composable("admin") {
            AdminScreen(navController = navController)
        }
    }
}
