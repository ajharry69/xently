package co.ke.xently.products

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
import co.ke.xently.data.Product
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.products.ui.detail.ProductDetailScreen
import co.ke.xently.products.ui.list.ProductListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XentlyTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
                    ProductsNavHost(navController = navController) {
                        if (!navController.navigateUp()) onBackPressed()
                    }
                }
            }
        }
    }
}

@Composable
internal fun ProductsNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onNavigationIconClicked: () -> Unit,
) {
    NavHost(modifier = modifier, navController = navController, startDestination = "products") {
        composable("products") {
            ProductListScreen(
                modifier = Modifier.fillMaxSize(),
                onItemClicked = {
                    navController.navigate("products/$it") {
                        launchSingleTop = true
                    }
                },
                onNavigationIconClicked = onNavigationIconClicked,
                onAddProductClicked = {
                    navController.navigate("products/${Product.default().id}") {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(
            "products/{id}",
            listOf(
                navArgument("id") {
                    type = NavType.LongType
                },
            ),
        ) {
            ProductDetailScreen(
                id = it.arguments?.getLong("id"),
                modifier = Modifier.fillMaxSize(),
                onNavigationIconClicked = onNavigationIconClicked,
            )
        }
    }
}