package co.ke.xently.accounts.ui.signin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.accounts.R
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.data.errorMessage
import kotlinx.coroutines.launch

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
    ) { uname, pword ->
        viewModel.signIn(uname, pword)
    }
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

    val (coroutineScope, scaffoldState) = Pair(rememberCoroutineScope(), rememberScaffoldState())

    if (result is TaskResult.Error) {
        val errorMessage = result.errorMessage ?: stringResource(R.string.fs_generic_error_message)
        LaunchedEffect(result, errorMessage) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
    } else if (result is TaskResult.Success && result.data != null) {
        SideEffect {
            onSuccessfulSignIn(result.data!!)
        }
    }

    val focusManager = LocalFocusManager.current

    Scaffold(scaffoldState = scaffoldState) {
        Column(modifier = modifier) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp,
                    navigationIcon = {
                        IconButton(onClick = onNavigationIconClicked) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.fa_navigation_icon_content_description),
                            )
                        }
                    },
                    title = { Text(stringResource(R.string.fa_signin_toolbar_title)) },
                )
                if (result is TaskResult.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            Column {
                Column(modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f)) {
                    TextField(
                        value = uname,
                        singleLine = true,
                        onValueChange = { uname = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        label = { Text(text = stringResource(R.string.fa_signin_username_label)) },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next),
                    )
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    TextField(
                        value = pword,
                        singleLine = true,
                        onValueChange = { pword = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        label = { Text(text = stringResource(R.string.fa_signin_password_label)) },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                        }),
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(painterResource(if (isPasswordVisible) R.drawable.ic_password_visible else R.drawable.ic_password_invisible),
                                    contentDescription = stringResource(R.string.fa_toggle_password_visibility))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Box(modifier = Modifier.weight(1f))
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
                        Text(stringResource(R.string.fa_signin_button_label).uppercase())
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
                    Text(text = stringResource(R.string.fa_signin_signup_button_label))
                }
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