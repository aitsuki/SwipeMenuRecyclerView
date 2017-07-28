package com.aitsuki.swipe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by AItsuki on 2017/7/25
 * 通过layout_gravity属性指定各个View的功能, 并且最多只能存在一个Content， 一个LeftMenu，一个RightMenu。
 * <p>
 * Content : 不设置layout_gravity属性
 * LeftMenu : layout_gravity & Gravity.LEFT != 0
 * RightMenu : layout_gravity & Gravity.RIGHT != 0
 * <p>
 * 分别使用LeftDragger和RightDragger两个DragHelper来控制左右滑动
 */
@SuppressLint("RtlHardcoded")
public class SwipeLayout extends ViewGroup {

    static final int[] LAYOUT_ATTRS = new int[]{
            android.R.attr.layout_gravity
    };

    private static final int NO_PREVIEW = -1;
    private static final int PREVIEW_LEFT = 0;
    private static final int PREVIEW_RIGHT = 1;

    /**
     * 预览菜单开启后的效果，只在editMode中起作用
     */
    private int mPreview = NO_PREVIEW;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeLayout);
        mPreview = a.getInt(R.styleable.SwipeLayout_preview, NO_PREVIEW);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);

        boolean hasContent = false;
        boolean hasLeftMenu = false;
        boolean hasRightMenu = false;

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {

            // 检测是否有重复的content或者相同方向的menu
            final View child = getChildAt(i);
            if (isContentView(child)) {
                if (hasContent) {
                    throw new IllegalStateException("The attribute layout_gravity = " +
                            "Gravity.NO_GRAVITY! Already have a content view!");
                }
                hasContent = true;
            } else if (isMenuView(child)) {
                boolean isLeftMenuView = checkChildViewAbsGravity(child, Gravity.LEFT);

                if (isLeftMenuView && hasLeftMenu) {
                    throw new IllegalStateException("Already have a left menu");
                } else if (!isLeftMenuView && hasRightMenu) {
                    throw new IllegalStateException("Already have a right menu");
                }

                if (isLeftMenuView) {
                    hasLeftMenu = true;
                } else {
                    hasRightMenu = true;
                }
            } else {
                throw new IllegalStateException("The attribute layout_gravity must be " +
                        "Gravity.LEFT, Gravity.RIGHT or Gravity.NO_GRAVITY!");
            }

            // 忽略padding属性
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, lp.width);
            final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0, lp.height);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();

            int childLeft = 0;

            if (isContentView(child)) {  // contentView
                if (isInEditMode()) {
                    if (mPreview == PREVIEW_LEFT) {
                        final View leftMenuView = findMenuViewByGravity(Gravity.LEFT);
                        if (leftMenuView != null) {
                            childLeft = leftMenuView.getMeasuredWidth();
                        }
                    } else if (mPreview == PREVIEW_RIGHT) {
                        final View rightMenuView = findMenuViewByGravity(Gravity.RIGHT);
                        childLeft = -rightMenuView.getMeasuredWidth();
                    }
                }
            } else {  // menuView
                final boolean isLeftMenuView = checkChildViewAbsGravity(child, Gravity.LEFT);
                if (!isLeftMenuView) {
                    childLeft = r - l - childWidth;
                }
            }
            child.layout(childLeft, t, childLeft + childWidth, t + childHeight);
        }

        // 隐藏或显示菜单（需要判断contentView的left是否等于0，所以必须layout完毕之后才执行下面代码）
        final View contentView = findContentView();
        final View leftMenuView = findMenuViewByGravity(Gravity.LEFT);
        final View rightMenuView = findMenuViewByGravity(Gravity.RIGHT);
        if (contentView != null) {
            final int offset = contentView.getLeft();
            if (leftMenuView != null) {
                int newVisibility = offset > 0 ? VISIBLE : INVISIBLE;
                if (leftMenuView.getVisibility() != newVisibility) {
                    leftMenuView.setVisibility(newVisibility);
                }
            }

            if (rightMenuView != null) {
                int newVisibility = offset < 0 ? VISIBLE : INVISIBLE;
                if (rightMenuView.getVisibility() != newVisibility) {
                    rightMenuView.setVisibility(newVisibility);
                }
            }
        }
    }

    /**
     * 判断child是不是ContentView
     *
     * @param view child
     * @return 判断是不是没有设置Gravity
     */
    boolean isContentView(View view) {
        final LayoutParams lp = (LayoutParams) view.getLayoutParams();
        final int gravity = lp.gravity;
        return gravity == Gravity.NO_GRAVITY;
    }

    /**
     * 判断child是不是menuView
     *
     * @param view child
     * @return 判断child是否拥有水平方向的Gravity
     */
    boolean isMenuView(View view) {
        final int absGravity = getChildViewAbsGravity(view);
        return (absGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) != 0;
    }

    /**
     * 查找ContentView， 没有设置Gravity的为ContentView
     *
     * @return 找不到返回null
     */
    View findContentView() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (isContentView(child)) {
                return child;
            }
        }
        return null;
    }

    /**
     * 查找某个方向上的gravity
     *
     * @param gravity Gravity
     * @return 找到返回，找不到返回null
     */
    View findMenuViewByGravity(int gravity) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (checkChildViewAbsGravity(child, gravity)) {
                return child;
            }
        }
        return null;
    }

    /**
     * 判断child的Gravity
     *
     * @param view  child
     * @param check Gravity方向
     * @return 如果child的absGravity == child，返回true
     */
    boolean checkChildViewAbsGravity(View view, int check) {
        final int absGravity = getChildViewAbsGravity(view);
        return (absGravity & check) == check;
    }

    /**
     * 获取child的absGravity
     *
     * @param view child
     * @return absGravity
     */
    int getChildViewAbsGravity(View view) {
        final LayoutParams lp = (LayoutParams) view.getLayoutParams();
        final int gravity = lp.gravity;
        return GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(this));
    }

    // ===============  DragCallBack =========================
    class DragCallBack extends ViewDragHelper.Callback {

        private final int mMenuGravity;
        private final ViewDragHelper mDragger;

        private View mMenuView;
        private View mContentView;
        private boolean mIsCaptureContent;

        DragCallBack(int gravity, ViewDragHelper dragHelper) {
            mMenuGravity = gravity;
            mDragger = dragHelper;
        }

        // menu和content都可以抓取，因为在menu的宽度为MatchParent的时候，是无法点击到content的
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            if (checkChildViewAbsGravity(child, mMenuGravity)) { // 抓取到相同gravity的menu
                mIsCaptureContent = false;
                mMenuView = child;
                mContentView = findContentView();
            } else if (isContentView(child)) { // 抓取到content
                mIsCaptureContent = true;
                mMenuView = findMenuViewByGravity(mMenuGravity);
                mContentView = child;
            } else { // 抓取到其他
                return false;
            }
            // content和menu都不能为null
            return !(mContentView == null || mMenuView == null);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int menuWidth = mMenuView.getWidth();

            // 需要判断抓到的是content还是menu
            if (mIsCaptureContent) {
                if (mMenuGravity == Gravity.LEFT) {
                    return left > menuWidth ? menuWidth : left < 0 ? 0 : left;
                } else {
                    return left > 0 ? 0 : left < -menuWidth ? -menuWidth : left;
                }
            } else {  // 如果抓取到的child是菜单，那么不移动child，而是移动contentView
                int newLeft = mContentView.getLeft() + dx;
                if (mMenuGravity == Gravity.LEFT) {
                    if (newLeft < 0) {
                        newLeft = 0;
                    } else if (newLeft > menuWidth) {
                        newLeft = menuWidth;
                    }
                } else {
                    if (newLeft > 0) {
                        newLeft = 0;
                    } else if (newLeft < -menuWidth) {
                        newLeft = -menuWidth;
                    }
                }
                mContentView.layout(newLeft, mContentView.getTop(),
                        newLeft + mContentView.getWidth(), mContentView.getBottom());
                return child.getLeft();
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            // 显示或隐藏菜单
            mMenuView.setVisibility(mContentView.getLeft() == 0 ? INVISIBLE : VISIBLE);
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            final int left = mContentView.getLeft();
            final int menuWidth = mMenuView.getWidth();
            int target;
            if (mMenuGravity == Gravity.LEFT) {
                target = xvel >= 0 && left > menuWidth / 2 ? menuWidth : 0;
            } else {
                target = xvel <= 0 && left < -menuWidth / 2 ? -menuWidth : 0;
            }
            mDragger.smoothSlideViewTo(mContentView, target, mContentView.getTop());
        }
    }


    // ===============  layoutParams =========================

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new SwipeLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof LayoutParams) {
            return new LayoutParams(((LayoutParams) lp));
        }
        return new LayoutParams(lp);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p);
    }


    public static class LayoutParams extends ViewGroup.LayoutParams {

        public int gravity = Gravity.NO_GRAVITY;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
            gravity = a.getInt(0, Gravity.NO_GRAVITY);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.gravity = source.gravity;
        }
    }
}
