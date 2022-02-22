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
import androidx.paging.compose.items
import co.ke.xently.data.Address
import co.ke.xently.feature.ui.AppendOnPagedData
import co.ke.xently.feature.ui.PagedDataScreen
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.shops.R
import co.ke.xently.shops.ui.list.addresses.item.AddressListItem
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun AddressListScreen(
    shopId: Long,
    modifier: Modifier = Modifier,
    viewModel: AddressListViewModel = hiltViewModel(),
    onNavigationIconClicked: () -> Unit = {},
) {
    val config = PagingConfig(20, enablePlaceholders = false)

    val addresses = viewModel.get(shopId, config).collectAsLazyPagingItems()
    var shopName by remember {
        mutableStateOf<String?>(null)
    }
    LaunchedEffect(shopId) {
        viewModel.getShopName(shopId).collectLatest {
            shopName = it
        }
    }
    AddressListScreen(modifier, addresses, shopName, onNavigationIconClicked)
}

@Composable
private fun AddressListScreen(
    modifier: Modifier = Modifier,
    addresses: LazyPagingItems<Address>,
    shopName: String? = null,
    onNavigationIconClicked: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                title = stringResource(R.string.fs_toolbar_title_addresses),
                onNavigationIconClicked = onNavigationIconClicked,
                subTitle = shopName,
            )
        },
    ) { paddingValues ->
        PagedDataScreen(modifier.padding(paddingValues), addresses) {
            items(addresses) { address ->
                if (address != null) {
                    AddressListItem(address, modifier = Modifier.fillMaxWidth())
                } // TODO: Show placeholders on null products...
            }
            item {
                AppendOnPagedData(addresses.loadState.append, scaffoldState)
            }
        }
    }
}
