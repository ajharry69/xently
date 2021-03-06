package co.ke.xently.accounts.ui.signin

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
import co.ke.xently.common.KENYA
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.theme.XentlyTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*


@OptIn(ExperimentalCoroutinesApi::class)
class SignInScreenTest {
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val activity by lazy { composeTestRule.activity }
    private val forgotPasswordLabel by lazy { activity.getString(R.string.fa_signin_forgot_password_button_label) }
    private val signInToolbarTitle by lazy {
        activity.getString(R.string.fa_signin_toolbar_title)
    }
    private val signInButtonLabel by lazy {
        signInToolbarTitle.uppercase(KENYA)
    }
    private val signUpButtonLabel by lazy {
        activity.getString(R.string.fa_signin_signup_button_label)
    }
    private val usernameTextFieldDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fa_signin_username_label),
        )
    }
    private val passwordTextFieldDescription by lazy {
        activity.getString(
            R.string.text_field_content_description,
            activity.getString(R.string.fa_signin_password_label),
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
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = SignInScreenFunction(
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
    @Ignore("Research on implementation.")
    fun taskResultWithAnErrorShowsSnackbar() = runTest {
        val snackbarHostStateMock: SnackbarHostState = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
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
    fun toolbarTitle() {
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithText(signInToolbarTitle)
            .assertIsDisplayed()
            .assertHasNoClickAction()
    }

    @Test
    fun signInButtonLabel() {
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithText(signInButtonLabel)
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun progressBarIsNotShownOnSuccessTaskResult() {
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
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
                SignInScreen(
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
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }
        composeTestRule.onNodeWithText(signInButtonLabel).assertIsDisplayed()
    }

    @Test
    fun createAccountButtonIsPresent() {
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }
        composeTestRule.onNodeWithText(signUpButtonLabel).assertIsDisplayed()
    }

    @Test
    fun forgotPasswordButtonIsPresent() {
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }
        composeTestRule.onNodeWithText(forgotPasswordLabel).assertIsDisplayed()
    }

    @Test
    fun usernameFieldDefaultsToEmptyString() {
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
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
                SignInScreen(
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
                SignInScreen(
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
                SignInScreen(
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
                SignInScreen(
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
    fun signInButtonIsDisabledIfEitherUsernameOrPasswordIsBlank() {
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                )
            }
        }

        composeTestRule.onNodeWithText(signInButtonLabel).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(usernameTextFieldDescription)
            .performTextInput("user@example.com")
        composeTestRule.onNodeWithText(signInButtonLabel).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(usernameTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithText(signInButtonLabel).assertIsNotEnabled()
    }

    @Test
    @Ignore("Research on implementation.")
    fun clickingOnSignInButtonHidesKeyboardThroughFocusManager() {
        val focusManagerMock: FocusManager = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    auth = User.BasicAuth("user@example.org", "password"),
                )
            }
        }

        composeTestRule.onNodeWithText(signInButtonLabel).performClick()
        with(argumentCaptor<Boolean> { }) {
            verify(focusManagerMock).clearFocus(capture())
            assertThat(firstValue, Matchers.`is`(false))
        }
    }

    @Test
    fun clickingOnSignInButtonCallsSignInFunctionWithRequiredArguments() {
        val signInCallback: (User.BasicAuth) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = SignInScreenFunction(signIn = signInCallback),
                )
            }
        }

        val username = "user@example.com"
        val password = "use a safe password"
        composeTestRule.onNodeWithContentDescription(usernameTextFieldDescription)
            .performTextInput(username)

        composeTestRule.onNodeWithContentDescription(passwordTextFieldDescription)
            .performTextInput(password)

        composeTestRule.onNodeWithText(signInButtonLabel).performClick()
        with(argumentCaptor<User.BasicAuth> { }) {
            verify(signInCallback, times(1)).invoke(capture())
            assertThat(firstValue.username, equalTo(username))
            assertThat(firstValue.password, equalTo(password))
        }
    }

    @Test
    fun clickingOnSignInButtonTrimSpacesFromStartAndEndOfTextInputs() {
        val signInCallbackMock: (User.BasicAuth) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = SignInScreenFunction(signIn = signInCallbackMock),
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
    fun clickingOnCreateAccountButtonTrimSpacesFromStartAndEndOfTextInputs() {
        val createAccountCallbackMock: (User.BasicAuth) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = SignInScreenFunction(createAccount = createAccountCallbackMock),
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
        with(argumentCaptor<User.BasicAuth> { }) {
            verify(createAccountCallbackMock, times(1)).invoke(capture())
            assertThat(firstValue.username, equalTo("user@example.com"))
            assertThat(firstValue.password, equalTo("use a safe password"))
        }
    }

    @Test
    fun clickingOnForgotPasswordButtonTrimSpacesFromStartAndEndOfTextInputs() {
        val forgotCallbackMock: (String) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = SignInScreenFunction(forgotPassword = forgotCallbackMock),
                )
            }
        }

        val username = "   user@example.com   "
        composeTestRule.onNodeWithContentDescription(usernameTextFieldDescription)
            .performTextInput(username)

        composeTestRule.onNodeWithText(forgotPasswordLabel).performClick()
        with(argumentCaptor<String> { }) {
            verify(forgotCallbackMock, times(1)).invoke(capture())
            assertThat(firstValue, equalTo("user@example.com"))
        }
    }

    @Test
    fun clickingOnForgotPasswordPassesWhenUsernameFieldIsEmpty() {
        val forgotPasswordCallback: (String) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = SignInScreenFunction(forgotPassword = forgotPasswordCallback),
                )
            }
        }

        composeTestRule.onNodeWithText(forgotPasswordLabel).performClick()
        with(argumentCaptor<String> {}) {
            verify(forgotPasswordCallback, times(1)).invoke(capture())
            assertThat(firstValue, emptyString())
        }
    }

    @Test
    fun clickingOnForgotPasswordPassesWhenUsernameFieldIsNotEmpty() {
        val forgotPasswordCallback: (String) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = SignInScreenFunction(forgotPassword = forgotPasswordCallback),
                )
            }
        }

        val username = "user@example.com"
        composeTestRule.onNodeWithContentDescription(usernameTextFieldDescription)
            .performTextInput(username)
        composeTestRule.onNodeWithText(forgotPasswordLabel).performClick()
        with(argumentCaptor<String> { }) {
            verify(forgotPasswordCallback, times(1)).invoke(capture())
            assertThat(firstValue, equalTo(username))
        }
    }

    @Test
    fun clickingOnCreateAccountButtonWhenUsernameOrPasswordTextFieldIsEmpty() {
        val createAccountCallback: (User.BasicAuth) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    function = SignInScreenFunction(createAccount = createAccountCallback),
                )
            }
        }

        composeTestRule.onNodeWithText(signUpButtonLabel).performClick()
        with(argumentCaptor<User.BasicAuth> { }) {
            verify(createAccountCallback, times(1)).invoke(capture())
            assertThat(firstValue.username, emptyString())
            assertThat(firstValue.password, emptyString())
        }
    }

    @Test
    fun clickingOnCreateAccountButtonWhenUsernameOrPasswordTextFieldIsNotEmpty() {
        val (username, password) = Pair("user@example.com", "secure password")
        val createAccountCallback: (User.BasicAuth) -> Unit = mock()
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(null),
                    auth = User.BasicAuth(username, password),
                    function = SignInScreenFunction(createAccount = createAccountCallback),
                )
            }
        }

        composeTestRule.onNodeWithText(signUpButtonLabel).performClick()
        with(argumentCaptor<User.BasicAuth> {}) {
            verify(createAccountCallback).invoke(capture())
            assertThat(firstValue.username, equalTo(username))
            assertThat(firstValue.password, equalTo(password))
        }

        composeTestRule.onNodeWithContentDescription(usernameTextFieldDescription)
            .performTextClearance()
        composeTestRule.onNodeWithText(signUpButtonLabel).performClick()
        with(argumentCaptor<User.BasicAuth> {}) {
            verify(createAccountCallback, atMost(2)).invoke(capture())
            assertThat(firstValue.password, equalTo(password))
            assertThat(firstValue.username, emptyString())
        }
    }

    @Test
    fun loadingTaskDisablesSignInButtonAndShowsProgressBar() {
        val (username, password) = Pair("user@example.com", "secure password")
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Loading,
                    auth = User.BasicAuth(username, password),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(progressbarDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(signInButtonLabel).assertIsNotEnabled()
    }

    @Test
    fun successTaskResultWithNonNullDataCallsSignInSuccessFunction() {
        val signInSuccessCallback: (User) -> Unit = mock()
        val user = User.default().copy(id = 1, email = "user@example.com")
        composeTestRule.setContent {
            XentlyTheme {
                SignInScreen(
                    modifier = Modifier.fillMaxSize(),
                    result = TaskResult.Success(user),
                    function = SignInScreenFunction(signInSuccess = signInSuccessCallback),
                )
            }
        }

        with(argumentCaptor<User> { }) {
            verify(signInSuccessCallback, times(1)).invoke(capture())
            assertThat(firstValue.id, equalTo(1))
            assertThat(firstValue.email, equalTo("user@example.com"))
        }
    }
}