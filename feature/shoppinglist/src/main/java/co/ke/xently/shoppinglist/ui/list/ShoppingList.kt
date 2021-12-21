package co.ke.xently.shoppinglist.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
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
import co.ke.xently.data.*
import co.ke.xently.shoppinglist.R
import kotlinx.coroutines.launch


@Composable
internal fun ShoppingListScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingListViewModel = hiltViewModel(),
    onShoppingListItemClicked: (itemId: Long) -> Unit,
    onRecommendClicked: (itemId: Long) -> Unit,
    onRecommendOptionsMenuClicked: (List<ShoppingListItem>?) -> Unit,
    onNavigationIconClicked: () -> Unit = {},
    onAddShoppingListItemClicked: () -> Unit = {},
) {
    val config = PagingConfig(20, enablePlaceholders = false)
    val items = viewModel.getPagingData(config).collectAsLazyPagingItems()
    ShoppingListScreen(
        modifier,
        items,
        onNavigationIconClicked,
        onRecommendOptionsMenuClicked,
        onShoppingListItemClicked,
        onRecommendClicked,
        onAddShoppingListItemClicked,
    )
}

@Composable
private fun ShoppingListScreen(
    modifier: Modifier,
    pagingItems: LazyPagingItems<ShoppingListItem>,
    onNavigationIconClicked: () -> Unit,
    onRecommendOptionsMenuClicked: (List<ShoppingListItem>?) -> Unit,
    onShoppingListItemClicked: (itemId: Long) -> Unit,
    onRecommendClicked: (itemId: Long) -> Unit,
    onAddShoppingListItemClicked: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val genericErrorMessage = stringResource(R.string.fsl_generic_error_message)
    var showOptionsMenu by remember { mutableStateOf(false) }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.fsl_toolbar_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigationIconClicked) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.fsl_navigation_icon_content_description),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showOptionsMenu = !showOptionsMenu }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More shopping list screen options menu",
                        )
                    }
                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }) {
                        DropdownMenuItem(onClick = {
                            showOptionsMenu = false
                            // TODO: Rethink implementation...
                            // onRecommendOptionsMenuClicked(shoppingListResult.getOrNull())
                        }) {
                            Text(text = stringResource(R.string.fsl_group_menu_recommend))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddShoppingListItemClicked) {
                Icon(
                    Icons.Default.Add,
                    stringResource(
                        R.string.fsl_add_shopping_list_item_toolbar_title,
                        stringResource(R.string.fsl_add),
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
                        Text(text = stringResource(id = R.string.fsl_empty_shopping_list))
                    }
                }
            }
        }

        LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(pagingItems) {
                if (it != null) {
                    ShoppingListItemCard(
                        it,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        onItemClicked = onShoppingListItemClicked,
                        onRecommendClicked = onRecommendClicked,
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
internal fun ShoppingListItemCard(
    item: ShoppingListItem, modifier: Modifier = Modifier,
    onItemClicked: ((id: Long) -> Unit) = {},
    onRecommendClicked: ((id: Long) -> Unit) = {},
    onDeleteClicked: ((id: Long) -> Unit) = {},
) {
    var showDropMenu by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.clickable { onItemClicked(item.id) }) {
        Column {
            Text(
                modifier = Modifier.wrapContentWidth(),
                text = item.name,
                style = MaterialTheme.typography.body1
            )
            Text(
                text = "${item.unitQuantity} ${item.unit}",
                style = MaterialTheme.typography.caption
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${item.purchaseQuantity}", style = MaterialTheme.typography.h6)
            Box {
                IconButton(onClick = { showDropMenu = true }) {
                    Icon(
                        if (showDropMenu) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = "${item.name} shopping list item options"
                    )
                }
                DropdownMenu(expanded = showDropMenu, onDismissRequest = { showDropMenu = false }) {
                    DropdownMenuItem(
                        onClick = {
                            onRecommendClicked(item.id)
                            showDropMenu = false
                        },
                    ) { Text(text = stringResource(id = R.string.fsl_group_menu_recommend)) }
                    DropdownMenuItem(
                        onClick = {
                            onDeleteClicked(item.id)
                            showDropMenu = false
                        },
                    ) { Text(text = stringResource(id = R.string.fsl_group_menu_delete)) }
                }
            }
        }
    }
}
