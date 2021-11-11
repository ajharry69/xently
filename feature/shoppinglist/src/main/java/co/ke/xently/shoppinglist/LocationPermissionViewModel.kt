package co.ke.xently.shoppinglist

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Application
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
open class LocationPermissionViewModel @Inject constructor(
    app: Application,
    private val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(app) {
    val locationPermissionsGranted: LiveData<Boolean> =
        Transformations.map(
            savedStateHandle.getLiveData(
                KEY,
                if (arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION).all {
                        checkSelfPermission(app.applicationContext, it) == PERMISSION_GRANTED
                    }) 1 else 0
            )
        ) { it == 1 }

    fun setLocationPermissionGranted(granted: Boolean) {
        savedStateHandle[KEY] = if (granted) 1 else 0
    }

    private companion object {
        private val KEY =
            "${LocationPermissionViewModel::class.java.name}.locationPermissionsGranted"
    }
}