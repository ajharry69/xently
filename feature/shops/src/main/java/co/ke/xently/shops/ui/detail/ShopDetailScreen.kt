package co.ke.xently.shops.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.data.errorMessage
import co.ke.xently.data.getOrNull
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.GoogleMapView
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.feature.ui.XentlyTextField
import co.ke.xently.feature.ui.stringRes
import co.ke.xently.feature.utils.MAP_HEIGHT
import co.ke.xently.shops.R

@Composable
internal fun ShopDetailScreen(
    id: Long?,
    modifier: Modifier = Modifier,
    viewModel: ShopDetailViewModel = hiltViewModel(),
    onNavigationIconClicked: () -> Unit = {},
) {
    id?.also {
        if (it != Shop.default().id) viewModel.get(it)
    }
    val shopResult by viewModel.shopResult.collectAsState()
    ShopDetailScreen(
        modifier,
        shopResult,
        onNavigationIconClicked,
        viewModel::setLocationPermissionGranted,
        viewModel::add,
    )
}

@Composable
private fun ShopDetailScreen(
    modifier: Modifier,
    result: TaskResult<Shop?>,
    onNavigationIconClicked: () -> Unit = {},
    onLocationPermissionChanged: (Boolean) -> Unit = {},
    onAddShopClicked: (Shop) -> Unit = {},
) {
    val shop = result.getOrNull() ?: Shop.default()
    var name by remember(shop.id, shop.name) {
        mutableStateOf(TextFieldValue(shop.name))
    }
    var taxPin by remember(shop.id, shop.taxPin) {
        mutableStateOf(TextFieldValue(shop.taxPin))
    }
    val toolbarTitle = stringRes(
        R.string.fs_add_shop_toolbar_title,
        if (shop.isDefault) R.string.add else R.string.update,
    )
    val scaffoldState = rememberScaffoldState()

    if (result is TaskResult.Error) {
        val errorMessage = result.errorMessage ?: stringResource(R.string.generic_error_message)
        LaunchedEffect(shop.id, result, errorMessage) {
            scaffoldState.snackbarHostState.showSnackbar(errorMessage)
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
            ) {
                GoogleMapView(
                    Modifier
                        .height(MAP_HEIGHT)
                        .fillMaxWidth(),
                    onLocationPermissionChanged = onLocationPermissionChanged,
                )
                ToolbarWithProgressbar(
                    toolbarTitle,
                    onNavigationIconClicked,
                    result is TaskResult.Loading,
                    elevation = 0.dp,
                    backgroundColor = Color.Transparent,
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            XentlyTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                label = stringResource(R.string.fs_shop_item_detail_name_label),
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            XentlyTextField(
                value = taxPin,
                onValueChange = { taxPin = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                label = stringResource(R.string.fs_shop_item_detail_tax_pin_label),
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Button(
                enabled = arrayOf(name, taxPin).all { it.text.isNotBlank() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = {
                    onAddShopClicked(shop.copy(name = name.text, taxPin = taxPin.text))
                }
            ) {
                Text(toolbarTitle.uppercase())
            }
        }
    }
}

@Preview(name = "Shop detail", showBackground = true)
@Composable
fun ShopDetailPreview() {
    XentlyTheme {
        ShopDetailScreen(
            modifier = Modifier.fillMaxSize(),
            result = Success(Shop(name = "Shop #1000", taxPin = "P000111222B")),
        )
    }
}

@Preview(name = "Shop detail showing progress bar", showBackground = true)
@Composable
fun ShopDetailOnNullPreview() {
    XentlyTheme {
        ShopDetailScreen(modifier = Modifier.fillMaxSize(), result = Success(null))
    }
}