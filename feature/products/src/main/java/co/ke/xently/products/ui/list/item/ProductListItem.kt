package co.ke.xently.products.ui.list.item

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import co.ke.xently.feature.R
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.utils.descriptive


@OptIn(ExperimentalUnitApi::class)
@Composable
internal fun ProductListItem(
    product: Product,
    modifier: Modifier = Modifier,
    showPopupMenu: Boolean = false,
    onItemClicked: (id: Long) -> Unit = {},
) {
    var showDropMenu by remember { mutableStateOf(showPopupMenu) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .padding(start = 8.dp)
            .clickable { onItemClicked(product.id) },
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxWidth(),
                text = "${product.name}, ${product.unitQuantity} ${product.unit}",
                style = MaterialTheme.typography.body1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                DateFormat.getMediumDateFormat(LocalContext.current).format(product.datePurchased),
                style = MaterialTheme.typography.caption,
            )
        }
        Row(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${stringResource(R.string.default_currency)}${product.unitPrice.descriptive()}",
                style = MaterialTheme.typography.subtitle2.copy(fontSize = TextUnit(18f,
                    TextUnitType.Sp)))
            Box {
                IconButton(onClick = { showDropMenu = true }) {
                    Icon(
                        if (showDropMenu) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = stringResource(
                            co.ke.xently.products.R.string.fp_product_item_menu_content_description,
                            product.name,
                        ),
                    )
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