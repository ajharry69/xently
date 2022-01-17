package co.ke.xently.accounts.ui.signin

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
    viewModel: SignInViewModel = hiltViewModel(),
    onNavigationIconClicked: () -> Unit = {},
    onSuccessfulSignIn: (User) -> Unit = {},
) {
    val result by viewModel.signInResult.collectAsState()
    SignInScreen(
        modifier,
        result,
        onNavigationIconClicked,
        onSuccessfulSignIn,
    ) { username, password ->
        viewModel.signIn(username, password)
    }
}

@Composable
private fun SignInScreen(
    modifier: Modifier,
    result: TaskResult<User>,
    onNavigationIconClicked: () -> Unit,
    onSuccessfulSignIn: (User) -> Unit,
    onSignInClicked: (String, String) -> Unit,
) {
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
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
            onSuccessfulSignIn(result.data)
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
                    title = { Text(stringResource(R.string.fa_signin_toolbar_title)) },
                )
                if (result is TaskResult.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                TextField(
                    value = username,
                    singleLine = true,
                    onValueChange = { username = it },
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
                    value = password,
                    singleLine = true,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    label = { Text(text = stringResource(R.string.fa_signin_password_label)) },
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
                    enabled = arrayOf(username, password).all { it.text.isNotBlank() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = { onSignInClicked(username.text, password.text) }
                ) {
                    Text(stringResource(R.string.fa_signin_button_label).uppercase())
                }
            }
        }
    }
}