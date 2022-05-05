package co.ke.xently.recommendation.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.Recommendation
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.data.getOrThrow
import co.ke.xently.feature.ui.*
import co.ke.xently.feature.utils.MAP_HEIGHT
import co.ke.xently.recommendation.R
import co.ke.xently.recommendation.Recommend
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItem
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItemFunction
import co.ke.xently.recommendation.ui.list.item.RecommendationCardItemMenuItem
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions

internal data class RecommendationListScreenFunction(
    val onNavigationIconClicked: () -> Unit = {},
    val onRetryClicked: (Throwable) -> Unit = {},
    val onItemClicked: (ShoppingListItem) -> Unit = {},
    val onLocationPermissionChanged: (Boolean) -> Unit = {},
    val function: RecommendationCardItemFunction = RecommendationCardItemFunction(),
)

@Composable
internal fun RecommendationListScreen(
    modifier: Modifier,
    menuItems: List<RecommendationCardItemMenuItem>,
    function: RecommendationListScreenFunction,
    recommend: Recommend = Recommend(),
    viewModel: RecommendationListViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val result by viewModel.result.collectAsState(
        initial = TaskResult.Loading,
        context = scope.coroutineContext,
    )

    LaunchedEffect(recommend) {
        viewModel.initRecommendation(recommend)
    }

    RecommendationListScreen(
        result = result,
        modifier = modifier,
        menuItems = menuItems,
        function = function.copy(
            onRetryClicked = {
                viewModel.initRecommendation(recommend)
            },
            onLocationPermissionChanged = viewModel::setLocationPermissionGranted,
        ),
    )
}

@Composable
private fun RecommendationListScreen(
    modifier: Modifier,
    result: TaskResult<List<Recommendation>>,
    menuItems: List<RecommendationCardItemMenuItem>,
    function: RecommendationListScreenFunction,
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            if (result !is TaskResult.Success) {
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
                val recommendations = result.getOrThrow()
                LazyColumn(
                    modifier = modifier.padding(it),
                    verticalArrangement = Arrangement.spacedBy(VIEW_SPACE_HALVED),
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .height(IntrinsicSize.Min)
                                .fillMaxWidth()
                        ) {
                            GoogleMapView(
                                Modifier
                                    .height(MAP_HEIGHT)
                                    .fillMaxWidth(),
                                markerPositions = recommendations.filter { recommendation ->
                                    recommendation.shop.coordinate != null
                                }.map { recommendation ->
                                    MarkerOptions().apply {
                                        title(recommendation.shop.descriptiveName)
                                        snippet("${recommendation.hit.count} item(s), ${recommendation.expenditure.total}")
                                        position(
                                            LatLng(
                                                recommendation.shop.coordinate!!.lat,
                                                recommendation.shop.coordinate!!.lon,
                                            )
                                        )
                                    }
                                },
                                onLocationPermissionChanged = function.onLocationPermissionChanged,
                            )
                            ToolbarWithProgressbar(
                                title = stringResource(R.string.fr_toolbar_title),
                                onNavigationIconClicked = function.onNavigationIconClicked,
                                backgroundColor = Color.Transparent,
                                elevation = 0.dp,
                            )
                        }
                    }
                    items(recommendations) { recommendation ->
                        RecommendationCardItem(
                            menuItems = menuItems,
                            function = function.function,
                            recommendation = recommendation,
                        )
                    }
                }
            }
        }
    }
}