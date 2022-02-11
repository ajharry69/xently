package co.ke.xently.feature.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import co.ke.xently.feature.R
import co.ke.xently.feature.theme.XentlyTheme

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
        val resource = if (isVisible) {
            R.drawable.ic_password_visible
        } else {
            R.drawable.ic_password_invisible
        }
        Icon(painterResource(resource), stringResource(R.string.toggle_password_visibility))
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
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            { Text(title) },
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

@Composable
fun FullscreenError(modifier: Modifier, message: String?, onButtonClicked: () -> Unit = {}) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(message ?: stringResource(R.string.generic_error_message))
            Button(onClick = onButtonClicked) {
                Text(stringResource(R.string.retry).uppercase())
            }
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
            if (error != null) {
                stringResource(error)
            } else {
                stringResource(R.string.empty_list,
                    T::class.java.simpleName.mapIndexed { i, c -> if (i != 0 && c.isUpperCase()) " $c" else "$c" }
                        .joinToString("") { it }.lowercase())
            },
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FullscreenEmptyListPreview() {
    XentlyTheme {
        FullscreenEmptyList<String>(Modifier.fillMaxSize())
    }
}

@Composable
inline fun <reified T : Any> PagedDataScreen(
    modifier: Modifier,
    items: LazyPagingItems<T>,
    @StringRes error: Int? = null,
    noinline noneEmptyItems: LazyListScope.() -> Unit,
) {
    when (val refresh = items.loadState.refresh) {
        is LoadState.Loading -> FullscreenLoading(modifier)
        is LoadState.Error -> FullscreenError(modifier, refresh.error.localizedMessage)
        is LoadState.NotLoading -> {
            if (items.itemCount == 0) {
                FullscreenEmptyList<T>(modifier, error)
            } else {
                LazyColumn(
                    modifier = modifier,
                    content = noneEmptyItems,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                )
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