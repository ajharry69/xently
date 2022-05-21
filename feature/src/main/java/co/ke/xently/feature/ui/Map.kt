package co.ke.xently.feature.ui

import android.annotation.SuppressLint
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import co.ke.xently.feature.PermissionGranted
import co.ke.xently.feature.R
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class MyUpdatedLocation(
    val myLocation: LatLng? = null,
    val isLocationPermissionGranted: Boolean,
)

/**
 * @param maxBatchWaitTime Sets the maximum time when batched location updates are delivered.
 * Updates may be delivered sooner than this interval.
 * @param fastestRefreshInterval Sets the fastest rate for active location updates. This
 * interval is exact, and your application will never receive updates more frequently than
 * this value.
 * @param refreshInterval Sets the desired interval for active location updates. This interval
 * is inexact. You may not receive updates at all if no location sources are available, or you
 * may receive them less frequently than requested. You may also receive updates more frequently
 * than requested if other applications are requesting location at a more frequent interval.
 * IMPORTANT NOTE: Apps running on Android 8.0 and higher devices (regardless of targetSdkVersion)
 * may receive updates less frequently than this interval when the app is no longer in the
 * foreground.
 */
data class MyUpdatedLocationArgs(
    val myDefaultLocation: LatLng? = null,
    val maxBatchWaitTime: Duration = 2.minutes,
    val refreshInterval: Duration = 60.seconds,
    val fastestRefreshInterval: Duration = 30.seconds,
    val shouldRequestPermission: Boolean = true,
    val onLocationPermissionChanged: (PermissionGranted) -> Unit,
)

/**
 * Requests and returns frequent location updates provided location permissions are granted.
 */
@SuppressLint("MissingPermission")
@Composable
fun rememberMyUpdatedLocation(args: MyUpdatedLocationArgs): MyUpdatedLocation {
    val permissionState = requestLocationPermission(
        shouldRequestPermission = args.shouldRequestPermission,
        onLocationPermissionChanged = args.onLocationPermissionChanged,
    )

    val isLocationPermissionEnabled by remember(permissionState) {
        derivedStateOf {
            permissionState.allPermissionsGranted
        }
    }

    val myUpdatedLocationSaver = run {
        val latitudeKey = "latitude"
        val longitudeKey = "longitude"
        mapSaver(
            save = {
                if (it.myLocation == null) {
                    emptyMap<String, Double>()
                } else {
                    mapOf(
                        latitudeKey to it.myLocation.latitude,
                        longitudeKey to it.myLocation.longitude,
                    )
                }
            },
            restore = {
                MyUpdatedLocation(
                    myLocation = if (it.isEmpty()) {
                        null
                    } else {
                        LatLng(
                            it[latitudeKey] as Double,
                            it[longitudeKey] as Double,
                        )
                    },
                    isLocationPermissionGranted = isLocationPermissionEnabled,
                )
            },
        )
    }

    var myUpdatedLocation by rememberSaveable(
        args.myDefaultLocation,
        isLocationPermissionEnabled,
        stateSaver = myUpdatedLocationSaver,
    ) {
        mutableStateOf(MyUpdatedLocation(args.myDefaultLocation, isLocationPermissionEnabled))
    }

    if (!isLocationPermissionEnabled) {
        // Do not continue receiving location updates if required location permissions are not
        // granted.
        return myUpdatedLocation
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            val myLocation = locationResult.lastLocation.run {
                LatLng(latitude, longitude)
            }
            myUpdatedLocation = myUpdatedLocation.copy(myLocation = myLocation)
        }
    }

    val context = LocalContext.current
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    DisposableEffect(
        args.refreshInterval,
        args.maxBatchWaitTime,
        args.fastestRefreshInterval,
        fusedLocationProviderClient,
    ) {
        val locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(args.refreshInterval.inWholeSeconds)

            fastestInterval =
                TimeUnit.SECONDS.toMillis(args.fastestRefreshInterval.inWholeSeconds)

            maxWaitTime = TimeUnit.MINUTES.toMillis(args.maxBatchWaitTime.inWholeMinutes)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper(),
        )
        onDispose {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    return myUpdatedLocation
}

@JvmInline
value class MapMaximized(val value: Boolean = false)

@Composable
fun GoogleMapViewWithLoadingIndicator(
    modifier: Modifier,
    zoomLevel: Float = 13f,
    isMapMaximized: Boolean = false,
    onMapClick: (LatLng) -> Unit = {},
    myUpdatedLocationArgs: MyUpdatedLocationArgs,
    onMapMaximizedOrMinimized: ((MapMaximized) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        val (myLocation, isLocationPermissionGranted) = rememberMyUpdatedLocation(args = myUpdatedLocationArgs)

        val cameraPositionState = rememberCameraPositionState()

        LaunchedEffect(myLocation, zoomLevel) {
            if (myLocation != null) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(myLocation, zoomLevel)
            }
        }

        val uiSettings: MapUiSettings by remember {
            mutableStateOf(MapUiSettings(compassEnabled = false))
        }

        val mapProperties: MapProperties by remember(isLocationPermissionGranted) {
            mutableStateOf(
                MapProperties(
                    mapType = MapType.NORMAL,
                    isMyLocationEnabled = isLocationPermissionGranted,
                )
            )
        }

        var isMapLoaded by remember {
            mutableStateOf(false)
        }
        GoogleMap(
            content = content,
            uiSettings = uiSettings,
            properties = mapProperties,
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = {
                isMapLoaded = true
            },
            onMapClick = onMapClick,
        )
        if (!isMapLoaded) {
            AnimatedVisibility(
                exit = fadeOut(),
                visible = !isMapLoaded,
                enter = EnterTransition.None,
                modifier = Modifier.matchParentSize(),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .background(MaterialTheme.colors.background)
                        .wrapContentSize()
                )
            }
        } else if (onMapMaximizedOrMinimized != null) {
            var mapMaximized by remember {
                mutableStateOf(isMapMaximized)
            }
            Column(
                modifier = Modifier
                    .matchParentSize()
                    .padding(PaddingValues(VIEW_SPACE)),
                verticalArrangement = Arrangement.Bottom,
            ) {
                val backgroundColor = Color.White.copy(alpha = 0.8f)
                IconToggleButton(
                    modifier = Modifier
                        .background(
                            color = backgroundColor,
                            shape = MaterialTheme.shapes.small.copy(CornerSize(2.dp)),
                        )
                        .shadow(
                            elevation = 1.dp,
                            shape = MaterialTheme.shapes.small.copy(CornerSize(2.dp)),
                            spotColor = contentColorFor(backgroundColor).copy(alpha = 0.2f),
                        )
                        .size(38.dp), // Size must be last
                    checked = mapMaximized,
                    onCheckedChange = {
                        mapMaximized = it
                        onMapMaximizedOrMinimized.invoke(MapMaximized(it))
                    },
                ) {
                    val (icon, description) = if (mapMaximized) {
                        Pair(
                            Icons.Default.FullscreenExit,
                            stringRes(R.string.maximize_or_minimize_map, R.string.minimize),
                        )
                    } else {
                        Pair(
                            Icons.Default.Fullscreen,
                            stringRes(R.string.maximize_or_minimize_map, R.string.maximize),
                        )
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = description,
                        tint = MaterialTheme.colors.contentColorFor(backgroundColor),
                    )
                }
            }
        }
    }
}
