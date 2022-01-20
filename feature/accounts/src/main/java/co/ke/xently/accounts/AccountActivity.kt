package co.ke.xently.accounts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.ke.xently.accounts.ui.signin.SignInScreen
import co.ke.xently.accounts.ui.signup.SignUpScreen
import co.ke.xently.feature.theme.XentlyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XentlyTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    val navController = rememberNavController()
                    ProductsNavHost(navController = navController) {
                        if (!navController.navigateUp()) onBackPressed()
                    }
                }
            }
        }
    }
}

@Composable
internal fun ProductsNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onNavigationIconClicked: () -> Unit,
) {
    NavHost(modifier = modifier, navController = navController, startDestination = "signin") {
        val signInScreen: @Composable (NavBackStackEntry) -> Unit = {
            SignInScreen(
                modifier = Modifier.fillMaxSize(),
                username = it.arguments?.getString("username") ?: "",
                password = it.arguments?.getString("password") ?: "",
                onNavigationIconClicked = onNavigationIconClicked,
                onSuccessfulSignIn = {
                    // TODO: Check if user is verified then navigate to verification screen if need be
                    onNavigationIconClicked()
                },
                onCreateAccountButtonClicked = { username, password ->
                    navController.navigate("signup?username=${username}&password=${password}") {
                        launchSingleTop = true
                    }
                },
                onForgotPasswordButtonClicked = {
                    // TODO: Navigate to request password reset screen...
                },
            )
        }
        composable("signin", content = signInScreen)
        composable(
            "signin?username={username}&password={password}",
            listOf(
                navArgument("username") {
                    defaultValue = ""
                },
                navArgument("password") {
                    defaultValue = ""
                },
            ),
            content = signInScreen,
        )
        composable(
            "signup?username={username}&password={password}",
            listOf(
                navArgument("username") {
                    defaultValue = ""
                },
                navArgument("password") {
                    defaultValue = ""
                },
            ),
        ) {
            SignUpScreen(
                modifier = Modifier.fillMaxSize(),
                username = it.arguments?.getString("username") ?: "",
                password = it.arguments?.getString("password") ?: "",
                onNavigationIconClicked = onNavigationIconClicked,
                onSuccessfulSignUp = {
                    // TODO: Check if user is verified then navigate to verification screen if need be
                    onNavigationIconClicked()
                },
                onSignInButtonClicked = { username, password ->
                    navController.navigate("signin?username=${username}&password=${password}") {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}