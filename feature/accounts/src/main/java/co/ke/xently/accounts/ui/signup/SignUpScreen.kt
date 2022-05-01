package co.ke.xently.accounts.ui.signup

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.accounts.R
import co.ke.xently.accounts.ui.signin.SignUpHttpException
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.data.errorMessage
import co.ke.xently.data.getOrNull
import co.ke.xently.feature.ui.*

data class SignUpScreenFunction(
    val signUp: (User) -> Unit = {},
    val navigationIcon: () -> Unit = {},
    val signUpSuccess: (User) -> Unit = {},
    val signIn: (User.BasicAuth) -> Unit = { },
)

@Composable
internal fun SignUpScreen(
    modifier: Modifier,
    auth: User.BasicAuth,
    function: SignUpScreenFunction,
    viewModel: SignUpViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val result by viewModel.result.collectAsState(
        context = scope.coroutineContext,
        initial = TaskResult.Success(null),
    )
    SignUpScreen(
        auth = auth,
        result = result,
        modifier = modifier,
        function = function.copy(signUp = viewModel::signUp),
    )
}

@Composable
@VisibleForTesting
fun SignUpScreen(
    modifier: Modifier,
    result: TaskResult<User?>,
    auth: User.BasicAuth = User.BasicAuth("", ""),
    function: SignUpScreenFunction = SignUpScreenFunction(),
) {
    val user = result.getOrNull() ?: User.default()
    var uname by remember(auth.username) {
        mutableStateOf(TextFieldValue(user.email.ifBlank { auth.username }))
    }
    var pword by remember(auth.password) {
        mutableStateOf(TextFieldValue(user.password ?: auth.password))
    }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val scaffoldState = rememberScaffoldState()

    var usernameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    if (result is TaskResult.Error) {
        val exception = result.error as? SignUpHttpException
        usernameError = exception?.email?.joinToString("\n") ?: ""
        passwordError = exception?.password?.joinToString("\n") ?: ""

        if (exception?.hasFieldErrors() != true) {
            val errorMessage =
                result.errorMessage ?: stringResource(R.string.generic_error_message)
            LaunchedEffect(result, errorMessage) {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
    } else if (result is TaskResult.Success && result.data != null) {
        SideEffect {
            function.signUpSuccess.invoke(result.data!!)
        }
    }
    val focusManager = LocalFocusManager.current
    val toolbarTitle = stringResource(R.string.fa_signup_toolbar_title)

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                title = toolbarTitle,
                showProgress = result is TaskResult.Loading,
                onNavigationIconClicked = function.navigationIcon,
            )
        },
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f),
            ) {
                var isUsernameError by remember(usernameError) {
                    mutableStateOf(usernameError.isNotBlank())
                }
                TextInputLayout(
                    modifier = VerticalLayoutModifier.padding(top = VIEW_SPACE),
                    value = uname,
                    isError = isUsernameError,
                    error = usernameError,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Email,
                    ),
                    onValueChange = {
                        uname = it
                        isUsernameError = false
                    },
                    label = stringResource(R.string.fa_signup_username_label),
                )
                Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))
                var isPasswordError by remember(passwordError) {
                    mutableStateOf(passwordError.isNotBlank())
                }
                TextInputLayout(
                    modifier = VerticalLayoutModifier,
                    value = pword,
                    isError = isPasswordError,
                    error = passwordError,
                    onValueChange = {
                        pword = it
                        isPasswordError = false
                    },
                    label = stringResource(R.string.fa_signup_password_label),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password,
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    visualTransformation = if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        PasswordVisibilityToggle(isVisible = isPasswordVisible) {
                            isPasswordVisible = !isPasswordVisible
                        }
                    }
                )
                Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))
                Button(
                    enabled = arrayOf(
                        uname,
                        pword,
                    ).all { it.text.isNotBlank() } && result !is TaskResult.Loading,
                    modifier = VerticalLayoutModifier,
                    onClick = {
                        focusManager.clearFocus()
                        function.signUp.invoke(user.copy(email = uname.text, password = pword.text))
                    }
                ) {
                    Text(toolbarTitle.uppercase())
                }
            }
            OutlinedButton(
                modifier = VerticalLayoutModifier,
                onClick = {
                    function.signIn.invoke(
                        auth.copy(
                            username = uname.text,
                            password = pword.text,
                        ),
                    )
                },
            ) {
                Text(stringResource(R.string.fa_signup_signin_button_label))
            }
        }
    }
}

@Preview
@Composable
private fun SignUpScreenLoadingPreview() {
    SignUpScreen(modifier = Modifier.fillMaxSize(), result = TaskResult.Loading)
}

@Preview
@Composable
private fun SignUpScreenSuccessPreview() {
    SignUpScreen(
        modifier = Modifier.fillMaxSize(),
        result = TaskResult.Success(User.default()),
    )
}