package co.ke.xently.shops.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.TEST_TAG_TEXT_FIELD_ERROR
import org.junit.Rule
import org.junit.Test

class ShopDetailScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun taskResultWithErrorOnShopFieldShowsErrorCaptionBelowShopField() {
        composeTestRule.setContent {
            XentlyTheme {
                ShopDetailScreen(
                    isTestMode = true,
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Shop.default().copy(
                            id = 1,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Error(ShopHttpException(taxPin = listOf("shop with this tax pin already exists."))),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_ERROR).assertIsDisplayed()
            .assert(hasText("shop with this tax pin already exists."))
    }
}