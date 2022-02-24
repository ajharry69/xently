package co.ke.xently.shoppinglist.ui.list.item

import androidx.annotation.StringRes
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
import co.ke.xently.feature.ui.ListItemSurface
import co.ke.xently.feature.ui.NEGLIGIBLE_SPACE_BY
import co.ke.xently.feature.ui.shimmerPlaceholder
import co.ke.xently.shoppinglist.R

internal data class MenuItem(
    @StringRes
    val label: Int,
    val onClick: (Long) -> Unit = {},
)

@Composable
internal fun ShoppingListItemCard(
    item: ShoppingListItem,
    modifier: Modifier = Modifier,
    showPlaceholder: Boolean = false,
    menuItems: List<MenuItem> = emptyList(),
    onClick: (ShoppingListItem) -> Unit,
) {
    val placeholderVisible = showPlaceholder || item.isDefault
    var showDropMenu by remember { mutableStateOf(false) }
    ListItemSurface(modifier = modifier, onClick = { onClick.invoke(item) }) {
        Column(verticalArrangement = Arrangement.spacedBy(NEGLIGIBLE_SPACE_BY)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.body1,
                modifier = Modifier
                    .wrapContentWidth()
                    .shimmerPlaceholder(placeholderVisible),
            )
            Text(
                text = "${item.unitQuantity} ${item.unit}",
                style = MaterialTheme.typography.caption,
                modifier = Modifier.shimmerPlaceholder(placeholderVisible),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${item.purchaseQuantity}",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.shimmerPlaceholder(placeholderVisible),
            )
            Box {
                IconButton(onClick = { showDropMenu = true }) {
                    Icon(
                        imageVector = if (showDropMenu) {
                            Icons.Default.KeyboardArrowDown
                        } else {
                            Icons.Default.KeyboardArrowRight
                        },
                        contentDescription = stringResource(
                            R.string.fsl_detail_more_actions_content_description,
                            item.name,
                        ),
                        modifier = Modifier.shimmerPlaceholder(placeholderVisible),
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