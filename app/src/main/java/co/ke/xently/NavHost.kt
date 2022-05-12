package co.ke.xently

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import co.ke.xently.accounts.accountsGraph
import co.ke.xently.data.User
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.utils.Routes
import co.ke.xently.feature.utils.buildRoute
import co.ke.xently.products.productsGraph
import co.ke.xently.recommendation.recommendationGraph
import co.ke.xently.shoppinglist.shoppingListGraph
import co.ke.xently.shops.shopsGraph


@Composable
@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
fun XentlyNavHost(
    sharedFunction: SharedFunction,
    onSignInOrOut: (User?) -> Unit,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.ShoppingList.toString(),
    ) {
        recommendationGraph(
            controller = navController,
            sharedFunction = sharedFunction,
        )
        productsGraph(
            navController = navController,
            onNavigationIconClicked = sharedFunction.onNavigationIconClicked,
        )
        accountsGraph(
            navController = navController,
            onNavigationIconClicked = sharedFunction.onNavigationIconClicked,
        )
        shopsGraph(
            navController = navController,
            onNavigationIconClicked = sharedFunction.onNavigationIconClicked,
            onLocationPermissionChanged = sharedFunction.onLocationPermissionChanged,
        )
        shoppingListGraph(
            navController = navController,
            sharedFunction = sharedFunction,
        ) { drawerState ->
            DrawerContent(
                user = sharedFunction.currentlyActiveUser.invoke(),
                drawerState = drawerState,
                function = DrawerContentFunction(
                    onSignInOrOut = onSignInOrOut,
                    onAccountMenuClicked = {
                        navController.navigate(Routes.Account.toString())
                    },
                    onShopMenuClicked = {
                        navController.navigate(Routes.Shops.toString())
                    },
                    onProductMenuClicked = {
                        navController.navigate(Routes.Products.toString())
                    },
                    onRecommendationMenuClicked = {
                        navController.navigate(Routes.ShoppingList.Recommendation.toString())
                    },
                    onShoppingListMenuClicked = {
                        if (navController.currentDestination?.route != Routes.ShoppingList.GROUPED) {
                            navController.navigate(Routes.ShoppingList.GROUPED.buildRoute()) {
                                launchSingleTop = true
                            }
                        }
                    },
                ),
            )
        }
    }
}
