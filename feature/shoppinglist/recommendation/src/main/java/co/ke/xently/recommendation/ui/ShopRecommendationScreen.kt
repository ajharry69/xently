package co.ke.xently.recommendation.ui

import androidx.annotation.VisibleForTesting
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.common.KENYA
import co.ke.xently.data.RecommendationRequest
import co.ke.xently.feature.ui.*
import co.ke.xently.recommendation.R

internal data class ShopRecommendationScreenFunction(
    val onNavigationClick: () -> Unit = {},
    val onDetailSubmitted: (RecommendationRequest) -> Unit = {},
)

@Composable
internal fun ShopRecommendationScreen(
    modifier: Modifier,
    function: ShopRecommendationScreenFunction,
    viewModel: ShopRecommendationViewModel = hiltViewModel(),
) {
    ShopRecommendationScreen(
        modifier = modifier,
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
) {
    val unPersistedShoppingList = remember {
        mutableStateListOf<String>()
    }
    var shouldPersist by remember {
        mutableStateOf(true)
    }

    val toolbarTitle = stringResource(R.string.fr_filter_toolbar_title)
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                title = toolbarTitle,
                onNavigationIconClicked = function.onNavigationClick,
            )
        },
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues)) {
            var productName by remember {
                mutableStateOf(TextFieldValue(""))
            }
            Row(
                modifier = VerticalLayoutModifier.padding(top = VIEW_SPACE),
                verticalAlignment = Alignment.CenterVertically
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
                                if (unPersistedShoppingList.size > 0) {
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
                Spacer(modifier = Modifier.width(VIEW_SPACE_HALVED))
                Button(
                    modifier = Modifier.height(IntrinsicSize.Max),
                    enabled = unPersistedShoppingList.isNotEmpty(),
                    onClick = {
                        function.onDetailSubmitted.invoke(
                            RecommendationRequest(
                                persist = shouldPersist,
                                items = unPersistedShoppingList,
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
                    },
                    modifier = Modifier.semantics {
                        contentDescription = description
                    },
                )
                Text(text = description)
            }
            LazyColumn {
                item {
                    Text(
                        text = stringResource(R.string.fr_filter_un_persisted_list_subheading),
                        style = MaterialTheme.typography.h5,
                        modifier = VerticalLayoutModifier,
                    )
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
                            onClick = { unPersistedShoppingList.removeAt(index) },
                            modifier = Modifier.semantics { testTag = description },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = description,
                            )
                        }
                    }
                }
            }
        }
    }
}
