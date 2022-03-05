package co.ke.xently.shoppinglist.ui.detail

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.*
import co.ke.xently.data.ShoppingListItem.Attribute
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.feature.ui.VIEW_SPACE
import co.ke.xently.feature.ui.VerticalLayoutModifier
import co.ke.xently.feature.ui.stringRes
import co.ke.xently.products.shared.*
import co.ke.xently.shoppinglist.R

internal data class ShoppingListItemScreenFunction(
    val navigationIcon: () -> Unit = {},
    val measurementUnitQueryChanged: (String) -> Unit = {},
    val brandQueryChanged: (String) -> Unit = {},
    val attributeQueryChanged: (AttributeQuery) -> Unit = {},
    val detailsSubmitted: (ShoppingListItem) -> Unit = {},
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
        context = scope.coroutineContext,
    )
    val brands by viewModel.brandsResult.collectAsState(
        context = scope.coroutineContext,
    )
    val attributes by viewModel.attributesResult.collectAsState(
        context = scope.coroutineContext
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
        attributeSuggestions = attributes,
        measurementUnits = measurementUnits,
        function = function.copy(
            detailsSubmitted = viewModel::addOrUpdate,
            brandQueryChanged = viewModel::setBrandQuery,
            attributeQueryChanged = viewModel::setAttributeQuery,
            measurementUnitQueryChanged = viewModel::setMeasurementUnitQuery,
        ),
    )
}


@Composable
private fun ShoppingListItemScreen(
    modifier: Modifier,
    result: TaskResult<ShoppingListItem?>,
    addResult: TaskResult<ShoppingListItem?>,
    permitReAddition: Boolean = false,
    brandSuggestions: List<Product.Brand> = emptyList(),
    attributeSuggestions: List<Product.Attribute> = emptyList(),
    measurementUnits: List<MeasurementUnit> = emptyList(),
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
                onNavigationIconClicked = function.navigationIcon,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .verticalScroll(scrollState),
        ) {
            Spacer(modifier = Modifier.padding(top = VIEW_SPACE))
            val name = productNameTextField(
                name = item.name,
                error = nameError,
                clearField = permitReAddition,
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            val unit = measurementUnitTextField(
                unit = item.unit,
                error = unitError,
                clearField = permitReAddition,
                suggestions = measurementUnits,
                onQueryChanged = function.measurementUnitQueryChanged,
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            val unitQuantity = numberTextField(
                number = item.unitQuantity,
                error = unitQuantityError,
                clearField = permitReAddition,
                label = R.string.fsp_product_detail_unit_quantity_label,
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            val purchaseQuantity = numberTextField(
                number = item.purchaseQuantity,
                error = purchaseQuantityError,
                clearField = permitReAddition,
                label = R.string.fsl_text_field_label_purchase_quantity,
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            val brands = productBrandsView(
                clearFields = permitReAddition,
                suggestions = brandSuggestions,
                onQueryChanged = function.brandQueryChanged,
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            val attributes = productAttributesView(
                clearFields = permitReAddition,
                suggestions = attributeSuggestions,
                onQueryChanged = function.attributeQueryChanged,
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
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
                    function.detailsSubmitted.invoke(
                        item.copy(
                            name = name.text,
                            unit = unit.text,
                            unitQuantity = unitQuantity.text.toFloat(),
                            purchaseQuantity = purchaseQuantity.text.toFloat(),
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
            ) { Text(toolbarTitle.uppercase()) }
        }
    }
}