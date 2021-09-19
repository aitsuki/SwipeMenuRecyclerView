package com.aitsuki.swipe

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import kotlin.math.abs

private const val TAG = "SwipeItemLayout"

/**
 * Created by AItsuki on 2017/2/23.
 */
class SwipeLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /* Attribute */
    private var preview = PREVIEW_UNSPECIFIED
    private var autoClose = true

    /* Motion events */
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val velocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private val dragger = ViewDragHelper.create(this, ViewDragCallback())
    private var isDragging = false
    private var initialMotionX = 0f
    private var initialMotionY = 0f
    private var alwaysInTapRegion = false

    /* State and listener */
    private var openState = 0
    private var activeMenu: View? = null
    private val listeners = arrayListOf<Listener>()
    internal val isOpenOrOpening
        get() = openState and FLAG_IS_OPENED == FLAG_IS_OPENED
                || openState and FLAG_IS_OPENING == FLAG_IS_OPENING

    /* Child views */
    private val contentView: View? get() = getChildAt(childCount - 1)
    private var leftMenu: View? = null
    private var rightMenu: View? = null

    var swipeEnable = true
        set(value) {
            closeActiveMenu()
            field = value
        }

    init {
        isClickable = true
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.SwipeLayout)
            preview = a.getInt(R.styleable.SwipeLayout_preview, preview)
            autoClose = a.getBoolean(R.styleable.SwipeLayout_autoClose, autoClose)
            a.recycle()
        }
    }

    fun closeMenu(animate: Boolean = true) {
        closeActiveMenu(animate)
    }

    fun isLeftMenuOpened(): Boolean {
        val activeMenu = activeMenu ?: return false
        return activeMenu == leftMenu && openState and FLAG_IS_OPENED == FLAG_IS_OPENED
    }

    fun isRightMenuOpened(): Boolean {
        val activeMenu = activeMenu ?: return false
        return activeMenu == rightMenu && openState and FLAG_IS_OPENED == FLAG_IS_OPENED
    }

    fun openLeftMenu(animate: Boolean = true) {
        activeMenu = leftMenu
        openActiveMenu(animate)
    }

    fun openRightMenu(animate: Boolean = true) {
        activeMenu = rightMenu
        openActiveMenu(animate)
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun closeActiveMenu(animate: Boolean = true) {
        if (activeMenu == null) {
            openState = 0
            return
        }
        val contentView = contentView ?: return
        if (animate) {
            openState = openState or FLAG_IS_CLOSING
            dragger.smoothSlideViewTo(contentView, paddingLeft, contentView.top)
        } else {
            contentView.offsetLeftAndRight(-contentView.left + paddingLeft)
            updateMenuState(STATE_IDLE)
        }
        invalidate()
    }

    private fun openActiveMenu(animate: Boolean = true) {
        if (activeMenu == null) {
            openState = 0
            return
        }
        val contentView = contentView ?: return
        val activeMenu = activeMenu ?: return
        val left = if (activeMenu == leftMenu) activeMenu.width + paddingLeft
        else -activeMenu.width + paddingLeft
        if (animate) {
            openState = openState or FLAG_IS_OPENING
            dragger.smoothSlideViewTo(contentView, left, contentView.top)
        } else {
            contentView.offsetLeftAndRight(left - contentView.left)
            updateMenuState(STATE_IDLE)
        }
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (isInEditMode) {
            preview()
        } else {
            updateMenuLayout()
        }
    }

    /**
     * 更新菜单，通过这个方法可以自定义menuView的样式。例如overlap，linear，parallax...
     */
    private fun updateMenuLayout() {
        val contentView = contentView ?: return

        // 将菜单移出屏幕，防止关闭的情况下被点击
        if (contentView.left == paddingLeft) {
            leftMenu?.let {
                it.layout(paddingLeft - it.width, it.top, paddingLeft, it.bottom)
            }
            rightMenu?.let {
                it.layout(
                    right - paddingRight, it.top, right - paddingRight + it.width, it.bottom
                )
            }
            return
        }

        val activeMenu = activeMenu ?: return
        if (activeMenu == leftMenu && activeMenu.left != paddingLeft) {
            activeMenu.layout(
                paddingLeft, activeMenu.top, paddingLeft + activeMenu.width, activeMenu.bottom
            )
        } else if (activeMenu == rightMenu && activeMenu.right != right - paddingLeft) {
            activeMenu.layout(
                right - paddingLeft - activeMenu.width,
                activeMenu.top,
                right - paddingLeft,
                activeMenu.bottom
            )
        }
    }

    private fun updateMenuState(activeState: Int) {
        val contentView = contentView ?: return
        val activeMenu = activeMenu ?: return

        if (activeState == STATE_IDLE) {
            if (contentView.left == paddingLeft) {
                dispatchOnMenuClosed(activeMenu)
            } else {
                dispatchOnMenuOpened(activeMenu)
            }
        }
    }

    private fun dispatchOnMenuClosed(menuView: View) {
        if (openState and FLAG_IS_OPENED == FLAG_IS_OPENED) {
            openState = 0
            for (listener in listeners.asReversed()) {
                listener.onClosed(menuView)
            }
        }
    }

    private fun dispatchOnMenuOpened(menuView: View) {
        if (openState and FLAG_IS_OPENED == 0) {
            openState = FLAG_IS_OPENED
            for (listener in listeners.asReversed()) {
                listener.onOpened(menuView)
            }
        }
    }


    /**
     * 编辑模式下可以预览: `app:preview="left|right|none"`
     */
    private fun preview() {
        if (preview == PREVIEW_LEFT) {
            previewLeftMenu()
        } else if (preview == PREVIEW_RIGHT) {
            previewRightMenu()
        }
    }

    private fun previewLeftMenu() {
        val leftMenu = leftMenu ?: return
        val contentView = contentView ?: return
        contentView.layout(
            leftMenu.measuredWidth,
            contentView.top,
            contentView.right + leftMenu.measuredWidth,
            contentView.bottom
        )
    }

    private fun previewRightMenu() {
        val rightMenu = rightMenu ?: return
        val contentView = contentView ?: return
        contentView.layout(
            -rightMenu.measuredWidth,
            contentView.top,
            contentView.right - rightMenu.measuredWidth,
            contentView.bottom
        )
    }

    private fun checkCanDrag(ev: MotionEvent) {
        if (isDragging) return

        val dx = ev.x - initialMotionX
        val dy = ev.y - initialMotionY
        val isRightDragging = dx > touchSlop && dx > abs(dy)
        val isLeftDragging = dx < -touchSlop && abs(dx) > abs(dy)

        if (isOpenOrOpening) {
            // 开启状态下，点击在content上直接捕获事件，点击在菜单上则判断touchSlop
            val initX = initialMotionX.toInt()
            val initY = initialMotionY.toInt()
            if (isTouchContent(initX, initY)) {
                isDragging = true
                alwaysInTapRegion = true
            } else if (isTouchMenu(initX, initY)) {
                isDragging = (activeMenu == leftMenu && isLeftDragging)
                        || (activeMenu == rightMenu && isRightDragging)
            }
        } else {
            // 关闭状态，获取当前即将要开启的菜单。
            if (isRightDragging) {
                activeMenu = leftMenu
                isDragging = activeMenu != null
            } else if (isLeftDragging) {
                activeMenu = rightMenu
                isDragging = activeMenu != null
            }
        }

        if (isDragging) {
            // 开始拖动后，分发down事件给DragHelper
            val downEvent = MotionEvent.obtain(ev).also { it.action = MotionEvent.ACTION_DOWN }
            dragger.processTouchEvent(downEvent)
            // 解决和父控件的滑动冲突。
            parent?.requestDisallowInterceptTouchEvent(true)
        }
    }

    /**
     * 类似于GestureDetector的SingleTap，用于关闭菜单。
     */
    private fun detectAlwaysInTapRegion(ev: MotionEvent) {
        val dx = (ev.x - initialMotionX).toInt()
        val dy = (ev.y - initialMotionY).toInt()
        val distance = (dx * dx) + (dy * dy)
        if (distance > touchSlop * touchSlop) {
            alwaysInTapRegion = false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = false
                initialMotionX = ev.x
                initialMotionY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val beforeCheckDrag = isDragging
                checkCanDrag(ev)
                if (isDragging) {
                    dragger.processTouchEvent(ev)
                }
                // 开始拖动后，发送一个cancel事件用来取消点击效果
                if (!beforeCheckDrag && isDragging) {
                    val cancelEvent = MotionEvent.obtain(ev)
                        .also { it.action = MotionEvent.ACTION_CANCEL }
                    super.onTouchEvent(cancelEvent)
                }
                // 菜单打开的状态下触摸content，检测是否是Tap事件，用于判断是否需要关闭菜单
                detectAlwaysInTapRegion(ev)
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    // 拖拽后手指抬起时不应该响应到点击事件
                    dragger.processTouchEvent(ev)
                    ev.action = MotionEvent.ACTION_CANCEL
                    isDragging = false
                }
                if (alwaysInTapRegion) {
                    closeActiveMenu()
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                dragger.processTouchEvent(ev)
                isDragging = false
            }
            else -> {
                if (isDragging) {
                    dragger.processTouchEvent(ev)
                }
            }
        }
        return isDragging || super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = false
                initialMotionX = ev.x
                initialMotionY = ev.y
                if (autoClose && isTouchMenu(ev.x.toInt(), ev.y.toInt())) {
                    alwaysInTapRegion = true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (autoClose && isTouchMenu(ev.x.toInt(), ev.y.toInt())) {
                    detectAlwaysInTapRegion(ev)
                }
                checkCanDrag(ev)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    dragger.processTouchEvent(ev)
                    isDragging = false
                }
                if (autoClose && alwaysInTapRegion) {
                    closeActiveMenu()
                }
            }
            else -> {
                if (isDragging) {
                    dragger.processTouchEvent(ev)
                }
            }
        }
        return isDragging || super.onInterceptTouchEvent(ev)
    }

    /**
     * Content和Menu均可以捕获，但是只有Content会移动。
     */
    private inner class ViewDragCallback : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return swipeEnable && (child == contentView || child == leftMenu || child == rightMenu)
        }

        override fun onViewPositionChanged(child: View, left: Int, top: Int, dx: Int, dy: Int) {
            updateMenuLayout()
        }

        override fun onViewDragStateChanged(state: Int) {
            val stateStr = when (state) {
                STATE_IDLE -> "idle"
                STATE_DRAGGING -> "dragging"
                STATE_SETTLING -> "settling"
                else -> "unknown"
            }
            Log.d(TAG, "onViewDragStateChanged: $stateStr")
            updateMenuState(state)
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            val contentView = contentView ?: return child.left
            val activeMenu = activeMenu ?: return child.left
            when (child) {
                contentView -> return if (activeMenu == leftMenu) {
                    left.coerceIn(paddingLeft, activeMenu.width + paddingLeft)
                } else {
                    left.coerceIn(paddingLeft - activeMenu.width, paddingLeft)
                }
                leftMenu -> {
                    val offset = (contentView.left + dx)
                        .coerceIn(paddingLeft, child.width + paddingLeft) - contentView.left
                    ViewCompat.offsetLeftAndRight(contentView, offset)
                }
                rightMenu -> {
                    val offset = (contentView.left + dx)
                        .coerceIn(paddingLeft - child.width, paddingLeft) - contentView.left
                    ViewCompat.offsetLeftAndRight(contentView, offset)
                }
            }
            return child.left
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return child.top
        }

        /**
         * 根据情况开启或关闭菜单
         */
        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val activeMenu = activeMenu ?: return
            val contentView = contentView ?: return
            if (activeMenu == leftMenu) {
                when {
                    xvel > velocity -> openActiveMenu()
                    xvel < -velocity -> closeActiveMenu()
                    contentView.left - paddingLeft > activeMenu.width / 2 -> openActiveMenu()
                    else -> closeActiveMenu()
                }
            } else {
                when {
                    xvel < -velocity -> openActiveMenu()
                    xvel > velocity -> closeActiveMenu()
                    contentView.left - paddingLeft < -activeMenu.width / 2 -> openActiveMenu()
                    else -> closeActiveMenu()
                }
            }
        }
    }

    override fun computeScroll() {
        if (dragger.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    /**
     * 最后一个[child]是[contentView]，倒数第1第2个[child]设置了`layout_gravity = start 或 end`的是菜单,
     * 其余的忽略
     */
    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        val lp = child.layoutParams as LayoutParams
        val gravity = GravityCompat.getAbsoluteGravity(
            lp.gravity,
            ViewCompat.getLayoutDirection(child)
        )
        if (gravity == Gravity.LEFT) {
            leftMenu = child
        } else if (gravity == Gravity.RIGHT) {
            rightMenu = child
        }
    }

    private fun isTouchContent(x: Int, y: Int): Boolean {
        val contentView = contentView ?: return false
        return contentView == dragger.findTopChildUnder(x, y)
    }

    private fun isTouchMenu(x: Int, y: Int): Boolean {
        val activeMenu = activeMenu ?: return false
        return activeMenu == dragger.findTopChildUnder(x, y)
    }

    companion object {
        private const val PREVIEW_UNSPECIFIED = -1
        private const val PREVIEW_LEFT = 0
        private const val PREVIEW_RIGHT = 1

        private const val FLAG_IS_OPENED = 0x1
        private const val FLAG_IS_OPENING = 0x2
        private const val FLAG_IS_CLOSING = 0x4

        const val STATE_IDLE = ViewDragHelper.STATE_IDLE
        const val STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING
        const val STATE_SETTLING = ViewDragHelper.STATE_SETTLING
    }

    interface Listener {
        fun onOpened(menuView: View)

        fun onClosed(menuView: View)
    }
}