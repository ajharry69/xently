package co.ke.xently

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import co.ke.xently.accounts.accountsGraph
import co.ke.xently.feature.LocationService
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.utils.Routes
import co.ke.xently.feature.viewmodels.LocationPermissionViewModel
import co.ke.xently.products.productsGraph
import co.ke.xently.recommendation.recommendationGraph
import co.ke.xently.shoppinglist.shoppingListGraph
import co.ke.xently.shops.shopsGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XentlyTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Routes.ShoppingList.toString(),
                    ) {
                        shoppingListGraph(
                            navController = navController,
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
                            onNavigationIconClicked = this@MainActivity::onBackPressed,
                        )
                        recommendationGraph(
                            controller = navController,
                            onNavigationIconClicked = this@MainActivity::onBackPressed,
                        )
                        productsGraph(
                            navController = navController,
                            onNavigationIconClicked = this@MainActivity::onBackPressed,
                        )
                        accountsGraph(
                            navController = navController,
                            onNavigationIconClicked = this@MainActivity::onBackPressed,
                        )
                        shopsGraph(
                            navController = navController,
                            onNavigationIconClicked = this@MainActivity::onBackPressed,
                        )
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
}