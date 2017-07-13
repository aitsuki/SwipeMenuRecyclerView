package com.aitsuki.swipedemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.aitsuki.swipe.SwipeItemLayout;
import com.aitsuki.swipedemo.data.Repository;
import com.aitsuki.swipedemo.entity.Data;
import com.aitsuki.swipedemo.entity.Type;
import com.aitsuki.swipedemo.util.ToastUtil;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * Created by AItsuki on 2017/7/11.
 * 使用CymChad的adapter库
 * <a href = "https://github.com/CymChad/BaseRecyclerViewAdapterHelper">
 * BaseRecyclerViewAdapterHelper
 * <a/>
 */
public class CymChadActivity extends AppCompatActivity {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, CymChadActivity.class);
    }

    ItemTouchListener mItemTouchListener = new ItemTouchListener() {
        @Override
        public void onItemClick(String str) {
            ToastUtil.show(str);
        }

        @Override
        public void onLeftMenuClick(String str) {
            ToastUtil.show(str);
        }

        @Override
        public void onRightMenuClick(String str) {
            ToastUtil.show(str);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_menu_recyclerview);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        DemoAdapter demoAdapter = new DemoAdapter(new Repository().fakeDate(), mItemTouchListener);
        recyclerView.setAdapter(demoAdapter);
    }

    private class DemoAdapter extends BaseMultiItemQuickAdapter<Data, BaseViewHolder> {

        private ItemTouchListener mItemTouchListener;

        DemoAdapter(List<Data> data, ItemTouchListener touchListener) {
            super(data);
            mItemTouchListener = touchListener;
            addItemType(Type.LEFT_MENU, R.layout.item_left_menu);
            addItemType(Type.RIGHT_MENU, R.layout.item_right_menu);
            addItemType(Type.LEFT_AND_RIGHT_MENU, R.layout.item_left_and_right_menu);
            addItemType(Type.LEFT_LONG_MENU, R.layout.item_left_long_menu);
            addItemType(Type.RIGHT_LONG_MENU, R.layout.item_right_long_menu);
            addItemType(Type.LEFT_AND_RIGHT_LONG_MENU, R.layout.item_left_and_right_long_menu);
            addItemType(Type.DISABLE_SWIPE_MENU, R.layout.item_disable_swipe_menu);
        }

        @Override
        protected void convert(BaseViewHolder helper, Data data) {

            helper.setText(R.id.tv_content, data.content + " " + helper.getAdapterPosition());
            final SwipeItemLayout swipeLayout = (SwipeItemLayout) helper.itemView;
            swipeLayout.setSwipeEnable(helper.getItemViewType() != Type.DISABLE_SWIPE_MENU);

            // 不使用helper设置点击事件是因为child点击的时候无法获取到ItemView……，无法关闭菜单
            // 希望BaseRecyclerViewAdapterHelper这个库的作者以后会加上……

            helper.itemView.setOnClickListener(v -> mItemTouchListener.onItemClick(data.content));
            final View leftMenu = helper.getView(R.id.left_menu);
            if (leftMenu != null) {
                leftMenu.setOnClickListener(v -> {
                    mItemTouchListener.onLeftMenuClick("left " + helper.getAdapterPosition());
                    swipeLayout.close();
                });
            }

            final View rightMenu = helper.getView(R.id.right_menu);
            if (rightMenu != null) {
                rightMenu.setOnClickListener(v -> {
                    mItemTouchListener.onLeftMenuClick("right " + helper.getAdapterPosition());
                    swipeLayout.close();
                });
            }
        }
    }
}
