package co.ke.xently.products.ui.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import co.ke.xently.data.Product
import co.ke.xently.feature.ui.*
import co.ke.xently.products.R
import co.ke.xently.products.ui.list.item.MenuItem
import co.ke.xently.products.ui.list.item.ProductListItem

internal data class ProductListScreenFunction(
    val onAddFabClicked: () -> Unit = {},
    val onNavigationIconClicked: () -> Unit = {},
)

@Composable
internal fun ProductListScreen(
    shopId: Long?,
    modifier: Modifier,
    function: ProductListScreenFunction,
    menuItems: List<MenuItem>,
    optionsMenu: List<OptionMenu>,
    viewModel: ProductListViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val shopName by viewModel.shopName.collectAsState(
        context = scope.coroutineContext,
    )
    LaunchedEffect(shopId) {
        viewModel.setShopId(shopId)
    }
    val items = viewModel.pagingData.collectAsLazyPagingItems()
    ProductListScreen(
        items = items,
        modifier = modifier,
        shopName = shopName,
        function = function,
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
private fun ProductListScreen(
    modifier: Modifier,
    shopName: String?,
    function: ProductListScreenFunction,
    items: LazyPagingItems<Product>,
    menuItems: List<MenuItem>,
    optionsMenu: List<OptionMenu>,
) {
    val listState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                title = stringResource(R.string.title_activity_products),
                onNavigationIconClicked = function.onNavigationIconClicked,
                subTitle = shopName,
            ) {
                OverflowOptionMenu(
                    menu = optionsMenu,
                    contentDescription = stringResource(R.string.fp_product_list_overflow_menu_description),
                )
            }
        },
        floatingActionButton = {
            if (!listState.isScrollInProgress) {
                FloatingActionButton(onClick = function.onAddFabClicked) {
                    Icon(Icons.Default.Add,
                        stringRes(R.string.fp_add_product_toolbar_title, R.string.add))
                }
            }
        }
    ) {
        PagedDataScreen(
            items = items,
            listState = listState,
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