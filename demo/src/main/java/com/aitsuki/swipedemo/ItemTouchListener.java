package com.aitsuki.swipedemo;

/**
 * Created by AItsuki on 2017/7/11.
 * ItemTouchListener
 */
interface ItemTouchListener {
    void onItemClick(String str);

    void onLeftMenuClick(String str);

    void onRightMenuClick(String str);
}
