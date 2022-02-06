package co.ke.xently.products.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import co.ke.xently.data.Product
import co.ke.xently.feature.ui.AppendOnPagedData
import co.ke.xently.feature.ui.prependOnPagedData
import co.ke.xently.products.R
import co.ke.xently.products.ui.list.item.ProductListItem

@Composable
internal fun ProductListScreen(
    modifier: Modifier = Modifier,
    viewModel: ProductListViewModel = hiltViewModel(),
    onUpdateRequested: (id: Long) -> Unit = {},
    onNavigationIconClicked: () -> Unit = {},
    onAddProductClicked: () -> Unit = {},
) {
    val config = PagingConfig(20, enablePlaceholders = false)
    val items = viewModel.get(config).collectAsLazyPagingItems()
    ProductListScreen(
        pagingItems = items,
        modifier = modifier,
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
    onUpdateRequested: (id: Long) -> Unit = {},
    onDeleteRequested: (id: Long) -> Unit = {},
    onNavigationIconClicked: () -> Unit = {},
    onAddProductClicked: () -> Unit = {},
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_activity_products)) },
                navigationIcon = {
                    IconButton(onClick = onNavigationIconClicked) {
                        Icon(
                            Icons.Default.ArrowBack,
                            stringResource(R.string.fp_navigation_icon_content_description),
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProductClicked) {
                Icon(
                    Icons.Default.Add,
                    stringResource(
                        R.string.fp_add_product_toolbar_title,
                        stringResource(R.string.fp_add),
                    )
                )
            }
        }
    ) {
        prependOnPagedData(modifier, pagingItems, R.string.fp_empty_product_list)?.also {
            return@Scaffold
        }

        LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
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