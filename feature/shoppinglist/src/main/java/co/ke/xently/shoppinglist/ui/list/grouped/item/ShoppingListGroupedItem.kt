package co.ke.xently.shoppinglist.ui.list.grouped.item

import androidx.annotation.StringRes
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
import co.ke.xently.feature.ui.HORIZONTAL_PADDING
import co.ke.xently.shoppinglist.R
import co.ke.xently.shoppinglist.ui.list.item.MenuItem
import co.ke.xently.shoppinglist.ui.list.item.ShoppingListItemCard
import java.util.*

internal data class GroupMenuItem(
    @StringRes
    val label: Int,
    val onClick: (group: Any) -> Unit,
)

@Composable
internal fun GroupedShoppingListCard(
    groupList: GroupedShoppingList,
    listCount: Map<Any, Int>,
    onSeeAllClicked: (group: Any) -> Unit = {},
    menuItems: List<MenuItem> = emptyList(),
    groupMenuItems: List<GroupMenuItem> = emptyList(),
    onItemClick: (ShoppingListItem) -> Unit = {},
) {
    val itemsPerCard = 3
    var showDropDownMenu by remember { mutableStateOf(false) }
    val numberOfItems = listCount.getOrElse(groupList.group) { groupList.numberOfItems }

    Card(
        modifier = Modifier
            .padding(horizontal = HORIZONTAL_PADDING)
            .padding(top = HORIZONTAL_PADDING),
    ) {
        Column(modifier = Modifier.padding(vertical = HORIZONTAL_PADDING / 2)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = HORIZONTAL_PADDING),
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
                        for (item in groupMenuItems) {
                            DropdownMenuItem(
                                onClick = {
                                    item.onClick.invoke(groupList.group)
                                    showDropDownMenu = false
                                },
                            ) { Text(text = stringResource(item.label)) }
                        }
                    }
                }
            }
            Divider(
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(end = HORIZONTAL_PADDING, start = HORIZONTAL_PADDING)
            )
            Column {
                for (item in groupList.shoppingList.take(itemsPerCard)) {
                    ShoppingListItemCard(
                        modifier = Modifier.fillMaxWidth(),
                        item = item,
                        menuItems = menuItems,
                        onClick = onItemClick,
                    )
                }
            }
            if (numberOfItems > itemsPerCard) {
                OutlinedButton(
                    onClick = { onSeeAllClicked(groupList.group) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            end = HORIZONTAL_PADDING,
                            start = HORIZONTAL_PADDING,
                            bottom = HORIZONTAL_PADDING / 2,
                        )
                ) {
                    Text(
                        stringResource(R.string.fsl_group_button_see_all),
                        style = MaterialTheme.typography.button,
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