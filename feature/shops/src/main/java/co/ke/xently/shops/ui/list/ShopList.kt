package co.ke.xently.shops.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import co.ke.xently.data.Shop
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.shops.R
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
internal fun ShopListScreen(
    modifier: Modifier = Modifier,
    viewModel: ShopListViewModel = hiltViewModel(),
    onItemClicked: ((id: Long) -> Unit) = {},
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
        onItemClicked = onItemClicked,
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
    onItemClicked: ((id: Long) -> Unit) = {},
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
                        onItemClicked = onItemClicked,
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

@Composable
private fun ShopListItem(
    shop: Shop,
    modifier: Modifier = Modifier,
    showPopupMenu: Boolean = false,
    onItemClicked: ((id: Long) -> Unit) = {},
    onProductsClicked: ((id: Long) -> Unit) = {},
    onAddressesClicked: ((id: Long) -> Unit) = {},
) {
    val resources = LocalContext.current.resources
    var showDropMenu by remember { mutableStateOf(showPopupMenu) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .padding(start = 8.dp)
            .clickable { onItemClicked(shop.id) },
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxWidth(),
                text = shop.name,
                style = MaterialTheme.typography.body1
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = shop.taxPin, style = MaterialTheme.typography.caption)
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                )
                Text(
                    text = resources.getQuantityString(
                        R.plurals.fs_shop_item_locations,
                        shop.addressesCount,
                        shop.addressesCount
                    ), style = MaterialTheme.typography.caption
                )
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                )
                Text(
                    text = resources.getQuantityString(
                        R.plurals.fs_shop_item_products,
                        shop.productsCount,
                        shop.productsCount
                    ), style = MaterialTheme.typography.caption
                )
            }
        }
        Box(modifier = Modifier.width(IntrinsicSize.Min)) {
            IconButton(onClick = { showDropMenu = true }) {
                Icon(
                    if (showDropMenu) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = stringResource(
                        R.string.fs_shop_item_menu_content_description,
                        shop.name,
                    ),
                )
            }
            DropdownMenu(expanded = showDropMenu, onDismissRequest = { showDropMenu = false }) {
                DropdownMenuItem(
                    onClick = {
                        onProductsClicked(shop.id)
                        showDropMenu = false
                    },
                ) { Text(text = stringResource(id = R.string.fs_shop_item_menu_products)) }
                DropdownMenuItem(
                    onClick = {
                        onAddressesClicked(shop.id)
                        showDropMenu = false
                    },
                ) { Text(text = stringResource(id = R.string.fs_shop_item_menu_addresses)) }
            }
        }
    }
}

@Preview("Shop item", showBackground = true)
@Composable
private fun ShopListItemPreview() {
    XentlyTheme {
        ShopListItem(
            modifier = Modifier.fillMaxWidth(),
            shop = Shop(
                name = "Shop #1000",
                taxPin = "P000111222B",
                productsCount = Random.nextInt(0, 500),
                addressesCount = Random.nextInt(0, 50),
            ),
        )
    }
}

@Preview("Shop item with popup menu showing", showBackground = true)
@Composable
private fun ShopListItemPopupMenuShowingPreview() {
    XentlyTheme {
        ShopListItem(
            modifier = Modifier.fillMaxWidth(),
            showPopupMenu = true,
            shop = Shop(
                name = "Shop #1000",
                taxPin = "P000111222B",
                productsCount = Random.nextInt(0, 500),
                addressesCount = Random.nextInt(0, 50),
            ),
        )
    }
}