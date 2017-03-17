package com.aitsuki.swipedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.aitsuki.swipedemo.util.ToastUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToastUtil.init(getApplication());
        View sil1 = findViewById(R.id.sil1);
        View tv_cancel1 = findViewById(R.id.tv_cancel1);
        View tv_confirm1 = findViewById(R.id.tv_confirm1);
        View sil2 = findViewById(R.id.sil2);
        View tv_confirm2 = findViewById(R.id.tv_confirm2);
        View sil3 = findViewById(R.id.sil3);
        View tv_cancel3 = findViewById(R.id.tv_cancel3);


        sil1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.show("1");
            }
        });

        tv_cancel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.show("取消 1");
            }
        });

        tv_confirm1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.show("确定 1");
            }
        });

        sil2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.show("2");
            }
        });

        tv_confirm2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.show("确定 2");
            }
        });

        sil3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.show("3");
            }
        });

        tv_cancel3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.show("取消 3");
            }
        });
    }
}
