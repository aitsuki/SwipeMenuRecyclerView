package com.aitsuki.swipedemo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aitsuki.swipe.SwipeItemLayout;
import com.aitsuki.swipe.SwipeMenuRecyclerView;
import com.aitsuki.swipedemo.data.Repository;
import com.aitsuki.swipedemo.entity.Data;
import com.aitsuki.swipedemo.entity.Type;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SwipeMenuRecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
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
            Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLeftMenuClick(String str) {
            Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRightMenuClick(String str) {
            Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
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


        @NonNull
        @Override
        public SimpleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

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
                default:
                    layout = R.layout.item_disable_swipe_menu;
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
                holder.mSwipeItemLayout.setOnClickListener(v -> mItemTouchListener.onItemClick(holder.mContent.getText().toString()));

                if (holder.mCardView != null) {
                    holder.mCardView.setOnClickListener(v -> mItemTouchListener.onItemClick(holder.mContent.getText().toString()));
                }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        menu.findItem(R.id.enableTouchAlways).setChecked(recyclerView.isEnableTouchAlways());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        item.setChecked(!item.isChecked());
        recyclerView.setEnableTouchAlways(item.isChecked());
        return false;
    }

    private static class SimpleViewHolder extends RecyclerView.ViewHolder {

        private final View mLeftMenu;
        private final View mRightMenu;
        private final TextView mContent;
        private final SwipeItemLayout mSwipeItemLayout;
        private final CardView mCardView;

        SimpleViewHolder(View itemView) {
            super(itemView);
            mSwipeItemLayout = itemView.findViewById(R.id.swipe_layout);
            mCardView = itemView.findViewById(R.id.card_view);
            mContent = itemView.findViewById(R.id.tv_content);
            mLeftMenu = itemView.findViewById(R.id.left_menu);
            mRightMenu = itemView.findViewById(R.id.right_menu);
        }
    }
}
