package co.ke.xently.shops.ui.list

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.Shop

@Composable
fun ShopList(modifier: Modifier = Modifier, viewModel: ShopListViewModel = hiltViewModel()) {
    Scaffold {
        LazyColumn(modifier = modifier) {
            items(arrayListOf<Shop>()) {

            }
        }
    }
}