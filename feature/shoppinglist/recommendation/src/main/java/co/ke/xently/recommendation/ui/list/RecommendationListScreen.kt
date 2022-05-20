package co.ke.xently.recommendation.ui.list

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import co.ke.xently.feature.PermissionGranted
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.ui.*
import co.ke.xently.feature.utils.MAP_HEIGHT
import co.ke.xently.recommendation.R
import co.ke.xently.recommendation.ui.detail.RecommendationDetailScreen
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItem
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItemFunction
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItemMenuItem
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
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
private fun ConsiderFailure(
    modifier: Modifier = Modifier,
    function: RecommendationListScreenFunction,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ToolbarWithProgressbar(
            title = stringResource(R.string.fr_toolbar_title),
            onNavigationIconClicked = function.sharedFunction.onNavigationIconClicked,
        )
        content()
    }
}

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

@Composable
private fun GoogleMapView(
    modifier: Modifier,
    numberOfItems: Int,
    isMapMaximized: Boolean,
    recommendations: List<Recommendation>,
    onInfoWindowClick: (Recommendation) -> Unit,
    onMapMaximizedOrMinimized: (MapMaximized) -> Unit,
    onLocationPermissionChanged: (PermissionGranted) -> Unit,
) {
    GoogleMapViewWithLoadingIndicator(
        modifier = modifier,
        isMapMaximized = isMapMaximized,
        onMapMaximizedOrMinimized = onMapMaximizedOrMinimized,
        onLocationPermissionChanged = onLocationPermissionChanged,
    ) {
        val recommendationsWithCoordinates = remember(recommendations) {
            recommendations.filter { recommendation ->
                recommendation.shop.coordinate != null
            }
        }
        for (recommendation in recommendationsWithCoordinates) {
            val markerState = rememberMarkerState(
                position = LatLng(
                    recommendation.shop.coordinate!!.lat,
                    recommendation.shop.coordinate!!.lon,
                ),
            )
            Marker(
                state = markerState,
                title = recommendation.shop.descriptiveName,
                snippet = LocalContext.current.resources.getQuantityString(
                    R.plurals.fr_recommendation_item,
                    numberOfItems,
                    recommendation.hit.count,
                    numberOfItems,
                    NumberFormat.getCurrencyInstance().apply {
                        currency = Currency.getInstance(KENYA)
                    }.format(recommendation.expenditure.total),
                ),
                onInfoWindowClick = {
                    onInfoWindowClick.invoke(recommendation)
                },
            )
        }
    }
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
    val recommendations: List<Recommendation>? = result.getOrNull()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            if (recommendation != null) {
                TopAppBar(
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
                        val contentDescription = stringResource(R.string.hide)
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (sheetState.isVisible) {
                                        sheetState.hide()
                                    }
                                }
                            },
                            modifier = Modifier.semantics {
                                testTag = contentDescription
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = contentDescription,
                            )
                        }
                    },
                )
                RecommendationDetailScreen(
                    recommendation = recommendation!!,
                    modifier = Modifier.padding(PaddingValues(horizontal = VIEW_SPACE)),
                )
            } else {
                Box(modifier = Modifier.height(1.dp))
            }
        },
    ) {
        when (result) {
            is TaskResult.Error -> {
                ConsiderFailure(function = function) {
                    FullscreenError(
                        error = result.error,
                        modifier = modifier,
                        click = HttpErrorButtonClick(retryAble = function.onRetryClicked),
                    )
                }
            }
            TaskResult -> {
                ConsiderFailure(function = function) {
                    FullscreenLoading<Recommendation>(modifier = modifier)
                }
            }
            is TaskResult.Success -> {
                val context = LocalContext.current
                if (recommendations!!.isEmpty()) {
                    ConsiderFailure(function = function) {
                        FullscreenEmptyList<Recommendation>(
                            modifier = modifier,
                            error = context.resources.getQuantityString(
                                R.plurals.fr_empty_recommendation_list,
                                numberOfItems,
                                numberOfItems,
                            ),
                        )
                    }
                } else {
                    var isMapMaximized by rememberSaveable {
                        mutableStateOf(MapMaximized().value)
                    }
                    val onItemClick: (Recommendation) -> Unit = {
                        recommendation = it
                        coroutineScope.launch {
                            if (sheetState.isVisible) {
                                sheetState.hide()
                            } else {
                                sheetState.show()
                            }
                        }
                    }
                    val mapWithToolbar: @Composable (
                        boxModifier: Modifier,
                        mapModifier: Modifier,
                    ) -> Unit = { boxModifier, mapModifier ->
                        Box(modifier = boxModifier) {
                            if (showMap) {
                                GoogleMapView(
                                    modifier = mapModifier,
                                    numberOfItems = numberOfItems,
                                    onInfoWindowClick = onItemClick,
                                    isMapMaximized = isMapMaximized,
                                    recommendations = recommendations,
                                    onLocationPermissionChanged = function.sharedFunction.onLocationPermissionChanged,
                                    onMapMaximizedOrMinimized = { mapMaximized ->
                                        isMapMaximized = mapMaximized.value
                                    },
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
                                    numberOfItems,
                                ),
                            )
                        }
                    }
                    if (isMapMaximized) {
                        mapWithToolbar(boxModifier = modifier, mapModifier = modifier)
                    } else {
                        LazyColumn(
                            modifier = modifier,
                            verticalArrangement = Arrangement.spacedBy(VIEW_SPACE_HALVED),
                        ) {
                            item {
                                mapWithToolbar(
                                    boxModifier = Modifier
                                        .height(IntrinsicSize.Min)
                                        .fillMaxWidth(),
                                    mapModifier = Modifier
                                        .height(MAP_HEIGHT)
                                        .fillMaxWidth(),
                                )
                            }
                            itemsIndexed(
                                recommendations,
                                key = { _, r -> r.shop.id },
                            ) { index, _recommendation ->
                                RecommendationCardItem(
                                    modifier = Modifier.semantics {
                                        testTag = context.getString(
                                            R.string.fr_recommendation_card_test_tag,
                                            index,
                                        )
                                    },
                                    recommendation = _recommendation,
                                    function = function.function.copy(
                                        onItemClicked = onItemClick,
                                    ),
                                    menuItems = listOf(
                                        RecommendationCardItemMenuItem(
                                            label = R.string.fr_directions,
                                            onClick = {

                                            },
                                        ),
                                        RecommendationCardItemMenuItem(
                                            label = R.string.fr_details,
                                            onClick = onItemClick,
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
}
