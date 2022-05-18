package co.ke.xently.recommendation.ui.list.item

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import co.ke.xently.common.KENYA
import co.ke.xently.data.Recommendation
import co.ke.xently.data.Shop
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.ListItemSurface
import co.ke.xently.feature.ui.NEGLIGIBLE_SPACE
import co.ke.xently.recommendation.R
import java.text.NumberFormat
import java.util.*

internal data class RecommendationCardItemFunction(
    val onItemClicked: (Recommendation) -> Unit = {},
)

internal data class RecommendationCardItemMenuItem(
    val label: Int,
    val onClick: (Recommendation) -> Unit = {},
)

@Composable
internal fun RecommendationCardItem(
    modifier: Modifier = Modifier,
    recommendation: Recommendation,
    function: RecommendationCardItemFunction,
    menuItems: List<RecommendationCardItemMenuItem>,
) {
    var showMenu by remember { mutableStateOf(false) }
    ListItemSurface(
        modifier = modifier,
        onClick = { function.onItemClicked.invoke(recommendation) },
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(NEGLIGIBLE_SPACE),
        ) {
            Text(
                modifier = Modifier.wrapContentWidth(),
                text = recommendation.shop.descriptiveName,
                style = MaterialTheme.typography.body1,
            )
            val totalExpenditureEstimate = NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance(KENYA)
            }.format(recommendation.expenditure.total)
            Text(
                modifier = Modifier.wrapContentWidth(),
                text = LocalContext.current.resources.getQuantityString(
                    R.plurals.fr_recommendation_item,
                    recommendation.numberOfItems,
                    recommendation.hit.count,
                    recommendation.numberOfItems,
                    totalExpenditureEstimate,
                ),
                style = MaterialTheme.typography.caption,
            )
        }
        Box(modifier = Modifier.width(IntrinsicSize.Min)) {
            val contentDescription = stringResource(
                R.string.fr_item_menu_content_description,
                recommendation.shop.descriptiveName,
            )
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.semantics {
                    testTag = contentDescription
                },
            ) {
                Icon(
                    imageVector = if (showMenu) {
                        Icons.Default.KeyboardArrowDown
                    } else {
                        Icons.Default.KeyboardArrowRight
                    },
                    contentDescription = contentDescription,
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

@Preview
@Composable
private fun RecommendationCardItemPreview() {
    XentlyTheme {
        Column {
            RecommendationCardItem(
                modifier = Modifier.fillMaxWidth(),
                recommendation = Recommendation(
                    shop = Shop.default(),
                    hit = Recommendation.Hit(
                        count = 2,
                        items = listOf(
                            Recommendation.Hit.Item(
                                found = "Bread",
                                requested = "Bread",
                                unitPrice = 50f,
                            ),
                            Recommendation.Hit.Item(
                                found = "Milk",
                                requested = "Milk",
                                unitPrice = 50f,
                            ),
                        ),
                    ),
                    miss = Recommendation.Miss(
                        count = 1,
                        items = listOf("Cocoa"),
                    ),
                    expenditure = Recommendation.Expenditure(
                        unit = 200f,
                        total = 200f,
                    ),
                ),
                function = RecommendationCardItemFunction(),
                menuItems = listOf(RecommendationCardItemMenuItem(R.string.fr_details)),
            )
        }
    }
}