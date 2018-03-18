package com.example.myapplication.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activity.CommentActivity;
import com.example.myapplication.bean.Loves;
import com.example.myapplication.bean.MyUser;
import com.example.myapplication.bean.Post;
import com.example.myapplication.database.DatabaseUtil;
import com.example.myapplication.utils.BitmapUtil;
import com.example.myapplication.utils.ImageLoader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 继承RecyclerView，实现ListView的效果
 * RecyclerView中google强烈要求使用ViewHolder
 * Created by 程洁 on 2017/12/4.
 */

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder>{

    private Context mContext;
    private List<Post> dataList;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;


    //构造方法的实现
    public PostAdapter(Context mContext, List<Post> dataList) {
        this.mContext = mContext;
        this.dataList = dataList;
        mInflater = LayoutInflater.from(mContext);
        mImageLoader=new ImageLoader();//不这样每次都会新建一个LruCache


    }

    /**
     * 创建MyViewHolder
     *
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = mInflater.inflate(R.layout.post_item, parent, false);
        MyViewHolder holder = new MyViewHolder(v);
        return holder;
    }


    /*
    * 绑定MyViewHolder类，就是给控件赋值
    *
    */
    @Override
    public void onBindViewHolder(final PostAdapter.MyViewHolder viewHolder, int position) {
        final Post entity = dataList.get(position);
        final MyUser user = entity.getAuthor();
        String avatarUrl = null;
        if (user.getAvatarUrl() != null) {
            avatarUrl = user.getAvatarUrl();
            viewHolder.userLogo.setTag(avatarUrl);
            //TODO 使用LruCache缓存机制来获取图片
            mImageLoader.showImageByAsyncTask(viewHolder.userLogo,avatarUrl);
        }
        else{
            viewHolder.userLogo.setImageResource(R.drawable.avatar1);
        }
        viewHolder.userName.setText(user.getUsername());
        viewHolder.contentText.setText(entity.getContent());
        viewHolder.date.setText(entity.getCreatedAt());

        /*
        //没有使用缓存是的写法
         new ImageLoader().showImageByAsyncTask(viewHolder.userLogo,
         avatarUrl);*/

        //使用Thread下载图片
        /*new ImageLoader().showImageByThread(viewHolder.userLogo,
                avatarUrl);*/

        //点赞
        viewHolder.love.setText(entity.getLove() + "");
        if (entity.isMyLove()) {
            viewHolder.love.setTextColor(Color.parseColor("#D95555"));
        } else {
            viewHolder.love.setTextColor(Color.parseColor("#000000"));
        }



        final MyUser currentUser= BmobUser.getCurrentUser(MyUser.class);
        final Loves loved=new Loves();
        viewHolder.love.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                if(entity.isMyLove()){
                    Toast.makeText(mContext,"您已经赞过啦~",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (DatabaseUtil.getInstance(mContext).isLoved(entity)) {//当前用户赞过post
                    Toast.makeText(mContext,"您已经赞过啦~",Toast.LENGTH_SHORT).show();
                    return;
                }

                //saveLove();
                publishLove();

            }

            private void publishLove() {
                entity.setLove(entity.getLove() + 1);
                viewHolder.love.setTextColor(Color.parseColor("#D95555"));
                viewHolder.love.setText(entity.getLove() + "");
                entity.increment("love", 1);//点赞数递增1
                //entity.setMyLove(true);//表示该用户已经点赞过该帖子
                entity.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if(e==null){
                            entity.setMyLove(true);
                            Toast.makeText(mContext,"点赞成功",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(mContext,"点赞失败^-^",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            private void saveLove() {
                loved.setUser(currentUser);
                loved.setPost(entity);
                loved.setLoved(true);
                loved.save(new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        if(e==null){
                            //Toast.makeText(mContext,"3~",Toast.LENGTH_SHORT).show();
                        }else{
                            //Toast.makeText(mContext,"4",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        });


        //聊聊
       /* viewHolder.chat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                MyUser currentUser=BmobUser.getCurrentUser(MyUser.class);
                if (currentUser.getUsername().equals(entity.getAuthor().getUsername())){//自己不能和自己对话{
                        Toast.makeText(mContext,"自己不能和自己聊天",Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent=new Intent();
                    intent.setClass(PostAdapter.this.mContext,ChatActivity.class);
                    intent.putExtra("data", entity.getAuthor());
                    mContext.startActivity(intent);
                }

            }
        });*/

        //评论
        viewHolder.comment.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setClass(PostAdapter.this.mContext,CommentActivity.class);
                intent.putExtra("data", entity);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (dataList != null)
            return dataList.size();
        return 0;
    }



    /**
     * MyViewHolder类，这个类的作用主要用于实例化控件
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView userLogo;
        public TextView userName;
        public TextView contentText;
        public TextView chat;
        public TextView love;
        public TextView date;
        public TextView comment;

        public MyViewHolder(View itemView) {
            super(itemView);
            userName = (TextView) itemView.findViewById(R.id.user_name);
            userLogo = (ImageView) itemView.findViewById(R.id.user_logo);
            contentText = (TextView) itemView.findViewById(R.id.content_text);
            //chat=(TextView)itemView.findViewById(R.id.item_action_chat);
            love = (TextView) itemView.findViewById(R.id.item_action_love);
            date = (TextView) itemView.findViewById(R.id.item_public_time);
            comment = (TextView) itemView.findViewById(R.id.item_action_comment);
        }
    }
}



