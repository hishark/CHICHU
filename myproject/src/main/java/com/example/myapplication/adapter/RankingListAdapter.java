package com.example.myapplication.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.utils.BitmapUtil;
import com.example.myapplication.utils.ImageLoader;

import java.util.ArrayList;

/**
 * Created by 777 on 2017/12/5.
 */

public class RankingListAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> UserNameList;
    ArrayList<String> UserStepsList;
    ArrayList<String> UserAvatarsList;
    private ImageLoader mImageLoader;
    public RankingListAdapter(Context applicationContext, ArrayList<String> userNameList, ArrayList<String> userStepsList, ArrayList<String> userAvatarsList) {
        this.context=applicationContext;
        this.UserNameList=userNameList;
        this.UserStepsList=userStepsList;
        this.UserAvatarsList=userAvatarsList;
        mImageLoader=new ImageLoader();
    }

    @Override
    public int getCount() {
        return UserNameList.size();
    }

    @Override
    public Object getItem(int i) {
        return UserNameList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LinearLayout ll=(LinearLayout)view.inflate(context, R.layout.rankinglist_item,null);

        final ImageView avatar=(ImageView)ll.findViewById(R.id.avatar_rankList);

        if(UserAvatarsList.get(i)==null){
            avatar.setImageResource(R.drawable.avatar1);
        }
        else{
            avatar.setTag(UserAvatarsList.get(i));
            //使用LruCache缓存机制来获取图片
            mImageLoader.showImageByAsyncTask(avatar,
                    UserAvatarsList.get(i));
            /*new AsyncTask<String, String, Bitmap>() {
                @Override
                protected Bitmap doInBackground(String... arg0) {
                    String url=arg0[0];
                    Bitmap bm= BitmapUtil.getPicture(url);
                    return bm;

                }

                @Override
                protected void onPostExecute(Bitmap result) {
                    avatar.setImageBitmap(result);
                }
            }.execute(UserAvatarsList.get(i));*/
        }


        TextView tv_userRank=(TextView)ll.findViewById(R.id.tv_userRank);
        tv_userRank.setText(String.valueOf(i+1));
        tv_userRank.setTextColor(Color.parseColor("#808080"));

        TextView tv_rankUserName=(TextView)ll.findViewById(R.id.tv_rankUserName);
        tv_rankUserName.setText(UserNameList.get(i));
        tv_rankUserName.setTextColor(Color.parseColor("#808080"));

        TextView tv_rankUserSteps=(TextView)ll.findViewById(R.id.tv_rankUserSteps);
        tv_rankUserSteps.setText(UserStepsList.get(i));
        tv_rankUserSteps.setTextColor(Color.parseColor("#808080"));

        return ll;
    }
}
