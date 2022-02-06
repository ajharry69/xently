package co.ke.xently.feature.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import co.ke.xently.feature.R
import co.ke.xently.feature.theme.XentlyTheme


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
                    T::class.java.simpleName.lowercase())
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
inline fun <reified T : Any> prependOnPagedData(
    modifier: Modifier,
    items: LazyPagingItems<T>,
    @StringRes error: Int? = null,
): Unit? {
    when (val refresh = items.loadState.refresh) {
        is LoadState.Loading -> return FullscreenLoading(modifier)
        is LoadState.Error -> return FullscreenError(modifier, refresh.error.localizedMessage)
        is LoadState.NotLoading -> {
            if (items.itemCount == 0) return FullscreenEmptyList<T>(modifier, error)
        }
    }
    return null
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