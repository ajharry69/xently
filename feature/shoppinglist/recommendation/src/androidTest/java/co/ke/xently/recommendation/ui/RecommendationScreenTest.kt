package co.ke.xently.recommendation.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import co.ke.xently.common.KENYA
import co.ke.xently.data.RecommendationRequest
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.recommendation.R
import co.ke.xently.source.remote.DeferredRecommendation
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class RecommendationScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity by lazy { composeTestRule.activity }

    private val progressbarDescription by lazy {
        activity.getString(R.string.progress_bar_content_description)
    }
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

    private fun addUnPersistedShoppingListItem(
        name: String,
        clearFieldFirst: Boolean = false,
        clickAddButton: Boolean = true,
    ) {
        composeTestRule.onNodeWithContentDescription(productNameDescription).run {
            if (clearFieldFirst) {
                performTextClearance()
            }
            performTextInput(name)
        }
        if (clickAddButton) {
            composeTestRule.onNodeWithTag(addProductNameButton).performClick()
        }
    }

    @Test
    fun toolbarTitle() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        composeTestRule.onNodeWithText(toolbarTitle).assertIsDisplayed().assertHasNoClickAction()
    }

    @Test
    fun progressbarIsShownWhenDeferredRecommendationResultIsLoading() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Loading,
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertIsDisplayed()
    }

    @Test
    fun progressbarIsShownWhenPersistedShoppingListResultIsLoading() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Loading,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertIsDisplayed()
    }

    @Test
    fun recommendButtonIsDisabledWhenDeferredRecommendationResultIsLoading() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Loading,
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        addUnPersistedShoppingListItem("Bread")

        composeTestRule.onNodeWithText(recommendButton)
            .assertIsNotEnabled()
    }

    @Test
    fun recommendButtonIsDisabledWhenPersistedShoppingListResultIsLoading() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Loading,
                )
            }
        }

        addUnPersistedShoppingListItem("Bread")

        composeTestRule.onNodeWithText(recommendButton)
            .assertIsNotEnabled()
    }

    @Test
    fun recommendButtonIsEnabledIfPersistedShoppingListResultIsNotEmptyList() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(listOf(ShoppingListItem.default())),
                )
            }
        }

        composeTestRule.onNodeWithText(recommendButton)
            .assertIsEnabled()
    }

    @Test
    fun errorIsShownIfPersistedShoppingListResultReturnsAnError() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Error("Sorry, something went wrong."),
                )
            }
        }

        composeTestRule.onNodeWithText("Sorry, something went wrong.")
            .assertIsDisplayed()
    }

    @Test
    fun errorIsShownIfDeferredRecommendationResultReturnsAnError() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Error("Location access is required."),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        composeTestRule.onNodeWithText("Location access is required.")
            .assertIsDisplayed()
    }

    @Test
    fun successfulDeferredRecommendationResultWithNonNullData() {
        val onSuccessMock: (DeferredRecommendation) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(onSuccess = onSuccessMock),
                    result = TaskResult.Success(DeferredRecommendation(id = "recommendation-lookup-key")),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        with(argumentCaptor<DeferredRecommendation> { }) {
            verify(onSuccessMock, times(1)).invoke(capture())
            assertThat(firstValue.toString(), equalTo("recommendation-lookup-key"))
        }
    }

    @Test
    fun recommendButtonText() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        composeTestRule.onNodeWithText(recommendButton).assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun recommendButtonIsDisabledByDefault() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        composeTestRule.onNodeWithText(recommendButton).assertIsNotEnabled()
    }

    @Test
    fun recommendButtonIsShownIfUnPersistedShoppingListItemIsAdded() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        addUnPersistedShoppingListItem("bread")

        composeTestRule.onNodeWithText(recommendButton).assertIsEnabled()
    }

    @Test
    fun persistCheckboxIsCheckedByDefault() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
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
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        onNavigationClick = onNavigationClickMock,
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
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
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
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
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
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
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        addUnPersistedShoppingListItem("       ", clickAddButton = false)
        composeTestRule.onNodeWithTag(addProductNameButton).assertIsNotEnabled()

        addUnPersistedShoppingListItem("   d  ", clickAddButton = false, clearFieldFirst = true)
        composeTestRule.onNodeWithTag(addProductNameButton).assertIsEnabled()
    }

    @Test
    fun clickingOnAddProductNameButtonClearsProductNameField() {
        val onDetailSubmittedMock: (RecommendationRequest) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        onDetailSubmitted = onDetailSubmittedMock,
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        addUnPersistedShoppingListItem("    bread    ")

        composeTestRule.onNodeWithContentDescription(productNameDescription).assert(hasText(""))
    }

    @Test
    fun clickingOnAddProductNameButtonAddsAnUnPersistedShoppingListItemWithStartAndEndSpacesTrimmed() {
        val onDetailSubmittedMock: (RecommendationRequest) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        onDetailSubmitted = onDetailSubmittedMock,
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        addUnPersistedShoppingListItem("    bread    ")

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
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        onDetailSubmitted = onDetailSubmittedMock,
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        addUnPersistedShoppingListItem("bread")

        composeTestRule.onNodeWithTag(addProductNameButton).performClick()

        composeTestRule.onNodeWithContentDescription(persistCheckbox).performClick()

        composeTestRule.onNodeWithText(recommendButton).performClick()
        with(argumentCaptor<RecommendationRequest> { }) {
            verify(onDetailSubmittedMock, times(1)).invoke(capture())
            assertThat(firstValue.persist, equalTo(false))
        }
    }

    @Test
    fun unsavedShoppingListItemHeadingIsHiddenIfUnPersistedShoppingListIsEmpty() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        composeTestRule.onNodeWithText(activity.getString(R.string.fr_filter_un_persisted_list_subheading))
            .assertDoesNotExist()
    }

    @Test
    fun unsavedShoppingListItemHeadingIsShownIfUnPersistedShoppingListIsNotEmpty() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        addUnPersistedShoppingListItem("Bread")
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_filter_un_persisted_list_subheading))
            .assertIsDisplayed()
    }

    @Test
    fun savedShoppingListItemHeadingIsHiddenIfPersistedShoppingListIsEmpty() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        composeTestRule.onNodeWithText(activity.getString(R.string.fr_filter_persisted_list_subheading))
            .assertDoesNotExist()
    }

    @Test
    fun savedShoppingListItemHeadingIsShownIfPersistedShoppingListIsNotEmpty() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(
                        listOf(ShoppingListItem.default().copy(isDefault = false))
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText(activity.getString(R.string.fr_filter_persisted_list_subheading))
            .assertIsDisplayed()
    }

    @Test
    fun clickingOnRemoveUnPersistedIconRemovesTheItemFromUnPersistedShoppingList() {
        val onDetailSubmittedMock: (RecommendationRequest) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        onDetailSubmitted = onDetailSubmittedMock,
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                )
            }
        }

        addUnPersistedShoppingListItem("bread")

        addUnPersistedShoppingListItem("milk")

        /*composeTestRule.onNodeWithText(recommendButton).performClick()
        with(argumentCaptor<RecommendationRequest> { }) {
            verify(onDetailSubmittedMock, times(1)).invoke(capture())
            assertThat(firstValue.items.size, equalTo(2))
            assertThat(firstValue.items[0], equalTo("milk"))
        }*/

        composeTestRule.onNodeWithTag(
            activity.getString(R.string.fr_filter_remove_unpersisted_item, "milk")
        ).performClick()

        composeTestRule.onNodeWithText(recommendButton)
            .performClick()
        with(argumentCaptor<RecommendationRequest> { }) {
            verify(onDetailSubmittedMock, times(1)).invoke(capture())
            assertThat(firstValue.items[0], equalTo("bread"))
        }
    }

    @Test
    fun persistedAndUnPersistedShoppingListsCanBeShownTogether() {
        val onDetailSubmittedMock: (RecommendationRequest) -> Unit = mock()
        val persistedShoppingList = listOf(
            ShoppingListItem.default().copy(isDefault = false, id = 1),
            ShoppingListItem.default().copy(
                isDefault = false,
                name = "White bread by superloaf",
                unit = "grams",
                unitQuantity = 400f,
                id = 2,
            ),
        )
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        onDetailSubmitted = onDetailSubmittedMock,
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(persistedShoppingList),
                )
            }
        }

        addUnPersistedShoppingListItem("White sugar by Kibos")
        addUnPersistedShoppingListItem("Salt by Kensalt")

        composeTestRule.onNodeWithText(activity.getString(R.string.fr_filter_persisted_list_subheading))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_filter_un_persisted_list_subheading))
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(recommendButton)
            .performClick()
        with(argumentCaptor<RecommendationRequest> { }) {
            verify(onDetailSubmittedMock, times(1)).invoke(capture())
            assertThat(firstValue.items.size, equalTo(4))
        }
    }

    @Test
    fun clickingOnRemovePersistedIconRemovesTheItemFromPersistedShoppingList() {
        val onDetailSubmittedMock: (RecommendationRequest) -> Unit = mock()
        val persistedShoppingList = listOf(
            ShoppingListItem.default().copy(isDefault = false, id = 1),
            ShoppingListItem.default().copy(
                isDefault = false,
                name = "White bread by superloaf",
                unit = "grams",
                unitQuantity = 400f,
                id = 2,
            ),
        )
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        onDetailSubmitted = onDetailSubmittedMock,
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(persistedShoppingList),
                )
            }
        }

        composeTestRule.onNodeWithTag(
            activity.getString(R.string.fr_filter_remove_persisted_item, persistedShoppingList[0])
        ).performClick()

        composeTestRule.onNodeWithText(recommendButton)
            .performClick()
        with(argumentCaptor<RecommendationRequest> { }) {
            verify(onDetailSubmittedMock, times(1)).invoke(capture())
            assertThat(firstValue.items[0], instanceOf(ShoppingListItem::class.java))
            assertThat(
                (firstValue.items[0] as ShoppingListItem).id,
                equalTo(persistedShoppingList[1].id),
            )
        }
    }
}