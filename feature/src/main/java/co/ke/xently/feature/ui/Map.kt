package co.ke.xently.feature.ui

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.maps.android.ktx.awaitMap


@Composable
fun GoogleMapView(
    modifier: Modifier,
    currentPosition: LatLng = rememberMyLocation(),
    markerPositions: Array<MarkerOptions> = arrayOf(),
    onMapViewUpdated: (MapView) -> Unit = NoOpUpdate,
    onLocationPermissionChanged: ((permissionGranted: Boolean) -> Unit) = {},
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
    )
}

@SuppressLint("MissingPermission")
@Composable
fun GoogleMapViewContainer(
    modifier: Modifier,
    map: MapView,
    currentPosition: LatLng,
    markerPositions: Array<MarkerOptions>,
    onMapViewUpdated: (MapView) -> Unit = NoOpUpdate,
    onLocationPermissionChanged: (permissionGranted: Boolean) -> Unit,
) {
    val myLocation by rememberSaveable(currentPosition.latitude, currentPosition.longitude) {
        mutableStateOf(currentPosition)
    }
    var showRationale by rememberSaveable { mutableStateOf(true) }

    val permissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    if (!permissionState.permissionRequested) {
        SideEffect {
            permissionState.launchMultiplePermissionRequest()
        }
    }
    // If the user denied the permission but a rationale should be shown, or the user sees
    // the permission for the first time, explain why the feature is needed by the app and allow
    // the user to be presented with the permission again or to not see the rationale any more.
    else if (permissionState.shouldShowRationale && showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        permissionState.launchMultiplePermissionRequest()
                        showRationale = false
                    },
                ) { Text("REQUEST PERMISSION") }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text("DON'T SHOW AGAIN")
                }
            },
            text = { Text("Location service is important for effective shop recommendation by this app. Please grant the permission.") },
        )
    }
    val cameraPositions = remember(*markerPositions) { markerPositions }

    LaunchedEffect(map, permissionState.allPermissionsGranted) {
        onLocationPermissionChanged(permissionState.allPermissionsGranted)
        val googleMap = map.awaitMap().apply {
            uiSettings.apply {
                isZoomControlsEnabled = true
                isZoomGesturesEnabled = true
                isMyLocationButtonEnabled = permissionState.allPermissionsGranted
            }
            isMyLocationEnabled = permissionState.allPermissionsGranted
            setMinZoomPreference(15f)
        }
        cameraPositions.forEach {
            googleMap.addMarker(it)
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation))
    }
    AndroidView(modifier = modifier, factory = { map }, update = onMapViewUpdated)
}