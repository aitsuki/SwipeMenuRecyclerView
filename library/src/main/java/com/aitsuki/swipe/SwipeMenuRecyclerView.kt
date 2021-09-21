package com.aitsuki.swipe

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class SwipeMenuRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return super.dispatchTouchEvent(ev)
    }
}