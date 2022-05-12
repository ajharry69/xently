package co.ke.xently.shoppinglist

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.*
import androidx.navigation.compose.composable
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.ui.OptionMenu
import co.ke.xently.feature.utils.Routes
import co.ke.xently.feature.utils.buildRoute
import co.ke.xently.shoppinglist.repository.ShoppingListGroup
import co.ke.xently.shoppinglist.ui.detail.ShoppingListItemScreen
import co.ke.xently.shoppinglist.ui.detail.ShoppingListItemScreenFunction
import co.ke.xently.shoppinglist.ui.list.ShoppingListScreen
import co.ke.xently.shoppinglist.ui.list.ShoppingListScreenFunction
import co.ke.xently.shoppinglist.ui.list.grouped.GroupedShoppingListScreen
import co.ke.xently.shoppinglist.ui.list.grouped.GroupedShoppingListScreenFunction
import co.ke.xently.shoppinglist.ui.list.grouped.item.GroupMenuItem
import co.ke.xently.shoppinglist.ui.list.grouped.item.GroupedShoppingListCardFunction
import co.ke.xently.shoppinglist.ui.list.item.MenuItem

fun NavGraphBuilder.shoppingListGraph(
    sharedFunction: SharedFunction,
    navController: NavHostController,
    drawerContent: @Composable (ColumnScope.(DrawerState) -> Unit),
) {
    navigation(
        route = Routes.ShoppingList.toString(),
        startDestination = Routes.ShoppingList.GROUPED,
    ) {
        val onShoppingListItemRecommendClicked: (id: Long) -> Unit = {
            navController.navigate(
                Routes.ShoppingList.Recommendation.FILTER.buildRoute(
                    "itemId" to it,
                )
            ) {
                launchSingleTop = true
            }
        }
        val onShoppingListItemClicked: (id: Long) -> Unit = {
            navController.navigate(Routes.ShoppingList.DETAIL.buildRoute("id" to it))
        }
        val onAddShoppingListItemClicked = {
            navController.navigate(Routes.ShoppingList.DETAIL.buildRoute("id" to ShoppingListItem.default().id))
        }
        val shoppingListItemMenuItems = listOf(
            MenuItem(R.string.fsl_group_menu_recommend, onShoppingListItemRecommendClicked),
            MenuItem(R.string.update, onShoppingListItemClicked),
            MenuItem(R.string.delete),
        )
        composable(Routes.ShoppingList.GROUPED) {
            GroupedShoppingListScreen(
                modifier = Modifier.fillMaxSize(),
                menuItems = shoppingListItemMenuItems,
                optionsMenu = listOf(
                    OptionMenu(title = stringResource(R.string.refresh)),
                ),
                groupMenuItems = listOf(
                    GroupMenuItem(R.string.fsl_group_menu_recommend) {
                        navController.navigate(
                            Routes.ShoppingList.Recommendation.FILTER.buildRoute(
                                "group" to it.group,
                                "groupBy" to it.groupBy,
                            )
                        ) {
                            launchSingleTop = true
                        }
                    },
                    GroupMenuItem(R.string.fsl_group_menu_duplicate) {

                    },
                    GroupMenuItem(R.string.delete) {

                    },
                ),
                function = GroupedShoppingListScreenFunction(
                    shared = sharedFunction,
                    onAddFabClicked = onAddShoppingListItemClicked,
                    function = GroupedShoppingListCardFunction(
                        onItemClicked = {
                            // TODO: ...
                        },
                        onSeeAllClicked = {
                            navController.navigate(
                                Routes.ShoppingList.LIST.buildRoute(
                                    "group" to it.group,
                                    "groupBy" to it.groupBy,
                                )
                            )
                        },
                    ),
                ),
                drawerContent = drawerContent,
            )
        }
        composable(
            route = Routes.ShoppingList.LIST,
            arguments = listOf(
                navArgument("group") {
                    nullable = true
                },
                navArgument("groupBy") {
                    defaultValue = GroupBy.DateAdded.name
                },
            ),
        ) {
            ShoppingListScreen(
                modifier = Modifier.fillMaxSize(),
                menuItems = shoppingListItemMenuItems,
                group = if (it.arguments?.get("group") == null) {
                    null
                } else {
                    ShoppingListGroup(
                        group = it.arguments!!.get("group")!!,
                        groupBy = it.arguments?.getString("groupBy")
                            ?.let { it1 -> GroupBy.valueOf(it1) } ?: GroupBy.DateAdded,
                    )
                },
                function = ShoppingListScreenFunction(
                    onAddClicked = onAddShoppingListItemClicked,
                    onNavigationIconClicked = sharedFunction.onNavigationIconClicked,
                    onItemClicked = {},
                ),
                optionsMenu = listOf(
                    OptionMenu(
                        title = stringResource(R.string.fsl_group_menu_recommend),
                        onClick = {
                            // TODO: Rethink implementation...
                            // onRecommendOptionsMenuClicked(shoppingListResult.getOrNull())
                        },
                    ),
                    OptionMenu(title = stringResource(R.string.refresh)),
                ),
            )
        }
        composable(
            route = Routes.ShoppingList.DETAIL,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                },
            ),
        ) {
            ShoppingListItemScreen(
                modifier = Modifier.fillMaxSize(),
                id = it.arguments?.getLong("id") ?: ShoppingListItem.default().id,
                function = ShoppingListItemScreenFunction(sharedFunction = sharedFunction),
            )
        }
    }
}