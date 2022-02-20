package co.ke.xently.shops

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import co.ke.xently.data.Shop
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.shops.ui.detail.ShopDetailScreen
import co.ke.xently.shops.ui.list.ShopListScreen
import co.ke.xently.shops.ui.list.addresses.AddressListScreen
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
    val context = LocalContext.current
    NavHost(modifier = modifier, navController = navController, startDestination = "shops") {
        composable("shops") {
            ShopListScreen(
                modifier = Modifier.fillMaxSize(),
                onUpdateRequested = {
                    navController.navigate("shops/$it")
                },
                onProductsClicked = {
                    val intent = Intent(Intent.ACTION_VIEW, "xently://shops/$it/products/".toUri())
                    try {
                        context.startActivity(intent)
                    } catch (ex: ActivityNotFoundException) {
                        Log.e(ShopsActivity::class.simpleName, "ShopsNavHost: ${ex.message}", ex)
                    }
                },
                onAddressesClicked = {
                    navController.navigate("shops/$it/addresses")
                },
                onAddShopClicked = {
                    navController.navigate("shops/${Shop.default().id}")
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
        composable(
            "shops/{id}/addresses",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "xently://shops/{id}/addresses/"
                },
            ),
        ) {
            AddressListScreen(
                shopId = it.arguments!!.getLong("id"),
                modifier = Modifier.fillMaxSize(),
                onNavigationIconClicked = onNavigationIconClicked,
            )
        }
    }
}