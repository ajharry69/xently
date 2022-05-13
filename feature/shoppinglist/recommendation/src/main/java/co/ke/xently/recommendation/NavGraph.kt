package co.ke.xently.recommendation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.utils.Routes
import co.ke.xently.feature.utils.buildRoute
import co.ke.xently.recommendation.ui.RecommendationScreen
import co.ke.xently.recommendation.ui.RecommendationScreenArgs
import co.ke.xently.recommendation.ui.RecommendationScreenFunction
import co.ke.xently.recommendation.ui.list.RecommendationListScreen
import co.ke.xently.recommendation.ui.list.RecommendationListScreenArgs
import co.ke.xently.recommendation.ui.list.RecommendationListScreenFunction
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItemFunction
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItemMenuItem
import co.ke.xently.shoppinglist.GroupBy
import co.ke.xently.shoppinglist.repository.ShoppingListGroup

fun NavGraphBuilder.recommendationGraph(
    controller: NavHostController,
    sharedFunction: SharedFunction,
) {
    navigation(
        route = Routes.ShoppingList.Recommendation.toString(),
        startDestination = Routes.ShoppingList.Recommendation.FILTER,
    ) {
        composable(
            route = Routes.ShoppingList.Recommendation.FILTER,
            arguments = listOf(
                navArgument("shopId") {
                    nullable = true
                },
                navArgument("group") {
                    nullable = true
                },
                navArgument("groupBy") {
                    nullable = true
                },
            ),
        ) { navBackStackEntry ->
            RecommendationScreen(
                modifier = Modifier.fillMaxSize(),
                function = RecommendationScreenFunction(
                    sharedFunction = sharedFunction,
                    onSuccess = {
                        controller.navigate(
                            route = Routes.ShoppingList.Recommendation.LIST.buildRoute(
                                "lookupId" to it,
                                "numberOfItems" to it.numberOfItems,
                            ),
                        ) {
                            launchSingleTop = true
                        }
                    },
                ),
                args = RecommendationScreenArgs(
                    itemId = navBackStackEntry.arguments?.get("itemId").toString().toLongOrNull(),
                    group = if (navBackStackEntry.arguments?.get("group") == null) {
                        null
                    } else {
                        ShoppingListGroup(
                            group = navBackStackEntry.arguments!!.get("group")!!,
                            groupBy = navBackStackEntry.arguments?.getString("groupBy")
                                ?.let { groupBy -> GroupBy.valueOf(groupBy) } ?: GroupBy.DateAdded,
                        )
                    },
                ),
            )
        }
        composable(
            route = Routes.ShoppingList.Recommendation.LIST,
            arguments = listOf(
                navArgument("lookupId") {},
                navArgument("numberOfItems") {
                    defaultValue = 0
                },
            ),
        ) {
            RecommendationListScreen(
                function = RecommendationListScreenFunction(
                    onItemClicked = {},
                    sharedFunction = sharedFunction,
                    function = RecommendationCardItemFunction(
                        onItemClicked = {
                            // TODO: ...
                        },
                    ),
                ),
                menuItems = listOf(
                    RecommendationCardItemMenuItem(
                        label = R.string.fr_directions,
                        onClick = {

                        },
                    ),
                    RecommendationCardItemMenuItem(
                        label = R.string.fr_details,
                        onClick = {
                            // TODO: Show a modal with hit and miss...
                        },
                    ),
                ),
                modifier = Modifier.fillMaxSize(),
                args = RecommendationListScreenArgs(
                    lookupId = it.arguments?.getString("lookupId")!!,
                    numberOfItems = it.arguments?.getInt("numberOfItems", 0)!!,
                ),
            )
        }
    }
}