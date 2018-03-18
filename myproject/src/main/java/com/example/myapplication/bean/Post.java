package com.example.myapplication.bean;


import android.widget.ImageView;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

/**
 * Created by yiya on 2017/12/3.
 */

public class Post extends BmobObject implements Serializable {
    private MyUser author;//帖子的发布者，一对一的关系，该post属于某个用户
    private String content;//帖子内容
    private Integer comment;//评论数
    private Integer love;//点赞数
    private boolean myLove;//赞



    public boolean isMyLove() {
        return myLove;
    }

    public void setMyLove(boolean myLove) {
        this.myLove = myLove;
    }


    public MyUser getAuthor() {
        return author;
    }

    public void setAuthor(MyUser author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }



    public Integer getComment() {
        return comment;
    }

    public void setComment(Integer comment) {
        this.comment = comment;
    }

    public Integer getLove() {
        return love;
    }

    public void setLove(Integer love) {
        this.love = love;
    }


}
