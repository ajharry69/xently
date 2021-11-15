package co.ke.xently.shops.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.Shop
import co.ke.xently.feature.MAP_HEIGHT
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.GoogleMapView
import co.ke.xently.shops.R
import kotlinx.coroutines.launch

@Composable
internal fun ShopDetailScreen(
    modifier: Modifier = Modifier,
    shopId: Long? = null,
    viewModel: ShopDetailViewModel = hiltViewModel(),
    onNavigationIconClicked: (() -> Unit) = {},
) {
    shopId?.also {
        if (it != Shop.DEFAULT_ID) viewModel.getShop(it)
    }
    val shopResult by viewModel.shopResult.collectAsState()
    ShopDetailScreen(
        modifier,
        shopResult,
        onNavigationIconClicked,
        viewModel::setLocationPermissionGranted,
    ) {
        viewModel.addShop(it)
    }
}

@Composable
private fun ShopDetailScreen(
    modifier: Modifier,
    result: Result<Shop?>,
    onNavigationIconClicked: (() -> Unit) = {},
    onLocationPermissionChanged: ((Boolean) -> Unit) = {},
    onAddShopClicked: ((Shop) -> Unit) = {},
) {
    val shop = result.getOrNull() ?: Shop()
    var name by remember(shop.id, shop.name) {
        mutableStateOf(TextFieldValue(shop.name))
    }
    var taxPin by remember(shop.id, shop.taxPin) {
        mutableStateOf(TextFieldValue(shop.taxPin))
    }
    val toolbarTitlePrefix = stringResource(
        if (shop.isDefaultID) R.string.fs_add else R.string.fs_update
    )
    val (coroutineScope, scaffoldState) = Pair(rememberCoroutineScope(), rememberScaffoldState())

    if (result.isFailure) {
        val errorMessage =
            result.exceptionOrNull()?.localizedMessage ?: stringResource(
                id = R.string.fs_generic_error_message
            )
        LaunchedEffect(shop.id, result, errorMessage) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
    }

    Scaffold(scaffoldState = scaffoldState) {
        Column(modifier = modifier) {
            Box(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .fillMaxWidth()
            ) {
                GoogleMapView(
                    Modifier
                        .height(MAP_HEIGHT)
                        .fillMaxWidth(),
                    onLocationPermissionChanged = onLocationPermissionChanged,
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    TopAppBar(
                        backgroundColor = Color.Transparent,
                        elevation = 0.dp,
                        navigationIcon = {
                            IconButton(onClick = onNavigationIconClicked) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.fs_navigation_icon_content_description),
                                )
                            }
                        },
                        title = {
                            Text(
                                stringResource(
                                    R.string.fs_add_shop_toolbar_title,
                                    toolbarTitlePrefix
                                )
                            )
                        },
                    )
                    if (result.isSuccess && result.getOrThrow() == null && !shop.isDefaultID) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                TextField(
                    value = name,
                    singleLine = true,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    label = { Text(text = stringResource(R.string.fs_shop_item_detail_name_label)) },
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                TextField(
                    value = taxPin,
                    singleLine = true,
                    onValueChange = { taxPin = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    label = { Text(text = stringResource(R.string.fs_shop_item_detail_tax_pin_label)) },
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
                    Text(
                        stringResource(
                            R.string.fs_shop_item_detail_button_label,
                            toolbarTitlePrefix
                        ).uppercase()
                    )
                }
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
            result = Result.success(Shop(name = "Shop #1000", taxPin = "P000111222B")),
        )
    }
}

@Preview(name = "Shop detail showing progress bar", showBackground = true)
@Composable
fun ShopDetailOnNullPreview() {
    XentlyTheme {
        ShopDetailScreen(modifier = Modifier.fillMaxSize(), result = Result.success(null))
    }
}