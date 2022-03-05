package co.ke.xently.shops.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.common.DEFAULT_LOCATION
import co.ke.xently.data.*
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.*
import co.ke.xently.feature.utils.MAP_HEIGHT
import co.ke.xently.shops.R
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions

internal data class ShopDetailScreenFunction(
    val onAddShopClicked: (Shop) -> Unit = {},
    val onNavigationIconClicked: () -> Unit = {},
    val onLocationPermissionChanged: (Boolean) -> Unit = {},
)

@Composable
internal fun ShopDetailScreen(
    id: Long,
    modifier: Modifier,
    function: ShopDetailScreenFunction,
    viewModel: ShopDetailViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val result by viewModel.result.collectAsState(
        initial = Success(null),
        context = scope.coroutineContext,
    )
    val addResult by viewModel.addResult.collectAsState(
        initial = Success(null),
        context = scope.coroutineContext,
    )
    LaunchedEffect(id) {
        viewModel.get(id)
    }
    val permitReAddition = id == Shop.default().id && addResult.getOrNull() != null
    ShopDetailScreen(
        modifier = modifier,
        result = if (permitReAddition) {
            Success(null)
        } else {
            result
        },
        addResult = addResult,
        permitReAddition = permitReAddition,
        function = function.copy(
            onAddShopClicked = viewModel::addOrUpdate,
            onLocationPermissionChanged = viewModel::setLocationPermissionGranted,
        ),
    )
}

