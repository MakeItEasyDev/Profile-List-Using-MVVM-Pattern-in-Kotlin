package com.kotlin.task.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.task.database.ProfileDatabase
import com.kotlin.task.database.entity.ProfileList
import com.kotlin.task.database.repository.ProfileRepository
import com.kotlin.task.model.ProfileData
import com.kotlin.task.model.WeatherData
import com.kotlin.task.repository.MainRepository
import com.kotlin.task.utils.Constants
import kotlinx.coroutines.*

class MainViewModel(private val mainRepository: MainRepository, private val application: Application) : ViewModel() {

    val errorMessage = MutableLiveData<String>()
    val getWeatherDetails = MutableLiveData<WeatherData>()
    var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }
    val loading = MutableLiveData<Boolean>()

    //database
    var allProfileList : LiveData<List<ProfileList>>
    val repository : ProfileRepository

    init {
        val dao = ProfileDatabase.getInstance(application).profileDao()
        repository = ProfileRepository(dao)
        allProfileList = repository.allProfileList
    }

    fun getAllData() {
        val profiledata: ArrayList<ProfileList> = ArrayList()

        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            loading.postValue(true)
            val response = mainRepository.getAllData()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    for (i in 0 until response.body()!!.results.size) {
                        profiledata.add(
                            ProfileList(
                                name = response.body()!!.results[i].name.title + " " +response.body()!!.results[i].name.first + " " + response.body()!!.results[i].name.last,
                                email = response.body()!!.results[i].email,
                                gender = response.body()!!.results[i].gender,
                                dob = response.body()!!.results[i].dob.date,
                                phone = response.body()!!.results[i].phone,
                                latitude = response.body()!!.results[i].location.coordinates.latitude,
                                longitude = response.body()!!.results[i].location.coordinates.longitude,
                                large = response.body()!!.results[i].picture.large,
                                medium = response.body()!!.results[i].picture.medium,
                                thumbnail = response.body()!!.results[i].picture.thumbnail
                            )
                        )
                    }
                    addProfileList(profiledata)
                    loading.value = false
                } else {
                    onError("Error : ${response.message()}")
                }
            }
        }
    }

    fun getWeatherDetail(lat: String, long: String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            loading.postValue(true)
            val response = mainRepository.getWeatherDetail(lat, long, Constants.API_KEY)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    getWeatherDetails.postValue(response.body())
                } else {
                    onError("Error : ${response.message()}")
                }
            }
        }
    }

    fun addProfileList(profileList: List<ProfileList>) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(profileList)
    }

    private fun onError(message: String) {
        errorMessage.value = message
        loading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}