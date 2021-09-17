package co.ke.xently.shoppinglist.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.GroupedShoppingListCount
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.shoppinglist.R
import java.util.*


@Composable
fun ShoppingList(
    modifier: Modifier = Modifier,
    viewModel: ShoppingListViewModel,
    loadRemote: Boolean = false,
) {
    viewModel.shouldLoadRemote(loadRemote)
    val groupedShoppingListResult = viewModel.groupedShoppingListResult.collectAsState().value
    // TODO: Return as Map for faster lookup
    val groupedShoppingListCount = viewModel.groupedShoppingListCount.collectAsState().value

    val modifier1 = modifier
        .fillMaxHeight()
        .fillMaxWidth()

    if (groupedShoppingListResult.isSuccess) {
        val groupedShoppingList = groupedShoppingListResult.getOrThrow()
        if (groupedShoppingList == null) {
            Box(contentAlignment = Alignment.Center, modifier = modifier1) {
                CircularProgressIndicator()
            }
        } else {
            if (groupedShoppingList.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = modifier1) {
                    Text(text = stringResource(R.string.fsl_empty_shopping_list))
                }
            } else {
                LazyColumn(modifier = modifier1) {
                    items(groupedShoppingList) { groupList: GroupedShoppingList ->
                        GroupedShoppingListCard(groupList, groupedShoppingListCount)
                    }
                }
            }
        }
    } else {
        Box(contentAlignment = Alignment.Center, modifier = modifier1) {
            Text(text = groupedShoppingListResult.exceptionOrNull()?.localizedMessage
                ?: stringResource(R.string.fsl_generic_error_message))
        }
    }
}

@Composable
private fun GroupedShoppingListCard(
    groupList: GroupedShoppingList,
    listCount: List<GroupedShoppingListCount>,
    onRecommendGroupClicked: ((group: Any) -> Unit) = {},
    onDuplicateGroupClicked: ((group: Any) -> Unit) = {},
    onDeleteGroupClicked: ((group: Any) -> Unit) = {},
    onSeeAllClicked: ((group: Any) -> Unit) = {},
) {
    val numberOfItems = listCount.firstOrNull {
        it.group == groupList.group
    }?.numberOfItems ?: groupList.numberOfItems
    val itemsPerCard = 3
    var showDropDownMenu by remember { mutableStateOf(false) }

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
                    Text(text = LocalContext.current.resources.getQuantityString(R.plurals.fsl_group_items_count,
                        numberOfItems, numberOfItems), style = MaterialTheme.typography.subtitle2)
                }
                Box(modifier = Modifier.align(Alignment.Top)) {
                    IconButton(onClick = { showDropDownMenu = true }) {
                        Icon(Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.fsl_group_card_menu_content_desc_more))
                    }
                    DropdownMenu(
                        expanded = showDropDownMenu,
                        onDismissRequest = { showDropDownMenu = false },
                    ) {
                        DropdownMenuItem(onClick = { onRecommendGroupClicked(groupList.group) }) {
                            Text(text = stringResource(R.string.fsl_group_menu_recommend))
                        }
                        DropdownMenuItem(onClick = { onDuplicateGroupClicked(groupList.group) }) {
                            Text(text = stringResource(R.string.fsl_group_menu_duplicate))
                        }
                        DropdownMenuItem(onClick = { onDeleteGroupClicked(groupList.group) }) {
                            Text(text = stringResource(R.string.fsl_group_menu_delete))
                        }
                    }
                }
            }
            Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(end = 16.dp))
            Column {
                for (item in groupList.shoppingList.take(itemsPerCard)) {
                    ShoppingListCardItem(item, modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth())
                }
            }
            if (numberOfItems > itemsPerCard) {
                OutlinedButton(onClick = { onSeeAllClicked(groupList.group) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp, bottom = 8.dp)) {
                    Text(text = stringResource(R.string.fsl_group_button_see_all),
                        style = MaterialTheme.typography.button)
                }
            }
        }
    }
}

@Composable
private fun ShoppingListCardItem(
    item: ShoppingListItem, modifier: Modifier = Modifier,
    onDeleteClicked: ((id: Long) -> Unit) = {},
) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier) {
        Column {
            Text(text = item.name, style = MaterialTheme.typography.body1)
            Text(text = "${item.unitQuantity} ${item.unit}",
                style = MaterialTheme.typography.caption)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${item.purchaseQuantity}", style = MaterialTheme.typography.h6)
            // TODO: Show delete button on click with ability to navigate to detail page on click
            IconButton(onClick = { onDeleteClicked(item.id) }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
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
        listOf(GroupedShoppingListCount("2021-09-29", shoppingList.size)),
    )
}