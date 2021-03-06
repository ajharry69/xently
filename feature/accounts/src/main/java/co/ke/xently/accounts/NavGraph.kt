package co.ke.xently.accounts

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.*
import androidx.navigation.compose.composable
import co.ke.xently.accounts.ui.password_reset.PasswordResetScreen
import co.ke.xently.accounts.ui.password_reset.PasswordResetScreenFunction
import co.ke.xently.accounts.ui.password_reset.request.PasswordResetRequestScreen
import co.ke.xently.accounts.ui.password_reset.request.PasswordResetRequestScreenFunction
import co.ke.xently.accounts.ui.profile.ProfileScreen
import co.ke.xently.accounts.ui.profile.ProfileScreenFunction
import co.ke.xently.accounts.ui.signin.SignInScreen
import co.ke.xently.accounts.ui.signin.SignInScreenFunction
import co.ke.xently.accounts.ui.signup.SignUpScreen
import co.ke.xently.accounts.ui.signup.SignUpScreenFunction
import co.ke.xently.accounts.ui.verification.VerificationScreen
import co.ke.xently.accounts.ui.verification.VerificationScreenFunction
import co.ke.xently.data.User
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.utils.Routes
import co.ke.xently.feature.utils.buildRoute

fun NavGraphBuilder.accountsGraph(
    sharedFunction: SharedFunction,
    navController: NavHostController,
) {
    navigation(
        route = Routes.Account.toString(),
        startDestination = Routes.Account.PROFILE,
    ) {

        composable(Routes.Account.PROFILE) {
            ProfileScreen(
                modifier = Modifier.fillMaxSize(),
                function = ProfileScreenFunction(sharedFunction = sharedFunction),
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
            val context = LocalContext.current
            SignInScreen(
                modifier = Modifier.fillMaxSize(),
                auth = User.BasicAuth(
                    username = navBackStackEntry.arguments?.getString("username") ?: "",
                    password = navBackStackEntry.arguments?.getString("password") ?: "",
                ),
                function = SignInScreenFunction(
                    sharedFunction = sharedFunction,
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
                            Routes.Account.SIGN_UP.buildRoute(
                                "username" to it.username,
                                "password" to it.password
                            )
                        ) {
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
            val context = LocalContext.current
            SignUpScreen(
                function = SignUpScreenFunction(
                    sharedFunction = sharedFunction,
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
                        Routes.Account.SIGN_IN.buildRoute(
                            "username" to it.username,
                            "password" to it.password
                        ).also { route ->
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
            val context = LocalContext.current
            VerificationScreen(
                modifier = Modifier.fillMaxSize(),
                verificationCode = navBackStackEntry.arguments?.getString("code") ?: "",
                function = VerificationScreenFunction(
                    sharedFunction = sharedFunction,
                    verificationSuccess = {
                        if (it.isVerified) {
                            (context as ComponentActivity).finish()
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
                    sharedFunction = sharedFunction,
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
            val context = LocalContext.current
            PasswordResetScreen(
                modifier = Modifier.fillMaxSize(),
                isChange = it.arguments?.getBoolean("isChange") ?: false,
                function = PasswordResetScreenFunction(
                    sharedFunction = sharedFunction,
                    resetSuccess = {
                        // TODO: Fix this...
                        (context as ComponentActivity).finish()
                    },
                ),
            )
        }
    }
}