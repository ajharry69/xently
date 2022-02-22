package co.ke.xently.shops.ui.list.item

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ke.xently.data.Shop
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.shops.R
import kotlin.random.Random


@Composable
internal fun ShopListItem(
    shop: Shop,
    modifier: Modifier = Modifier,
    showPopupMenu: Boolean = false,
    onUpdateRequested: ((id: Long) -> Unit) = {},
    onProductsClicked: ((id: Long) -> Unit) = {},
    onAddressesClicked: ((id: Long) -> Unit) = {},
) {
    val resources = LocalContext.current.resources
    var showDropMenu by remember { mutableStateOf(showPopupMenu) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.padding(start = 16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxWidth(),
                text = shop.name,
                style = MaterialTheme.typography.body1
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = shop.taxPin,
                style = MaterialTheme.typography.caption,
            )
        }
        Box(modifier = Modifier.width(IntrinsicSize.Min)) {
            IconButton(onClick = { showDropMenu = true }) {
                Icon(
                    if (showDropMenu) {
                        Icons.Default.KeyboardArrowDown
                    } else {
                        Icons.Default.KeyboardArrowRight
                    },
                    contentDescription = stringResource(
                        R.string.fs_shop_item_menu_content_description,
                        shop.name,
                    ),
                )
            }
            DropdownMenu(expanded = showDropMenu, onDismissRequest = { showDropMenu = false }) {
                DropdownMenuItem(
                    onClick = {
                        onUpdateRequested(shop.id)
                        showDropMenu = false
                    },
                ) { Text(stringResource(R.string.update)) }
                if (shop.productsCount > 0) {
                    DropdownMenuItem(
                        onClick = {
                            onProductsClicked(shop.id)
                            showDropMenu = false
                        },
                    ) {
                        Text(resources.getQuantityString(
                            R.plurals.fs_shop_item_menu_products,
                            shop.productsCount,
                            shop.productsCount
                        ))
                    }
                }
                if (shop.addressesCount > 0) {
                    DropdownMenuItem(
                        onClick = {
                            onAddressesClicked(shop.id)
                            showDropMenu = false
                        },
                    ) {
                        Text(resources.getQuantityString(
                            R.plurals.fs_shop_item_menu_addresses,
                            shop.addressesCount,
                            shop.addressesCount
                        ))
                    }
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