package com.aitsuki.swipedemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aitsuki.swipedemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.adapter = DemoAdapter()
    }
}