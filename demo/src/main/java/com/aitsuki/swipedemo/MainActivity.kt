package com.aitsuki.swipedemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.aitsuki.swipedemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.leftMenu.setOnClickListener {
            Toast.makeText(this, "LEFT", Toast.LENGTH_SHORT).show()
        }
        binding.rightMenu.setOnClickListener {
            Toast.makeText(this, "RIGHT", Toast.LENGTH_SHORT).show()
        }
        binding.content.setOnClickListener {
            Toast.makeText(this, "CONTENT", Toast.LENGTH_SHORT).show()
        }
    }
}