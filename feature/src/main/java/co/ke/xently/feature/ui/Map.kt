package co.ke.xently.feature.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.ke.xently.feature.PermissionGranted
import co.ke.xently.feature.R
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

/*
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.compose.ui.platform.LocalContext
import co.ke.xently.common.MY_LOCATION_LATITUDE_SHARED_PREFERENCE_KEY
import co.ke.xently.common.MY_LOCATION_LONGITUDE_SHARED_PREFERENCE_KEY
import co.ke.xently.source.local.di.StorageModule.provideEncryptedSharedPreference
import com.google.android.gms.maps.model.LatLng

internal const val DEFAULT_LATITUDE = -1.306635
internal const val DEFAULT_LONGITUDE = -1.306635

@Composable
fun rememberMyLocation(sharedPreference: SharedPreferences? = null): LatLng {
    val preferences = sharedPreference ?: provideEncryptedSharedPreference(
        LocalContext.current
    )

    fun myLocation(): LatLng {
        val latitude =
            preferences.getString(MY_LOCATION_LATITUDE_SHARED_PREFERENCE_KEY, null)?.toDouble()
                ?: DEFAULT_LATITUDE
        val longitude =
            preferences.getString(MY_LOCATION_LONGITUDE_SHARED_PREFERENCE_KEY, null)?.toDouble()
                ?: DEFAULT_LONGITUDE
        return LatLng(latitude, longitude)
    }

    var coordinates by remember { mutableStateOf(myLocation()) }

    val preferenceChanged = OnSharedPreferenceChangeListener { _, _ ->
        coordinates = myLocation()
    }

    DisposableEffect(preferences) {
        preferences.registerOnSharedPreferenceChangeListener(preferenceChanged)
        onDispose {
            preferences.unregisterOnSharedPreferenceChangeListener(preferenceChanged)
        }
    }
    return coordinates
}
*/

@JvmInline
value class MapMaximized(val value: Boolean = false)

@Composable
fun GoogleMapViewWithLoadingIndicator(
    modifier: Modifier,
    isMapMaximized: Boolean = false,
    onMapClick: (LatLng) -> Unit = {},
    onMapMaximizedOrMinimized: ((MapMaximized) -> Unit)? = null,
    onLocationPermissionChanged: (PermissionGranted) -> Unit,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        var isMapLoaded by remember {
            mutableStateOf(false)
        }
        val cameraPositionState = rememberCameraPositionState {
            // TODO: Replace with real-time response values
            val uthiru = LatLng(-1.268780651485453, 36.71817776897877)
            position = CameraPosition.fromLatLngZoom(uthiru, 11f)
        }
        val permissionState =
            requestLocationPermission(onLocationPermissionChanged = onLocationPermissionChanged)

        val isMyLocationEnabled by remember(permissionState) {
            derivedStateOf {
                permissionState.allPermissionsGranted
            }
        }

        val uiSettings: MapUiSettings by remember {
            mutableStateOf(MapUiSettings(compassEnabled = false))
        }
        val mapProperties: MapProperties by remember(isMyLocationEnabled) {
            mutableStateOf(
                MapProperties(
                    mapType = MapType.NORMAL,
                    isMyLocationEnabled = isMyLocationEnabled,
                )
            )
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
