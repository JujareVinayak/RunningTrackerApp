package com.vinayak.runningtrackerapp.ui.viewmodels

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vinayak.runningtrackerapp.db.MainRepository
import com.vinayak.runningtrackerapp.db.Run
import com.vinayak.runningtrackerapp.enums.SortType
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(val mainRepository: MainRepository):ViewModel() {

    fun insertRun(run: Run) {
        viewModelScope.launch {
            var id = mainRepository.insertRun(run)
            Log.d("Room",id.toString())
        }
    }

    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()

    private val runsSortedByTimeInMillis = mainRepository.getAllRunsSortedByTimeInMillis()

    private val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()

    private val runsSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()

    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()

    val runs = MediatorLiveData<List<Run>>() //Merge Several LiveData together and write our custom logic,
    // for that when we want to emit which kinds of data. These LiveData can be made private and only MediatorLiveData is not private.

    var sortType = SortType.DATE

    init {
        runs.addSource(runsSortedByDate) { result ->
            if(sortType == SortType.DATE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByAvgSpeed) { result ->
            if(sortType == SortType.AVG_SPEED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByCaloriesBurned) { result ->
            if(sortType == SortType.CALORIES_BURNED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByDistance) { result ->
            if(sortType == SortType.DISTANCE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByTimeInMillis) { result ->
            if(sortType == SortType.RUNNING_TIME) {
                result?.let { runs.value = it }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when(sortType) {
        SortType.DATE -> runsSortedByDate.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runsSortedByTimeInMillis.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runsSortedByAvgSpeed.value?.let { runs.value = it }
        SortType.DISTANCE -> runsSortedByDistance.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runsSortedByCaloriesBurned.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }

}