package co.ke.xently.feature.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer

fun Modifier.shimmerPlaceholder(visible: Boolean) = composed {
    placeholder(
        visible = visible,
        highlight = PlaceholderHighlight.shimmer(),
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListItemSurface(
    modifier: Modifier,
    onClick: () -> Unit = {},
    content: @Composable (RowScope.() -> Unit),
) {
    Surface(modifier = modifier, onClick = onClick) {
        Row(
            content = content,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = HORIZONTAL_PADDING)
                .padding(vertical = HORIZONTAL_PADDING / 2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        )
    }
}