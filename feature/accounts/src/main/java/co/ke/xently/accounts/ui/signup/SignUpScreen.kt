package co.ke.xently.accounts.ui.signup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import co.ke.xently.data.getOrNull
import kotlinx.coroutines.launch

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
    ) { user ->
        viewModel.signUp(user)
    }
}

@Composable
private fun SignUpScreen(
    modifier: Modifier,
    result: TaskResult<User>,
    username: String = "",
    password: String = "",
    onNavigationIconClicked: () -> Unit = {},
    onSuccessfulSignUp: (User) -> Unit = {},
    onSignInButtonClicked: (String, String) -> Unit = { _, _ -> },
    onSignUpClicked: (User) -> Unit = {},
) {
    val user = result.getOrNull() ?: User.default()
    var uname by remember(user.id,
        user.email,
        username) { mutableStateOf(TextFieldValue(user.email.ifBlank { username })) }
    var pword by remember(user.id, user.password, password) {
        mutableStateOf(TextFieldValue(user.password ?: password))
    }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val (coroutineScope, scaffoldState) = Pair(rememberCoroutineScope(), rememberScaffoldState())

    if (result is TaskResult.Error) {
        val errorMessage = result.errorMessage ?: stringResource(R.string.fs_generic_error_message)
        LaunchedEffect(result, errorMessage) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
    } else if (result is TaskResult.Success) {
        SideEffect {
            onSuccessfulSignUp(result.data)
        }
    }

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
                    title = { Text(stringResource(R.string.fa_signup_toolbar_title)) },
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
                        label = { Text(text = stringResource(R.string.fa_signup_username_label)) },
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
                        label = { Text(text = stringResource(R.string.fa_signup_password_label)) },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(painterResource(if (isPasswordVisible) R.drawable.ic_password_visible else R.drawable.ic_password_invisible),
                                    contentDescription = stringResource(R.string.fa_toggle_password_visibility))
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
                            onSignUpClicked(user.copy(email = uname.text, password = pword.text))
                        }
                    ) {
                        Text(stringResource(R.string.fa_signup_button_label).uppercase())
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
                    Text(text = stringResource(R.string.fa_signup_signin_button_label))
                }
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