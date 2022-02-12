package co.ke.xently.products.ui.detail


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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward

@Composable
internal fun ProductDetailScreen(
    id: Long?,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel(),
    onNavigationIconClicked: () -> Unit = {},
) {
    val isDefaultProduct by remember(id) {
        mutableStateOf(id == null || id == Product.default().id)
    }

    val fetch by rememberUpdatedState { viewModel.get(id!!) }
    LaunchedEffect(true) {
        if (!isDefaultProduct) fetch()
    }

    val shops by viewModel.shopsResult.collectAsState()
    val productResult by viewModel.productResult.collectAsState()
    // TODO: Fix case where searching on either measurement units or shops clears fields
    val measurementUnits by viewModel.measurementUnitsResult.collectAsState()
    val brands by viewModel.brandsResult.collectAsState()
    val attributes by viewModel.attributesResult.collectAsState()

    // Allow addition of more items if the screen was initially for adding.
    val permitReAddition = isDefaultProduct && productResult.getOrNull() != null

    ProductDetailScreen(
        modifier,
        if (permitReAddition) {
            Success(null)
        } else {
            productResult
        },
        permitReAddition,
        shops,
        brands,
        attributes,
        measurementUnits,
        onNavigationIconClicked,
        viewModel::setShopQuery,
        viewModel::setMeasurementUnitQuery,
        viewModel::setBrandQuery,
        viewModel::addOrUpdate,
    )
}

