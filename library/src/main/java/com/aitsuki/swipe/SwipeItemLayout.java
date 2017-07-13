package com.aitsuki.swipe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by AItsuki on 2017/2/23.
 * 1. 最多同时设置两个菜单
 * 2. 菜单必须设置layoutGravity属性. start left end right
 */
public class SwipeItemLayout extends FrameLayout {

    public static final String TAG = "SwipeItemLayout";

    private ViewDragHelper mDragHelper;
    private int mTouchSlop;
    private int mVelocity;

    private float mDownX;
    private float mDownY;
    private boolean mIsDragged;
    private boolean mSwipeEnable = true;

    /**
     * 通过判断手势进行赋值 {@link #checkCanDragged(MotionEvent)}
     */
    private View mCurrentMenu;

    /**
     * 某些情况下，不能通过mIsOpen判断当前菜单是否开启或是关闭。
     * 因为在调用 {@link #open()} 或者 {@link #close()} 的时候，mIsOpen的值已经被改变，但是
     * 此时ContentView还没有到达应该的位置。亦或者ContentView已经到拖拽达指定位置，但是此时并没有
     * 松开手指，mIsOpen并不会重新赋值。
     */
    private boolean mIsOpen;

    /**
     * Menu的集合，以{@link android.view.Gravity#LEFT}和{@link android.view.Gravity#LEFT}作为key，
     * 菜单View作为value保存。
     */
    private LinkedHashMap<Integer, View> mMenus = new LinkedHashMap<>();

    private List<SwipeListener> mListeners;

    public SwipeItemLayout(Context context) {
        this(context, null);
    }

    public SwipeItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        mDragHelper = ViewDragHelper.create(this, new DragCallBack());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateMenu();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 关闭菜单过程中禁止接收down事件
            if (isCloseAnimating()) {
                return false;
            }

