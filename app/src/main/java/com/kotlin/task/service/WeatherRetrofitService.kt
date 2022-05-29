package com.kotlin.task.service

import com.kotlin.task.model.WeatherData
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherRetrofitService {

    @GET("weather")
    suspend fun getWeatherDetail(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("APPID") apiKey: String
    ) : Response<WeatherData>

    companion object {
        var retrofitService: WeatherRetrofitService? = null

        fun getInstance() : WeatherRetrofitService {
            if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.openweathermap.org/data/2.5/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                retrofitService = retrofit.create(WeatherRetrofitService::class.java)
            }
            return retrofitService!!
        }
    }
}