package co.ke.xently.shoppinglist.ui.list.grouped

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.*
import co.ke.xently.shoppinglist.R
import co.ke.xently.shoppinglist.ui.list.ShoppingListItemCard
import kotlinx.coroutines.launch
import java.util.*

@Composable
internal fun GroupedShoppingListScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingListGroupedViewModel = hiltViewModel(),
    onShoppingListItemClicked: (itemId: Long) -> Unit,
    onShoppingListItemRecommendClicked: (itemId: Long) -> Unit,
    onRecommendGroupClicked: (group: Any) -> Unit = {},
    onSeeAllClicked: (group: Any) -> Unit = {},
    onShopMenuClicked: (() -> Unit) = {},
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
                    .height(IntrinsicSize.Min)
                    .clickable(role = Role.Tab) {
                        onShopMenuClicked()
                        coroutineScope.launch {
                            scaffoldState.drawerState.apply { if (isOpen) close() }
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_shop), null, modifier = Modifier.padding(16.dp))
                Text(
                    stringResource(R.string.drawer_menu_shops),
                    style = MaterialTheme.typography.button,
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

@Composable
private fun GroupedShoppingListCard(
    groupList: GroupedShoppingList,
    listCount: Map<Any, Int>,
    onShoppingListItemClicked: ((itemId: Long) -> Unit) = {},
    onShoppingListItemRecommendClicked: ((itemId: Long) -> Unit) = {},
    onRecommendGroupClicked: (group: Any) -> Unit = {},
    onDuplicateGroupClicked: (group: Any) -> Unit = {},
    onDeleteGroupClicked: (group: Any) -> Unit = {},
    onSeeAllClicked: (group: Any) -> Unit = {},
) {
    val itemsPerCard = 3
    var showDropDownMenu by remember { mutableStateOf(false) }
    val numberOfItems = listCount.getOrElse(groupList.group) { groupList.numberOfItems }

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .padding(start = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(text = groupList.group, style = MaterialTheme.typography.h6)
                    Text(
                        text = LocalContext.current.resources.getQuantityString(
                            R.plurals.fsl_group_items_count,
                            numberOfItems, numberOfItems
                        ), style = MaterialTheme.typography.subtitle2
                    )
                }
                Box(modifier = Modifier.align(Alignment.Top)) {
                    IconButton(onClick = { showDropDownMenu = true }) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.fsl_group_card_menu_content_desc_more)
                        )
                    }
                    DropdownMenu(
                        expanded = showDropDownMenu,
                        onDismissRequest = { showDropDownMenu = false },
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onRecommendGroupClicked(groupList.group)
                                showDropDownMenu = false
                            },
                        ) { Text(text = stringResource(R.string.fsl_group_menu_recommend)) }
                        DropdownMenuItem(
                            onClick = {
                                onDuplicateGroupClicked(groupList.group)
                                showDropDownMenu = false
                            },
                        ) { Text(text = stringResource(R.string.fsl_group_menu_duplicate)) }
                        DropdownMenuItem(
                            onClick = {
                                onDeleteGroupClicked(groupList.group)
                                showDropDownMenu = false
                            },
                        ) { Text(text = stringResource(R.string.fsl_group_menu_delete)) }
                    }
                }
            }
            Divider(
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                for (item in groupList.shoppingList.take(itemsPerCard)) {
                    ShoppingListItemCard(
                        item,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        onItemClicked = onShoppingListItemClicked,
                        onRecommendClicked = onShoppingListItemRecommendClicked,
                    )
                }
            }
            if (numberOfItems > itemsPerCard) {
                OutlinedButton(
                    onClick = { onSeeAllClicked(groupList.group) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.fsl_group_button_see_all),
                        style = MaterialTheme.typography.button
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun GroupedShoppingListCardPreview() {
    val shoppingList = listOf(
        ShoppingListItem(1L, "Bread", "grams", 400f, 1f, Date()),
        ShoppingListItem(2L, "Milk", "litres", 1f, 1f, Date()),
        ShoppingListItem(3L, "Sugar", "kilograms", 2f, 1f, Date()),
        ShoppingListItem(4L, "Toothpaste", "millilitres", 75f, 1f, Date()),
        ShoppingListItem(5L, "Book", "piece", 1f, 1f, Date()),
    )
    GroupedShoppingListCard(
        GroupedShoppingList(group = "2021-09-29", shoppingList = shoppingList),
        mapOf(Pair("2021-09-29", shoppingList.size)),
    )
}