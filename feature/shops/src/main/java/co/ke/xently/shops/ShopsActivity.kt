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
import co.ke.xently.feature.ui.OptionMenu
import co.ke.xently.feature.utils.Routes
import co.ke.xently.feature.utils.buildRoute
import co.ke.xently.shops.ui.detail.ShopDetailScreen
import co.ke.xently.shops.ui.detail.ShopDetailScreenArgs
import co.ke.xently.shops.ui.detail.ShopDetailScreenFunction
import co.ke.xently.shops.ui.list.ShopListScreen
import co.ke.xently.shops.ui.list.ShopListScreenFunction
import co.ke.xently.shops.ui.list.addresses.AddressListScreen
import co.ke.xently.shops.ui.list.addresses.AddressListScreenFunction
import co.ke.xently.shops.ui.list.addresses.item.AddressListItemFunction
import co.ke.xently.shops.ui.list.item.MenuItem
import co.ke.xently.shops.ui.list.item.ShopListItemFunction
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
                        onBackPressed()
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
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Routes.Shops.LIST,
    ) {
        composable(Routes.Shops.LIST) {
            ShopListScreen(
                modifier = Modifier.fillMaxSize(),
                optionsMenu = listOf(
                    OptionMenu(title = stringResource(R.string.refresh)),
                ),
                menuItems = {
                    buildList {
                        add(
                            MenuItem(
                                label = stringResource(R.string.update),
                                onClick = {
                                    navController.navigate(Routes.Shops.DETAIL.buildRoute("id" to it.id))
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
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Routes.Products.Deeplinks.FILTERED_BY_SHOP.buildRoute("shopId" to it.id)
                                                .toUri(),
                                        )
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
                                        navController.navigate(Routes.Shops.ADDRESSES.buildRoute("id" to it.id))
                                    },
                                ),
                            )
                        }
                    }
                },
                function = ShopListScreenFunction(
                    onAddFabClicked = {
                        navController.navigate(Routes.Shops.DETAIL.buildRoute("id" to Shop.default().id))
                    },
                    onNavigationIcon = onNavigationIconClicked,
                    function = ShopListItemFunction(
                        onItemClicked = {},
                    ),
                ),
            )
        }
        composable(
            route = Routes.Shops.DETAIL,
            arguments = listOf(
                navArgument("id") {
                    nullable = false
                    type = NavType.LongType
                },
                navArgument("name") {
                    defaultValue = ""
                },
                navArgument("moveBack") {
                    defaultValue = 0
                    type = NavType.LongType
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = Routes.Shops.Deeplinks.DETAIL
                },
            ),
        ) {
            ShopDetailScreen(
                modifier = Modifier.fillMaxSize(),
                id = it.arguments?.getLong("id") ?: Shop.default().id,
                args = ShopDetailScreenArgs(
                    name = it.arguments?.getString("name") ?: "",
                    moveBack = it.arguments?.getLong("moveBack") == 1L,
                ),
                function = ShopDetailScreenFunction(
                    onNavigationIconClicked = onNavigationIconClicked,
                ),
            )
        }
        composable(
            route = Routes.Shops.ADDRESSES,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = Routes.Shops.Deeplinks.ADDRESSES
                },
            ),
        ) {
            AddressListScreen(
                modifier = Modifier.fillMaxSize(),
                shopId = it.arguments!!.getLong("id"),
                optionsMenu = listOf(
                    OptionMenu(title = stringResource(R.string.refresh)),
                ),
                function = AddressListScreenFunction(
                    onNavigationIcon = onNavigationIconClicked,
                    function = AddressListItemFunction(onItemClick = {}),
                ),
            )
        }
    }
}