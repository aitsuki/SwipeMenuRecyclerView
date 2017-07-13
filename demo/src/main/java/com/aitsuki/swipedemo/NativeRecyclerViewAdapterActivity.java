package com.aitsuki.swipedemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aitsuki.swipe.SwipeItemLayout;
import com.aitsuki.swipedemo.data.Repository;
import com.aitsuki.swipedemo.entity.Data;
import com.aitsuki.swipedemo.entity.Type;
import com.aitsuki.swipedemo.util.ToastUtil;

import java.util.List;

public class NativeRecyclerViewAdapterActivity extends AppCompatActivity {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, NativeRecyclerViewAdapterActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_menu_recyclerview);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        DemoAdapter adapter = new DemoAdapter(new Repository().fakeDate(), mItemTouchListener);
        recyclerView.setAdapter(adapter);
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

    private static class DemoAdapter extends RecyclerView.Adapter<SimpleViewHolder> {

        private ItemTouchListener mItemTouchListener;
        private List<Data> mData;

        DemoAdapter(List<Data> data, ItemTouchListener itemTouchListener) {
            this.mData = data;
            this.mItemTouchListener = itemTouchListener;
        }

        @Override
        public int getItemViewType(int position) {
            return mData.get(position).type;
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        @Override
        public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            @LayoutRes
            int layout;

            switch (viewType) {
                case Type.LEFT_MENU:
                    layout = R.layout.item_left_menu;
                    break;
                case Type.RIGHT_MENU:
                    layout = R.layout.item_right_menu;
                    break;
                case Type.LEFT_AND_RIGHT_MENU:
                    layout = R.layout.item_left_and_right_menu;
                    break;
                case Type.LEFT_LONG_MENU:
                    layout = R.layout.item_left_long_menu;
                    break;
                case Type.RIGHT_LONG_MENU:
                    layout = R.layout.item_right_long_menu;
                    break;
                case Type.LEFT_AND_RIGHT_LONG_MENU:
                    layout = R.layout.item_left_and_right_long_menu;
                    break;
                case Type.DISABLE_SWIPE_MENU:
                    layout = R.layout.item_disable_swipe_menu;
                    break;
                default:
                    layout = R.layout.item_left_menu;
                    break;
            }
            View rootView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return new SimpleViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(final SimpleViewHolder holder, int position) {
            holder.mContent.setText(mData.get(position).content.concat(" " + position));
            holder.mSwipeItemLayout.setSwipeEnable(getItemViewType(position) != Type.DISABLE_SWIPE_MENU);
            if (mItemTouchListener != null) {
                holder.itemView.setOnClickListener(v -> mItemTouchListener.onItemClick(holder.mContent.getText().toString()));

                if (holder.mLeftMenu != null) {
                    holder.mLeftMenu.setOnClickListener(v -> {
                        mItemTouchListener.onLeftMenuClick("left " + holder.getAdapterPosition());
                        holder.mSwipeItemLayout.close();
                    });
                }

                if (holder.mRightMenu != null) {
                    holder.mRightMenu.setOnClickListener(v -> {
                        mItemTouchListener.onRightMenuClick("right " + holder.getAdapterPosition());
                        holder.mSwipeItemLayout.close();
                    });
                }
            }
        }

    }

    private static class SimpleViewHolder extends RecyclerView.ViewHolder {

        private final View mLeftMenu;
        private final View mRightMenu;
        private final TextView mContent;
        private final SwipeItemLayout mSwipeItemLayout;

        SimpleViewHolder(View itemView) {
            super(itemView);
            mSwipeItemLayout = (SwipeItemLayout) itemView.findViewById(R.id.swipe_layout);
            mContent = (TextView) itemView.findViewById(R.id.tv_content);
            mLeftMenu = itemView.findViewById(R.id.left_menu);
            mRightMenu = itemView.findViewById(R.id.right_menu);
        }
    }
}
