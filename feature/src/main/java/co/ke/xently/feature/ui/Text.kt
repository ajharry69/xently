package co.ke.xently.feature.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun TextFieldErrorText(error: String, modifier: Modifier = Modifier) {
    Text(
        error,
        modifier = modifier.padding(start = 16.dp, end = 12.dp),
        color = MaterialTheme.colors.error,
        style = MaterialTheme.typography.caption,
    )
}