package co.ke.xently.products.ui.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import co.ke.xently.data.Product
import co.ke.xently.feature.ui.PagedDataScreen
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.feature.ui.stringRes
import co.ke.xently.products.R
import co.ke.xently.products.ui.list.item.MenuItem
import co.ke.xently.products.ui.list.item.ProductListItem

internal data class ProductListScreenClick(
    val add: () -> Unit = {},
    val navigationIcon: () -> Unit = {},
)

@Composable
internal fun ProductListScreen(
    shopId: Long?,
    click: ProductListScreenClick,
    menuItems: List<MenuItem>,
    modifier: Modifier = Modifier,
    viewModel: ProductListViewModel = hiltViewModel(),
) {
    val items = viewModel.pagingData.collectAsLazyPagingItems()
    val shopName by viewModel.shopName.collectAsState()
    LaunchedEffect(shopId) {
        viewModel.setShopId(shopId)
    }
    ProductListScreen(
        modifier = modifier,
        shopName = shopName,
        click = click,
        items = items,
        menuItems = menuItems,
    )
}

@Composable
private fun ProductListScreen(
    modifier: Modifier,
    shopName: String?,
    click: ProductListScreenClick,
    items: LazyPagingItems<Product>,
    menuItems: List<MenuItem>,
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                title = stringResource(R.string.title_activity_products),
                onNavigationIconClicked = click.navigationIcon,
                subTitle = shopName,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = click.add) {
                Icon(Icons.Default.Add,
                    stringRes(R.string.fp_add_product_toolbar_title, R.string.add))
            }
        }
    ) {
        PagedDataScreen(
            items = items,
            scaffoldState = scaffoldState,
            modifier = modifier.padding(it),
            placeholder = { Product.default() },
        ) { product ->
            ProductListItem(
                product = product,
                menuItems = menuItems,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}