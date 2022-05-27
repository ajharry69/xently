package co.ke.xently.recommendation.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.input.ImeAction
import co.ke.xently.common.KENYA
import co.ke.xently.data.RecommendationRequest
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.PermissionGranted
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.MyUpdatedLocation
import co.ke.xently.recommendation.R
import co.ke.xently.source.remote.DeferredRecommendation
import com.google.android.gms.maps.model.LatLng
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import java.util.*

class RecommendationScreenTest {
    companion object {
        private val KICC = LatLng(-1.2890932945781504, 36.8209502554869)
    }

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
    fun productNameFieldImeAction() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(productNameDescription)
            .assert(hasImeAction(ImeAction.Done))
    }

    @Test
    fun nonBlankProductNameFieldSubmittedThroughImeAction() {
        val addUnPersistedShoppingListItemMock: (String) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        addUnPersistedShoppingListItem = addUnPersistedShoppingListItemMock,
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        addUnPersistedShoppingListItem("Bread   ", clickAddButton = false)

        composeTestRule.onNodeWithText(activity.getString(R.string.fr_filter_un_persisted_list_subheading))
            .assertDoesNotExist()
        verify(addUnPersistedShoppingListItemMock, times(0)).invoke("Bread")
        composeTestRule.onNodeWithContentDescription(productNameDescription)
            .performImeAction()
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_filter_un_persisted_list_subheading))
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(productNameDescription)
            .assert(hasText(""))
        with(argumentCaptor<String>()) {
            verify(addUnPersistedShoppingListItemMock, times(1)).invoke(capture())
            assertThat(firstValue, equalTo("Bread"))
        }
    }

    @Test
    fun duplicateNonBlankProductNameFieldSubmittedThroughImeAction() {
        val addUnPersistedShoppingListItemMock: (String) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        addUnPersistedShoppingListItem = addUnPersistedShoppingListItemMock,
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        addUnPersistedShoppingListItem("Bread   ", clickAddButton = false)

        composeTestRule.onNodeWithContentDescription(productNameDescription)
            .performImeAction()
        with(argumentCaptor<String>()) {
            verify(addUnPersistedShoppingListItemMock, times(1)).invoke(capture())
            assertThat(firstValue, equalTo("Bread"))
        }

        addUnPersistedShoppingListItem("  Bread   ", clickAddButton = false)

        composeTestRule.onNodeWithContentDescription(productNameDescription)
            .performImeAction()
        with(argumentCaptor<String>()) {
            verify(addUnPersistedShoppingListItemMock, times(1)).invoke(capture())
            assertThat(firstValue, equalTo("Bread"))
        }
    }

    @Test
    fun duplicateNonBlankProductNameFieldSubmittedThroughImeActionAlwaysClearTextField() {
        val addUnPersistedShoppingListItemMock: (String) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        addUnPersistedShoppingListItem = addUnPersistedShoppingListItemMock,
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        addUnPersistedShoppingListItem("Bread   ", clickAddButton = false)

        composeTestRule.onNodeWithContentDescription(productNameDescription)
            .performImeAction()
        composeTestRule.onNodeWithContentDescription(productNameDescription)
            .assert(hasText(""))

        addUnPersistedShoppingListItem("  Bread   ", clickAddButton = false)

        composeTestRule.onNodeWithContentDescription(productNameDescription)
            .performImeAction()
        composeTestRule.onNodeWithContentDescription(productNameDescription)
            .assert(hasText(""))
    }

    @Test
    fun blankProductNameFieldSubmittedThroughImeAction() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true,
                    ),
                )
            }
        }

        addUnPersistedShoppingListItem("    ", clickAddButton = false)

        composeTestRule.onNodeWithText(activity.getString(R.string.fr_filter_un_persisted_list_subheading))
            .assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(productNameDescription)
            .performImeAction()
        composeTestRule.onNodeWithContentDescription(productNameDescription)
            .assert(hasText(""))
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_filter_un_persisted_list_subheading))
            .assertDoesNotExist()
    }

    @Test
    fun recommendButtonIsDisabledIfLocationPermissionIsNotGranted() {
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
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(persistedShoppingList),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = false
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText(recommendButton).assertIsNotEnabled()
    }

    @Test
    fun messageShownWhenLocationPermissionIsGrantedAndMyLocationIsNull() {
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
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(persistedShoppingList),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = null,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText(activity.getString(R.string.fr_initiating_location_tracking))
            .assertIsDisplayed()
    }

    @Test
    fun locationTrackingMessageShownWhenLocationPermissionIsGrantedAndMyLocationIsNotNull() {
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
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(persistedShoppingList),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true,
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText(activity.getString(R.string.fr_initiating_location_tracking))
            .assertDoesNotExist()
    }

    @Test
    fun recommendButtonIsRenamedIfLocationPermissionIsGrantedAndMyLocationIsNull() {
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
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(persistedShoppingList),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = null,
                        isLocationPermissionGranted = true,
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText(recommendButton).assertDoesNotExist()
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_initiating_location_tracking))
            .assertIsDisplayed()
    }

    @Test
    fun locationRequestMessageWithButtonIsShownIfLocationPermissionIsNotGranted() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = false
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText(
            activity.getString(R.string.location_permission_rationale_minified)
        ).assertIsDisplayed()

        composeTestRule.onNodeWithText(
            activity.getString(R.string.grant_button_label)
        ).assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun clickingOnLocationRequestButton() {
        val onLocationPermissionRequestMock: (PermissionGranted) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        sharedFunction = SharedFunction(
                            onLocationPermissionChanged = onLocationPermissionRequestMock,
                        ),
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = false
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText(
            activity.getString(R.string.grant_button_label)
        ).performClick()

        with(argumentCaptor<PermissionGranted> { }) {
            verify(onLocationPermissionRequestMock, times(1)).invoke(capture())
            assertThat(firstValue.value, `is`(false))
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        with(argumentCaptor<DeferredRecommendation> { }) {
            verify(onSuccessMock, times(1)).invoke(capture())
            assertThat(firstValue.toString(), equalTo("recommendation-lookup-key"))
            assertThat(firstValue.id, equalTo("recommendation-lookup-key"))
            assertThat(firstValue.numberOfItems, equalTo(0))
        }
    }

    @Test
    fun successfulDeferredRecommendationResultWithNonNullDataAndNonEmptyPersistedShoppingList() {
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
        val onSuccessMock: (DeferredRecommendation) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(onSuccess = onSuccessMock),
                    result = TaskResult.Success(DeferredRecommendation(id = "recommendation-lookup-key")),
                    persistedShoppingListResult = TaskResult.Success(persistedShoppingList),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        with(argumentCaptor<DeferredRecommendation> { }) {
            verify(onSuccessMock, times(1)).invoke(capture())
            assertThat(firstValue.toString(), equalTo("recommendation-lookup-key"))
            assertThat(firstValue.id, equalTo("recommendation-lookup-key"))
            assertThat(firstValue.numberOfItems, equalTo(2))
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                        sharedFunction = SharedFunction(
                            onNavigationIconClicked = onNavigationClickMock
                        ),
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
        val addUnPersistedShoppingListItemMock: (String) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        addUnPersistedShoppingListItem = addUnPersistedShoppingListItemMock,
                        onDetailSubmitted = onDetailSubmittedMock,
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        addUnPersistedShoppingListItem("    bread    ")
        with(argumentCaptor<String>()) {
            verify(addUnPersistedShoppingListItemMock, times(1)).invoke(capture())
            assertThat(firstValue, equalTo("bread"))
        }

        composeTestRule.onNodeWithContentDescription(productNameDescription).assert(hasText(""))
    }

    @Test
    fun clickingOnAddProductNameButtonWhenItemIsAlreadyPresent() {
        val onDetailSubmittedMock: (RecommendationRequest) -> Unit = mock()
        val addUnPersistedShoppingListItemMock: (String) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        addUnPersistedShoppingListItem = addUnPersistedShoppingListItemMock,
                        onDetailSubmitted = onDetailSubmittedMock,
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        addUnPersistedShoppingListItem("    bread    ")
        with(argumentCaptor<String>()) {
            verify(addUnPersistedShoppingListItemMock, times(1)).invoke(capture())
            assertThat(firstValue, equalTo("bread"))
        }

        addUnPersistedShoppingListItem("    bread  ")
        with(argumentCaptor<String>()) {
            verify(addUnPersistedShoppingListItemMock, times(1)).invoke(capture())
            assertThat(firstValue, equalTo("bread"))
        }
    }

    @Test
    fun clickingOnAddProductNameButtonWhenItemIsAlreadyPresentAlwaysClearsTextField() {
        val onDetailSubmittedMock: (RecommendationRequest) -> Unit = mock()
        val addUnPersistedShoppingListItemMock: (String) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        addUnPersistedShoppingListItem = addUnPersistedShoppingListItemMock,
                        onDetailSubmitted = onDetailSubmittedMock,
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        addUnPersistedShoppingListItem("    bread    ")

        composeTestRule.onNodeWithContentDescription(productNameDescription).assert(hasText(""))

        addUnPersistedShoppingListItem("    bread  ")

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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
    fun persistenceFlagCanBeToggledFromCheckbox() {
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
    fun persistenceFlagCanBeToggledFromCheckboxLabel() {
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        addUnPersistedShoppingListItem("bread")

        composeTestRule.onNodeWithTag(addProductNameButton).performClick()

        composeTestRule.onNodeWithText(persistCheckbox).performClick()

        composeTestRule.onNodeWithText(recommendButton).performClick()
        with(argumentCaptor<RecommendationRequest> { }) {
            verify(onDetailSubmittedMock, times(1)).invoke(capture())
            assertThat(firstValue.persist, equalTo(false))
        }
    }

    @Test
    fun persistenceFlagCanBeToggledFromCheckboxContainer() {
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        addUnPersistedShoppingListItem("bread")

        composeTestRule.onNodeWithTag(addProductNameButton).performClick()

        composeTestRule.onNodeWithTag(persistCheckbox).performClick()

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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
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
        val removeUnPersistedShoppingListItemAtMock: (Int) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        onDetailSubmitted = onDetailSubmittedMock,
                        removeUnPersistedShoppingListItemAt = removeUnPersistedShoppingListItemAtMock,
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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

        verify(removeUnPersistedShoppingListItemAtMock, times(0)).invoke(0)
        composeTestRule.onNodeWithTag(
            activity.getString(R.string.fr_filter_remove_un_persisted_item, "milk")
        ).performClick()

        with(argumentCaptor<Int>()) {
            verify(removeUnPersistedShoppingListItemAtMock, times(1)).invoke(capture())
            assertThat(firstValue, equalTo(0))
        }

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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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
    fun unPersistedShoppingListCanBePrePopulatedFromUnpersistedShoppingListArgument() {
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
        val unPersistedShoppingList = java.util.Stack<String>()
        unPersistedShoppingList.push("White sugar by Kibos")
        unPersistedShoppingList.push("Salt by Kensalt")

        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(
                        onDetailSubmitted = onDetailSubmittedMock,
                    ),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(persistedShoppingList),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                    unpersistedShoppingList = unPersistedShoppingList,
                )
            }
        }

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
    fun toolbarSubtitleForEmptyPersistedShoppingList() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText("0 items").assertIsDisplayed()
    }

    @Test
    fun toolbarSubtitleForNonEmptyPersistedShoppingList() {
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
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(persistedShoppingList),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText("2 items").assertIsDisplayed()
    }

    @Test
    fun toolbarSubtitleForPersistedShoppingListOfSize1() {
        val persistedShoppingList = listOf(
            ShoppingListItem.default().copy(isDefault = false, id = 1)
        )
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(persistedShoppingList),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText("1 item").assertIsDisplayed()
    }

    @Test
    fun toolbarSubtitleForPersistedAndUnPersistedShoppingList() {
        val persistedShoppingList = listOf(
            ShoppingListItem.default().copy(isDefault = false, id = 1)
        )
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(persistedShoppingList),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        addUnPersistedShoppingListItem("Milk")

        composeTestRule.onNodeWithText("2 items").assertIsDisplayed()
    }

    @Test
    fun toolbarSubtitleWhenPersistedShoppingListItemIsRemoved() {
        val persistedShoppingList = listOf(
            ShoppingListItem.default().copy(isDefault = false, id = 1)
        )
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(persistedShoppingList),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText("1 item").assertIsDisplayed()

        composeTestRule.onNodeWithTag(
            activity.getString(R.string.fr_filter_remove_persisted_item, persistedShoppingList[0])
        ).performClick()

        composeTestRule.onNodeWithText("0 items").assertIsDisplayed()
    }

    @Test
    fun toolbarSubtitleWhenUnPersistedShoppingListItemIsRemoved() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationScreen(
                    modifier = Modifier.fillMaxSize(),
                    function = RecommendationScreenFunction(),
                    result = TaskResult.Success(null),
                    persistedShoppingListResult = TaskResult.Success(emptyList()),
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
                )
            }
        }

        addUnPersistedShoppingListItem("Bread")

        composeTestRule.onNodeWithText("1 item").assertIsDisplayed()

        composeTestRule.onNodeWithTag(
            activity.getString(R.string.fr_filter_remove_un_persisted_item, "Bread")
        ).performClick()

        composeTestRule.onNodeWithText("0 items").assertIsDisplayed()
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
                    myUpdatedLocation = MyUpdatedLocation(
                        myLocation = KICC,
                        isLocationPermissionGranted = true
                    ),
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