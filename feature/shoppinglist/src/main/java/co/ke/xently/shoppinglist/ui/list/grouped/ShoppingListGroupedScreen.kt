package co.ke.xently.shoppinglist.ui.list.grouped

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.TaskResult
import co.ke.xently.data.errorMessage
import co.ke.xently.data.getOrThrow
import co.ke.xently.feature.ui.*
import co.ke.xently.shoppinglist.R
import co.ke.xently.shoppinglist.ui.list.grouped.item.GroupedShoppingListCard
import kotlinx.coroutines.launch

@Composable
internal fun GroupedShoppingListScreen(
    drawerItems: List<NavDrawerItem>,
    modifier: Modifier = Modifier,
    viewModel: ShoppingListGroupedViewModel = hiltViewModel(),
    onShoppingListItemClicked: (itemId: Long) -> Unit,
    onShoppingListItemRecommendClicked: (itemId: Long) -> Unit,
    onRecommendGroupClicked: (group: Any) -> Unit = {},
    onSeeAllClicked: (group: Any) -> Unit = {},
    onAddShoppingListItemClicked: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val groupedShoppingListResult by viewModel.groupedShoppingListResult.collectAsState(scope.coroutineContext)
    val groupedShoppingListCount by viewModel.groupedShoppingListCount.collectAsState(scope.coroutineContext)

    GroupedShoppingListScreen(
        modifier,
        groupedShoppingListCount,
        groupedShoppingListResult,
        drawerItems,
        onShoppingListItemClicked,
        onShoppingListItemRecommendClicked,
        onRecommendGroupClicked,
        onSeeAllClicked,
        onAddShoppingListItemClicked,
    )
}

@Composable
private fun GroupedShoppingListScreen(
    modifier: Modifier,
    groupedShoppingListCount: Map<Any, Int>,
    groupedShoppingListResult: TaskResult<List<GroupedShoppingList>>,
    drawerItems: List<NavDrawerItem>,
    onShoppingListItemClicked: (itemId: Long) -> Unit,
    onShoppingListItemRecommendClicked: (itemId: Long) -> Unit,
    onRecommendGroupClicked: (group: Any) -> Unit,
    onSeeAllClicked: (group: Any) -> Unit,
    onAddShoppingListItemClicked: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddShoppingListItemClicked) {
                Icon(Icons.Default.Add,
                    stringRes(R.string.fsl_detail_screen_toolbar_title, R.string.add))
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fsl_toolbar_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            scaffoldState.drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                }
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
    ) {
        when (groupedShoppingListResult) {
            is TaskResult.Error -> {
                FullscreenError(modifier, groupedShoppingListResult.errorMessage)
            }
            TaskResult -> FullscreenLoading(modifier)
            is TaskResult.Success -> {
                val groupedShoppingList = groupedShoppingListResult.getOrThrow()
                if (groupedShoppingList.isEmpty()) {
                    Box(contentAlignment = Alignment.Center, modifier = modifier) {
                        Text(stringResource(R.string.fsl_empty_shopping_list))
                    }
                } else {
                    LazyColumn(modifier = modifier) {
                        items(groupedShoppingList) { groupList ->
                            GroupedShoppingListCard(
                                groupList, groupedShoppingListCount,
                                onShoppingListItemClicked = onShoppingListItemClicked,
                                onShoppingListItemRecommendClicked = onShoppingListItemRecommendClicked,
                                onRecommendGroupClicked = onRecommendGroupClicked,
                                onSeeAllClicked = onSeeAllClicked,
                            )
                        }
                    }
                }
            }
        }
    }
}