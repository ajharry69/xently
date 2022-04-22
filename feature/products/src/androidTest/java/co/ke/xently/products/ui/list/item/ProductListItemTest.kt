package co.ke.xently.products.ui.list.item

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import co.ke.xently.data.Product
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.products.R
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock

class ProductListItemTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity by lazy { composeTestRule.activity }

    @Test
    fun productDetailsForADefaultProduct() {
        val product = Product.default()
        composeTestRule.setContent {
            XentlyTheme {
                ProductListItem(modifier = Modifier.fillMaxWidth(), product = product)
            }
        }

        composeTestRule.onNodeWithText(product.name).assertDoesNotExist()
    }

    @Test
    fun productDetailsForANonDefaultProduct() {
        val product = Product.default().copy(
            isDefault = false,
            id = 1,
            name = "Bread",
            unit = "grams",
            unitQuantity = 400f,
            unitPrice = 55f,
        )
        composeTestRule.setContent {
            XentlyTheme {
                ProductListItem(modifier = Modifier.fillMaxWidth(), product = product)
            }
        }

        composeTestRule.onNodeWithText(product.name, substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(product.unit, substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("400", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("55", substring = true).assertIsDisplayed()
    }

    @Test
    fun clickingOnProductListItemForDefaultProduct() {
        val onItemClickMock: (Product) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                ProductListItem(
                    modifier = Modifier.fillMaxWidth(),
                    product = Product.default(),
                    function = ProductListItemFunction(onItemClicked = onItemClickMock)
                )
            }
        }

        composeTestRule.onNodeWithTag(PRODUCT_LIST_ITEM_CONTAINER).performClick()
        with(argumentCaptor<Product> { }) {
            verify(onItemClickMock, never()).invoke(capture())
        }
    }

    @Test
    fun clickingOnProductListItemNonDefaultProduct() {
        val onItemClickMock: (Product) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                ProductListItem(
                    modifier = Modifier.fillMaxWidth(),
                    product = Product.default().copy(id = 1, isDefault = false),
                    function = ProductListItemFunction(onItemClicked = onItemClickMock)
                )
            }
        }

        composeTestRule.onNodeWithTag(PRODUCT_LIST_ITEM_CONTAINER).performClick()
        with(argumentCaptor<Product> { }) {
            verify(onItemClickMock).invoke(capture())
            assertThat(firstValue.id, equalTo(1))
        }
    }

    @Test
    fun clickingOnMoreOptionsForDefaultProduct() {
        val product = Product.default()
        composeTestRule.setContent {
            XentlyTheme {
                ProductListItem(
                    modifier = Modifier.fillMaxWidth(),
                    product = product,
                    menuItems = listOf(MenuItem(R.string.update)),
                )
            }
        }

        composeTestRule.onNodeWithTag(
            activity.getString(R.string.fp_product_item_menu_content_description, product.name)
        ).performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.update)).assertDoesNotExist()
    }

    @Test
    fun clickingOnMoreOptionsNonDefaultProduct() {
        val onUpdateClickedMock: (Product) -> Unit = mock()
        val product = Product.default().copy(id = 1, isDefault = false)
        composeTestRule.setContent {
            XentlyTheme {
                ProductListItem(
                    modifier = Modifier.fillMaxWidth(),
                    product = product,
                    menuItems = listOf(MenuItem(R.string.update, onUpdateClickedMock)),
                )
            }
        }

        composeTestRule.onNodeWithTag(
            activity.getString(R.string.fp_product_item_menu_content_description, product.name)
        ).performClick()
        composeTestRule.onNodeWithText(activity.getString(R.string.update)).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.update)).performClick()
        with(argumentCaptor<Product> { }) {
            verify(onUpdateClickedMock).invoke(capture())
            assertThat(firstValue.id, equalTo(product.id))
        }
    }
}