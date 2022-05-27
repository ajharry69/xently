package co.ke.xently.recommendation.ui

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal open class MyUpdatedLocationViewModel @Inject constructor() : ViewModel() {
    var myDefaultLocation: LatLng? = null
        private set

    fun setMyDefaultLocation(myDefaultLocation: LatLng) {
        this.myDefaultLocation = myDefaultLocation
    }
}