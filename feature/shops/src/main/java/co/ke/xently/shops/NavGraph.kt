package co.ke.xently.shops

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.navigation.*
import androidx.navigation.compose.composable
import co.ke.xently.data.Shop
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.ui.OptionMenu
import co.ke.xently.feature.utils.Routes
import co.ke.xently.feature.utils.buildRoute
import co.ke.xently.shops.ui.detail.ShopDetailScreen
import co.ke.xently.shops.ui.detail.ShopDetailScreenArgs
import co.ke.xently.shops.ui.detail.ShopDetailScreenFunction
import co.ke.xently.shops.ui.list.ShopListScreen
import co.ke.xently.shops.ui.list.ShopListScreenFunction
import co.ke.xently.shops.ui.list.item.MenuItem
import co.ke.xently.shops.ui.list.item.ShopListItemFunction

fun NavGraphBuilder.shopsGraph(
    sharedFunction: SharedFunction,
    navController: NavHostController,
) {
    navigation(route = Routes.Shops.toString(), startDestination = Routes.Shops.LIST) {
        composable(Routes.Shops.LIST) {
            val context = LocalContext.current
            val resources = context.resources
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
                                            Log.e(
                                                "ShopsActivity",
                                                "ShopsNavHost: ${ex.message}",
                                                ex
                                            )
                                        }
                                    }
                                ),
                            )
                        }
                    }
                },
                function = ShopListScreenFunction(
                    onAddFabClicked = {
                        navController.navigate(Routes.Shops.DETAIL.buildRoute("id" to Shop.default().id))
                    },
                    sharedFunction = sharedFunction,
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
                function = ShopDetailScreenFunction(sharedFunction = sharedFunction),
            )
        }
    }
}