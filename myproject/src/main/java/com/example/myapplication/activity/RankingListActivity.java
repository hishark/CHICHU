package com.example.myapplication.activity;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.adapter.RankingListAdapter;
import com.example.myapplication.bean.MyUser;
import com.example.myapplication.bean.WalkInfo;
import com.example.myapplication.utils.BitmapUtil;
import com.example.myapplication.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class RankingListActivity extends AppCompatActivity {
    //1.
    CircleImageView CurrentUserAvatar;
    ListView lv_RankingList;
    TextView tv_CurrentUserName,tv_CurrentUserRank;
    Button bt_back;
    ArrayList<String> UserNameList=new ArrayList<String>();
    ArrayList<String> UserStepsList=new ArrayList<String>();
    ArrayList<String> UserAvatarsList=new ArrayList<String>();
    private ImageLoader rankImageLoader=new ImageLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking_list);
        getSupportActionBar().hide();


        //2.初始化控件
        initView();


        //3.从Bmob的WalkInfo表中取出所有数据
        //MyUser currentuser= BmobUser.getCurrentUser(MyUser.class);
        BmobQuery<WalkInfo> query = new BmobQuery<WalkInfo>();
        //查询行走信息的同时要查询出用户的信息，一定不能少了这句include
        query.include("user");
        query.order("-WalkSteps");
        query.findObjects(new FindListener<WalkInfo>() {
            @Override
            public void done(List<WalkInfo> list, BmobException e) {
                if(e==null){
                    for(int i=0;i<list.size();i++){
                        UserNameList.add(list.get(i).getUser().getUsername());
                    }
                    for(int i=0;i<list.size();i++){
                        UserStepsList.add(list.get(i).getWalkSteps().toString());
                    }
                    for(int i=0;i<list.size();i++){
                        UserAvatarsList.add(list.get(i).getUser().getAvatarUrl());
                    }
                    RankingListAdapter rla=new RankingListAdapter(getApplicationContext(),UserNameList,UserStepsList,UserAvatarsList);
                    lv_RankingList.setAdapter(rla);
                    lv_RankingList.setVerticalScrollBarEnabled(false);

                    //把当前用户的姓名和排名设置到排行榜顶部显示
                    setRankTop(list);
                }else{
                    Toast.makeText(RankingListActivity.this, "fail", Toast.LENGTH_SHORT).show();
                }
            }
        });


        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.animator.in_from_bottom, R.animator.out_to_top);
            }
        });


    }

    private void setRankTop(List<WalkInfo> list) {
        MyUser currentuser=BmobUser.getCurrentUser(MyUser.class);
        String url=currentuser.getAvatarUrl();


        if (url == null) {
            CurrentUserAvatar.setImageResource(R.drawable.avatar1);
        } else {
            CurrentUserAvatar.setTag(url);
            rankImageLoader.showImageByAsyncTask(CurrentUserAvatar, url);
        }
        /*//这个是被抛弃的方法哈哈哈 留着留着
        MyTask task=new MyTask();
        task.execute(url);*/
        tv_CurrentUserName.setText(currentuser.getUsername());
        for(int i=0;i<list.size();i++){
            if(list.get(i).getUser().getUsername().equals(currentuser.getUsername())){
                tv_CurrentUserRank.setText("第"+String.valueOf(i+1)+"名");
            }
        }
    }
    class MyTask extends AsyncTask<String, String, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            String url=arg0[0];
            Bitmap bm= BitmapUtil.getPicture(url);
            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO Auto-generated method stub
            CurrentUserAvatar.setImageBitmap(result);
        }
    }

    private void initView() {
        CurrentUserAvatar=(CircleImageView)this.findViewById(R.id.rank_CurrentUser_avatar);
        lv_RankingList=(ListView)this.findViewById(R.id.lv_rankinglist);
        tv_CurrentUserName=(TextView)this.findViewById(R.id.rank_CurrentUser_Name);
        tv_CurrentUserRank=(TextView)this.findViewById(R.id.rank_CurrentUser_ranking);
        bt_back = (Button)this.findViewById(R.id.bt_rank_back);
    }

}
