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
import co.ke.xently.feature.viewmodels.LocationPermissionViewModel
import co.ke.xently.shoppinglist.Recommend.From
import co.ke.xently.shoppinglist.ui.detail.ShoppingListItemScreen
import co.ke.xently.shoppinglist.ui.list.ShoppingListScreen
import co.ke.xently.shoppinglist.ui.list.grouped.GroupedShoppingListScreen
import co.ke.xently.shoppinglist.ui.list.grouped.item.Click
import co.ke.xently.shoppinglist.ui.list.grouped.item.GroupMenuItem
import co.ke.xently.shoppinglist.ui.list.item.MenuItem
import co.ke.xently.shoppinglist.ui.list.recommendation.RecommendationCardItemClick
import co.ke.xently.shoppinglist.ui.list.recommendation.ShoppingListRecommendationScreen
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
                        if (!navController.navigateUp()) onBackPressed()
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
        startDestination = "shopping-list-grouped",
    ) {
        val onShoppingListItemRecommendClicked: (id: Long) -> Unit = {
            navController.navigate("shopping-list/recommendations/${it}?from=${From.Item}")
        }
        val onShoppingListItemClicked: (id: Long) -> Unit = {
            navController.navigate("shopping-list/${it}")
        }
        val onAddShoppingListItemClicked = {
            navController.navigate("shopping-list/${ShoppingListItem.default().id}")
        }
        val shoppingListItemMenuItems = listOf(
            MenuItem(R.string.fsl_group_menu_recommend, onShoppingListItemRecommendClicked),
            MenuItem(R.string.update, onShoppingListItemClicked),
            MenuItem(R.string.delete),
        )
        composable("shopping-list-grouped") {
            GroupedShoppingListScreen(
                drawerItems = listOf(
                    NavMenuItem(
                        context = context,
                        label = R.string.drawer_menu_shopping_list,
                        icon = Icons.Default.List,
                        onClick = {
                            if (navController.currentDestination?.route != "shopping-list-grouped") {
                                navController.navigate("shopping-list-grouped") {
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
                groupMenuItems = listOf(
                    GroupMenuItem(R.string.fsl_group_menu_recommend) {
                        navController.navigate("shopping-list/recommendations/${it}")
                    },
                    GroupMenuItem(R.string.fsl_group_menu_duplicate) {

                    },
                    GroupMenuItem(R.string.delete) {

                    },
                ),
                click = co.ke.xently.shoppinglist.ui.list.grouped.Click(
                    add = onAddShoppingListItemClicked,
                    click = Click(
                        item = {
                            // TODO: ...
                        },
                        seeAll = {
                            navController.navigate("shopping-list")
                        },
                    ),
                ),
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable("shopping-list") {
            ShoppingListScreen(
                modifier = Modifier.fillMaxSize(),
                menuItems = shoppingListItemMenuItems,
                click = co.ke.xently.shoppinglist.ui.list.Click(
                    add = onAddShoppingListItemClicked,
                    navigationIcon = onNavigationIconClicked,
                    item = {},
                )
            )
        }
        composable(
            "shopping-list/recommendations/{recommendBy}?from={from}",
            listOf(navArgument("recommendBy") {}, navArgument("from") {
                defaultValue = From.GroupedList.name
            })
        ) {
            ShoppingListRecommendationScreen(
                click = co.ke.xently.shoppinglist.ui.list.recommendation.Click(
                    item = {},
                    navigationIcon = onNavigationIconClicked,
                    recommendationItemClick = RecommendationCardItemClick(
                        base = {
                            // TODO: ...
                        },
                    ),
                ),
                menuItems = listOf(
                    co.ke.xently.shoppinglist.ui.list.recommendation.MenuItem(
                        label = R.string.fsl_recommendation_directions,
                        onClick = {

                        },
                    ),
                    co.ke.xently.shoppinglist.ui.list.recommendation.MenuItem(
                        label = R.string.fsl_recommendation_hits,
                        onClick = {

                        },
                    ),
                    co.ke.xently.shoppinglist.ui.list.recommendation.MenuItem(
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
            "shopping-list/{id}",
            listOf(
                navArgument("id") {
                    type = NavType.LongType
                },
            ),
        ) {
            ShoppingListItemScreen(
                it.arguments?.getLong("id"),
                modifier = Modifier.fillMaxSize(),
                onNavigationIconClicked = onNavigationIconClicked,
            )
        }
    }
}