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

val DEFAULT_VERTICAL_SPACING_ONLY = PaddingValues(vertical = VIEW_SPACE_HALVED)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListItemSurface(
    modifier: Modifier,
    paddingValues: PaddingValues = PaddingValues(
        start = VIEW_SPACE,
        top = VIEW_SPACE_HALVED,
        bottom = VIEW_SPACE_HALVED,
    ),
    onClick: () -> Unit = {},
    content: @Composable (RowScope.() -> Unit),
) {
    Surface(modifier = modifier, onClick = onClick) {
        Box(modifier = Modifier.padding(paddingValues = paddingValues)) {
            Row(
                content = content,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            )
        }
    }
}