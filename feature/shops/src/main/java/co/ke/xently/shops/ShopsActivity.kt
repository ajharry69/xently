package co.ke.xently.shops

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.ke.xently.data.Shop
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.shops.ui.detail.ShopDetailScreen
import co.ke.xently.shops.ui.list.ShopListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XentlyTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
                    ShopsNavHost(navController = navController) {
                        if (!navController.navigateUp()) onBackPressed()
                    }
                }
            }
        }
    }
}

@Composable
internal fun ShopsNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onNavigationIconClicked: () -> Unit,
) {
    NavHost(modifier = modifier, navController = navController, startDestination = "shops") {
        composable("shops") {
            ShopListScreen(
                modifier = Modifier.fillMaxSize(),
                onItemClicked = {
                    navController.navigate("shops/$it")
                },
                onProductsClicked = {
                    // TODO: Show shop's products screen
                },
                onAddressesClicked = {
                    // TODO: Show shop's addresses screen
                },
                onAddShopClicked = {
                    navController.navigate("shops/${Shop.DEFAULT_ID}")
                },
                onNavigationIconClicked = onNavigationIconClicked,
            )
        }
        composable(
            "shops/{id}",
            arguments = listOf(
                navArgument("id") {
                    nullable = false
                    type = NavType.LongType
                },
            ),
        ) {
            ShopDetailScreen(
                modifier = Modifier.fillMaxSize(),
                id = it.arguments?.getLong("id")
            )
        }
    }
}