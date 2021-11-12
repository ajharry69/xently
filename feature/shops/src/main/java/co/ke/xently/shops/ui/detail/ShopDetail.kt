package co.ke.xently.shops.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Scaffold
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.Shop

@Composable
fun ShopDetail(modifier: Modifier = Modifier, viewModel: ShopDetailViewModel = hiltViewModel()) {
    val shopResult by viewModel.shopResult.collectAsState()
    ShopDetail(modifier, shopResult)
}

@Composable
private fun ShopDetail(modifier: Modifier, result: Result<Shop?>) {
    Scaffold {
        Column(modifier = modifier, verticalArrangement = Arrangement.SpaceBetween) {
            TextField(value = "", onValueChange = {})
        }
    }
}