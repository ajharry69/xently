package co.ke.xently.products.ui.list

import android.text.format.DateFormat
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import co.ke.xently.data.Product
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.utils.descriptive
import co.ke.xently.products.R
import kotlinx.coroutines.launch

@Composable
internal fun ProductListScreen(
    modifier: Modifier = Modifier,
    viewModel: ProductListViewModel = hiltViewModel(),
    onItemClicked: ((id: Long) -> Unit) = {},
    onProductsClicked: ((id: Long) -> Unit) = {},
    onAddressesClicked: ((id: Long) -> Unit) = {},
    onNavigationIconClicked: (() -> Unit) = {},
    onAddProductClicked: (() -> Unit) = {},
) {
    val config = PagingConfig(20, enablePlaceholders = false)
    val items = viewModel.getPagingData(config).collectAsLazyPagingItems()
    ProductListScreen(
        pagingItems = items,
        modifier = modifier,
        onItemClicked = onItemClicked,
        onProductsClicked = onProductsClicked,
        onAddressesClicked = onAddressesClicked,
        onNavigationIconClicked = onNavigationIconClicked,
        onAddProductClicked = onAddProductClicked,
    )
}

@Composable
private fun ProductListScreen(
    pagingItems: LazyPagingItems<Product>,
    modifier: Modifier = Modifier,
    onItemClicked: (id: Long) -> Unit = {},
    onProductsClicked: (id: Long) -> Unit = {},
    onAddressesClicked: (id: Long) -> Unit = {},
    onNavigationIconClicked: () -> Unit = {},
    onAddProductClicked: () -> Unit = {},
) {
    val genericErrorMessage = stringResource(R.string.fp_generic_error_message)
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.title_activity_products))
                },
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
                        Text(text = stringResource(id = R.string.fp_empty_product_list))
                    }
                }
            }
        }

        LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(pagingItems) {
                if (it != null) {
                    ProductListItem(
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

@OptIn(ExperimentalUnitApi::class)
@Composable
private fun ProductListItem(
    product: Product,
    modifier: Modifier = Modifier,
    showPopupMenu: Boolean = false,
    onItemClicked: ((id: Long) -> Unit) = {},
    onProductsClicked: ((id: Long) -> Unit) = {},
    onAddressesClicked: ((id: Long) -> Unit) = {},
) {
    var showDropMenu by remember { mutableStateOf(showPopupMenu) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .padding(start = 8.dp)
            .clickable { onItemClicked(product.id) },
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxWidth(),
                text = "${product.name}, ${product.unitQuantity} ${product.unit}",
                style = MaterialTheme.typography.body1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                DateFormat.getMediumDateFormat(LocalContext.current).format(product.datePurchased),
                style = MaterialTheme.typography.caption,
            )
        }
        Row(
            modifier = Modifier.width(IntrinsicSize.Min).padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${stringResource(co.ke.xently.feature.R.string.default_currency)}${product.unitPrice.descriptive()}", style = MaterialTheme.typography.subtitle2.copy(fontSize = TextUnit(18f, TextUnitType.Sp)))
            Box {
                IconButton(onClick = { showDropMenu = true }) {
                    Icon(
                        if (showDropMenu) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = stringResource(
                            R.string.fp_product_item_menu_content_description,
                            product.name,
                        ),
                    )
                }
                DropdownMenu(expanded = showDropMenu, onDismissRequest = { showDropMenu = false }) {
                    DropdownMenuItem(
                        onClick = {
                            onProductsClicked(product.id)
                            showDropMenu = false
                        },
                    ) { Text(text = stringResource(id = R.string.fp_product_item_menu_products)) }
                    DropdownMenuItem(
                        onClick = {
                            onAddressesClicked(product.id)
                            showDropMenu = false
                        },
                    ) { Text(text = stringResource(id = R.string.fp_product_item_menu_addresses)) }
                }
            }
        }
    }
}

@Preview("Product item", showBackground = true)
@Composable
private fun ProductListItemPreview() {
    XentlyTheme {
        ProductListItem(
            modifier = Modifier.fillMaxWidth(),
            product = Product(
                name = "Bread",
                unit = "grams",
                unitQuantity = 400f,
                unitPrice = 1_000.53f,
            ),
        )
    }
}