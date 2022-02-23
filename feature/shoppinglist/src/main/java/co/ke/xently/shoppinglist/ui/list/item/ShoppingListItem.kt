package co.ke.xently.shoppinglist.ui.list.item

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.feature.ui.ListItemSurface

internal data class MenuItem(
    @StringRes
    val label: Int,
    val onClick: (Long) -> Unit = {},
)

@Composable
internal fun ShoppingListItemCard(
    item: ShoppingListItem,
    modifier: Modifier = Modifier,
    menuItems: List<MenuItem> = emptyList(),
    onClick: (ShoppingListItem) -> Unit,
) {
    var showDropMenu by remember { mutableStateOf(false) }
    ListItemSurface(modifier = modifier, onClick = { onClick.invoke(item) }) {
        Column {
            Text(
                modifier = Modifier.wrapContentWidth(),
                text = item.name,
                style = MaterialTheme.typography.body1
            )
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
                        if (showDropMenu) {
                            Icons.Default.KeyboardArrowDown
                        } else {
                            Icons.Default.KeyboardArrowRight
                        },
                        contentDescription = "${item.name} shopping list item options"
                    )
                }
                DropdownMenu(expanded = showDropMenu, onDismissRequest = { showDropMenu = false }) {
                    for (menuItem in menuItems) {
                        DropdownMenuItem(
                            onClick = {
                                menuItem.onClick.invoke(item.id)
                                showDropMenu = false
                            },
                        ) { Text(stringResource(menuItem.label)) }
                    }
                }
            }
        }
    }
}