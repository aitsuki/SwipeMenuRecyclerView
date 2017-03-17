package com.aitsuki.swipedemo.util;

import android.annotation.SuppressLint;
import android.app.Application;
import android.widget.Toast;

/**
 * Created by AItsuki on 2017/3/17.
 */
public class ToastUtil {

    private static Toast sToast;

    @SuppressLint("ShowToast")
    public static void init(Application context) {
        sToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    public static void show(String message) {
        sToast.setText(message);
        sToast.show();
    }
}
