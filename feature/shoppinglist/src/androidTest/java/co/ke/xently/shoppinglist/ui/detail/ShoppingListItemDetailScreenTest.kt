package co.ke.xently.shoppinglist.ui.detail

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import co.ke.xently.common.KENYA
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.shoppinglist.R
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.mockito.internal.verification.VerificationModeFactory.times
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ShoppingListItemDetailScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private val activity by lazy { composeTestRule.activity }
    private val addShoppingListItemLabel by lazy {
        activity.getString(
            R.string.fsl_detail_screen_toolbar_title,
            activity.getString(R.string.add),
        ).uppercase(KENYA)
    }
    private val updateShoppingListItemLabel by lazy {
        activity.getString(
            R.string.fsl_detail_screen_toolbar_title,
            activity.getString(R.string.update),
        ).uppercase(KENYA)
    }
    private val nameTextFieldDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fsp_product_detail_name_label),
        )
    }
    private val unitTextFieldDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fsp_product_detail_unit_label),
        )
    }
    private val unitQuantityTextFieldDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fsp_product_detail_unit_quantity_label),
        )
    }
    private val purchaseQuantityTextFieldDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fsl_text_field_label_purchase_quantity),
        )
    }

    @Test
    fun clickingOnNavigationButton() {
        val onNavigationIconClickedMock: () -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                ShoppingListItemScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    addResult = TaskResult.Success(null),
                    function = ShoppingListItemScreenFunction(
                        sharedFunction = SharedFunction(
                            onNavigationIconClicked = onNavigationIconClickedMock,
                        ),
                    ),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.move_back))
            .performClick()
        verify(onNavigationIconClickedMock, org.mockito.kotlin.times(1)).invoke()
    }

    @Test
    fun clickingOnAddShoppingListItemButtonTrimSpacesFromStartAndEndOfTextInputs() {
        val onDetailsSubmittedMock: (ShoppingListItem) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                ShoppingListItemScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    addResult = TaskResult.Success(null),
                    function = ShoppingListItemScreenFunction(onDetailsSubmitted = onDetailsSubmittedMock),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(nameTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithContentDescription(nameTextFieldDescription)
            .performTextInput(" Bread   ")

        composeTestRule.onNodeWithContentDescription(unitTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithContentDescription(unitTextFieldDescription)
            .performTextInput(" grams   ")

        composeTestRule.onNodeWithContentDescription(unitQuantityTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithContentDescription(unitQuantityTextFieldDescription)
            .performTextInput(" 1   ")

        composeTestRule.onNodeWithContentDescription(purchaseQuantityTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithContentDescription(purchaseQuantityTextFieldDescription)
            .performTextInput(" 1   ")

        composeTestRule.onNodeWithText(addShoppingListItemLabel).performClick()
        with(argumentCaptor<ShoppingListItem> { }) {
            verify(onDetailsSubmittedMock, times(1)).invoke(capture())
            assertThat(firstValue.name, equalTo("Bread"))
            assertThat(firstValue.unit, equalTo("grams"))
            assertThat(firstValue.unitQuantity, equalTo(1f))
            assertThat(firstValue.purchaseQuantity, equalTo(1f))
        }
    }

    @Test
    fun clickingOnUpdateShoppingListItemButtonTrimSpacesFromStartAndEndOfTextInputs() {
        val onDetailsSubmittedMock: (ShoppingListItem) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                ShoppingListItemScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        ShoppingListItem.default()
                            .copy(isDefault = false, id = 1)
                    ),
                    addResult = TaskResult.Success(null),
                    function = ShoppingListItemScreenFunction(onDetailsSubmitted = onDetailsSubmittedMock),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(nameTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithContentDescription(nameTextFieldDescription)
            .performTextInput(" Bread   ")

        composeTestRule.onNodeWithContentDescription(unitTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithContentDescription(unitTextFieldDescription)
            .performTextInput(" grams   ")

        composeTestRule.onNodeWithContentDescription(unitQuantityTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithContentDescription(unitQuantityTextFieldDescription)
            .performTextInput(" 1   ")

        composeTestRule.onNodeWithContentDescription(purchaseQuantityTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithContentDescription(purchaseQuantityTextFieldDescription)
            .performTextInput(" 1   ")

        composeTestRule.onNodeWithText(updateShoppingListItemLabel).performClick()
        with(argumentCaptor<ShoppingListItem> { }) {
            verify(onDetailsSubmittedMock, times(1)).invoke(capture())
            assertThat(firstValue.name, equalTo("Bread"))
            assertThat(firstValue.unit, equalTo("grams"))
            assertThat(firstValue.unitQuantity, equalTo(1f))
            assertThat(firstValue.purchaseQuantity, equalTo(1f))
        }
    }
}