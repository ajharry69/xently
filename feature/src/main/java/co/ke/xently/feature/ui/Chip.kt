package co.ke.xently.feature.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ke.xently.feature.R
import co.ke.xently.feature.theme.XentlyTheme

@Composable
private fun ChipContent(
    isCheckable: Boolean,
    isChecked: Boolean,
    icon: ImageVector?,
    text: String,
    onClose: (() -> Unit)?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, end = 6.dp),
    ) {
        val iconModifier = Modifier.size(size = 24.dp)
        if (icon != null) {
            if (isCheckable && isChecked) {
                Icon(painterResource(R.drawable.ic_mtrl_chip_checked_circle), null, iconModifier)
            } else {
                Icon(icon, null, modifier = iconModifier)
            }
        } else if (isCheckable && isChecked) {
            Icon(painterResource(R.drawable.ic_mtrl_chip_checked_circle), null, iconModifier)
        }
        Text(
            text = text,
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
    icon: ImageVector? = null,
    border: BorderStroke? = null,
    onCheckChanged: ((Boolean) -> Unit)? = null,
    onClose: (() -> Unit)? = null,
) {
    val isCheckable = checkable || type in arrayOf(ChipType.CHOICE, ChipType.FILTER)

    val shape = MaterialTheme.shapes.small.copy(all = CornerSize(50))
    val color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
    val m = modifier.requiredHeightIn(min = 32.dp)

    var isChecked by remember { mutableStateOf(false) }

    val content = @Composable { ChipContent(isCheckable, isChecked, icon, text, onClose) }
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
            role = Role.Checkbox,
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
    chipItems: List<T> = emptyList(),
    chipItem: @Composable (T) -> Unit,
) {
    if (isSingleLine) {
        LazyRow(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(chipItems) {
                chipItem(it)
            }
        }
    } else {
        FlowRow(modifier = modifier, verticalSpacing = 8.dp, horizontalSpacing = 8.dp) {
            for (item in chipItems) {
                chipItem(item)
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
            ) {
                Chip(text = it)
            }
            Text("Multi-line", style = MaterialTheme.typography.h5)
            ChipGroup(
                Modifier.fillMaxWidth(),
                isSingleLine = false,
                chipItems = List(20) { "Action ${it + 1}" },
            ) {
                Chip(text = it)
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
            Chip(text = "Action with thumbnail", icon = Icons.Default.Person)
            Chip(text = "Disabled with thumbnail", icon = Icons.Default.Person, enabled = false)
            Chip(text = "Checkable action with thumbnail",
                icon = Icons.Default.Person,
                type = ChipType.CHOICE,
                onCheckChanged = {})
            Chip(text = "Checkable disabled with thumbnail",
                icon = Icons.Default.Person,
                type = ChipType.CHOICE,
                enabled = false,
                onCheckChanged = {})
            Chip(text = "Very very very very very long chip text") {}
            Chip(text = "Very very very very very long chip text", icon = Icons.Default.Person) {}
        }
    }
}