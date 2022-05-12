package co.ke.xently

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.DrawerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import co.ke.xently.data.User
import co.ke.xently.feature.ui.NavDrawerGroupItem
import co.ke.xently.feature.ui.NavMenuItem
import co.ke.xently.feature.ui.NavigationDrawer
import co.ke.xently.shoppinglist.R

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
data class DrawerContentFunction(
    val onHelpClicked: () -> Unit = {},
    val onFeedbackClicked: () -> Unit = {},
    val onShopMenuClicked: () -> Unit = {},
    val onSignInOrOut: (User?) -> Unit = {},
    val onAccountMenuClicked: () -> Unit = {},
    val onProductMenuClicked: () -> Unit = {},
    val onShoppingListMenuClicked: () -> Unit = {},
    val onRecommendationMenuClicked: () -> Unit = {},
)

@Composable
@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
fun ColumnScope.DrawerContent(
    user: User?,
    drawerState: DrawerState,
    function: DrawerContentFunction,
) {
    val context = LocalContext.current
    NavigationDrawer(
        drawerState = drawerState,
        navGroups = listOf(
            NavDrawerGroupItem(
                items = listOf(
                    NavMenuItem(
                        context = context,
                        label = R.string.drawer_menu_shopping_list,
                        icon = Icons.Default.List,
                        onClick = function.onShoppingListMenuClicked,
                    ),
                    NavMenuItem(
                        // TODO: Show only if user is signed in
                        context = context,
                        label = R.string.drawer_menu_account,
                        icon = Icons.Default.Person,
                        onClick = function.onAccountMenuClicked,
                    ),
                    NavMenuItem(
                        context = context,
                        label = R.string.drawer_menu_shops,
                        icon = Icons.Default.Business,
                        onClick = function.onShopMenuClicked,
                    ),
                    NavMenuItem(
                        context = context,
                        label = R.string.drawer_menu_products,
                        icon = Icons.Default.Category,
                        onClick = function.onProductMenuClicked,
                    ),
                    NavMenuItem(
                        context = context,
                        label = R.string.drawer_menu_recommendation,
                        icon = Icons.Default.AltRoute,
                        onClick = function.onRecommendationMenuClicked,
                    ),
                ),
            ),
            NavDrawerGroupItem(
                checkable = false,
                title = stringResource(R.string.fsl_navigation_drawer_other_menu),
                items = listOf(
                    NavMenuItem(
                        onClick = function.onFeedbackClicked,
                        icon = Icons.Default.Feedback,
                        label = stringResource(R.string.fsl_drawer_menu_feedback),
                    ),
                    NavMenuItem(
                        onClick = function.onHelpClicked,
                        icon = Icons.Default.Help,
                        label = stringResource(R.string.fsl_drawer_menu_help),
                    ),
                    NavMenuItem(
                        onClick = {
                            function.onSignInOrOut.invoke(user)
                        },
                        icon = Icons.Default.ExitToApp,
                        label = stringResource(
                            if (user == null) {
                                R.string.fsl_drawer_menu_signin
                            } else {
                                R.string.fsl_drawer_menu_signout
                            },
                        ),
                    ),
                ),
            )
        ),
    ) {
        Image(
            painterResource(R.drawable.ic_launcher_background),
            null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}