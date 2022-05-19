package co.ke.xently.feature

import androidx.compose.runtime.Composable
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User

@JvmInline
value class PermissionGranted(val value: Boolean)

data class SharedFunction(
    val onNavigationIconClicked: () -> Unit = {},
    val currentlyActiveUser: @Composable () -> User? = { null },
    val onLocationPermissionChanged: (PermissionGranted) -> Unit = {},
    val signOutResult: @Composable () -> TaskResult<Unit> = { TaskResult.Success(Unit) },
)
