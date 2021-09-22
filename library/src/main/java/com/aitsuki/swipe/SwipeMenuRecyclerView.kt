package com.aitsuki.swipe

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Aitsuki on 2017/2/23.
 */
class SwipeMenuRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    private val rect = Rect()
    private var cancelTouch = false
    private var initX = 0
    private var initY = 0
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x.toInt()
                val y = ev.y.toInt()
                initX = x
                initY = y
                val touchItem = getTouchItem(x, y)
                for (openItem in findOpenItems()) {
                    if (openItem != touchItem) {
                        cancelTouch = true
                        findSwipeLayout(openItem)?.closeMenu(true)
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val x = ev.getX(ev.actionIndex).toInt()
                val y = ev.getY(ev.actionIndex).toInt()
                for (openItem in findOpenItems()) {
                    if (openItem != getTouchItem(x, y)) {
                        return true
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val x = ev.x.toInt()
                val y = ev.y.toInt()
                val dx = x - initX
                val dy = y - initY
                if (dx * dx + dy * dy > touchSlop * touchSlop) {
                    cancelTouch = false
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (cancelTouch) {
                    ev.action = MotionEvent.ACTION_CANCEL
                    cancelTouch = false
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun getTouchItem(x: Int, y: Int): View? {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == VISIBLE) {
                child.getHitRect(rect)
                if (rect.contains(x, y)) {
                    return child
                }
            }
        }
        return null
    }

    private fun findOpenItems(): List<View> {
        val openItems = arrayListOf<View>()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val swipeLayout = findSwipeLayout(child)
            if (swipeLayout != null && swipeLayout.onScreen > 0f) {
                openItems.add(child)
            }
        }
        return openItems
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