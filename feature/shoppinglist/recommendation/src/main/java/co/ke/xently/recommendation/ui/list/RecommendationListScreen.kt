package co.ke.xently.recommendation.ui.list

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
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
import co.ke.xently.feature.ui.*
import co.ke.xently.feature.utils.MAP_HEIGHT
import co.ke.xently.recommendation.R
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItem
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItemFunction
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItemMenuItem
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import java.text.NumberFormat
import java.util.*

internal data class RecommendationListScreenFunction(
    internal val onNavigationIconClicked: () -> Unit = {},
    internal val onRetryClicked: (Throwable) -> Unit = {},
    internal val onItemClicked: (ShoppingListItem) -> Unit = {},
    internal val onLocationPermissionChanged: (Boolean) -> Unit = {},
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
    menuItems: List<RecommendationCardItemMenuItem>,
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
        menuItems = menuItems,
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
    menuItems: List<RecommendationCardItemMenuItem>,
    showMap: Boolean = true,
) {
    val scaffoldState = rememberScaffoldState()
    val recommendations: List<Recommendation>? = result.getOrNull()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            if (recommendations.isNullOrEmpty()) {
                ToolbarWithProgressbar(
                    title = stringResource(R.string.fr_toolbar_title),
                    onNavigationIconClicked = function.onNavigationIconClicked,
                )
            }
        }
    ) {
        when (result) {
            is TaskResult.Error -> {
                FullscreenError(
                    error = result.error,
                    modifier = modifier.padding(it),
                    click = HttpErrorButtonClick(retryAble = function.onRetryClicked),
                )
            }
            TaskResult -> {
                FullscreenLoading<Recommendation>(modifier = modifier.padding(it))
            }
            is TaskResult.Success -> {
                if (recommendations!!.isEmpty()) {
                    FullscreenEmptyList<Recommendation>(
                        modifier = modifier.padding(it),
                        error = LocalContext.current.resources.getQuantityString(
                            R.plurals.fr_empty_recommendation_list,
                            numberOfItems,
                            numberOfItems,
                        ),
                    )
                } else {
                    LazyColumn(
                        modifier = modifier.padding(it),
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
                                        onLocationPermissionChanged = function.onLocationPermissionChanged,
                                    )
                                }
                                ToolbarWithProgressbar(
                                    elevation = 0.dp,
                                    backgroundColor = Color.Transparent,
                                    title = stringResource(R.string.fr_toolbar_title),
                                    onNavigationIconClicked = function.onNavigationIconClicked,
                                    subTitle = context.resources.getQuantityString(
                                        R.plurals.fr_filter_toolbar_subtitle,
                                        numberOfItems,
                                        numberOfItems
                                    ),
                                )
                            }
                        }
                        itemsIndexed(recommendations) { index, recommendation ->
                            val recommendationTestTag = stringResource(
                                R.string.fr_recommendation_card_test_tag,
                                index,
                            )
                            RecommendationCardItem(
                                menuItems = menuItems,
                                function = function.function,
                                recommendation = recommendation,
                                modifier = Modifier.semantics {
                                    testTag = recommendationTestTag
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}