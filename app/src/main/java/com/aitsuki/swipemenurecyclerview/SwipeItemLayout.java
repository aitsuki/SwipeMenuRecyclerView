package com.aitsuki.swipemenurecyclerview;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class SwipeItemLayout extends FrameLayout {

    private View menu;
    private View content;
    private final ViewDragHelper dragHelper;
    private boolean isOpen;
    private int currentState;
    private boolean swipeEnable = true;

    public SwipeItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        dragHelper = ViewDragHelper.create(this, callback);
    }

    public void setSwipeEnable(boolean swipeEnable) {
        this.swipeEnable = swipeEnable;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        menu = getChildAt(0);
        content = getChildAt(1);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (swipeEnable) {
            dragHelper.processTouchEvent(event);
        }
        return swipeEnable || super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {

        // 触摸到View的时候就会回调这个方法。
        // return true表示抓取这个View。
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return content == child;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left > 0 ? 0 : left < -menu.getWidth() ? -menu.getWidth() : left;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {

            // x轴移动速度大于菜单一半，或者已经移动到菜单的一般之后，展开菜单
            if (isOpen) {
                if (xvel > menu.getWidth() || -content.getLeft() < menu.getWidth() / 2) {
                    close();
                } else {
                    open();
                }
            } else {
                if (-xvel > menu.getWidth() || -content.getLeft() > menu.getWidth() / 2) {
                    open();
                } else {
                    close();
                }
            }
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return 1;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            currentState = state;
        }
    };

    public void close() {
        dragHelper.smoothSlideViewTo(content, 0, 0);
        isOpen = false;
        invalidate();
    }

    public void open() {
        dragHelper.smoothSlideViewTo(content, -menu.getWidth(), 0);
        isOpen = true;
        invalidate();
    }

    public boolean isOpen() {
        return isOpen;
    }

    public int getState() {
        return currentState;
    }

    private Rect outRect = new Rect();

    public Rect getMenuRect() {
        menu.getHitRect(outRect);
        return outRect;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (dragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        content.setOnClickListener(l);
    }
}
