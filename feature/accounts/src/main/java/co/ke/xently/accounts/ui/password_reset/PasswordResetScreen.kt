package co.ke.xently.accounts.ui.password_reset

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
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.accounts.R
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.data.errorMessage
import co.ke.xently.feature.ui.*

internal data class PasswordResetScreenFunction(
    val navigationIcon: () -> Unit = {},
    val resetSuccess: (User) -> Unit = {},
    val reset: (User.ResetPassword) -> Unit = {},
)

@Composable
internal fun PasswordResetScreen(
    modifier: Modifier,
    isChange: Boolean,
    function: PasswordResetScreenFunction,
    viewModel: PasswordResetViewModel = hiltViewModel(),
) {
    val result by viewModel.taskResult.collectAsState()
    PasswordResetScreen(
        modifier = modifier,
        result = result,
        isChange = isChange,
        function = function.copy(reset = viewModel::resetPassword),
    )
}

@Composable
private fun PasswordResetScreen(
    modifier: Modifier,
    result: TaskResult<User?>,
    isChange: Boolean = false,
    function: PasswordResetScreenFunction = PasswordResetScreenFunction(),
) {
    var oldPassword by remember { mutableStateOf(TextFieldValue("")) }
    var newPassword by remember { mutableStateOf(TextFieldValue("")) }

    var oldPasswordError by remember { mutableStateOf("") }
    var newPasswordError by remember { mutableStateOf("") }

    var isOldPasswordError by remember { mutableStateOf(false) }
    var isNewPasswordError by remember { mutableStateOf(false) }

    var isOldPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }

    val scaffoldState = rememberScaffoldState()

    if (result is TaskResult.Error) {
        oldPasswordError =
            ((result.error as? PasswordResetHttpException)?.oldPassword?.joinToString("\n")
                ?: "").also {
                isOldPasswordError = it.isNotBlank()
            }
        newPasswordError =
            ((result.error as? PasswordResetHttpException)?.newPassword?.joinToString("\n")
                ?: "").also {
                isNewPasswordError = it.isNotBlank()
            }

        if (setOf(isOldPasswordError, isNewPasswordError).all { false }) {
            val errorMessage =
                result.errorMessage ?: stringResource(R.string.generic_error_message)
            LaunchedEffect(result, errorMessage) {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
    } else if (result is TaskResult.Success && result.data != null) {
        oldPassword = TextFieldValue()
        newPassword = TextFieldValue()
        SideEffect {
            function.resetSuccess.invoke(result.data!!)
        }
    }
    val focusManager = LocalFocusManager.current

    val toolbarTitle = stringRes(R.string.fa_reset_password_toolbar_title,
        if (isChange) R.string.fa_change else R.string.fa_reset)

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
                value = oldPassword,
                isError = isOldPasswordError,
                error = oldPasswordError,
                onValueChange = {
                    oldPassword = it
                    isOldPasswordError = false
                },
                label = stringResource(R.string.fa_reset_password_old_password_label),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = if (isOldPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    PasswordVisibilityToggle(isOldPasswordVisible) {
                        isOldPasswordVisible = !isOldPasswordVisible
                    }
                }
            )
            Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))
            TextInputLayout(
                modifier = VerticalLayoutModifier,
                value = newPassword,
                isError = isNewPasswordError,
                error = newPasswordError,
                onValueChange = {
                    newPassword = it
                    isNewPasswordError = false
                },
                label = stringResource(R.string.fa_reset_password_new_password_label),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password,
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                visualTransformation = if (isNewPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    PasswordVisibilityToggle(isNewPasswordVisible) {
                        isNewPasswordVisible = !isNewPasswordVisible
                    }
                }
            )
            Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))
            Button(
                enabled = arrayOf(oldPassword, newPassword).all { it.text.isNotBlank() },
                modifier = VerticalLayoutModifier,
                onClick = {
                    focusManager.clearFocus()
                    function.reset.invoke(
                        User.ResetPassword(oldPassword.text, newPassword.text, isChange),
                    )
                },
            ) {
                Text(toolbarTitle.uppercase())
            }
        }
    }
}

@Preview
@Composable
private fun PasswordResetLoadingPreview() {
    PasswordResetScreen(modifier = Modifier.fillMaxSize(), result = TaskResult.Loading)
}

@Preview
@Composable
private fun PasswordResetSuccessPreview() {
    PasswordResetScreen(
        modifier = Modifier.fillMaxSize(),
        result = TaskResult.Success(User.default()),
    )
}