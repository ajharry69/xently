package co.ke.xently

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import co.ke.xently.shoppinglist.ui.ShoppingList
import co.ke.xently.shoppinglist.ui.ShoppingListDetail
import co.ke.xently.shoppinglist.ui.ShoppingListRecommendation
import co.ke.xently.shoppinglist.ui.ShoppingListViewModel
import co.ke.xently.ui.theme.XentlyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
fun XentlyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: ShoppingListViewModel = viewModel(),
) {
    NavHost(navController = navController, startDestination = "shoppinglist", modifier = modifier) {
        composable("shoppinglist") {
            ShoppingList(viewModel = viewModel, navController = navController)
        }
        composable(
            "shoppingdetail/{groupId}",
            listOf(
                navArgument("groupId") {
                    type = NavType.LongType
                },
            ),
        ) {
            ShoppingListDetail()
        }
        composable(
            "shoppingrecommendation/{group}",
            listOf(
                navArgument("group") {
                    type = NavType.StringType
                },
            ),
        ) {
            ShoppingListRecommendation()
        }
    }
}