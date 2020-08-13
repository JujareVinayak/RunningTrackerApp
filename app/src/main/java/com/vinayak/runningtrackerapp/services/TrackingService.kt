package com.vinayak.runningtrackerapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.vinayak.runningtrackerapp.R
import com.vinayak.runningtrackerapp.ui.MainActivity
import com.vinayak.runningtrackerapp.util.Constants
import com.vinayak.runningtrackerapp.util.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.vinayak.runningtrackerapp.util.Constants.NOTIFICATION_CHANNEL_ID
import com.vinayak.runningtrackerapp.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.vinayak.runningtrackerapp.util.Constants.NOTIFICATION_ID
import com.vinayak.runningtrackerapp.util.TrackingUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService:LifecycleService() {

    var isFirstRun = true
    var isServiceKilled = false
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var curNotificationBuilder: NotificationCompat.Builder

    private val timeRunInSeconds = MutableLiveData<Long>()
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    companion object{
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>() //equal to MutableLiveData<MutableList<MutableList<LatLng>>>(), reduces code by using typealias
        val timeRunInMillis = MutableLiveData<Long>()
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
             isTracking.observe(this, Observer {
                 updateLocationTracking(it)
                 updateNotificationTrackingState(it)
        })
        curNotificationBuilder = baseNotificationBuilder
    }

    private fun updateLocationTracking(isTracking: Boolean) {
        if(isTracking) {
            if(TrackingUtils.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = Constants.LOCATION_UPDATE_INTERVAL
                    fastestInterval = Constants.FASTEST_LOCATION_INTERVAL
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if(isTracking.value!!) {
                result?.locations?.let { locations ->
                    for(location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                Constants.ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("ACTION_START_OR_RESUME_SERVICE")
                    if(isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    }else{
                        startTimer() //startForegroundService()
                        Timber.d("Service already started.")
                    }
                }
                Constants.ACTION_PAUSE_SERVICE -> {
                    Timber.d("ACTION_PAUSE_SERVICE")
                    pauseService()
                }
                Constants.ACTION_STOP_SERVICE -> {
                    Timber.d("ACTION_STOP_SERVICE")
                    killService()
                }

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun killService() {
        isFirstRun = true
        isServiceKilled = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    private fun startForegroundService() {
        addEmptyPolyline()
        isTracking.postValue(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())
        timeRunInSeconds.observe(this, Observer {
            if(!isServiceKilled) {
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtils.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted
                // post the new lapTime
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(Constants.TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if(isTracking) "Pause" else "Resume"
        val pendingIntent = if(isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = Constants.ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = Constants.ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if(!isServiceKilled) {
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }
}