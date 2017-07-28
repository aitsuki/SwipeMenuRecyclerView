package com.aitsuki.swipedemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.aitsuki.swipedemo.util.ToastUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToastUtil.init(getApplication());
    }


    public void onNativeAdapterClick(View view) {
        Intent callingIntent = NativeRecyclerViewAdapterActivity.getCallingIntent(this);
        startActivity(callingIntent);
    }

    public void onBaseRecyclerViewAdapterHelperClick(View view) {
        Intent callingIntent = CymChadActivity.getCallingIntent(this);
        startActivity(callingIntent);
    }
}
