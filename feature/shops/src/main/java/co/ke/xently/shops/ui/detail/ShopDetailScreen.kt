package co.ke.xently.shops.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.data.errorMessage
import co.ke.xently.data.getOrNull
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.*
import co.ke.xently.shops.R
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions

internal data class ShopDetailScreenArgs(
    val name: String = "",
    val moveBack: Boolean = false,
)

internal data class ShopDetailScreenFunction(
    val onAddShopClicked: (Shop) -> Unit = {},
    val onNavigationIconClicked: () -> Unit = {},
    val onLocationPermissionChanged: (Boolean) -> Unit = {},
)

@Composable
internal fun ShopDetailScreen(
    id: Long,
    modifier: Modifier,
    args: ShopDetailScreenArgs,
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
    if (permitReAddition && args.moveBack) {
        SideEffect(function.onNavigationIconClicked)
    }

    ShopDetailScreen(
        args = args,
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ShopDetailScreen(
    modifier: Modifier,
    result: TaskResult<Shop?>,
    addResult: TaskResult<Shop?>,
    permitReAddition: Boolean = false,
    args: ShopDetailScreenArgs = ShopDetailScreenArgs(),
    function: ShopDetailScreenFunction = ShopDetailScreenFunction(),
) {
    val shop = result.getOrNull() ?: Shop.default().copy(name = args.name)
    val toolbarTitle = stringRes(
        R.string.fs_add_shop_toolbar_title,
        if (shop.isDefault) {
            R.string.add
        } else {
            R.string.update
        },
    )
    val scaffoldState = rememberBackdropScaffoldState(BackdropValue.Revealed)
    var nameError by remember { mutableStateOf("") }
    var taxPinError by remember { mutableStateOf("") }

    if (addResult is TaskResult.Error) {
        val exception = addResult.error as? ShopHttpException
        nameError = exception?.name?.joinToString("\n") ?: ""
        taxPinError = exception?.taxPin?.joinToString("\n") ?: ""

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

    val isTaskLoading = arrayOf(result, addResult).any { it is TaskResult.Loading }
    BackdropScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        frontLayerScrimColor = Color.Unspecified,
        backLayerBackgroundColor = MaterialTheme.colors.background,
        appBar = {
            ToolbarWithProgressbar(
                title = toolbarTitle,
                showProgress = isTaskLoading,
                onNavigationIconClicked = function.onNavigationIconClicked,
            )
        },
        backLayerContent = {
            ShopDetailEntry(
                shop = shop,
                nameError = nameError,
                taxPinError = taxPinError,
                toolbarTitle = toolbarTitle,
                isTaskLoading = isTaskLoading,
                permitReAddition = permitReAddition,
                args = args,
                function = function,
            )
        },
        frontLayerContent = {
            Column(modifier = Modifier.fillMaxSize()) {
                val markerPositions = if (shop.coordinate != null) {
                    listOf(MarkerOptions().apply {
                        position(LatLng(shop.coordinate!!.lat, shop.coordinate!!.lon))
                    })
                } else {
                    emptyList()
                }
                GoogleMapView(
                    modifier = Modifier.fillMaxSize(),
                    markerPositions = markerPositions,
                    onLocationPermissionChanged = function.onLocationPermissionChanged,
                ) {
                    setOnMapClickListener {
                        // TODO: Add coordinate to shop
                    }
                    setOnMarkerClickListener { marker ->
                        marker.remove()
                        true
                    }
                }

                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        },
    )
}

@Composable
private fun ShopDetailEntry(
    modifier: Modifier = Modifier,
    shop: Shop,
    nameError: String,
    taxPinError: String,
    toolbarTitle: String,
    isTaskLoading: Boolean,
    permitReAddition: Boolean,
    args: ShopDetailScreenArgs,
    function: ShopDetailScreenFunction,
) {
    val focusManager = LocalFocusManager.current
    Column(modifier = modifier) {
        var name by remember(shop.id, shop.name, permitReAddition) {
            mutableStateOf(TextFieldValue(if (!shop.isDefault) shop.name else args.name))
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
        Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))
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
        Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))
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
                    ),
                )
            }
        ) {
            Text(toolbarTitle.uppercase())
        }
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Preview(name = "Shop detail", showBackground = true)
@Composable
private fun ShopDetailPreview() {
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
private fun ShopDetailOnNullPreview() {
    XentlyTheme {
        ShopDetailScreen(
            result = Success(null),
            addResult = Success(null),
            modifier = Modifier.fillMaxSize(),
        )
    }
}