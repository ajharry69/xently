package co.ke.xently.accounts.ui.signup

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.accounts.R
import co.ke.xently.accounts.ui.signin.SignUpHttpException
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.data.errorMessage
import co.ke.xently.data.getOrNull
import co.ke.xently.feature.ui.PasswordVisibilityToggle
import co.ke.xently.feature.ui.TextInputLayout
import co.ke.xently.feature.ui.ToolbarWithProgressbar

@Composable
internal fun SignUpScreen(
    modifier: Modifier = Modifier,
    username: String = "",
    password: String = "",
    viewModel: SignUpViewModel = hiltViewModel(),
    onNavigationIconClicked: () -> Unit = {},
    onSuccessfulSignUp: (User) -> Unit = {},
    onSignInButtonClicked: (String, String) -> Unit = { _, _ -> },
) {
    val result by viewModel.signUpResult.collectAsState()
    SignUpScreen(
        modifier,
        result,
        username,
        password,
        onNavigationIconClicked,
        onSuccessfulSignUp,
        onSignInButtonClicked,
        viewModel::signUp,
    )
}

@Composable
private fun SignUpScreen(
    modifier: Modifier,
    result: TaskResult<User?>,
    username: String = "",
    password: String = "",
    onNavigationIconClicked: () -> Unit = {},
    onSuccessfulSignUp: (User) -> Unit = {},
    onSignInButtonClicked: (String, String) -> Unit = { _, _ -> },
    onSignUpClicked: (User) -> Unit = {},
) {
    val user = result.getOrNull() ?: User.default()
    var uname by remember(user.id, user.email, username) {
        mutableStateOf(TextFieldValue(user.email.ifBlank { username }))
    }
    var pword by remember(user.id, user.password, password) {
        mutableStateOf(TextFieldValue(user.password ?: password))
    }
    var usernameError by remember { mutableStateOf("") }
    var isUsernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf("") }
    var isPasswordError by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val scaffoldState = rememberScaffoldState()

    if (result is TaskResult.Error) {
        usernameError =
            ((result.error as? SignUpHttpException)?.email?.joinToString("\n") ?: "").also {
                isUsernameError = it.isNotBlank()
            }
        passwordError =
            ((result.error as? SignUpHttpException)?.password?.joinToString("\n") ?: "").also {
                isPasswordError = it.isNotBlank()
            }

        if (setOf(isUsernameError, isPasswordError).all { false }) {
            val errorMessage =
                result.errorMessage ?: stringResource(R.string.generic_error_message)
            LaunchedEffect(result, errorMessage) {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
    } else if (result is TaskResult.Success && result.data != null) {
        SideEffect {
            uname = TextFieldValue()
            pword = TextFieldValue()
            onSuccessfulSignUp(result.data!!)
        }
    }
    val focusManager = LocalFocusManager.current
    val toolbarTitle = stringResource(R.string.fa_signup_toolbar_title)

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                toolbarTitle,
                onNavigationIconClicked,
                result is TaskResult.Loading,
            )
        },
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues)) {
            Column(modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f)) {
                TextInputLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    value = uname,
                    isError = isUsernameError,
                    error = usernameError,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next),
                    onValueChange = {
                        uname = it
                        isUsernameError = false
                    },
                    label = stringResource(R.string.fa_signup_username_label),
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                TextInputLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    value = pword,
                    isError = isPasswordError,
                    error = passwordError,
                    onValueChange = {
                        pword = it
                        isPasswordError = false
                    },
                    label = stringResource(R.string.fa_signup_password_label),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done),
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
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Button(
                    enabled = arrayOf(uname, pword).all { it.text.isNotBlank() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = {
                        focusManager.clearFocus()
                        onSignUpClicked(user.copy(email = uname.text, password = pword.text))
                    }
                ) {
                    Text(toolbarTitle.uppercase())
                }
            }
            TextButton(
                { onSignInButtonClicked(uname.text, pword.text) },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                border = BorderStroke(1.dp,
                    MaterialTheme.colors.onBackground.copy(alpha = 0.2f)),
            ) {
                Text(stringResource(R.string.fa_signup_signin_button_label))
            }
        }
    }
}

@Preview
@Composable
private fun SignUpScreenLoadingPreview() {
    SignUpScreen(Modifier.fillMaxSize(), TaskResult)
}

@Preview
@Composable
private fun SignUpScreenErrorPreview() {
    SignUpScreen(Modifier.fillMaxSize(),
        TaskResult.Error("Sorry, uname already exists"))
}

@Preview
@Composable
private fun SignUpScreenSuccessPreview() {
    SignUpScreen(Modifier.fillMaxSize(),
        TaskResult.Success(User.default()))
}