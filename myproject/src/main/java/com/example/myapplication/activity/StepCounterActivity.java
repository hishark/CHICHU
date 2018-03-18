package com.example.myapplication.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import java.text.DecimalFormat;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.bean.MyUser;
import com.example.myapplication.bean.Preference;
import com.example.myapplication.bean.WalkInfo;
import com.example.myapplication.fragment.SettingFragment;
import com.example.myapplication.service.StepCounterService;
import com.example.myapplication.utils.StepDetector;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class StepCounterActivity extends AppCompatActivity {
    //定义文本框控件
    private TextView tv_show_step;// 步数

    private TextView tv_show_timer;// 运行时间

    private Button bt_start;// 开始按钮
    private Button bt_stop;// 暂停按钮
    private Button bt_end;//结束按钮
    private Button bt_back;

    private long timer = 0;// 运动时间
    private  long startTimer = 0;// 开始时间

    private  long tempTime = 0;

    private Double distance = 0.0;// 路程：米
    private Double calories = 0.0;// 热量：卡路里
    private Double velocity = 0.0;// 速度：米每秒

    private int step_length = 0;  //步长
    private int weight = 0;       //体重
    private int total_step = 0;   //走的总步数

    private Thread thread;  //定义线程对象


    private TextView step_counter;



    // 当创建一个新的Handler实例时, 它会绑定到当前线程和消息的队列中,开始分发数据
    // Handler有两个作用, (1) : 定时执行Message和Runnalbe 对象
    // (2): 让一个动作,在不同的线程中执行.

    Handler handler = new Handler() {
        // Handler对象用于更新当前步数,定时发送消息，调用方法查询数据用于显示
        //主要接受子线程发送的数据, 并用此数据配合主线程更新UI
        //Handler运行在主线程中(UI线程中), 它与子线程可以通过Message对象来传递数据,
        //Handler就承担着接受子线程传过来的(子线程用sendMessage()方法传递Message对象，(里面包含数据)
        //把这些消息放入主线程队列中，配合主线程进行更新UI。

        @Override                  //这个方法是从父类/接口 继承过来的，需要重写一次
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);        // 此处可以更新UI

            countDistance();     //调用距离方法，看一下走了多远

            if (timer != 0 && distance != 0.0) {

                // 体重、距离
                // 跑步热量（kcal）＝体重（kg）×距离（公里）×1.036
                calories = weight * distance * 0.001;
                //速度velocity
                velocity = distance * 1000 / timer;
            } else {
                calories = 0.0;
                velocity = 0.0;
            }

            countStep();          //调用步数方法

            tv_show_step.setText(total_step + "");// 显示当前步数

            tv_show_timer.setText(getFormatTime(timer));// 显示当前运行时间



        }



    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.activity_current_walk);  //设置当前屏幕
        getSupportActionBar().hide();

        // 获取界面控件
        addView();

        // 初始化控件
        init();

        if (thread == null) {

            thread = new Thread() {// 子线程用于监听当前步数的变化

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    super.run();
                    int temp = 0;
                    while (true) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (StepCounterService.FLAG) {
                            Message msg = new Message();
                            if (temp != StepDetector.CURRENT_SETP) {
                                temp = StepDetector.CURRENT_SETP;
                            }
                            if (startTimer != System.currentTimeMillis()) {
                                timer = tempTime + System.currentTimeMillis()
                                        - startTimer;
                            }
                            handler.sendMessage(msg);// 通知主线程
                        }
                    }
                }
            };
            thread.start();
        }

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        Log.v("APP", "on resume.");

        //下面这两个方法放到oncreate去 不然切换到后台再回来就重置为0了
        // 获取界面控件
        //addView();

        // 初始化控件
        //init();

        //实例化一个intent用来启动实时计步服务
        final Intent service = new Intent(this, StepCounterService.class);

        //开始、暂停（继续）、结束的点击事件

        //开始按钮只要负责一个事情，点击就开启服务，然后之后就没他的事情啦
        //可是放在那很奇怪，要不就点击之后变成一个不可以点击的状态，而且要变颜色，用selector实现
        //点击开始之后，服务就开启了，然后暂停按钮就要变成可用状态！开始按钮变成不可用状态！
        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startService(service);

                //开始按钮变成不可用状态！
                bt_start.setEnabled(false);

                //暂停按钮变成可用状态！！
                bt_stop.setEnabled(true);

                //而且暂停按钮的内容设置为暂停
                bt_stop.setText("暂停");

                startTimer = System.currentTimeMillis();
                tempTime = timer;
            }
        });

        //暂停按钮的逻辑
        //计步服务未开启的时候，为不可用状态
        bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //stopService(service);

                //计步服务开启而且有步数，停止服务的同时将按钮内容改为“继续”
                if (StepCounterService.FLAG && StepDetector.CURRENT_SETP > 0) {
                    stopService(service);
                    bt_stop.setText("继续");
                } else {
                    //计步服务关闭了！也就是显示“继续”的时候，这个时候点击按钮就要重新开启服务
                    startService(service);

                    //而且暂停按钮的内容设置为暂停
                    bt_stop.setText("暂停");

                    startTimer = System.currentTimeMillis();
                    tempTime = timer;

                }
                //bt_start.setEnabled(true);
            }
        });

        /**
         * 点击结束按钮时，应该停止计步服务，同时开启一个新的ShowWalkInfoActivity
         * 利用intent把步数、时间、距离、消耗的卡路里、行走速度这些数据传入ShowWalkInfoActivity
         * 在ShowWalkInfoActivity中用户可以看到本次行走的相关数据。
         *
         * 还要实现的一个功能是
         * 每点击一次结束，就更新WalkInfo表中的WalkTotalNum的值（+1）
         *
         *
         */
        bt_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //更新WalkInfo表
                upgradeWalkInfo();


                //String str="行走距离："+formatDouble(distance)+"卡路里"+formatDouble(calories)+"速度"+formatDouble(velocity);
                //Toast.makeText(getApplicationContext(),str, Toast.LENGTH_LONG).show();
                String walksteps = String.valueOf(total_step);
                String walktime = getFormatTime(timer);
                String walkdistance = formatDouble(distance/1000);
                String walkcalories = formatDouble(calories);
                String walkvelocity = formatDouble(velocity);


                stopService(service);
                Intent intent = new Intent(StepCounterActivity.this,ShowWalkInfoActivity.class);
                intent.putExtra("WalkSteps",walksteps);
                intent.putExtra("WalkTime",walktime);
                intent.putExtra("Distance",walkdistance);
                intent.putExtra("Calories",walkcalories);
                intent.putExtra("Velocity",walkvelocity);


                startActivity(intent);
                overridePendingTransition(R.animator.in_from_left, R.animator.out_to_right);
                finish();
            }
        });

        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.animator.in_from_right, R.animator.out_to_left);
            }
        });
    }

    private void upgradeWalkInfo() {
        MyUser currentuser2= BmobUser.getCurrentUser(MyUser.class);
        BmobQuery<WalkInfo> query = new BmobQuery<WalkInfo>();

        //查询WalkInfo表中user为currentuser2的数据记录。
        query.addWhereEqualTo("user",currentuser2);

        //执行查询方法
        query.findObjects(new FindListener<WalkInfo>() {
            @Override
            public void done(List<WalkInfo> list, BmobException e) {
                if(e==null){
                    //找到了当前用户的记录 直接加一
                    WalkInfo walkinfo = new WalkInfo();
                    walkinfo.setWalkTotalNum(list.get(0).getWalkTotalNum()+1);
                    walkinfo.update(list.get(0).getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if(e==null){

                            }
                        }
                    });
                }else{

                }
            }
        });
    }






    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    /**
     * 获取Activity相关控件
     */
    private void addView() {
        tv_show_step = (TextView) this.findViewById(R.id.show_step);

        tv_show_timer = (TextView) this.findViewById(R.id.show_time);


        bt_start = (Button) this.findViewById(R.id.bt_cur_start);
        bt_stop = (Button) this.findViewById(R.id.bt_cur_stop);
        bt_end = (Button) this.findViewById(R.id.bt_cur_end);
        bt_back=(Button)this.findViewById(R.id.bt_currenrwalk_back);




        Intent service = new Intent(this, StepCounterService.class);
        stopService(service);
        StepDetector.CURRENT_SETP = 0;
        tempTime = timer = 0;
        tv_show_timer.setText(getFormatTime(timer));      //如果关闭之后，格式化时间
        tv_show_step.setText("0");


        handler.removeCallbacks(thread);

    }

    /**
     * 初始化界面
     */
    private void init() {


        step_length = 70;
        weight = 50;

        countDistance();
        countStep();

        if ((timer += tempTime) != 0 && distance != 0.0) {
            //tempTime记录运动的总时间，timer记录每次运动时间
            // 体重、距离
            // 跑步热量（kcal）＝体重（kg）×距离（公里）×1.036，换算一下
            calories = weight * distance * 0.001;

            velocity = distance * 1000 / timer;
        } else {
            calories = 0.0;
            velocity = 0.0;
        }

        tv_show_timer.setText(getFormatTime(timer + tempTime));



        tv_show_step.setText(total_step + "");

        bt_start.setEnabled(!StepCounterService.FLAG);
        bt_stop.setEnabled(StepCounterService.FLAG);

        if (StepCounterService.FLAG) {
            bt_stop.setText("暂停");
        } else if (StepDetector.CURRENT_SETP > 0) {
            bt_stop.setEnabled(true);
            bt_stop.setText("继续");
        }

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



    /**
     * 得到一个格式化的时间
     *
     * @param time
     *            时间 毫秒
     * @return 时：分：秒：毫秒
     */
    private String getFormatTime(long time) {
        time = time / 1000;

        long second = time % 60;
        long minute = (time % 3600) / 60;
        long hour = time / 3600;


        // 秒显示两位
        String strSecond = ("00" + second)
                .substring(("00" + second).length() - 2);
        // 分显示两位
        String strMinute = ("00" + minute)
                .substring(("00" + minute).length() - 2);
        // 时显示两位
        String strHour = ("00" + hour).substring(("00" + hour).length() - 2);

        return strHour + ":" + strMinute + ":" + strSecond;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.activity_step, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        /*switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.ment_information:
                break;
        }*/
        return super.onOptionsItemSelected(item);
    }

    /**
     * 计算行走的距离
     */
    private void countDistance() {
        if (StepDetector.CURRENT_SETP % 2 == 0) {
            distance = (StepDetector.CURRENT_SETP / 2) * 3 * step_length * 0.01;
        } else {
            distance = ((StepDetector.CURRENT_SETP / 2) * 3 + 1) * step_length * 0.01;
        }
    }

    /**
     * 实际的步数
     */
    private void countStep() {
        if (StepDetector.CURRENT_SETP % 2 == 0) {
            total_step = StepDetector.CURRENT_SETP;
        } else {
            total_step = StepDetector.CURRENT_SETP +1;
        }
        total_step = StepDetector.CURRENT_SETP;
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        finish();
    }

}

