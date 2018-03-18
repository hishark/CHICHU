package com.example.myapplication.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.example.myapplication.R;
import com.example.myapplication.bean.MyUser;
import com.example.myapplication.map.DynamicDemo;

import com.mob.mobapi.API;
import com.mob.mobapi.APICallback;
import com.mob.mobapi.MobAPI;
import com.mob.mobapi.apis.Mobile;
import com.mob.mobapi.apis.Weather;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.bmob.v3.BmobUser;

public class ChooseMapOrNotActivity extends AppCompatActivity {

    //---------地图用到的一堆-------
    private SDKReceiver mReceiver;

    //------天气预报-----
    TextView tv_weather,tv_temp,tv_air,tv_sport;
    ImageView img_weather;
    //-------------------


    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();

            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                Log.v("apikey","apikey验证失败，地图功能无法正常使用");
                //Toast.makeText(ChooseMapOrNotActivity.this,"apikey验证失败，地图功能无法正常使用",Toast.LENGTH_SHORT).show();
            } else if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                Log.v("apikey","apikey验证成功");
                //Toast.makeText(ChooseMapOrNotActivity.this,"apikey验证成功",Toast.LENGTH_SHORT).show();
            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                Log.v("apikey","网络错误");
                //Toast.makeText(ChooseMapOrNotActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //-------地图地图地图---------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
    //--------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_map_or_not);
        getSupportActionBar().hide();

        //-------百度地图地图------
        // apikey的授权需要一定的时间，在授权成功之前地图相关操作会出现异常；apikey授权成功后会发送广播通知，我们这里注册 SDK 广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);

        Button btBack=(Button)findViewById(R.id.bt_choose_back);
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.animator.in_from_right, R.animator.out_to_left);
            }
        });



        Button mapbutton = (Button) findViewById(R.id.bt_map);
        mapbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseMapOrNotActivity.this, DynamicDemo.class);
                ChooseMapOrNotActivity.this.startActivity(intent);

            }
        });


        Button notmapbutton = (Button) findViewById(R.id.bt_notmap);
        notmapbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseMapOrNotActivity.this, StepCounterActivity.class);
                ChooseMapOrNotActivity.this.startActivity(intent);
                overridePendingTransition(R.animator.in_from_left, R.animator.out_to_right);

            }
        });


        //获取当前用户的地区，用来去查天气
        String cityname ;
        MyUser currentuser = BmobUser.getCurrentUser(MyUser.class);
        cityname = currentuser.getRegion();


        //---------通过MobAPI获取天气预报---------
        Weather api = (Weather) MobAPI.getAPI(Weather.NAME);
        //tv_date = (TextView)this.findViewById(R.id.tv_weather_date);
        tv_weather = (TextView)this.findViewById(R.id.tv_weather_weather);
        tv_temp = (TextView)this.findViewById(R.id.tv_weather_temp);
        tv_air = (TextView)this.findViewById(R.id.tv_weather_air);
        tv_sport = (TextView)this.findViewById(R.id.tv_weather_sport);
        img_weather = (ImageView)this.findViewById(R.id.img_weather);
        api.queryByCityName(cityname,new APICallback() {
            @Override
            public void onSuccess(API api, int i, Map<String, Object> map) {
                switch (i) {

                    case Weather.ACTION_QUERY:
                        ArrayList<HashMap<String, Object>> results = (ArrayList<HashMap<String, Object>>) map.get("result");
                        HashMap<String, Object> WeatherMap = results.get(0);
                        String weather = WeatherMap.get("weather").toString();
                        String temp = WeatherMap.get("temperature").toString();
                        String air = WeatherMap.get("airCondition").toString();
                        String sport = WeatherMap.get("exerciseIndex").toString();
                        tv_weather.setText(weather);
                        tv_temp.setText(temp);
                        tv_air.setText(air);
                        tv_sport.setText(sport);
                        //"多云,少云,晴,阴,小雨,雨,雷阵雨,中雨,阵雨,零散阵雨,零散雷雨,小雪,雨夹雪,阵雪,霾",
                        switch (weather){
                            case "多云":
                                img_weather.setImageResource(R.drawable.duoyun);
                                break;
                            case "少云":
                                img_weather.setImageResource(R.drawable.shaoyun);
                                break;
                            case "晴":
                                img_weather.setImageResource(R.drawable.qing);
                                break;
                            case "阴":
                                img_weather.setImageResource(R.drawable.yin);
                                break;
                            case "小雨":
                                img_weather.setImageResource(R.drawable.xiaoyu);
                                break;
                            case "雨":
                                img_weather.setImageResource(R.drawable.yu);
                                break;
                            case "雷阵雨":
                                img_weather.setImageResource(R.drawable.leizhenyu);
                                break;
                            case "中雨":
                                img_weather.setImageResource(R.drawable.zhongyu);
                                break;
                            case "阵雨":
                                img_weather.setImageResource(R.drawable.zhenyu);
                                break;
                            case "零散阵雨":
                                img_weather.setImageResource(R.drawable.zhenyu);
                                break;
                            case "零散雷雨":
                                img_weather.setImageResource(R.drawable.leizhenyu);
                                break;
                            case "小雪":
                                img_weather.setImageResource(R.drawable.xiaoxue);
                                break;
                            case "雨夹雪":
                                img_weather.setImageResource(R.drawable.yujiaxue);
                                break;
                            case "阵雪":
                                img_weather.setImageResource(R.drawable.zhenxue);
                                break;
                            case "霾":
                                img_weather.setImageResource(R.drawable.mai);
                                break;
                        }

                }
            }

            @Override
            public void onError(API api, int i, Throwable throwable) {
                Toast.makeText(ChooseMapOrNotActivity.this,"error", Toast.LENGTH_SHORT).show();

            }
        });

    }
}
