package co.ke.xently.recommendation.ui.list.item

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import co.ke.xently.common.KENYA
import co.ke.xently.data.Recommendation
import co.ke.xently.data.Shop
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.recommendation.R
import org.junit.Rule
import org.junit.Test
import java.text.NumberFormat
import java.util.*

class RecommendationItemTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun recommendationDetails() {
        val recommendation = Recommendation(
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
        )
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationCardItem(
                    modifier = Modifier.fillMaxWidth(),
                    recommendation = recommendation,
                    function = RecommendationCardItemFunction(),
                    menuItems = listOf(RecommendationCardItemMenuItem(R.string.fr_details)),
                )
            }
        }

        composeTestRule.onNodeWithText(recommendation.shop.descriptiveName).assertIsDisplayed()
        val totalExpenditureEstimate = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(KENYA)
        }.format(recommendation.expenditure.total)
        composeTestRule.onNodeWithText(
            "Found 2 of 3 items at an estimated total expenditure of $totalExpenditureEstimate",
            substring = true,
        ).assertIsDisplayed()
    }
}