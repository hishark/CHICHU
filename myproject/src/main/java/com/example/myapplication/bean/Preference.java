package com.example.myapplication.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by 777 on 2017/12/13.
 */

public class Preference extends BmobObject{
    private MyUser user;
    private boolean ShowDistance;
    private boolean ShowSteps;

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }

    public boolean isShowDistance() {
        return ShowDistance;
    }

    public void setShowDistance(boolean showDistance) {
        ShowDistance = showDistance;
    }

    public boolean isShowSteps() {
        return ShowSteps;
    }

    public void setShowSteps(boolean showSteps) {
        ShowSteps = showSteps;
    }
}
