package co.ke.xently.accounts.ui.password_reset

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import co.ke.xently.accounts.R
import co.ke.xently.common.KENYA
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.TEST_TAG_TEXT_FIELD_ERROR
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class PasswordResetScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity by lazy { composeTestRule.activity }

    private val resetPasswordButtonLabel by lazy {
        activity.getString(
            R.string.fa_reset_password_toolbar_title,
            activity.getString(R.string.fa_reset)
        ).uppercase(KENYA)
    }

    private val changePasswordButtonLabel by lazy {
        activity.getString(
            R.string.fa_reset_password_toolbar_title,
            activity.getString(R.string.fa_change),
        ).uppercase(KENYA)
    }
    private val oldPasswordTextFieldDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fa_reset_password_old_password_label),
        )
    }
    private val newPasswordTextFieldDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fa_reset_password_new_password_label),
        )
    }
    private val progressbarDescription by lazy {
        activity.getString(R.string.progress_bar_content_description)
    }

    @Test
    fun clickingOnNavigationIcon() {
        val navigationIconClickMock: () -> Unit = mock()

        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = PasswordResetScreenFunction(navigationIcon = navigationIconClickMock),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(activity.getString(R.string.move_back))
            .performClick()
        verify(navigationIconClickMock, VerificationModeFactory.atMostOnce()).invoke()
    }

    @Test
    fun progressBarIsNotShownOnSuccessTaskResult() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
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
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Error("An error was encountered"),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertDoesNotExist()
    }

    @Test
    fun toolbarTitleIsSameAsResetPasswordButtonLabelForPasswordReset() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onAllNodesWithText(resetPasswordButtonLabel, ignoreCase = true)
            .assertCountEquals(2)
    }

    @Test
    fun toolbarTitleIsSameAsChangePasswordButtonLabelForPasswordChange() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    isChange = true,
                )
            }
        }

        composeTestRule.onAllNodesWithText(changePasswordButtonLabel, ignoreCase = true)
            .assertCountEquals(2)
    }

    @Test
    fun oldPasswordFieldsDefaultToEmptyString() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(oldPasswordTextFieldDescription)
            .assert(hasText(""))
    }

    @Test
    fun newPasswordFieldsDefaultToEmptyString() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(newPasswordTextFieldDescription)
            .assert(hasText(""))
    }

    @Test
    fun loadingTaskDisablesResetButtonAndShowsProgressBarForPasswordReset() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Loading,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(resetPasswordButtonLabel).assertIsNotEnabled()
    }

    @Test
    fun loadingTaskDisablesChangeButtonAndShowsProgressBarForPasswordChange() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Loading,
                    isChange = true,
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(changePasswordButtonLabel).assertIsNotEnabled()
    }

    @Test
    fun passwordResetButtonIsDisabledIfEitherUsernameOrPasswordIsBlankForPasswordReset() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithText(resetPasswordButtonLabel).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(oldPasswordTextFieldDescription)
            .performTextInput("old password")
        composeTestRule.onNodeWithText(resetPasswordButtonLabel).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(oldPasswordTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithText(resetPasswordButtonLabel).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(newPasswordTextFieldDescription)
            .performTextInput("new password")
        composeTestRule.onNodeWithText(resetPasswordButtonLabel).assertIsNotEnabled()
    }

    @Test
    fun passwordChangeButtonIsDisabledIfEitherUsernameOrPasswordIsBlankForPasswordChange() {
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    isChange = true,
                )
            }
        }

        composeTestRule.onNodeWithText(changePasswordButtonLabel).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(oldPasswordTextFieldDescription)
            .performTextInput("old password")
        composeTestRule.onNodeWithText(changePasswordButtonLabel).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(oldPasswordTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithText(changePasswordButtonLabel).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(newPasswordTextFieldDescription)
            .performTextInput("new password")
        composeTestRule.onNodeWithText(changePasswordButtonLabel).assertIsNotEnabled()
    }

    @Test
    @Ignore("Research on correct implementation")
    fun clickingOnPasswordResetButtonHidesKeyboardThroughFocusManagerForPasswordReset() {
        val focusManagerMock: FocusManager = mock()
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithText(resetPasswordButtonLabel).performClick()
        with(argumentCaptor<Boolean> { }) {
            verify(focusManagerMock).clearFocus(capture())
            assertThat(firstValue, `is`(false))
        }
    }

    @Test
    @Ignore("Research on correct implementation")
    fun clickingOnPasswordChangeButtonHidesKeyboardThroughFocusManagerPasswordChange() {
        val focusManagerMock: FocusManager = mock()
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    isChange = true,
                )
            }
        }

        composeTestRule.onNodeWithText(resetPasswordButtonLabel).performClick()
        with(argumentCaptor<Boolean> { }) {
            verify(focusManagerMock).clearFocus(capture())
            assertThat(firstValue, `is`(false))
        }
    }

    @Test
    fun clickingOnPasswordResetButtonCallsPasswordResetFunctionWithRequiredArguments() {
        val resetCallbackMock: (User.ResetPassword) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = PasswordResetScreenFunction(reset = resetCallbackMock),
                )
            }
        }

        val oldPassword = "old password"
        val newPassword = "new password"
        composeTestRule.onNodeWithContentDescription(oldPasswordTextFieldDescription)
            .performTextInput(oldPassword)

        composeTestRule.onNodeWithContentDescription(newPasswordTextFieldDescription)
            .performTextInput(newPassword)

        composeTestRule.onNodeWithText(resetPasswordButtonLabel).performClick()
        with(argumentCaptor<User.ResetPassword> { }) {
            verify(resetCallbackMock, VerificationModeFactory.atMostOnce()).invoke(capture())
            assertThat(firstValue.oldPassword, equalTo(oldPassword))
            assertThat(firstValue.newPassword, equalTo(newPassword))
            assertThat(firstValue.isChange, `is`(false))
        }
    }

    @Test
    fun clickingOnPasswordChangeButtonCallsPasswordResetFunctionWithRequiredArguments() {
        val resetCallbackMock: (User.ResetPassword) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = PasswordResetScreenFunction(reset = resetCallbackMock),
                    isChange = true,
                )
            }
        }

        val oldPassword = "old password"
        val newPassword = "new password"
        composeTestRule.onNodeWithContentDescription(oldPasswordTextFieldDescription)
            .performTextInput(oldPassword)

        composeTestRule.onNodeWithContentDescription(newPasswordTextFieldDescription)
            .performTextInput(newPassword)

        composeTestRule.onNodeWithText(changePasswordButtonLabel).performClick()
        with(argumentCaptor<User.ResetPassword> { }) {
            verify(resetCallbackMock, VerificationModeFactory.atMostOnce()).invoke(capture())
            assertThat(firstValue.oldPassword, equalTo(oldPassword))
            assertThat(firstValue.newPassword, equalTo(newPassword))
            assertThat(firstValue.isChange, `is`(true))
        }
    }

    @Test
    fun taskResultWithErrorOnOldPasswordFieldShowsErrorCaptionBelowOldPasswordField() {
        val errorMessage = "Expired temporary password"
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Error(
                        PasswordResetHttpException(oldPassword = listOf(errorMessage)),
                    ),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_ERROR).assertIsDisplayed()
            .assert(hasText(errorMessage))
    }

    @Test
    fun taskResultWithErrorOnNewPasswordFieldShowsErrorCaptionBelowNewPasswordField() {
        val errorMessage = "Password is too common"
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Error(
                        PasswordResetHttpException(newPassword = listOf(errorMessage)),
                    ),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_ERROR).assertIsDisplayed()
            .assert(hasText(errorMessage))
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    @Ignore("Research on implementation.")
    fun taskResultWithAnErrorShowsSnackbar() = runTest {
        val snackbarHostStateMock: SnackbarHostState = mock()
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Error("Error encountered"),
                )
            }
        }

        with(argumentCaptor<String> { }) {
            argumentCaptor<SnackbarDuration> { }.also { duration ->
                verify(snackbarHostStateMock).showSnackbar(capture(), duration = duration.capture())
                assertThat(firstValue, equalTo("Error encountered"))
                assertThat(duration.firstValue, equalTo(SnackbarDuration.Long))
            }
        }
    }

    @Test
    fun successTaskResultWithNonNullDataCallsResetSuccessFunction() {
        val resetSuccessCallback: (User) -> Unit = mock()
        val user = User.default().copy(id = 1, email = "user@example.com")
        composeTestRule.setContent {
            XentlyTheme {
                PasswordResetScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(user),
                    function = PasswordResetScreenFunction(resetSuccess = resetSuccessCallback),
                )
            }
        }

        with(argumentCaptor<User> { }) {
            verify(resetSuccessCallback, VerificationModeFactory.atMostOnce()).invoke(capture())
            assertThat(firstValue.id, equalTo(1))
            assertThat(firstValue.email, equalTo("user@example.com"))
        }
    }
}