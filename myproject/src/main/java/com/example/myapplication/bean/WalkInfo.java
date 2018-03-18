package com.example.myapplication.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by 777 on 2017/12/4.
 */

public class WalkInfo extends BmobObject {
    //指向MyUser表,一对一的关系该WalkInfo属于某个用户User
    private MyUser user;
    private Integer WalkSteps;
    private Double WalkDistance;
    private Integer WalkTotalNum;

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }

    public Integer getWalkSteps() {
        return WalkSteps;
    }

    public void setWalkSteps(Integer walkSteps) {
        WalkSteps = walkSteps;
    }

    public Double getWalkDistance() {
        return WalkDistance;
    }

    public void setWalkDistance(Double walkDistance) {
        WalkDistance = walkDistance;
    }

    public Integer getWalkTotalNum() {
        return WalkTotalNum;
    }

    public void setWalkTotalNum(Integer walkTotalNum) {
        WalkTotalNum = walkTotalNum;
    }
}
