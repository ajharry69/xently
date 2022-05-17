package co.ke.xently.feature.ui

import android.annotation.SuppressLint
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.maps.android.ktx.awaitMap


@Composable
fun GoogleMapView(
    modifier: Modifier,
    currentPosition: LatLng = rememberMyLocation(),
    markerPositions: List<MarkerOptions> = emptyList(),
    onMapViewUpdated: (MapView) -> Unit = NoOpUpdate,
    onLocationPermissionChanged: ((permissionGranted: Boolean) -> Unit) = {},
    setUp: GoogleMap.() -> Unit = {},
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
        onMapViewUpdated,
        onLocationPermissionChanged,
        setUp,
    )
}

@SuppressLint("MissingPermission")
@Composable
private fun GoogleMapViewContainer(
    modifier: Modifier,
    map: MapView,
    currentPosition: LatLng,
    markerPositions: List<MarkerOptions>,
    onMapViewUpdated: (MapView) -> Unit = NoOpUpdate,
    onLocationPermissionChanged: (permissionGranted: Boolean) -> Unit,
    setUp: GoogleMap.() -> Unit,
) {
    val permissionState =
        requestLocationPermission(onLocationPermissionChanged = onLocationPermissionChanged)
    val myLocation by rememberSaveable(currentPosition.latitude, currentPosition.longitude) {
        mutableStateOf(currentPosition)
    }

    val enableMyLocation by remember(permissionState) {
        derivedStateOf {
            permissionState.allPermissionsGranted
        }
    }
    LaunchedEffect(map, enableMyLocation) {
        map.awaitMap().apply {
            uiSettings.apply {
                isZoomControlsEnabled = true
                isZoomGesturesEnabled = true
                isMyLocationButtonEnabled = enableMyLocation
            }
            isMyLocationEnabled = enableMyLocation
            setMinZoomPreference(15f)
            markerPositions.forEach {
                addMarker(it)
            }
            moveCamera(CameraUpdateFactory.newLatLng(myLocation))
            setUp()
        }
    }
    AndroidView(modifier = modifier, factory = { map }, update = onMapViewUpdated)
}
