package co.ke.xently.shoppinglist.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.common.DEFAULT_LOCAL_DATE_FORMAT
import co.ke.xently.common.KENYA
import co.ke.xently.common.localDefaultDateFormatToServerDate
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.data.TaskResult.Loading
import co.ke.xently.data.errorMessage
import co.ke.xently.data.getOrNull
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.feature.ui.rememberDatePickerDialog
import co.ke.xently.feature.ui.rememberFragmentManager
import co.ke.xently.feature.ui.stringRes
import co.ke.xently.shoppinglist.R
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import kotlinx.coroutines.launch


@Composable
internal fun ShoppingListItemScreen(
    id: Long?,
    modifier: Modifier = Modifier,
    viewModel: ShoppingListItemViewModel = hiltViewModel(),
    onNavigationIconClicked: () -> Unit = {},
) {
    var isUpdate = false
    id?.also {
        isUpdate = it != ShoppingListItem.DEFAULT_ID
        if (isUpdate) viewModel.get(it)
    }
    val coroutineScope = rememberCoroutineScope()
    val itemResult by viewModel.shoppingItemResult.collectAsState(
        coroutineScope.coroutineContext,
    )
    ShoppingListItemScreen(
        isUpdate,
        id,
        itemResult,
        modifier,
        onNavigationIconClicked,
        viewModel::add,
    )
}

@Composable
private fun ShoppingListItemScreen(
    isUpdate: Boolean,
    itemId: Long?,
    result: TaskResult<ShoppingListItem?>,
    modifier: Modifier,
    onNavigationIconClicked: () -> Unit,
    onAddShoppingListItemClicked: (ShoppingListItem) -> Unit,
) {
    val item = result.getOrNull() ?: ShoppingListItem()

    var name by remember(itemId, result, item) { mutableStateOf(TextFieldValue(item.name)) }
    var unit by remember(itemId, result, item) { mutableStateOf(TextFieldValue(item.unit)) }
    var unitQuantity by remember(itemId, result, item) {
        mutableStateOf(TextFieldValue(item.unitQuantity.toString()))
    }
    var purchaseQuantity by remember(itemId, result, item) {
        mutableStateOf(TextFieldValue(item.purchaseQuantity.toString()))
    }
    var dateAdded by remember(itemId, result, item) {
        mutableStateOf(TextFieldValue(DEFAULT_LOCAL_DATE_FORMAT.format(item.dateAdded)))
    }

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    if (result is TaskResult.Error) {
        val errorMessage =
            result.errorMessage ?: stringResource(R.string.generic_error_message)
        LaunchedEffect(itemId, result, errorMessage) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
    }

    val toolbarTitle = stringRes(
        R.string.fsl_detail_screen_toolbar_title,
        if (isUpdate) R.string.update else R.string.add,
    )
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(toolbarTitle, onNavigationIconClicked, result is Loading)
        },
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TextField(
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text(stringResource(R.string.fsl_text_field_label_name)) },
                    value = name,
                    onValueChange = { name = it })
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.fsl_text_field_label_unit)) },
                        singleLine = true,
                        value = unit,
                        onValueChange = { unit = it })
                    TextField(
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.fsl_text_field_label_unit_quantity)) },
                        singleLine = true,
                        value = unitQuantity,
                        onValueChange = { unitQuantity = it },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.fsl_text_field_label_purchase_quantity)) },
                        singleLine = true,
                        value = purchaseQuantity,
                        onValueChange = { purchaseQuantity = it },
                    )

                    val fragmentManager = rememberFragmentManager()
                    val datePicker = rememberDatePickerDialog(
                        R.string.fsl_text_field_label_date_added,
                        DEFAULT_LOCAL_DATE_FORMAT.parse(dateAdded.text),
                        CalendarConstraints.Builder()
                            .setValidator(DateValidatorPointForward.now()).build(),
                    ) { dateAdded = TextFieldValue(DEFAULT_LOCAL_DATE_FORMAT.format(it)) }

                    TextField(
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.fsl_text_field_label_date_added)) },
                        singleLine = true,
                        value = dateAdded,
                        onValueChange = { dateAdded = it },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                datePicker.show(fragmentManager, "ShoppingListDateAdded")
                            }) {
                                Icon(Icons.Default.DateRange, contentDescription = null)
                            }
                        },
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = arrayOf(name,
                        unit,
                        unitQuantity,
                        purchaseQuantity).all { it.text.isNotBlank() },
                    onClick = {
                        onAddShoppingListItemClicked(item.copy(
                            name = name.text,
                            unit = unit.text,
                            unitQuantity = unitQuantity.text.toFloatOrNull()
                                ?: TODO("Raise invalid error"),
                            purchaseQuantity = purchaseQuantity.text.toFloatOrNull()
                                ?: TODO("Raise invalid error"),
                            dateAdded = localDefaultDateFormatToServerDate(dateAdded.text)!!,
                        ))
                    },
                ) { Text(toolbarTitle.uppercase(KENYA)) }
            }
        }
    }
}