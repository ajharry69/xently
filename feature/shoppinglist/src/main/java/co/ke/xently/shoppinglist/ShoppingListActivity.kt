package co.ke.xently.shoppinglist

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.feature.LocationService
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.NavMenuItem
import co.ke.xently.feature.ui.OptionMenu
import co.ke.xently.feature.utils.Routes
import co.ke.xently.feature.utils.buildRoute
import co.ke.xently.feature.viewmodels.LocationPermissionViewModel
import co.ke.xently.shoppinglist.Recommend.From
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
import co.ke.xently.shoppinglist.ui.list.recommendation.ShoppingListRecommendationScreen
import co.ke.xently.shoppinglist.ui.list.recommendation.ShoppingListRecommendationScreenFunction
import co.ke.xently.shoppinglist.ui.list.recommendation.item.RecommendationCardItemFunction
import co.ke.xently.shoppinglist.ui.list.recommendation.item.RecommendationCardItemMenuItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShoppingListActivity : AppCompatActivity() {
    private val viewModel: LocationPermissionViewModel by viewModels()

    private var locationService: LocationService? = null
    private var locationServiceBound: Boolean = false
    private var locationPermissionsGranted: Boolean = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            locationService = (service as? LocationService.LocalBinder)?.service?.also {
                locationServiceBound = true
                if (locationPermissionsGranted) it.subscribeToLocationUpdates()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            locationService = null
            locationServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XentlyTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
                    ShoppingListNavHost(
                        navController = navController,
                        onShopMenuClicked = {
                            Intent("co.ke.xently.action.SHOPS").also {
                                startActivity(it)
                            }
                        },
                        onProductMenuClicked = {
                            Intent("co.ke.xently.action.PRODUCTS").also {
                                startActivity(it)
                            }
                        },
                        onAccountMenuClicked = {
                            Intent("co.ke.xently.action.ACCOUNTS").also {
                                startActivity(it)
                            }
                        },
                    ) {
                        onBackPressed()
                    }
                }
            }
        }
        viewModel.locationPermissionsGranted.observe(this) {
            if (it && locationServiceBound && !locationPermissionsGranted) {
                locationService!!.subscribeToLocationUpdates()
            }
            locationPermissionsGranted = it
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, LocationService::class.java).also {
            bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        if (locationServiceBound) {
            unbindService(serviceConnection)
            locationServiceBound = false
        }
        super.onStop()
    }
}

@Composable
internal fun ShoppingListNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onShopMenuClicked: () -> Unit = {},
    onProductMenuClicked: () -> Unit = {},
    onAccountMenuClicked: () -> Unit = {},
    onNavigationIconClicked: () -> Unit = {},
) {
    val context = LocalContext.current
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Routes.ShoppingList.GROUPED,
    ) {
        val onShoppingListItemRecommendClicked: (id: Long) -> Unit = {
            navController.navigate(
                Routes.ShoppingList.RECOMMENDATION.buildRoute(
                    "recommendBy" to it,
                    "from" to From.Item
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
                        navController.navigate(Routes.ShoppingList.RECOMMENDATION.buildRoute("recommendBy" to it))
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
            route = Routes.ShoppingList.RECOMMENDATION,
            arguments = listOf(
                navArgument("recommendBy") {},
                navArgument("from") {
                    defaultValue = From.GroupedList.name
                },
            ),
        ) {
            ShoppingListRecommendationScreen(
                function = ShoppingListRecommendationScreenFunction(
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
                        label = R.string.fsl_recommendation_directions,
                        onClick = {

                        },
                    ),
                    RecommendationCardItemMenuItem(
                        label = R.string.fsl_recommendation_details,
                        onClick = {

                        },
                    ),
                ),
                modifier = Modifier.fillMaxSize(),
                recommend = Recommend(
                    it.arguments?.get("recommendBy")!!,
                    From.valueOf(it.arguments?.getString("from", From.GroupedList.name)!!),
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