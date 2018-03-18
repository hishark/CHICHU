package com.example.myapplication.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MenuItem;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.example.myapplication.database.DBOpenHelper;
import com.example.myapplication.database.StepDataDao;
import com.example.myapplication.fragment.CommunicationFragment;
import com.example.myapplication.R;
import com.example.myapplication.fragment.RecordFragment;
import com.example.myapplication.fragment.SettingFragment;
import com.example.myapplication.fragment.WalkFragment;
import com.example.myapplication.bean.MyUser;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;


public class MainActivity extends AppCompatActivity {

    //四个大大的碎片
    WalkFragment walkFragment = new WalkFragment();
    RecordFragment recordFragment = new RecordFragment();
    CommunicationFragment communicationFragment = new CommunicationFragment();
    SettingFragment settingFragment = new SettingFragment();

    //日历的宽高
    public static int screenWidth, screenHeight;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        getSupportActionBar().hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Bmob初始化
        Bmob.initialize(this, "008409ccab8a8f15d110552bcb2b57e3");

        //底部导航的初始化以及监听事件的设置
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //设置默认显示的碎片
        setFirstFragment();

        //得到屏幕的宽高！日历的控件要用到这个数据！
        getWindowSize();



    }
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    replaceFragment(walkFragment);
                    return true;

                case R.id.navigation_dashboard:
                    MyUser user_record = BmobUser.getCurrentUser(MyUser.class);
                    replaceFragment(recordFragment);
                    if(user_record==null){
                        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);
                    }else{
                        //Toast.makeText(MainActivity.this, "用户已登录", Toast.LENGTH_SHORT).show();
                    }
                    return true;

                case R.id.navigation_notifications:
                    MyUser user2 = BmobUser.getCurrentUser(MyUser.class);
                    replaceFragment(communicationFragment);
                    if(user2==null){
                        Toast.makeText(MainActivity.this, "用户未登录", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);

                    }else{
                        //Toast.makeText(MainActivity.this, "你可以愉快的发帖啦~", Toast.LENGTH_SHORT).show();
                    }

                    return true;

                case R.id.navigation_user:
                    MyUser user1 = BmobUser.getCurrentUser(MyUser.class);
                    replaceFragment(settingFragment);
                    /*if(user1==null){
                        Toast.makeText(MainActivity.this, "用户未登录", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);

                    }else{

                        Toast.makeText(MainActivity.this, "用户已登录", Toast.LENGTH_SHORT).show();
                    }*/
                    return true;
            }
            return false;
        }
    };



    //动态添加碎片
    public void replaceFragment(Fragment fragment) {
        FragmentManager manager=getSupportFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.replace(R.id.content, fragment);
        //transaction.addToBackStack(null);
        transaction.commit();
    }

    //设置默认显示的碎片
    private void setFirstFragment() {
        replaceFragment(walkFragment);
    }


    //得到屏幕的宽高！日历的控件要用到这个数据！
    private void getWindowSize() {

        //获取屏幕宽高
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

    }





    //---------百度鹰眼轨迹-----------------------


}
