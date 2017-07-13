package com.aitsuki.swipedemo.data;

import com.aitsuki.swipedemo.entity.Data;
import com.aitsuki.swipedemo.entity.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AItsuki on 2017/3/23.
 *
 */
public class Repository {

    public List<Data> fakeDate() {
        List<Data> dataList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Data data = new Data();
            data.type = i % 7;
            switch (data.type) {
                case Type.LEFT_MENU:
                    data.content = "LEFT_MENU";
                    break;
                case Type.RIGHT_MENU:
                    data.content = "RIGHT_MENU";
                    break;
                case Type.LEFT_AND_RIGHT_MENU:
                    data.content = "LEFT_AND_RIGHT_MENU";
                    break;
                case Type.LEFT_LONG_MENU:
                    data.content = "LEFT_LONG_MENU";
                    break;
                case Type.RIGHT_LONG_MENU:
                    data.content = "RIGHT_LONG_MENU";
                    break;
                case Type.LEFT_AND_RIGHT_LONG_MENU:
                    data.content = "LEFT_AND_RIGHT_LONG_MENU";
                    break;
                case Type.DISABLE_SWIPE_MENU:
                    data.content = "DISABLE_SWIPE_MENU";
                    break;
                default:
                    data.content = "DEFAULT";
                    break;
            }
            dataList.add(data);
        }
        return dataList;
    }
}
