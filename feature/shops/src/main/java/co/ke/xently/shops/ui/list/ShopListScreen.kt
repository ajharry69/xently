package co.ke.xently.shops.ui.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
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
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.ui.*
import co.ke.xently.shops.R
import co.ke.xently.shops.ui.list.item.MenuItem
import co.ke.xently.shops.ui.list.item.ShopListItem
import co.ke.xently.shops.ui.list.item.ShopListItemFunction

internal data class ShopListScreenFunction(
    val onAddFabClicked: () -> Unit = {},
    val sharedFunction: SharedFunction = SharedFunction(),
    val function: ShopListItemFunction = ShopListItemFunction(),
)

@Composable
internal fun ShopListScreen(
    modifier: Modifier,
    optionsMenu: List<OptionMenu>,
    function: ShopListScreenFunction,
    menuItems: @Composable (Shop) -> List<MenuItem>,
    viewModel: ShopListViewModel = hiltViewModel(),
) {
    val items = viewModel.pagingData.collectAsLazyPagingItems()
    ShopListScreen(
        items = items,
        function = function,
        modifier = modifier,
        menuItems = menuItems,
        optionsMenu = optionsMenu.map { menu ->
            if (menu.title == stringResource(R.string.refresh)) {
                menu.copy(onClick = items::refresh)
            } else {
                menu
            }
        },
    )
}

@Composable
private fun ShopListScreen(
    function: ShopListScreenFunction,
    modifier: Modifier,
    items: LazyPagingItems<Shop>,
    optionsMenu: List<OptionMenu>,
    menuItems: @Composable (Shop) -> List<MenuItem>,
) {
    val listState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        topBar = {
            ToolbarWithProgressbar(
                title = stringResource(R.string.title_activity_shops),
                onNavigationIconClicked = function.sharedFunction.onNavigationIconClicked,
            ) {
                OverflowOptionMenu(
                    menu = optionsMenu,
                    contentDescription = stringResource(R.string.fs_shop_list_overflow_menu_description),
                )
            }
        },
        floatingActionButton = {
            if (!listState.isScrollInProgress) {
                FloatingActionButton(onClick = function.onAddFabClicked) {
                    Icon(
                        Icons.Default.Add,
                        stringRes(R.string.fs_shop_detail_toolbar_title, R.string.add)
                    )
                }
            }
        },
    ) {
        PagedDataScreen(
            items = items,
            listState = listState,
            scaffoldState = scaffoldState,
            modifier = modifier.padding(it),
            placeholder = { Shop.default() },
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