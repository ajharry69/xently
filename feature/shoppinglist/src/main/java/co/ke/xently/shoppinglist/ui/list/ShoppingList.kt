package co.ke.xently.shoppinglist.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.shoppinglist.R
import co.ke.xently.shoppinglist.ui.list.grouped.GroupedShoppingListCard
import co.ke.xently.shoppinglist.ui.list.recommendation.ShoppingListRecommendationScreen
import kotlinx.coroutines.launch


@Composable
fun ShoppingListScreen(
    modifier: Modifier = Modifier,
    viewModel: ShoppingListViewModel,
    loadRemote: Boolean = false,
    onShoppingListItemClicked: (itemId: Long) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    viewModel.shouldLoadRemote(loadRemote)
    val groupedShoppingListResult = viewModel.groupedShoppingListResult.collectAsState().value
    val groupedShoppingListCount = viewModel.groupedShoppingListCount.collectAsState().value
    var groupToRecommend by remember { mutableStateOf<Any?>(null) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Xently") },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Menu, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        },
        sheetContent = {
            if (groupToRecommend != null) {
                ShoppingListRecommendationScreen(
                    group = groupToRecommend!!,
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                )
            }
        },
        sheetPeekHeight = 0.dp,
    ) {
        if (groupedShoppingListResult.isSuccess) {
            val groupedShoppingList = groupedShoppingListResult.getOrThrow()
            when {
                groupedShoppingList == null -> {
                    Box(contentAlignment = Alignment.Center, modifier = modifier) {
                        CircularProgressIndicator()
                    }
                }
                groupedShoppingList.isEmpty() -> {
                    Box(contentAlignment = Alignment.Center, modifier = modifier) {
                        Text(text = stringResource(R.string.fsl_empty_shopping_list))
                    }
                }
                else -> {
                    LazyColumn(modifier = modifier) {
                        items(groupedShoppingList) { groupList ->
                            GroupedShoppingListCard(
                                groupList, groupedShoppingListCount,
                                onShoppingListItemClicked = onShoppingListItemClicked,
                                onRecommendGroupClicked = { group ->
                                    coroutineScope.launch {
                                        scaffoldState.bottomSheetState.expand()
                                    }
                                    groupToRecommend = group
                                },
                            )
                        }
                    }
                }
            }
        } else {
            Box(contentAlignment = Alignment.Center, modifier = modifier) {
                Text(
                    text = groupedShoppingListResult.exceptionOrNull()?.localizedMessage
                        ?: stringResource(R.string.fsl_generic_error_message)
                )
            }
        }
    }
}

@Composable
internal fun ShoppingListCardItem(
    item: ShoppingListItem, modifier: Modifier = Modifier,
    onRecommendClicked: ((id: Long) -> Unit) = {},
    onDeleteClicked: ((id: Long) -> Unit) = {},
) {
    var showDropMenu by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier) {
        Column {
            Text(text = item.name, style = MaterialTheme.typography.body1)
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
