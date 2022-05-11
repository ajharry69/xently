package co.ke.xently.products.ui.detail


import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentManager
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

internal const val TEST_TAG_PRODUCT_DETAIL_BODY_CONTAINER = "TEST_TAG_PRODUCT_DETAIL_BODY_CONTAINER"

internal data class ProductDetailScreenFunction(
    val onNavigationIconClicked: () -> Unit = {},
    val onShopQueryChanged: (String) -> Unit = {},
    val onBrandQueryChanged: (String) -> Unit = {},
    val onDetailsSubmitted: (Product) -> Unit = {},
    val onAddNewShop: (shopName: String) -> Unit = {},
    val onProductQueryChanged: (String) -> Unit = {},
    val onMeasurementUnitQueryChanged: (String) -> Unit = {},
    val onAttributeQueryChanged: (AttributeQuery) -> Unit = {},
)

@Composable
internal fun ProductDetailScreen(
    id: Long,
    modifier: Modifier,
    function: ProductDetailScreenFunction,
    viewModel: ProductDetailViewModel = hiltViewModel(),
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
    // TODO: Fix case where searching on either measurement units or shops clears fields
    val shops by viewModel.shopsResult.collectAsState(
        initial = emptyList(),
        context = scope.coroutineContext,
    )
    val measurementUnits by viewModel.measurementUnitsResult.collectAsState(
        initial = emptyList(),
        context = scope.coroutineContext,
    )
    val brands by viewModel.brandsResult.collectAsState(
        initial = emptyList(),
        context = scope.coroutineContext,
    )
    val products by viewModel.productsResult.collectAsState(
        initial = emptyList(),
        context = scope.coroutineContext,
    )
    val attributes by viewModel.attributesResult.collectAsState(
        initial = emptyList(),
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
        shopSuggestions = shops,
        brandSuggestions = brands,
        productSuggestions = products,
        attributeSuggestions = attributes,
        measurementUnits = measurementUnits,
        fragmentManager = rememberFragmentManager(),
        function = function.copy(
            onDetailsSubmitted = viewModel::addOrUpdate,
            onShopQueryChanged = viewModel::setShopQuery,
            onBrandQueryChanged = viewModel::setBrandQuery,
            onProductQueryChanged = viewModel::setProductQuery,
            onAttributeQueryChanged = viewModel::setAttributeQuery,
            onMeasurementUnitQueryChanged = viewModel::setMeasurementUnitQuery,
        ),
    )
}

