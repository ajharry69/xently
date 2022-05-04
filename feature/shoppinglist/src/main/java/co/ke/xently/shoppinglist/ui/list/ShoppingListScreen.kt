package co.ke.xently.shoppinglist.ui.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.feature.ui.*
import co.ke.xently.shoppinglist.R
import co.ke.xently.shoppinglist.repository.ShoppingListGroup
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
    optionsMenu: List<OptionMenu>,
    group: ShoppingListGroup? = null,
    viewModel: ShoppingListViewModel = hiltViewModel(),
) {
    val items = viewModel.pagingData.collectAsLazyPagingItems()
    LaunchedEffect(group) {
        viewModel.setGroup(group)
    }
    ShoppingListScreen(
        items = items,
        group = group,
        function = function,
        modifier = modifier,
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
private fun ShoppingListScreen(
    modifier: Modifier,
    group: ShoppingListGroup?,
    menuItems: List<MenuItem>,
    optionsMenu: List<OptionMenu>,
    function: ShoppingListScreenFunction,
    items: LazyPagingItems<ShoppingListItem>,
) {
    val listState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                subTitle = group?.group?.toString(),
                title = stringResource(R.string.fsl_toolbar_title),
                onNavigationIconClicked = function.onNavigationIconClicked,
            ) {
                OverflowOptionMenu(
                    menu = optionsMenu,
                    contentDescription = stringResource(R.string.fsl_shopping_list_overflow_menu_description),
                )
            }
        },
        floatingActionButton = {
            if (!listState.isScrollInProgress) {
                FloatingActionButton(onClick = function.onAddClicked) {
                    Icon(
                        Icons.Default.Add,
                        stringRes(R.string.fsl_detail_screen_toolbar_title, R.string.add)
                    )
                }
            }
        },
    ) {
        PagedDataScreen(
            items = items,
            listState = listState,
            scaffoldState = scaffoldState,
            modifier = modifier.padding(it),
            placeholder = { ShoppingListItem.default() },
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
