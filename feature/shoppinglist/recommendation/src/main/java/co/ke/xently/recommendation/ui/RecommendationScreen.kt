package co.ke.xently.recommendation.ui

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.common.KENYA
import co.ke.xently.data.*
import co.ke.xently.feature.PermissionGranted
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.ui.*
import co.ke.xently.recommendation.R
import co.ke.xently.shoppinglist.ui.list.item.ShoppingListItemCard
import co.ke.xently.source.remote.DeferredRecommendation
import kotlin.time.Duration.Companion.seconds

internal const val TEST_TAG_RECOMMENDATION_BODY_CONTAINER = "TEST_TAG_RECOMMENDATION_BODY_CONTAINER"

internal data class RecommendationScreenFunction(
    internal val sharedFunction: SharedFunction = SharedFunction(),
    internal val onSuccess: (DeferredRecommendation) -> Unit = {},
    internal val onDetailSubmitted: (RecommendationRequest) -> Unit = {},
)

@Composable
internal fun RecommendationScreen(
    modifier: Modifier,
    args: RecommendationScreenArgs,
    function: RecommendationScreenFunction,
    viewModel: RecommendationViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val persistedShoppingListResult by viewModel.persistedShoppingListResult.collectAsState(
        initial = TaskResult.Success(emptyList()),
        context = scope.coroutineContext,
    )
    LaunchedEffect(args) {
        viewModel.setArgs(args)
    }

    val result by viewModel.result.collectAsState(
        initial = TaskResult.Success(null),
        context = scope.coroutineContext,
    )

    var shouldRequestPermission by remember {
        mutableStateOf(false)
    }

    val myUpdatedLocation = rememberMyUpdatedLocation(
        args = MyUpdatedLocationArgs(
            fastestRefreshInterval = 10.seconds,
            shouldRequestPermission = shouldRequestPermission,
            onLocationPermissionChanged = function.sharedFunction.onLocationPermissionChanged,
        ),
    )

    RecommendationScreen(
        modifier = modifier,
        result = result,
        myUpdatedLocation = myUpdatedLocation,
        persistedShoppingListResult = persistedShoppingListResult,
        function = function.copy(
            onDetailSubmitted = viewModel::recommend,
            sharedFunction = function.sharedFunction.copy(
                onLocationPermissionChanged = {
                    shouldRequestPermission = true
                }
            ),
        ),
    )
}

