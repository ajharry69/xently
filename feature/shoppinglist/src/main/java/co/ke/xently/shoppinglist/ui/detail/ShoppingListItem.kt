package co.ke.xently.shoppinglist.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.common.DEFAULT_LOCAL_DATE_FORMAT
import co.ke.xently.common.KENYA
import co.ke.xently.common.localDefaultDateFormatToServerDate
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.data.errorMessage
import co.ke.xently.data.getOrNull
import co.ke.xently.shoppinglist.R
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.util.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit


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
    ) {
        viewModel.add(it)
    }
}

@Composable
private fun ShoppingListItemScreen(
    isUpdate: Boolean,
    itemId: Long?,
    itemResult: TaskResult<ShoppingListItem?>,
    modifier: Modifier,
    onNavigationIconClicked: () -> Unit,
    onAddShoppingListItemClicked: (ShoppingListItem) -> Unit,
) {
    val item = itemResult.getOrNull() ?: ShoppingListItem()

    var name by remember(itemId, itemResult, item) { mutableStateOf(TextFieldValue(item.name)) }
    var unit by remember(itemId, itemResult, item) { mutableStateOf(TextFieldValue(item.unit)) }
    var unitQuantity by remember(itemId, itemResult, item) {
        mutableStateOf(TextFieldValue(item.unitQuantity.toString()))
    }
    var purchaseQuantity by remember(itemId, itemResult, item) {
        mutableStateOf(TextFieldValue(item.purchaseQuantity.toString()))
    }
    var dateAdded by remember(itemId, itemResult, item) {
        mutableStateOf(TextFieldValue(DEFAULT_LOCAL_DATE_FORMAT.format(item.dateAdded)))
    }

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    if (itemResult is TaskResult.Error) {
        val errorMessage =
            itemResult.errorMessage ?: stringResource(R.string.fsl_generic_error_message)
        LaunchedEffect(itemId, itemResult, errorMessage) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
    }

    val toolbarTitle = stringResource(
        R.string.fsl_detail_screen_toolbar_title,
        stringResource(if (isUpdate) R.string.fsl_update else R.string.fsl_add),
    )
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(text = toolbarTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigationIconClicked) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.fsl_navigation_icon_content_description),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            if (itemResult is TaskResult.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

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

                    val datePicker = MaterialDatePicker.Builder.datePicker()
                        .setSelection((DEFAULT_LOCAL_DATE_FORMAT.parse(dateAdded.text)?.time ?: Date().time) + 24.hours.toLong(DurationUnit.MILLISECONDS))
                        .setCalendarConstraints(CalendarConstraints.Builder()
                            .setValidator(DateValidatorPointForward.now()).build())
                        .setTitleText(R.string.fsl_text_field_label_date_added)
                        .build()
                    datePicker.addOnPositiveButtonClickListener {
                        dateAdded = TextFieldValue(DEFAULT_LOCAL_DATE_FORMAT.format(Date(it)))
                    }
                    val context = LocalContext.current
                    TextField(
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.fsl_text_field_label_date_added)) },
                        singleLine = true,
                        value = dateAdded,
                        onValueChange = { dateAdded = it },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                datePicker.show((context as FragmentActivity).supportFragmentManager,
                                    "ShoppingListDateAdded")
                            }) {
                                Icon(Icons.Default.DateRange, contentDescription = null)
                            }
                        },
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled= arrayOf(name, unit, unitQuantity, purchaseQuantity).all { it.text.isNotBlank() },
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
                ) { Text(text = toolbarTitle.uppercase(KENYA)) }
            }
        }
    }
}