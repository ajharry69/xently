package co.ke.xently.feature.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ke.xently.feature.R
import co.ke.xently.feature.theme.XentlyTheme
import kotlinx.coroutines.launch

private val DRAWER_HORIZONTAL_PADDING = 22.dp
val DRAWER_HEADER_HEIGHT = 176.dp

data class NavMenuItem(
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
private fun NavigationDrawerItem(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isSelected: Boolean = false,
    @Suppress("SameParameterValue") isCheckable: Boolean = true,
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
private fun NavigationDrawerGroup(
    menuItems: List<NavMenuItem>,
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
            for ((index, item) in menuItems.withIndex()) {
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
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .padding(horizontal = DRAWER_HORIZONTAL_PADDING),
                contentAlignment = Alignment.BottomStart,
            ) {
                Text(
                    text = title,
                    maxLines = 1,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSecondary,
                )
            }
            content(Modifier.fillMaxWidth())
        }
    }
}


data class NavDrawerGroupItem(
    val items: List<NavMenuItem>,
    val title: String? = null,
    val checkable: Boolean = true,
) {
    init {
        if (items.isEmpty()) {
            throw IllegalStateException("Menu items cannot be empty")
        }
    }
}

@SuppressLint("ComposableNaming")
@Composable
fun ColumnScope.NavigationDrawer(
    navGroups: List<NavDrawerGroupItem>,
    drawerState: DrawerState? = null,
    headerContentAlignment: Alignment = Alignment.BottomCenter,
    headerContent: @Composable (BoxScope.() -> Unit)? = null,
) = apply {
    if (headerContent != null) {
        Box(
            modifier = Modifier
                .height(DRAWER_HEADER_HEIGHT)
                .fillMaxWidth(),
            content = headerContent,
            contentAlignment = headerContentAlignment,
        )
        Divider(modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(VIEW_SPACE / 2))
    }
    for (groupItem in navGroups) {
        NavigationDrawerGroup(
            title = groupItem.title,
            drawerState = drawerState,
            menuItems = groupItem.items,
            isCheckable = groupItem.checkable,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NavigationDrawerGroup() {
    XentlyTheme {
        Column(Modifier.fillMaxSize()) {
            NavigationDrawer(
                navGroups = listOf(
                    NavDrawerGroupItem(
                        items = listOf(
                            NavMenuItem(icon = Icons.Default.Person, label = "Account"),
                            NavMenuItem(icon = Icons.Default.Favorite, label = "Favourites"),
                            NavMenuItem(label = "No icon"),
                        ),
                    ),
                    NavDrawerGroupItem(
                        title = "Title",
                        checkable = false,
                        items = listOf(
                            NavMenuItem(icon = Icons.Default.Settings, label = "Settings"),
                            NavMenuItem(icon = Icons.Default.Help, label = "Help"),
                        ),
                    )
                ),
            ) {
                Image(
                    painterResource(R.drawable.ic_launcher_background),
                    null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}