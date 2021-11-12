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
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.shops.ui.detail.ShopDetail
import co.ke.xently.shops.ui.list.ShopList

class ShopsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XentlyTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    ShopsNavHost()
                }
            }
        }
    }
}

@Composable
internal fun ShopsNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(modifier = modifier, navController = navController, startDestination = "shop-list") {
        composable("shop-list") {
            ShopList(
                modifier = Modifier.fillMaxSize(),
                onItemClicked = {
                    navController.navigate("shop/$it")
                },
                onProductsClicked = {
                    // TODO: Show shop's products screen
                },
                onAddressesClicked = {
                    // TODO: Show shop's addresses screen
                },
            )
        }
        composable(
            "shop/{id}",
            arguments = listOf(
                navArgument("id") {
                    nullable = false
                    type = NavType.LongType
                },
            ),
        ) {
            ShopDetail(modifier = Modifier.fillMaxSize())
        }
    }
}