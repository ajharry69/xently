package co.ke.xently.products

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.ke.xently.data.Product
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.products.ui.detail.ProductDetailScreen
import co.ke.xently.products.ui.list.ProductListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(ProductsActivity::class.simpleName,
            "onCreate: <${intent.extras}>, Action: <${intent.action}>, Uri: <${intent.data}>")
        setContent {
            XentlyTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
                    ProductsNavHost("products", navController = navController) {
                        if (!navController.navigateUp()) onBackPressed()
                    }
                }
            }
        }
    }
}

@Composable
internal fun ProductsNavHost(
    startDestination: String,
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onNavigationIconClicked: () -> Unit,
) {
    NavHost(modifier = modifier,
        navController = navController,
        startDestination = startDestination) {
        val productList: @Composable (NavBackStackEntry) -> Unit = {
            ProductListScreen(
                shopId = it.arguments?.getLong("shopId"),
                modifier = Modifier.fillMaxSize(),
                onUpdateRequested = { productId ->
                    navController.navigate("products/$productId") {
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
        composable("products", content = productList)
        composable(
            "shops/{shopId}/products",
            content = productList,
            arguments = listOf(
                navArgument("shopId") {
                    type = NavType.LongType
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "xently://shops/{shopId}/products/"
                },
            ),
        )
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