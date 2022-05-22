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
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
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
                    modifier = Modifier.fillMaxSize(),
                    numberOfItems = 1,
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
    fun directionsClick() {
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
        val onDirectionsClickMock: (Recommendation) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationListScreen(
                    modifier = Modifier.fillMaxSize(),
                    numberOfItems = 1,
                    showMap = false,
                    result = TaskResult.Success(recommendations),
                    function = RecommendationListScreenFunction(onDirectionClick = onDirectionsClickMock),
                )
            }
        }

        composeTestRule.onNodeWithTag(
            activity.getString(
                R.string.fr_item_menu_content_description,
                recommendations[0].shop.descriptiveName,
            )
        ).performClick()

        composeTestRule.onNodeWithText(activity.getString(R.string.fr_directions))
            .performClick()

        with(argumentCaptor<Recommendation>()) {
            verify(onDirectionsClickMock, times(1)).invoke(capture())
            assertThat(firstValue, equalTo(recommendations[0]))
        }
    }

    @Test
    fun toolbarTitle() {
        composeTestRule.setContent {
            XentlyTheme {
                RecommendationListScreen(
                    modifier = Modifier.fillMaxSize(),
                    numberOfItems = 1,
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
                    modifier = Modifier.fillMaxSize(),
                    numberOfItems = 1,
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
                    modifier = Modifier.fillMaxSize(),
                    numberOfItems = 1,
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
                    modifier = Modifier.fillMaxSize(),
                    numberOfItems = 1,
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
                    modifier = Modifier.fillMaxSize(),
                    numberOfItems = 1,
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
                    modifier = Modifier.fillMaxSize(),
                    numberOfItems = 1,
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
                    modifier = Modifier.fillMaxSize(),
                    numberOfItems = 3,
                    result = TaskResult.Success(recommendations),
                    function = RecommendationListScreenFunction(),
                    showMap = false,
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

    @Test
    fun clickingOnShoppingListItemDetailsPopupMenu() {
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
                    modifier = Modifier.fillMaxSize(),
                    numberOfItems = 3,
                    result = TaskResult.Success(recommendations),
                    function = RecommendationListScreenFunction(),
                    showMap = false,
                )
            }
        }

        composeTestRule.onNodeWithTag(
            activity.getString(
                R.string.fr_item_menu_content_description,
                recommendations[0].shop.descriptiveName,
            )
        ).performClick()

        // Before clicking on details menu item...
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_shop_details))
            .assertDoesNotExist()
        composeTestRule.onAllNodesWithText(recommendations[0].shop.descriptiveName)
            .assertCountEquals(1)

        composeTestRule.onNodeWithText(activity.getString(R.string.fr_details))
            .performClick()

        // After clicking on details menu item...
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_shop_details))
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithText(recommendations[0].shop.descriptiveName)
            .assertCountEquals(2)
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_detail_hit_heading))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_detail_miss_heading))
            .assertIsDisplayed()

        // After hiding the modal...
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.hide))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_detail_hit_heading))
            .assertIsNotDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_detail_miss_heading))
            .assertIsNotDisplayed()
    }

    @Test
    fun clickingOnShoppingListItem() {
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
                    modifier = Modifier.fillMaxSize(),
                    numberOfItems = 3,
                    result = TaskResult.Success(recommendations),
                    function = RecommendationListScreenFunction(),
                    showMap = false,
                )
            }
        }

        // Before clicking...
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_shop_details))
            .assertDoesNotExist()
        composeTestRule.onAllNodesWithText(recommendations[0].shop.descriptiveName)
            .assertCountEquals(1)

        composeTestRule.onNodeWithTag(
            activity.getString(R.string.fr_recommendation_card_test_tag, 0)
        ).performClick()

        // After clicking...
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_shop_details))
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithText(recommendations[0].shop.descriptiveName)
            .assertCountEquals(2)
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_detail_hit_heading))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_detail_miss_heading))
            .assertIsDisplayed()

        // After hiding the modal...
        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.hide))
            .performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_detail_hit_heading))
            .assertIsNotDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.fr_detail_miss_heading))
            .assertIsNotDisplayed()
    }
}