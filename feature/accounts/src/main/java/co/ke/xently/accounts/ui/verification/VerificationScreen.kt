package co.ke.xently.accounts.ui.verification

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.accounts.R
import co.ke.xently.common.TAG
import co.ke.xently.common.replaceAt
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.data.errorMessage
import co.ke.xently.feature.ui.TextFieldErrorText
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds


@Composable
internal fun VerificationScreen(
    modifier: Modifier = Modifier,
    verificationCode: String = "",
    viewModel: VerificationViewModel = hiltViewModel(),
    onSuccessfulVerification: (User) -> Unit = {},
    onNavigationIconClicked: () -> Unit = {},
) {
    val result by viewModel.taskResult.collectAsState()
    VerificationScreen(
        modifier,
        result,
        verificationCode,
        onSuccessfulVerification,
        onNavigationIconClicked,
        viewModel::resendVerificationCode,
        viewModel::verifyAccount,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun VerificationScreen(
    modifier: Modifier,
    result: TaskResult<User?>,
    verificationCode: String = "",
    onSuccessfulVerification: (User) -> Unit = {},
    onNavigationIconClicked: () -> Unit = {},
    onResendClicked: () -> Unit = {},
    onVerifyClicked: (String) -> Unit = {},
) {
    var code by remember { mutableStateOf(verificationCode) }

    var codeError by remember { mutableStateOf("") }

    var isCodeError by remember { mutableStateOf(false) }

    // Avoid unnecessarily restarting count down for enabling resend code button
    var resendLastSavedCountDownSecond by rememberSaveable { mutableStateOf(60) }

    var resendCountDownSecond by remember { mutableStateOf(resendLastSavedCountDownSecond) }

    var resendCountDownFinished by remember { mutableStateOf(false) }

    LaunchedEffect(result !is TaskResult.Loading) {
        for (i in 1..resendLastSavedCountDownSecond) {
            delay(1.seconds)
            resendCountDownFinished = i == resendLastSavedCountDownSecond
            resendCountDownSecond = resendLastSavedCountDownSecond - i
        }
    }

    val scaffoldState = rememberScaffoldState()

    if (result is TaskResult.Loading) {
        resendLastSavedCountDownSecond = resendCountDownSecond
    } else if (result is TaskResult.Error) {
        codeError = ((result.error as? VerificationHttpException)?.code?.joinToString("\n")
            ?: "").also {
            isCodeError = it.isNotBlank()
        }

        if (!isCodeError) {
            val errorMessage =
                result.errorMessage ?: stringResource(R.string.generic_error_message)
            LaunchedEffect(result, errorMessage) {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
        code = ""  // Reset code...
    } else if (result is TaskResult.Success && result.data != null) {
        SideEffect {
            code = ""
            onSuccessfulVerification(result.data!!)
        }
    }
    val focusManager = LocalFocusManager.current

    val toolbarTitle = stringResource(R.string.fa_verify_account_toolbar_title)

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                toolbarTitle,
                onNavigationIconClicked,
                showProgress = result is TaskResult.Loading,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            val onCodeEntryFinished = {
                focusManager.clearFocus()
                onVerifyClicked(code)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    for (i in 0..5) {
                        TextField(
                            colors = TextFieldDefaults.textFieldColors(cursorColor = Color.Transparent),
                            enabled = result !is TaskResult.Loading,
                            value = code.getOrNull(i)?.toString() ?: "",
                            singleLine = true,
                            isError = isCodeError,
                            textStyle = MaterialTheme.typography.h5.copy(textAlign = TextAlign.Center),
                            modifier = Modifier
                                .weight(1f)
                                .onKeyEvent { keyEvent ->
                                    (keyEvent.key == Key.Backspace).also {
                                        if (it) {
                                            focusManager.moveFocus(FocusDirection.Previous)
                                            if (i > 0) code = code.take(i - 1)
                                        }
                                    }
                                }
                                .onFocusEvent {
                                    // Don't permit focus if previous fields have no values.
                                    // Previous fields must be preceded by a text field!
                                    if (it.isFocused && i > 0 && code.length < i) {
                                        try {
                                            focusManager.moveFocus(FocusDirection.Previous)
                                        } catch (ex: IllegalStateException) {
                                            Log.e(TAG,
                                                "VerificationScreen: very weird error!",
                                                ex)
                                        } catch (ex: IllegalArgumentException) {
                                            Log.e(TAG,
                                                "VerificationScreen: very weird error!",
                                                ex)
                                        }
                                    }
                                },
                            onValueChange = {
                                val thisCode = it.getOrNull(0)?.toString() ?: ""
                                code = if (code.length > i) {
                                    code.replaceAt(i, thisCode)
                                } else {
                                    "${code}$thisCode"
                                }
                                isCodeError = false
                                if (it.isNotEmpty()) {
                                    if (i != 5) {
                                        focusManager.moveFocus(FocusDirection.Next)
                                    } else if (code.length == 6) {
                                        // The sixth code should trigger verification
                                        onCodeEntryFinished()
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = if (i == 5) ImeAction.Done else ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(onDone = { onCodeEntryFinished() }),
                        )
                    }
                }
                if (isCodeError) {
                    TextFieldErrorText(codeError, Modifier.fillMaxWidth())
                }
            }
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(
                    enabled = resendCountDownFinished && result !is TaskResult.Loading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary),
                    onClick = {
                        // Reset count down to original starting point
                        resendLastSavedCountDownSecond = 60
                        focusManager.clearFocus()
                        onResendClicked()
                    },
                ) {
                    val resendButtonLabel = if (resendCountDownSecond > 0) {
                        // Show seconds counting down to when resend button will be enabled
                        stringResource(
                            R.string.fa_verify_account_resend_code_count_down_label,
                            resendCountDownSecond,
                        )
                    } else {
                        stringResource(R.string.fa_verify_account_resend_code_label).uppercase()
                    }
                    Text(resendButtonLabel)
                }
                Button(
                    enabled = code.length == 6 && result !is TaskResult.Loading,
                    modifier = Modifier.weight(1f),
                    onClick = onCodeEntryFinished,
                ) {
                    Text(toolbarTitle.uppercase())
                }
            }
        }
    }
}

@Preview
@Composable
private fun VerificationLoadingPreview() {
    VerificationScreen(Modifier.fillMaxSize(), TaskResult.Loading, "12345")
}

@Preview
@Composable
private fun VerificationErrorPreview() {
    VerificationScreen(Modifier.fillMaxSize(), TaskResult.Error("Error message"), "12345")
}

@Preview
@Composable
private fun VerificationSuccessPreview() {
    VerificationScreen(Modifier.fillMaxSize(), TaskResult.Success(User.default()), "12345")
}