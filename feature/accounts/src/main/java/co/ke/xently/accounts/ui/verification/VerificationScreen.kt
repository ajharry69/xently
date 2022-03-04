package co.ke.xently.accounts.ui.verification

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import co.ke.xently.feature.ui.ToolbarWithProgressbar
import co.ke.xently.feature.ui.VIEW_SPACE
import co.ke.xently.feature.ui.VIEW_SPACE_HALVED
import co.ke.xently.feature.ui.VerticalLayoutModifier

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
    val scope = rememberCoroutineScope()
    val verifyResult by viewModel.verifyResult.collectAsState(
        context = scope.coroutineContext,
        initial = TaskResult.Success(null),
    )
    val resendResult by viewModel.resendResult.collectAsState(
        context = scope.coroutineContext,
        initial = TaskResult.Success(null),
    )
    val resendCountDownSecond by viewModel.resendCountDownSecond.collectAsState(
        context = scope.coroutineContext,
    )
    VerificationScreen(
        modifier = modifier,
        verifyResult = verifyResult,
        resendResult = resendResult,
        verificationCode = verificationCode,
        resendCountDownSecond = resendCountDownSecond,
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
    verifyResult: TaskResult<User?>,
    resendResult: TaskResult<User?>,
    resendCountDownSecond: Int = 60,
    verificationCode: String = "",
    function: VerificationScreenFunction = VerificationScreenFunction(),
) {
    var code by remember { mutableStateOf(verificationCode) }

    val scaffoldState = rememberScaffoldState()

    var codeError by remember { mutableStateOf("") }
    if (verifyResult is TaskResult.Error) {
        val exception = verifyResult.error as? VerificationHttpException
        codeError = exception?.code?.joinToString("\n") ?: ""

        if (exception?.hasFieldErrors() != true) {
            val errorMessage =
                verifyResult.errorMessage ?: stringResource(R.string.generic_error_message)
            LaunchedEffect(verifyResult, errorMessage) {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
        code = ""  // Reset code...
    } else if (resendResult is TaskResult.Success && resendResult.data != null) {
        SideEffect {
            function.verificationSuccess.invoke(resendResult.data!!)
        }
    }
    val focusManager = LocalFocusManager.current

    val toolbarTitle = stringResource(R.string.fa_verify_account_toolbar_title)

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                title = toolbarTitle,
                showProgress = arrayOf(resendResult, verifyResult).any { it is TaskResult.Loading },
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
                var isCodeError by remember(codeError) {
                    mutableStateOf(codeError.isNotBlank())
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    for (i in 0..5) {
                        TextField(
                            colors = TextFieldDefaults.textFieldColors(cursorColor = Color.Transparent),
                            enabled = resendResult !is TaskResult.Loading,
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
                    Text(
                        text = codeError,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 12.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.padding(vertical = VIEW_SPACE_HALVED))
            Row(
                modifier = VerticalLayoutModifier,
                horizontalArrangement = Arrangement.spacedBy(VIEW_SPACE),
            ) {
                Button(
                    enabled = resendCountDownSecond == 0 && resendResult !is TaskResult.Loading,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.secondary),
                    onClick = {
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
                    enabled = code.length == 6 && verifyResult !is TaskResult.Loading,
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
        verifyResult = TaskResult.Loading,
        resendResult = TaskResult.Loading,
        modifier = Modifier.fillMaxSize(),
    )
}

@Preview
@Composable
private fun VerificationSuccessPreview() {
    VerificationScreen(
        verificationCode = "12345",
        modifier = Modifier.fillMaxSize(),
        verifyResult = TaskResult.Success(User.default()),
        resendResult = TaskResult.Success(User.default()),
    )
}