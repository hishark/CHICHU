package com.example.myapplication.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by yiya on 2017/12/3.
 */

public class Advice extends BmobObject {
    private String advices;
    public String getAdvices() {
        return advices;
    }

    public void setAdvices(String advices) {
        this.advices = advices;
    }

}
