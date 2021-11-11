package co.ke.xently.shoppinglist

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import co.ke.xently.shoppinglist.Recommend.From
import co.ke.xently.shoppinglist.ui.detail.ShoppingListItemScreen
import co.ke.xently.shoppinglist.ui.list.ShoppingListScreen
import co.ke.xently.shoppinglist.ui.list.grouped.ShoppingListGroupedScreen
import co.ke.xently.shoppinglist.ui.list.recommendation.ShoppingListRecommendationScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShoppingListActivity : ComponentActivity() {
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
                ShoppingListNavHost()
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
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = "shopping-list-grouped",
        modifier = modifier,
    ) {
        val onShoppingListItemRecommendClicked: (itemId: Long) -> Unit = {
            navController.navigate("shopping-list/recommendations/${it}?from=${From.Item}")
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
                defaultValue = From.GroupedList.name
            })
        ) {
            ShoppingListRecommendationScreen(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                recommend = Recommend(
                    it.arguments?.get("recommendBy")!!,
                    From.valueOf(it.arguments?.getString("from", From.GroupedList.name)!!),
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