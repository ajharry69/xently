package co.ke.xently.accounts.ui.signin

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
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.data.errorMessage
import co.ke.xently.feature.ui.PasswordVisibilityToggle
import co.ke.xently.feature.ui.TextInputLayout
import co.ke.xently.feature.ui.ToolbarWithProgressbar

@Composable
internal fun SignInScreen(
    modifier: Modifier = Modifier,
    username: String = "",
    password: String = "",
    viewModel: SignInViewModel = hiltViewModel(),
    onNavigationIconClicked: () -> Unit = {},
    onForgotPasswordButtonClicked: () -> Unit = {},
    onCreateAccountButtonClicked: (String, String) -> Unit = { _, _ -> },
    onSuccessfulSignIn: (User) -> Unit = {},
) {
    val result by viewModel.signInResult.collectAsState()
    SignInScreen(
        modifier,
        result,
        username,
        password,
        onNavigationIconClicked,
        onSuccessfulSignIn,
        onForgotPasswordButtonClicked,
        onCreateAccountButtonClicked,
        viewModel::signIn,
    )
}

@Composable
private fun SignInScreen(
    modifier: Modifier,
    result: TaskResult<User?>,
    username: String = "",
    password: String = "",
    onNavigationIconClicked: () -> Unit = {},
    onSuccessfulSignIn: (User) -> Unit = {},
    onForgotPasswordButtonClicked: () -> Unit = {},
    onCreateAccountButtonClicked: (String, String) -> Unit = { _, _ -> },
    onSignInClicked: (String, String) -> Unit = { _, _ -> },
) {
    var uname by remember { mutableStateOf(TextFieldValue(username)) }
    var pword by remember { mutableStateOf(TextFieldValue(password)) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val scaffoldState = rememberScaffoldState()

    if (result is TaskResult.Error) {
        val errorMessage = result.errorMessage ?: stringResource(R.string.generic_error_message)
        LaunchedEffect(result, errorMessage) {
            scaffoldState.snackbarHostState.showSnackbar(errorMessage)
        }
    } else if (result is TaskResult.Success && result.data != null) {
        SideEffect {
            uname = TextFieldValue()
            pword = TextFieldValue()
            onSuccessfulSignIn(result.data!!)
        }
    }

    val focusManager = LocalFocusManager.current
    val toolbarTitle = stringResource(R.string.fa_signin_toolbar_title)

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
                    value = uname,
                    onValueChange = { uname = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    label = stringResource(R.string.fa_signin_username_label),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next),
                )
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                TextInputLayout(
                    value = pword,
                    onValueChange = { pword = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    label = stringResource(R.string.fa_signin_password_label),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
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
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onForgotPasswordButtonClicked) {
                        Text(stringResource(R.string.fa_signin_forgot_password_button_label))
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 4.dp))
                Button(
                    enabled = arrayOf(uname, pword).all { it.text.isNotBlank() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = {
                        focusManager.clearFocus()
                        onSignInClicked(uname.text, pword.text)
                    }
                ) {
                    Text(toolbarTitle.uppercase())
                }
            }
            TextButton(
                { onCreateAccountButtonClicked(uname.text, pword.text) },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                border = BorderStroke(1.dp,
                    MaterialTheme.colors.onBackground.copy(alpha = 0.2f)),
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
private fun SignInScreenErrorPreview() {
    SignInScreen(Modifier.fillMaxSize(),
        TaskResult.Error("Incorrect username or password"))
}

@Preview
@Composable
private fun SignInScreenSuccessPreview() {
    SignInScreen(Modifier.fillMaxSize(), TaskResult.Success(User.default()))
}