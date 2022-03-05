package co.ke.xently.shops.ui.list.addresses.item

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import co.ke.xently.data.Address
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.ListItemSurface
import co.ke.xently.feature.ui.NEGLIGIBLE_SPACE
import co.ke.xently.feature.ui.shimmerPlaceholder

internal data class AddressListItemFunction(val onItemClick: (Address) -> Unit = {})

@Composable
internal fun AddressListItem(
    address: Address,
    modifier: Modifier = Modifier,
    function: AddressListItemFunction = AddressListItemFunction(),
) {
    ListItemSurface(modifier = modifier, onClick = { function.onItemClick.invoke(address) }) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(NEGLIGIBLE_SPACE),
        ) {
            Text(
                text = address.town,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.shimmerPlaceholder(address.isDefault),
            )
            Text(
                text = address.town,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.shimmerPlaceholder(address.isDefault),
            )
        }
        IconButton(modifier = Modifier.width(IntrinsicSize.Min), onClick = { /*TODO*/ }) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.shimmerPlaceholder(address.isDefault),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddressListItem() {
    XentlyTheme {
        AddressListItem(Address(town = "Westlands, Nairobi"), Modifier.fillMaxSize())
    }
}