package co.ke.xently.accounts.ui.password_reset.request

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import co.ke.xently.feature.ui.TextInputLayout
import co.ke.xently.feature.ui.ToolbarWithProgressbar

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
        viewModel::requestTemporaryPassword,
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
    val scaffoldState = rememberScaffoldState()

    if (result is TaskResult.Error) {
        emailError =
            ((result.error as? PasswordResetRequestHttpException)?.email?.joinToString("\n")
                ?: "").also {
                isEmailError = it.isNotBlank()
            }

        if (!isEmailError) {
            val errorMessage =
                result.errorMessage ?: stringResource(R.string.generic_error_message)
            LaunchedEffect(result, errorMessage) {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
    } else if (result is TaskResult.Success && result.data != null) {
        SideEffect {
            emailAddress = TextFieldValue()
            onSuccessfulRequest(result.data!!)
        }
    }
    val focusManager = LocalFocusManager.current
    val toolbarTitle = stringResource(R.string.fa_request_password_reset_toolbar_title)

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
        Column(
            modifier = modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            TextInputLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                value = emailAddress,
                isError = isEmailError,
                error = emailError,
                onValueChange = {
                    emailAddress = it
                    isEmailError = false
                },
                label = stringResource(R.string.fa_request_password_reset_email_label),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )
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
                Text(toolbarTitle.uppercase())
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