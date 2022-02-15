package co.ke.xently.accounts.ui.password_reset

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
import co.ke.xently.feature.ui.stringRes


@Composable
internal fun PasswordResetScreen(
    modifier: Modifier = Modifier,
    isChange: Boolean = false,
    viewModel: PasswordResetViewModel = hiltViewModel(),
    onSuccessfulReset: (User) -> Unit = {},
    onNavigationIconClicked: () -> Unit = {},
) {
    val result by viewModel.taskResult.collectAsState()
    PasswordResetScreen(
        modifier,
        result,
        isChange,
        onSuccessfulReset,
        onNavigationIconClicked,
        viewModel::resetPassword,
    )
}

@Composable
private fun PasswordResetScreen(
    modifier: Modifier,
    result: TaskResult<User?>,
    isChange: Boolean = false,
    onSuccessfulReset: (User) -> Unit = {},
    onNavigationIconClicked: () -> Unit = {},
    onResetClicked: (User.ResetPassword) -> Unit = {},
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
        SideEffect {
            oldPassword = TextFieldValue()
            newPassword = TextFieldValue()
            onSuccessfulReset(result.data!!)
        }
    }
    val focusManager = LocalFocusManager.current

    val toolbarTitle = stringRes(R.string.fa_reset_password_toolbar_title,
        if (isChange) R.string.fa_change else R.string.fa_reset)

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
                value = oldPassword,
                isError = isOldPasswordError,
                error = oldPasswordError,
                onValueChange = {
                    oldPassword = it
                    isOldPasswordError = false
                },
                label = stringResource(R.string.fa_reset_password_old_password_label),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next),
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
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            TextInputLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = newPassword,
                isError = isNewPasswordError,
                error = newPasswordError,
                onValueChange = {
                    newPassword = it
                    isNewPasswordError = false
                },
                label = stringResource(R.string.fa_reset_password_new_password_label),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done),
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
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Button(
                enabled = arrayOf(oldPassword, newPassword).all { it.text.isNotBlank() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = {
                    focusManager.clearFocus()
                    onResetClicked(User.ResetPassword(oldPassword.text,
                        newPassword.text,
                        isChange))
                }
            ) {
                Text(toolbarTitle.uppercase())
            }
        }
    }
}

@Preview
@Composable
private fun PasswordResetLoadingPreview() {
    PasswordResetScreen(Modifier.fillMaxSize(), TaskResult.Loading)
}

@Preview
@Composable
private fun PasswordResetErrorPreview() {
    PasswordResetScreen(Modifier.fillMaxSize(), TaskResult.Error("Error message"))
}

@Preview
@Composable
private fun PasswordResetSuccessPreview() {
    PasswordResetScreen(Modifier.fillMaxSize(), TaskResult.Success(User.default()))
}