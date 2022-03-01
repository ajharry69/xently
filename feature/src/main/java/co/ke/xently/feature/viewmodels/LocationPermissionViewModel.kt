package co.ke.xently.feature.viewmodels

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
open class LocationPermissionViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
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

    private companion object {
        private val KEY =
            "${LocationPermissionViewModel::class.java.name}.locationPermissionsGranted"
        private val PERMISSIONS = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
    }
}