package co.ke.xently.accounts.ui.password_reset.request

import androidx.annotation.VisibleForTesting
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
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.ui.*

internal data class PasswordResetRequestScreenFunction(
    val request: (String) -> Unit = {},
    val requestSuccess: (User) -> Unit = {},
    val sharedFunction: SharedFunction = SharedFunction(),
)

@Composable
internal fun PasswordResetRequestScreen(
    modifier: Modifier,
    email: String,
    function: PasswordResetRequestScreenFunction,
    viewModel: PasswordResetRequestViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val result by viewModel.result.collectAsState(
        context = scope.coroutineContext,
        initial = TaskResult.Success(null),
    )
    PasswordResetRequestScreen(
        email = email,
        result = result,
        modifier = modifier,
        function = function.copy(request = viewModel::requestTemporaryPassword)
    )
}

@Composable
@VisibleForTesting
internal fun PasswordResetRequestScreen(
    modifier: Modifier,
    result: TaskResult<User?>,
    email: String = "",
    function: PasswordResetRequestScreenFunction = PasswordResetRequestScreenFunction(),
) {
    var emailAddress by remember(email) {
        mutableStateOf(TextFieldValue(email))
    }
    val scaffoldState = rememberScaffoldState()

    var emailError by remember { mutableStateOf("") }
    if (result is TaskResult.Error) {
        val exception = result.error as? PasswordResetRequestHttpException
        emailError = exception?.email?.joinToString("\n") ?: ""

        if (exception?.hasFieldErrors() != true) {
            val errorMessage =
                result.errorMessage ?: stringResource(R.string.generic_error_message)
            LaunchedEffect(result, errorMessage) {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
    } else if (result is TaskResult.Success && result.data != null) {
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
                onNavigationIconClicked = function.sharedFunction.onNavigationIconClicked,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            var isEmailError by remember(emailError) {
                mutableStateOf(emailError.isNotBlank())
            }
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
                enabled = emailAddress.text.isNotBlank() && result !is TaskResult.Loading,
                modifier = VerticalLayoutModifier,
                onClick = {
                    focusManager.clearFocus()
                    function.request.invoke(emailAddress.text.trim())
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