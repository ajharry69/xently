package co.ke.xently.shops.ui.list.addresses

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import co.ke.xently.data.Address
import co.ke.xently.data.Shop
import co.ke.xently.feature.ui.AppendOnPagedData
import co.ke.xently.feature.ui.PagedDataScreen
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.shops.R
import co.ke.xently.shops.ui.list.addresses.item.AddressListItem

@Composable
internal fun AddressListScreen(
    shopId: Long,
    modifier: Modifier = Modifier,
    viewModel: AddressListViewModel = hiltViewModel(),
    onNavigationIconClicked: () -> Unit = {},
) {
    val config = PagingConfig(20, enablePlaceholders = false)

    val addresses = viewModel.get(shopId, config).collectAsLazyPagingItems()
    AddressListScreen(modifier, addresses, onNavigationIconClicked)
}

@Composable
private fun AddressListScreen(
    modifier: Modifier = Modifier,
    addresses: LazyPagingItems<Address>,
    onNavigationIconClicked: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    var shop by remember { mutableStateOf<Shop?>(null) }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                title = stringResource(R.string.fs_toolbar_title_addresses),
                onNavigationIconClicked = onNavigationIconClicked,
                subTitle = shop?.name ?: "",
            )
        },
    ) { paddingValues ->
        PagedDataScreen(modifier.padding(paddingValues), addresses) {
            itemsIndexed(addresses) { index, address ->
                if (address != null) {
                    if (index == 0) shop = address.shop
                    AddressListItem(address, modifier = Modifier.fillMaxWidth())
                } // TODO: Show placeholders on null products...
            }
            item {
                AppendOnPagedData(addresses.loadState.append, scaffoldState)
            }
        }
    }
}
