package co.ke.xently.products.ui.detail


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.common.DEFAULT_LOCAL_DATE_FORMAT
import co.ke.xently.common.DEFAULT_LOCAL_DATE_TIME_FORMAT
import co.ke.xently.common.DEFAULT_LOCAL_TIME_FORMAT
import co.ke.xently.data.*
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.AutoCompleteTextField
import co.ke.xently.feature.ui.TextFieldErrorText
import co.ke.xently.products.R
import kotlinx.coroutines.Job

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

    // Allow addition of more items if the screen was initially for adding.
    val permitReAddition = isDefaultProduct && productResult.getOrNull() != null

    var unitsJob: Job? = null
    var shopsJob: Job? = null

    ProductDetailScreen(
        modifier,
        if (permitReAddition) {
            Success(null)
        } else {
            productResult
        },
        permitReAddition,
        shops,
        measurementUnits,
        onNavigationIconClicked,
        {
            shopsJob?.cancel()
            shopsJob = viewModel.getShops(it)
        },
        {
            unitsJob?.cancel()
            unitsJob = viewModel.getMeasurementUnits(it)
        },
    ) {
        viewModel.addOrUpdate(it)
    }
}

@Composable
private fun ProductDetailScreen(
    modifier: Modifier,
    result: TaskResult<Product?>,
    permitReAddition: Boolean = false,
    shops: List<Shop> = emptyList(),
    measurementUnits: List<MeasurementUnit> = emptyList(),
    onNavigationIconClicked: () -> Unit = {},
    onShopQueryChanged: (String) -> Unit = {},
    onMeasurementUnitQueryChanged: (String) -> Unit = {},
    onProductDetailsSubmitted: (Product) -> Unit = {},
) {
    val product = result.getOrNull() ?: Product.default()

    var shop by remember(product.id, product.shop) {
        mutableStateOf(if (product.isDefault) "" else product.shop.toString())
    }
    var savableShop by remember(product.id, product.shop) { mutableStateOf(product.shop) }
    var shopError by remember { mutableStateOf("") }
    var isShopError by remember { mutableStateOf(false) }

    var name by remember(product.id, product.name) {
        mutableStateOf(TextFieldValue(product.name))
    }
    var nameError by remember { mutableStateOf("") }
    var isNameError by remember { mutableStateOf(false) }

    var unit by remember(product.id, product.unit) {
        mutableStateOf(product.unit)
    }
    var unitError by remember { mutableStateOf("") }
    var isUnitError by remember { mutableStateOf(false) }

    var unitQuantity by remember(product.id, product.unitQuantity) {
        mutableStateOf(TextFieldValue(if (product.isDefault) "" else product.unitQuantity.toString()))
    }
    var unitQuantityError by remember { mutableStateOf("") }
    var isUnitQuantityError by remember { mutableStateOf(false) }

    var unitPrice by remember(product.id, product.unitPrice) {
        mutableStateOf(TextFieldValue(if (product.isDefault) "" else product.unitPrice.toString()))
    }
    var unitPriceError by remember { mutableStateOf("") }
    var isUnitPriceError by remember { mutableStateOf(false) }

    var dateOfPurchase by remember(product.id, product.datePurchased) {
        mutableStateOf(TextFieldValue(DEFAULT_LOCAL_DATE_FORMAT.format(product.datePurchased)))
    }
    var timeOfPurchase by remember(product.id, product.datePurchased) {
        mutableStateOf(TextFieldValue(DEFAULT_LOCAL_TIME_FORMAT.format(product.datePurchased)))
    }
    var datePurchasedError by remember { mutableStateOf("") }
    var isDatePurchasedError by remember { mutableStateOf(false) }

    val toolbarTitle = stringResource(
        R.string.fp_add_product_toolbar_title,
        stringResource(if (product.isDefault) R.string.fp_add else R.string.fp_update),
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
        datePurchasedError = (productHttpException?.datePurchased?.joinToString("\n") ?: "").also {
            isDatePurchasedError = it.isNotBlank()
        }

        if (productHttpException?.hasFieldErrors() != true) {
            val errorMessage =
                result.errorMessage ?: stringResource(R.string.fp_generic_error_message)
            LaunchedEffect(product.id, result, errorMessage) {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
    } else if (permitReAddition) {
        val message = stringResource(R.string.fp_success_adding_product)
        LaunchedEffect(message) {
            scaffoldState.snackbarHostState.showSnackbar(message)
        }
        unit = ""
        name = TextFieldValue("")
        unitPrice = TextFieldValue("")
        unitQuantity = TextFieldValue("")
    }
    val focusManager = LocalFocusManager.current

    Scaffold(scaffoldState = scaffoldState) {
        Column(modifier = modifier) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp,
                    navigationIcon = {
                        IconButton(onClick = onNavigationIconClicked) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.fp_navigation_icon_content_description),
                            )
                        }
                    },
                    title = { Text(toolbarTitle) },
                )
                if (result is TaskResult.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                    label = { Text(stringResource(R.string.fp_product_detail_shop_label)) },
                    keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next),
                    onValueChange = {
                        shop = it
                        isShopError = false
                        onShopQueryChanged(it)
                    },
                    onOptionSelected = {
                        shop = it.toString()
                        savableShop = it.id
                    },
                    suggestions = shops,
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(it.name, style = MaterialTheme.typography.body1)
                        Text(it.taxPin, style = MaterialTheme.typography.subtitle1)
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    TextField(
                        value = name,
                        singleLine = true,
                        isError = isNameError,
                        onValueChange = {
                            name = it
                            isNameError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.fp_product_detail_name_label)) },
                        keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next),
                    )
                    if (isNameError) {
                        TextFieldErrorText(nameError, Modifier.fillMaxWidth())
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                AutoCompleteTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    value = unit,
                    singleLine = true,
                    isError = isUnitError,
                    error = unitError,
                    label = { Text(text = stringResource(R.string.fp_product_detail_unit_label)) },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    onValueChange = {
                        unit = it
                        isUnitError = false
                        onMeasurementUnitQueryChanged(it)
                    },
                    onOptionSelected = {
                        unit = it.name
                    },
                    suggestions = measurementUnits,
                ) {
                    Text(it.name, style = MaterialTheme.typography.body1)
                }
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    TextField(
                        value = unitQuantity,
                        singleLine = true,
                        isError = isUnitQuantityError,
                        onValueChange = {
                            unitQuantity = it
                            isUnitQuantityError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.fp_product_detail_unit_quantity_label)) },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number),
                    )
                    if (isUnitQuantityError) {
                        TextFieldErrorText(unitQuantityError, Modifier.fillMaxWidth())
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    TextField(
                        value = unitPrice,
                        singleLine = true,
                        isError = isUnitPriceError,
                        onValueChange = {
                            unitPrice = it
                            isUnitPriceError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.fp_product_detail_unit_price_label)) },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number),
                    )
                    if (isUnitPriceError) {
                        TextFieldErrorText(unitPriceError, Modifier.fillMaxWidth())
                    }
                }
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
                        TextField(
                            value = dateOfPurchase,
                            singleLine = true,
                            isError = isDatePurchasedError,
                            onValueChange = {
                                dateOfPurchase = it
                                isDatePurchasedError = false
                            },
                            trailingIcon = {
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = stringResource(
                                            R.string.fp_product_detail_date_of_purchase_content_desc),
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text(text = stringResource(R.string.fp_product_detail_date_of_purchased_label)) },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        )
                        TextField(
                            value = timeOfPurchase,
                            singleLine = true,
                            isError = isDatePurchasedError,
                            onValueChange = {
                                timeOfPurchase = it
                                isDatePurchasedError = false
                            },
                            trailingIcon = {
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = stringResource(
                                            R.string.fp_product_detail_time_of_purchase_content_desc),
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text(text = stringResource(R.string.fp_product_detail_time_of_purchased_label)) },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        )
                    }
                    if (isDatePurchasedError) {
                        TextFieldErrorText(datePurchasedError, Modifier.fillMaxWidth())
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Button(
                    enabled = arrayOf(
                        name,
                        unitQuantity,
                        unitPrice,
                        dateOfPurchase,
                        timeOfPurchase,
                    ).all { it.text.isNotBlank() } && unit.isNotBlank() && savableShop != Product.default().shop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = {
                        focusManager.clearFocus()
                        onProductDetailsSubmitted(product.copy(
                            shop = savableShop,
                            name = name.text,
                            unit = unit,
                            unitQuantity = unitQuantity.text.toFloat(),
                            unitPrice = unitPrice.text.toFloat(),
                            datePurchased = DEFAULT_LOCAL_DATE_TIME_FORMAT.parse("${dateOfPurchase.text} ${timeOfPurchase.text}")
                                ?: error("Invalid date and/or time format"),
                        ))
                    }
                ) { Text(toolbarTitle.uppercase()) }
            }
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