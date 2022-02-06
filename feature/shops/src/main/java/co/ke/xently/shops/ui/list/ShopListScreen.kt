package co.ke.xently.shops.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import co.ke.xently.data.Shop
import co.ke.xently.shops.R
import co.ke.xently.shops.ui.list.item.ShopListItem
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()
    val genericErrorMessage = stringResource(R.string.fs_generic_error_message)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.title_activity_shops))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigationIconClicked) {
                        Icon(
                            Icons.Default.ArrowBack,
                            stringResource(R.string.fs_navigation_icon_content_description),
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddShopClicked) {
                Icon(
                    Icons.Default.Add,
                    stringResource(
                        R.string.fs_add_shop_toolbar_title,
                        stringResource(R.string.fs_add),
                    )
                )
            }
        },
    ) {
        when (val refresh = pagingItems.loadState.refresh) {
            is LoadState.Loading -> {
                return@Scaffold Box(modifier = modifier, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is LoadState.Error -> {
                return@Scaffold Box(modifier = modifier, contentAlignment = Alignment.Center) {
                    Text(refresh.error.localizedMessage ?: genericErrorMessage)
                }
            }
            is LoadState.NotLoading -> {
                if (pagingItems.itemCount == 0) {
                    return@Scaffold Box(modifier = modifier, contentAlignment = Alignment.Center) {
                        Text(text = stringResource(id = R.string.fs_empty_shop_list))
                    }
                }
            }
        }

        LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            when (val result = pagingItems.loadState.append) {
                is LoadState.Loading -> item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
                is LoadState.Error -> coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(result.error.localizedMessage
                        ?: genericErrorMessage)
                }
                is LoadState.NotLoading -> Unit
            }
        }
    }
}