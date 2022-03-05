package co.ke.xently.shoppinglist.ui.list.grouped

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.*
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
    val signInOrOut: () -> Unit = {},
    val onHelpClicked: () -> Unit = {},
    val onAddFabClicked: () -> Unit = {},
    val onFeedbackClicked: () -> Unit = {},
    val onRetryClicked: (Throwable) -> Unit = {},
    val function: GroupedShoppingListCardFunction = GroupedShoppingListCardFunction(),
)

@Composable
internal fun GroupedShoppingListScreen(
    modifier: Modifier,
    drawerItems: List<NavMenuItem>,
    menuItems: List<MenuItem>,
    groupMenuItems: List<GroupMenuItem>,
    function: GroupedShoppingListScreenFunction,
    viewModel: ShoppingListGroupedViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val result by viewModel.shoppingListResult.collectAsState(
        context = scope.coroutineContext,
    )
    val shoppingListCount by viewModel.shoppingListCount.collectAsState(
        context = scope.coroutineContext,
    )
    val user by viewModel.currentlyActiveUser.collectAsState(
        initial = null,
        context = scope.coroutineContext,
    )
    val signOutResult by viewModel.signOutResult.collectAsState(
        initial = TaskResult.Success(Unit),
        context = scope.coroutineContext,
    )
    val isRefreshing by viewModel.isRefreshing.collectAsState(
        context = scope.coroutineContext,
    )

    var groupBy by remember {
        mutableStateOf(GroupBy.DateAdded)
    }
    LaunchedEffect(true) {
        viewModel.setGroupBy(groupBy)
    }

    val context = LocalContext.current
    GroupedShoppingListScreen(
        user = user,
        result = result,
        modifier = modifier,
        menuItems = menuItems,
        drawerItems = drawerItems,
        isRefreshing = isRefreshing,
        signOutResult = signOutResult,
        groupCount = shoppingListCount,
        groupMenuItems = groupMenuItems,
        function = function.copy(
            onRefresh = viewModel::refresh,
            signInOrOut = {
                if (user == null) {
                    navigateToSignInScreen.invoke(context)
                } else {
                    viewModel.signOut()
                }
            },
            onRetryClicked = {
                groupBy = GroupBy.DateAdded
            },
        ),
    )
}

@Composable
private fun GroupedShoppingListScreen(
    user: User?,
    function: GroupedShoppingListScreenFunction,
    modifier: Modifier,
    menuItems: List<MenuItem>,
    drawerItems: List<NavMenuItem>,
    groupMenuItems: List<GroupMenuItem>,
    groupCount: Map<Any, Int>,
    signOutResult: TaskResult<Unit>,
    result: TaskResult<List<GroupedShoppingList>>,
    isRefreshing: Boolean,
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            if (user != null && !(result is TaskResult.Error && result.error.isAuthError())) {
                FloatingActionButton(onClick = function.onAddFabClicked) {
                    Icon(Icons.Default.Add,
                        stringRes(R.string.fsl_detail_screen_toolbar_title, R.string.add))
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
                },
                showProgress = signOutResult is TaskResult.Loading,
            )
        },
        drawerContent = {
            NavigationDrawer(
                drawerState = scaffoldState.drawerState,
                navGroups = listOf(
                    NavDrawerGroupItem(items = drawerItems),
                    NavDrawerGroupItem(
                        checkable = false,
                        title = stringResource(R.string.fsl_navigation_drawer_other_menu),
                        items = listOf(
                            NavMenuItem(
                                onClick = function.onFeedbackClicked,
                                icon = Icons.Default.Feedback,
                                label = stringResource(R.string.fsl_drawer_menu_feedback),
                            ),
                            NavMenuItem(
                                onClick = function.onHelpClicked,
                                icon = Icons.Default.Help,
                                label = stringResource(R.string.fsl_drawer_menu_help),
                            ),
                            NavMenuItem(
                                onClick = function.signInOrOut,
                                icon = Icons.Default.ExitToApp,
                                label = stringResource(
                                    if (user == null) {
                                        R.string.fsl_drawer_menu_signin
                                    } else {
                                        R.string.fsl_drawer_menu_signout
                                    },
                                ),
                            ),
                        ),
                    )
                ),
            ) {
                Image(
                    painterResource(R.drawable.ic_launcher_background),
                    null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        },
    ) { paddingValues ->
        val itemContent: @Composable (LazyItemScope.(GroupedShoppingList) -> Unit) = { groupList ->
            GroupedShoppingListCard(
                function = function.function,
                groupList = groupList,
                menuItems = menuItems,
                listCount = groupCount,
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
                        LazyColumn(modifier = modifier.padding(paddingValues)) {
                            items(groupedShoppingList, itemContent = itemContent)
                        }
                    }
                }
            }
        }
    }
}