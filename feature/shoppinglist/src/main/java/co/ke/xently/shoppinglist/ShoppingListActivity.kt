package co.ke.xently.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.shoppinglist.ui.ShoppingListDetail
import co.ke.xently.shoppinglist.ui.ShoppingListScreen
import co.ke.xently.shoppinglist.ui.ShoppingListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShoppingListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XentlyTheme {
                val navController = rememberNavController()
                Scaffold {
                    XentlyNavHost(navController = navController, modifier = Modifier.padding(it))
                }
            }
        }
    }
}

@Composable
internal fun XentlyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: ShoppingListViewModel = viewModel(),
) {
    NavHost(
        navController = navController,
        startDestination = "shopping-list",
        modifier = modifier,
    ) {
        composable("shopping-list") {
            ShoppingListScreen(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                navController = navController,
            )
        }
        composable(
            "shopping-list/{groupId}",
            listOf(
                navArgument("groupId") {
                    type = NavType.LongType
                },
            ),
        ) {
            ShoppingListDetail(
                it.arguments?.getLong("groupId"),
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}