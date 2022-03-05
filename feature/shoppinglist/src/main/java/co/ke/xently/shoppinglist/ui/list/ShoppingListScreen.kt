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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.feature.ui.PagedDataScreen
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.feature.ui.stringRes
import co.ke.xently.shoppinglist.R
import co.ke.xently.shoppinglist.ui.list.item.MenuItem
import co.ke.xently.shoppinglist.ui.list.item.ShoppingListItemCard

internal data class ShoppingListScreenFunction(
    val onAddClicked: () -> Unit = {},
    val onNavigationIconClicked: () -> Unit = {},
    val onItemClicked: (ShoppingListItem) -> Unit = {},
)

@Composable
internal fun ShoppingListScreen(
    modifier: Modifier,
    menuItems: List<MenuItem>,
    function: ShoppingListScreenFunction,
    viewModel: ShoppingListViewModel = hiltViewModel(),
) {
    ShoppingListScreen(
        function = function,
        modifier = modifier,
        menuItems = menuItems,
        items = viewModel.pagingData.collectAsLazyPagingItems(),
    )
}

@Composable
private fun ShoppingListScreen(
    modifier: Modifier,
    items: LazyPagingItems<ShoppingListItem>,
    menuItems: List<MenuItem>,
    function: ShoppingListScreenFunction,
) {
    val scaffoldState = rememberScaffoldState()
    var showOptionsMenu by remember { mutableStateOf(false) }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                title = stringResource(R.string.fsl_toolbar_title),
                onNavigationIconClicked = function.onNavigationIconClicked,
            ) {
                IconButton(onClick = { showOptionsMenu = !showOptionsMenu }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More shopping list screen options menu",
                    )
                }
                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false },
                ) {
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
            FloatingActionButton(onClick = function.onAddClicked) {
                Icon(Icons.Default.Add,
                    stringRes(R.string.fsl_detail_screen_toolbar_title, R.string.add))
            }
        },
    ) {
        PagedDataScreen(
            modifier = modifier.padding(it),
            placeholder = { ShoppingListItem.default() },
            items = items,
            scaffoldState = scaffoldState,
            emptyListMessage = R.string.fsl_empty_shopping_list,
        ) { item ->
            ShoppingListItemCard(
                item = item,
                menuItems = menuItems,
                onClick = function.onItemClicked,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
