package com.vinayak.runningtrackerapp.util

import android.graphics.Color

object Constants {

    const val RUNNING_DATABASE_NAME = "running_db"

    const val REQUEST_CODE_FOR_LOCATION_PERMISSIONS = 5

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"
    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    const val POLYLINE_COLOR = Color.GREEN
    const val POLYLINE_WIDTH = 50f
    const val MAP_ZOOM = 15.3f

    const val TIMER_UPDATE_INTERVAL = 50L

    const val SHARED_PREF_NAME = "sharedPref"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME = "name"
    const val KEY_WEIGHT = "weight"
}