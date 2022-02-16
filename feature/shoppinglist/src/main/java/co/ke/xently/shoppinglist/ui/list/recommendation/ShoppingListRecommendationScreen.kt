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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.RecommendationReport
import co.ke.xently.data.RecommendationReport.Recommendation
import co.ke.xently.data.TaskResult
import co.ke.xently.data.errorMessage
import co.ke.xently.data.getOrThrow
import co.ke.xently.feature.ui.FullscreenError
import co.ke.xently.feature.ui.FullscreenLoading
import co.ke.xently.feature.ui.GoogleMapView
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.feature.utils.MAP_HEIGHT
import co.ke.xently.shoppinglist.R
import co.ke.xently.shoppinglist.Recommend
import co.ke.xently.shoppinglist.ui.list.item.ShoppingListItemCard
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import java.text.DecimalFormat

@Composable
internal fun ShoppingListRecommendationScreen(
    modifier: Modifier = Modifier,
    recommend: Recommend = Recommend(),
    viewModel: ShoppingListRecommendationViewModel = hiltViewModel(),
    onNavigationIconClicked: (() -> Unit) = {},
) {
    viewModel.setRecommend(recommend)
    val recommendationReportResult by viewModel.recommendationReportResult.collectAsState()

    ShoppingListRecommendationScreen(
        modifier,
        recommendationReportResult,
        onNavigationIconClicked,
        viewModel::setLocationPermissionGranted,
    )
}

@Composable
private fun ShoppingListRecommendationScreen(
    modifier: Modifier,
    result: TaskResult<RecommendationReport>,
    onNavigationIconClicked: () -> Unit,
    onLocationPermissionChanged: (Boolean) -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            if (result !is TaskResult.Success) {
                ToolbarWithProgressbar(
                    stringResource(R.string.fsl_recommendations_toolbar_title),
                    onNavigationIconClicked = onNavigationIconClicked,
                )
            }
        }
    ) {
        when (result) {
            is TaskResult.Error -> {
                FullscreenError(modifier.padding(it), result.errorMessage)
            }
            TaskResult -> {
                FullscreenLoading(modifier.padding(it))
            }
            is TaskResult.Success -> {
                val report = result.getOrThrow()
                LazyColumn(
                    modifier.padding(it),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                                }.toTypedArray(),
                                onLocationPermissionChanged = onLocationPermissionChanged,
                            )
                            ToolbarWithProgressbar(
                                stringResource(R.string.fsl_recommendations_toolbar_title),
                                elevation = 0.dp,
                                backgroundColor = Color.Transparent,
                                onNavigationIconClicked = onNavigationIconClicked
                            )
                        }
                    }
                    item {
                        RecommendationReportItemGroup(
                            modifier = Modifier.padding(start = 16.dp),
                            title = "Synopsis",
                        ) {
                            RecommendationReportSynopsisCard(
                                report = report,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp),
                            )
                        }
                    }
                    if (report.count.hitItems > 0) {
                        item {
                            RecommendationReportItemGroup(
                                modifier = Modifier.padding(start = 16.dp),
                                title = "Recommendations",
                            ) {
                                Column {
                                    report.recommendations.forEach { recommendation ->
                                        RecommendationCardItem(
                                            recommendation = recommendation,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (report.count.missedItems > 0) {
                        item {
                            RecommendationReportItemGroup(
                                modifier = Modifier.padding(start = 16.dp),
                                title = "Missed items",
                            ) {
                                report.missedItems.forEach { item ->
                                    ShoppingListItemCard(
                                        item = item,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .fillMaxWidth(),
                                    ) {
                                        // TODO: Implement click listener...
                                    }
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
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Text(text = title.uppercase(), style = MaterialTheme.typography.h6)
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
                .padding(vertical = 8.dp)
                .padding(end = 8.dp)
        ) {
            mutableListOf<Pair<String, String>>().apply {
                if (report.count.hitItems > 0) add("Hits" to "${report.count.hitItems}")
                if (report.count.missedItems > 0) add("Misses" to "${report.count.missedItems}")
                if (report.count.shopsVisited > 0) add("Shops visited" to "${report.count.shopsVisited}")
                if (report.count.recommendations > 0) add("Recommendations" to "${report.count.recommendations}")
                add("Lookup duration" to DecimalFormat("###,###.##s").format(report.lookupDuration))
            }.forEach {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = it.first, modifier = Modifier.weight(2f))
                    Text(text = it.second, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RecommendationCardItem(
    recommendation: Recommendation,
    modifier: Modifier = Modifier,
    onDirectionClicked: ((Recommendation) -> Unit) = {},
    onHitsClicked: ((Recommendation) -> Unit) = {},
    onDetailsClicked: ((Recommendation) -> Unit) = {},
) {
    var showMenu by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier) {
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
                    DropdownMenuItem(
                        onClick = {
                            onDirectionClicked(recommendation)
                            showMenu = false
                        },
                    ) { Text(text = "Directions") }
                    DropdownMenuItem(
                        onClick = {
                            onHitsClicked(recommendation)
                            showMenu = false
                        },
                    ) { Text(text = "Hits") }
                    DropdownMenuItem(
                        onClick = {
                            onDetailsClicked(recommendation)
                            showMenu = false
                        },
                    ) { Text(text = "Details") }
                }
            }
        }
    }
}