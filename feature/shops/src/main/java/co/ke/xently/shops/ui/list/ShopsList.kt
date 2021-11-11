package co.ke.xently.shops.ui.list

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.ke.xently.data.Shop

@Composable
fun ShopsList(modifier: Modifier = Modifier) {
    Scaffold {
        LazyColumn {
            items(arrayListOf<Shop>()) {

            }
        }
    }
}