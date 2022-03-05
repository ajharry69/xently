package co.ke.xently.products.ui.detail


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.common.DEFAULT_LOCAL_DATE_FORMAT
import co.ke.xently.common.DEFAULT_LOCAL_DATE_TIME_FORMAT
import co.ke.xently.common.DEFAULT_LOCAL_TIME_FORMAT
import co.ke.xently.data.*
import co.ke.xently.data.TaskResult.Loading
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.*
import co.ke.xently.products.R
import co.ke.xently.products.shared.*
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward

internal data class ProductDetailScreenFunction(
    val onNavigationIconClicked: () -> Unit = {},
    val onShopQueryChanged: (String) -> Unit = {},
    val onMeasurementUnitQueryChanged: (String) -> Unit = {},
    val onBrandQueryChanged: (String) -> Unit = {},
    val onAttributeQueryChanged: (AttributeQuery) -> Unit = {},
    val onDetailsSubmitted: (Product) -> Unit = {},
)

@Composable
internal fun ProductDetailScreen(
    id: Long,
    modifier: Modifier,
    function: ProductDetailScreenFunction,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val shops by viewModel.shopsResult.collectAsState(context = scope.coroutineContext)
    val result by viewModel.result.collectAsState(
        initial = Success(null),
        context = scope.coroutineContext,
    )
    val addResult by viewModel.addResult.collectAsState(
        initial = Success(null),
        context = scope.coroutineContext,
    )
    // TODO: Fix case where searching on either measurement units or shops clears fields
    val measurementUnits by viewModel.measurementUnitsResult.collectAsState(
        context = scope.coroutineContext,
    )
    val brands by viewModel.brandsResult.collectAsState(
        context = scope.coroutineContext,
    )
    val attributes by viewModel.attributesResult.collectAsState(
        context = scope.coroutineContext,
    )

    LaunchedEffect(id) {
        viewModel.get(id)
    }
    // Allow addition of more items if the screen was initially for adding.
    val permitReAddition = id == Product.default().id && addResult.getOrNull() != null

    ProductDetailScreen(
        modifier = modifier,
        result = if (permitReAddition) {
            Success(null)
        } else {
            result
        },
        addResult = addResult,
        permitReAddition = permitReAddition,
        shops = shops,
        brandSuggestions = brands,
        attributeSuggestions = attributes,
        measurementUnits = measurementUnits,
        function = function.copy(
            onShopQueryChanged = viewModel::setShopQuery,
            onMeasurementUnitQueryChanged = viewModel::setMeasurementUnitQuery,
            onBrandQueryChanged = viewModel::setBrandQuery,
            onAttributeQueryChanged = viewModel::setAttributeQuery,
            onDetailsSubmitted = viewModel::addOrUpdate,
        ),
    )
}

