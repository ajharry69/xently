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
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import co.ke.xently.data.Shop
import co.ke.xently.feature.ui.PagedDataScreen
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.feature.ui.stringRes
import co.ke.xently.shops.R
import co.ke.xently.shops.ui.list.item.MenuItem
import co.ke.xently.shops.ui.list.item.ShopListItem

internal data class Click(
    val add: () -> Unit = {},
    val navigationIcon: () -> Unit = {},
    val click: co.ke.xently.shops.ui.list.item.Click = co.ke.xently.shops.ui.list.item.Click(),
)

@Composable
internal fun ShopListScreen(
    click: Click,
    menuItems: @Composable (Shop) -> List<MenuItem>,
    modifier: Modifier = Modifier,
    viewModel: ShopListViewModel = hiltViewModel(),
) {
    val config = PagingConfig(20, enablePlaceholders = false)
    val items = viewModel.get(config).collectAsLazyPagingItems()
    ShopListScreen(
        items = items,
        modifier = modifier,
        menuItems = menuItems,
        click = click,
    )
}

@Composable
private fun ShopListScreen(
    items: LazyPagingItems<Shop>,
    modifier: Modifier = Modifier,
    menuItems: @Composable (Shop) -> List<MenuItem>,
    click: Click,
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        topBar = {
            ToolbarWithProgressbar(
                title = stringResource(R.string.title_activity_shops),
                onNavigationIconClicked = click.navigationIcon,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = click.add) {
                Icon(Icons.Default.Add, stringRes(R.string.fs_add_shop_toolbar_title, R.string.add))
            }
        },
    ) {
        PagedDataScreen(
            items = items,
            defaultItem = Shop.default(),
            scaffoldState = scaffoldState,
            modifier = modifier.padding(it),
        ) { shop, modifier ->
            ShopListItem(
                shop = shop,
                modifier = modifier.fillMaxWidth(),
                click = click.click,
                menuItems = menuItems,
            )
        }
    }
}