@Composable
private fun ShopDetailScreen(
    modifier: Modifier,
    result: TaskResult<Shop?>,
    addResult: TaskResult<Shop?>,
    permitReAddition: Boolean = false,
    function: ShopDetailScreenFunction = ShopDetailScreenFunction(),
) {
    val shop = result.getOrNull() ?: Shop.default()
    val toolbarTitle = stringRes(
        R.string.fs_add_shop_toolbar_title,
        if (shop.isDefault) {
            R.string.add
        } else {
            R.string.update
        },
    )
    val scaffoldState = rememberScaffoldState()
    var nameError by remember { mutableStateOf("") }
    var taxPinError by remember { mutableStateOf("") }
    var addressesError by remember { mutableStateOf("") }

    if (addResult is TaskResult.Error) {
        val exception = addResult.error as? ShopHttpException
        nameError = exception?.name?.joinToString("\n") ?: ""
        taxPinError = exception?.taxPin?.joinToString("\n") ?: ""
        addressesError = exception?.addresses?.joinToString("\n") ?: ""

        if (exception?.hasFieldErrors() != true) {
            val errorMessage =
                addResult.errorMessage ?: stringResource(R.string.generic_error_message)
            LaunchedEffect(shop.id, addResult, errorMessage) {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
    } else if (permitReAddition) {
        val message = stringResource(R.string.fs_success_adding_shop)
        LaunchedEffect(message) {
            scaffoldState.snackbarHostState.showSnackbar(message)
        }
    }

    var isAddressError by remember { mutableStateOf(addressesError.isNotBlank()) }
    val addresses =
        remember(permitReAddition) { mutableStateListOf(*shop.addresses.toTypedArray()) }
    val focusManager = LocalFocusManager.current
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
            ) {
                GoogleMapView(
                    modifier = Modifier
                        .height(MAP_HEIGHT)
                        .fillMaxWidth(),
                    markerPositions = addresses.map {
                        MarkerOptions().apply {
                            position(
                                LatLng(
                                    it.location.latitude,
                                    it.location.longitude,
                                )
                            )
                        }
                    },
                    onLocationPermissionChanged = function.onLocationPermissionChanged,
                ) {
                    setOnMapClickListener {
                        val address = Address(location = DEFAULT_LOCATION.apply {
                            latitude = it.latitude
                            longitude = it.longitude
                        })
                        if (address in addresses) {
                            return@setOnMapClickListener
                        }
                        addresses.add(address)
                        val markerOptions = MarkerOptions().apply {
                            position(
                                LatLng(
                                    address.location.latitude,
                                    address.location.longitude,
                                )
                            )
                        }
                        addMarker(markerOptions)
                        isAddressError = false
                    }
                    setOnMarkerClickListener { marker ->
                        marker.remove()
                        addresses.removeAll {
                            it.location.latitude == marker.position.latitude &&
                                    it.location.longitude == marker.position.longitude
                        }
                        isAddressError = false
                        true
                    }
                }
                Surface(
                    shape = RectangleShape,
                    color = Color.Transparent,
                    modifier = Modifier
                        .height(56.dp)
                        .padding(AppBarDefaults.ContentPadding),
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(68.dp),
                            onClick = function.onNavigationIconClicked,
                        ) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.move_back))
                        }
                        Text(toolbarTitle, style = MaterialTheme.typography.h6)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues)) {
            val isTaskLoading = arrayOf(result, addResult).any { it is TaskResult.Loading }
            if (isTaskLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                var name by remember(shop.id, shop.name, permitReAddition) {
                    mutableStateOf(TextFieldValue(if (!shop.isDefault) shop.name else ""))
                }
                var isNameError by remember {
                    mutableStateOf(nameError.isNotBlank())
                }
                TextInputLayout(
                    value = name,
                    error = nameError,
                    isError = isNameError,
                    onValueChange = {
                        name = it
                        isNameError = false
                    },
                    modifier = VerticalLayoutModifier.padding(top = VIEW_SPACE),
                    label = stringResource(R.string.fs_shop_item_detail_name_label),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                var taxPin by remember(shop.id, shop.taxPin, permitReAddition) {
                    mutableStateOf(TextFieldValue(if (!shop.isDefault) shop.taxPin else ""))
                }
                var isTaxPinError by remember {
                    mutableStateOf(taxPinError.isNotBlank())
                }
                TextInputLayout(
                    value = taxPin,
                    error = taxPinError,
                    isError = isTaxPinError,
                    onValueChange = {
                        taxPin = it
                        isTaxPinError = false
                    },
                    modifier = VerticalLayoutModifier,
                    label = stringResource(R.string.fs_shop_item_detail_tax_pin_label),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )
                if (addresses.isNotEmpty()) {
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        stringRes(R.string.fs_shop_item_detail_addresses_label),
                        style = MaterialTheme.typography.h5,
                        modifier = VerticalLayoutModifier,
                    )
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    ChipGroup(
                        modifier = VerticalLayoutModifier,
                        isSingleLine = false,
                        chipItems = addresses,
                    ) { i, address ->
                        Chip(address.toString()) {
                            addresses.removeAt(i)
                            isAddressError = false
                            // TODO: Remove marker from map...
                        }
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                if (isAddressError) {
                    Text(
                        text = addressesError,
                        modifier = VerticalLayoutModifier,
                        style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.error),
                    )
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                }
                Button(
                    enabled = arrayOf(
                        name,
                        taxPin,
                    ).all { it.text.isNotBlank() } && !isTaskLoading,
                    modifier = VerticalLayoutModifier,
                    onClick = {
                        focusManager.clearFocus()
                        function.onAddShopClicked.invoke(
                            shop.copy(
                                name = name.text.trim(),
                                taxPin = taxPin.text.trim(),
                                addresses = addresses,
                            ),
                        )
                    }
                ) {
                    Text(toolbarTitle.uppercase())
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
            result = Success(Shop(name = "Shop #1000", taxPin = "P000111222B")),
            addResult = Success(Shop(name = "Shop #1000", taxPin = "P000111222B")),
        )
    }
}

@Preview(name = "Shop detail showing progress bar", showBackground = true)
@Composable
fun ShopDetailOnNullPreview() {
    XentlyTheme {
        ShopDetailScreen(
            result = Success(null),
            addResult = Success(null),
            modifier = Modifier.fillMaxSize(),
        )
    }
}