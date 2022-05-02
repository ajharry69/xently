package co.ke.xently.shoppinglist.ui.list.recommendation.item

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import co.ke.xently.common.KENYA
import co.ke.xently.data.Recommendation
import co.ke.xently.data.Shop
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.shoppinglist.R
import org.junit.Rule
import org.junit.Test
import java.text.NumberFormat
import java.util.*

class ShoppingListRecommendationItemTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun recommendationDetails() {
        val recommendation = Recommendation(
            shop = Shop.default(),
            hit = Recommendation.Hit(
                count = 2,
                items = listOf("Bread", "Milk"),
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
                    menuItems = listOf(RecommendationCardItemMenuItem(R.string.fsl_recommendation_details)),
                    function = RecommendationCardItemFunction(),
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