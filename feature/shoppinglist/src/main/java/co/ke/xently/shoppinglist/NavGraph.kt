package co.ke.xently.shoppinglist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.*
import androidx.navigation.compose.composable
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.feature.ui.NavMenuItem
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
    navController: NavHostController,
    recommendFrom: String,
    onAccountMenuClicked: () -> Unit,
    onShopMenuClicked: () -> Unit,
    onProductMenuClicked: () -> Unit,
    onNavigationIconClicked: () -> Unit,
) {
    navigation(
        route = Routes.ShoppingList.toString(),
        startDestination = Routes.ShoppingList.GROUPED,
    ) {
        val onShoppingListItemRecommendClicked: (id: Long) -> Unit = {
            navController.navigate(
                Routes.ShoppingList.Recommendation.FILTER.buildRoute(
                    "recommendBy" to it,
                    "from" to recommendFrom,
                )
            )
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
            val context = LocalContext.current
            GroupedShoppingListScreen(
                modifier = Modifier.fillMaxSize(),
                drawerItems = listOf(
                    NavMenuItem(
                        context = context,
                        label = R.string.drawer_menu_shopping_list,
                        icon = Icons.Default.List,
                        onClick = {
                            if (navController.currentDestination?.route != Routes.ShoppingList.GROUPED) {
                                navController.navigate(Routes.ShoppingList.GROUPED.buildRoute()) {
                                    launchSingleTop = true
                                }
                            }
                        },
                    ),
                    NavMenuItem(
                        // TODO: Show only if user is signed in
                        context = context,
                        label = R.string.drawer_menu_account,
                        icon = Icons.Default.Person,
                        onClick = onAccountMenuClicked,
                    ),
                    NavMenuItem(
                        context = context,
                        label = R.string.drawer_menu_shops,
                        icon = Icons.Default.Business,
                        onClick = onShopMenuClicked,
                    ),
                    NavMenuItem(
                        context = context,
                        label = R.string.drawer_menu_products,
                        icon = Icons.Default.Category,
                        onClick = onProductMenuClicked,
                    ),
                ),
                menuItems = shoppingListItemMenuItems,
                optionsMenu = listOf(
                    OptionMenu(title = stringResource(R.string.refresh)),
                ),
                groupMenuItems = listOf(
                    GroupMenuItem(R.string.fsl_group_menu_recommend) {
                        navController.navigate(
                            Routes.ShoppingList.Recommendation.FILTER.buildRoute(
                                "recommendBy" to it
                            )
                        )
                    },
                    GroupMenuItem(R.string.fsl_group_menu_duplicate) {

                    },
                    GroupMenuItem(R.string.delete) {

                    },
                ),
                function = GroupedShoppingListScreenFunction(
                    onAddFabClicked = onAddShoppingListItemClicked,
                    function = GroupedShoppingListCardFunction(
                        onItemClicked = {
                            // TODO: ...
                        },
                        onSeeAllClicked = {
                            navController.navigate(
                                Routes.ShoppingList.LIST.buildRoute(
                                    "group" to it.group,
                                    "groupBy" to it.groupBy
                                )
                            )
                        },
                    ),
                ),
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
                    onNavigationIconClicked = onNavigationIconClicked,
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
                function = ShoppingListItemScreenFunction(
                    onNavigationIconClicked = onNavigationIconClicked,
                ),
            )
        }
    }
}