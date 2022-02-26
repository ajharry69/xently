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
import co.ke.xently.data.*
import co.ke.xently.data.RecommendationReport.Recommendation
import co.ke.xently.feature.ui.*
import co.ke.xently.feature.utils.MAP_HEIGHT
import co.ke.xently.shoppinglist.R
import co.ke.xently.shoppinglist.Recommend
import co.ke.xently.shoppinglist.ui.list.item.ShoppingListItemCard
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import java.text.DecimalFormat

internal data class RecommendationCardItemClick(
    val base: (Recommendation) -> Unit = {},
)

internal data class Click(
    val navigationIcon: () -> Unit = {},
    val item: (ShoppingListItem) -> Unit = {},
    val recommendationItemClick: RecommendationCardItemClick = RecommendationCardItemClick(),
)

@Composable
internal fun ShoppingListRecommendationScreen(
    click: Click,
    menuItems: List<MenuItem>,
    modifier: Modifier = Modifier,
    recommend: Recommend = Recommend(),
    viewModel: ShoppingListRecommendationViewModel = hiltViewModel(),
) {
    viewModel.setRecommend(recommend)
    val recommendationReportResult by viewModel.recommendationReportResult.collectAsState()

    ShoppingListRecommendationScreen(
        modifier,
        recommendationReportResult,
        click,
        viewModel::setLocationPermissionGranted,
        menuItems,
    )
}

@Composable
private fun ShoppingListRecommendationScreen(
    modifier: Modifier,
    result: TaskResult<RecommendationReport>,
    click: Click,
    onLocationPermissionChanged: (Boolean) -> Unit,
    menuItems: List<MenuItem>,
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            if (result !is TaskResult.Success) {
                ToolbarWithProgressbar(
                    title = stringResource(R.string.fsl_recommendations_toolbar_title),
                    onNavigationIconClicked = click.navigationIcon,
                )
            }
        }
    ) {
        when (result) {
            is TaskResult.Error -> {
                FullscreenError(modifier.padding(it), result.error)
            }
            TaskResult -> {
                FullscreenLoading(modifier.padding(it))
            }
            is TaskResult.Success -> {
                val report = result.getOrThrow()
                LazyColumn(
                    modifier = modifier.padding(it),
                    verticalArrangement = Arrangement.spacedBy(HORIZONTAL_PADDING / 2),
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
                                onLocationPermissionChanged = onLocationPermissionChanged,
                            )
                            ToolbarWithProgressbar(
                                title = stringResource(R.string.fsl_recommendations_toolbar_title),
                                onNavigationIconClicked = click.navigationIcon,
                                backgroundColor = Color.Transparent,
                                elevation = 0.dp
                            )
                        }
                    }
                    item {
                        RecommendationReportItemGroup(
                            modifier = Modifier.padding(start = HORIZONTAL_PADDING),
                            title = stringResource(R.string.fsl_recommendations_synopsis),
                        ) {
                            RecommendationReportSynopsisCard(
                                report = report,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = HORIZONTAL_PADDING),
                            )
                        }
                    }
                    if (report.count.hitItems > 0) {
                        item {
                            RecommendationReportItemGroup(
                                textModifier = Modifier.padding(start = HORIZONTAL_PADDING),
                                title = stringResource(R.string.fsl_recommendations),
                            ) {
                                Column {
                                    report.recommendations.forEach { recommendation ->
                                        RecommendationCardItem(
                                            menuItems = menuItems,
                                            recommendation = recommendation,
                                            modifier = Modifier.fillMaxWidth(),
                                            click = click.recommendationItemClick,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (report.count.missedItems > 0) {
                        item {
                            RecommendationReportItemGroup(
                                textModifier = Modifier.padding(start = HORIZONTAL_PADDING),
                                title = stringResource(R.string.fsl_recommendations_missed),
                            ) {
                                report.missedItems.forEach { item ->
                                    ShoppingListItemCard(
                                        item = item,
                                        onClick = click.item,
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
                .padding(end = HORIZONTAL_PADDING / 2)
                .padding(vertical = HORIZONTAL_PADDING / 2),
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
                        .padding(start = HORIZONTAL_PADDING / 2),
                ) {
                    Text(text = it.first, modifier = Modifier.weight(2f))
                    Text(text = it.second, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

internal data class MenuItem(
    val label: Int,
    val onClick: (Recommendation) -> Unit = {},
)

@Composable
private fun RecommendationCardItem(
    recommendation: Recommendation,
    menuItems: List<MenuItem>,
    click: RecommendationCardItemClick,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    ListItemSurface(modifier = modifier, onClick = { click.base.invoke(recommendation) }) {
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