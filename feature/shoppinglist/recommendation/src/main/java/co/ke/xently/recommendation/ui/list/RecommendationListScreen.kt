package co.ke.xently.recommendation.ui.list

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.common.KENYA
import co.ke.xently.data.Recommendation
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.data.getOrNull
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.ui.*
import co.ke.xently.feature.utils.MAP_HEIGHT
import co.ke.xently.recommendation.R
import co.ke.xently.recommendation.ui.detail.RecommendationDetailScreen
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItem
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItemFunction
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItemMenuItem
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

internal data class RecommendationListScreenFunction(
    internal val onRetryClicked: (Throwable) -> Unit = {},
    internal val onItemClicked: (ShoppingListItem) -> Unit = {},
    internal val sharedFunction: SharedFunction = SharedFunction(),
    internal val function: RecommendationCardItemFunction = RecommendationCardItemFunction(),
)

internal data class RecommendationListScreenArgs(
    internal val lookupId: String,
    internal val numberOfItems: Int,
)

@Composable
internal fun RecommendationListScreen(
    modifier: Modifier,
    args: RecommendationListScreenArgs,
    function: RecommendationListScreenFunction,
    viewModel: RecommendationListViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val result by viewModel.result.collectAsState(
        context = scope.coroutineContext,
        initial = TaskResult.Success(emptyList()),
    )

    LaunchedEffect(args.lookupId) {
        viewModel.recommend(args.lookupId)
    }

    RecommendationListScreen(
        result = result,
        modifier = modifier,
        numberOfItems = args.numberOfItems,
        function = function.copy(
            onRetryClicked = {
                viewModel.recommend(args.lookupId)
            },
        ),
    )
}

private fun Recommendation.createMarkerOption(context: Context) = MarkerOptions().apply {
    title(shop.descriptiveName)
    val subtitle = context.resources.getQuantityString(
        R.plurals.fr_recommendation_item,
        numberOfItems,
        hit.count,
        numberOfItems,
        NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(KENYA)
        }.format(expenditure.total),
    )
    snippet(subtitle)
    position(LatLng(shop.coordinate!!.lat, shop.coordinate!!.lon))
}

@Composable
@VisibleForTesting
internal fun RecommendationListScreen(
    modifier: Modifier,
    numberOfItems: Int,
    result: TaskResult<List<Recommendation>>,
    function: RecommendationListScreenFunction,
    showMap: Boolean = true,
) {
    var recommendation by remember {
        mutableStateOf<Recommendation?>(null)
    }
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val recommendations: List<Recommendation>? = result.getOrNull()
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            if (recommendations.isNullOrEmpty()) {
                ToolbarWithProgressbar(
                    title = stringResource(R.string.fr_toolbar_title),
                    onNavigationIconClicked = function.sharedFunction.onNavigationIconClicked,
                )
            }
        },
        sheetContent = {
            if (recommendation != null) {
                TopAppBar(
                    elevation = 1.dp,
                    title = {
                        val title = stringResource(R.string.fr_shop_details)
                        Column {
                            Text(title, style = MaterialTheme.typography.body1)
                            Text(
                                recommendation!!.shop.descriptiveName,
                                style = MaterialTheme.typography.caption,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    scaffoldState.bottomSheetState.apply {
                                        if (!isCollapsed && isExpanded) {
                                            collapse()
                                        }
                                    }
                                }
                            },
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.hide),
                            )
                        }
                    },
                )
                RecommendationDetailScreen(
                    recommendation = recommendation!!,
                    modifier = Modifier.padding(PaddingValues(horizontal = VIEW_SPACE)),
                )
            }
        },
        sheetPeekHeight = 0.dp,
    ) { paddingValues ->
        when (result) {
            is TaskResult.Error -> {
                FullscreenError(
                    error = result.error,
                    modifier = modifier.padding(paddingValues),
                    click = HttpErrorButtonClick(retryAble = function.onRetryClicked),
                )
            }
            TaskResult -> {
                FullscreenLoading<Recommendation>(modifier = modifier.padding(paddingValues))
            }
            is TaskResult.Success -> {
                if (recommendations!!.isEmpty()) {
                    FullscreenEmptyList<Recommendation>(
                        modifier = modifier.padding(paddingValues),
                        error = LocalContext.current.resources.getQuantityString(
                            R.plurals.fr_empty_recommendation_list,
                            numberOfItems,
                            numberOfItems,
                        ),
                    )
                } else {
                    LazyColumn(
                        modifier = modifier.padding(paddingValues),
                        verticalArrangement = Arrangement.spacedBy(VIEW_SPACE_HALVED),
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .height(IntrinsicSize.Min)
                                    .fillMaxWidth(),
                            ) {
                                val context = LocalContext.current
                                if (showMap) {
                                    GoogleMapView(
                                        modifier = Modifier
                                            .height(MAP_HEIGHT)
                                            .fillMaxWidth(),
                                        markerPositions = recommendations.filter { recommendation ->
                                            recommendation.shop.coordinate != null
                                        }.map { recommendation ->
                                            recommendation.createMarkerOption(context)
                                        },
                                        onLocationPermissionChanged = function.sharedFunction.onLocationPermissionChanged,
                                    )
                                }
                                ToolbarWithProgressbar(
                                    elevation = 0.dp,
                                    backgroundColor = Color.Transparent,
                                    title = stringResource(R.string.fr_toolbar_title),
                                    onNavigationIconClicked = function.sharedFunction.onNavigationIconClicked,
                                    subTitle = context.resources.getQuantityString(
                                        R.plurals.fr_filter_toolbar_subtitle,
                                        numberOfItems,
                                        numberOfItems
                                    ),
                                )
                            }
                        }
                        itemsIndexed(recommendations) { index, _recommendation ->
                            val recommendationTestTag = stringResource(
                                R.string.fr_recommendation_card_test_tag,
                                index,
                            )
                            RecommendationCardItem(
                                function = function.function,
                                recommendation = _recommendation,
                                modifier = Modifier.semantics {
                                    testTag = recommendationTestTag
                                },
                                menuItems = listOf(
                                    RecommendationCardItemMenuItem(
                                        label = R.string.fr_details,
                                        onClick = {
                                            recommendation = it
                                            coroutineScope.launch {
                                                scaffoldState.bottomSheetState.apply {
                                                    if (isCollapsed && !isExpanded) {
                                                        expand()
                                                    }
                                                }
                                            }
                                        },
                                    ),
                                    RecommendationCardItemMenuItem(
                                        label = R.string.fr_directions,
                                        onClick = {

                                        },
                                    ),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}