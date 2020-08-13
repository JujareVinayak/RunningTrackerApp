package com.vinayak.runningtrackerapp.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.vinayak.runningtrackerapp.db.MainRepository

class StatisticsViewModel @ViewModelInject constructor(val mainRepository: MainRepository):ViewModel() {

    val totalTimeRun = mainRepository.getTotalTimeInMillis()

    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()

    val totalDistance= mainRepository.getTotalDistance()

    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

    val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
}