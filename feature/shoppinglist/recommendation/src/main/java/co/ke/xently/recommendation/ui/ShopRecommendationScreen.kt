package co.ke.xently.recommendation.ui

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.common.KENYA
import co.ke.xently.data.*
import co.ke.xently.feature.ui.*
import co.ke.xently.recommendation.R
import co.ke.xently.shoppinglist.ui.list.item.ShoppingListItemCard
import co.ke.xently.source.remote.DeferredRecommendation

internal data class ShopRecommendationScreenFunction(
    val onNavigationClick: () -> Unit = {},
    val onDetailSubmitted: (RecommendationRequest) -> Unit = {},
)

@Composable
internal fun ShopRecommendationScreen(
    modifier: Modifier,
    args: ShopRecommendationScreenArgs,
    function: ShopRecommendationScreenFunction,
    viewModel: ShopRecommendationViewModel = hiltViewModel(),
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

    ShopRecommendationScreen(
        modifier = modifier,
        result = result,
        persistedShoppingListResult = persistedShoppingListResult,
        function = function.copy(
            onDetailSubmitted = viewModel::recommend,
        ),
    )
}

@Composable
@VisibleForTesting
internal fun ShopRecommendationScreen(
    modifier: Modifier,
    function: ShopRecommendationScreenFunction,
    result: TaskResult<DeferredRecommendation?>,
    persistedShoppingListResult: TaskResult<List<ShoppingListItem>>,
) {
    val unPersistedShoppingList = remember {
        mutableStateListOf<String>()
    }
    val persistedShoppingList = remember(persistedShoppingListResult) {
        mutableStateListOf(*(persistedShoppingListResult.getOrNull() ?: emptyList()).toTypedArray())
    }
    var shouldPersist by remember {
        mutableStateOf(true)
    }

    val scaffoldState = rememberScaffoldState()

    if (result is TaskResult.Error || persistedShoppingListResult is TaskResult.Error) {
        val errorMessage = result.errorMessage ?: persistedShoppingListResult.errorMessage
        ?: stringResource(R.string.generic_error_message)
        LaunchedEffect(result, persistedShoppingListResult) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Long,
            )
        }
    }

    val toolbarTitle = stringResource(R.string.fr_filter_toolbar_title)
    val isTaskLoading =
        result is TaskResult.Loading || persistedShoppingListResult is TaskResult.Loading
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                title = toolbarTitle,
                showProgress = isTaskLoading,
                onNavigationIconClicked = function.onNavigationClick,
            )
        },
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues)) {
            val focusManager = LocalFocusManager.current
            var productName by remember {
                mutableStateOf(TextFieldValue(""))
            }
            Row(
                modifier = VerticalLayoutModifier.padding(top = VIEW_SPACE),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(VIEW_SPACE_HALVED),
            ) {
                TextInputLayout(
                    value = productName,
                    label = stringResource(R.string.fr_filter_product_name),
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        productName = it
                    },
                    trailingIcon = {
                        val description =
                            stringResource(R.string.fr_filter_add_product_name_content_description)
                        IconButton(
                            enabled = productName.text.isNotBlank(),
                            modifier = Modifier.semantics { testTag = description },
                            onClick = {
                                if (unPersistedShoppingList.isNotEmpty()) {
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
                TextButton(
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colors.primary,
                    ),
                    modifier = Modifier
                        .height(IntrinsicSize.Max)
                        .weight(1f),
                    enabled = !isTaskLoading && (unPersistedShoppingList.isNotEmpty() || persistedShoppingList.isNotEmpty()),
                    onClick = {
                        focusManager.clearFocus()
                        val items = unPersistedShoppingList + persistedShoppingList
                        function.onDetailSubmitted.invoke(
                            RecommendationRequest(
                                items = items,
                                persist = shouldPersist,
                                cacheRecommendationsForLater = true,
                            ),
                        )
                    },
                ) {
                    Text(text = stringResource(R.string.fr_filter_recommend).uppercase(KENYA))
                }
            }
            Row(modifier = VerticalLayoutModifier, verticalAlignment = Alignment.CenterVertically) {
                val description = stringResource(R.string.fr_filter_should_persist_shopping_lists)
                Checkbox(
                    checked = shouldPersist,
                    onCheckedChange = {
                        shouldPersist = it
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.semantics {
                        contentDescription = description
                    },
                )
                Text(text = description)
            }
            LazyColumn {
                if (unPersistedShoppingList.isNotEmpty()) {
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
                            stringResource(R.string.fr_filter_remove_unpersisted_item, item)
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
                if (persistedShoppingList.isNotEmpty()) {
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
