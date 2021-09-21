package co.ke.xently.shoppinglist.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.NavHostController
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.shoppinglist.R
import kotlinx.coroutines.launch
import okhttp3.internal.http.toHttpDateOrNull


@Composable
fun ShoppingListDetail(
    itemId: Long?,
    modifier: Modifier = Modifier,
    viewModel: ShoppingListViewModel,
    navController: NavHostController,
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val scrollableState = rememberScrollableState { it }

    viewModel.getShoppingListItem(itemId)
    val itemResult by viewModel.shoppingItemResult.collectAsState(
        Result.success(null),
        coroutineScope.coroutineContext,
    )
    val item = itemResult.getOrNull() ?: ShoppingListItem()

    var name by remember(item) { mutableStateOf(TextFieldValue(item.name)) }
    var unit by remember(item) { mutableStateOf(TextFieldValue(item.unit)) }
    var unitQuantity by remember(item) { mutableStateOf(TextFieldValue(item.unitQuantity.toString())) }
    var purchaseQuantity by remember(item) { mutableStateOf(TextFieldValue(item.purchaseQuantity.toString())) }
    var dateAdded by remember(item) { mutableStateOf(TextFieldValue(item.dateAdded.toString())) }

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            R.string.fsl_detail_screen_toolbar_title,
                            stringResource(if (itemId == null) R.string.fsl_add else R.string.fsl_update),
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.fsl_menu_navigation_icon_content_desc_back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .scrollable(state = scrollableState, orientation = Orientation.Vertical),
        ) {
            if (itemResult.isFailure) {
                val errorMessage = itemResult.exceptionOrNull()?.localizedMessage ?: stringResource(
                    id = R.string.fsl_generic_error_message
                )
                LaunchedEffect(itemId, itemResult, errorMessage) {
                    coroutineScope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(errorMessage)
                    }
                }
            } else if (itemResult.isSuccess && itemResult.getOrThrow() == null) {
                LinearProgressIndicator()
            }
            TextField(value = name, onValueChange = {
                name = it
            })
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                TextField(value = unit, onValueChange = {
                    unit = it
                })
                TextField(value = unitQuantity, onValueChange = {
                    unitQuantity = it
                })
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                TextField(value = purchaseQuantity, onValueChange = {
                    purchaseQuantity = it
                })
                TextField(value = dateAdded, onValueChange = {
                    dateAdded = it
                })
            }
            Button(
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
            ) {
                Text(text = stringResource(R.string.fsl_add_item_text))
            }
        }
    }
}