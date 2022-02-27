package co.ke.xently.feature.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import co.ke.xently.common.KENYA
import co.ke.xently.common.TAG
import co.ke.xently.feature.R
import co.ke.xently.feature.theme.XentlyTheme
import co.ke.xently.source.remote.HttpException

val HORIZONTAL_PADDING = 16.dp

val NEGLIGIBLE_SPACE_BY = 2.dp

val VerticalLayoutModifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = HORIZONTAL_PADDING)

@Composable
fun rememberFragmentManager(): FragmentManager {
    val context = LocalContext.current
    return remember(context) {
        (context as FragmentActivity).supportFragmentManager
    }
}

@Composable
fun PasswordVisibilityToggle(isVisible: Boolean, onClick: () -> Unit) {
    IconButton(onClick) {
        Icon(
            if (isVisible) {
                Icons.Default.VisibilityOff
            } else {
                Icons.Default.Visibility
            },
            stringResource(
                R.string.toggle_password_visibility,
                if (isVisible) {
                    R.string.hide
                } else {
                    R.string.show
                },
            ),
        )
    }
}

@Composable
fun ToolbarWithProgressbar(
    title: String,
    onNavigationIconClicked: () -> Unit,
    showProgress: Boolean = false,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    navigationIcon: @Composable () -> Unit = {
        Icon(Icons.Default.ArrowBack, stringResource(R.string.move_back))
    },
    subTitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                if (subTitle.isNullOrBlank()) {
                    Text(title)
                } else {
                    Column {
                        Text(title, style = MaterialTheme.typography.body1)
                        Text(subTitle, style = MaterialTheme.typography.caption)
                    }
                }
            },
            actions = actions,
            elevation = elevation,
            contentColor = contentColor,
            backgroundColor = backgroundColor,
            navigationIcon = {
                IconButton(onNavigationIconClicked, content = navigationIcon)
            },
        )
        if (showProgress) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

val navigateToSignInScreen: (Context) -> Unit = {
    val intent = Intent(Intent.ACTION_VIEW, "xently://accounts/signin/".toUri())
    try {
        it.startActivity(intent)
    } catch (ex: ActivityNotFoundException) {
        Log.e(TAG, ex.message, ex)
    }
}

fun Throwable.isAuthError() = this is HttpException && statusCode == 401

@Composable
fun HttpErrorButton(
    error: Throwable,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    if (error.isAuthError()) {
        val context = LocalContext.current
        Button(
            modifier = modifier,
            onClick = onClick ?: { navigateToSignInScreen.invoke(context) },
        ) {
            Text(
                style = MaterialTheme.typography.button,
                text = stringResource(R.string.common_signin_button_text).uppercase(KENYA),
            )
        }
    }
}

@Composable
fun FullscreenError(
    modifier: Modifier,
    error: Throwable,
    httpSignInErrorClick: (() -> Unit)? = null,
    preErrorContent: @Composable ColumnScope.(Throwable) -> Unit = {},
    postErrorContent: @Composable ColumnScope.(Throwable) -> Unit = {
        HttpErrorButton(it, onClick = httpSignInErrorClick)
    },
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(HORIZONTAL_PADDING),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            preErrorContent(error)
            Text(
                textAlign = TextAlign.Center,
                text = error.localizedMessage ?: stringResource(R.string.generic_error_message),
            )
            postErrorContent(error)
        }
    }
}

@Composable
fun FullscreenLoading(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
inline fun <reified T : Any> FullscreenEmptyList(
    modifier: Modifier,
    @StringRes error: Int? = null,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            modifier = Modifier.padding(HORIZONTAL_PADDING),
            textAlign = TextAlign.Center,
            text = if (error != null) {
                stringResource(error)
            } else {
                stringResource(R.string.empty_list,
                    T::class.java.simpleName.mapIndexed { i, c -> if (i != 0 && c.isUpperCase()) " $c" else "$c" }
                        .joinToString("") { it }.lowercase())
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FullscreenEmptyListPreview() {
    XentlyTheme {
        FullscreenEmptyList<String>(Modifier.fillMaxSize())
    }
}

@Composable
inline fun <reified T : Any> PagedDataScreen(
    modifier: Modifier,
    items: LazyPagingItems<T>,
    @StringRes emptyListMessage: Int? = null,
    noinline httpSignInErrorClick: (() -> Unit)? = null,
    noinline preErrorContent: @Composable ColumnScope.(Throwable) -> Unit = {},
    noinline postErrorContent: @Composable ColumnScope.(Throwable) -> Unit = {
        HttpErrorButton(it, onClick = httpSignInErrorClick)
    },
    noinline noneEmptyItems: LazyListScope.() -> Unit,
) {
    when (val refresh = items.loadState.refresh) {
        is LoadState.Loading -> FullscreenLoading(modifier)
        is LoadState.Error -> {
            FullscreenError(
                modifier = modifier,
                error = refresh.error,
                preErrorContent = preErrorContent,
                postErrorContent = postErrorContent,
            )
        }
        is LoadState.NotLoading -> {
            if (items.itemCount == 0) {
                FullscreenEmptyList<T>(modifier, emptyListMessage)
            } else {
                LazyColumn(modifier = modifier, content = noneEmptyItems)
            }
        }
    }
}

@Composable
fun AppendOnPagedData(loadState: LoadState, scaffoldState: ScaffoldState) {
    when (loadState) {
        is LoadState.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
        is LoadState.Error -> {
            val message = (loadState.error.localizedMessage
                ?: stringResource(R.string.generic_error_message))
            LaunchedEffect(message) {
                scaffoldState.snackbarHostState.showSnackbar(message)
            }
        }
        is LoadState.NotLoading -> Unit
    }
}

@Composable
fun MultipleTextFieldRow(
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    error: String = "",
    content: @Composable RowScope.(Modifier) -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HORIZONTAL_PADDING / 2),
        ) {
            content(Modifier.weight(1f))
        }
        if (isError) {
            TextFieldErrorText(error, Modifier.fillMaxWidth())
        }
    }
}