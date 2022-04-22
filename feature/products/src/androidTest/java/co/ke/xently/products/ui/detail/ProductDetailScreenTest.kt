package co.ke.xently.products.ui.detail

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import co.ke.xently.data.Product
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.TEST_TAG_AUTOCOMPLETE_TEXT_FIELD_SUGGESTIONS
import co.ke.xently.feature.ui.TEST_TAG_TEXT_FIELD_ERROR
import co.ke.xently.products.R
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.mockito.internal.verification.VerificationModeFactory.atMostOnce
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ProductDetailScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity by lazy { composeTestRule.activity }
    private val progressbarDescription by lazy {
        activity.getString(R.string.progress_bar_content_description)
    }
    private val addProductButtonLabel by lazy {
        activity.getString(R.string.fp_detail_toolbar_title, activity.getString(R.string.add))
    }
    private val updateProductButtonLabel by lazy {
        activity.getString(R.string.fp_detail_toolbar_title, activity.getString(R.string.update))
    }
    private val shopDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fp_product_detail_shop_label),
        )
    }
    private val addShopDescription by lazy {
        activity.getString(R.string.fp_product_detail_shop_add_icon_description)
    }
    private val measurementUnitDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fsp_product_detail_unit_label),
        )
    }
    private val nameDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fsp_product_detail_name_label),
        )
    }
    private val unitQuantityDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fsp_product_detail_unit_quantity_label),
        )
    }
    private val unitPriceDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fp_product_detail_unit_price_label),
        )
    }
    private val purchasedQuantityDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fp_product_detail_purchased_quantity_label),
        )
    }
    private val brandsDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fsp_product_detail_brand_query_label),
        )
    }
    private val addBrandDescription by lazy {
        activity.getString(R.string.fsp_product_detail_brand_add_icon_description)
    }

    @Test
    fun clickingOnNavigationIcon() {
        val onNavigationIconClickMock: () -> Unit = mock()

        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    addResult = TaskResult.Success(null),
                    function = ProductDetailScreenFunction(onNavigationIconClicked = onNavigationIconClickMock),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.move_back))
            .performClick()
        verify(onNavigationIconClickMock, atMostOnce()).invoke()
    }

    @Test
    fun toolbarTitleIsSameAsAddButtonLabelWhenAddingProduct() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onAllNodesWithText(addProductButtonLabel, ignoreCase = true)
            .assertCountEquals(2)
    }

    @Test
    fun toolbarTitleIsSameAsUpdateButtonLabelWhenUpdatingProduct() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(isDefault = false, name = "Bread")
                    ),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onAllNodesWithText(updateProductButtonLabel, ignoreCase = true)
            .assertCountEquals(2)
    }

    @Test
    fun progressBarIsNotShownOnSuccessTaskResult() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertDoesNotExist()
    }

    @Test
    fun progressBarIsNotShownOnErrorTaskResult() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Error("An error was encountered"),
                    addResult = TaskResult.Error("An error was encountered"),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertDoesNotExist()
    }

    @Test
    fun progressBarIsShownOnLoadingForGetResult() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Loading,
                    addResult = TaskResult.Error("An error was encountered"),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertIsDisplayed()
    }

    @Test
    fun progressBarIsShownOnLoadingForAddResult() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Error("An error was encountered"),
                    addResult = TaskResult.Loading,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertIsDisplayed()
    }

    @Test
    fun addOrUpdateProductButtonIsDisabledByDefault() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_PRODUCT_DETAIL_BODY_CONTAINER)
            .performScrollToNode(hasText(addProductButtonLabel.uppercase()))
        composeTestRule.onNodeWithText(addProductButtonLabel.uppercase()).assertIsNotEnabled()
    }

    @Test
    fun shopFieldDefaultsToBlankWhenAddingNewProduct() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(shopDescription).assert(hasText(""))
    }

    @Test
    fun shopFieldDefaultsToBlankWhenUpdatingProduct() {
        val shop = Shop.default().copy(id = 1, name = "Quickmart", taxPin = "P051188806D")
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            shop = shop,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(shopDescription)
            .assert(hasText(shop.toString()))
    }

    @Test
    fun typingOnShopFieldWhenShopsSuggestionsAreEmptyShowsAnAddShopButton() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(shopDescription).run {
            performTextClearance()
            performTextInput("Name of new shop")
        }
        composeTestRule.onNodeWithContentDescription(addShopDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(activity.getString(R.string.fp_product_detail_shop_query_help_text))
            .assertIsDisplayed()
    }

    @Test
    fun typingOnShopFieldWhenShopsSuggestionsAreNotEmpty() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Success(null),
                    shopSuggestions = listOf(Shop.default().copy(name = "Naivas", id = 1)),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(shopDescription).run {
            performTextClearance()
            performTextInput("Nai")
        }
        composeTestRule.onNodeWithTag(TEST_TAG_AUTOCOMPLETE_TEXT_FIELD_SUGGESTIONS)
            .performScrollToNode(hasText("Naivas", substring = true)).performClick()
        composeTestRule.onNodeWithContentDescription(addShopDescription).assertDoesNotExist()
        composeTestRule.onNodeWithText(activity.getString(R.string.fp_product_detail_shop_query_help_text))
            .assertDoesNotExist()
    }

    @Test
    fun taskResultWithErrorOnShopFieldShowsErrorCaptionBelowShopField() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Error(ProductHttpException(shop = listOf("Invalid shop"))),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_ERROR).assertIsDisplayed()
            .assert(hasText("Invalid shop"))
    }

    @Test
    fun clickingOnAddShopButton() {
        val addShopCallbackMock: (String) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Success(null),
                    function = ProductDetailScreenFunction(onAddNewShop = addShopCallbackMock),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(shopDescription).run {
            performTextClearance()
            performTextInput("Name of new shop")
        }
        composeTestRule.onNodeWithContentDescription(addShopDescription).performClick()
        with(argumentCaptor<String> { }) {
            verify(addShopCallbackMock, atMostOnce()).invoke(capture())
            assertThat(firstValue, equalTo("Name of new shop"))
        }
    }

    @Test
    fun measurementUnitFieldDefaultsToBlankWhenAddingNewProduct() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(measurementUnitDescription).assert(hasText(""))
    }

    @Test
    fun measurementUnitFieldDefaultsToBlankWhenUpdatingProduct() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            unit = "grams",
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(measurementUnitDescription)
            .assert(hasText("grams"))
    }

    @Test
    fun taskResultWithErrorOnMeasurementUnitFieldShowsErrorCaptionBelowMeasurementUnitField() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Error(ProductHttpException(unit = listOf("Invalid unit"))),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_ERROR).assertIsDisplayed()
            .assert(hasText("Invalid unit"))
    }

    @Test
    fun nameFieldDefaultsToBlankWhenAddingNewProduct() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(nameDescription).assert(hasText(""))
    }

    @Test
    fun nameFieldDefaultsToBlankWhenUpdatingProduct() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            name = "Bread",
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(nameDescription)
            .assert(hasText("Bread"))
    }

    @Test
    fun taskResultWithErrorOnNameFieldShowsErrorCaptionBelowNameField() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Error(ProductHttpException(name = listOf("An error in product's name"))),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_ERROR).assertIsDisplayed()
            .assert(hasText("An error in product's name"))
    }

    @Test
    fun unitQuantityFieldDefaultsToBlankWhenAddingNewProduct() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(unitQuantityDescription).assert(hasText("1"))
    }

    @Test
    fun unitQuantityFieldDefaultsToBlankWhenUpdatingProduct() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            unitQuantity = 1.25f,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(unitQuantityDescription)
            .assert(hasText("1.25"))
    }

    @Test
    fun taskResultWithErrorOnUnitQuantityFieldShowsErrorCaptionBelowUnitQuantityField() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Error(ProductHttpException(unitQuantity = listOf("Negative value is not allowed"))),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_ERROR).assertIsDisplayed()
            .assert(hasText("Negative value is not allowed"))
    }

    @Test
    fun unitPriceFieldDefaultsToBlankWhenAddingNewProduct() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(unitPriceDescription).assert(hasText(""))
    }

    @Test
    fun unitPriceFieldDefaultsToBlankWhenUpdatingProduct() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            unitPrice = 1.25f,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(unitPriceDescription)
            .assert(hasText("1.25"))
    }

    @Test
    fun taskResultWithErrorOnUnitPriceFieldShowsErrorCaptionBelowUnitPriceField() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Error(ProductHttpException(unitPrice = listOf("Negative values are not allowed"))),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_ERROR).assertIsDisplayed()
            .assert(hasText("Negative values are not allowed"))
    }

    @Test
    fun purchasedQuantityFieldDefaultsToBlankWhenAddingNewProduct() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(purchasedQuantityDescription)
            .assert(hasText("1"))
    }

    @Test
    fun purchasedQuantityFieldDefaultsToBlankWhenUpdatingProduct() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            purchasedQuantity = 12f,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(purchasedQuantityDescription)
            .assert(hasText("12"))
    }

    @Test
    fun taskResultWithErrorOnPurchaseQuantityFieldShowsErrorCaptionBelowPurchaseQuantityField() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Error(ProductHttpException(purchasedQuantity = listOf("Negative values not allowed"))),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_ERROR).assertIsDisplayed()
            .assert(hasText("Negative values not allowed"))
    }

    @Test
    fun brandsFieldDefaultsToBlankWhenAddingNewProduct() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(brandsDescription)
            .assert(hasText(""))
        composeTestRule.onNodeWithContentDescription(addBrandDescription).assertDoesNotExist()
    }

    @Test
    fun brandsFieldDefaultsToBlankWhenUpdatingProduct() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            isDefault = false,
                            brands = listOf(Product.Brand.default().copy("Bidco")),
                        )
                    ),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(brandsDescription)
            .assert(hasText(""))
        composeTestRule.onNodeWithContentDescription(addBrandDescription).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TEST_TAG_PRODUCT_DETAIL_BODY_CONTAINER)
            .performScrollToNode(hasText("Bidco")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_PRODUCT_DETAIL_BODY_CONTAINER)
            .performScrollToNode(hasText(activity.getString(R.string.fsp_product_detail_brands_title)))
            .assertIsDisplayed()
    }

    @Test
    fun typingOnBrandsFieldWhenBrandsSuggestionsAreEmptyShowsAnAddBrandButton() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_PRODUCT_DETAIL_BODY_CONTAINER)
            .performScrollToNode(hasContentDescription(brandsDescription))
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(brandsDescription).run {
            performTextClearance()
            performTextInput("Bidco")
        }
        composeTestRule.onNodeWithContentDescription(addBrandDescription).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_PRODUCT_DETAIL_BODY_CONTAINER)
            .performScrollToNode(hasText(activity.getString(R.string.fsp_product_detail_brand_query_help_text)))
        composeTestRule.onNodeWithText(activity.getString(R.string.fsp_product_detail_brand_query_help_text))
            .assertIsDisplayed()
    }

    @Test
    fun typingOnBrandFieldWhenBrandsSuggestionsAreNotEmpty() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Success(null),
                    brandSuggestions = listOf(Product.Brand.default().copy(name = "Bidco")),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_PRODUCT_DETAIL_BODY_CONTAINER)
            .performScrollToNode(hasContentDescription(brandsDescription))
        composeTestRule.onNodeWithContentDescription(brandsDescription).run {
            performTextClearance()
            performTextInput("Bid")
        }
        composeTestRule.onNodeWithTag(TEST_TAG_AUTOCOMPLETE_TEXT_FIELD_SUGGESTIONS)
            .performScrollToNode(hasText("Bidco", substring = true)).performClick()
        composeTestRule.onNodeWithContentDescription(addBrandDescription).assertDoesNotExist()
        composeTestRule.onNodeWithText(activity.getString(R.string.fsp_product_detail_brand_query_help_text))
            .assertDoesNotExist()
    }

    @Test
    fun clickingOnAddBrandButton() {
        composeTestRule.setContent {
            XentlyTheme {
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(
                        Product.default().copy(
                            id = 1,
                            isDefault = false,
                        )
                    ),
                    addResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_PRODUCT_DETAIL_BODY_CONTAINER)
            .performScrollToNode(hasContentDescription(brandsDescription)).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(brandsDescription).run {
            performTextClearance()
            performTextInput("Bidco")
        }
        composeTestRule.onNodeWithContentDescription(addBrandDescription).performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_PRODUCT_DETAIL_BODY_CONTAINER)
            .performScrollToNode(hasText(activity.getString(R.string.fsp_product_detail_brands_title)))
        composeTestRule.onNodeWithTag(TEST_TAG_PRODUCT_DETAIL_BODY_CONTAINER)
            .performScrollToNode(hasText("Bidco")).assertExists()
        composeTestRule.onNodeWithText("Bidco").assertIsDisplayed()
    }

}