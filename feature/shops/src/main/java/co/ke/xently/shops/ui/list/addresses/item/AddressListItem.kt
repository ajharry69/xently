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
import androidx.compose.ui.unit.dp
import co.ke.xently.data.Address
import co.ke.xently.feature.theme.XentlyTheme

@Composable
internal fun AddressListItem(address: Address, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(start = 16.dp)
            .padding(vertical = 8.dp),
    ) {
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