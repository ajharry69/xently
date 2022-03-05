package co.ke.xently.shops.ui.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import co.ke.xently.data.Shop
import co.ke.xently.feature.ui.PagedDataScreen
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.feature.ui.stringRes
import co.ke.xently.shops.R
import co.ke.xently.shops.ui.list.item.MenuItem
import co.ke.xently.shops.ui.list.item.ShopListItem
import co.ke.xently.shops.ui.list.item.ShopListItemFunction

internal data class ShopListScreenFunction(
    val onAddFabClicked: () -> Unit = {},
    val onNavigationIcon: () -> Unit = {},
    val function: ShopListItemFunction = ShopListItemFunction(),
)

@Composable
internal fun ShopListScreen(
    function: ShopListScreenFunction,
    modifier: Modifier,
    menuItems: @Composable (Shop) -> List<MenuItem>,
    viewModel: ShopListViewModel = hiltViewModel(),
) {
    ShopListScreen(
        function = function,
        modifier = modifier,
        menuItems = menuItems,
        items = viewModel.pagingData.collectAsLazyPagingItems(),
    )
}

@Composable
private fun ShopListScreen(
    function: ShopListScreenFunction,
    modifier: Modifier,
    items: LazyPagingItems<Shop>,
    menuItems: @Composable (Shop) -> List<MenuItem>,
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        topBar = {
            ToolbarWithProgressbar(
                title = stringResource(R.string.title_activity_shops),
                onNavigationIconClicked = function.onNavigationIcon,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = function.onAddFabClicked) {
                Icon(Icons.Default.Add, stringRes(R.string.fs_add_shop_toolbar_title, R.string.add))
            }
        },
    ) {
        PagedDataScreen(
            items = items,
            placeholder = { Shop.default() },
            scaffoldState = scaffoldState,
            modifier = modifier.padding(it),
        ) { shop ->
            ShopListItem(
                shop = shop,
                modifier = Modifier.fillMaxWidth(),
                function = function.function,
                menuItems = menuItems,
            )
        }
    }
}