package co.ke.xently.feature.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ke.xently.feature.R
import co.ke.xently.feature.theme.XentlyTheme

@Composable
private fun ChipContent(
    isCheckable: Boolean,
    isChecked: Boolean,
    thumbnail: (@Composable (Modifier) -> Unit)?,
    text: String,
    onClose: (() -> Unit)?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, end = 6.dp),
    ) {
        val iconModifier = Modifier.size(size = 24.dp)
        if (thumbnail != null) {
            if (isCheckable && isChecked) {
                Icon(painterResource(R.drawable.ic_mtrl_chip_checked_circle), null, iconModifier)
            } else {
                thumbnail(iconModifier)
            }
        } else if (isCheckable && isChecked) {
            Icon(painterResource(R.drawable.ic_mtrl_chip_checked_circle), null, iconModifier)
        }
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(start = 8.dp, end = 6.dp),
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.87f),
        )
        if (onClose != null) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(size = 18.dp)
                    .padding(horizontal = 2.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_mtrl_chip_close_circle),
                    stringResource(R.string.mtrl_chip_close_icon_content_description, text),
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.87f),
                )
            }
        }
    }
}

enum class ChipType {
    ACTION,
    FILTER,
    CHOICE,
}

/**
 * Specs: https://material.io/components/chips/android#action-chip
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    type: ChipType = ChipType.ACTION,
    checkable: Boolean = false,
    enabled: Boolean = true,
    thumbnail: (@Composable (Modifier) -> Unit)? = null,
    border: BorderStroke? = null,
    onCheckChanged: ((Boolean) -> Unit)? = null,
    onClose: (() -> Unit)? = null,
) {
    val isCheckable = checkable || type in arrayOf(ChipType.CHOICE, ChipType.FILTER)

    val shape = MaterialTheme.shapes.small.copy(all = CornerSize(50))
    val color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
    val m = modifier.requiredHeightIn(min = 32.dp)

    var isChecked by remember { mutableStateOf(false) }

    val content = @Composable { ChipContent(isCheckable, isChecked, thumbnail, text, onClose) }
    if (isCheckable) {
        onCheckChanged
            ?: error("Improperly configured. `onCheckChanged` cannot be null for a checkable chip.")
        Surface(
            modifier = m,
            color = color,
            shape = shape,
            border = border,
            enabled = enabled,
            content = content,
            onClick = {
                isChecked = !isChecked
                onCheckChanged.invoke(isChecked)
            },
        )
    } else {
        Surface(modifier = m, color = color, shape = shape, border = border, content = content)
    }
}

@Composable
fun <T> ChipGroup(
    modifier: Modifier = Modifier,
    isSingleLine: Boolean = true,
    chipItems: Iterable<T> = emptyList(),
    chipItem: @Composable (Int, T) -> Unit,
) {
    if (isSingleLine) {
        LazyRow(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(chipItems.toList()) { index, item ->
                chipItem(index, item)
            }
        }
    } else {
        FlowRow(modifier = modifier, verticalSpacing = 8.dp, horizontalSpacing = 8.dp) {
            for ((index, item) in chipItems.withIndex()) {
                chipItem(index, item)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChipGroup() {
    XentlyTheme {
        Column(
            Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Single line", style = MaterialTheme.typography.h5)
            ChipGroup(
                Modifier.fillMaxWidth(),
                chipItems = List(20) { "Action ${it + 1}" },
            ) { _, str ->
                Chip(text = str)
            }
            Text("Multi-line", style = MaterialTheme.typography.h5)
            ChipGroup(
                Modifier.fillMaxWidth(),
                isSingleLine = false,
                chipItems = List(20) { "Action ${it + 1}" },
            ) { _, str ->
                Chip(text = str)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Chip() {
    XentlyTheme {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip(text = "Action")
            Chip(text = "Disabled action", enabled = false)
            Chip(text = "Checkable action", checkable = true, onCheckChanged = {})
            val thumbnail: @Composable (Modifier) -> Unit = { Icon(Icons.Default.Person, null, it) }
            Chip(text = "Action with thumbnail", thumbnail = thumbnail)
            Chip(text = "Disabled with thumbnail", thumbnail = thumbnail, enabled = false)
            Chip(
                text = "Checkable action with thumbnail",
                thumbnail = thumbnail,
                type = ChipType.CHOICE,
                onCheckChanged = {},
            )
            Chip(
                text = "Checkable disabled with thumbnail",
                thumbnail = thumbnail,
                type = ChipType.CHOICE,
                enabled = false,
                onCheckChanged = {},
            )
            Chip(text = "Very very very very very long chip text") {}
            Chip(text = "Very very very very very long chip text", thumbnail = thumbnail) {}
        }
    }
}