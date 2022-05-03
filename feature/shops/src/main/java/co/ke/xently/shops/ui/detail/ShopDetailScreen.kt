package co.ke.xently.shops.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.common.KENYA
import co.ke.xently.data.Shop
import co.ke.xently.data.Shop.Coordinate
import co.ke.xently.data.TaskResult
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.data.errorMessage
import co.ke.xently.data.getOrNull
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.*
import co.ke.xently.feature.utils.MAP_HEIGHT
import co.ke.xently.shops.R
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions

const val TEST_TAG_SHOP_DETAIL_BODY_CONTAINER = "TEST_TAG_SHOP_DETAIL_BODY_CONTAINER"

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
    var shop = result.getOrNull() ?: Shop.default().copy(name = args.name)
    var coordinate by remember(shop.coordinate) {
        mutableStateOf(shop.coordinate)
    }
    val toolbarTitle = stringRes(
        R.string.fs_add_shop_toolbar_title,
        if (shop.isDefault) {
            R.string.add
        } else {
            R.string.update
        },
    )
    val (scrollState, scaffoldState) = Pair(rememberScrollState(), rememberScaffoldState())
    var nameError by remember { mutableStateOf("") }
    var townError by remember { mutableStateOf("") }
    var taxPinError by remember { mutableStateOf("") }
    var coordinateError by remember { mutableStateOf("") }

    if (addResult is TaskResult.Error) {
        val exception = addResult.error as? ShopHttpException
        nameError = exception?.name?.joinToString("\n") ?: ""
        townError = exception?.town?.joinToString("\n") ?: ""
        taxPinError = exception?.taxPin?.joinToString("\n") ?: ""
        coordinateError = exception?.coordinate?.joinToString("\n") ?: ""

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
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        topBar = {
            Box(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .fillMaxWidth()
            ) {
                val markerPositions = if (coordinate != null) {
                    val marker = MarkerOptions().apply {
                        position(LatLng(coordinate!!.lat, coordinate!!.lon))
                    }
                    listOf(marker)
                } else {
                    emptyList()
                }
                GoogleMapView(
                    modifier = Modifier
                        .height(MAP_HEIGHT)
                        .fillMaxWidth(),
                    markerPositions = markerPositions,
                    onLocationPermissionChanged = function.onLocationPermissionChanged,
                ) {
                    setOnMapClickListener {
                        coordinate = Coordinate(it.latitude, it.longitude)
                        shop = shop.copy(coordinate = coordinate)
                    }
                    setOnMarkerClickListener {
                        coordinate = null
                        shop = shop.copy(coordinate = coordinate)
                        true
                    }
                }
                ToolbarWithProgressbar(
                    elevation = 0.dp,
                    title = toolbarTitle,
                    backgroundColor = Color.Transparent,
                    onNavigationIconClicked = function.onNavigationIconClicked,
                )
            }
        },
    ) { paddingValues ->
        ShopDetailEntry(
            shop = shop,
            args = args,
            function = function,
            nameError = nameError,
            townError = townError,
            taxPinError = taxPinError,
            toolbarTitle = toolbarTitle,
            isTaskLoading = isTaskLoading,
            coordinateError = coordinateError,
            permitReAddition = permitReAddition,
            modifier = modifier
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .semantics { testTag = TEST_TAG_SHOP_DETAIL_BODY_CONTAINER },
        )
    }
}

@Composable
private fun ShopDetailEntry(
    modifier: Modifier = Modifier,
    shop: Shop,
    nameError: String,
    townError: String,
    taxPinError: String,
    coordinateError: String,
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
        )
        Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))
        var town by remember(shop.id, shop.town, permitReAddition) {
            mutableStateOf(TextFieldValue(if (!shop.isDefault) shop.town else ""))
        }
        var isTownError by remember {
            mutableStateOf(townError.isNotBlank())
        }
        TextInputLayout(
            value = town,
            error = townError,
            isError = isTownError,
            onValueChange = {
                town = it
                isTownError = false
            },
            modifier = VerticalLayoutModifier,
            label = stringResource(R.string.fs_shop_item_detail_town_label),
        )
        Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))
        var coordinate by remember(shop.id, shop.coordinate, permitReAddition) {
            mutableStateOf(TextFieldValue(if (!shop.isDefault && shop.coordinate != null) shop.coordinate.toString() else ""))
        }
        var isCoordinateError by remember {
            mutableStateOf(coordinateError.isNotBlank())
        }
        TextInputLayout(
            value = coordinate,
            error = coordinateError,
            isError = isCoordinateError,
            onValueChange = {
                coordinate = it
                isCoordinateError = false
            },
            modifier = VerticalLayoutModifier,
            label = stringResource(R.string.fs_shop_item_detail_coordinate_label),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )
        Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))
        Button(
            enabled = arrayOf(
                name,
                taxPin,
                coordinate,
            ).all { it.text.isNotBlank() } && !isTaskLoading,
            modifier = VerticalLayoutModifier,
            onClick = {
                focusManager.clearFocus()
                function.onAddShopClicked.invoke(
                    shop.copy(
                        name = name.text.trim(),
                        taxPin = taxPin.text.trim(),
                        town = town.text.trim(),
                    ),
                )
            },
        ) {
            Text(toolbarTitle.uppercase(KENYA))
        }
    }
}

@Preview(name = "Shop detail", showBackground = true)
@Composable
private fun ShopDetailPreview() {
    XentlyTheme {
        ShopDetailScreen(
            modifier = Modifier.fillMaxSize(),
            result = Success(Shop.default()),
            addResult = Success(Shop.default()),
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