package co.ke.xently.accounts.ui.signin

import androidx.compose.foundation.layout.*
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
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.data.errorMessage
import co.ke.xently.feature.ui.*

internal data class SignInScreenFunction(
    val navigationIcon: () -> Unit = {},
    val forgotPassword: () -> Unit = {},
    val signInSuccess: (User) -> Unit = {},
    val signIn: (User.BasicAuth) -> Unit = { },
    val createAccount: (User.BasicAuth) -> Unit = { },
)

@Composable
internal fun SignInScreen(
    modifier: Modifier,
    auth: User.BasicAuth,
    function: SignInScreenFunction,
    viewModel: SignInViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val result by viewModel.result.collectAsState(
        context = scope.coroutineContext,
        initial = TaskResult.Success(null),
    )
    SignInScreen(
        auth = auth,
        result = result,
        modifier = modifier,
        function = function.copy(signIn = viewModel::signIn),
    )
}

@Composable
private fun SignInScreen(
    modifier: Modifier,
    result: TaskResult<User?>,
    auth: User.BasicAuth = User.BasicAuth("", ""),
    function: SignInScreenFunction = SignInScreenFunction(),
) {
    var uname by remember(auth.username) {
        mutableStateOf(TextFieldValue(auth.username))
    }
    var pword by remember(auth.password) {
        mutableStateOf(TextFieldValue(auth.password))
    }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val scaffoldState = rememberScaffoldState()

    if (result is TaskResult.Error) {
        val errorMessage = result.errorMessage ?: stringResource(R.string.generic_error_message)
        LaunchedEffect(result, errorMessage) {
            scaffoldState.snackbarHostState.showSnackbar(errorMessage)
        }
    } else if (result is TaskResult.Success && result.data != null) {
        SideEffect {
            function.signInSuccess.invoke(result.data!!)
        }
    }

    val focusManager = LocalFocusManager.current
    val toolbarTitle = stringResource(R.string.fa_signin_toolbar_title)

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                title = toolbarTitle,
                onNavigationIconClicked = function.navigationIcon,
                showProgress = result is TaskResult.Loading,
            )
        },
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues)) {
            Column(modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f)) {
                TextInputLayout(
                    value = uname,
                    onValueChange = { uname = it },
                    modifier = VerticalLayoutModifier.padding(top = VIEW_SPACE),
                    label = stringResource(R.string.fa_signin_username_label),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Email,
                    ),
                )
                Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))
                TextInputLayout(
                    value = pword,
                    onValueChange = { pword = it },
                    modifier = VerticalLayoutModifier,
                    label = stringResource(R.string.fa_signin_password_label),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                    }),
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
                Row(modifier = Modifier.padding(horizontal = VIEW_SPACE)) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(function.forgotPassword) {
                        Text(stringResource(R.string.fa_signin_forgot_password_button_label))
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))
                Button(
                    enabled = arrayOf(
                        uname,
                        pword,
                    ).all { it.text.isNotBlank() } && result !is TaskResult.Loading,
                    modifier = VerticalLayoutModifier,
                    onClick = {
                        focusManager.clearFocus()
                        function.signIn.invoke(
                            auth.copy(
                                username = uname.text,
                                password = pword.text,
                            ),
                        )
                    }
                ) {
                    Text(toolbarTitle.uppercase())
                }
            }
            OutlinedButton(
                modifier = VerticalLayoutModifier,
                onClick = {
                    function.createAccount.invoke(
                        auth.copy(
                            username = uname.text,
                            password = pword.text,
                        ),
                    )
                },
            ) {
                Text(stringResource(R.string.fa_signin_signup_button_label))
            }
        }
    }
}

@Preview
@Composable
private fun SignInScreenLoadingPreview() {
    SignInScreen(Modifier.fillMaxSize(), TaskResult.Loading)
}

@Preview
@Composable
private fun SignInScreenSuccessPreview() {
    SignInScreen(Modifier.fillMaxSize(), TaskResult.Success(User.default()))
}