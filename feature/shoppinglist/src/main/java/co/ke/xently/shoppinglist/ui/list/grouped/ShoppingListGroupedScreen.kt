package co.ke.xently.shoppinglist.ui.list.grouped

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.data.getOrThrow
import co.ke.xently.feature.ui.*
import co.ke.xently.shoppinglist.R
import co.ke.xently.shoppinglist.ui.list.grouped.item.GroupMenuItem
import co.ke.xently.shoppinglist.ui.list.grouped.item.GroupedShoppingListCard
import co.ke.xently.shoppinglist.ui.list.item.MenuItem
import kotlinx.coroutines.launch

internal data class Click(
    val add: () -> Unit = {},
    val click: co.ke.xently.shoppinglist.ui.list.grouped.item.Click = co.ke.xently.shoppinglist.ui.list.grouped.item.Click(),
)

@Composable
internal fun GroupedShoppingListScreen(
    drawerItems: List<NavDrawerItem>,
    menuItems: List<MenuItem>,
    groupMenuItems: List<GroupMenuItem>,
    click: Click,
    modifier: Modifier = Modifier,
    viewModel: ShoppingListGroupedViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val shoppingListResult by viewModel.shoppingListResult.collectAsState(scope.coroutineContext)
    val shoppingListCount by viewModel.shoppingListCount.collectAsState(scope.coroutineContext)

    GroupedShoppingListScreen(
        click = click,
        modifier = modifier,
        menuItems = menuItems,
        drawerItems = drawerItems,
        groupMenuItems = groupMenuItems,
        groupCount = shoppingListCount,
        result = shoppingListResult,
    )
}

@Composable
private fun GroupedShoppingListScreen(
    click: Click,
    modifier: Modifier,
    menuItems: List<MenuItem>,
    drawerItems: List<NavDrawerItem>,
    groupMenuItems: List<GroupMenuItem>,
    groupCount: Map<Any, Int>,
    result: TaskResult<List<GroupedShoppingList>>,
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(onClick = click.add) {
                Icon(Icons.Default.Add,
                    stringRes(R.string.fsl_detail_screen_toolbar_title, R.string.add))
            }
        },
        topBar = {
            ToolbarWithProgressbar(
                title = stringResource(R.string.fsl_toolbar_title),
                navigationIcon = {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = stringRes(
                            R.string.toggle_drawer_visibility,
                            if (scaffoldState.drawerState.isOpen) {
                                R.string.hide
                            } else {
                                R.string.show
                            },
                        ),
                    )
                },
                onNavigationIconClicked = {
                    coroutineScope.launch {
                        scaffoldState.drawerState.apply {
                            if (isClosed) {
                                open()
                            } else {
                                close()
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                },
            )
        },
        drawerContent = {
            Image(
                painterResource(R.drawable.ic_launcher_background),
                null,
                modifier = Modifier
                    .height(DRAWER_HEADER_HEIGHT)
                    .fillMaxWidth(),
            )
            NavigationDrawerGroup(
                drawerItems = drawerItems,
                modifier = Modifier.fillMaxWidth(),
                drawerState = scaffoldState.drawerState,
            )
        },
    ) { paddingValues ->
        when (result) {
            is TaskResult.Error -> {
                FullscreenError(modifier = modifier.padding(paddingValues), error = result.error)
            }
            TaskResult -> FullscreenLoading(modifier.padding(paddingValues))
            is TaskResult.Success -> {
                val groupedShoppingList = result.getOrThrow()
                if (groupedShoppingList.isEmpty()) {
                    FullscreenEmptyList<ShoppingListItem>(
                        modifier = modifier.padding(paddingValues),
                        error = R.string.fsl_empty_shopping_list,
                    )
                } else {
                    LazyColumn(modifier = modifier.padding(paddingValues)) {
                        items(groupedShoppingList) { groupList ->
                            GroupedShoppingListCard(
                                click = click.click,
                                groupList = groupList,
                                menuItems = menuItems,
                                listCount = groupCount,
                                groupMenuItems = groupMenuItems,
                            )
                        }
                    }
                }
            }
        }
    }
}