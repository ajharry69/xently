package co.ke.xently.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.shoppinglist.repository.RecommendFrom
import co.ke.xently.shoppinglist.repository.RecommendFrom.GroupedList
import co.ke.xently.shoppinglist.ui.detail.ShoppingListItemScreen
import co.ke.xently.shoppinglist.ui.list.ShoppingListScreen
import co.ke.xently.shoppinglist.ui.list.grouped.ShoppingListGroupedScreen
import co.ke.xently.shoppinglist.ui.list.recommendation.ShoppingListRecommendationScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShoppingListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XentlyTheme {
                ShoppingListNavHost()
            }
        }
    }
}

@Composable
internal fun ShoppingListNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = "shopping-list-grouped",
        modifier = modifier,
    ) {
        val onShoppingListItemRecommendClicked: (itemId: Long) -> Unit = {
            navController.navigate("shopping-list/recommendations/${it}?from=${RecommendFrom.Item}")
        }
        val onShoppingListItemClicked: (itemId: Long) -> Unit = {
            navController.navigate("shopping-list/${it}")
        }
        composable("shopping-list-grouped") {
            ShoppingListGroupedScreen(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                onShoppingListItemClicked = onShoppingListItemClicked,
                onShoppingListItemRecommendClicked = onShoppingListItemRecommendClicked,
                onRecommendGroupClicked = {
                    navController.navigate("shopping-list/recommendations/${it}")
                },
                onSeeAllClicked = { navController.navigate("shopping-list") },
            )
        }
        composable("shopping-list") {
            ShoppingListScreen(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                onShoppingListItemClicked = onShoppingListItemClicked,
                onRecommendClicked = onShoppingListItemRecommendClicked,
                onRecommendOptionsMenuClicked = {},
                onNavigationIconClicked = { navController.navigateUp() }
            )
        }
        composable(
            "shopping-list/recommendations/{recommendBy}?from={from}",
            listOf(navArgument("recommendBy") {}, navArgument("from") {
                defaultValue = GroupedList.name
            })
        ) {
            ShoppingListRecommendationScreen(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                recommendBy = it.arguments?.get("recommendBy")!!,
                recommendFrom = RecommendFrom.valueOf(
                    it.arguments?.getString("from", GroupedList.name)!!
                ),
            ) { navController.navigateUp() }
        }
        composable(
            "shopping-list/{itemId}",
            listOf(
                navArgument("itemId") {
                    type = NavType.LongType
                },
            ),
        ) {
            ShoppingListItemScreen(it.arguments?.getLong("itemId"), onNavigationIconClicked = {
                navController.navigateUp()
            })
        }
    }
}