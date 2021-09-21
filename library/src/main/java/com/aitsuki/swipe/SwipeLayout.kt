package com.aitsuki.swipe

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import java.lang.reflect.Constructor
import kotlin.math.abs

private const val TAG = "SwipeLayout"

private val designerConstructors =
    ThreadLocal<MutableMap<String, Constructor<SwipeLayout.Designer>>>()

/**
 * Created by AItsuki on 2017/2/23.
 */
class SwipeLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    companion object {
        private const val PREVIEW_NONE = 0
        private const val PREVIEW_LEFT = 1
        private const val PREVIEW_RIGHT = 2

        private const val FLAG_IS_OPENED = 0x1
        private const val FLAG_IS_OPENING = 0x2
        private const val FLAG_IS_CLOSING = 0x4

        const val STATE_IDLE = ViewDragHelper.STATE_IDLE
        const val STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING
        const val STATE_SETTLING = ViewDragHelper.STATE_SETTLING
    }

    private val matchParentChildren = ArrayList<View>(1)

    private var preview = PREVIEW_NONE
    var autoClose = false

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val velocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity

    private var isDragging = false
    private var initialMotionX = 0f
    private var initialMotionY = 0f
    private var alwaysInTapRegion = false

    private val dragger = ViewDragHelper.create(this, ViewDragCallback())

    private var openState = 0
        set(value) {
            field = value
            Log.d(TAG, "openState: $value")
        }
    private var activeMenu: View? = null
    private var onScreen = 0f
    private val listeners = arrayListOf<Listener>()
    private val isOpenOrOpening
        get() = openState and FLAG_IS_OPENED == FLAG_IS_OPENED
                || openState and FLAG_IS_OPENING == FLAG_IS_OPENING

    private var contentView: View? = null
    private var leftMenu: View? = null
    private var rightMenu: View? = null
    private val designer: Designer
    private var initDesigner = false
    private var firstLayout = true

    var swipeEnable = true
        set(value) {
            closeActiveMenu()
            field = value
        }

    init {
        isClickable = true
        var designer: Designer? = null
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.SwipeLayout)
            preview = a.getInt(R.styleable.SwipeLayout_preview, preview)
            autoClose = a.getBoolean(R.styleable.SwipeLayout_autoClose, autoClose)
            designer =
                Designer.parseDesigner(context, a.getString(R.styleable.SwipeLayout_designer))
            a.recycle()
        }
        this.designer = designer ?: ClassicDesigner()
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
        val activeMenu = activeMenu ?: return
        when {
            firstLayout -> {
                onScreen = 0f
                openState = 0
            }
            animate -> {
                if (openState and FLAG_IS_OPENED == FLAG_IS_OPENED) {
                    openState = openState or FLAG_IS_CLOSING
                }
                dragger.smoothSlideViewTo(contentView, paddingLeft, contentView.top)
            }
            else -> {
                ViewCompat.offsetLeftAndRight(contentView, -contentView.left + paddingLeft)
                dispatchOnSwipe(activeMenu, 0f)
                updateMenuState(STATE_IDLE)
            }
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
        when {
            firstLayout -> {
                onScreen = 1f
                openState = FLAG_IS_OPENED
            }
            animate -> {
                if (openState == 0) {
                    openState = openState or FLAG_IS_OPENING
                }
                dragger.smoothSlideViewTo(contentView, left, contentView.top)
            }
            else -> {
                ViewCompat.offsetLeftAndRight(contentView, left - contentView.left)
                dispatchOnSwipe(activeMenu, 1f)
                updateMenuState(STATE_IDLE)
            }
        }
        invalidate()
    }

    private fun updateMenuState(activeState: Int) {
        val activeMenu = activeMenu ?: return

        for (listener in listeners.asReversed()) {
            listener.onSwipeStateChanged(activeMenu, activeState)
        }

        if (activeState == STATE_IDLE) {
            if (onScreen == 1f) {
                dispatchOnMenuOpened(activeMenu)
            } else {
                dispatchOnMenuClosed(activeMenu)
            }
        }
    }

    private fun dispatchOnMenuClosed(menuView: View) {
        if (openState and FLAG_IS_OPENED == FLAG_IS_OPENED) {
            for (listener in listeners.asReversed()) {
                listener.onMenuClosed(menuView)
            }
        }
        openState = 0
    }

    private fun dispatchOnMenuOpened(menuView: View) {
        if (openState and FLAG_IS_OPENED == 0) {
            for (listener in listeners.asReversed()) {
                listener.onMenuOpened(menuView)
            }
        }
        openState = FLAG_IS_OPENED
    }

    private fun dispatchOnSwipe(menuView: View, offset: Float) {
        onScreen = offset
        for (listener in listeners.asReversed()) {
            listener.onSwipe(menuView, offset)
        }
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

    private fun setContentViewOffset() {
        val contentView = contentView ?: return
        val activeMenu = activeMenu ?: return
        val offset: Float
        if (activeMenu == leftMenu) {
            offset = (contentView.left - paddingLeft).toFloat() / activeMenu.width
            designer.onLayout(
                activeMenu,
                paddingLeft,
                paddingTop,
                contentView.left,
                bottom - paddingBottom
            )
        } else {
            offset = (right - paddingRight - contentView.right).toFloat() / activeMenu.width
            designer.onLayout(
                activeMenu,
                contentView.right,
                paddingTop,
                right - paddingRight,
                bottom - paddingBottom
            )
        }
        if (onScreen != offset) {
            dispatchOnSwipe(activeMenu, offset)
        }
    }

    /**
     * Content和Menu均可以捕获，但是只有Content会移动。
     */
    private inner class ViewDragCallback : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return swipeEnable && (child == contentView || child == leftMenu || child == rightMenu)
        }

        override fun onViewPositionChanged(child: View, left: Int, top: Int, dx: Int, dy: Int) {
            setContentViewOffset()
        }

        override fun onViewDragStateChanged(state: Int) {
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
            if (activeMenu == leftMenu) {
                when {
                    xvel > velocity -> openActiveMenu()
                    xvel < -velocity -> closeActiveMenu()
                    onScreen > 0.5f -> openActiveMenu()
                    else -> closeActiveMenu()
                }
            } else {
                when {
                    xvel < -velocity -> openActiveMenu()
                    xvel > velocity -> closeActiveMenu()
                    onScreen > 0.5f -> openActiveMenu()
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

    private fun isTouchContent(x: Int, y: Int): Boolean {
        val contentView = contentView ?: return false
        return contentView == dragger.findTopChildUnder(x, y)
    }

    private fun isTouchMenu(x: Int, y: Int): Boolean {
        val activeMenu = activeMenu ?: return false
        return activeMenu == dragger.findTopChildUnder(x, y)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        firstLayout = true
    }

    override fun onDetachedFromWindow() {
        if (openState and FLAG_IS_CLOSING == FLAG_IS_CLOSING) {
            dragger.abort()
            contentView?.let {
                onScreen = 0f
                ViewCompat.offsetLeftAndRight(it, paddingLeft - it.left)
            }
        }
        firstLayout = true
        super.onDetachedFromWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var count = childCount
        val measureMatchParentChildren =
            MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                    MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY
        matchParentChildren.clear()
        var childWidth = 0
        var childHeight = 0
        var childState = 0
        for (i in 0 until count) {
            val child = getChildAt(i)

            val lp = child.layoutParams as LayoutParams
            if (lp.gravity == Gravity.NO_GRAVITY) {
                contentView = child
            }
            val absoluteGravity = GravityCompat.getAbsoluteGravity(
                lp.gravity,
                ViewCompat.getLayoutDirection(child)
            )
            val gravity = absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK
            if (gravity == Gravity.LEFT) {
                leftMenu = child
            } else if (gravity == Gravity.RIGHT) {
                rightMenu = child
            }

            if (child.visibility != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
                childWidth = childWidth
                    .coerceAtLeast(child.measuredWidth + lp.leftMargin + lp.rightMargin)
                childHeight = childHeight
                    .coerceAtLeast(child.measuredHeight + lp.topMargin + lp.bottomMargin)
                childState = combineMeasuredStates(childState, child.measuredState)
                if (measureMatchParentChildren) {
                    if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT
                        || lp.height == ViewGroup.LayoutParams.MATCH_PARENT
                    ) {
                        matchParentChildren.add(child)
                    }
                }
            }
        }

        childWidth += paddingLeft + paddingRight
        childHeight += paddingTop + paddingBottom

        setMeasuredDimension(
            resolveSizeAndState(childWidth, widthMeasureSpec, childState),
            resolveSizeAndState(
                childHeight, heightMeasureSpec,
                childState shl MEASURED_HEIGHT_STATE_SHIFT
            )
        )

        count = matchParentChildren.size
        if (count > 1) {
            for (i in 0 until count) {
                val child = matchParentChildren[i]
                val lp = child.layoutParams as MarginLayoutParams

                val childWidthMeasureSpec: Int =
                    if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                        val width = 0.coerceAtLeast(
                            measuredWidth - paddingLeft - paddingRight
                                    - lp.leftMargin - lp.rightMargin
                        )
                        MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
                    } else {
                        getChildMeasureSpec(
                            widthMeasureSpec,
                            paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin,
                            lp.width
                        )
                    }

                val childHeightMeasureSpec: Int =
                    if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                        val height = 0.coerceAtLeast(
                            measuredHeight - paddingTop - paddingBottom
                                    - lp.topMargin - lp.bottomMargin
                        )
                        MeasureSpec.makeMeasureSpec(
                            height, MeasureSpec.EXACTLY
                        )
                    } else {
                        getChildMeasureSpec(
                            heightMeasureSpec,
                            paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin,
                            lp.height
                        )
                    }
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        layoutChildren(left, top, right, bottom)

        if (!initDesigner) {
            designer.onInit(this, leftMenu, rightMenu)
            initDesigner = true
        }

        if (contentView != null && activeMenu != null) {
            val contentView = contentView!!
            val activeMenu = activeMenu!!
            val dx = (activeMenu.width * onScreen).toInt()
            ViewCompat.offsetLeftAndRight(contentView, if (activeMenu == leftMenu) dx else -dx)

            val parentLeft = paddingLeft
            val parentRight = right - left - paddingRight
            val parentTop = paddingTop
            val parentBottom = bottom - top - paddingBottom

            if (activeMenu == leftMenu) {
                designer.onLayout(activeMenu, parentLeft, parentTop, contentView.left, parentBottom)
            } else {
                designer.onLayout(
                    activeMenu, contentView.right, parentTop, parentRight, parentBottom
                )
            }
        }

        if (isInEditMode) {
            if (preview == PREVIEW_LEFT) {
                openLeftMenu(false)
            } else if (preview == PREVIEW_RIGHT) {
                openRightMenu(false)
            }
        }
        firstLayout = false
    }

    private fun layoutChildren(left: Int, top: Int, right: Int, bottom: Int) {
        val parentLeft = paddingLeft
        val parentRight = right - left - paddingRight
        val parentTop = paddingTop
        val parentBottom = bottom - top - paddingBottom

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue

            val width = child.measuredWidth
            val height = child.measuredHeight

            val lp = child.layoutParams as LayoutParams
            val layoutDirection = ViewCompat.getLayoutDirection(this)
            val absoluteGravity = GravityCompat.getAbsoluteGravity(lp.gravity, layoutDirection)
            val verticalGravity = lp.gravity and Gravity.VERTICAL_GRAVITY_MASK

            val childLeft: Int = when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.RIGHT -> parentRight - width - lp.rightMargin
                else -> parentLeft + lp.leftMargin
            }

            val childTop = when (verticalGravity) {
                Gravity.TOP -> parentTop + lp.topMargin
                Gravity.CENTER_VERTICAL ->
                    parentTop + (parentBottom - parentTop - height) / 2 +
                            lp.topMargin - lp.bottomMargin
                Gravity.BOTTOM -> parentBottom - height - lp.bottomMargin
                else -> parentTop + lp.topMargin
            }
            child.layout(childLeft, childTop, childLeft + width, childTop + height)
        }
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return when (p) {
            is LayoutParams -> LayoutParams(p)
            is MarginLayoutParams -> LayoutParams(p)
            else -> LayoutParams(p)
        }
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams && super.checkLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    class LayoutParams : MarginLayoutParams {

        var gravity = Gravity.NO_GRAVITY

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.layout_gravity))
            this.gravity = a.getInt(0, Gravity.NO_GRAVITY)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)

        constructor(width: Int, height: Int, gravity: Int) : this(width, height) {
            this.gravity = gravity
        }

        constructor(source: LayoutParams) : super(source) {
            this.gravity = source.gravity
        }

        constructor(source: ViewGroup.LayoutParams) : super(source)

        constructor(source: MarginLayoutParams) : super(source)
    }

    interface Listener {

        fun onSwipe(menuView: View, swipeOffset: Float) {}

        fun onSwipeStateChanged(menuView: View, newState: Int) {}

        fun onMenuOpened(menuView: View) {}

        fun onMenuClosed(menuView: View) {}
    }

    interface Designer {


        fun onInit(parent: SwipeLayout, leftMenu: View?, rightMenu: View?)

        /**
         * @param menuView activeMenu
         * @param left visible left
         * @param top visible top
         * @param right visible right
         * @param bottom visible bottom
         */
        fun onLayout(menuView: View, left: Int, top: Int, right: Int, bottom: Int)

        companion object {
            fun parseDesigner(context: Context, name: String?): Designer? {
                if (name.isNullOrEmpty()) return null

                val fullName =
                    if (name.startsWith(".")) context.packageName + name else name

                try {
                    var constructors = designerConstructors.get()
                    if (constructors == null) {
                        constructors = mutableMapOf()
                    }
                    var c = constructors[fullName]
                    if (c == null) {
                        @Suppress("UNCHECKED_CAST")
                        val clazz = Class.forName(fullName, false, context.classLoader)
                                as Class<Designer>
                        c = clazz.getConstructor()
                        c.isAccessible = true
                        constructors[fullName] = c
                        return c.newInstance()
                    }
                    return null
                } catch (e: Exception) {
                    throw RuntimeException("Could not inflate Designer subclass $fullName", e)
                }
            }
        }
    }

    class OverlayDesigner : Designer {

        private lateinit var parent: SwipeLayout

        override fun onInit(parent: SwipeLayout, leftMenu: View?, rightMenu: View?) {
            this.parent = parent
            leftMenu?.visibility = INVISIBLE
            rightMenu?.visibility = INVISIBLE
        }

        override fun onLayout(menuView: View, left: Int, top: Int, right: Int, bottom: Int) {
            menuView.visibility = if (right - left > 0) VISIBLE else INVISIBLE
        }
    }

    class ClassicDesigner : Designer {

        private var leftMenu: View? = null

        override fun onInit(parent: SwipeLayout, leftMenu: View?, rightMenu: View?) {
            this.leftMenu = leftMenu
            leftMenu?.visibility = View.INVISIBLE
            rightMenu?.visibility = View.INVISIBLE
        }

        override fun onLayout(menuView: View, left: Int, top: Int, right: Int, bottom: Int) {
            menuView.visibility = if (right - left > 0) VISIBLE else INVISIBLE
            if (menuView == leftMenu) {
                menuView.layout(right - menuView.width, menuView.top, right, menuView.bottom)
            } else {
                menuView.layout(left, menuView.top, left + menuView.width, menuView.bottom)
            }
        }
    }
}