@Composable
private fun ProductDetailScreen(
    modifier: Modifier,
    result: TaskResult<Product?>,
    addResult: TaskResult<Product?>,
    permitReAddition: Boolean = false,
    shops: List<Shop> = emptyList(),
    brandSuggestions: List<Product.Brand> = emptyList(),
    attributeSuggestions: List<Product.Attribute> = emptyList(),
    measurementUnits: List<MeasurementUnit> = emptyList(),
    function: ProductDetailScreenFunction = ProductDetailScreenFunction(),
) {
    val product = result.getOrNull() ?: Product.default()

    val toolbarTitle = stringRes(
        R.string.fp_add_product_toolbar_title,
        if (product.isDefault) {
            R.string.add
        } else {
            R.string.update
        },
    )

    val (scrollState, scaffoldState) = Pair(rememberScrollState(), rememberScaffoldState())

    var unitError by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }
    var purchasedQuantityError by remember { mutableStateOf("") }
    var unitQuantityError by remember { mutableStateOf("") }
    var unitPriceError by remember { mutableStateOf("") }
    var shopError by remember { mutableStateOf("") }
    var datePurchasedError by remember { mutableStateOf("") }

    if (addResult is TaskResult.Error) {
        val exception = addResult.error as? ProductHttpException
        shopError = exception?.shop?.joinToString("\n") ?: ""
        nameError = exception?.name?.joinToString("\n") ?: ""
        unitError = exception?.unit?.joinToString("\n") ?: ""
        unitQuantityError = exception?.unitQuantity?.joinToString("\n") ?: ""
        unitPriceError = exception?.unitPrice?.joinToString("\n") ?: ""
        purchasedQuantityError = exception?.purchasedQuantity?.joinToString("\n") ?: ""
        datePurchasedError = exception?.datePurchased?.joinToString("\n") ?: ""

        if (exception?.hasFieldErrors() != true) {
            val message = addResult.errorMessage ?: stringResource(R.string.generic_error_message)
            LaunchedEffect(addResult, message) {
                scaffoldState.snackbarHostState.showSnackbar(message)
            }
        }
    } else if (permitReAddition) {
        val message = stringResource(R.string.fp_success_adding_product)
        LaunchedEffect(message) {
            scaffoldState.snackbarHostState.showSnackbar(message)
        }
    }
    val focusManager = LocalFocusManager.current

    val isTaskLoading = arrayOf(result, addResult).any { it is Loading }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                title = toolbarTitle,
                showProgress = isTaskLoading,
                onNavigationIconClicked = function.onNavigationIconClicked,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .verticalScroll(scrollState),
        ) {
            var isShopError by remember { mutableStateOf(shopError.isNotBlank()) }
            var shop by remember(product.shop) {
                val value = if (product.isDefault) {
                    ""
                } else {
                    product.shop.toString()
                }
                mutableStateOf(TextFieldValue(value))
            }
            var savableShop by remember(product.shopId) { mutableStateOf(product.shopId) }
            AutoCompleteTextField(
                value = shop,
                isError = isShopError,
                error = shopError,
                modifier = VerticalLayoutModifier.padding(top = VIEW_SPACE),
                label = stringResource(R.string.fp_product_detail_shop_label),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                onValueChange = {
                    shop = it
                    isShopError = false
                    function.onShopQueryChanged.invoke(it.text)
                },
                onOptionSelected = { s ->
                    shop = TextFieldValue(s.toString())
                    savableShop = s.id
                },
                suggestions = shops,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(it.name, style = MaterialTheme.typography.body1)
                    Text(it.taxPin, style = MaterialTheme.typography.subtitle1)
                }
            }
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            val name = productNameTextField(product.name, nameError, permitReAddition)
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            val unit = measurementUnitTextField(
                unit = product.unit,
                error = unitError,
                clearField = permitReAddition,
                suggestions = measurementUnits,
                onQueryChanged = function.onMeasurementUnitQueryChanged,
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            val unitQuantity = numberTextField(
                number = product.unitQuantity,
                error = unitQuantityError,
                clearField = permitReAddition,
                label = R.string.fsp_product_detail_unit_quantity_label,
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            val purchasedQuantity = numberTextField(
                number = product.purchasedQuantity,
                error = purchasedQuantityError,
                clearField = permitReAddition,
                label = R.string.fp_product_detail_purchased_quantity_label,
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            val unitPrice = numberTextField(
                number = product.unitPrice,
                error = unitPriceError,
                clearField = permitReAddition,
                label = R.string.fp_product_detail_unit_price_label,
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            var dateOfPurchase by remember {
                mutableStateOf(TextFieldValue(DEFAULT_LOCAL_DATE_FORMAT.format(product.datePurchased)))
            }
            var timeOfPurchase by remember {
                mutableStateOf(TextFieldValue(DEFAULT_LOCAL_TIME_FORMAT.format(product.datePurchased)))
            }
            var isDatePurchasedError by remember { mutableStateOf(datePurchasedError.isNotBlank()) }
            MultipleTextFieldRow(
                modifier = VerticalLayoutModifier,
                isError = isDatePurchasedError,
                error = datePurchasedError,
            ) { fieldModifier ->
                val fragmentManager = rememberFragmentManager()

                val dateOfPurchasePicker = rememberDatePickerDialog(
                    select = DEFAULT_LOCAL_DATE_FORMAT.parse(dateOfPurchase.text),
                    title = R.string.fp_product_detail_date_of_purchased_label,
                    bounds = CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointBackward.now()).build(),
                ) { dateOfPurchase = TextFieldValue(DEFAULT_LOCAL_DATE_FORMAT.format(it)) }

                TextInputLayout(
                    readOnly = true,
                    value = dateOfPurchase,
                    isError = isDatePurchasedError,
                    modifier = fieldModifier,
                    label = stringRes(R.string.fp_product_detail_date_of_purchased_label),
                    onValueChange = {
                        dateOfPurchase = it
                        isDatePurchasedError = false
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                dateOfPurchasePicker.show(fragmentManager,
                                    "ProductDetailDateOfPurchase")
                            },
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = stringResource(
                                    R.string.fp_product_detail_date_of_purchase_content_desc),
                            )
                        }
                    },
                )

                val timeOfPurchasePicker = rememberTimePickerDialog(
                    select = DEFAULT_LOCAL_TIME_FORMAT.parse(timeOfPurchase.text),
                    title = R.string.fp_product_detail_time_of_purchased_label,
                ) { timeOfPurchase = TextFieldValue(DEFAULT_LOCAL_TIME_FORMAT.format(it)) }

                TextInputLayout(
                    readOnly = true,
                    value = timeOfPurchase,
                    isError = isDatePurchasedError,
                    modifier = fieldModifier,
                    label = stringRes(R.string.fp_product_detail_time_of_purchased_label),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    onValueChange = {
                        timeOfPurchase = it
                        isDatePurchasedError = false
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                timeOfPurchasePicker.show(fragmentManager,
                                    "ProductDetailTimeOfPurchase")
                            },
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                stringResource(
                                    R.string.fp_product_detail_time_of_purchase_content_desc),
                            )
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            val brands = productBrandsView(
                clearFields = permitReAddition,
                suggestions = brandSuggestions,
                onQueryChanged = function.onBrandQueryChanged,
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            val attributes = productAttributesView(
                clearFields = permitReAddition,
                suggestions = attributeSuggestions,
                onQueryChanged = function.onAttributeQueryChanged,
            )

            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Button(
                enabled = arrayOf(
                    unit,
                    name,
                    unitQuantity,
                    purchasedQuantity,
                    unitPrice,
                    dateOfPurchase,
                    timeOfPurchase,
                ).all { it.text.isNotBlank() } && savableShop != Product.default().shopId && !isTaskLoading,
                modifier = VerticalLayoutModifier,
                onClick = {
                    focusManager.clearFocus()
                    function.onDetailsSubmitted.invoke(
                        product.copy(
                            shopId = savableShop,
                            name = name.text.trim(),
                            unit = unit.text.trim(),
                            unitQuantity = unitQuantity.text.toFloat(),
                            purchasedQuantity = purchasedQuantity.text.toFloat(),
                            unitPrice = unitPrice.text.toFloat(),
                            datePurchased = DEFAULT_LOCAL_DATE_TIME_FORMAT.parse("${dateOfPurchase.text} ${timeOfPurchase.text}")
                                ?: error("Invalid date and/or time format"),
                            brands = brands.filterNot { it.isDefault },
                            attributes = attributes.filterNot { it.name.isBlank() or it.value.isBlank() },
                        ),
                    )
                }
            ) { Text(toolbarTitle.uppercase()) }
        }
    }
}

@Preview(name = "Product detail", showBackground = true)
@Composable
fun ProductDetailPreview() {
    XentlyTheme {
        ProductDetailScreen(
            modifier = Modifier.fillMaxSize(),
            result = Success(Product.default()),
            addResult = Success(Product.default()),
        )
    }
}

@Preview(name = "Product detail showing progress bar", showBackground = true)
@Composable
fun ProductDetailOnNullPreview() {
    XentlyTheme {
        ProductDetailScreen(
            result = Success(null),
            addResult = Success(null),
            modifier = Modifier.fillMaxSize(),
        )
    }
}