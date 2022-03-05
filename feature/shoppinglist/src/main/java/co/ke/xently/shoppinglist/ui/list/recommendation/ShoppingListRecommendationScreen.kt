package co.ke.xently.shoppinglist.ui.list.recommendation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.RecommendationReport
import co.ke.xently.data.RecommendationReport.Recommendation
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.data.getOrThrow
import co.ke.xently.feature.ui.*
import co.ke.xently.feature.utils.MAP_HEIGHT
import co.ke.xently.shoppinglist.R
import co.ke.xently.shoppinglist.Recommend
import co.ke.xently.shoppinglist.ui.list.item.ShoppingListItemCard
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import java.text.DecimalFormat

internal data class RecommendationCardItemFunction(
    val onItemClicked: (Recommendation) -> Unit = {},
)

internal data class ShoppingListRecommendationScreenFunction(
    val onNavigationIconClicked: () -> Unit = {},
    val onRetryClicked: (Throwable) -> Unit = {},
    val onItemClicked: (ShoppingListItem) -> Unit = {},
    val onLocationPermissionChanged: (Boolean) -> Unit = {},
    val function: RecommendationCardItemFunction = RecommendationCardItemFunction(),
)

@Composable
internal fun ShoppingListRecommendationScreen(
    modifier: Modifier,
    menuItems: List<RecommendationCardItemMenuItem>,
    function: ShoppingListRecommendationScreenFunction,
    recommend: Recommend = Recommend(),
    viewModel: ShoppingListRecommendationViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val result by viewModel.result.collectAsState(
        initial = TaskResult.Loading,
        context = scope.coroutineContext,
    )

    LaunchedEffect(recommend) {
        viewModel.initRecommendation(recommend)
    }

    ShoppingListRecommendationScreen(
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
private fun ShoppingListRecommendationScreen(
    modifier: Modifier,
    result: TaskResult<RecommendationReport>,
    menuItems: List<RecommendationCardItemMenuItem>,
    function: ShoppingListRecommendationScreenFunction,
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            if (result !is TaskResult.Success) {
                ToolbarWithProgressbar(
                    title = stringResource(R.string.fsl_recommendations_toolbar_title),
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
                FullscreenLoading<RecommendationReport>(modifier = modifier.padding(it))
            }
            is TaskResult.Success -> {
                val report = result.getOrThrow()
                LazyColumn(
                    modifier = modifier.padding(it),
                    verticalArrangement = Arrangement.spacedBy(VIEW_SPACE / 2),
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
                                markerPositions = report.recommendations.flatMap { recommendation ->
                                    recommendation.addresses.map { address ->
                                        MarkerOptions().apply {
                                            title("${recommendation.name}, ${recommendation.taxPin}")
                                            snippet("${recommendation.hits.count} item(s), ${recommendation.printableTotalPrice}")
                                            position(
                                                LatLng(
                                                    address.location.latitude,
                                                    address.location.longitude,
                                                )
                                            )
                                        }
                                    }
                                },
                                onLocationPermissionChanged = function.onLocationPermissionChanged,
                            )
                            ToolbarWithProgressbar(
                                title = stringResource(R.string.fsl_recommendations_toolbar_title),
                                onNavigationIconClicked = function.onNavigationIconClicked,
                                backgroundColor = Color.Transparent,
                                elevation = 0.dp
                            )
                        }
                    }
                    item {
                        RecommendationReportItemGroup(
                            modifier = Modifier.padding(start = VIEW_SPACE),
                            title = stringResource(R.string.fsl_recommendations_synopsis),
                        ) {
                            RecommendationReportSynopsisCard(
                                report = report,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = VIEW_SPACE),
                            )
                        }
                    }
                    if (report.count.hitItems > 0) {
                        item {
                            RecommendationReportItemGroup(
                                textModifier = Modifier.padding(start = VIEW_SPACE),
                                title = stringResource(R.string.fsl_recommendations),
                            ) {
                                Column {
                                    report.recommendations.forEach { recommendation ->
                                        RecommendationCardItem(
                                            menuItems = menuItems,
                                            recommendation = recommendation,
                                            modifier = Modifier.fillMaxWidth(),
                                            function = function.function,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (report.count.missedItems > 0) {
                        item {
                            RecommendationReportItemGroup(
                                textModifier = Modifier.padding(start = VIEW_SPACE),
                                title = stringResource(R.string.fsl_recommendations_missed),
                            ) {
                                report.missedItems.forEach { item ->
                                    ShoppingListItemCard(
                                        item = item,
                                        onClick = function.onItemClicked,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationReportItemGroup(
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Text(text = title.uppercase(), modifier = textModifier, style = MaterialTheme.typography.h6)
        content()
    }
}

@Composable
private fun RecommendationReportSynopsisCard(
    modifier: Modifier = Modifier,
    report: RecommendationReport,
) {
    Card(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .padding(end = VIEW_SPACE / 2)
                .padding(vertical = VIEW_SPACE / 2),
        ) {
            val context = LocalContext.current
            buildList {
                if (report.count.hitItems > 0) {
                    add(context.getString(R.string.fsl_recommendation_hits) to "${report.count.hitItems}")
                }
                if (report.count.missedItems > 0) {
                    add(context.getString(R.string.fsl_recommendation_misses) to "${report.count.missedItems}")
                }
                if (report.count.shopsVisited > 0) {
                    add(stringResource(R.string.fsl_recommendation_shops_visited) to "${report.count.shopsVisited}")
                }
                if (report.count.recommendations > 0) {
                    add(stringResource(R.string.fsl_recommendation_recommendations) to "${report.count.recommendations}")
                }
                add(context.getString(R.string.fsl_recommendation_lookup_duration) to DecimalFormat(
                    "###,###.##s").format(report.lookupDuration))
            }.forEach {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = VIEW_SPACE / 2),
                ) {
                    Text(text = it.first, modifier = Modifier.weight(2f))
                    Text(text = it.second, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

internal data class RecommendationCardItemMenuItem(
    val label: Int,
    val onClick: (Recommendation) -> Unit = {},
)

@Composable
private fun RecommendationCardItem(
    recommendation: Recommendation,
    menuItems: List<RecommendationCardItemMenuItem>,
    function: RecommendationCardItemFunction,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    ListItemSurface(modifier = modifier,
        onClick = { function.onItemClicked.invoke(recommendation) }) {
        Column {
            Text(text = recommendation.name, style = MaterialTheme.typography.body1)
            Text(
                text = "${recommendation.printableTotalPrice} | ${recommendation.estimatedDistance} away",
                style = MaterialTheme.typography.caption
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "${recommendation.hits.count}", style = MaterialTheme.typography.h6)
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        if (showMenu) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = "${recommendation.name} recommendation options",
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    for (item in menuItems) {
                        DropdownMenuItem(
                            onClick = {
                                item.onClick.invoke(recommendation)
                                showMenu = false
                            },
                        ) { Text(text = stringResource(item.label)) }
                    }
                }
            }
        }
    }
}