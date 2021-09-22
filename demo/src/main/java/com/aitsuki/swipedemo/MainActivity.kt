package com.aitsuki.swipedemo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.aitsuki.swipe.SwipeLayout
import com.aitsuki.swipedemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.adapter = DemoAdapter()

        binding.root.postDelayed({
            val result = findSwipeLayout(binding.root)
            Log.d("123123", result?.toString() ?: "not found")
        }, 3000)
    }

    private fun findSwipeLayout(view: View): SwipeLayout? {
        if (view is SwipeLayout) {
            return view
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                return findSwipeLayout(view.getChildAt(i))
            }
        }
        return null
    }
}