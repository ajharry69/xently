package co.ke.xently.accounts

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
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
import co.ke.xently.data.User
import co.ke.xently.feature.utils.Routes
import co.ke.xently.feature.utils.buildRoute


@Composable
internal fun AccountNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onNavigationIconClicked: () -> Unit,
) {
    val context = LocalContext.current
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Routes.Account.PROFILE,
    ) {
        composable(Routes.Account.PROFILE) {
            ProfileScreen(
                modifier = Modifier.fillMaxSize(),
                click = ProfileScreenClick(
                    navigationIcon = onNavigationIconClicked,
                ),
            )
        }
        composable(
            route = Routes.Account.SIGN_IN,
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
                    uriPattern = Routes.Account.Deeplinks.SIGN_IN
                },
            ),
        ) { navBackStackEntry ->
            SignInScreen(
                modifier = Modifier.fillMaxSize(),
                auth = User.BasicAuth(
                    username = navBackStackEntry.arguments?.getString("username") ?: "",
                    password = navBackStackEntry.arguments?.getString("password") ?: "",
                ),
                function = SignInScreenFunction(
                    navigationIcon = onNavigationIconClicked,
                    forgotPassword = {
                        navController.navigate(Routes.Account.PASSWORD_RESET_REQUEST.buildRoute("email" to it)) {
                            launchSingleTop = true
                        }
                    },
                    signInSuccess = { user ->
                        if (user.isVerified) {
                            (context as ComponentActivity).finish()
                        } else {
                            navController.navigate(Routes.Account.VERIFY) {
                                launchSingleTop = true
                            }
                        }
                    },
                    createAccount = {
                        navController.navigate(
                            Routes.Account.SIGN_UP.buildRoute("username" to it.username,
                            "password" to it.password)) {
                            launchSingleTop = true
                        }
                    },
                ),
            )
        }
        composable(
            route = Routes.Account.SIGN_UP,
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
                    uriPattern = Routes.Account.Deeplinks.SIGN_UP
                },
            ),
        ) { navBackStackEntry ->
            SignUpScreen(
                function = SignUpScreenFunction(
                    navigationIcon = onNavigationIconClicked,
                    signUpSuccess = { user ->
                        if (user.isVerified) {
                            (context as ComponentActivity).finish()
                        } else {
                            navController.navigate(Routes.Account.VERIFY) {
                                launchSingleTop = true
                            }
                        }
                    },
                    signIn = {
                        Routes.Account.SIGN_IN.buildRoute("username" to it.username,
                            "password" to it.password).also { route ->
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
                auth = User.BasicAuth(
                    username = navBackStackEntry.arguments?.getString("username") ?: "",
                    password = navBackStackEntry.arguments?.getString("password") ?: "",
                ),
            )
        }
        composable(
            route = Routes.Account.VERIFY,
            arguments = listOf(
                navArgument("code") {
                    defaultValue = ""
                },
            ),
        ) { navBackStackEntry ->
            VerificationScreen(
                modifier = Modifier.fillMaxSize(),
                verificationCode = navBackStackEntry.arguments?.getString("code") ?: "",
                function = VerificationScreenFunction(
                    navigationIcon = onNavigationIconClicked,
                    verificationSuccess = {
                        if (it.isVerified) {
                            (context as ComponentActivity).finish()
                        } else {
                            Log.d(AccountActivity.TAG,
                                "ProductsNavHost: User account not verified successfully")
                        }
                    }
                ),
            )
        }
        composable(
            route = Routes.Account.PASSWORD_RESET_REQUEST,
            arguments = listOf(
                navArgument("email") {
                    defaultValue = ""
                },
            ),
        ) {
            PasswordResetRequestScreen(
                modifier = Modifier.fillMaxSize(),
                email = it.arguments?.getString("email") ?: "",
                function = PasswordResetRequestScreenFunction(
                    navigationIcon = onNavigationIconClicked,
                    requestSuccess = {
                        navController.navigate(Routes.Account.RESET_PASSWORD) {
                            launchSingleTop = true
                        }
                    },
                ),
            )
        }
        composable(
            route = Routes.Account.RESET_PASSWORD,
            arguments = listOf(
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
                        (context as ComponentActivity).finish()
                    },
                ),
            )
        }
    }
}