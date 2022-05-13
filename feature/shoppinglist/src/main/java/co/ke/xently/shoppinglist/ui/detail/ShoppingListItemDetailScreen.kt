package co.ke.xently.shoppinglist.ui.detail

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.common.KENYA
import co.ke.xently.data.*
import co.ke.xently.data.ShoppingListItem.Attribute
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.ui.*
import co.ke.xently.products.shared.*
import co.ke.xently.shoppinglist.R

internal data class ShoppingListItemScreenFunction(
    val onBrandQueryChanged: (String) -> Unit = {},
    val onProductQueryChanged: (String) -> Unit = {},
    val sharedFunction: SharedFunction = SharedFunction(),
    val onDetailsSubmitted: (ShoppingListItem) -> Unit = {},
    val onMeasurementUnitQueryChanged: (String) -> Unit = {},
    val onAttributeQueryChanged: (AttributeQuery) -> Unit = {},
)

@Composable
internal fun ShoppingListItemScreen(
    id: Long,
    modifier: Modifier,
    function: ShoppingListItemScreenFunction,
    viewModel: ShoppingListItemViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val result by viewModel.result.collectAsState(
        context = scope.coroutineContext,
        initial = TaskResult.Success(null),
    )
    val addResult by viewModel.addResult.collectAsState(
        context = scope.coroutineContext,
        initial = TaskResult.Success(null),
    )
    // TODO: Fix case where searching on either measurement units or shops clears fields
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
    val permitReAddition = id == ShoppingListItem.default().id && addResult.getOrNull() != null

    ShoppingListItemScreen(
        modifier = modifier,
        result = if (permitReAddition) {
            TaskResult.Success(null)
        } else {
            result
        },
        addResult = addResult,
        permitReAddition = permitReAddition,
        brandSuggestions = brands,
        productSuggestions = products,
        attributeSuggestions = attributes,
        measurementUnitSuggestions = measurementUnits,
        function = function.copy(
            onDetailsSubmitted = viewModel::addOrUpdate,
            onBrandQueryChanged = viewModel::setBrandQuery,
            onProductQueryChanged = viewModel::setProductQuery,
            onAttributeQueryChanged = viewModel::setAttributeQuery,
            onMeasurementUnitQueryChanged = viewModel::setMeasurementUnitQuery,
        ),
    )
}

@Composable
@VisibleForTesting
internal fun ShoppingListItemScreen(
    modifier: Modifier,
    result: TaskResult<ShoppingListItem?>,
    addResult: TaskResult<ShoppingListItem?>,
    permitReAddition: Boolean = false,
    productSuggestions: List<Product> = emptyList(),
    brandSuggestions: List<Product.Brand> = emptyList(),
    attributeSuggestions: List<Product.Attribute> = emptyList(),
    measurementUnitSuggestions: List<MeasurementUnit> = emptyList(),
    function: ShoppingListItemScreenFunction = ShoppingListItemScreenFunction(),
) {
    val item = result.getOrNull() ?: ShoppingListItem.default()

    val toolbarTitle = stringRes(
        R.string.fsl_detail_screen_toolbar_title,
        if (item.isDefault) {
            R.string.add
        } else {
            R.string.update
        },
    )

    val (scrollState, scaffoldState) = Pair(rememberScrollState(), rememberScaffoldState())

    var nameError by remember { mutableStateOf("") }
    var unitError by remember { mutableStateOf("") }
    var unitQuantityError by remember { mutableStateOf("") }
    var purchaseQuantityError by remember { mutableStateOf("") }

    if (addResult is TaskResult.Error) {
        val exception = addResult.error as? ShoppingListItemHttpException
        nameError = exception?.name?.joinToString("\n") ?: ""
        unitError = exception?.unit?.joinToString("\n") ?: ""
        unitQuantityError = exception?.unitQuantity?.joinToString("\n") ?: ""
        purchaseQuantityError = exception?.purchaseQuantity?.joinToString("\n") ?: ""

        if (exception?.hasFieldErrors() != true) {
            val message = addResult.errorMessage ?: stringResource(R.string.generic_error_message)
            LaunchedEffect(addResult, message) {
                scaffoldState.snackbarHostState.showSnackbar(message)
            }
        }
    } else if (permitReAddition) {
        val message = stringResource(R.string.fsl_success_adding_shopping_list_item)
        LaunchedEffect(message) {
            scaffoldState.snackbarHostState.showSnackbar(message)
        }
    }
    val focusManager = LocalFocusManager.current

    val isTaskLoading = arrayOf(result, addResult).any { it is TaskResult.Loading }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                title = toolbarTitle,
                showProgress = isTaskLoading,
                onNavigationIconClicked = function.sharedFunction.onNavigationIconClicked,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .verticalScroll(scrollState),
        ) {
            Spacer(modifier = Modifier.padding(top = VIEW_SPACE))
            var initialMeasurementUnit by remember {
                mutableStateOf(
                    if (item.isDefault) {
                        ""
                    } else {
                        item.unit
                    }
                )
            }
            var initialMeasurementUnitQuantity by remember {
                mutableStateOf(item.unitQuantity)
            }
            var initialBrands by remember {
                mutableStateOf(item.brands.map { Product.Brand(it.name) })
            }
            var initialAttributes by remember {
                mutableStateOf(item.attributes.map {
                    Product.Attribute(
                        it.name,
                        it.value,
                        values = it.values
                    )
                })
            }
            val name = productNameTextField(
                initial = if (item.isDefault) {
                    ""
                } else {
                    item.name
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
                suggestions = measurementUnitSuggestions,
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

            val purchaseQuantity = numberTextField(
                initial = item.purchaseQuantity,
                error = purchaseQuantityError,
                clearField = permitReAddition,
                label = R.string.fsl_text_field_label_purchase_quantity,
            )
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
                    purchaseQuantity,
                ).all { it.text.isNotBlank() } && !isTaskLoading,
                modifier = VerticalLayoutModifier.padding(bottom = VIEW_SPACE),
                onClick = {
                    focusManager.clearFocus()
                    function.onDetailsSubmitted.invoke(
                        item.copy(
                            name = name.text.trim(),
                            unit = unit.text.trim(),
                            unitQuantity = unitQuantity.text.trim().toFloat(),
                            purchaseQuantity = purchaseQuantity.text.trim().toFloat(),
                            brands = brands.filterNot { it.isDefault }.map {
                                ShoppingListItem.Brand(name = it.name)
                            },
                            attributes = attributes.filterNot { it.name.isBlank() or it.value.isBlank() }
                                .map {
                                    Attribute(name = it.name, value = it.value, values = it.values)
                                },
                        ),
                    )
                }
            ) { Text(toolbarTitle.uppercase(KENYA)) }
        }
    }
}