package co.ke.xently.shops.ui.list.item

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import co.ke.xently.data.Shop
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.ListItemSurface
import co.ke.xently.feature.ui.NEGLIGIBLE_SPACE
import co.ke.xently.feature.ui.shimmerPlaceholder
import co.ke.xently.shops.R
import kotlin.random.Random

internal data class Click(val base: (Shop) -> Unit = {})

internal data class MenuItem(val label: String, val onClick: (Shop) -> Unit = {})

@Composable
internal fun ShopListItem(
    shop: Shop,
    modifier: Modifier = Modifier,
    showPopupMenu: Boolean = false,
    click: Click = Click(),
    menuItems: @Composable (Shop) -> List<MenuItem> = { emptyList() },
) {
    var showDropMenu by remember { mutableStateOf(showPopupMenu) }
    ListItemSurface(modifier = modifier, onClick = { click.base.invoke(shop) }) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(NEGLIGIBLE_SPACE),
        ) {
            Text(
                modifier = Modifier
                    .wrapContentWidth()
                    .shimmerPlaceholder(shop.isDefault),
                text = shop.name,
                style = MaterialTheme.typography.body1,
            )
            Text(
                maxLines = 1,
                text = shop.taxPin,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.shimmerPlaceholder(shop.isDefault),
            )
        }
        Box(modifier = Modifier.width(IntrinsicSize.Min)) {
            IconButton(onClick = { showDropMenu = true }) {
                Icon(
                    imageVector = if (showDropMenu) {
                        Icons.Default.KeyboardArrowDown
                    } else {
                        Icons.Default.KeyboardArrowRight
                    },
                    contentDescription = stringResource(
                        R.string.fs_shop_item_menu_content_description,
                        shop.name,
                    ),
                    modifier = Modifier.shimmerPlaceholder(shop.isDefault),
                )
            }
            DropdownMenu(expanded = showDropMenu, onDismissRequest = { showDropMenu = false }) {
                for (item in menuItems.invoke(shop)) {
                    DropdownMenuItem(
                        onClick = {
                            item.onClick.invoke(shop)
                            showDropMenu = false
                        },
                    ) { Text(item.label) }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShopListItem() {
    XentlyTheme {
        Column {
            ShopListItem(
                modifier = Modifier.fillMaxWidth(),
                shop = Shop(
                    name = "Shop #1000",
                    taxPin = "P000111222B",
                    productsCount = Random.nextInt(0, 500),
                    addressesCount = Random.nextInt(0, 50),
                ),
            )

            ShopListItem(
                modifier = Modifier.fillMaxWidth(),
                showPopupMenu = true,
                shop = Shop(
                    name = "Shop #1001",
                    taxPin = "P000111222C",
                    productsCount = Random.nextInt(0, 500),
                    addressesCount = Random.nextInt(0, 50),
                ),
            )
        }
    }
}