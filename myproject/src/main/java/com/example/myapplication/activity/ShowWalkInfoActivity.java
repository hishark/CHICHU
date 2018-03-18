package com.example.myapplication.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.bean.MyUser;
import com.example.myapplication.bean.WalkInfo;
import com.example.myapplication.fragment.RecordFragment;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class ShowWalkInfoActivity extends AppCompatActivity {

    private String walksteps;
    private String walktime;
    private String walkdistance;
    private String walkcalories;
    private String walkvelocity;




    //布局中的一大堆控件
    TextView tv_walktime,tv_walksteps,tv_walkdistance,tv_walkvelocity,tv_calories,tv_curUserWalkTotalNum;
    Button bt_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_walk_info);
        getSupportActionBar().hide();

        initView();

        Intent intent=getIntent();
        walksteps = intent.getExtras().getString("WalkSteps")+"步";
        walktime = intent.getExtras().getString("WalkTime");
        walkdistance = intent.getExtras().getString("Distance")+"公里";
        walkcalories = intent.getExtras().getString("Calories")+"kcal";
        walkvelocity = intent.getExtras().getString("Velocity")+"m/s";


        tv_walksteps.setText(walksteps);
        tv_walktime.setText(walktime);
        tv_walkdistance.setText(walkdistance);
        tv_calories.setText(walkcalories);
        tv_walkvelocity.setText(walkvelocity);



        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                /**
                 * 重写方法实现平滑切换activity
                 * overridePendingTransition(in,out)方法
                 * 第一个参数是新activity进入的方式，第二个参数是当前activity退出的方式
                 */
                overridePendingTransition(R.animator.in_from_right, R.animator.out_to_left);
            }
        });

    }

    private void initView() {
        tv_walktime = (TextView)this.findViewById(R.id.showinfo_time);
        tv_walksteps = (TextView)this.findViewById(R.id.showinfo_steps);
        tv_walkdistance = (TextView)this.findViewById(R.id.showinfo_distance);
        tv_walkvelocity = (TextView)this.findViewById(R.id.showinfo_velocity);
        tv_calories = (TextView)this.findViewById(R.id.showinfo_calories);
        bt_back = (Button)this.findViewById(R.id.bt_showinfo_back);

    }



}
