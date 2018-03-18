package com.example.myapplication.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by 777 on 2017/12/5.
 */

public class WalkGoal extends BmobObject{
    //指向MyUser表,一对一的关系该WalkInfo属于某个用户User
    private MyUser user;
    private Integer GoalNum;

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }

    public Integer getGoalNum() {
        return GoalNum;
    }

    public void setGoalNum(Integer goalNum) {
        GoalNum = goalNum;
    }
}
