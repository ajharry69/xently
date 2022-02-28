package co.ke.xently.products.ui.list.item

import android.text.format.DateFormat
import androidx.annotation.StringRes
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import co.ke.xently.data.Product
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.ListItemSurface
import co.ke.xently.feature.ui.NEGLIGIBLE_SPACE
import co.ke.xently.feature.ui.shimmerPlaceholder
import co.ke.xently.feature.utils.descriptive
import co.ke.xently.products.R

internal data class MenuItem(
    @StringRes val label: Int,
    val onClick: (Product) -> Unit = {},
)

@OptIn(ExperimentalUnitApi::class)
@Composable
internal fun ProductListItem(
    product: Product,
    modifier: Modifier = Modifier,
    showPopupMenu: Boolean = false,
    menuItems: List<MenuItem> = emptyList(),
) {
    var showDropMenu by remember(showPopupMenu) { mutableStateOf(showPopupMenu) }
    ListItemSurface(modifier = modifier) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(NEGLIGIBLE_SPACE),
        ) {
            Text(
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxWidth()
                    .shimmerPlaceholder(product.isDefault),
                text = product.toString(),
                style = MaterialTheme.typography.body1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier.shimmerPlaceholder(product.isDefault),
                text = DateFormat.getMediumDateFormat(LocalContext.current)
                    .format(product.datePurchased),
                style = MaterialTheme.typography.caption,
            )
        }
        Row(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${stringResource(co.ke.xently.feature.R.string.default_currency)}${product.unitPrice.descriptive()}",
                style = MaterialTheme.typography.subtitle2.copy(
                    fontSize = TextUnit(18f, TextUnitType.Sp),
                ),
                modifier = Modifier.shimmerPlaceholder(product.isDefault),
            )
            Box {
                IconButton(onClick = { showDropMenu = !showDropMenu }) {
                    Icon(
                        imageVector = if (showDropMenu) {
                            Icons.Default.KeyboardArrowDown
                        } else {
                            Icons.Default.KeyboardArrowRight
                        },
                        contentDescription = stringResource(
                            R.string.fp_product_item_menu_content_description,
                            product.name,
                        ),
                        modifier = Modifier.shimmerPlaceholder(product.isDefault),
                    )
                }
                DropdownMenu(
                    expanded = showDropMenu,
                    onDismissRequest = { showDropMenu = false },
                ) {
                    for (item in menuItems) {
                        DropdownMenuItem(
                            onClick = {
                                item.onClick.invoke(product)
                                showDropMenu = false
                            },
                        ) { Text(stringResource(item.label)) }
                    }
                }
            }
        }
    }
}

@Preview("Product item", showBackground = true)
@Composable
private fun ProductListItemPreview() {
    XentlyTheme {
        ProductListItem(
            product = Product(
                name = "Bread",
                unit = "grams",
                unitQuantity = 400f,
                unitPrice = 1_000.53f,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}