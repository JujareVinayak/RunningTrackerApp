package com.vinayak.runningtrackerapp.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.vinayak.runningtrackerapp.db.RunningDatabase
import com.vinayak.runningtrackerapp.util.Constants
import com.vinayak.runningtrackerapp.util.Constants.RUNNING_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class) //defines life time. Other types are ActivityComponent, FragmentComponent, ServiceComponent etc.
object AppModule {

    @Provides
    @Singleton
    fun provideRunningDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context,RunningDatabase::class.java,RUNNING_DATABASE_NAME).build()

    @Provides
    @Singleton
    fun provideRunDAO(db: RunningDatabase) = db.getRunDao()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context) =context.getSharedPreferences(Constants.SHARED_PREF_NAME,Context.MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPref: SharedPreferences) = sharedPref.getString(Constants.KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(Constants.KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences) =
        sharedPref.getBoolean(Constants.KEY_FIRST_TIME_TOGGLE, true)
}