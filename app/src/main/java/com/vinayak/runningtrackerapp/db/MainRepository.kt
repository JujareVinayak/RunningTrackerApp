package com.vinayak.runningtrackerapp.db

import javax.inject.Inject

class MainRepository @Inject constructor(val runDAO: RunDAO) {

    suspend fun insertRun(run: Run):Long = runDAO.insertRun(run)

    suspend fun deleteRun(run: Run) = runDAO.deleteRun(run)

    fun getAllRunsSortedByDate() = runDAO.getAllRunsSortedByDate()

    fun getAllRunsSortedByTimeInMillis() = runDAO.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByCaloriesBurned() = runDAO.getAllRunsSortedByCaloriesBurned()

    fun getAllRunsSortedByAvgSpeed() = runDAO.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByDistance() = runDAO.getAllRunsSortedByDistance()

    fun getTotalTimeInMillis() = runDAO.getTotalTimeInMillis()

    fun getTotalCaloriesBurned() = runDAO.getTotalCaloriesBurned()

    fun getTotalDistance() = runDAO.getTotalDistance()

    fun getTotalAvgSpeed() = runDAO.getTotalAvgSpeed()
}