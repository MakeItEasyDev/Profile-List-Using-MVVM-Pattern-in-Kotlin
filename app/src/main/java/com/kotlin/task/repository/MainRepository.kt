package com.kotlin.task.repository

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kotlin.task.service.RetrofitService
import com.kotlin.task.service.WeatherRetrofitService
import com.kotlin.task.viewmodel.MainViewModel

class MainRepository constructor(private val retrofitService: RetrofitService, private val weatherRetrofitService: WeatherRetrofitService) {

    suspend fun getAllData() = retrofitService.getAllData()

    suspend fun getWeatherDetail(lat: String, long: String, apiKey: String) = weatherRetrofitService.getWeatherDetail(lat, long, apiKey)
}

class MyViewModelFactory(private val repository: MainRepository, private val application: Application): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            MainViewModel(this.repository, this.application) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}