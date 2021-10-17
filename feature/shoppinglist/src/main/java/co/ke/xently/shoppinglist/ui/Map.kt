package co.ke.xently.shoppinglist.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import androidx.core.app.ActivityCompat
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap


@Composable
internal fun GoogleMapView(
    modifier: Modifier,
    currentPosition: LatLng,
    markerPositions: Array<LatLng>,
    onLocationPermissionNotGranted: (GoogleMap) -> Unit,
    onMapViewUpdated: (MapView) -> Unit = NoOpUpdate,
) {
    // The MapView lifecycle is handled by this composable. As the MapView also needs to be updated
    // with input from Compose UI, those updates are encapsulated into the GoogleMapViewContainer
    // composable. In this way, when an update to the MapView happens, this composable won't
    // recompose and the MapView won't need to be recreated.
    val mapView = rememberMapViewWithLifecycle()
    GoogleMapViewContainer(
        modifier,
        mapView,
        currentPosition,
        markerPositions,
        onLocationPermissionNotGranted,
        onMapViewUpdated,
    )
}

private suspend fun MapView.xentlyAwaitedMap(onLocationPermissionNotGranted: (GoogleMap) -> Unit): GoogleMap {
    return awaitMap().apply {
        uiSettings.apply {
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
            isMyLocationButtonEnabled = true
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            onLocationPermissionNotGranted(this)
        } else isMyLocationEnabled = true
    }
}

@Composable
internal fun GoogleMapViewContainer(
    modifier: Modifier,
    map: MapView,
    currentPosition: LatLng,
    markerPositions: Array<LatLng>,
    onLocationPermissionNotGranted: (GoogleMap) -> Unit,
    onMapViewUpdated: (MapView) -> Unit = NoOpUpdate,
) {
    val cameraPositions = remember(*markerPositions) { markerPositions }

    LaunchedEffect(map) {
        val googleMap = map.xentlyAwaitedMap(onLocationPermissionNotGranted)
        cameraPositions.forEach {
            googleMap.addMarker { position(it) }
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition))
    }
    AndroidView(modifier = modifier, factory = { map }, update = onMapViewUpdated)
}