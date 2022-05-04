package co.ke.xently.products

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.navigation.*
import androidx.navigation.compose.composable
import co.ke.xently.data.Product
import co.ke.xently.data.Shop
import co.ke.xently.feature.ui.OptionMenu
import co.ke.xently.feature.utils.Routes
import co.ke.xently.feature.utils.buildRoute
import co.ke.xently.products.ui.detail.ProductDetailScreen
import co.ke.xently.products.ui.detail.ProductDetailScreenFunction
import co.ke.xently.products.ui.list.ProductListScreen
import co.ke.xently.products.ui.list.ProductListScreenFunction
import co.ke.xently.products.ui.list.item.MenuItem

fun NavGraphBuilder.productsGraph(
    navController: NavHostController,
    onNavigationIconClicked: () -> Unit,
) {
    navigation(route = Routes.Products.toString(), startDestination = Routes.Products.LIST) {
        val productList: @Composable (NavBackStackEntry) -> Unit = { backStackEntry ->
            ProductListScreen(
                shopId = backStackEntry.arguments?.getLong("shopId", Shop.default().id),
                function = ProductListScreenFunction(
                    onAddFabClicked = {
                        navController.navigate(Routes.Products.DETAIL.buildRoute("id" to Product.default().id)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigationIconClicked = onNavigationIconClicked,
                ),
                menuItems = listOf(
                    MenuItem(R.string.update) {
                        navController.navigate(Routes.Products.DETAIL.buildRoute("id" to it.id)) {
                            launchSingleTop = true
                        }
                    },
                    MenuItem(R.string.delete) {
                        // TODO: Handle delete...
                    },
                ),
                modifier = Modifier.fillMaxSize(),
                optionsMenu = listOf(
                    OptionMenu(title = stringResource(R.string.refresh)),
                ),
            )
        }
        composable(Routes.Products.LIST, content = productList)
        composable(
            route = Routes.Products.FILTERED_BY_SHOP,
            content = productList,
            arguments = listOf(
                navArgument("shopId") {
                    type = NavType.LongType
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = Routes.Products.Deeplinks.FILTERED_BY_SHOP
                },
            ),
        )
        composable(
            route = Routes.Products.DETAIL,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                },
            ),
        ) { navBackStackEntry ->
            val context = LocalContext.current
            ProductDetailScreen(
                modifier = Modifier.fillMaxSize(),
                id = navBackStackEntry.arguments?.getLong("id") ?: Product.default().id,
                function = ProductDetailScreenFunction(
                    onNavigationIconClicked = onNavigationIconClicked,
                    onAddNewShop = {
                        val route = Routes.Shops.Deeplinks.DETAIL.buildRoute(
                            "name" to it,
                            "moveBack" to 1,
                            "id" to Shop.default().id,
                        )
                        context.startActivity(Intent(Intent.ACTION_VIEW, route.toUri()))
                    },
                ),
            )
        }
    }
}