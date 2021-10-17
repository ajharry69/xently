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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.data.RecommendationReport
import co.ke.xently.data.RecommendationReport.Recommendation
import co.ke.xently.shoppinglist.R
import co.ke.xently.shoppinglist.ui.GoogleMapView
import co.ke.xently.shoppinglist.ui.list.ShoppingListItemCard
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.LatLng
import java.text.DecimalFormat


@Composable
internal fun ShoppingListRecommendationScreen(
    recommendBy: Any,
    modifier: Modifier = Modifier,
    viewModel: ShoppingListRecommendationViewModel = hiltViewModel(),
    onLocationPermissionNotGranted: ((GoogleMap) -> Unit) = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val recommendationReportResult by viewModel.getRecommendations(recommendBy = recommendBy)
        .collectAsState(Result.success(null), coroutineScope.coroutineContext)

    if (recommendationReportResult.isSuccess) {
        when (val report = recommendationReportResult.getOrThrow()) {
            null -> {
                Box(contentAlignment = Alignment.Center, modifier = modifier) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(R.string.fsl_data_loading))
                    }
                }
            }
            else -> {
                LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        GoogleMapView(
                            Modifier.height(250.dp),
                            LatLng(0.0, 0.0),
                            report.recommendations.flatMap {
                                it.addresses.map { address ->
                                    LatLng(address.latitude, address.longitude)
                                }
                            }.toTypedArray(),
                            onLocationPermissionNotGranted,
                        )
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
                                    report.recommendations.forEach {
                                        RecommendationCardItem(
                                            recommendation = it,
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
                                modifier = Modifier.padding(start = 16.dp), title = "Missed items",
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
    } else {
        Box(contentAlignment = Alignment.Center, modifier = modifier) {
            Text(
                text = recommendationReportResult.exceptionOrNull()?.localizedMessage
                    ?: stringResource(R.string.fsl_generic_error_message)
            )
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
    report: RecommendationReport
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
            val totalPrice = DecimalFormat("KES ###,###.##").format(recommendation.hits.totalPrice)
            Text(
                text = "$totalPrice | ${recommendation.estimatedDistance} away",
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