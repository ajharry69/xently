package co.ke.xently.feature.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ke.xently.feature.theme.XentlyTheme
import kotlinx.coroutines.launch

private val DRAWER_HORIZONTAL_PADDING = 22.dp
val DRAWER_HEADER_HEIGHT = 176.dp

data class NavDrawerItem(
    val label: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit = {},
) {
    constructor(
        context: Context,
        @StringRes label: Int,
        icon: ImageVector? = null,
        onClick: () -> Unit = {},
    ) : this(context.getString(label), icon, onClick)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavigationDrawerItem(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isSelected: Boolean = false,
    isCheckable: Boolean = true,
    onClick: () -> Unit = {},
) {
    var selected by remember(isSelected) {
        mutableStateOf(isSelected)
    }
    Surface(
        modifier = modifier.padding(horizontal = DRAWER_HORIZONTAL_PADDING),
        color = if (selected) {
            MaterialTheme.colors.primary.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        },
        shape = MaterialTheme.shapes.small,
        onClick = {
            selected = if (isCheckable) {
                !selected
            } else {
                isSelected
            }
            onClick.invoke()
        },
        onClickLabel = label,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .padding(all = 14.dp)
                    .size(size = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (icon != null) {
                    Icon(
                        modifier = Modifier.fillMaxSize(),
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) {
                            MaterialTheme.colors.primary
                        } else {
                            MaterialTheme.colors.onSurface
                        },
                    )
                }
            }
            Text(
                text = label,
                color = if (selected) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.onSurface
                },
                style = MaterialTheme.typography.subtitle2,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun NavigationDrawerGroup(
    drawerItems: List<NavDrawerItem>,
    modifier: Modifier = Modifier,
    drawerState: DrawerState? = null,
    title: String? = null,
    isCheckable: Boolean = true,
) {
    var currentlySelected by remember {
        mutableStateOf(0)
    }
    val coroutineScope = rememberCoroutineScope()

    val content: @Composable (Modifier) -> Unit = {
        Column(modifier = it) {
            for ((index, item) in drawerItems.withIndex()) {
                NavigationDrawerItem(
                    modifier = Modifier.fillMaxWidth(),
                    label = item.label,
                    icon = item.icon,
                    onClick = {
                        if (currentlySelected != index) {
                            currentlySelected = index
                        }
                        item.onClick.invoke()
                        drawerState?.also { state ->
                            coroutineScope.launch {
                                if (state.isOpen) {
                                    state.close()
                                }
                            }
                        }
                    },
                    isCheckable = false,
                    isSelected = isCheckable && index == currentlySelected,
                )
            }
        }
    }
    if (title == null) {
        content(modifier)
    } else {
        Column(modifier = modifier) {
            Text(
                text = title,
                maxLines = 1,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSecondary,
                modifier = Modifier.padding(horizontal = DRAWER_HORIZONTAL_PADDING),
            )
            content(Modifier.fillMaxWidth())
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NavigationDrawerGroup() {
    XentlyTheme {
        Column(Modifier.fillMaxSize()) {
            NavigationDrawerGroup(
                modifier = Modifier.fillMaxWidth(),
                drawerItems = listOf(
                    NavDrawerItem(icon = Icons.Default.Person, label = "Account"),
                    NavDrawerItem(icon = Icons.Default.Favorite, label = "Favourites"),
                    NavDrawerItem(label = "No icon"),
                ),
            )
            NavigationDrawerGroup(
                modifier = Modifier.fillMaxWidth(),
                title = "Others",
                isCheckable = false,
                drawerItems = listOf(
                    NavDrawerItem(icon = Icons.Default.Settings, label = "Settings"),
                    NavDrawerItem(icon = Icons.Default.Help, label = "Help"),
                ),
            )
        }
    }
}