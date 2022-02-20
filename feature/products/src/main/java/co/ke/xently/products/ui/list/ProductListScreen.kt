package co.ke.xently.products.ui.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import co.ke.xently.data.Product
import co.ke.xently.feature.ui.AppendOnPagedData
import co.ke.xently.feature.ui.PagedDataScreen
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.feature.ui.stringRes
import co.ke.xently.products.R
import co.ke.xently.products.ui.list.item.ProductListItem
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun ProductListScreen(
    shopId: Long?,
    modifier: Modifier = Modifier,
    viewModel: ProductListViewModel = hiltViewModel(),
    onUpdateRequested: (id: Long) -> Unit = {},
    onNavigationIconClicked: () -> Unit = {},
    onAddProductClicked: () -> Unit = {},
) {
    val config = PagingConfig(20, enablePlaceholders = false)
    val items = viewModel.get(config, shopId).collectAsLazyPagingItems()
    var shopName by remember {
        mutableStateOf<String?>(null)
    }
    LaunchedEffect(shopId) {
        if (shopId != null) {
            viewModel.getShopName(shopId).collectLatest {
                shopName = it
            }
        }
    }
    ProductListScreen(
        pagingItems = items,
        modifier = modifier,
        shopName = shopName,
        onUpdateRequested = onUpdateRequested,
        onDeleteRequested = { /* TODO: Delete should only be permitted to superusers */ },
        onNavigationIconClicked = onNavigationIconClicked,
        onAddProductClicked = onAddProductClicked,
    )
}

@Composable
private fun ProductListScreen(
    pagingItems: LazyPagingItems<Product>,
    modifier: Modifier = Modifier,
    shopName: String? = null,
    onUpdateRequested: (id: Long) -> Unit = {},
    onDeleteRequested: (id: Long) -> Unit = {},
    onNavigationIconClicked: () -> Unit = {},
    onAddProductClicked: () -> Unit = {},
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                title = stringResource(R.string.title_activity_products),
                subTitle = shopName,
                onNavigationIconClicked = onNavigationIconClicked,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProductClicked) {
                Icon(Icons.Default.Add,
                    stringRes(R.string.fp_add_product_toolbar_title, R.string.add))
            }
        }
    ) {
        PagedDataScreen(modifier, pagingItems) {
            items(pagingItems) {
                if (it != null) {
                    ProductListItem(
                        it,
                        modifier = Modifier.fillMaxWidth(),
                        onUpdateRequested = onUpdateRequested,
                        onDeleteRequested = onDeleteRequested,
                    )
                } // TODO: Show placeholders on null products...
            }
            item {
                AppendOnPagedData(pagingItems.loadState.append, scaffoldState)
            }
        }
    }
}