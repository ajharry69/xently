package co.ke.xently.shops.ui.list.addresses

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

internal data class Click(
    val navigationIcon: () -> Unit = {},
    val click: co.ke.xently.shops.ui.list.addresses.item.Click = co.ke.xently.shops.ui.list.addresses.item.Click(),
)

@Composable
internal fun AddressListScreen(
    shopId: Long,
    click: Click,
    modifier: Modifier,
    viewModel: AddressListViewModel = hiltViewModel(),
) {
    LaunchedEffect(shopId) {
        viewModel.setShopId(shopId)
    }

    val shopName by viewModel.shopName.collectAsState()
    AddressListScreen(
        click = click,
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
    click: Click,
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                title = stringResource(R.string.fs_toolbar_title_addresses),
                onNavigationIconClicked = click.navigationIcon,
                subTitle = shopName,
            )
        },
    ) {
        PagedDataScreen(
            modifier = modifier.padding(it),
            defaultItem = Address.default(),
            items = items,
            scaffoldState = scaffoldState,
        ) { address, modifier ->
            AddressListItem(
                address = address,
                click = click.click,
                modifier = modifier.fillMaxWidth(),
            )
        }
    }
}
