package com.aitsuki.swipemenurecyclerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rlv = (RecyclerView) findViewById(R.id.rlv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rlv.setLayoutManager(linearLayoutManager);
//        rlv.addItemDecoration(new DividerItemDecoration(this));

        ArrayList<Data> data = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Data d = new Data();
            d.type = i % 3;
            d.content = "这是测试 " + String.valueOf(i);
            data.add(d);
        }

        MainAdapter mainAdapter = new MainAdapter(data);
        rlv.setAdapter(mainAdapter);
    }

    class Data {
        int type;
        String content;
    }

    private static class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        static final int NO_MENU = 0;
        static final int SIMPLE_MENU = 1;
        static final int SINGLE_DELETE_MENU = 2;

        ArrayList<Data> data;

        MainAdapter(ArrayList<Data> data) {
            this.data = data;
        }

        @Override
        public int getItemViewType(int position) {
            return data.get(position).type;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case SIMPLE_MENU:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_item, parent, false);
                    return new SimpleHolder(view);
                case SINGLE_DELETE_MENU:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_item2, parent, false);
                    return new SingleDeleteHolder(view);
                case NO_MENU:
                default:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content, parent, false);
                    return new BaseHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            BaseHolder baseHolder = (BaseHolder) holder;
            baseHolder.item_content.setText(data.get(holder.getAdapterPosition()).content);
            baseHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), data.get(holder.getAdapterPosition()).content, Toast.LENGTH_SHORT).show();
                }
            });

            int itemViewType = getItemViewType(position);
            switch (itemViewType) {
                case SIMPLE_MENU:
                    SimpleHolder simpleHolder = (SimpleHolder) holder;
                    simpleHolder.negative.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(v.getContext(), "删除", Toast.LENGTH_SHORT).show();
                            data.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                        }
                    });
                    simpleHolder.positive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(v.getContext(), "确定", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case SINGLE_DELETE_MENU:
                    SingleDeleteHolder singleDeleteHolder = (SingleDeleteHolder) holder;
                    singleDeleteHolder.negative.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(v.getContext(), "删除", Toast.LENGTH_SHORT).show();
                            data.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                        }
                    });
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private static class BaseHolder extends RecyclerView.ViewHolder {

        TextView item_content;

        BaseHolder(View itemView) {
            super(itemView);
            item_content = (TextView) itemView.findViewById(R.id.item_content);
        }
    }

    private static class SimpleHolder extends BaseHolder {
        private final View negative;
        private final View positive;

        SimpleHolder(final View itemView) {
            super(itemView);
            negative = itemView.findViewById(R.id.negative);
            positive = itemView.findViewById(R.id.positive);
        }
    }

    private static class SingleDeleteHolder extends BaseHolder {

        private final View negative;

        SingleDeleteHolder(View itemView) {
            super(itemView);
            negative = itemView.findViewById(R.id.negative);
        }
    }
}
