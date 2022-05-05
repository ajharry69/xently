package co.ke.xently.shops.ui.detail

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import co.ke.xently.common.KENYA
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.TEST_TAG_TEXT_FIELD_ERROR
import co.ke.xently.shops.R
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.mockito.internal.verification.VerificationModeFactory.times
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ShopDetailScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity by lazy { composeTestRule.activity }
    private val addShopButtonLabel by lazy {
        activity.getString(R.string.fs_shop_detail_toolbar_title, activity.getString(R.string.add))
            .uppercase(
                KENYA
            )
    }
    private val updateShopButtonLabel by lazy {
        activity.getString(
            R.string.fs_shop_detail_toolbar_title,
            activity.getString(R.string.update)
        ).uppercase(
            KENYA
        )
    }
    private val nameTextFieldDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fs_shop_item_detail_name_label),
        )
    }
    private val taxPinTextFieldDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fs_shop_item_detail_tax_pin_label),
        )
    }
    private val townTextFieldDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fs_shop_item_detail_town_label),
        )
    }
    private val coordinateTextFieldDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fs_shop_item_detail_coordinate_label),
        )
    }

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

    @Test
    fun clickingOnAddShopButtonTrimSpacesFromStartAndEndOfTextInputs() {
        val onAddShopClickedMock: (Shop) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                ShopDetailScreen(
                    isTestMode = true,
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    addResult = TaskResult.Success(null),
                    function = ShopDetailScreenFunction(onAddShopClicked = onAddShopClickedMock),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(nameTextFieldDescription)
            .performTextInput(" Shop name   ")

        composeTestRule.onNodeWithContentDescription(taxPinTextFieldDescription)
            .performTextInput(" PIN   ")

        composeTestRule.onNodeWithContentDescription(townTextFieldDescription)
            .performTextInput(" Nairobi   ")

        // This is only required to enable the button
        composeTestRule.onNodeWithContentDescription(coordinateTextFieldDescription)
            .performTextInput(" 0,1   ")

        composeTestRule.onNodeWithText(addShopButtonLabel).performClick()
        with(argumentCaptor<Shop> { }) {
            verify(onAddShopClickedMock, times(1)).invoke(capture())
            assertThat(firstValue.name, equalTo("Shop name"))
            assertThat(firstValue.taxPin, equalTo("PIN"))
            assertThat(firstValue.town, equalTo("Nairobi"))
        }
    }

    @Test
    fun clickingOnUpdateShopButtonTrimSpacesFromStartAndEndOfTextInputs() {
        val onAddShopClickedMock: (Shop) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                ShopDetailScreen(
                    isTestMode = true,
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Shop.default()
                            .copy(isDefault = false, id = 1, coordinate = Shop.Coordinate(0.0, 0.0))
                    ),
                    addResult = TaskResult.Success(null),
                    function = ShopDetailScreenFunction(onAddShopClicked = onAddShopClickedMock),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(nameTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithContentDescription(nameTextFieldDescription)
            .performTextInput(" Shop name   ")

        composeTestRule.onNodeWithContentDescription(taxPinTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithContentDescription(taxPinTextFieldDescription)
            .performTextInput(" PIN   ")

        composeTestRule.onNodeWithContentDescription(townTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithContentDescription(townTextFieldDescription)
            .performTextInput(" Nairobi   ")

        // This is only required to enable the button
        composeTestRule.onNodeWithContentDescription(coordinateTextFieldDescription)
            .performTextInput(" 0,1   ")

        composeTestRule.onNodeWithText(updateShopButtonLabel).performClick()
        with(argumentCaptor<Shop> { }) {
            verify(onAddShopClickedMock, times(1)).invoke(capture())
            assertThat(firstValue.name, equalTo("Shop name"))
            assertThat(firstValue.taxPin, equalTo("PIN"))
            assertThat(firstValue.town, equalTo("Nairobi"))
        }
    }
}