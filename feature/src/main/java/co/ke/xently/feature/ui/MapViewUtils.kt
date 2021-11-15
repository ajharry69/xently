/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.ke.xently.feature.ui

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import co.ke.xently.common.MY_LOCATION_LATITUDE_SHARED_PREFERENCE_KEY
import co.ke.xently.common.MY_LOCATION_LONGITUDE_SHARED_PREFERENCE_KEY
import co.ke.xently.feature.R
import co.ke.xently.source.local.di.StorageModule.provideEncryptedSharedPreference
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng

private fun getMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
            Lifecycle.Event.ON_START -> mapView.onStart()
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            Lifecycle.Event.ON_STOP -> mapView.onStop()
            Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
            else -> throw IllegalStateException()
        }
    }

/**
 * Remembers a MapView and gives it the lifecycle of the current LifecycleOwner
 */
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        // Make MapView follow the current lifecycle
        val lifecycleObserver = getMapLifecycleObserver(mapView)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

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
