package co.ke.xently.recommendation.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import co.ke.xently.common.KENYA
import co.ke.xently.data.RecommendationRequest
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.recommendation.R
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class ShopRecommendationScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity by lazy { composeTestRule.activity }

    private val toolbarTitle by lazy {
        activity.getString(R.string.fr_filter_toolbar_title)
    }
    private val recommendButton by lazy {
        activity.getString(R.string.fr_filter_recommend).uppercase(KENYA)
    }
    private val productNameDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fr_filter_product_name),
        )
    }
    private val addProductNameButton by lazy {
        activity.getString(R.string.fr_filter_add_product_name_content_description)
    }
    private val persistCheckbox by lazy {
        activity.getString(R.string.fr_filter_should_persist_shopping_lists)
    }

    @Test
    fun toolbarTitle() {
        composeTestRule.setContent {
            XentlyTheme {
                ShopRecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = ShopRecommendationScreenFunction(),
                )
            }
        }

        composeTestRule.onNodeWithText(toolbarTitle).assertIsDisplayed().assertHasNoClickAction()
    }

    /*@Test
    fun recommendButtonText() {
        composeTestRule.setContent {
            XentlyTheme {
                ShopRecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = ShopRecommendationScreenFunction(),
                )
            }
        }

        composeTestRule.onNodeWithText(recommendButton).assertIsDisplayed()
            .assertHasClickAction()
    }*/

    @Test
    fun recommendButtonIsHiddenByDefault() {
        composeTestRule.setContent {
            XentlyTheme {
                ShopRecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = ShopRecommendationScreenFunction(),
                )
            }
        }

        composeTestRule.onNodeWithText(recommendButton).assertDoesNotExist()
    }

    @Test
    fun recommendButtonIsShownIfUnPersistedShoppingListItemIsAdded() {
        composeTestRule.setContent {
            XentlyTheme {
                ShopRecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = ShopRecommendationScreenFunction(),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(productNameDescription).run {
            performTextClearance()
            performTextInput("bread")
        }
        composeTestRule.onNodeWithTag(addProductNameButton).performClick()

        composeTestRule.onNodeWithText(recommendButton).assertIsEnabled()
    }

    @Test
    fun persistCheckboxIsCheckedByDefault() {
        composeTestRule.setContent {
            XentlyTheme {
                ShopRecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = ShopRecommendationScreenFunction(),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(persistCheckbox).assertIsToggleable()
            .assertIsOn()
    }

    @Test
    fun clickingOnNavigationButton() {
        val onNavigationClickMock: () -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                ShopRecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = ShopRecommendationScreenFunction(onNavigationClick = onNavigationClickMock),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.move_back))
            .performClick()
        verify(onNavigationClickMock, times(1)).invoke()
    }

    @Test
    fun productNameTextFieldIsEmptyByDefault() {
        composeTestRule.setContent {
            XentlyTheme {
                ShopRecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = ShopRecommendationScreenFunction(),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(productNameDescription).assertIsDisplayed()
            .assert(hasText(""))
    }

    @Test
    fun addProductNameButtonIsDisabledByDefault() {
        composeTestRule.setContent {
            XentlyTheme {
                ShopRecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = ShopRecommendationScreenFunction(),
                )
            }
        }

        composeTestRule.onNodeWithTag(addProductNameButton).assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun addProductNameButtonIsEnabledIfProductNameFieldIsNotBlank() {
        composeTestRule.setContent {
            XentlyTheme {
                ShopRecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = ShopRecommendationScreenFunction(),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(productNameDescription)
            .performTextInput("        ")
        composeTestRule.onNodeWithTag(addProductNameButton).assertIsNotEnabled()

        composeTestRule.onNodeWithContentDescription(productNameDescription).run {
            performTextClearance()
            performTextInput("    d    ")
        }
        composeTestRule.onNodeWithTag(addProductNameButton).assertIsEnabled()
    }

    @Test
    fun clickingOnAddProductNameButtonClearsProductNameField() {
        val onDetailSubmittedMock: (RecommendationRequest) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                ShopRecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = ShopRecommendationScreenFunction(
                        onDetailSubmitted = onDetailSubmittedMock,
                    ),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(productNameDescription).run {
            performTextClearance()
            performTextInput("    bread    ")
        }
        composeTestRule.onNodeWithTag(addProductNameButton).performClick()

        composeTestRule.onNodeWithContentDescription(productNameDescription).assert(hasText(""))
    }

    @Test
    fun clickingOnAddProductNameButtonAddsAnUnPersistedShoppingListItemWithStartAndEndSpacesTrimmed() {
        val onDetailSubmittedMock: (RecommendationRequest) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                ShopRecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = ShopRecommendationScreenFunction(
                        onDetailSubmitted = onDetailSubmittedMock,
                    ),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(productNameDescription).run {
            performTextClearance()
            performTextInput("    bread    ")
        }
        composeTestRule.onNodeWithTag(addProductNameButton).performClick()

        // We are performing a click here to help us retrieve the data for assertion
        composeTestRule.onNodeWithText(recommendButton).performClick()
        with(argumentCaptor<RecommendationRequest> { }) {
            verify(onDetailSubmittedMock, times(1)).invoke(capture())
            assertThat(firstValue.items[0], equalTo("bread"))
        }
    }

    @Test
    fun persistenceFlagCanBeToggledFromACheckbox() {
        val onDetailSubmittedMock: (RecommendationRequest) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                ShopRecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = ShopRecommendationScreenFunction(
                        onDetailSubmitted = onDetailSubmittedMock,
                    ),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(productNameDescription).run {
            performTextClearance()
            performTextInput("bread")
        }
        composeTestRule.onNodeWithTag(addProductNameButton).performClick()

        composeTestRule.onNodeWithContentDescription(persistCheckbox).performClick()

        composeTestRule.onNodeWithText(recommendButton).performClick()
        with(argumentCaptor<RecommendationRequest> { }) {
            verify(onDetailSubmittedMock, times(1)).invoke(capture())
            assertThat(firstValue.persist, equalTo(false))
        }
    }

    @Test
    fun clickingOnRemoveIconRemovesTheItemFromUnPersistedShoppingList() {
        val onDetailSubmittedMock: (RecommendationRequest) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                ShopRecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = ShopRecommendationScreenFunction(
                        onDetailSubmitted = onDetailSubmittedMock,
                    ),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(productNameDescription)
            .performTextInput("bread")
        composeTestRule.onNodeWithTag(addProductNameButton).performClick()

        composeTestRule.onNodeWithContentDescription(productNameDescription)
            .performTextInput("milk")
        composeTestRule.onNodeWithTag(addProductNameButton).performClick()

        composeTestRule.onNodeWithText(recommendButton).performClick()
        with(argumentCaptor<RecommendationRequest> { }) {
            verify(onDetailSubmittedMock, times(1)).invoke(capture())
            assertThat(firstValue.items.size, equalTo(2))
            assertThat(firstValue.items[0], equalTo("milk"))
        }

        composeTestRule.onNodeWithTag(activity.getString(R.string.fr_filter_remove_unpersisted_item, "milk")).performClick()

        composeTestRule.onNodeWithText(recommendButton).performClick()
        with(argumentCaptor<RecommendationRequest> { }) {
            verify(onDetailSubmittedMock, times(2)).invoke(capture())
            assertThat(firstValue.items[0], equalTo("bread"))
        }
    }
}