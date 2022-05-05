package co.ke.xently.recommendation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import co.ke.xently.feature.utils.Routes
import co.ke.xently.recommendation.ui.list.RecommendationListScreen
import co.ke.xently.recommendation.ui.list.RecommendationListScreenFunction
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItemFunction
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItemMenuItem

fun NavGraphBuilder.recommendationGraph(
//    controller: NavHostController,
    onNavigationIconClicked: () -> Unit,
) {
    navigation(
        route = Routes.ShoppingList.Recommendation.toString(),
        startDestination = Routes.ShoppingList.Recommendation.FILTER,
    ) {
        composable(
            route = Routes.ShoppingList.Recommendation.LIST,
            arguments = listOf(
                navArgument("recommendBy") {},
                navArgument("from") {
                    defaultValue = Recommend.From.GroupedList.name
                },
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
                recommend = Recommend(
                    it.arguments?.get("recommendBy")!!,
                    Recommend.From.valueOf(
                        it.arguments?.getString(
                            "from",
                            Recommend.From.GroupedList.name
                        )!!
                    ),
                ),
            )
        }
    }
}