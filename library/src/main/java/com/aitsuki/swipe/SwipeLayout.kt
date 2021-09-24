package com.aitsuki.swipe

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.SparseIntArray
import android.view.*
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import java.lang.reflect.Constructor
import kotlin.math.abs

/**
 * Created by Aitsuki on 2017/2/23.
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

        private val designerConstructors =
            ThreadLocal<MutableMap<String, Constructor<Designer>>>()
    }

    private val matchParentChildren = ArrayList<View>(1)

    private var preview = PREVIEW_NONE
    var autoClose = false

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val velocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity

    private var isDragging = false
    private var downX = 0
    private var downY = 0
    private var alwaysInTapRegion = false

    private val dragger = ViewDragHelper.create(this, ViewDragCallback())

    private var openState = 0
    private var activeMenu: View? = null
    internal var onScreen = 0f
    private val listeners = arrayListOf<Listener>()
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
                openState = openState or FLAG_IS_CLOSING
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
                openState = openState or FLAG_IS_OPENING
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

        val dx = ev.x.toInt() - downX
        val dy = ev.y.toInt() - downY
        val isRightDragging = dx > touchSlop && dx > abs(dy)
        val isLeftDragging = dx < -touchSlop && abs(dx) > abs(dy)

        if (openState and FLAG_IS_OPENED == FLAG_IS_OPENED
            || openState and FLAG_IS_OPENING == FLAG_IS_OPENING
        ) {
            if (isTouchContent(downX, downY)) {
                isDragging = true
            } else if (isTouchMenu(downX, downY)) {
                isDragging = isLeftDragging || isRightDragging
            }
        } else {
            if (isRightDragging) {
                activeMenu = leftMenu
                isDragging = activeMenu != null
            } else if (isLeftDragging) {
                activeMenu = rightMenu
                isDragging = activeMenu != null
            }
        }

        if (isDragging) {
            val downEvent = MotionEvent.obtain(ev).also { it.action = MotionEvent.ACTION_DOWN }
            dragger.processTouchEvent(downEvent)
        }
    }

    private fun processTouchEvent(ev: MotionEvent) {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x.toInt()
                downY = ev.y.toInt()
                if (autoClose || isTouchContent(downX, downY)) {
                    alwaysInTapRegion = true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val beforeCheckDrag = isDragging
                checkCanDrag(ev)
                if (isDragging) {
                    dragger.processTouchEvent(ev)
                }
                // If begging drag, send a cancel event to cancel click effect.
                if (!beforeCheckDrag && isDragging) {
                    requestDisallowInterceptTouchEvent(true)
                    val cancelEvent = MotionEvent.obtain(ev)
                        .also { it.action = MotionEvent.ACTION_CANCEL }
                    super.onTouchEvent(cancelEvent)
                }
                detectAlwaysInTapRegion(ev)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    dragger.processTouchEvent(ev)
                    isDragging = false
                    requestDisallowInterceptTouchEvent(false)
                }
                if (alwaysInTapRegion) {
                    closeActiveMenu()
                }
            }
            else -> {
                if (isDragging) {
                    dragger.processTouchEvent(ev)
                }
            }
        }
    }

    private fun detectAlwaysInTapRegion(ev: MotionEvent) {
        val dx = (ev.x - downX).toInt()
        val dy = (ev.y - downY).toInt()
        val distance = (dx * dx) + (dy * dy)
        if (distance > touchSlop * touchSlop) {
            alwaysInTapRegion = false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        processTouchEvent(ev)
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        processTouchEvent(ev)
        return isDragging
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

            if (!initDesigner) {
                designer.onInit(this, leftMenu, rightMenu)
                initDesigner = true
            }

            if (child.visibility != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)
                childWidth = childWidth.coerceAtLeast(child.measuredWidth)
                childHeight = childHeight.coerceAtLeast(child.measuredHeight)
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
                val lp = child.layoutParams

                val childWidthMeasureSpec: Int =
                    if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                        val width = 0.coerceAtLeast(measuredWidth - paddingLeft - paddingRight)
                        MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
                    } else {
                        getChildMeasureSpec(widthMeasureSpec, paddingLeft + paddingRight, lp.width)
                    }

                val childHeightMeasureSpec: Int =
                    if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                        val height = 0.coerceAtLeast(measuredHeight - paddingTop - paddingBottom)
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
                    } else {
                        getChildMeasureSpec(
                            heightMeasureSpec,
                            paddingTop + paddingBottom, lp.height
                        )
                    }
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (isInEditMode) {
            if (preview == PREVIEW_LEFT) {
                openLeftMenu(false)
            } else if (preview == PREVIEW_RIGHT) {
                openRightMenu(false)
            }
        }
        val parentLeft = paddingLeft
        val parentRight = right - paddingRight
        val parentTop = paddingTop
        val parentBottom = bottom - paddingBottom

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue

            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            val lp = child.layoutParams as LayoutParams
            val layoutDirection = ViewCompat.getLayoutDirection(this)
            val absoluteGravity = GravityCompat.getAbsoluteGravity(lp.gravity, layoutDirection)
            val verticalGravity = lp.gravity and Gravity.VERTICAL_GRAVITY_MASK

            val childLeft: Int = when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                // layout child out of content
                Gravity.LEFT -> parentLeft - childWidth
                Gravity.RIGHT -> parentRight + childWidth
                else -> parentLeft
            }

            val childTop = when (verticalGravity) {
                Gravity.TOP -> parentTop
                Gravity.CENTER_VERTICAL -> parentTop + (parentBottom - parentTop - childHeight) / 2
                Gravity.BOTTOM -> parentBottom - childHeight
                else -> parentTop
            }
            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
        }

        val contentView = contentView
        if (contentView != null) {
            val activeMenu = activeMenu
            if (activeMenu != null) {
                val dx = (activeMenu.width * onScreen).toInt()
                ViewCompat.offsetLeftAndRight(contentView, if (activeMenu == leftMenu) dx else -dx)
            }

            leftMenu?.let {
                designer.onLayout(it, parentLeft, parentTop, contentView.left, parentBottom)
            }

            rightMenu?.let {
                designer.onLayout(it, contentView.right, parentTop, parentRight, parentBottom)
            }
        }
        firstLayout = false
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return if (p is LayoutParams) {
            LayoutParams(p)
        } else {
            LayoutParams(p)
        }
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams && super.checkLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    class LayoutParams : ViewGroup.LayoutParams {

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
    }

    interface Listener {

        fun onSwipe(menuView: View, swipeOffset: Float) {}

        fun onSwipeStateChanged(menuView: View, newState: Int) {}

        fun onMenuOpened(menuView: View) {}

        fun onMenuClosed(menuView: View) {}
    }

    interface Designer {


        fun onInit(parent: SwipeLayout, leftMenu: View?, rightMenu: View?)

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

    class OverlayDesigner : Designer {

        private var leftMenu: View? = null

        override fun onInit(parent: SwipeLayout, leftMenu: View?, rightMenu: View?) {
            this.leftMenu = leftMenu
            leftMenu?.visibility = View.INVISIBLE
            rightMenu?.visibility = View.INVISIBLE
        }

        override fun onLayout(menuView: View, left: Int, top: Int, right: Int, bottom: Int) {
            val width = right - left
            menuView.visibility = if (width > 0) VISIBLE else INVISIBLE
            if (menuView == leftMenu) {
                if (width == 0) {
                    // If menu is INVISIBLE, move it to outside. See isTouchMenu.
                    menuView.layout(left - menuView.width, menuView.top, left, menuView.bottom)
                } else {
                    menuView.layout(left, menuView.top, left + menuView.width, menuView.bottom)
                }
            } else {
                if (width == 0) {
                    // If menu is INVISIBLE, move it to outside. See isTouchMenu.
                    menuView.layout(right, menuView.top, right + menuView.width, menuView.bottom)
                } else {
                    menuView.layout(right - menuView.width, menuView.top, right, menuView.bottom)
                }
            }
        }
    }

    class ParallaxDesigner : Designer {

        private var leftMenu: View? = null

        override fun onInit(parent: SwipeLayout, leftMenu: View?, rightMenu: View?) {
            this.leftMenu = leftMenu
            leftMenu?.visibility = View.INVISIBLE
            rightMenu?.visibility = View.INVISIBLE
        }

        override fun onLayout(menuView: View, left: Int, top: Int, right: Int, bottom: Int) {
            val width = right - left
            menuView.visibility = if (width > 0) VISIBLE else INVISIBLE
            if (menuView == leftMenu) {
                if (width == 0) {
                    // If menu is INVISIBLE, move it to outside. See isTouchMenu.
                    menuView.layout(left - menuView.width, menuView.top, left, menuView.bottom)
                } else {
                    menuView.layout(left, menuView.top, left + menuView.width, menuView.bottom)
                    if (menuView is ViewGroup && menuView.childCount > 1) {
                        layoutLeftMenu(menuView, left, right)
                    }
                }
            } else {
                if (width == 0) {
                    // If menu is INVISIBLE, move it to outside. See isTouchMenu.
                    menuView.layout(right, menuView.top, right + menuView.width, menuView.bottom)
                } else {
                    menuView.layout(right - menuView.width, menuView.top, right, menuView.bottom)
                    if (menuView is ViewGroup && menuView.childCount > 0) {
                        layoutRightMenu(menuView, left, right)
                    }
                }
            }
        }

        private fun layoutLeftMenu(menuView: ViewGroup, left: Int, right: Int) {
            val onScreen = (right - left).toFloat() / menuView.width
            var child = menuView.getChildAt(menuView.childCount - 1)
            var childLeft = (right - child.width * onScreen).toInt()
            child.layout(childLeft, child.top, childLeft + child.width, child.bottom)
            var prevChild = child
            for (i in menuView.childCount - 2 downTo 0) {
                child = menuView.getChildAt(i)
                childLeft = (prevChild.left - child.width * onScreen).toInt()
                child.layout(
                    childLeft,
                    child.top,
                    childLeft + child.width,
                    child.bottom
                )
                prevChild = child
            }
        }

        private fun layoutRightMenu(menuView: ViewGroup, left: Int, right: Int) {
            val onScreen = (right - left).toFloat() / menuView.width
            var child = menuView.getChildAt(menuView.childCount - 1)
            var childLeft = menuView.width - (child.width * onScreen).toInt()
            child.layout(childLeft, child.top, childLeft + child.width, child.bottom)
            var prevChild = child
            for (i in menuView.childCount - 2 downTo 0) {
                child = menuView.getChildAt(i)
                childLeft = (prevChild.left - child.width * onScreen).toInt()
                child.layout(
                    childLeft,
                    child.top,
                    childLeft + child.width,
                    child.bottom
                )
                prevChild = child
            }
        }
    }
}