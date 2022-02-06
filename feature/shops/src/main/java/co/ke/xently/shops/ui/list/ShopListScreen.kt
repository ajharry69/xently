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
import androidx.paging.compose.items
import co.ke.xently.data.Shop
import co.ke.xently.feature.ui.AppendOnPagedData
import co.ke.xently.feature.ui.PagedDataScreen
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.feature.ui.stringRes
import co.ke.xently.shops.R
import co.ke.xently.shops.ui.list.item.ShopListItem

@Composable
internal fun ShopListScreen(
    modifier: Modifier = Modifier,
    viewModel: ShopListViewModel = hiltViewModel(),
    onUpdateRequested: ((id: Long) -> Unit) = {},
    onProductsClicked: ((id: Long) -> Unit) = {},
    onAddressesClicked: ((id: Long) -> Unit) = {},
    onNavigationIconClicked: (() -> Unit) = {},
    onAddShopClicked: (() -> Unit) = {},
) {
    val config = PagingConfig(20, enablePlaceholders = false)
    val items = viewModel.get(config).collectAsLazyPagingItems()
    ShopListScreen(
        items,
        modifier = modifier,
        onUpdateRequested = onUpdateRequested,
        onProductsClicked = onProductsClicked,
        onAddressesClicked = onAddressesClicked,
        onNavigationIconClicked = onNavigationIconClicked,
        onAddShopClicked = onAddShopClicked,
    )
}

@Composable
private fun ShopListScreen(
    pagingItems: LazyPagingItems<Shop>,
    modifier: Modifier = Modifier,
    onUpdateRequested: ((id: Long) -> Unit) = {},
    onProductsClicked: ((id: Long) -> Unit) = {},
    onAddressesClicked: ((id: Long) -> Unit) = {},
    onNavigationIconClicked: (() -> Unit) = {},
    onAddShopClicked: (() -> Unit) = {},
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        topBar = {
            ToolbarWithProgressbar(
                stringResource(R.string.title_activity_shops),
                onNavigationIconClicked,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddShopClicked) {
                Icon(Icons.Default.Add, stringRes(R.string.fs_add_shop_toolbar_title, R.string.add))
            }
        },
    ) { paddingValues ->
        PagedDataScreen(modifier.padding(paddingValues), pagingItems) {
            items(pagingItems) {
                if (it != null) {
                    ShopListItem(
                        it,
                        modifier = Modifier.fillMaxWidth(),
                        onUpdateRequested = onUpdateRequested,
                        onProductsClicked = onProductsClicked,
                        onAddressesClicked = onAddressesClicked,
                    )
                } // TODO: Show placeholders on null products...
            }
            item {
                AppendOnPagedData(pagingItems.loadState.append, scaffoldState)
            }
        }
    }
}