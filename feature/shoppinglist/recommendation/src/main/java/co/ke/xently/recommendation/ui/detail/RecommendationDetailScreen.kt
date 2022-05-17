package co.ke.xently.recommendation.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.ke.xently.common.KENYA
import co.ke.xently.data.Recommendation
import co.ke.xently.feature.ui.DEFAULT_VERTICAL_SPACING_ONLY
import co.ke.xently.feature.ui.ListItemSurface
import co.ke.xently.recommendation.R
import java.text.NumberFormat
import java.util.*

@Composable
internal fun RecommendationDetailScreen(
    modifier: Modifier = Modifier,
    recommendation: Recommendation,
) {
    LazyColumn(modifier = modifier) {
        if (recommendation.hit.count > 0) {
            item {
                Text(
                    text = stringResource(R.string.fr_detail_hit_heading),
                    style = MaterialTheme.typography.h5
                )
            }
            items(recommendation.hit.items) { hit ->
                ListItemSurface(
                    modifier = Modifier.fillMaxWidth(),
                    paddingValues = DEFAULT_VERTICAL_SPACING_ONLY,
                ) {
                    Column(modifier = Modifier.fillParentMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = hit.requested,
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.weight(1f),
                            )
                            val unitPrice = NumberFormat.getCurrencyInstance().apply {
                                currency = Currency.getInstance(KENYA)
                            }.format(hit.unitPrice)
                            Text(text = unitPrice, style = MaterialTheme.typography.body1)
                        }
                        Text(text = hit.found, style = MaterialTheme.typography.caption)
                    }
                }
            }
        }
        if (recommendation.miss.count > 0) {
            item {
                Text(
                    text = stringResource(R.string.fr_detail_miss_heading),
                    style = MaterialTheme.typography.h5
                )
            }
            items(recommendation.miss.items) { miss ->
                ListItemSurface(
                    modifier = Modifier.fillMaxWidth(),
                    paddingValues = DEFAULT_VERTICAL_SPACING_ONLY,
                ) {
                    Text(text = miss, style = MaterialTheme.typography.body1)
                }
            }
        }
    }
}