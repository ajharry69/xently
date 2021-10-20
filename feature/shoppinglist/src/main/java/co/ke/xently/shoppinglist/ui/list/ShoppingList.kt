package co.ke.xently.shoppinglist.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.shoppinglist.R


@Composable
internal fun ShoppingListScreen(
    modifier: Modifier = Modifier,
    loadRemote: Boolean = false,
    viewModel: ShoppingListViewModel = hiltViewModel(),
    onShoppingListItemClicked: (itemId: Long) -> Unit,
    onRecommendClicked: (itemId: Long) -> Unit,
    onRecommendOptionsMenuClicked: (List<ShoppingListItem>?) -> Unit,
    onNavigationIconClicked: (() -> Unit) = {},
) {
    val scaffoldState = rememberScaffoldState()
    var showOptionsMenu by remember { mutableStateOf(false) }

    viewModel.shouldLoadRemote(loadRemote)
    val shoppingListResult = viewModel.shoppingListResult.collectAsState().value

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.fsl_toolbar_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigationIconClicked) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.fsl_menu_navigation_icon_content_desc_back),
                        )
                    }
                },
                actions = {
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
                            onRecommendOptionsMenuClicked(shoppingListResult.getOrNull())
                        }) {
                            Text(text = stringResource(R.string.fsl_group_menu_recommend))
                        }
                    }
                }
            )
        },
    ) {
        if (shoppingListResult.isSuccess) {
            val shoppingList = shoppingListResult.getOrThrow()
            when {
                shoppingList == null -> {
                    Box(contentAlignment = Alignment.Center, modifier = modifier) {
                        CircularProgressIndicator()
                    }
                }
                shoppingList.isEmpty() -> {
                    Box(contentAlignment = Alignment.Center, modifier = modifier) {
                        Text(text = stringResource(R.string.fsl_empty_shopping_list))
                    }
                }
                else -> {
                    LazyColumn(modifier = modifier) {
                        items(shoppingList) { item ->
                            ShoppingListItemCard(
                                item,
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth(),
                                onItemClicked = onShoppingListItemClicked,
                                onRecommendClicked = onRecommendClicked,
                            )
                        }
                    }
                }
            }
        } else {
            Box(contentAlignment = Alignment.Center, modifier = modifier) {
                Text(
                    text = shoppingListResult.exceptionOrNull()?.localizedMessage
                        ?: stringResource(R.string.fsl_generic_error_message)
                )
            }
        }
    }
}

@Composable
internal fun ShoppingListItemCard(
    item: ShoppingListItem, modifier: Modifier = Modifier,
    onItemClicked: ((id: Long) -> Unit) = {},
    onRecommendClicked: ((id: Long) -> Unit) = {},
    onDeleteClicked: ((id: Long) -> Unit) = {},
) {
    var showDropMenu by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.clickable { onItemClicked(item.id) }) {
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
