package co.ke.xently

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
            XentlyApp()
        }
    }
}

@Composable
fun XentlyApp() {
    XentlyTheme {
        val navController = rememberNavController()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Xently") },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Menu, contentDescription = null)
                        }
                    },
                    actions = {
                        // RowScope here, so these icons will be placed horizontally
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Favorite,
                                contentDescription = "Localized description")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Search,
                                contentDescription = "Localized description")
                        }
                    }
                )
            }
        ) {
            XentlyNavHost(navController = navController, modifier = Modifier.padding(it))
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
            ShoppingList(viewModel = viewModel)
        }
        composable("shoppingdetail") {
            ShoppingListDetail()
        }
        composable("shoppingrecommendation") {
            ShoppingListRecommendation()
        }
    }
}