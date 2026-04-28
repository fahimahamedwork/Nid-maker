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
import com.nidcard.app.ui.screens.create.EditNIDScreen
import com.nidcard.app.ui.screens.home.HomeScreen
import com.nidcard.app.ui.screens.search.SearchScreen
import com.nidcard.app.ui.screens.view.ViewNIDScreen
import com.nidcard.app.viewmodel.NIDViewModel

object ScreenRoutes {
    const val HOME = "home"
    const val CREATE_NID = "create_nid"
    const val VIEW_NID = "view_nid/{cardId}"
    const val EDIT_NID = "edit_nid/{cardId}"
    const val SEARCH = "search"
    const val ADMIN = "admin"

    fun viewNid(cardId: Long) = "view_nid/$cardId"
    fun editNid(cardId: Long) = "edit_nid/$cardId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ScreenRoutes.HOME) {
        composable(ScreenRoutes.HOME) {
            HomeScreen(navController = navController)
        }
        composable(ScreenRoutes.CREATE_NID) {
            CreateNIDScreen(navController = navController)
        }
        composable(
            route = ScreenRoutes.VIEW_NID,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) {
            val cardId = it.arguments?.getLong("cardId") ?: 0L
            val viewModel: NIDViewModel = viewModel()
            viewModel.loadCardById(cardId)
            ViewNIDScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            route = ScreenRoutes.EDIT_NID,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) {
            val cardId = it.arguments?.getLong("cardId") ?: 0L
            val viewModel: NIDViewModel = viewModel()
            viewModel.loadCardById(cardId)
            viewModel.enterEditMode()
            EditNIDScreen(navController = navController, viewModel = viewModel)
        }
        composable(ScreenRoutes.SEARCH) {
            SearchScreen(navController = navController)
        }
        composable(ScreenRoutes.ADMIN) {
            AdminScreen(navController = navController)
        }
    }
}
