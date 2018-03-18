package com.example.myapplication.fragment;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;

import com.example.myapplication.bean.MyUser;
import com.example.myapplication.bean.StepEntity;
import com.example.myapplication.bean.WalkGoal;
import com.example.myapplication.bean.WalkInfo;
import com.example.myapplication.calendar.BeforeOrAfterCalendarView;
import com.example.myapplication.constant.Constant;
import com.example.myapplication.database.StepDataDao;
import com.example.myapplication.service.StepService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.example.myapplication.utils.StepCountCheckUtil;
import com.example.myapplication.utils.TimeUtil;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;


public class RecordFragment extends Fragment {


    private View view;

    //日历要用到的变量们
    private LinearLayout movementCalenderLl;
    private BeforeOrAfterCalendarView calenderView;

    //计步器要用到的变量们
    private TextView kmTimeTv;
    private TextView totalKmTv;
    private TextView stepsTimeTv;
    private TextView totalStepsTv;
    private TextView supportTv;
    private String curSelDate;
    private DecimalFormat df = new DecimalFormat("#.##");
    private List<StepEntity> stepEntityList = new ArrayList<>();
    private StepDataDao stepDataDao;

    //总行走次数和目标次数的相对布局
    private RelativeLayout WalkTotalNum,WalkGoalNum;

    //当前日期
    private static String CURRENT_DATE;

