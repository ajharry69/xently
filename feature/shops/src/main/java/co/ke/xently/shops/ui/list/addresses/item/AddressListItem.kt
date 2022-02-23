package co.ke.xently.shops.ui.list.addresses.item

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
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

internal data class Click(val base: (Address) -> Unit = {})

@Composable
internal fun AddressListItem(
    address: Address,
    modifier: Modifier = Modifier,
    click: Click = Click(),
) {
    ListItemSurface(modifier = modifier, onClick = { click.base.invoke(address) }) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = address.town, style = MaterialTheme.typography.body1)
            Text(text = address.town, style = MaterialTheme.typography.caption)
        }
        IconButton(modifier = Modifier.width(IntrinsicSize.Min), onClick = { /*TODO*/ }) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
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