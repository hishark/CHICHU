package com.example.myapplication.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by yiya on 2017/12/8.
 */

public class Loves extends BmobObject {
    private MyUser user;//点赞的用户
    private Post post;//点赞对应的帖子  一个赞只能属于某一篇帖子，一篇帖子可以有很多用户对其进行点赞，那么帖子和赞之间的关系就是一对多关系
    private boolean loved;

    public boolean isLoved() {
        return loved;
    }

    public void setLoved(boolean loved) {
        this.loved = loved;
    }

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
