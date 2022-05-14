package co.ke.xently.recommendation.ui.list

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import co.ke.xently.data.Recommendation
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.TEST_TAG_CIRCULAR_PROGRESS_BAR
import co.ke.xently.recommendation.R
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class RecommendationListScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity by lazy { composeTestRule.activity }

    private val toolbarTitle by lazy {
        activity.getString(R.string.fr_toolbar_title)
    }

    @Test
    fun clickingOnNavigationButton() {
        val onNavigationClickMock: () -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationListScreen(
                    numberOfItems = 1,
                    menuItems = emptyList(),
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(emptyList()),
                    function = RecommendationListScreenFunction(
                        sharedFunction = SharedFunction(
                            onNavigationIconClicked = onNavigationClickMock,
                        ),
                    ),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.move_back))
            .performClick()
        verify(onNavigationClickMock, times(1)).invoke()
    }

    @Test
    fun toolbarTitle() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationListScreen(
                    numberOfItems = 1,
                    menuItems = emptyList(),
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(emptyList()),
                    function = RecommendationListScreenFunction(),
                )
            }
        }

        composeTestRule.onNodeWithText(toolbarTitle)
            .assertIsDisplayed()
            .assertHasNoClickAction()
        composeTestRule.onNodeWithText("1 item")
            .assertDoesNotExist()
    }

    @Test
    fun emptyRecommendationStringShownIfResultIsSuccessWithEmptyRecommendations() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationListScreen(
                    numberOfItems = 1,
                    menuItems = emptyList(),
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(emptyList()),
                    function = RecommendationListScreenFunction(),
                )
            }
        }

        composeTestRule
            .onNodeWithText("Shop recommendation for 1 item not found within your current location.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("1 item")
            .assertDoesNotExist()
    }

    @Test
    fun progressBarIsShownIsShownIfRecommendationsAreLoading() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationListScreen(
                    numberOfItems = 1,
                    menuItems = emptyList(),
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Loading,
                    function = RecommendationListScreenFunction(),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_CIRCULAR_PROGRESS_BAR)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("1 item")
            .assertDoesNotExist()
    }

    @Test
    fun progressBarIsNotShownIsShownIfRecommendationIsASuccess() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationListScreen(
                    numberOfItems = 1,
                    menuItems = emptyList(),
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(emptyList()),
                    function = RecommendationListScreenFunction(),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_CIRCULAR_PROGRESS_BAR)
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("1 item")
            .assertDoesNotExist()
    }

    @Test
    fun progressBarIsNotShownIsShownIfRecommendationIsAnError() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationListScreen(
                    numberOfItems = 1,
                    menuItems = emptyList(),
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Error("An error occurred."),
                    function = RecommendationListScreenFunction(),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_CIRCULAR_PROGRESS_BAR)
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("1 item")
            .assertDoesNotExist()
    }

    @Test
    fun errorFromTaskResultIsShownIfOneWasThrown() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationListScreen(
                    numberOfItems = 1,
                    menuItems = emptyList(),
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Error("An error occurred."),
                    function = RecommendationListScreenFunction(),
                )
            }
        }

        composeTestRule.onNodeWithText("An error occurred.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("1 item")
            .assertDoesNotExist()
    }

    @Test
    fun listOfShopRecommendationsAreShownIfTaskResultIsASuccessWithNonEmptyList() {
        val recommendations = listOf(
            Recommendation(
                shop = Shop.default().copy(isDefault = false, id = 1),
                hit = Recommendation.Hit(
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
                    count = 2,
                ),
                miss = Recommendation.Miss(
                    items = listOf("Sugar"),
                    count = 1,
                ),
                expenditure = Recommendation.Expenditure(
                    unit = 100f,
                    total = 100f,
                ),
            ),
        )
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationListScreen(
                    showMap = false,
                    numberOfItems = 3,
                    menuItems = emptyList(),
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(recommendations),
                    function = RecommendationListScreenFunction(),
                )
            }
        }

        composeTestRule.onNodeWithTag(
            activity.getString(
                R.string.fr_recommendation_card_test_tag,
                0
            )
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("3 items")
            .assertIsDisplayed()
    }
}