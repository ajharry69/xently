package co.ke.xently

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import co.ke.xently.feature.repository.IAuthRepository
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.feature.viewmodels.AbstractAuthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: IAuthRepository,
    private val savedStateHandle: SavedStateHandle,
) : AbstractAuthViewModel(repository = repository) {
    val locationPermissionsGranted: LiveData<Boolean> = Transformations.map(
        savedStateHandle.getLiveData(
            KEY,
            if (PERMISSIONS.all { checkSelfPermission(context, it) == PERMISSION_GRANTED }) {
                1
            } else {
                0
            }
        )
    ) { it == 1 }

    fun setLocationPermissionGranted(granted: Boolean) {
        savedStateHandle[KEY] = if (granted) {
            1
        } else {
            0
        }
    }

    private val signout = MutableSharedFlow<Boolean>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val signOutResult = signout.flatMapLatest {
        repository.signOut().flagLoadingOnStart()
    }.shareIn(viewModelScope, SharingStarted.Lazily)

    fun signOut() {
        viewModelScope.launch {
            signout.emit(true)
        }
    }

    private companion object {
        private val KEY =
            "${MainActivityViewModel::class.java.name}.locationPermissionsGranted"
        private val PERMISSIONS = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
    }
}