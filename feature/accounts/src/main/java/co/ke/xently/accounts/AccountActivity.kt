package co.ke.xently.accounts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.ke.xently.accounts.ui.password_reset.PasswordResetScreen
import co.ke.xently.accounts.ui.password_reset.PasswordResetScreenFunction
import co.ke.xently.accounts.ui.password_reset.request.PasswordResetRequestScreen
import co.ke.xently.accounts.ui.password_reset.request.PasswordResetRequestScreenFunction
import co.ke.xently.accounts.ui.profile.ProfileScreen
import co.ke.xently.accounts.ui.profile.ProfileScreenClick
import co.ke.xently.accounts.ui.signin.SignInScreen
import co.ke.xently.accounts.ui.signin.SignInScreenFunction
import co.ke.xently.accounts.ui.signup.SignUpScreen
import co.ke.xently.accounts.ui.signup.SignUpScreenFunction
import co.ke.xently.accounts.ui.verification.VerificationScreen
import co.ke.xently.accounts.ui.verification.VerificationScreenFunction
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
                        onBackPressed()
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
    NavHost(modifier = modifier, navController = navController, startDestination = "profile") {
        composable("profile") {
            ProfileScreen(
                modifier = Modifier.fillMaxSize(),
                click = ProfileScreenClick(
                    navigationIcon = onNavigationIconClicked,
                ),
            )
        }
        composable(
            "signin?username={username}&password={password}",
            arguments = listOf(
                navArgument("username") {
                    defaultValue = ""
                },
                navArgument("password") {
                    defaultValue = ""
                },
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "xently://accounts/signin/"
                }
            ),
        ) {
            SignInScreen(
                modifier = Modifier.fillMaxSize(),
                username = it.arguments?.getString("username") ?: "",
                password = it.arguments?.getString("password") ?: "",
                function = SignInScreenFunction(
                    navigationIcon = onNavigationIconClicked,
                    forgotPassword = {
                        navController.navigate("request-password-reset")
                    },
                    signInSuccess = { user ->
                        if (user.isVerified) {
                            // TODO: Navigate to home screen...
                            onNavigationIconClicked()
                        } else {
                            navController.navigate("verify-account")
                        }
                    },
                    createAccount = { username, password ->
                        navController.navigate("signup?username=${username}&password=${password}") {
                            launchSingleTop = true
                        }
                    },
                ),
            )
        }
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
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "xently://accounts/signup/"
                }
            ),
        ) {
            SignUpScreen(
                function = SignUpScreenFunction(
                    navigationIcon = onNavigationIconClicked,
                    signUpSuccess = { user ->
                        if (user.isVerified) {
                            // TODO: Navigate to home screen...
                            onNavigationIconClicked()
                        } else {
                            navController.navigate("verify-account")
                        }
                    },
                    signIn = { username, password ->
                        "signin?username=${username}&password=${password}".also { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                                popUpTo(route.substringBefore("?")) {
                                    inclusive = false
                                }
                            }
                        }
                    }
                ),
                modifier = Modifier.fillMaxSize(),
                username = it.arguments?.getString("username") ?: "",
                password = it.arguments?.getString("password") ?: "",
            )
        }
        composable(
            "verify-account?code={code}",
            listOf(navArgument("code") { defaultValue = "" }),
        ) {
            VerificationScreen(
                modifier = Modifier.fillMaxSize(),
                verificationCode = it.arguments?.getString("code") ?: "",
                function = VerificationScreenFunction(
                    navigationIcon = onNavigationIconClicked,
                    verificationSuccess = {
//                    navController.navigate("reset-password")
                        // TODO: Navigate to home page... Above code may not be necessary if the `this` activity is finished when starting a new activity
                    }
                ),
            )
        }
        composable(
            "request-password-reset?email={email}",
            listOf(navArgument("email") { defaultValue = "" }),
        ) {
            PasswordResetRequestScreen(
                modifier = Modifier.fillMaxSize(),
                email = it.arguments?.getString("email") ?: "",
                function = PasswordResetRequestScreenFunction(
                    navigationIcon = onNavigationIconClicked,
                    requestSuccess = {
                        navController.navigate("reset-password")
                    },
                ),
            )
        }
        composable(
            "reset-password?isChange={isChange}",
            listOf(
                navArgument("isChange") {
                    defaultValue = false
                    type = NavType.BoolType
                },
            ),
        ) {
            PasswordResetScreen(
                modifier = Modifier.fillMaxSize(),
                isChange = it.arguments?.getBoolean("isChange") ?: false,
                function = PasswordResetScreenFunction(
                    navigationIcon = onNavigationIconClicked,
                    resetSuccess = {
                        navController.popBackStack("reset-password", true)
                        // TODO: Navigate to home page... Above code may not be necessary if the `this` activity is finished when starting a new activity
                    },
                ),
            )
        }
    }
}