@Composable
private fun ProductDetailScreen(
    modifier: Modifier,
    result: TaskResult<Product?>,
    permitReAddition: Boolean = false,
    shops: List<Shop> = emptyList(),
    brandSuggestions: List<Brand> = emptyList(),
    attributesSuggestions: List<Attribute> = emptyList(),
    measurementUnits: List<MeasurementUnit> = emptyList(),
    onNavigationIconClicked: () -> Unit = {},
    onShopQueryChanged: (String) -> Unit = {},
    onMeasurementUnitQueryChanged: (String) -> Unit = {},
    onBrandQueryChanged: (String) -> Unit = {},
    onProductDetailsSubmitted: (Product) -> Unit = {},
) {
    val product = result.getOrNull() ?: Product.default()

    var shop by remember(product.id, product.shop) {
        val value = if (product.isDefault) {
            ""
        } else {
            product.shop.toString()
        }
        mutableStateOf(TextFieldValue(value))
    }
    var savableShop by remember(product.id, product.shopId) { mutableStateOf(product.shopId) }
    var shopError by remember { mutableStateOf("") }
    var isShopError by remember { mutableStateOf(false) }

    var name by remember(product.id, product.name) {
        mutableStateOf(TextFieldValue(product.name))
    }
    var nameError by remember { mutableStateOf("") }
    var isNameError by remember { mutableStateOf(false) }

    var unit by remember(product.id, product.unit) {
        mutableStateOf(TextFieldValue(product.unit))
    }
    var unitError by remember { mutableStateOf("") }
    var isUnitError by remember { mutableStateOf(false) }

    var unitQuantity by remember(product.id, product.unitQuantity) {
        mutableStateOf(TextFieldValue(if (product.isDefault) "" else product.unitQuantity.toString()))
    }
    var unitQuantityError by remember { mutableStateOf("") }
    var isUnitQuantityError by remember { mutableStateOf(false) }

    var purchasedQuantity by remember(product.id, product.purchasedQuantity) {
        mutableStateOf(TextFieldValue(if (product.isDefault) "" else product.purchasedQuantity.toString()))
    }
    var purchasedQuantityError by remember { mutableStateOf("") }
    var isPurchasedQuantityError by remember { mutableStateOf(false) }

    var unitPrice by remember(product.id, product.unitPrice) {
        mutableStateOf(TextFieldValue(if (product.isDefault) "" else product.unitPrice.toString()))
    }
    var unitPriceError by remember { mutableStateOf("") }
    var isUnitPriceError by remember { mutableStateOf(false) }

    var dateOfPurchase by remember {
        mutableStateOf(TextFieldValue(DEFAULT_LOCAL_DATE_FORMAT.format(product.datePurchased)))
    }
    var timeOfPurchase by remember {
        mutableStateOf(TextFieldValue(DEFAULT_LOCAL_TIME_FORMAT.format(product.datePurchased)))
    }
    var datePurchasedError by remember { mutableStateOf("") }
    var isDatePurchasedError by remember { mutableStateOf(false) }

    val brands = remember { mutableStateListOf<Brand>() }
    var brandQuery by remember { mutableStateOf(TextFieldValue("")) }

    val toolbarTitle = stringResource(
        R.string.fp_add_product_toolbar_title,
        stringResource(if (product.isDefault) R.string.add else R.string.update),
    )

    val (scrollState, scaffoldState) = Pair(rememberScrollState(), rememberScaffoldState())

    if (result is TaskResult.Error) {
        val productHttpException = result.error as? ProductHttpException
        shopError = (productHttpException?.shop?.joinToString("\n") ?: "").also {
            isShopError = it.isNotBlank()
        }
        nameError = (productHttpException?.name?.joinToString("\n") ?: "").also {
            isNameError = it.isNotBlank()
        }
        unitError = (productHttpException?.unit?.joinToString("\n") ?: "").also {
            isUnitError = it.isNotBlank()
        }
        unitQuantityError = (productHttpException?.unitQuantity?.joinToString("\n") ?: "").also {
            isUnitQuantityError = it.isNotBlank()
        }
        unitPriceError = (productHttpException?.unitPrice?.joinToString("\n") ?: "").also {
            isUnitPriceError = it.isNotBlank()
        }
        purchasedQuantityError =
            (productHttpException?.purchasedQuantity?.joinToString("\n") ?: "").also {
                isPurchasedQuantityError = it.isNotBlank()
            }
        datePurchasedError = (productHttpException?.datePurchased?.joinToString("\n") ?: "").also {
            isDatePurchasedError = it.isNotBlank()
        }

        if (productHttpException?.hasFieldErrors() != true) {
            val errorMessage = result.errorMessage ?: stringResource(R.string.generic_error_message)
            LaunchedEffect(product.id, result, errorMessage) {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
    } else if (permitReAddition) {
        val message = stringResource(R.string.fp_success_adding_product)
        LaunchedEffect(message) {
            scaffoldState.snackbarHostState.showSnackbar(message)
        }
        unit = TextFieldValue()
        name = TextFieldValue()
        unitPrice = TextFieldValue()
        unitQuantity = TextFieldValue()
        purchasedQuantity = TextFieldValue(product.purchasedQuantity.toString())
        brandQuery = TextFieldValue()
    }
    val focusManager = LocalFocusManager.current

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(toolbarTitle, onNavigationIconClicked, result is Loading)
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .verticalScroll(scrollState),
        ) {
            AutoCompleteTextField(
                value = shop,
                isError = isShopError,
                error = shopError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                label = stringResource(R.string.fp_product_detail_shop_label),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                onValueChange = {
                    shop = it
                    isShopError = false
                    onShopQueryChanged(it.text)
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
            XentlyTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = name,
                isError = isNameError,
                error = nameError,
                keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next),
                onValueChange = {
                    name = it
                    isNameError = false
                },
                label = stringResource(R.string.fp_product_detail_name_label),
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            AutoCompleteTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = unit,
                isError = isUnitError,
                error = unitError,
                label = stringResource(R.string.fp_product_detail_unit_label),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                onValueChange = {
                    unit = it
                    isUnitError = false
                    onMeasurementUnitQueryChanged(it.text)
                },
                onOptionSelected = {
                    unit = TextFieldValue(it.name)
                },
                suggestions = measurementUnits,
            ) {
                Text(it.toString(), style = MaterialTheme.typography.body1)
            }
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            XentlyTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = unitQuantity,
                isError = isUnitQuantityError,
                error = unitQuantityError,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number),
                onValueChange = {
                    unitQuantity = it
                    isUnitQuantityError = false
                },
                label = stringResource(R.string.fp_product_detail_unit_quantity_label),
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            XentlyTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = purchasedQuantity,
                isError = isPurchasedQuantityError,
                error = purchasedQuantityError,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number),
                onValueChange = {
                    purchasedQuantity = it
                    isPurchasedQuantityError = false
                },
                label = stringResource(R.string.fp_product_detail_purchased_quantity_label),
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            XentlyTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = unitPrice,
                isError = isUnitPriceError,
                error = unitPriceError,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number),
                onValueChange = {
                    unitPrice = it
                    isUnitPriceError = false
                },
                label = stringResource(R.string.fp_product_detail_unit_price_label),
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val fragmentManager = rememberFragmentManager()

                    val dateOfPurchasePicker = rememberDatePickerDialog(
                        select = DEFAULT_LOCAL_DATE_FORMAT.parse(dateOfPurchase.text),
                        title = R.string.fp_product_detail_date_of_purchased_label,
                        bounds = CalendarConstraints.Builder()
                            .setValidator(DateValidatorPointBackward.now()).build(),
                    ) { dateOfPurchase = TextFieldValue(DEFAULT_LOCAL_DATE_FORMAT.format(it)) }

                    XentlyTextField(
                        readOnly = true,
                        value = dateOfPurchase,
                        isError = isDatePurchasedError,
                        modifier = Modifier.weight(1f),
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

                    XentlyTextField(
                        readOnly = true,
                        value = timeOfPurchase,
                        isError = isDatePurchasedError,
                        modifier = Modifier.weight(1f),
                        label = stringRes(R.string.fp_product_detail_time_of_purchased_label),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        onValueChange = {
                            timeOfPurchase = it
                            isDatePurchasedError = false
                        },
                        trailingIcon = {
                            IconButton({
                                timeOfPurchasePicker.show(fragmentManager,
                                    "ProductDetailTimeOfPurchase")
                            }) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    stringResource(
                                        R.string.fp_product_detail_time_of_purchase_content_desc),
                                )
                            }
                        },
                    )
                }
                if (isDatePurchasedError) {
                    TextFieldErrorText(datePurchasedError, Modifier.fillMaxWidth())
                }
            }
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            var showAddBrandIcon by remember { mutableStateOf(false) }
            val addBrand: (Brand) -> Unit = {
                brands.add(0, it)
                brandQuery = TextFieldValue() // Reset search
            }
            AutoCompleteTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = brandQuery,
                label = stringResource(R.string.fp_product_detail_brand_query_label),
                helpText = if(showAddBrandIcon) {
                    stringResource(R.string.fp_product_detail_brand_query_help_text)
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                onValueChange = {
                    brandQuery = it
                    onBrandQueryChanged(it.text)
                },
                trailingIcon = if (!showAddBrandIcon) {
                    null
                } else {
                    {
                        IconButton(onClick = { addBrand(Brand(name = brandQuery.text.trim())) }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                },
                onOptionSelected = addBrand,
                wasSuggestionPicked = {
                    showAddBrandIcon = !it && brandQuery.text.isNotBlank()
                },
                suggestions = brandSuggestions,
            ) {
                Text(it.toString(), style = MaterialTheme.typography.body1)
            }
            if (brands.isNotEmpty()) {
                Text(
                    stringRes(R.string.fp_product_detail_brands_title),
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                ChipGroup(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    isSingleLine = false, chipItems = brands,
                ) { i, b ->
                    Chip(b.toString()) {
                        brands.removeAt(i)
                    }
                }
            }
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
                ).all { it.text.isNotBlank() } && savableShop != Product.default().shopId,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = {
                    focusManager.clearFocus()
                    onProductDetailsSubmitted(product.copy(
                        shopId = savableShop,
                        name = name.text,
                        unit = unit.text,
                        unitQuantity = unitQuantity.text.toFloat(),
                        purchasedQuantity = purchasedQuantity.text.toFloat(),
                        unitPrice = unitPrice.text.toFloat(),
                        datePurchased = DEFAULT_LOCAL_DATE_TIME_FORMAT.parse("${dateOfPurchase.text} ${timeOfPurchase.text}")
                            ?: error("Invalid date and/or time format"),
                        brands = brands.filterNot { it.isDefault },
                    ))
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
        )
    }
}

@Preview(name = "Product detail showing progress bar", showBackground = true)
@Composable
fun ProductDetailOnNullPreview() {
    XentlyTheme {
        ProductDetailScreen(modifier = Modifier.fillMaxSize(), result = Success(null))
    }
}