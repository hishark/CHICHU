package com.example.myapplication.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by yiya on 2017/12/5.
 */

public class Comment extends BmobObject {
    private MyUser user;//评论的用户,指向User表,一对一的关系该comment属于某个用户User
    private String commentContent;//评论内容
    private Post post;//评论对应的帖子  一条评论只能属于某一篇帖子，一篇帖子可以有很多用户对其进行评论，那么帖子和评论之间的关系就是一对多关系

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }
}
