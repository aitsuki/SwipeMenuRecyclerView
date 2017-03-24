package com.aitsuki.swipe;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by AItsuki on 2017/2/23.
 * 仿IOS message列表，QQ好友列表的交互体验
 * 当有菜单打开的时候，只要不是点击在菜单上，关闭该菜单。
 * 只能同时打开一个菜单，防止多点触控打开菜单
 */
public class SwipeMenuRecyclerView extends RecyclerView {

    public SwipeMenuRecyclerView(Context context) {
        super(context);
    }

    public SwipeMenuRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeMenuRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        // 手指按下的时候，如果有开启的菜单，只要手指不是落在该Item上，则关闭菜单, 并且不分发事件。
        if (action == MotionEvent.ACTION_DOWN) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            View openItem = findOpenItem();
            if (openItem != null && openItem != getTouchItem(x, y)) {
                SwipeItemLayout swipeItemLayout = findSwipeItemLayout(openItem);
                if (swipeItemLayout != null) {
                    swipeItemLayout.close();
                    return false;
                }
            }
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            // FIXME: 2017/3/22 不知道怎么解决多点触控导致可以同时打开多个菜单的bug，先暂时禁止多点触控
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 获取按下位置的Item
     */
    @Nullable
    private View getTouchItem(int x, int y) {
        Rect frame = new Rect();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == VISIBLE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * 找到当前屏幕中开启的的Item
     */
    @Nullable
    private View findOpenItem() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            SwipeItemLayout swipeItemLayout = findSwipeItemLayout(getChildAt(i));
            if (swipeItemLayout != null && swipeItemLayout.isOpen()) {
                return getChildAt(i);
            }
        }
        return null;
    }

    /**
     * 获取该View
     */
    @Nullable
    private SwipeItemLayout findSwipeItemLayout(View view) {
        if (view instanceof SwipeItemLayout) {
            return (SwipeItemLayout) view;
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                SwipeItemLayout swipeLayout = findSwipeItemLayout(group.getChildAt(i));
                if (swipeLayout != null) {
                    return swipeLayout;
                }
            }
        }
        return null;
    }

}