    //当前用户目标行走次数
    Integer curUserWalkGoal,curUserWalkTotalNum;
    private TextView tv_curUserWalkGoal,tv_curUserWalkTotalNum;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_record, container, false);

        //实例化日历控件并且添加到碎片顶部
        movementCalenderLl = (LinearLayout)view.findViewById(R.id.movement_records_calender_ll);
        calenderView = new BeforeOrAfterCalendarView(this.getContext());
        movementCalenderLl.addView(calenderView);

        //初始化记录控件们
        initView();

        //如果设备支持计步就将数据显示到界面上
        initData();

        //设置日历点击事件监听器
        initListener();

        //如果当前有用户已登录，就把当日行走信息从本地数据库取出然后保存到Bmob云数据库中去
        if(BmobUser.getCurrentUser(MyUser.class)!=null){
            saveToDatabase();
        }

        //如果当前有用户已登录，就把当前用户的目标信息从bmob取出，然后展示到碎片中
        if(BmobUser.getCurrentUser(MyUser.class)!=null){
            showUserGoal();
        }

        //如果当前有用户已登录，就把当前用户的总行走次数信息从bmob取出，然后展示到碎片中
        if(BmobUser.getCurrentUser(MyUser.class)!=null){
            showUserTotalWalkNum();
        }

        return view;
    }

    private void showUserTotalWalkNum() {
        MyUser currentuser=BmobUser.getCurrentUser(MyUser.class);
        BmobQuery<WalkInfo> query = new BmobQuery<WalkInfo>();

        //查询WalkGoal表中user为currentuser的数据记录。
        query.addWhereEqualTo("user",currentuser);

        //执行查询方法
        query.findObjects(new FindListener<WalkInfo>() {
            @Override
            public void done(List<WalkInfo> list, BmobException e) {
                if(e==null){
                    //Toast.makeText(RecordFragment.this.getContext(), list.get(0).getGoalNum().toString(), Toast.LENGTH_SHORT).show();
                    curUserWalkTotalNum=list.get(0).getWalkTotalNum();

                    if(curUserWalkTotalNum!=null){
                        tv_curUserWalkTotalNum.setText(String.valueOf(curUserWalkTotalNum));
                    }else{
                        //使总行走次数显示为0
                        tv_curUserWalkTotalNum.setText(String.valueOf(0));

                        //使总行走次数显示为0的同时还要更新walkinfo表里的总行走次数，改为0！
                        WalkInfo walkInfo=new WalkInfo();
                        walkInfo.setValue("WalkTotalNum",0);
                        walkInfo.update(list.get(0).getObjectId(), new UpdateListener() {
                            @Override
                            public void done(BmobException e) {

                            }
                        });

                    }


                }else{

                    Toast.makeText(RecordFragment.this.getContext(), "fail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showUserGoal() {
        MyUser currentuser=BmobUser.getCurrentUser(MyUser.class);


        BmobQuery<WalkGoal> query = new BmobQuery<WalkGoal>();

        //查询WalkGoal表中user为currentuser的数据记录。
        query.addWhereEqualTo("user",currentuser);

        //执行查询方法
        query.findObjects(new FindListener<WalkGoal>() {
            @Override
            public void done(List<WalkGoal> list, BmobException e) {
                if(e==null){
                    //Toast.makeText(RecordFragment.this.getContext(), list.get(0).getGoalNum().toString(), Toast.LENGTH_SHORT).show();
                    curUserWalkGoal=list.get(0).getGoalNum();

                    //curUserWalkGoal已经存了当前用户的目标，接下来就将这个值显示到界面中
                    //另外，这一句如果写在这个函数外面就会反应慢一些，鬼知道为什么OTZ
                    tv_curUserWalkGoal.setText(String.valueOf(curUserWalkGoal));
                }else{
                    tv_curUserWalkGoal.setText(String.valueOf("0"));
                }
            }
        });
    }

    private void saveToDatabase() {
        //-----------在这里把当天的数据存到bmob运动信息表里去-------------
        /**
         * 如果当前用户在walkinfo表中已存在，就更新信息
         * 如果当前用户在walkinfo表中不存在，就添加信息
         */

        //获取当前时间
        CURRENT_DATE = TimeUtil.getCurrentDate();
        //获取本地数据库
        stepDataDao = new StepDataDao(RecordFragment.this.getContext());
        //从本地数据库中获取当天的数据
        final StepEntity entity = stepDataDao.getCurDataByDate(CURRENT_DATE);

        final MyUser currentuser = BmobUser.getCurrentUser(MyUser.class);
        final String objectid=currentuser.getObjectId();

        BmobQuery<WalkInfo> query = new BmobQuery<WalkInfo>();
        //查询WalkInfo表中user为currentuser的数据记录。
        query.addWhereEqualTo("user",currentuser);
        //执行查询方法
        query.findObjects(new FindListener<WalkInfo>() {
            @Override
            public void done(List<WalkInfo> list, BmobException e) {
                if(e==null){
                    WalkInfo walkinfo=new WalkInfo();
                    walkinfo.setWalkSteps(Integer.parseInt(entity.getSteps()));
                    walkinfo.update(list.get(0).getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {

                        }
                    });

                }else{
                    WalkInfo walkinfo=new WalkInfo();
                    walkinfo.setUser(currentuser);
                    walkinfo.setWalkSteps(Integer.parseInt(entity.getSteps()));
                    walkinfo.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {

                        }
                    });

                }
            }
        });




    }

    private void initData() {
        /**
         * 这里判断当前设备是否支持计步
         */
        if (StepCountCheckUtil.isSupportStepCountSensor(this.getContext())) {
            getRecordList();
            supportTv.setVisibility(View.GONE);
            //setDatasFromBmob();
            setDatas();
            setupService();
        } else {
            totalStepsTv.setText("0");
            supportTv.setVisibility(View.VISIBLE);
        }
    }

    private void initView() {
        movementCalenderLl = (LinearLayout)view.findViewById(R.id.movement_records_calender_ll);
        kmTimeTv = (TextView)view.findViewById(R.id.movement_total_km_time_tv);
        totalKmTv = (TextView)view.findViewById(R.id.movement_total_km_tv);
        stepsTimeTv = (TextView)view.findViewById(R.id.movement_total_steps_time_tv);
        totalStepsTv = (TextView)view.findViewById(R.id.movement_total_steps_tv);
        supportTv = (TextView)view.findViewById(R.id.is_support_tv);
        curSelDate = TimeUtil.getCurrentDate();


        WalkTotalNum=(RelativeLayout)view.findViewById(R.id.rl_WalkTotalNum);
        WalkGoalNum=(RelativeLayout)view.findViewById(R.id.rl_WalkGoalNum);
        tv_curUserWalkGoal=(TextView)view.findViewById(R.id.tv_walk_goal_num);
        tv_curUserWalkTotalNum=(TextView)view.findViewById(R.id.tv_walk_total_num);
    }

    private void initListener() {
        calenderView.setOnBoaCalenderClickListener(new BeforeOrAfterCalendarView.BoaCalenderClickListener() {
            @Override
            public void onClickToRefresh(int position, String curDate) {
                //获取当前选中的时间
                curSelDate = curDate;
                //根据日期去取数据
                if(curSelDate.equals(CURRENT_DATE)){
                    setDatas();
                    WalkGoalNum.setVisibility(View.VISIBLE);
                    WalkTotalNum.setVisibility(View.VISIBLE);
                }else{
                    setDatas();
                    WalkGoalNum.setVisibility(View.GONE);
                    WalkTotalNum.setVisibility(View.GONE);
                }


            }
        });
    }

    private boolean isBind = false;
    private Messenger mGetReplyMessenger = new Messenger(new Handler());
    private Messenger messenger;

    /**
     * 开启计步服务
     */
    private void setupService() {
        Intent intent = new Intent(RecordFragment.this.getContext(), StepService.class);
        isBind = getContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
        getContext().startService(intent);
    }

    /**
     * 定时任务
     */
    private TimerTask timerTask;
    private Timer timer;

    /**
     * 用于查询应用服务（application Service）的状态的一种interface，
     * 更详细的信息可以参考Service 和 context.bindService()中的描述，
     * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
     */
    private ServiceConnection conn = new ServiceConnection() {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         * @param name 实际所连接到的Service组件名称
         * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
         */
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            /**
             * 设置定时器，每个三秒钟去更新一次运动步数
             */
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        messenger = new Messenger(service);
                        Message msg = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                        msg.replyTo = mGetReplyMessenger;
                        messenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            };
            timer = new Timer();
            timer.schedule(timerTask, 0, 500);
        }

        /**
         * 当与Service之间的连接丢失的时候会调用该方法，
         * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
         * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
         * @param name 丢失连接的组件名称
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    /**
     * 设置记录数据
     *
     */
    private void setDatas() {
        StepEntity stepEntity = stepDataDao.getCurDataByDate(curSelDate);

        if (stepEntity != null) {
            int steps = Integer.parseInt(stepEntity.getSteps());

            //获取全局的步数
            totalStepsTv.setText(String.valueOf(steps));
            //计算总公里数
            totalKmTv.setText(countTotalKM(steps));
        } else {
            //获取全局的步数
            totalStepsTv.setText("0");
            //计算总公里数
            totalKmTv.setText("0");
        }

        //设置时间
        String time = TimeUtil.getWeekStr(curSelDate);
        kmTimeTv.setText(time);
        stepsTimeTv.setText(time);
    }

    //TODO:从Bmob里取出当前用户的运动信息显示到记录碎片中
    /**
     * 从Bmob里取出当前用户的运动信息显示到记录碎片中
     * 尝试一下
     */
    /*private void setDatasFromBmob() {

        MyUser currentuser = BmobUser.getCurrentUser(MyUser.class);
        String objectid=currentuser.getObjectId();

        BmobQuery<WalkInfo> query = new BmobQuery<WalkInfo>();
        //查询WalkInfo表中user为currentuser的数据记录。
        query.addWhereEqualTo("user",currentuser);
        //执行查询方法
        query.findObjects(new FindListener<WalkInfo>() {
            @Override
            public void done(List<WalkInfo> list, BmobException e) {
                if(e==null){
                    //Toast.makeText(RecordFragment.this.getContext(), list.get(0).getWalkSteps().toString(), Toast.LENGTH_SHORT).show();
                    totalStepsTv.setText(String.valueOf(list.get(0).getWalkSteps()));
                    totalKmTv.setText(countTotalKM(list.get(0).getWalkSteps()));
                }else{
                    Toast.makeText(RecordFragment.this.getContext(), "未从bmob中获得数据", Toast.LENGTH_SHORT).show();

                }
            }
        });

        //设置时间
        String time = TimeUtil.getWeekStr(curSelDate);
        kmTimeTv.setText(time);
        stepsTimeTv.setText(time);
    }*/



    /**
     * 简易计算公里数，假设一步大约有0.7米
     *
     * @param steps 用户当前步数
     * @return
     */
    private String countTotalKM(int steps) {
        double totalMeters = steps * 0.7;
        //保留两位有效数字
        return df.format(totalMeters / 1000);
    }


    /**
     * 获取全部运动历史纪录
     */
    private void getRecordList() {
        //获取数据库
        stepDataDao = new StepDataDao(this.getContext());
        stepEntityList.clear();
        stepEntityList.addAll(stepDataDao.getAllDatas());
        if (stepEntityList.size() >= 7) {
            // TODO: 2017/3/27 在这里获取历史记录条数，当条数达到7条或以上时，就开始删除第七天之前的数据,暂未实现

        }

    }


    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            //这里用来获取到Service发来的数据
            case Constant.MSG_FROM_SERVER:

                //如果是今天则更新数据
                if (curSelDate.equals(TimeUtil.getCurrentDate())) {
                    //记录运动步数
                    int steps = msg.getData().getInt("steps");
                    //设置的步数
                    totalStepsTv.setText(String.valueOf(steps));
                    //计算总公里数
                    totalKmTv.setText(countTotalKM(steps));

                }
                break;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //记得解绑Service，不然多次绑定Service会异常
        if (isBind)
            this.getContext().unbindService(conn);
    }




    //动态添加碎片
    public void replaceFragment(Fragment fragment) {
        FragmentManager manager=getActivity().getSupportFragmentManager();
        FragmentTransaction transaction=manager.beginTransaction();
        transaction.replace(R.id.content, fragment);
        //transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}
