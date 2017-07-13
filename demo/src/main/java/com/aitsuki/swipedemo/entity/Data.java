package com.aitsuki.swipedemo.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by AItsuki on 2017/3/23.
 * Data
 */
public class Data implements MultiItemEntity {
    public int type;
    public String content;

    @Override
    public int getItemType() {
        return type;
    }
}
