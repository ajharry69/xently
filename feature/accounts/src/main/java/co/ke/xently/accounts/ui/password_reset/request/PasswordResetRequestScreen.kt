package co.ke.xently.accounts.ui.password_reset.request

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.accounts.R
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.data.errorMessage
import co.ke.xently.feature.ui.TextFieldErrorText
import kotlinx.coroutines.launch

@Composable
internal fun PasswordResetRequestScreen(
    modifier: Modifier = Modifier,
    email: String = "",
    viewModel: PasswordResetRequestViewModel = hiltViewModel(),
    onSuccessfulRequest: (User) -> Unit = {},
    onNavigationIconClicked: () -> Unit = {},
) {
    val result by viewModel.taskResult.collectAsState()
    PasswordResetRequestScreen(
        modifier,
        result,
        email,
        onSuccessfulRequest,
        onNavigationIconClicked,
        { viewModel.requestTemporaryPassword(it) },
    )
}

@Composable
private fun PasswordResetRequestScreen(
    modifier: Modifier,
    result: TaskResult<User?>,
    email: String = "",
    onSuccessfulRequest: (User) -> Unit = {},
    onNavigationIconClicked: () -> Unit = {},
    onRequestClicked: (String) -> Unit = {},
) {
    var emailAddress by remember(email) {
        mutableStateOf(TextFieldValue(email))
    }
    var emailError by remember { mutableStateOf("") }
    var isEmailError by remember { mutableStateOf(false) }
    val (coroutineScope, scaffoldState) = Pair(rememberCoroutineScope(), rememberScaffoldState())

    if (result is TaskResult.Error) {
        emailError =
            ((result.error as? PasswordResetRequestHttpException)?.email?.joinToString("\n")
                ?: "").also {
                isEmailError = it.isNotBlank()
            }

        if (!isEmailError) {
            val errorMessage =
                result.errorMessage ?: stringResource(R.string.fs_generic_error_message)
            LaunchedEffect(result, errorMessage) {
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(errorMessage)
                }
            }
        }
    } else if (result is TaskResult.Success && result.data != null) {
        SideEffect {
            emailAddress = emailAddress.copy(text = "")
            onSuccessfulRequest(result.data!!)
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
                    title = { Text(stringResource(R.string.fa_request_password_reset_toolbar_title)) },
                )
                if (result is TaskResult.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            Column(modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)) {
                    TextField(
                        value = emailAddress,
                        singleLine = true,
                        isError = isEmailError,
                        modifier = Modifier.fillMaxWidth(),
                        onValueChange = {
                            emailAddress = it
                            isEmailError = false
                        },
                        label = { Text(text = stringResource(R.string.fa_request_password_reset_email_label)) },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    )
                    if (isEmailError) {
                        TextFieldErrorText(emailError, Modifier.fillMaxWidth())
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Button(
                    enabled = emailAddress.text.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = {
                        focusManager.clearFocus()
                        onRequestClicked(emailAddress.text)
                    }
                ) {
                    Text(stringResource(R.string.fa_request_password_reset_button_label).uppercase())
                }
            }
        }
    }
}

@Preview
@Composable
private fun PasswordResetRequestLoadingPreview() {
    PasswordResetRequestScreen(Modifier.fillMaxSize(), TaskResult.Loading)
}

@Preview
@Composable
private fun PasswordResetRequestErrorPreview() {
    PasswordResetRequestScreen(Modifier.fillMaxSize(), TaskResult.Error("Error message"))
}

@Preview
@Composable
private fun PasswordResetRequestSuccessPreview() {
    PasswordResetRequestScreen(Modifier.fillMaxSize(), TaskResult.Success(User.default()))
}