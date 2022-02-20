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
import co.ke.xently.data.TaskResult.Loading
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.feature.ui.VerticalLayoutModifier
import co.ke.xently.feature.ui.stringRes
import co.ke.xently.products.shared.*
import co.ke.xently.shoppinglist.R


@Composable
internal fun ShoppingListItemScreen(
    id: Long?,
    modifier: Modifier = Modifier,
    viewModel: ShoppingListItemViewModel = hiltViewModel(),
    onNavigationIconClicked: () -> Unit = {},
) {
    val isDefault by remember(id) {
        mutableStateOf(id == null || id == Product.default().id)
    }

    val fetch by rememberUpdatedState { viewModel.get(id!!) }
    LaunchedEffect(true) {
        if (!isDefault) fetch()
    }

    val scope = rememberCoroutineScope()
    val result by viewModel.result.collectAsState(scope.coroutineContext)
    // TODO: Fix case where searching on either measurement units or shops clears fields
    val measurementUnits by viewModel.measurementUnitsResult.collectAsState(scope.coroutineContext)
    val brands by viewModel.brandsResult.collectAsState(scope.coroutineContext)
    val attributes by viewModel.attributesResult.collectAsState(scope.coroutineContext)

    // Allow addition of more items if the screen was initially for adding.
    val permitReAddition = isDefault && result.getOrNull() != null

    ShoppingListItemScreen(
        modifier,
        if (permitReAddition) {
            TaskResult.Success(null)
        } else {
            result
        },
        permitReAddition,
        brands,
        attributes,
        measurementUnits,
        onNavigationIconClicked,
        viewModel::setMeasurementUnitQuery,
        viewModel::setBrandQuery,
        viewModel::setAttributeQuery,
        viewModel::addOrUpdate,
    )
}


@Composable
private fun ShoppingListItemScreen(
    modifier: Modifier,
    result: TaskResult<ShoppingListItem?>,
    permitReAddition: Boolean = false,
    brandSuggestions: List<Product.Brand> = emptyList(),
    attributeSuggestions: List<Product.Attribute> = emptyList(),
    measurementUnits: List<MeasurementUnit> = emptyList(),
    onNavigationIconClicked: () -> Unit = {},
    onMeasurementUnitQueryChanged: (String) -> Unit = {},
    onBrandQueryChanged: (String) -> Unit = {},
    onAttributeQueryChanged: (AttributeQuery) -> Unit = {},
    onDetailsSubmitted: (ShoppingListItem) -> Unit = {},
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

    if (result is TaskResult.Error) {
        val exception = result.error as? ShoppingListItemHttpException
        nameError = exception.error.name
        unitError = exception.error.unit
        unitQuantityError = exception.error.unitQuantity
        purchaseQuantityError = exception.error.purchaseQuantity

        if (exception?.hasFieldErrors() != true) {
            val message = result.errorMessage ?: stringResource(R.string.generic_error_message)
            LaunchedEffect(result, message) {
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

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(toolbarTitle,
                onNavigationIconClicked,
                showProgress = result is Loading)
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .verticalScroll(scrollState),
        ) {
            Spacer(modifier = Modifier.padding(top = 16.dp))
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
                onQueryChanged = onMeasurementUnitQueryChanged,
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
                onQueryChanged = onBrandQueryChanged,
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            val attributes = productAttributesView(
                clearFields = permitReAddition,
                suggestions = attributeSuggestions,
                onQueryChanged = onAttributeQueryChanged,
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Button(
                enabled = arrayOf(
                    unit,
                    name,
                    unitQuantity,
                    purchaseQuantity,
                ).all { it.text.isNotBlank() },
                modifier = VerticalLayoutModifier.padding(bottom = 16.dp),
                onClick = {
                    focusManager.clearFocus()
                    onDetailsSubmitted(item.copy(
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
                    ))
                }
            ) { Text(toolbarTitle.uppercase()) }
        }
    }
}