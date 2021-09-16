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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.GroupedShoppingListCount
import co.ke.xently.data.ShoppingListItem
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
                    Text(text = "You have no shopping list, yet!")
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
                ?: "An error occurred")
        }
    }
}

@Composable
private fun GroupedShoppingListCard(
    groupList: GroupedShoppingList,
    listCount: List<GroupedShoppingListCount>,
) {
    val numberOfItems = listCount.firstOrNull {
        it.group == groupList.group
    }?.numberOfItems ?: groupList.numberOfItems
    val itemsPerCard = 3
    var showDropDownMenu by remember { mutableStateOf(false) }

    Card(modifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 16.dp)) {
        Column(modifier = Modifier
            .padding(vertical = 8.dp)
            .padding(start = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    // TODO: Change text (style) to h3
                    Text(text = groupList.group, style = MaterialTheme.typography.h6)
                    // TODO: Change text (style) to subtitle
                    Text(text = "$numberOfItems items", style = MaterialTheme.typography.subtitle2)
                }
                Box(modifier = Modifier.align(Alignment.Top)) {
                    IconButton(onClick = { showDropDownMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Show card menu")
                    }
                    DropdownMenu(
                        expanded = showDropDownMenu,
                        onDismissRequest = { showDropDownMenu = false },
                    ) {
                        DropdownMenuItem(onClick = { /*TODO*/ }) {
                            Text(text = "Recommend")
                        }
                        DropdownMenuItem(onClick = { /*TODO*/ }) {
                            Text(text = "Duplicate")
                        }
                        DropdownMenuItem(onClick = { /*TODO*/ }) {
                            Text(text = "Delete")
                        }
                    }
                }
            }
            Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(end = 16.dp))
            Column {
                for (item in groupList.shoppingList.take(itemsPerCard)) {
                    ShoppingListCardItem(item, modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth())
                }
            }
            if (numberOfItems > itemsPerCard) {
                OutlinedButton(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth().padding(end = 16.dp, bottom = 8.dp)) {
                    Text(text = "SEE ALL", style = MaterialTheme.typography.button)
                }
            }
        }
    }
}

@Composable
private fun ShoppingListCardItem(item: ShoppingListItem, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier) {
        Column {
            Text(text = item.name, style = MaterialTheme.typography.body1)
            Text(text = "${item.unitQuantity} ${item.unit}",
                style = MaterialTheme.typography.caption)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${item.purchaseQuantity}", style = MaterialTheme.typography.h6)
            IconButton(onClick = { /*TODO*/ }) {
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

@Composable
fun ShoppingListDetail(
    modifier: Modifier = Modifier,
//    viewModel: ShoppingListViewModel = viewModel(),
) {
    Text("Shopping detail...", modifier = modifier)
}