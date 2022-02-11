package co.ke.xently.shoppinglist.ui.list.grouped.item

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.shoppinglist.R
import co.ke.xently.shoppinglist.ui.list.item.ShoppingListItemCard
import java.util.*


@Composable
internal fun GroupedShoppingListCard(
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
                        ) { Text(stringResource(R.string.delete)) }
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
                        onUpdateRequested = onShoppingListItemClicked,
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
                        stringResource(R.string.fsl_group_button_see_all),
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