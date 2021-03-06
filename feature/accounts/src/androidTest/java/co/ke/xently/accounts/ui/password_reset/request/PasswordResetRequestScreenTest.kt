package co.ke.xently.accounts.ui.password_reset.request

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import co.ke.xently.accounts.R
import co.ke.xently.common.KENYA
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.TEST_TAG_TEXT_FIELD_ERROR
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class PasswordResetRequestScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity by lazy { composeTestRule.activity }

    private val emailTextFieldDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fa_request_password_reset_email_label)
        )
    }
    private val requestResetToolbarTitle by lazy {
        activity.getString(R.string.fa_request_password_reset_toolbar_title)
    }
    private val progressbarDescription by lazy {
        activity.getString(R.string.progress_bar_content_description)
    }

    @Test
    fun clickingOnNavigationIcon() {
        val navigationIconClickMock: () -> Unit = mock()

        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetRequestScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = PasswordResetRequestScreenFunction(
                        sharedFunction = SharedFunction(
                            onNavigationIconClicked = navigationIconClickMock,
                        ),
                    ),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.move_back))
            .performClick()
        verify(navigationIconClickMock, times(1)).invoke()
    }

    @Test
    fun toolbarTitle() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetRequestScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithText(requestResetToolbarTitle)
            .assertIsDisplayed()
            .assertHasNoClickAction()
    }

    @Test
    fun resetRequestButtonLabel() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetRequestScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithText(requestResetToolbarTitle.uppercase(KENYA))
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun progressBarIsNotShownOnSuccessTaskResult() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetRequestScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertDoesNotExist()
    }

    @Test
    fun progressBarIsNotShownOnErrorTaskResult() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetRequestScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Error("An error was encountered"),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertDoesNotExist()
    }

    @Test
    fun resetRequestButtonIsPresent() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetRequestScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }
        composeTestRule.onNodeWithText(requestResetToolbarTitle.uppercase(KENYA)).assertIsDisplayed()
    }

    @Test
    fun emailFieldDefaultsToEmptyString() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetRequestScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }
        composeTestRule.onNodeWithContentDescription(emailTextFieldDescription)
            .assert(hasText(""))
    }

    @Test
    fun emailFieldDefaultCanBeOverridden() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetRequestScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    email = "user@example.com",
                )
            }
        }
        composeTestRule.onNodeWithContentDescription(emailTextFieldDescription)
            .assert(hasText("user@example.com"))
    }

    @Test
    fun requestResetButtonIsDisabledIfEitherEmailIsBlank() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetRequestScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithText(requestResetToolbarTitle.uppercase(KENYA)).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(emailTextFieldDescription)
            .performTextInput("user@example.com")
        composeTestRule.onNodeWithText(requestResetToolbarTitle.uppercase(KENYA)).assertIsEnabled()
    }

    @Test
    fun loadingTaskDisablesResetRequestButtonAndShowsProgressBar() {
        val email = "user@example.org"
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetRequestScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult,
                    email = email,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(requestResetToolbarTitle.uppercase(KENYA)).assertIsNotEnabled()
    }

    @Test
    fun successTaskResultWithNonNullDataCallsResetRequestSuccessFunction() {
        val requestSuccessCallback: (User) -> Unit = mock()
        val user = User.default().copy(id = 1, email = "user@example.com")
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetRequestScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(user),
                    function = PasswordResetRequestScreenFunction(requestSuccess = requestSuccessCallback),
                )
            }
        }

        with(argumentCaptor<User> { }) {
            verify(requestSuccessCallback, times(1)).invoke(capture())
            assertThat(firstValue.id, equalTo(1))
            assertThat(firstValue.email, equalTo("user@example.com"))
        }
    }

    @Test
    @Ignore("Research on correct implementation")
    fun clickingOnResetRequestButtonHidesKeyboardThroughFocusManager() {
        val focusManagerMock: FocusManager = mock()
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetRequestScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    email = "user@example.org",
                )
            }
        }

        composeTestRule.onNodeWithText(requestResetToolbarTitle.uppercase(KENYA)).performClick()
        with(argumentCaptor<Boolean> { }) {
            verify(focusManagerMock).clearFocus(capture())
            assertThat(firstValue, `is`(false))
        }
    }

    @Test
    fun clickingOnResetRequestButtonCallsPasswordResetRequestFunctionWithRequiredArguments() {
        val requestCallbackMock: (String) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetRequestScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = PasswordResetRequestScreenFunction(request = requestCallbackMock),
                )
            }
        }

        val email = "user@example.com"
        composeTestRule.onNodeWithContentDescription(emailTextFieldDescription)
            .performTextInput(email)

        composeTestRule.onNodeWithText(requestResetToolbarTitle.uppercase(KENYA)).performClick()
        with(argumentCaptor<String> { }) {
            verify(requestCallbackMock, times(1)).invoke(capture())
            assertThat(firstValue, equalTo(email))
        }
    }

    @Test
    fun taskResultWithErrorOnEmailFieldShowsErrorCaptionBelowEmailField() {
        val errorMessage = "User with the email no found"
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetRequestScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Error(
                        PasswordResetRequestHttpException(
                            email = listOf(
                                errorMessage
                            )
                        )
                    ),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_ERROR).assertIsDisplayed()
            .assert(hasText(errorMessage))
    }

    @Test
    fun clickingResetRequestButtonTrimSpacesFromStartAndEndOfTextInputs() {
        val requestCallbackMock: (String) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetRequestScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = PasswordResetRequestScreenFunction(request = requestCallbackMock),
                )
            }
        }

        val email = "   user@example.com   "
        composeTestRule.onNodeWithContentDescription(emailTextFieldDescription)
            .performTextInput(email)

        composeTestRule.onNodeWithText(requestResetToolbarTitle.uppercase(KENYA)).performClick()
        with(argumentCaptor<String> { }) {
            verify(requestCallbackMock, times(1)).invoke(capture())
            assertThat(firstValue, equalTo("user@example.com"))
        }
    }
}