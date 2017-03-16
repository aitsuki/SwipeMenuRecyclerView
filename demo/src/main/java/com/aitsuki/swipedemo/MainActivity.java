package com.aitsuki.swipedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                Toast.makeText(MainActivity.this, "1", Toast.LENGTH_SHORT).show();
            }
        });

        tv_cancel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "取消 1", Toast.LENGTH_SHORT).show();
            }
        });

        tv_confirm1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "确定 1", Toast.LENGTH_SHORT).show();
            }
        });

        sil2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "2", Toast.LENGTH_SHORT).show();
            }
        });

        tv_confirm2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "确定 2", Toast.LENGTH_SHORT).show();
            }
        });

        sil3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "3", Toast.LENGTH_SHORT).show();
            }
        });

        tv_cancel3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "取消 3", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
