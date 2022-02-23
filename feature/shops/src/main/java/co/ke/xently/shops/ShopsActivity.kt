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
import androidx.compose.ui.res.stringResource
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
import co.ke.xently.shops.ui.list.item.MenuItem
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
    val resources = context.resources
    NavHost(modifier = modifier, navController = navController, startDestination = "shops") {
        composable("shops") {
            ShopListScreen(
                modifier = Modifier.fillMaxSize(),
                menuItems = {
                    buildList {
                        add(
                            MenuItem(
                                label = stringResource(R.string.update),
                                onClick = {
                                    navController.navigate("shops/${it.id}")
                                },
                            ),
                        )
                        if (it.productsCount > 0) {
                            add(
                                MenuItem(
                                    label = resources.getQuantityString(
                                        R.plurals.fs_shop_item_menu_products,
                                        it.productsCount,
                                        it.productsCount,
                                    ),
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW,
                                            "xently://shops/${it.id}/products/".toUri())
                                        try {
                                            context.startActivity(intent)
                                        } catch (ex: ActivityNotFoundException) {
                                            Log.e(ShopsActivity::class.simpleName,
                                                "ShopsNavHost: ${ex.message}",
                                                ex)
                                        }
                                    }
                                ),
                            )
                        }
                        if (it.addressesCount > 0) {
                            add(
                                MenuItem(
                                    label = resources.getQuantityString(
                                        R.plurals.fs_shop_item_menu_addresses,
                                        it.addressesCount,
                                        it.addressesCount,
                                    ),
                                    onClick = {
                                        navController.navigate("shops/${it.id}/addresses")
                                    },
                                ),
                            )
                        }
                    }
                },
                click = co.ke.xently.shops.ui.list.Click(
                    add = {
                        navController.navigate("shops/${Shop.default().id}")
                    },
                    navigationIcon = onNavigationIconClicked,
                    click = co.ke.xently.shops.ui.list.item.Click(
                        base = {},
                    ),
                ),
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
                modifier = Modifier.fillMaxSize(),
                shopId = it.arguments!!.getLong("id"),
                click = co.ke.xently.shops.ui.list.addresses.Click(
                    navigationIcon = onNavigationIconClicked,
                    click = co.ke.xently.shops.ui.list.addresses.item.Click(base = {}),
                ),
            )
        }
    }
}