@SuppressLint("MutableCollectionMutableState")
@Composable
@VisibleForTesting
internal fun RecommendationScreen(
    modifier: Modifier,
    myUpdatedLocation: MyUpdatedLocation,
    function: RecommendationScreenFunction,
    result: TaskResult<DeferredRecommendation?>,
    persistedShoppingListResult: TaskResult<List<ShoppingListItem>>,
) {
    val unPersistedShoppingListSaver = listSaver<SnapshotStateList<String>, String>(
        save = { stateList ->
            stateList.map {
                it
            }
        },
        restore = {
            it.toMutableStateList()
        },
    )
    val unPersistedShoppingList by rememberSaveable(stateSaver = unPersistedShoppingListSaver) {
        mutableStateOf(mutableStateListOf())
    }
    val persistedShoppingList = remember(persistedShoppingListResult) {
        mutableStateListOf(*(persistedShoppingListResult.getOrNull() ?: emptyList()).toTypedArray())
    }
    var shouldPersist by rememberSaveable {
        mutableStateOf(true)
    }

    val scaffoldState = rememberScaffoldState()

    val (myLocation, isLocationPermissionGranted) = myUpdatedLocation

    val isMyLocationNull by remember(myLocation) {
        derivedStateOf {
            myLocation == null
        }
    }
    val shouldShowRequestingMyUpdatedLocationMessage by remember(
        isMyLocationNull,
        isLocationPermissionGranted,
    ) {
        derivedStateOf {
            isLocationPermissionGranted && isMyLocationNull
        }
    }
    if (!isLocationPermissionGranted) {
        val message = stringResource(R.string.location_permission_rationale_minified)
        val actionLabel = stringResource(R.string.grant_button_label)
        LaunchedEffect(isLocationPermissionGranted) {
            val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = SnackbarDuration.Indefinite,
            )
            when (snackbarResult) {
                SnackbarResult.Dismissed -> TODO()
                SnackbarResult.ActionPerformed -> {
                    function.sharedFunction.onLocationPermissionChanged(PermissionGranted(false))
                }
            }
        }
    }

    if (result is TaskResult.Error || persistedShoppingListResult is TaskResult.Error) {
        val errorMessage = result.errorMessage ?: persistedShoppingListResult.errorMessage
        ?: stringResource(R.string.generic_error_message)
        LaunchedEffect(result, persistedShoppingListResult) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Long,
            )
        }
    } else if (result is TaskResult.Success && result.data != null) {
        LaunchedEffect(result.data) {
            val deferredRecommendation = result.data!!.copy(
                numberOfItems = unPersistedShoppingList.size + persistedShoppingList.size,
            )
            function.onSuccess.invoke(deferredRecommendation)
        }
    }

    val isTaskLoading by remember(result, persistedShoppingListResult) {
        derivedStateOf {
            result is TaskResult.Loading || persistedShoppingListResult is TaskResult.Loading
        }
    }
    val isUnPersistedShoppingListNotEmpty by remember(unPersistedShoppingList) {
        derivedStateOf {
            unPersistedShoppingList.isNotEmpty()
        }
    }
    val isPersistedShoppingListNotEmpty by remember(persistedShoppingList) {
        derivedStateOf {
            persistedShoppingList.isNotEmpty()
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                showProgress = isTaskLoading,
                title = stringResource(R.string.fr_filter_toolbar_title),
                onNavigationIconClicked = function.sharedFunction.onNavigationIconClicked,
                subTitle = LocalContext.current.resources.getQuantityString(
                    R.plurals.fr_filter_toolbar_subtitle,
                    unPersistedShoppingList.size + persistedShoppingList.size,
                    unPersistedShoppingList.size + persistedShoppingList.size,
                ),
            )
        },
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues)) {
            val focusManager = LocalFocusManager.current
            var productName by remember {
                mutableStateOf(TextFieldValue(""))
            }
            TextInputLayout(
                value = productName,
                label = stringResource(R.string.fr_filter_product_name),
                modifier = VerticalLayoutModifier.padding(top = VIEW_SPACE),
                onValueChange = {
                    productName = it
                },
                keyboardOptions = DefaultKeyboardOptions.copy(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                ),
                trailingIcon = {
                    val description =
                        stringResource(R.string.fr_filter_add_product_name_content_description)
                    IconButton(
                        enabled = productName.text.isNotBlank(),
                        modifier = Modifier.semantics { testTag = description },
                        onClick = {
                            if (isUnPersistedShoppingListNotEmpty) {
                                unPersistedShoppingList.add(0, productName.text.trim())
                            } else {
                                unPersistedShoppingList.add(productName.text.trim())
                            }
                            productName = TextFieldValue("")
                        },
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = description)
                    }
                },
            )
            val checkBoxDescription =
                stringResource(R.string.fr_filter_should_persist_shopping_lists)
            val onCheckedChange: (Boolean) -> Unit = {
                shouldPersist = it
                focusManager.clearFocus()
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = VerticalLayoutModifier
                    .toggleable(
                        role = Role.Checkbox,
                        value = shouldPersist,
                        onValueChange = onCheckedChange,
                    )
                    .semantics {
                        testTag = checkBoxDescription
                    },
            ) {
                Checkbox(
                    checked = shouldPersist,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier.semantics {
                        contentDescription = checkBoxDescription
                    },
                )
                Text(text = checkBoxDescription)
            }
            val shouldEnableRecommendButton by remember(
                isTaskLoading,
                isMyLocationNull,
                isLocationPermissionGranted,
                isPersistedShoppingListNotEmpty,
                isUnPersistedShoppingListNotEmpty,
            ) {
                derivedStateOf {
                    isLocationPermissionGranted && !isMyLocationNull && !isTaskLoading &&
                            (isUnPersistedShoppingListNotEmpty || isPersistedShoppingListNotEmpty)
                }
            }
            Button(
                modifier = VerticalLayoutModifier,
                enabled = shouldEnableRecommendButton,
                onClick = {
                    focusManager.clearFocus()
                    val items = unPersistedShoppingList + persistedShoppingList
                    function.onDetailSubmitted.invoke(
                        RecommendationRequest(
                            items = items,
                            persist = shouldPersist,
                            cacheRecommendationsForLater = true,
                            isLocationPermissionGranted = isLocationPermissionGranted,
                            myLocation = myLocation?.let {
                                Coordinate(it.latitude, myLocation.longitude)
                            },
                        ),
                    )
                },
            ) {
                Text(
                    text = if (shouldShowRequestingMyUpdatedLocationMessage) {
                        stringResource(R.string.fr_initiating_location_tracking)
                    } else {
                        stringResource(R.string.fr_filter_recommend).uppercase(KENYA)
                    },
                )
            }
            LazyColumn(
                modifier = Modifier.semantics {
                    testTag = TEST_TAG_RECOMMENDATION_BODY_CONTAINER
                },
            ) {
                if (isUnPersistedShoppingListNotEmpty) {
                    item {
                        Text(
                            text = stringResource(R.string.fr_filter_un_persisted_list_subheading),
                            style = MaterialTheme.typography.h5,
                            modifier = VerticalLayoutModifier,
                        )
                    }
                }
                itemsIndexed(unPersistedShoppingList) { index, item ->
                    ListItemSurface(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier.wrapContentWidth(),
                        )
                        val description =
                            stringResource(R.string.fr_filter_remove_un_persisted_item, item)
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                unPersistedShoppingList.removeAt(index)
                            },
                            modifier = Modifier.semantics { testTag = description },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = description,
                            )
                        }
                    }
                }
                if (isPersistedShoppingListNotEmpty) {
                    item {
                        Text(
                            text = stringResource(R.string.fr_filter_persisted_list_subheading),
                            style = MaterialTheme.typography.h5,
                            modifier = VerticalLayoutModifier,
                        )
                    }
                }
                itemsIndexed(persistedShoppingList) { index, item: ShoppingListItem ->
                    val description =
                        stringResource(R.string.fr_filter_remove_persisted_item, item)
                    ShoppingListItemCard(
                        item = item,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    persistedShoppingList.removeAt(index)
                                },
                                modifier = Modifier.semantics { testTag = description },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = description,
                                )
                            }
                        },
                    ) {}
                }
            }
        }
    }
}
