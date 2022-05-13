package co.ke.xently.accounts.ui.signup

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.input.PasswordVisualTransformation
import co.ke.xently.accounts.R
import co.ke.xently.accounts.ui.signin.SignUpHttpException
import co.ke.xently.common.KENYA
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.TEST_TAG_TEXT_FIELD_ERROR
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity by lazy { composeTestRule.activity }
    private val usernameTextFieldDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fa_signup_username_label)
        )
    }
    private val passwordTextFieldDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fa_signup_password_label)
        )
    }
    private val signInButtonLabel by lazy {
        activity.getString(R.string.fa_signup_signin_button_label).uppercase(KENYA)
    }
    private val signUpToolbarTitle by lazy {
        activity.getString(R.string.fa_signup_toolbar_title)
    }
    private val signUpButtonLabel by lazy {
        signUpToolbarTitle.uppercase(KENYA)
    }
    private val progressbarDescription by lazy {
        activity.getString(R.string.progress_bar_content_description)
    }

    @Test
    fun clickingOnNavigationIcon() {
        val navigationIconClickMock: () -> Unit = mock()

        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = SignUpScreenFunction(
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
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithText(signUpToolbarTitle)
            .assertIsDisplayed()
            .assertHasNoClickAction()
    }

    @Test
    fun signUpButtonLabel() {
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithText(signUpButtonLabel)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun progressBarIsNotShownOnSuccessTaskResult() {
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
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
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Error("An error was encountered"),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertDoesNotExist()
    }

    @Test
    fun signInButtonIsPresent() {
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }
        composeTestRule.onNodeWithText(signInButtonLabel).assertIsDisplayed()
    }

    @Test
    fun createSignUpIsPresent() {
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }
        composeTestRule.onNodeWithText(signUpButtonLabel).assertIsDisplayed()
    }

    @Test
    fun usernameFieldDefaultsToEmptyString() {
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }
        composeTestRule.onNodeWithContentDescription(usernameTextFieldDescription)
            .assert(hasText(""))
    }

    @Test
    fun passwordFieldDefaultsToEmptyString() {
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }
        composeTestRule.onNodeWithContentDescription(passwordTextFieldDescription)
            .assert(hasText(""))
    }

    @Test
    fun usernameFieldDefaultCanBeOverridden() {
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    auth = User.BasicAuth("user@example.com", ""),
                )
            }
        }
        composeTestRule.onNodeWithContentDescription(usernameTextFieldDescription)
            .assert(hasText("user@example.com"))
    }

    @Test
    fun passwordFieldDefaultCanBeOverridden() {
        val password = "use a safe password"
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    auth = User.BasicAuth("", password),
                )
            }
        }
        val passwordMask = PasswordVisualTransformation().mask
        composeTestRule.onNodeWithContentDescription(passwordTextFieldDescription)
            .assert(hasText(List(password.length) { passwordMask }.joinToString("")))
    }

    @Test
    @Ignore("Research on implementation.")
    fun passwordFieldCanBeToggledToShowUnmaskedPassword() {
        val password = "use a safe password"
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    auth = User.BasicAuth("user@example.com", password),
                )
            }
        }
        composeTestRule.onNodeWithTag(
            activity.getString(
                R.string.toggle_password_visibility,
                activity.getString(R.string.show),
            )
        ).performClick()
        composeTestRule.onNodeWithContentDescription(passwordTextFieldDescription)
            .assert(hasText(password))
    }

    @Test
    fun signUpButtonIsDisabledIfEitherUsernameOrPasswordIsBlank() {
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithText(signUpButtonLabel).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(usernameTextFieldDescription)
            .performTextInput("user@example.com")
        composeTestRule.onNodeWithText(signUpButtonLabel).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(usernameTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithText(signUpButtonLabel).assertIsNotEnabled()
    }

    @Test
    fun loadingTaskDisablesSignUpButtonAndShowsProgressBar() {
        val (username, password) = Pair("user@example.com", "secure password")
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Loading,
                    auth = User.BasicAuth(username, password),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(signUpButtonLabel).assertIsNotEnabled()
    }

    @Test
    @Ignore("Research on implementation.")
    fun taskResultWithAnErrorShowsSnackbar() = runTest {
        val snackbarHostStateMock: SnackbarHostState = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
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
    fun successTaskResultWithNonNullDataCallsSignUpSuccessFunction() {
        val signUpSuccessCallback: (User) -> Unit = mock()
        val user = User.default().copy(id = 1, email = "user@example.com")
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(user),
                    function = SignUpScreenFunction(signUpSuccess = signUpSuccessCallback),
                )
            }
        }

        with(argumentCaptor<User> { }) {
            verify(signUpSuccessCallback, times(1)).invoke(capture())
            assertThat(firstValue.id, equalTo(1))
            assertThat(firstValue.email, equalTo("user@example.com"))
        }
    }

    @Test
    @Ignore("Research on correct implementation")
    fun clickingOnSignUpButtonHidesKeyboardThroughFocusManager() {
        val focusManagerMock: FocusManager = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    auth = User.BasicAuth("user@example.org", "password"),
                )
            }
        }

        composeTestRule.onNodeWithText(signUpButtonLabel).performClick()
        with(argumentCaptor<Boolean> { }) {
            verify(focusManagerMock).clearFocus(capture())
            assertThat(firstValue, `is`(false))
        }
    }

    @Test
    fun clickingOnSignUpButtonCallsSignUpFunctionWithRequiredArguments() {
        val signUpCallbackMock: (User) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = SignUpScreenFunction(signUp = signUpCallbackMock),
                )
            }
        }

        val email = "user@example.com"
        val password = "use a safe password"
        composeTestRule.onNodeWithContentDescription(usernameTextFieldDescription)
            .performTextInput(email)

        composeTestRule.onNodeWithContentDescription(passwordTextFieldDescription)
            .performTextInput(password)

        composeTestRule.onNodeWithText(signUpButtonLabel).performClick()
        with(argumentCaptor<User> { }) {
            verify(signUpCallbackMock, times(1)).invoke(capture())
            assertThat(firstValue.email, equalTo(email))
            assertThat(firstValue.password, equalTo(password))
        }
    }

    @Test
    fun clickingOnSignInButtonWhenUsernameOrPasswordTextFieldIsEmpty() {
        val signInCallbackMock: (User.BasicAuth) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = SignUpScreenFunction(signIn = signInCallbackMock),
                )
            }
        }

        composeTestRule.onNodeWithText(signInButtonLabel).performClick()
        with(argumentCaptor<User.BasicAuth> { }) {
            verify(signInCallbackMock, times(1)).invoke(capture())
            assertThat(firstValue.username, emptyString())
            assertThat(firstValue.password, emptyString())
        }
    }

    @Test
    fun clickingOnSignInButtonWhenUsernameOrPasswordTextFieldIsNotEmpty() {
        val (username, password) = Pair("user@example.com", "secure password")
        val signInCallbackMock: (User.BasicAuth) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    auth = User.BasicAuth(username, password),
                    function = SignUpScreenFunction(signIn = signInCallbackMock),
                )
            }
        }

        composeTestRule.onNodeWithText(signInButtonLabel).performClick()
        with(argumentCaptor<User.BasicAuth> {}) {
            verify(signInCallbackMock).invoke(capture())
            assertThat(firstValue.username, equalTo(username))
            assertThat(firstValue.password, equalTo(password))
        }

        composeTestRule.onNodeWithContentDescription(usernameTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithText(signInButtonLabel).performClick()
        with(argumentCaptor<User.BasicAuth> {}) {
            verify(signInCallbackMock, atMost(2)).invoke(capture())
            assertThat(firstValue.password, equalTo(password))
            assertThat(firstValue.username, emptyString())
        }
    }

    @Test
    fun taskResultWithErrorOnEmailFieldShowsErrorCaptionBelowEmailField() {
        val errorMessage = "Email has been registered"
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Error(SignUpHttpException(email = listOf(errorMessage))),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_ERROR).assertIsDisplayed()
            .assert(hasText(errorMessage))
    }

    @Test
    fun taskResultWithErrorOnPasswordFieldShowsErrorCaptionBelowPasswordField() {
        val errorMessage = "Password is too common"
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Error(SignUpHttpException(password = listOf(errorMessage))),
                )
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_TEXT_FIELD_ERROR).assertIsDisplayed()
            .assert(hasText(errorMessage))
    }

    @Test
    fun clickingOnSignInButtonTrimSpacesFromStartAndEndOfTextInputs() {
        val signInCallbackMock: (User.BasicAuth) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = SignUpScreenFunction(signIn = signInCallbackMock),
                )
            }
        }

        val username = "   user@example.com   "
        val password = "    use a safe password    "
        composeTestRule.onNodeWithContentDescription(usernameTextFieldDescription)
            .performTextInput(username)

        composeTestRule.onNodeWithContentDescription(passwordTextFieldDescription)
            .performTextInput(password)

        composeTestRule.onNodeWithText(signInButtonLabel).performClick()
        with(argumentCaptor<User.BasicAuth> { }) {
            verify(signInCallbackMock, times(1)).invoke(capture())
            assertThat(firstValue.username, equalTo("user@example.com"))
            assertThat(firstValue.password, equalTo("use a safe password"))
        }
    }

    @Test
    fun clickingOnSignUpButtonTrimSpacesFromStartAndEndOfTextInputs() {
        val signUpCallbackMock: (User) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignUpScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = SignUpScreenFunction(signUp = signUpCallbackMock),
                )
            }
        }

        val username = "   user@example.com   "
        val password = "    use a safe password    "
        composeTestRule.onNodeWithContentDescription(usernameTextFieldDescription)
            .performTextInput(username)

        composeTestRule.onNodeWithContentDescription(passwordTextFieldDescription)
            .performTextInput(password)

        composeTestRule.onNodeWithText(signUpButtonLabel).performClick()
        with(argumentCaptor<User> { }) {
            verify(signUpCallbackMock, times(1)).invoke(capture())
            assertThat(firstValue.email, equalTo("user@example.com"))
            assertThat(firstValue.password, equalTo("use a safe password"))
        }
    }
}