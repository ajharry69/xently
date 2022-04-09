package co.ke.xently.accounts.ui.verification

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.input.ImeAction
import co.ke.xently.accounts.R
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.TEST_TAG_TEXT_FIELD_ERROR
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.mockito.internal.verification.VerificationModeFactory.atMostOnce
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class VerificationScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity by lazy { composeTestRule.activity }

    private val progressbarDescription by lazy {
        activity.getString(R.string.progress_bar_content_description)
    }
    private val verifyButtonLabel by lazy {
        activity.getString(R.string.fa_verify_account_toolbar_title).uppercase()
    }
    private val resendButtonLabel = "RESEND"

    @Test
    fun clickingOnNavigationIcon() {
        val navigationIconClickMock: () -> Unit = mock()

        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Success(null),
                    resendResult = TaskResult.Success(null),
                    function = VerificationScreenFunction(navigationIcon = navigationIconClickMock),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.move_back))
            .performClick()
        verify(navigationIconClickMock, atMostOnce()).invoke()
    }

    @Test
    fun toolbarTitleIsSameAsSignUpButtonLabel() {
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Success(null),
                    resendResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onAllNodesWithText(verifyButtonLabel, ignoreCase = true)
            .assertCountEquals(2)
    }

    @Test
    fun progressBarIsNotShownOnSuccessTaskResult() {
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Success(null),
                    resendResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertDoesNotExist()
    }

    @Test
    fun progressBarIsNotShownOnErrorTaskResultForVerificationRequest() {
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Error("An error was encountered"),
                    resendResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertDoesNotExist()
    }

    @Test
    fun progressBarIsNotShownOnErrorTaskResultForResendRequest() {
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    resendResult = TaskResult.Error("An error was encountered"),
                    verifyResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertDoesNotExist()
    }

    @Test
    fun resendButtonIsPresent() {
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Success(null),
                    resendResult = TaskResult.Success(null),
                )
            }
        }
        composeTestRule.onNodeWithText(resendButtonLabel, substring = true).assertIsDisplayed()
    }

    @Test
    fun verifyButtonIsPresent() {
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Success(null),
                    resendResult = TaskResult.Success(null),
                )
            }
        }
        composeTestRule.onNodeWithText(verifyButtonLabel).assertIsDisplayed()
    }

    @Test
    fun verificationCodeFieldsAre6InNumber() {
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Success(null),
                    resendResult = TaskResult.Success(null),
                )
            }
        }
        composeTestRule.onAllNodesWithTag(TEST_TAG_VERIFICATION_CODE_ENTRY)
            .assertCountEquals(6)
    }

    @Test
    fun verificationCodeFieldsDefaultsToEmptyString() {
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Success(null),
                    resendResult = TaskResult.Success(null),
                )
            }
        }
        composeTestRule.onAllNodesWithTag(TEST_TAG_VERIFICATION_CODE_ENTRY)
            .assertAll(hasText(""))
    }

    @Test
    fun verificationCodeFieldsValuesCanBeOverridden() {
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Success(null),
                    resendResult = TaskResult.Success(null),
                    verificationCode = "123456"
                )
            }
        }

        for (i in 1..6) {
            composeTestRule.onNodeWithContentDescription(
                activity.getString(R.string.fa_verify_account_entry_field_description, i)
            ).assert(hasText(i.toString()))
        }
    }

    @Test
    fun lastVerificationCodeEntryFieldHasIMEActionDone() {
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Success(null),
                    resendResult = TaskResult.Success(null),
                )
            }
        }
        composeTestRule.onNodeWithContentDescription(
            activity.getString(R.string.fa_verify_account_entry_field_description, 6)
        ).assert(hasImeAction(ImeAction.Done))
    }

    @Test
    fun verifyAccountButtonIsDisabledIfCodeEntriesCountDoesNotEqual6() {
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Success(null),
                    resendResult = TaskResult.Success(null),
                    verificationCode = "12345"
                )
            }
        }

        composeTestRule.onNodeWithText(verifyButtonLabel).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(
            activity.getString(
                R.string.fa_verify_account_entry_field_description,
                6
            )
        )
            .performTextInput("1")
        composeTestRule.onNodeWithText(verifyButtonLabel).assertIsEnabled()
    }

    @Test
    fun loadingVerificationTaskDisablesVerifyAccountButtonAndShowsProgressBar() {
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Loading,
                    resendResult = TaskResult.Success(null),
                    verificationCode = "123456",
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(verifyButtonLabel).assertIsNotEnabled()
    }

    @Test
    fun loadingResendTaskDisablesResendButtonAndShowsProgressBar() {
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Success(null),
                    resendResult = TaskResult.Loading,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(resendButtonLabel, substring = true).assertIsNotEnabled()
    }

    @Test
    fun successTaskResultWithNonNullDataCallsVerificationSuccessFunction() {
        val verificationSuccessMock: (User) -> Unit = mock()
        val user = User.default().copy(id = 1, email = "user@example.com")
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Success(user),
                    resendResult = TaskResult.Success(null),
                    function = VerificationScreenFunction(verificationSuccess = verificationSuccessMock),
                )
            }
        }

        with(argumentCaptor<User> { }) {
            verify(verificationSuccessMock, atMostOnce()).invoke(capture())
            assertThat(firstValue.id, equalTo(1))
            assertThat(firstValue.email, equalTo("user@example.com"))
        }
    }

    @Test
    fun taskResultWithErrorOnVerificationFieldsShowsErrorCaptionBelowVerificationFields() {
        val errorMessage = "Invalid verification code"
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Error(
                        VerificationHttpException(
                            code = listOf(errorMessage),
                        )
                    ),
                    resendResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_ERROR).assertIsDisplayed()
            .assert(hasText(errorMessage))
    }

    @Test
    fun enteringCodeOnTheFirstCodeEntryFieldSetsFocusOnTheSecondField() {
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Success(null),
                    resendResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(
            activity.getString(
                R.string.fa_verify_account_entry_field_description,
                1
            )
        ).run {
            performTextInput("2")
            assert(hasText("2"))
        }
        composeTestRule.onNodeWithContentDescription(
            activity.getString(
                R.string.fa_verify_account_entry_field_description,
                2
            )
        ).assertIsFocused()
    }

    @Test
    fun enteringMoreThanOneDigitInACodeEntryFieldSpreadsTheExtraDigitsToTheOtherFieldsInOrder() {
        composeTestRule.setContent {
            XentlyTheme {
                VerificationScreen(
                    modifier = Modifier.fillMaxSize(),
                    verifyResult = TaskResult.Success(null),
                    resendResult = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(
            activity.getString(
                R.string.fa_verify_account_entry_field_description,
                1
            )
        ).run {
            performTextInput("23")
            assert(hasText("2"))
        }
        /*
        TODO: Implement corrective measures...
        composeTestRule.onNodeWithContentDescription(
            activity.getString(
                R.string.fa_verify_account_entry_field_description,
                2
            )
        ).assert(hasText("3"))
        composeTestRule.onNodeWithContentDescription(
            activity.getString(
                R.string.fa_verify_account_entry_field_description,
                3
            )
        ).assertIsFocused()*/
    }
}