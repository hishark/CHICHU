package com.example.myapplication.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.bean.Comment;
import com.example.myapplication.bean.MyUser;
import com.example.myapplication.utils.ImageLoader;

import java.util.List;

/**
 * Created by 程洁 on 2017/12/5.
 */

public class CommentAdapter extends BaseAdapter {
    //private static  String TAG="CommentAdapter2";
    private Context mContext;
    private List<Comment> dataList;
    private ImageLoader mImageLoader;
    public CommentAdapter(Context mContext, List<Comment> dataList) {
        this.mContext = mContext;
        this.dataList = dataList;
        mImageLoader=new ImageLoader();
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int i) {
        return dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        CommentViewHolder commentViewHolder;
        if (view==null){
            commentViewHolder=new CommentViewHolder();
            view=View.inflate(mContext, R.layout.comment_item, null);
            commentViewHolder.userLogo=(ImageView)view.findViewById(R.id.user_logo2);
            commentViewHolder.userName=(TextView)view.findViewById(R.id.item_user_name);
            commentViewHolder.commentContent=(TextView)view.findViewById(R.id.item_content_comment);
            commentViewHolder.date=(TextView)view.findViewById(R.id.item_public_time2);
            commentViewHolder.index=(TextView)view.findViewById(R.id.item_index_comment);
            view.setTag(commentViewHolder);
        }else {
            commentViewHolder=(CommentViewHolder)view.getTag();
        }

        final Comment comment=dataList.get(position);
        //if (comment.getUser()!=null){
            commentViewHolder.userName.setText(comment.getUser().getUsername());
        //}/*else {
            //commentViewHolder.userName.setText("墙友");
       // }*/
        MyUser user=comment.getUser();

        String url=user.getAvatarUrl();
        if(user.getAvatarUrl()==null){
            commentViewHolder.userLogo.setImageResource(R.drawable.avatar1);
        }
        else {
            commentViewHolder.userLogo.setTag(url);
            mImageLoader.showImageByAsyncTask(commentViewHolder.userLogo, url);
        }
        commentViewHolder.index.setText((position+1)+"楼");
        commentViewHolder.date.setText(comment.getCreatedAt());
        commentViewHolder.commentContent.setText(comment.getCommentContent());
        return view;
    }

    private class CommentViewHolder {
        public ImageView userLogo;
        public TextView userName;
        public TextView commentContent;
        public TextView date;
        public  TextView index;
    }
}
