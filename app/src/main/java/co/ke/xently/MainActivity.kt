package co.ke.xently

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.navigateToSignInScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XentlyTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val scope = rememberCoroutineScope()
                    val navController = rememberNavController()
                    XentlyNavHost(
                        navController = navController,
                        onSignInOrOut = {
                            if (it == null) {
                                navigateToSignInScreen.invoke(this)
                            } else {
                                viewModel.signOut()
                            }
                        },
                        sharedFunction = SharedFunction(
                            onNavigationIconClicked = this::onBackPressed,
                            currentlyActiveUser = {
                                val user by viewModel.currentlyActiveUser.collectAsState(
                                    initial = null,
                                    context = scope.coroutineContext,
                                )
                                user
                            },
                        ) {
                            val signOutResult: TaskResult<Unit> by viewModel.signOutResult.collectAsState(
                                initial = TaskResult.Success(Unit),
                                context = scope.coroutineContext,
                            )
                            signOutResult
                        },
                    )
                }
            }
        }
    }
}