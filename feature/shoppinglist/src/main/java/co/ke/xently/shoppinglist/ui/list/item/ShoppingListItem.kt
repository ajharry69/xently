package co.ke.xently.shoppinglist.ui.list.item

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.shoppinglist.R


@Composable
internal fun ShoppingListItemCard(
    item: ShoppingListItem,
    modifier: Modifier = Modifier,
    onUpdateRequested: ((id: Long) -> Unit) = {},
    onRecommendClicked: ((id: Long) -> Unit) = {},
    onDeleteClicked: ((id: Long) -> Unit) = {},
) {
    var showDropMenu by remember { mutableStateOf(false) }
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
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
                    DropdownMenuItem(
                        onClick = {
                            onUpdateRequested(item.id)
                            showDropMenu = false
                        },
                    ) { Text(stringResource(R.string.fsl_group_menu_update)) }
                    DropdownMenuItem(
                        onClick = {
                            onRecommendClicked(item.id)
                            showDropMenu = false
                        },
                    ) { Text(stringResource(R.string.fsl_group_menu_recommend)) }
                    DropdownMenuItem(
                        onClick = {
                            onDeleteClicked(item.id)
                            showDropMenu = false
                        },
                    ) { Text(stringResource(R.string.fsl_group_menu_delete)) }
                }
            }
        }
    }
}