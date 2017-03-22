package com.aitsuki.swipedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aitsuki.swipe.SwipeItemLayout;
import com.aitsuki.swipedemo.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToastUtil.init(getApplication());
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        DemoAdapter adapter =new DemoAdapter(fakeData(20, "测试RecyclerView"), mItemTouchListener);
        recyclerView.setAdapter(adapter);
    }

    private List<String> fakeData(int count, String content) {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            data.add(content);
        }
        return data;
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

    private static class DemoAdapter extends RecyclerView.Adapter<SwipeItemHolder> {

        private ItemTouchListener mItemTouchListener;
        private List<String> mData;

        public DemoAdapter(List<String> data, ItemTouchListener itemTouchListener) {
            this.mData = data;
            this.mItemTouchListener = itemTouchListener;
        }

        @Override
        public SwipeItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.swipe_item, parent, false);
            return new SwipeItemHolder(rootView);
        }

        @Override
        public void onBindViewHolder(final SwipeItemHolder holder, int position) {
            holder.mContent.setText(mData.get(position).concat(" " + position));
            if (mItemTouchListener != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mItemTouchListener.onItemClick(holder.mContent.getText().toString());
                    }
                });

                holder.mLeftMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mItemTouchListener.onLeftMenuClick("leftMenu" + holder.getAdapterPosition());
                        holder.mSwipeItemLayout.close();
                    }
                });

                holder.mRightMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mItemTouchListener.onRightMenuClick("rightMenu" + holder.getAdapterPosition());
                        holder.mSwipeItemLayout.close();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    private static class SwipeItemHolder extends RecyclerView.ViewHolder {

        private final View mLeftMenu;
        private final View mRightMenu;
        private final TextView mContent;
        private final SwipeItemLayout mSwipeItemLayout;

        SwipeItemHolder(View itemView) {
            super(itemView);
            mSwipeItemLayout = (SwipeItemLayout) itemView.findViewById(R.id.swipe_layout);
            mContent = (TextView) itemView.findViewById(R.id.content);
            mLeftMenu = itemView.findViewById(R.id.left_menu);
            mRightMenu = itemView.findViewById(R.id.right_menu);
        }
    }

    private interface ItemTouchListener {
        void onItemClick(String str);

        void onLeftMenuClick(String str);

        void onRightMenuClick(String str);
    }
}
