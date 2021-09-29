package co.ke.xently.shoppinglist.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.shoppinglist.R
import kotlinx.coroutines.launch
import okhttp3.internal.http.toHttpDateOrNull


@Composable
fun ShoppingListDetail(
    itemId: Long?,
    modifier: Modifier = Modifier,
    viewModel: ShoppingListItemViewModel = hiltViewModel(),
    onNavigationIconClicked: (() -> Unit) = {},
) {
    val scrollState = rememberScrollState()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    viewModel.getShoppingListItem(itemId)
    val itemResult by viewModel.shoppingItemResult.collectAsState(
        Result.success(null),
        coroutineScope.coroutineContext,
    )
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
        mutableStateOf(TextFieldValue(item.dateAdded.toString()))
    }

    val toolbarTitle = stringResource(
        R.string.fsl_detail_screen_toolbar_title,
        stringResource(if (itemId == null) R.string.fsl_add else R.string.fsl_update),
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
                            contentDescription = stringResource(R.string.fsl_menu_navigation_icon_content_desc_back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            if (itemResult.isFailure) {
                val errorMessage =
                    itemResult.exceptionOrNull()?.localizedMessage ?: stringResource(
                        id = R.string.fsl_generic_error_message
                    )
                LaunchedEffect(itemId, itemResult, errorMessage) {
                    coroutineScope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(errorMessage)
                    }
                }
            } else if (itemResult.isSuccess && itemResult.getOrThrow() == null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
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
                        onValueChange = { unitQuantity = it })
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
                        onValueChange = { purchaseQuantity = it })
                    TextField(
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.fsl_text_field_label_date_added)) },
                        singleLine = true,
                        value = dateAdded,
                        onValueChange = { dateAdded = it })
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.addShoppingListItem(
                            ShoppingListItem(
                                id = -1L,
                                name = name.text,
                                unit = unit.text,
                                unitQuantity = unitQuantity.text.toFloatOrNull()
                                    ?: TODO("Raise invalid error"),
                                purchaseQuantity = purchaseQuantity.text.toFloatOrNull()
                                    ?: TODO("Raise invalid error"),
                                dateAdded = dateAdded.text.toHttpDateOrNull()
                                    ?: TODO("Raise invalid error"),
                            )
                        )
                    },
                ) { Text(text = toolbarTitle.uppercase()) }
            }
        }
    }
}