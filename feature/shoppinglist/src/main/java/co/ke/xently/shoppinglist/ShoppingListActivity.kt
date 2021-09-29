package co.ke.xently.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
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
import co.ke.xently.shoppinglist.ui.detail.ShoppingListItemScreen
import co.ke.xently.shoppinglist.ui.list.ShoppingListScreen
import co.ke.xently.shoppinglist.ui.list.ShoppingListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShoppingListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XentlyTheme {
                ShoppingListNavHost()
            }
        }
    }
}

@Composable
internal fun ShoppingListNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: ShoppingListViewModel = viewModel(),
) {
    NavHost(
        navController = navController,
        startDestination = "shopping-list",
        modifier = modifier,
    ) {
        composable("shopping-list") {
            ShoppingListScreen(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                viewModel = viewModel,
                onShoppingListItemClicked = { navController.navigate("shopping-list/${it}") },
            )
        }
        composable(
            "shopping-list/{itemId}",
            listOf(
                navArgument("itemId") {
                    type = NavType.LongType
                },
            ),
        ) {
            ShoppingListItemScreen(it.arguments?.getLong("itemId"), onNavigationIconClicked = {
                navController.navigateUp()
            })
        }
    }
}