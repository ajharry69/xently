package co.ke.xently.shoppinglist.ui.list.grouped

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.*
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.ui.*
import co.ke.xently.shoppinglist.GroupBy
import co.ke.xently.shoppinglist.R
import co.ke.xently.shoppinglist.ui.list.grouped.item.GroupMenuItem
import co.ke.xently.shoppinglist.ui.list.grouped.item.GroupedShoppingListCard
import co.ke.xently.shoppinglist.ui.list.grouped.item.GroupedShoppingListCardFunction
import co.ke.xently.shoppinglist.ui.list.item.MenuItem
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch

internal data class GroupedShoppingListScreenFunction(
    val onRefresh: () -> Unit = {},
    val onAddFabClicked: () -> Unit = {},
    val onRetryClicked: (Throwable) -> Unit = {},
    val shared: SharedFunction = SharedFunction(),
    val function: GroupedShoppingListCardFunction = GroupedShoppingListCardFunction(),
)

@Composable
internal fun GroupedShoppingListScreen(
    modifier: Modifier,
    menuItems: List<MenuItem>,
    optionsMenu: List<OptionMenu>,
    groupMenuItems: List<GroupMenuItem>,
    function: GroupedShoppingListScreenFunction,
    groupBy: GroupBy = GroupBy.DateAdded,
    viewModel: ShoppingListGroupedViewModel = hiltViewModel(),
    drawerContent: @Composable (ColumnScope.(DrawerState) -> Unit),
) {
    val scope = rememberCoroutineScope()
    val result by viewModel.shoppingListResult.collectAsState(
        context = scope.coroutineContext,
    )
    val shoppingListCount by viewModel.shoppingListCount.collectAsState(
        context = scope.coroutineContext,
    )
    val isRefreshing by viewModel.isRefreshing.collectAsState(
        context = scope.coroutineContext,
    )
    val user = function.shared.currentlyActiveUser.invoke()
    val signOutResult = function.shared.signOutResult.invoke()

    LaunchedEffect(groupBy) {
        viewModel.initFetch(groupBy)
    }

    GroupedShoppingListScreen(
        user = user,
        result = result,
        groupBy = groupBy,
        modifier = modifier,
        menuItems = menuItems,
        isRefreshing = isRefreshing,
        signOutResult = signOutResult,
        drawerContent = drawerContent,
        groupCount = shoppingListCount,
        groupMenuItems = groupMenuItems,
        optionsMenu = optionsMenu.map { menu ->
            if (menu.title == stringResource(R.string.refresh)) {
                menu.copy(onClick = viewModel::refresh)
            } else {
                menu
            }
        },
        function = function.copy(
            onRefresh = viewModel::refresh,
            onRetryClicked = {
                viewModel.initFetch(groupBy)
            },
        ),
    )
}

@Composable
private fun GroupedShoppingListScreen(
    user: User?,
    groupBy: GroupBy,
    modifier: Modifier,
    isRefreshing: Boolean,
    menuItems: List<MenuItem>,
    groupCount: Map<Any, Int>,
    optionsMenu: List<OptionMenu>,
    signOutResult: TaskResult<Unit>,
    groupMenuItems: List<GroupMenuItem>,
    function: GroupedShoppingListScreenFunction,
    result: TaskResult<List<GroupedShoppingList>>,
    drawerContent: @Composable (ColumnScope.(DrawerState) -> Unit),
) {
    val listState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            if (!listState.isScrollInProgress && user != null && !(result is TaskResult.Error && result.error.isAuthError())) {
                FloatingActionButton(onClick = function.onAddFabClicked) {
                    Icon(
                        Icons.Default.Add,
                        stringRes(R.string.fsl_detail_screen_toolbar_title, R.string.add)
                    )
                }
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
                    OverflowOptionMenu(
                        menu = optionsMenu,
                        contentDescription = stringResource(R.string.fsl_grouped_shopping_list_overflow_menu_description),
                    )
                },
                showProgress = signOutResult is TaskResult.Loading,
            )
        },
        drawerContent = {
            drawerContent(scaffoldState.drawerState)
        },
    ) { paddingValues ->
        val itemContent: @Composable (LazyItemScope.(GroupedShoppingList) -> Unit) = { groupList ->
            GroupedShoppingListCard(
                groupBy = groupBy,
                groupList = groupList,
                menuItems = menuItems,
                listCount = groupCount,
                function = function.function,
                groupMenuItems = groupMenuItems,
                showPlaceholder = groupList.isDefault,
            )
        }
        when (result) {
            is TaskResult.Error -> {
                FullscreenError(
                    error = result.error,
                    modifier = modifier.padding(paddingValues),
                    click = HttpErrorButtonClick(retryAble = function.onRetryClicked),
                )
            }
            TaskResult -> {
                FullscreenLoading(
                    placeholderContent = itemContent,
                    modifier = modifier.padding(paddingValues),
                    placeholder = { GroupedShoppingList.default() },
                    numberOfPlaceholders = PLACEHOLDER_COUNT_LARGE_ITEM_SIZE,
                )
            }
            is TaskResult.Success -> {
                val groupedShoppingList = result.getOrThrow()
                if (groupedShoppingList.isEmpty()) {
                    FullscreenEmptyList<ShoppingListItem>(
                        modifier = modifier.padding(paddingValues),
                        error = R.string.fsl_empty_shopping_list,
                    )
                } else {
                    SwipeRefresh(
                        onRefresh = function.onRefresh,
                        modifier = modifier.padding(paddingValues),
                        state = rememberSwipeRefreshState(isRefreshing),
                    ) {
                        LazyColumn(state = listState, modifier = modifier.padding(paddingValues)) {
                            items(groupedShoppingList, itemContent = itemContent)
                        }
                    }
                }
            }
        }
    }
}