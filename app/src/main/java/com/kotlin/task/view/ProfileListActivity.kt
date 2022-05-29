package com.kotlin.task.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kotlin.task.R
import com.kotlin.task.adapter.OnItemClickListener
import com.kotlin.task.adapter.ProfileAdapter
import com.kotlin.task.database.entity.ProfileList
import com.kotlin.task.databinding.ActivityProfileListBinding
import com.kotlin.task.pagination.PaginationScrollListener
import com.kotlin.task.repository.MainRepository
import com.kotlin.task.repository.MyViewModelFactory
import com.kotlin.task.service.RetrofitService
import com.kotlin.task.service.WeatherRetrofitService
import com.kotlin.task.viewmodel.MainViewModel
import java.util.*

class ProfileListActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileListBinding
    private lateinit var viewModel: MainViewModel
    private val adapter = ProfileAdapter()
    private var profileList: List<ProfileList> = ArrayList()
    var loadFlag: Boolean = true

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null

    var isLastPage: Boolean = false
    var isLoading: Boolean = false
    var count = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Profile List"

        val retrofitService = RetrofitService.getInstance()
        val weatherRetrofitService = WeatherRetrofitService.getInstance()
        val mainRepository = MainRepository(retrofitService, weatherRetrofitService)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this, MyViewModelFactory(mainRepository, application)).get(MainViewModel::class.java)

        viewModel.allProfileList.observe(this) {
            if (it.isNotEmpty()) {
                loadFlag = true
                profileList = it
                adapter.setProfileData(it, object : OnItemClickListener {
                    override fun onItemClick(profileList: ProfileList) {
                        val intent = Intent(this@ProfileListActivity, DetailScreen::class.java)
                        intent.putExtra("name", profileList.name)
                        intent.putExtra("email", profileList.email)
                        intent.putExtra("image", profileList.large)
                        intent.putExtra("lat", profileList.latitude)
                        intent.putExtra("long", profileList.longitude)
                        startActivity(intent)
                    }
                })
                binding.progressDialog.visibility = View.GONE
            } else {
                if (checkForInternet(this)) {
                    Log.d("TAG", "count1: $count")
                    viewModel.getAllData()
                } else {
                    Toast.makeText(this, "Please check internet connection!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.getWeatherDetails.observe(this) {
            if (it != null) {
                binding.progressDialog.visibility = View.GONE

                Glide.with(this)
                    .load("https://openweathermap.org/img/w/" + it.weather[0].icon + ".png")
                    .listener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(
                            @Nullable e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.progress.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable?>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.progress.visibility = View.GONE
                            return false
                        }
                    })
                    .into(binding.weatherAirImg)
            }
        }

        viewModel.errorMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.loading.observe(this, Observer {
            if (it) {
                binding.progressDialog.visibility = View.VISIBLE
            } else {
                binding.progressDialog.visibility = View.GONE
            }
        })

        binding.recyclerView.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            override fun loadMoreItems() {
                isLoading = true
                getMoreItems()
            }
        })
    }

    fun getMoreItems() {
        if (loadFlag) {
            Log.d("TAG", "count2: ${++count}")
            loadFlag = false
            viewModel.getAllData()
        }
        isLoading = false
    }

    override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            getLastLocation()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient?.lastLocation!!.addOnCompleteListener(this) { task ->
            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result
                binding.latitudeLong.text = "Latitude : " + (lastLocation)!!.latitude.toString().substring(0, 7) +
                        "\nLongitude : " + (lastLocation)!!.longitude.toString().substring(0, 7)

                if (checkForInternet(this)) {
                    Log.d("TAG", "count Weather: ${++count}")
                    viewModel.getWeatherDetail(
                        (lastLocation)!!.latitude.toString(),
                        (lastLocation)!!.longitude.toString()
                    )
                } else {
                    Toast.makeText(this, "Please check internet connection!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@ProfileListActivity, "No location detected. Make sure location is enabled on the device.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (shouldProvideRationale) {
            Toast.makeText(this@ProfileListActivity, "Location permission is needed for core functionality", Toast.LENGTH_LONG).show()
        } else {
            startLocationPermissionRequest()
        }
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this@ProfileListActivity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when (PackageManager.PERMISSION_GRANTED) {
                grantResults[0] -> {
                    getLastLocation()
                }
                else -> {
                    Toast.makeText(this@ProfileListActivity, "Permission was denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val search = menu.findItem(R.id.appSearchBar)
        val searchView = search.actionView as SearchView
        searchView.queryHint = "Search"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("TAG", "count1 onQueryTextSubmit: ")
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                loadFlag = false
                Log.d("TAG", "count onQueryTextSubmit: ")
                filter(newText)
                return true
            }
        })

        searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn).setOnClickListener {
            Log.d("TAG", "count4 onQueryTextSubmit: ")
            searchView.isIconified = true
            loadFlag = true
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun filter(text: String) {
        val filteredList: ArrayList<ProfileList> = ArrayList()

        for (item in profileList) {
            if (item.name.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filteredList.add(item)
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            adapter.filterList(filteredList)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun checkForInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
}