            // 菜单打开的时候，按下Content关闭菜单
            if (mIsOpen && isTouchContent(((int) ev.getX()), ((int) ev.getY()))) {
                close();
                return false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mSwipeEnable) {
            return false;
        }

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mIsDragged = false;
                mDownX = ev.getX();
                mDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                checkCanDragged(ev);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mIsDragged) {
                    mDragHelper.processTouchEvent(ev);
                    mIsDragged = false;
                }
                break;
            default:
                if (mIsDragged) {
                    mDragHelper.processTouchEvent(ev);
                }
                break;
        }
        return mIsDragged || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mSwipeEnable) {
            return super.onTouchEvent(ev);
        }

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mIsDragged = false;
                mDownX = ev.getX();
                mDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                boolean beforeCheckDrag = mIsDragged;
                checkCanDragged(ev);
                if (mIsDragged) {
                    mDragHelper.processTouchEvent(ev);
                }

                // 开始拖动后，发送一个cancel事件用来取消点击效果
                if (!beforeCheckDrag && mIsDragged) {
                    MotionEvent obtain = MotionEvent.obtain(ev);
                    obtain.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(obtain);
                }

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mIsDragged || mIsOpen) {
                    mDragHelper.processTouchEvent(ev);
                    // 拖拽后手指抬起，或者已经开启菜单，不应该响应到点击事件
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    mIsDragged = false;
                }
                break;
            default:
                if (mIsDragged) {
                    mDragHelper.processTouchEvent(ev);
                }
                break;
        }
        return mIsDragged || super.onTouchEvent(ev)
                // 此判断是因为当没有点击事件时，事件会给RecylcerView响应导致无法划开菜单。
                || (!isClickable() && mMenus.size() > 0);
    }

    /**
     * 判断是否可以拖拽View
     */
    @SuppressLint("RtlHardcoded")
    private void checkCanDragged(MotionEvent ev) {
        if (mIsDragged) {
            return;
        }

        float dx = ev.getX() - mDownX;
        float dy = ev.getY() - mDownY;
        boolean isRightDrag = dx > mTouchSlop && dx > Math.abs(dy);
        boolean isLeftDrag = dx < -mTouchSlop && Math.abs(dx) > Math.abs(dy);

        if (mIsOpen) {
            // 开启状态下，点击在content上就捕获事件，点击在菜单上则判断touchSlop
            int downX = (int) mDownX;
            int downY = (int) mDownY;
            if (isTouchContent(downX, downY)) {
                mIsDragged = true;
            } else if (isTouchMenu(downX, downY)) {
                mIsDragged = (isLeftMenu() && isLeftDrag) || (isRightMenu() && isRightDrag);
            }

        } else {
            // 关闭状态，获取当前即将要开启的菜单。
            if (isRightDrag) {
                mCurrentMenu = mMenus.get(Gravity.LEFT);
                mIsDragged = mCurrentMenu != null;
            } else if (isLeftDrag) {
                mCurrentMenu = mMenus.get(Gravity.RIGHT);
                mIsDragged = mCurrentMenu != null;
            }
        }

        if (mIsDragged) {
            // 开始拖动后，分发down事件给DragHelper，并且发送一个cancel取消点击事件
            MotionEvent obtain = MotionEvent.obtain(ev);
            obtain.setAction(MotionEvent.ACTION_DOWN);
            mDragHelper.processTouchEvent(obtain);
            if (getParent() != null) {
                // 解决和父控件的滑动冲突。
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }
    }

    // 最后一个是内容，倒数第1第2个设置了layout_gravity = right or left的是菜单，其余的忽略
    @SuppressLint("RtlHardcoded")
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int gravity = GravityCompat.getAbsoluteGravity(lp.gravity, ViewCompat.getLayoutDirection(child));
        switch (gravity) {
            case Gravity.RIGHT:
                mMenus.put(Gravity.RIGHT, child);
                break;
            case Gravity.LEFT:
                mMenus.put(Gravity.LEFT, child);
                break;
            default:
                break;
        }
    }

    public void setSwipeEnable(boolean enable) {
        mSwipeEnable = enable;
    }

    public boolean isSwipeEnable() {
        return mSwipeEnable;
    }

    /**
     * 获取ContentView，最上层显示的View即为ContentView
     */
    public View getContentView() {
        return getChildAt(getChildCount() - 1);
    }

    /**
     * 判断down是否点击在Content上
     */
    public boolean isTouchContent(int x, int y) {
        View contentView = getContentView();
        if (contentView == null) {
            return false;
        }
        Rect rect = new Rect();
        contentView.getHitRect(rect);
        return rect.contains(x, y);
    }

    @SuppressLint("RtlHardcoded")
    private boolean isLeftMenu() {
        return mCurrentMenu != null && mCurrentMenu == mMenus.get(Gravity.LEFT);
    }

    @SuppressLint("RtlHardcoded")
    private boolean isRightMenu() {
        return mCurrentMenu != null && mCurrentMenu == mMenus.get(Gravity.RIGHT);
    }

    public boolean isTouchMenu(int x, int y) {
        if (mCurrentMenu == null) {
            return false;
        }

        Rect rect = new Rect();
        mCurrentMenu.getHitRect(rect);
        return rect.contains(x, y);
    }

    private boolean checkAbsoluteGravity(View menu, int checkFor) {
        final int absGravity = getAbsoluteGravity(menu);
        return (absGravity & checkFor) == checkFor;
    }

    private int getAbsoluteGravity(View menu) {
        final int gravity = ((LayoutParams) menu.getLayoutParams()).gravity;
        return GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(this));
    }

    /**
     * 关闭菜单
     */
    public void close() {
        if (mCurrentMenu == null) {
            mIsOpen = false;
            return;
        }
        mDragHelper.smoothSlideViewTo(getContentView(), getPaddingLeft(), getPaddingTop());
        mIsOpen = false;
        if (mListeners != null) {
            int listenerCount = mListeners.size();
            for (int i = listenerCount - 1; i >= 0; i--) {
                mListeners.get(i).onSwipeClose(this);
            }
        }
        invalidate();
    }

    /**
     * 开启菜单
     */
    public void open() {
        if (mCurrentMenu == null) {
            mIsOpen = false;
            return;
        }

        if (isLeftMenu()) {
            mDragHelper.smoothSlideViewTo(getContentView(), mCurrentMenu.getWidth(), getPaddingTop());
        } else if (isRightMenu()) {
            mDragHelper.smoothSlideViewTo(getContentView(), -mCurrentMenu.getWidth(), getPaddingTop());
        }
        mIsOpen = true;
        if (mListeners != null) {
            int listenerCount = mListeners.size();
            for (int i = listenerCount - 1; i >= 0; i--) {
                mListeners.get(i).onSwipeOpen(this);
            }
        }
        invalidate();
    }

    /**
     * 菜单是否开始拖动
     */
    public boolean isOpen() {
        return mIsOpen;
    }

    /**
     * 是否正在做开启动画
     */
    private boolean isOpenAnimating() {
        if (mCurrentMenu != null) {
            int contentLeft = getContentView().getLeft();
            int menuWidth = mCurrentMenu.getWidth();
            if (mIsOpen && ((isLeftMenu() && contentLeft < menuWidth)
                    || (isRightMenu() && -contentLeft < menuWidth))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否正在做关闭动画
     */
    private boolean isCloseAnimating() {
        if (mCurrentMenu != null) {
            int contentLeft = getContentView().getLeft();
            if (!mIsOpen && ((isLeftMenu() && contentLeft > 0) || (isRightMenu() && contentLeft < 0))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 当菜单被ContentView遮住的时候，要设置菜单为Invisible，防止已隐藏的菜单接收到点击事件。
     */
    private void updateMenu() {
        View contentView = getContentView();
        if (contentView != null) {
            int contentLeft = contentView.getLeft();
            if (contentLeft == 0) {
                for (View view : mMenus.values()) {
                    if (checkAbsoluteGravity(view, Gravity.LEFT)) {
                        view.layout(-view.getWidth(), view.getTop(), 0, view.getBottom());
                    } else {
                        view.layout(getMeasuredWidth(), view.getTop(),
                                getMeasuredWidth() + view.getMeasuredWidth(), view.getBottom());
                    }
                }
            } else {
                if (mCurrentMenu != null && mCurrentMenu.getLeft() != 0) {
                    if (isLeftMenu()) {
                        mCurrentMenu.layout(0, mCurrentMenu.getTop(),
                                mCurrentMenu.getMeasuredWidth(), mCurrentMenu.getBottom());
                    } else {
                        mCurrentMenu.layout(
                                getMeasuredWidth() - mCurrentMenu.getMeasuredWidth(),
                                mCurrentMenu.getTop(),
                                getMeasuredWidth(),
                                mCurrentMenu.getBottom());
                    }
                }
            }
        }
    }

    /**
     * 添加一个监听器用于监听SwipeItemLayout的开启和关闭
     *
     * @param listener SwipeListener
     */
    public void addSwipeListener(SwipeListener listener) {
        if (listener == null) {
            return;
        }

        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(listener);
    }

    /**
     * 移除监听器
     */
    public void removeSwipeListener(SwipeListener listener) {
        if (listener == null) {
            return;
        }

        if (mListeners == null) {
            return;
        }

        mListeners.remove(listener);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private class DragCallBack extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            // menu和content都可以抓取，因为在menu的宽度为MatchParent的时候，是无法点击到content的
            return child == getContentView() || mMenus.containsValue(child);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {

            // 如果child是内容， 那么可以左划或右划，开启或关闭菜单
            if (child == getContentView()) {
                if (isRightMenu()) {
                    return left > 0 ? 0 : left < -mCurrentMenu.getWidth() ?
                            -mCurrentMenu.getWidth() : left;
                } else if (isLeftMenu()) {
                    return left > mCurrentMenu.getWidth() ? mCurrentMenu.getWidth() : left < 0 ?
                            0 : left;
                }
            }

            // 如果抓取到的child是菜单，那么不移动child，而是移动contentView
            else if (isRightMenu()) {
                View contentView = getContentView();
                int newLeft = contentView.getLeft() + dx;
                if (newLeft > 0) {
                    newLeft = 0;
                } else if (newLeft < -child.getWidth()) {
                    newLeft = -child.getWidth();
                }
                contentView.layout(newLeft, contentView.getTop(), newLeft + contentView.getWidth(),
                        contentView.getBottom());
                return child.getLeft();
            } else if (isLeftMenu()) {
                View contentView = getContentView();
                int newLeft = contentView.getLeft() + dx;
                if (newLeft < 0) {
                    newLeft = 0;
                } else if (newLeft > child.getWidth()) {
                    newLeft = child.getWidth();
                }
                contentView.layout(newLeft, contentView.getTop(), newLeft + contentView.getWidth(),
                        contentView.getBottom());
                return child.getLeft();
            }
            return 0;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            updateMenu();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            Log.e(TAG, "onViewReleased: " + xvel + " ,releasedChild = " + releasedChild);
            if (isLeftMenu()) {
                if (xvel > mVelocity) {
                    open();
                } else if (xvel < -mVelocity) {
                    close();
                } else {
                    if (getContentView().getLeft() > mCurrentMenu.getWidth() / 3 * 2) {
                        open();
                    } else {
                        close();
                    }
                }
            } else if (isRightMenu()) {
                if (xvel < -mVelocity) {
                    open();
                } else if (xvel > mVelocity) {
                    close();
                } else {
                    if (getContentView().getLeft() < -mCurrentMenu.getWidth() / 3 * 2) {
                        open();
                    } else {
                        close();
                    }
                }
            }
        }

    }

    public interface SwipeListener {
        void onSwipeOpen(SwipeItemLayout view);

        void onSwipeClose(SwipeItemLayout view);
    }
}
