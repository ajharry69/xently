/*
 * Copyright 2019 Google LLC
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
package co.ke.xently.feature

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import co.ke.xently.feature.repository.ILocationServiceRepository
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Service tracks location when requested and updates Activity via binding. If Activity is
 * stopped/unbinds and tracking is enabled, the service promotes itself to a foreground service to
 * insure location updates aren't interrupted.
 *
 * For apps running in the background on O+ devices, location is computed much less than previous
 * versions. Please reference documentation for details.
 */
@AndroidEntryPoint
class LocationService : Service() {
    @Inject
    lateinit var repository: ILocationServiceRepository

    /*
     * Checks whether the bound activity has really gone away (foreground service with notification
     * created) or simply orientation change (no-op).
     */
    private var configurationChange = false

    private var serviceRunningInForeground = false

    private val localBinder = LocalBinder()

    private lateinit var notificationManager: NotificationManager

    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // TODO: Step 1.2, Review the FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                locationResult.lastLocation.run {
                    runBlocking {
                        repository.updateLocation(arrayOf(latitude, longitude)).collect()
                    }
                }

                if (serviceRunningInForeground) {
                    notificationManager.notify(NOTIFICATION_ID, generateNotification())
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val cancelLocationTrackingFromNotification =
            intent.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)

        if (cancelLocationTrackingFromNotification) {
            unsubscribeToLocationUpdates()
            stopSelf()
        }
        // Tells the system not to recreate the service after it's been killed.
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        // Activity (client) comes into foreground and binds to service, so the service can
        // become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        // Activity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        // Activity (client) leaves foreground, so service needs to become a foreground service
        // to maintain the 'while-in-use' label.
        // NOTE: If this method is called due to a configuration change in Activity,
        // we do nothing.
        if (!configurationChange && repository.getLocationTrackingPref()) {
            startForeground(NOTIFICATION_ID, generateNotification())
            serviceRunningInForeground = true
        }

        // Ensures onRebind() is called if Activity (client) rebinds.
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    fun subscribeToLocationUpdates() {
        repository.saveLocationTrackingPref(true)

        // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
        // ensure this Service can be promoted to a foreground service, i.e., the service needs to
        // be officially started (which we do here).
        startService(Intent(applicationContext, LocationService::class.java))

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                LocationRequest.create().apply {
                    // Sets the desired interval for active location updates. This interval is inexact. You
                    // may not receive updates at all if no location sources are available, or you may
                    // receive them less frequently than requested. You may also receive updates more
                    // frequently than requested if other applications are requesting location at a more
                    // frequent interval.
                    //
                    // IMPORTANT NOTE: Apps running on Android 8.0 and higher devices (regardless of
                    // targetSdkVersion) may receive updates less frequently than this interval when the app
                    // is no longer in the foreground.
                    interval = TimeUnit.SECONDS.toMillis(60)

                    // Sets the fastest rate for active location updates. This interval is exact, and your
                    // application will never receive updates more frequently than this value.
                    fastestInterval = TimeUnit.SECONDS.toMillis(30)

                    // Sets the maximum time when batched location updates are delivered. Updates may be
                    // delivered sooner than this interval.
                    maxWaitTime = TimeUnit.MINUTES.toMillis(2)

                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                },
                locationCallback,
                Looper.getMainLooper(),
            )
        } catch (unlikely: SecurityException) {
            repository.saveLocationTrackingPref(false)
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    private fun unsubscribeToLocationUpdates() {
        if (!repository.getLocationTrackingPref()) return
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) stopSelf()
                }
            repository.saveLocationTrackingPref(false)
        } catch (unlikely: SecurityException) {
            repository.saveLocationTrackingPref(true)
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    private fun generateNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            )

            // Adds NotificationChannel to system. Attempting to create an
            // existing notification channel with its original values performs
            // no operation, so it's safe to perform the below sequence.
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val mainNotificationText = getString(R.string.location_tracking_notification_text)
        return NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mainNotificationText))
            .setContentText(mainNotificationText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_launch,
                getString(R.string.show_screen_button_text),
                getLaunchActivityIntent(),
            )
            .addAction(
                R.drawable.ic_cancel,
                getString(R.string.stop_location_tracking_button_text),
                getStopTrackingIntent(),
            )
            .build()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getLaunchActivityIntent(): PendingIntent {
        val mainActivityIntent = Intent("co.ke.xently.actions.MAIN")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                this,
                0,
                mainActivityIntent,
                PendingIntent.FLAG_IMMUTABLE,
            )
        } else {
            PendingIntent.getActivity(
                this,
                0,
                mainActivityIntent,
                0, // return an existing PendingIntent if there is one that matches the parameters provided
            )
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getStopTrackingIntent(): PendingIntent {
        val stopTrackingIntent = Intent(this, LocationService::class.java).apply {
            putExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, true)
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getService(
                this,
                0,
                stopTrackingIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getService(
                this, 0, stopTrackingIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        val service: LocationService
            get() = this@LocationService
    }

    companion object {
        private val TAG = LocationService::class.java.simpleName

        private const val PACKAGE_NAME = "co.ke.xently"

        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"

        private const val NOTIFICATION_ID = 12345678

        private const val NOTIFICATION_CHANNEL_ID = "while_in_use_channel_01"
    }
}
