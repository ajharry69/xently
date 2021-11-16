package co.ke.xently.shops.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.data.errorMessage
import co.ke.xently.data.getOrThrow
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.shops.R
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
    val shopListResult by viewModel.shopListResult.collectAsState()
    ShopListScreen(
        shopListResult,
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
    shopListResult: TaskResult<List<Shop>>,
    modifier: Modifier = Modifier,
    onItemClicked: ((id: Long) -> Unit) = {},
    onProductsClicked: ((id: Long) -> Unit) = {},
    onAddressesClicked: ((id: Long) -> Unit) = {},
    onNavigationIconClicked: (() -> Unit) = {},
    onAddShopClicked: (() -> Unit) = {},
) {
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
        }
    ) {
        when (shopListResult) {
            is TaskResult.Error -> {
                Box(contentAlignment = Alignment.Center, modifier = modifier) {
                    Text(
                        shopListResult.errorMessage
                            ?: stringResource(R.string.fs_generic_error_message)
                    )
                }
            }
            TaskResult -> {
                Box(modifier = modifier, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Success -> {
                val shopList = shopListResult.getOrThrow()
                if (shopList.isEmpty()) {
                    Box(modifier = modifier, contentAlignment = Alignment.Center) {
                        Text(text = stringResource(id = R.string.fs_empty_shop_list))
                    }
                } else {
                    LazyColumn(
                        modifier = modifier,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(shopList) {
                            ShopListItem(
                                it,
                                modifier = Modifier.fillMaxWidth(),
                                onItemClicked = onItemClicked,
                                onProductsClicked = onProductsClicked,
                                onAddressesClicked = onAddressesClicked,
                            )
                        }
                    }
                }
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

@Preview(name = "Empty shop list")
@Composable
private fun ShopListEmptyPreview() {
    XentlyTheme {
        ShopListScreen(
            modifier = Modifier.fillMaxSize(),
            shopListResult = Success(emptyList())
        )
    }
}

@Preview(name = "Loading shop list")
@Composable
private fun ShopListNullPreview() {
    XentlyTheme {
        ShopListScreen(modifier = Modifier.fillMaxSize(), shopListResult = TaskResult.Loading)
    }
}

@Preview(name = "Non empty shop list")
@Composable
private fun ShopListNonEmptyListPreview() {
    XentlyTheme {
        ShopListScreen(
            modifier = Modifier.fillMaxSize(),
            shopListResult = Success(List(20) {
                Shop(
                    name = "Shop #${it + 1}",
                    taxPin = "P0001${it + 1}1222B",
                    productsCount = Random.nextInt(0, 500),
                    addressesCount = Random.nextInt(0, 50),
                )
            }),
        )
    }
}