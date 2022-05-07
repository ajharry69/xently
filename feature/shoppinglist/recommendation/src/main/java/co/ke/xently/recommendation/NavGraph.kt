package co.ke.xently.recommendation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import co.ke.xently.feature.utils.Routes
import co.ke.xently.recommendation.ui.ShopRecommendationScreen
import co.ke.xently.recommendation.ui.ShopRecommendationScreenArgs
import co.ke.xently.recommendation.ui.ShopRecommendationScreenFunction
import co.ke.xently.recommendation.ui.list.RecommendationListScreen
import co.ke.xently.recommendation.ui.list.RecommendationListScreenFunction
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItemFunction
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItemMenuItem
import co.ke.xently.shoppinglist.GroupBy
import co.ke.xently.shoppinglist.repository.ShoppingListGroup

fun NavGraphBuilder.recommendationGraph(
//    controller: NavHostController,
    onNavigationIconClicked: () -> Unit,
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
        ) {
            ShopRecommendationScreen(
                modifier = Modifier.fillMaxSize(),
                function = ShopRecommendationScreenFunction(
                    onNavigationClick = onNavigationIconClicked,
                ),
                args = ShopRecommendationScreenArgs(
                    itemId = it.arguments?.get("itemId").toString().toLongOrNull(),
                    group = if (it.arguments?.get("group") == null) {
                        null
                    } else {
                        ShoppingListGroup(
                            group = it.arguments!!.get("group")!!,
                            groupBy = it.arguments?.getString("groupBy")
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
            ),
        ) {
            RecommendationListScreen(
                function = RecommendationListScreenFunction(
                    onItemClicked = {},
                    onNavigationIconClicked = onNavigationIconClicked,
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

                        },
                    ),
                ),
                modifier = Modifier.fillMaxSize(),
                lookupId = it.arguments?.getString("lookupId")!!,
            )
        }
    }
}