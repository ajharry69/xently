package co.ke.xently.accounts.ui.password_reset.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.accounts.R
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.data.errorMessage
import co.ke.xently.feature.ui.*

internal data class PasswordResetRequestScreenFunction(
    val navigationIcon: () -> Unit = {},
    val request: (String) -> Unit = {},
    val requestSuccess: (User) -> Unit = {},
)

@Composable
internal fun PasswordResetRequestScreen(
    modifier: Modifier,
    email: String,
    function: PasswordResetRequestScreenFunction,
    viewModel: PasswordResetRequestViewModel = hiltViewModel(),
) {
    val result by viewModel.taskResult.collectAsState()
    PasswordResetRequestScreen(
        modifier = modifier,
        result = result,
        email = email,
        function = function.copy(request = viewModel::requestTemporaryPassword)
    )
}

@Composable
private fun PasswordResetRequestScreen(
    modifier: Modifier,
    result: TaskResult<User?>,
    email: String = "",
    function: PasswordResetRequestScreenFunction = PasswordResetRequestScreenFunction(),
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
        emailAddress = TextFieldValue()
        SideEffect {
            function.requestSuccess.invoke(result.data!!)
        }
    }
    val focusManager = LocalFocusManager.current
    val toolbarTitle = stringResource(R.string.fa_request_password_reset_toolbar_title)

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
        Column(
            modifier = modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            TextInputLayout(
                modifier = VerticalLayoutModifier.padding(top = VIEW_SPACE),
                value = emailAddress,
                isError = isEmailError,
                error = emailError,
                onValueChange = {
                    emailAddress = it
                    isEmailError = false
                },
                label = stringResource(R.string.fa_request_password_reset_email_label),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Email,
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )
            Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))
            Button(
                enabled = emailAddress.text.isNotBlank(),
                modifier = VerticalLayoutModifier,
                onClick = {
                    focusManager.clearFocus()
                    function.request.invoke(emailAddress.text)
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
    PasswordResetRequestScreen(modifier = Modifier.fillMaxSize(), result = TaskResult.Loading)
}

@Preview
@Composable
private fun PasswordResetRequestSuccessPreview() {
    PasswordResetRequestScreen(
        modifier = Modifier.fillMaxSize(),
        result = TaskResult.Success(User.default()),
    )
}