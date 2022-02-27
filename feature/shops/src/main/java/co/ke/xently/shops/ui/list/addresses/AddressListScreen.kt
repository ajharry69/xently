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
import co.ke.xently.data.Address
import co.ke.xently.feature.ui.PagedDataScreen
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.shops.R
import co.ke.xently.shops.ui.list.addresses.item.AddressListItem
import kotlinx.coroutines.flow.collectLatest

internal data class Click(
    val navigationIcon: () -> Unit = {},
    val click: co.ke.xently.shops.ui.list.addresses.item.Click = co.ke.xently.shops.ui.list.addresses.item.Click(),
)

@Composable
internal fun AddressListScreen(
    shopId: Long,
    click: Click,
    modifier: Modifier = Modifier,
    viewModel: AddressListViewModel = hiltViewModel(),
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
    AddressListScreen(modifier, addresses, shopName, click)
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
            items = items,
            scaffoldState = scaffoldState,
            modifier = modifier.padding(it),
            defaultItem = Address.default(),
        ) { address, modifier ->
            AddressListItem(
                address = address,
                click = click.click,
                modifier = modifier.fillMaxWidth(),
            )
        }
    }
}
