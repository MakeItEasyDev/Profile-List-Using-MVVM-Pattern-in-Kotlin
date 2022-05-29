package com.kotlin.task

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.kotlin.task.databinding.ActivitySplashBinding
import com.kotlin.task.view.ProfileListActivity

class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler().postDelayed({
            val intent = Intent(this, ProfileListActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}