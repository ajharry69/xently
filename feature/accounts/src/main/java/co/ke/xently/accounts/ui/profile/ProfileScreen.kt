package co.ke.xently.accounts.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.accounts.R
import co.ke.xently.common.KENYA
import co.ke.xently.data.TaskResult
import co.ke.xently.data.TaskResult.Loading
import co.ke.xently.data.User
import co.ke.xently.data.errorMessage
import co.ke.xently.data.getOrNull
import co.ke.xently.feature.SharedFunction
import co.ke.xently.feature.repository.AccountHttpException
import co.ke.xently.feature.repository.error
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.feature.ui.*

internal data class ProfileScreenFunction(
    val update: (User) -> Unit = {},
    internal val sharedFunction: SharedFunction = SharedFunction(),
)

@Composable
internal fun ProfileScreen(
    modifier: Modifier,
    function: ProfileScreenFunction,
    userId: Long? = null,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val fetchResult by viewModel.fetchResult.collectAsState(
        initial = Loading,
        context = scope.coroutineContext,
    )
    val updateResult by viewModel.updateResult.collectAsState(
        context = scope.coroutineContext,
        initial = TaskResult.Success(null),
    )
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }
    ProfileScreen(
        modifier = modifier,
        fetchResult = fetchResult,
        updateResult = updateResult,
        function = function.copy(update = viewModel::update),
    )
}

@Composable
private fun ProfileScreen(
    fetchResult: TaskResult<User>,
    updateResult: TaskResult<User?>,
    modifier: Modifier = Modifier,
    function: ProfileScreenFunction = ProfileScreenFunction(),
) {
    val user = fetchResult.getOrNull()
    val scaffoldState = rememberScaffoldState()
    var isEditMode by remember {
        mutableStateOf(false)
    }
    var email by remember(user?.email) {
        mutableStateOf(TextFieldValue(user?.email ?: ""))
    }
    var emailError by remember { mutableStateOf("") }

    if (updateResult is TaskResult.Error) {
        val exception = updateResult.error as? AccountHttpException
        emailError = exception.error.email

        if (exception?.hasFieldErrors() != true) {
            val errorMessage =
                updateResult.errorMessage ?: stringResource(R.string.generic_error_message)
            LaunchedEffect(user?.id, updateResult, errorMessage) {
                scaffoldState.snackbarHostState.showSnackbar(errorMessage)
            }
        }
    }
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            ToolbarWithProgressbar(
                showProgress = fetchResult is Loading || updateResult is Loading,
                onNavigationIconClicked = function.sharedFunction.onNavigationIconClicked,
                title = stringResource(R.string.fa_account_profile_screen_title),
            ) {
                IconButton(onClick = { isEditMode = !isEditMode }) {
                    Icon(
                        imageVector = if (isEditMode) {
                            Icons.Default.Visibility
                        } else {
                            Icons.Default.Edit
                        },
                        contentDescription = stringRes(
                            R.string.fa_account_profile_edit_detail_content_description,
                            if (isEditMode) {
                                R.string.view
                            } else {
                                R.string.edit
                            },
                        ),
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            var isEmailError by remember {
                mutableStateOf(emailError.isNotBlank())
            }
            TextInputLayout(
                value = email,
                readOnly = !isEditMode,
                error = emailError,
                isError = isEmailError,
                onValueChange = {
                    email = it
                    isEmailError = false
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                helpText = stringResource(R.string.fa_profile_email_help_text),
                modifier = VerticalLayoutModifier.padding(
                    top = VIEW_SPACE,
                    bottom = VIEW_SPACE_HALVED,
                ),
            )
            Button(
                enabled = arrayOf(
                    email,
                ).all { it.text.isNotBlank() } && updateResult !is Loading,
                modifier = VerticalLayoutModifier.padding(top = VIEW_SPACE_HALVED),
                onClick = { function.update.invoke(user!!) },
            ) {
                Text(
                    style = MaterialTheme.typography.button,
                    text = stringResource(R.string.update).uppercase(KENYA),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    XentlyTheme {
        ProfileScreen(
            modifier = Modifier.fillMaxSize(),
            fetchResult = TaskResult.Success(User.default()),
            updateResult = TaskResult.Success(User.default()),
        )
    }
}