package com.kotlin.task.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.kotlin.task.R
import com.kotlin.task.databinding.ActivityDetailScreenBinding
import com.kotlin.task.repository.MainRepository
import com.kotlin.task.repository.MyViewModelFactory
import com.kotlin.task.service.RetrofitService
import com.kotlin.task.service.WeatherRetrofitService
import com.kotlin.task.viewmodel.MainViewModel

class DetailScreen : AppCompatActivity() {
    lateinit var binding: ActivityDetailScreenBinding
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "Profile Detail"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val retrofitService = RetrofitService.getInstance()
        val weatherRetrofitService = WeatherRetrofitService.getInstance()
        val mainRepository = MainRepository(retrofitService, weatherRetrofitService)
        viewModel = ViewModelProvider(this, MyViewModelFactory(mainRepository, application)).get(MainViewModel::class.java)

        val bundle = intent.extras
        if (bundle != null) {
            binding.userName.text = bundle.getString("name")
            binding.emailId.text = bundle.getString("email")
            binding.latTxt.text = "Latitude: ${bundle.getString("lat")}"
            binding.longTxt.text = "Longitude: ${bundle.getString("long")}"

            if (checkForInternet(this)) {
                viewModel.getWeatherDetail(
                    bundle.getString("lat").toString(),
                    bundle.getString("long").toString()
                )
            } else {
                Toast.makeText(this, "Please check internet connection!", Toast.LENGTH_SHORT).show()
            }

            Glide.with(this)
                .load(bundle.getString("image"))
                .placeholder(R.drawable.default_image)
                .error(R.drawable.image_not_supported)
                .into(binding.profileImage)
        }

        viewModel.getWeatherDetails.observe(this) {
            if (it != null) {
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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

    override fun onBackPressed() {
        super.onBackPressed()
    }
}