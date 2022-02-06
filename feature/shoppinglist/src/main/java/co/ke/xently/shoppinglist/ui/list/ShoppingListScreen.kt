package co.ke.xently.shoppinglist.ui.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.feature.ui.AppendOnPagedData
import co.ke.xently.feature.ui.PagedDataScreen
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.feature.ui.stringRes
import co.ke.xently.shoppinglist.R
import co.ke.xently.shoppinglist.ui.list.item.ShoppingListItemCard


@Composable
internal fun ShoppingListScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingListViewModel = hiltViewModel(),
    onShoppingListItemClicked: (itemId: Long) -> Unit,
    onRecommendClicked: (itemId: Long) -> Unit,
    onNavigationIconClicked: () -> Unit = {},
    onAddShoppingListItemClicked: () -> Unit = {},
) {
    val config = PagingConfig(20, enablePlaceholders = false)
    val items = viewModel.get(config).collectAsLazyPagingItems()
    ShoppingListScreen(
        modifier,
        items,
        onNavigationIconClicked,
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
    onShoppingListItemClicked: (itemId: Long) -> Unit,
    onRecommendClicked: (itemId: Long) -> Unit,
    onAddShoppingListItemClicked: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    var showOptionsMenu by remember { mutableStateOf(false) }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                stringResource(R.string.fsl_toolbar_title),
                onNavigationIconClicked,
            ) {
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
                        Text(stringResource(R.string.fsl_group_menu_recommend))
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddShoppingListItemClicked) {
                Icon(Icons.Default.Add,
                    stringRes(R.string.fsl_add_shopping_list_item_toolbar_title, R.string.add))
            }
        },
    ) {
        PagedDataScreen(modifier, pagingItems, R.string.fsl_empty_shopping_list) {
            items(pagingItems) {
                if (it != null) {
                    ShoppingListItemCard(
                        it,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        onUpdateRequested = onShoppingListItemClicked,
                        onRecommendClicked = onRecommendClicked,
                    )
                } // TODO: Show placeholders on null products...
            }
            item {
                AppendOnPagedData(pagingItems.loadState.append, scaffoldState)
            }
        }
    }
}
