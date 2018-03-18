package com.example.myapplication.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activity.SendPostActivity;
import com.example.myapplication.adapter.PostAdapter;
import com.example.myapplication.bean.MyUser;
import com.example.myapplication.bean.Post;
import com.example.myapplication.constant.Constant;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


/**
 * Created by 程洁 on 2017/11/26.
 */
public class CommunicationFragment extends Fragment {


    private View view;
    private MyUser user;
    //数据显示所用控件
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private PostAdapter adapter;

    private BmobQuery<Post> query;//用于查询数据private BmobQuery<Post> query;//用于查询数据
    private int pageNum=0;
    private Toolbar mToolBar;
    private FloatingActionButton sendPost;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_communication, container, false);

        initView();//初始化视图
        if(BmobUser.getCurrentUser(MyUser.class)!=null){
            initDates();

        }
        initSwipe();//下拉刷新

        sendPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),SendPostActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.animator.in_from_left, R.animator.out_to_right);
            }
        });
        return view;
    }

    private void initToolbar() {
        TextView comToolbar=(TextView)view.findViewById(R.id.tv_post_title);
        MyUser currentuser=BmobUser.getCurrentUser(MyUser.class);
        if (currentuser.getRegion()==null){
            return;
        }
        comToolbar.setText(currentuser.getRegion()+"彳亍圈");
    }


    private void initSwipe() {
        //设置刷新时动画的颜色
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //TODO 刷新的时候获取数据
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initDates(); //刷新
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });
    }

    private void initDates() {
        initToolbar();
        user= BmobUser.getCurrentUser(MyUser.class);
        query = new BmobQuery<Post>();
        BmobQuery<MyUser> innerQuery=new BmobQuery<MyUser>();
        innerQuery.addWhereEqualTo("region", user.getRegion());//帖子按地区呈现
        query.include("author");
        query.setLimit(Constant.NUMBERS_PER_PAGE);
        query.order("-createdAt");//根据createdAt字段降序显示数据
        query.addWhereMatchesQuery("author","_User",innerQuery);

        query.findObjects(new FindListener<Post>() {
            @Override
            public void done(List<Post> list, BmobException e) {
                if (e == null) {
                    if (list != null) {
                        initPosts(list);
                        pageNum++;
                        if (list.size() < Constant.NUMBERS_PER_PAGE)
                            //Toast.makeText(getContext(), "已加载完所有数据~", Toast.LENGTH_SHORT).show();
                            Log.i("CommunicationFragment","已加载完所有数据~");
                    } else {
                        pageNum--;
                        Log.i("CommunicationFragment","暂无更多数据~");
                        //Toast.makeText(getContext(), "暂无更多数据~", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    pageNum--;
                    Toast.makeText(getContext(), "请检查网络是否畅通~", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void initPosts(List<Post> list) {
        adapter = new PostAdapter(getContext(), list);
        mRecyclerView.setAdapter(adapter);

        //设置RecyclerView的布局管理
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        //设置RecyclerView的动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //设置监听事件

    }
    private void initView() {
        sendPost = (FloatingActionButton)view.findViewById(R.id.bt_send);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipeRefreshLayout);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.rv_posts);
    }

}