@Composable
@VisibleForTesting
internal fun ProductDetailScreen(
    modifier: Modifier,
    result: TaskResult<Product?>,
    addResult: TaskResult<Product?>,
    permitReAddition: Boolean = false,
    shopSuggestions: List<Shop> = emptyList(),
    productSuggestions: List<Product> = emptyList(),
    brandSuggestions: List<Product.Brand> = emptyList(),
    attributeSuggestions: List<Product.Attribute> = emptyList(),
    measurementUnits: List<MeasurementUnit> = emptyList(),
    fragmentManager: FragmentManager? = null,
    function: ProductDetailScreenFunction = ProductDetailScreenFunction(),
) {
    val product = result.getOrNull() ?: Product.default()

    val toolbarTitle = stringRes(
        R.string.fp_detail_toolbar_title,
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
                .verticalScroll(scrollState)
                .semantics { testTag = TEST_TAG_PRODUCT_DETAIL_BODY_CONTAINER },
        ) {
            var isShopError by remember {
                mutableStateOf(shopError.isNotBlank())
            }
            var savableShop by remember(product.shopId) {
                mutableStateOf(product.shopId)
            }
            var shop by remember(product.shop) {
                val value = if (product.isDefault) {
                    ""
                } else {
                    product.shop.toString()
                }
                mutableStateOf(TextFieldValue(value))
            }
            val showAddShopIcon by rememberSaveable(shop.text, savableShop) {
                mutableStateOf(savableShop == Shop.default().id && shop.text.isNotBlank())
            }
            // TODO: Auto-search after adding a new shop has succeeded
            AutoCompleteTextField(
                value = shop,
                error = shopError,
                suggestions = shopSuggestions,
                isError = isShopError,
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
                helpText = if (showAddShopIcon) {
                    stringResource(R.string.fp_product_detail_shop_query_help_text)
                } else {
                    null
                },
                trailingIcon = if (showAddShopIcon) {
                    {
                        val description =
                            stringResource(R.string.fp_product_detail_shop_add_icon_description)
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                function.onAddNewShop.invoke(shop.text)
                            },
                            modifier = Modifier.semantics { contentDescription = description },
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                } else {
                    null
                },
            ) {
                if (it.descriptiveName.isBlank()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(it.name, style = MaterialTheme.typography.body1)
                        Text(it.taxPin, style = MaterialTheme.typography.subtitle1)
                    }
                } else {
                    Text(it.descriptiveName, style = MaterialTheme.typography.body1)
                }
            }
            Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))

            var initialMeasurementUnit by remember {
                mutableStateOf(
                    if (product.isDefault) {
                        ""
                    } else {
                        product.unit
                    }
                )
            }
            var initialMeasurementUnitQuantity by remember {
                mutableStateOf(product.unitQuantity)
            }
            var initialBrands by remember {
                mutableStateOf(product.brands)
            }
            var initialAttributes by remember {
                mutableStateOf(product.attributes)
            }
            val name = productNameTextField(
                initial = if (product.isDefault) {
                    ""
                } else {
                    product.name
                },
                error = nameError,
                clearField = permitReAddition,
                suggestions = productSuggestions,
                onQueryChanged = function.onProductQueryChanged,
                onOptionSelected = {
                    initialBrands = it.brands
                    initialMeasurementUnit = it.unit
                    initialAttributes = it.attributes
                    initialMeasurementUnitQuantity = it.unitQuantity
                },
            )
            Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))

            val unit = measurementUnitTextField(
                error = unitError,
                unit = initialMeasurementUnit,
                clearField = permitReAddition,
                suggestions = measurementUnits,
                onQueryChanged = function.onMeasurementUnitQueryChanged,
            )
            Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))

            val unitQuantity = numberTextField(
                error = unitQuantityError,
                clearField = permitReAddition,
                initial = initialMeasurementUnitQuantity,
                label = R.string.fsp_product_detail_unit_quantity_label,
            )
            Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))

            val purchasedQuantity = numberTextField(
                initial = product.purchasedQuantity,
                error = purchasedQuantityError,
                clearField = permitReAddition,
                label = R.string.fp_product_detail_purchased_quantity_label,
            )
            Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))

            val unitPrice = numberTextField(
                initial = if (product.isDefault) {
                    0
                } else {
                    product.unitPrice
                },
                error = unitPriceError,
                clearField = permitReAddition,
                label = R.string.fp_product_detail_unit_price_label,
            )
            Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))

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
                                fragmentManager?.let {
                                    dateOfPurchasePicker.show(
                                        it,
                                        "ProductDetailDateOfPurchase"
                                    )
                                }
                            },
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = stringResource(
                                    R.string.fp_product_detail_date_of_purchase_content_desc
                                ),
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
                                fragmentManager?.let {
                                    timeOfPurchasePicker.show(
                                        it,
                                        "ProductDetailTimeOfPurchase"
                                    )
                                }
                            },
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                stringResource(
                                    R.string.fp_product_detail_time_of_purchase_content_desc
                                ),
                            )
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))

            val brands = productBrandsView(
                initial = initialBrands,
                clearFields = permitReAddition,
                suggestions = brandSuggestions,
                onQueryChanged = function.onBrandQueryChanged,
            )
            Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))

            val attributes = productAttributesView(
                initial = initialAttributes,
                clearFields = permitReAddition,
                suggestions = attributeSuggestions,
                onQueryChanged = function.onAttributeQueryChanged,
            )

            Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))
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
                            unitQuantity = unitQuantity.text.trim().toFloat(),
                            purchasedQuantity = purchasedQuantity.text.trim().toFloat(),
                            unitPrice = unitPrice.text.trim().toFloat(),
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
private fun ProductDetailPreview() {
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
private fun ProductDetailOnNullPreview() {
    XentlyTheme {
        ProductDetailScreen(
            result = Success(null),
            addResult = Success(null),
            modifier = Modifier.fillMaxSize(),
        )
    }
}