package co.ke.xently.feature

import androidx.compose.runtime.Composable
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User

data class SharedFunction(
    val onNavigationIconClicked: () -> Unit = {},
    val currentlyActiveUser: @Composable () -> User? = { null },
    val onLocationPermissionChanged: (permissionGranted: Boolean) -> Unit = {},
    val signOutResult: @Composable () -> TaskResult<Unit> = { TaskResult.Success(Unit) },
)
