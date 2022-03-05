package co.ke.xently.shops.ui.list.addresses

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import co.ke.xently.data.Address
import co.ke.xently.feature.ui.PagedDataScreen
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.shops.R
import co.ke.xently.shops.ui.list.addresses.item.AddressListItem
import co.ke.xently.shops.ui.list.addresses.item.AddressListItemFunction

internal data class AddressListScreenFunction(
    val onNavigationIcon: () -> Unit = {},
    val function: AddressListItemFunction = AddressListItemFunction(),
)

@Composable
internal fun AddressListScreen(
    shopId: Long,
    modifier: Modifier,
    function: AddressListScreenFunction,
    viewModel: AddressListViewModel = hiltViewModel(),
) {
    viewModel.setShopId(shopId)

    val scope = rememberCoroutineScope()
    val shopName by viewModel.shopName.collectAsState(
        context = scope.coroutineContext,
    )
    AddressListScreen(
        function = function,
        shopName = shopName,
        modifier = modifier,
        items = viewModel.pagingData.collectAsLazyPagingItems(),
    )
}

@Composable
private fun AddressListScreen(
    modifier: Modifier = Modifier,
    items: LazyPagingItems<Address>,
    shopName: String?,
    function: AddressListScreenFunction,
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                subTitle = shopName,
                onNavigationIconClicked = function.onNavigationIcon,
                title = stringResource(R.string.fs_toolbar_title_addresses),
            )
        },
    ) {
        PagedDataScreen(
            modifier = modifier.padding(it),
            placeholder = { Address.default() },
            items = items,
            scaffoldState = scaffoldState,
        ) { address ->
            AddressListItem(
                address = address,
                function = function.function,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
