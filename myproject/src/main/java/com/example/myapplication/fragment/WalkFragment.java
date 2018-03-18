package com.example.myapplication.fragment;
import android.annotation.TargetApi;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
//import android.widget.TextView;

import com.example.myapplication.R;


import com.example.myapplication.activity.ChooseMapOrNotActivity;
import com.example.myapplication.bean.MyUser;
import com.example.myapplication.bean.Preference;
import com.example.myapplication.bean.StepEntity;
import com.example.myapplication.database.DBOpenHelper;
import com.example.myapplication.database.StepDataDao;
import com.example.myapplication.ui.CircleBar;
import com.example.myapplication.utils.TimeUtil;

import java.text.DecimalFormat;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


public class WalkFragment extends Fragment {
    private View view;
    private CircleBar circleBar;
    private StepDataDao stepDataDao;
    private DBOpenHelper stepHelper;
    private SQLiteDatabase stepDb;

    TextView tv_Circle_Show;

    //当前日期
    private static String CURRENT_DATE;


    //用户设置偏好需要用到的变量
    Boolean DistanceIsChecked = false;
    Boolean StepsIsChecked = false;


    StepEntity entity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_walk, container, false);
        tv_Circle_Show=(TextView)view.findViewById(R.id.tv_circle_show);


        //获取当前时间
        CURRENT_DATE = TimeUtil.getCurrentDate();
        //获取本地数据库
        stepDataDao = new StepDataDao(WalkFragment.this.getContext());

        Log.v("hello","hello1");
        //本地数据库不为空再把数据设置显示到圈圈里！
        if(stepDataDao.getCurDataByDate(CURRENT_DATE)!=null){
            //从本地数据库中获取当天的数据
            entity = stepDataDao.getCurDataByDate(CURRENT_DATE);
            String steps=entity.getSteps()+"步";

            //从Bmob的Preference表中获取到用户设置的偏好,并显示在界面中的绿色圆饼中
            getUserPreference(entity.getSteps());

            //tv_Circle_Show.setText(steps);
        }else{
            Log.v("hello","hello2");
        }


        Button bt=(Button)view.findViewById(R.id.bt_startWalk);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //点击开始按钮进入实时记录界面
                ///2.0--点击按钮进入地图选择界面
                Intent intent=new Intent(WalkFragment.this.getContext(), ChooseMapOrNotActivity.class);
                getContext().startActivity(intent);
                getActivity().overridePendingTransition(R.animator.in_from_left, R.animator.out_to_right);


            }
        });

        return view;
    }

    private void getUserPreference(final String steps) {
        MyUser currentuser2=BmobUser.getCurrentUser(MyUser.class);
        BmobQuery<Preference> query = new BmobQuery<Preference>();

        //查询Preference表中user为currentuser2的数据记录。
        query.addWhereEqualTo("user",currentuser2);

        //执行查询方法
        query.findObjects(new FindListener<Preference>() {
            @Override
            public void done(List<Preference> list, BmobException e) {
                if(e==null){
                    //偏好表里存在当前用户，就取出两个布尔值进行判断

                    if(list.get(0).isShowSteps()){
                        //如果用户选择展示步数就显示步数
                        tv_Circle_Show.setText(steps+"步");
                    }else if(list.get(0).isShowDistance()){
                        //如果用户选择展示距离就显示距离
                        String distance;
                        distance=countDistance(steps);
                        tv_Circle_Show.setText(distance+"公里");
                    }

                }else{
                    //偏好表里不存在当前用户，就说明用户没有进行设置，那么默认显示步数！
                    tv_Circle_Show.setText(steps+"步");
                }
            }
        });
    }


    private String countDistance(String steps) {
        double distance;
        int step=Integer.parseInt(steps);
        if (step % 2 == 0) {
            distance = (step / 2) * 3 * 70 * 0.01;
        } else {
            distance = ((step / 2) * 3 + 1) * 70 * 0.01;
        }
        distance = distance / 1000;
        return formatDouble(distance);
    }


    /**
     * 计算并格式化doubles数值，保留两位有效数字
     *
     * @param doubles
     * @return 返回当前路程
     */
    @TargetApi(24)
    private String formatDouble(Double doubles) {
        DecimalFormat format = new DecimalFormat("####.##");
        String distanceStr = format.format(doubles);
        return distanceStr.equals("0") ? "0.0"
                : distanceStr;
    }


    @Override
    public void onResume() {
        super.onResume();
        if(stepDataDao.getCurDataByDate(CURRENT_DATE)!=null){
            //从本地数据库中获取当天的数据
            entity = stepDataDao.getCurDataByDate(CURRENT_DATE);

            //从Bmob的Preference表中获取到用户设置的偏好,并显示在界面中的绿色圆饼中
            getUserPreference(entity.getSteps());

        }else{

        }

    }
}