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
import co.ke.xently.feature.ui.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

internal data class VerificationScreenFunction(
    val navigationIcon: () -> Unit = {},
    val verificationSuccess: (User) -> Unit = {},
    val resendCode: () -> Unit = {},
    val verify: (String) -> Unit = {},
)

@Composable
internal fun VerificationScreen(
    modifier: Modifier,
    verificationCode: String,
    function: VerificationScreenFunction,
    viewModel: VerificationViewModel = hiltViewModel(),
) {
    val result by viewModel.taskResult.collectAsState()
    VerificationScreen(
        modifier = modifier,
        result = result,
        verificationCode = verificationCode,
        function = function.copy(
            verify = viewModel::verifyAccount,
            resendCode = viewModel::resendVerificationCode,
        ),
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun VerificationScreen(
    modifier: Modifier,
    result: TaskResult<User?>,
    verificationCode: String = "",
    function: VerificationScreenFunction = VerificationScreenFunction(),
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
        code = ""
        SideEffect {
            function.verificationSuccess.invoke(result.data!!)
        }
    }
    val focusManager = LocalFocusManager.current

    val toolbarTitle = stringResource(R.string.fa_verify_account_toolbar_title)

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
            val onCodeEntryFinished = {
                focusManager.clearFocus()
                function.verify.invoke(code)
            }
            Column(modifier = VerticalLayoutModifier.padding(top = VIEW_SPACE)) {
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
                                imeAction = if (i == 5) {
                                    ImeAction.Done
                                } else {
                                    ImeAction.Next
                                },
                            ),
                            keyboardActions = KeyboardActions(onDone = { onCodeEntryFinished() }),
                        )
                    }
                }
                if (isCodeError) {
                    TextFieldErrorText(codeError, Modifier.fillMaxWidth())
                }
            }
            Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))
            Row(
                modifier = VerticalLayoutModifier,
                horizontalArrangement = Arrangement.spacedBy(VIEW_SPACE),
            ) {
                Button(
                    enabled = resendCountDownFinished && result !is TaskResult.Loading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary),
                    onClick = {
                        // Reset count down to original starting point
                        resendLastSavedCountDownSecond = 60
                        focusManager.clearFocus()
                        function.resendCode.invoke()
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
    VerificationScreen(
        verificationCode = "12345",
        result = TaskResult.Loading,
        modifier = Modifier.fillMaxSize(),
    )
}

@Preview
@Composable
private fun VerificationSuccessPreview() {
    VerificationScreen(
        verificationCode = "12345",
        modifier = Modifier.fillMaxSize(),
        result = TaskResult.Success(User.default()),
    )
}