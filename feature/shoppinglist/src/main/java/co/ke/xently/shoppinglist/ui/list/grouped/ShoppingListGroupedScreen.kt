package co.ke.xently.shoppinglist.ui.list.grouped

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.TaskResult
import co.ke.xently.data.errorMessage
import co.ke.xently.data.getOrThrow
import co.ke.xently.shoppinglist.R
import co.ke.xently.shoppinglist.ui.list.grouped.item.GroupedShoppingListCard
import kotlinx.coroutines.launch

@Composable
internal fun GroupedShoppingListScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingListGroupedViewModel = hiltViewModel(),
    onShoppingListItemClicked: (itemId: Long) -> Unit,
    onShoppingListItemRecommendClicked: (itemId: Long) -> Unit,
    onRecommendGroupClicked: (group: Any) -> Unit = {},
    onSeeAllClicked: (group: Any) -> Unit = {},
    onShopMenuClicked: (() -> Unit) = {},
    onProductMenuClicked: (() -> Unit) = {},
    onSignoutMenuClicked: (() -> Unit) = {},
) {
    val groupedShoppingListResult by viewModel.groupedShoppingListResult.collectAsState()
    val groupedShoppingListCount by viewModel.groupedShoppingListCount.collectAsState()

    GroupedShoppingListScreen(
        modifier,
        groupedShoppingListCount,
        groupedShoppingListResult,
        onShoppingListItemClicked,
        onShoppingListItemRecommendClicked,
        onRecommendGroupClicked,
        onSeeAllClicked,
        onShopMenuClicked,
        onProductMenuClicked,
        onSignoutMenuClicked,
    )
}

@Composable
private fun GroupedShoppingListScreen(
    modifier: Modifier,
    groupedShoppingListCount: Map<Any, Int>,
    groupedShoppingListResult: TaskResult<List<GroupedShoppingList>>,
    onShoppingListItemClicked: (itemId: Long) -> Unit,
    onShoppingListItemRecommendClicked: (itemId: Long) -> Unit,
    onRecommendGroupClicked: (group: Any) -> Unit,
    onSeeAllClicked: (group: Any) -> Unit,
    onShopMenuClicked: () -> Unit,
    onProductMenuClicked: () -> Unit,
    onSignoutMenuClicked: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.fsl_toolbar_title)) },
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
                    .height(176.dp)
                    .fillMaxWidth(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(IntrinsicSize.Min)
                    .clickable(role = Role.Tab) {
                        onShopMenuClicked()
                        coroutineScope.launch {
                            scaffoldState.drawerState.apply { if (isOpen) close() }
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(painterResource(R.drawable.ic_shops), null)
                Text(
                    stringResource(R.string.drawer_menu_shops),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(IntrinsicSize.Min)
                    .clickable(role = Role.Tab) {
                        onProductMenuClicked()
                        coroutineScope.launch {
                            scaffoldState.drawerState.apply { if (isOpen) close() }
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(painterResource(R.drawable.ic_products), null)
                Text(
                    stringResource(R.string.drawer_menu_products),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(IntrinsicSize.Min)
                    .clickable(role = Role.Tab) {
                        onSignoutMenuClicked()
                        coroutineScope.launch {
                            scaffoldState.drawerState.apply { if (isOpen) close() }
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Default.ExitToApp, null)
                Text(
                    stringResource(R.string.drawer_menu_signout),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) {
        when (groupedShoppingListResult) {
            is TaskResult.Error -> {
                Box(contentAlignment = Alignment.Center, modifier = modifier) {
                    Text(
                        groupedShoppingListResult.errorMessage
                            ?: stringResource(R.string.fsl_generic_error_message)
                    )
                }
            }
            TaskResult -> {
                Box(contentAlignment = Alignment.Center, modifier = modifier) {
                    CircularProgressIndicator()
                }
            }
            is TaskResult.Success -> {
                val groupedShoppingList = groupedShoppingListResult.getOrThrow()
                if (groupedShoppingList.isEmpty()) {
                    Box(contentAlignment = Alignment.Center, modifier = modifier) {
                        Text(text = stringResource(R.string.fsl_empty_shopping_list))
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