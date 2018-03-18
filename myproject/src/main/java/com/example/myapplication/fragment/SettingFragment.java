package com.example.myapplication.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.activity.FeedBackActivity;
import com.example.myapplication.activity.LoginActivity;
import com.example.myapplication.activity.ModifyDataActivity;
import com.example.myapplication.activity.RankingListActivity;
import com.example.myapplication.bean.MyUser;
import com.example.myapplication.bean.Preference;
import com.example.myapplication.bean.WalkGoal;
import com.example.myapplication.utils.BitmapUtil;
import com.example.myapplication.utils.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;


/**
 * Created by 程洁 on 2017/11/26.
 */
public class SettingFragment extends Fragment {

    private View view;
    private GridView gridview;
    //private ImageView avatar;
    TextView tv_username;
    TextView tv_userContent;
    ImageView avatar,gender;

    //---设置偏好对话框要用到的变量------------
    boolean StepsIsChecked = true;
    boolean DistanceIsChecked = false;
    //-------------------------------

    //---设置目标对话框要用到的变量------------
    Integer WalkGoalNum;
    Button bt_SetGoal,bt_return;
    //数字选择器
    NumberPicker np_Goal;
    //----------------------------------------

    private ImageLoader setImageLoader=new ImageLoader();

    private int images[]={R.drawable.setdata1,R.drawable.goal6,R.drawable.setpre1,
            R.drawable.feedback1,R.drawable.rank1,R.drawable.login1};
    private String textOfSet[]={"修改资料","设置目标","设置偏好","反馈信息","排行榜","登录"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_setting, container, false);
        init();
        initData();
        initGriew();

        Button bt_logout=(Button)view.findViewById(R.id.bt_logout);
        MyUser user = BmobUser.getCurrentUser(MyUser.class);
        bt_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BmobUser.logOut();
                Toast.makeText(getContext(),"已注销",Toast.LENGTH_SHORT).show();
                tv_username.setText("");
                tv_userContent.setText("");
                avatar.setImageResource(R.drawable.avatar1);
            }
        });

        if(user!=null){
            tv_username.setText(user.getUsername().toString());

            if(user.getSign().toString().equals("")){
                tv_userContent.setText("这个家伙很懒，什么也没留下~");
            }
            else{
                tv_userContent.setText(user.getSign().toString());

            }
        }


        return view;
    }

    private void initGriew() {
        ArrayList<Map<String,Object>> listItems=new ArrayList<Map<String,Object>>();
        for(int i=0;i<images.length;i++) {
            Map<String,Object> listItem=new HashMap<String,Object>();
            listItem.put("image", images[i]);
            listItem.put("text",textOfSet[i]);
            listItems.add(listItem);
        }
        SimpleAdapter sa=new SimpleAdapter(getActivity(),
                listItems,R.layout.gridview_item,new String[]{"image","text"},
                new int[]{R.id.image_setting,R.id.tv_setting});
        gridview.setAdapter(sa);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                switch(i) {
                    case 0:  //i=0  跳转到修改资料页面
                        Intent intent1 = new Intent(getActivity(),ModifyDataActivity.class);
                        startActivity(intent1);
                        getActivity().overridePendingTransition(R.animator.in_from_top, R.animator.out_to_bottom);
                        break;
                    case 1:  //i=1
                        //弹出对话框让用户进行目标设置
                        createGoalDialog();
                        break;
                    case 2:  //i=2  弹出对话框让用户进行偏好设置
                        createPreferenceDialog();
                        break;
                    case 3:  //i=3  提交反馈信息界面
                        Intent intent4 = new Intent(getActivity(), FeedBackActivity.class);
                        startActivity(intent4);
                        getActivity().overridePendingTransition(R.animator.in_from_left, R.animator.out_to_right);
                        break;
                    case 4:  //i=4  查看排行榜
                        Intent intent5 = new Intent(getActivity(), RankingListActivity.class);
                        startActivity(intent5);
                        getActivity().overridePendingTransition(R.animator.in_from_top, R.animator.out_to_bottom);
                        break;
                    case 5:  //i=5  登录
                        Intent intent=new Intent(getActivity(),LoginActivity.class);
                        startActivity(intent);
                        break;
                }

            }
        });
    }

    private void initData() {
        MyUser user = BmobUser.getCurrentUser(MyUser.class);
        if(user!=null) {
            String url = user .getAvatarUrl();
            if (url == null) {
                avatar.setImageResource(R.drawable.avatar1);
            } else {
                avatar.setTag(url);
                setImageLoader.showImageByAsyncTask(avatar,url);
                /*new AsyncTask<String, String, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(String... arg0) {
                        String url = arg0[0];
                        Bitmap bm = BitmapUtil.getPicture(url);
                        return bm;
                    }

                    @Override
                    protected void onPostExecute(Bitmap result) {
                        avatar.setImageBitmap(result);
                    }
                }.execute(url);*/

            }
            if(user.getSign().equals("")){
                tv_userContent.setText("这个用户很懒，什么也没留下~");
            }else{
                tv_userContent.setText(user.getSign());
            }
            if(user.getSex()==null)
            {
                gender.setImageResource(R.drawable.gender);
            }else if(user.getSex().equals("男")){
                gender.setImageResource(R.drawable.man);
            }else{
                gender.setImageResource(R.drawable.female);
            }
        } else{
            avatar.setImageResource(R.drawable.defaultavatar);
            tv_userContent.setText("");
            tv_username.setText("");
            gender.setImageResource(R.drawable.gender);
        }
    }

    private void init() {
        tv_username=(TextView)view.findViewById(R.id.tv_userName);
        tv_userContent=(TextView)view.findViewById(R.id.tv_userContent);
        avatar=view.findViewById(R.id.rank_CurrentUser_avatar);
        gender=(ImageView)view.findViewById(R.id.setting_img_gender);
        gridview=(GridView)view.findViewById(R.id.gridviewOfset);
    }

    private void createGoalDialog() {
        final AlertDialog alertDialog;
        AlertDialog.Builder ad = new AlertDialog.Builder(SettingFragment.this.getActivity());
        ad.setTitle("设置目标");

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_set_goal, null);//加载自定义的布局文件
        np_Goal = (NumberPicker)view.findViewById(R.id.np_goal);

        ad.setView(view);
        //初始化数字选择器
        setNumberPicker();

        np_Goal.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                //i是当前值的上一个值，i1是当前值

                //把当前选择的目标值保存下来，存入Bmob
                WalkGoalNum=i1;
            }
        });


        ad.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final MyUser currentuser=BmobUser.getCurrentUser(MyUser.class);

                BmobQuery<WalkGoal> query = new BmobQuery<WalkGoal>();

                //查询WalkGoal表中user为currentuser的数据记录。
                query.addWhereEqualTo("user",currentuser);

                //执行查询方法
                query.findObjects(new FindListener<WalkGoal>() {
                    @Override
                    public void done(List<WalkGoal> list, BmobException e) {
                        if(e==null){
                            WalkGoal walkgoal=new WalkGoal();
                            walkgoal.setGoalNum(WalkGoalNum);
                            walkgoal.update(list.get(0).getObjectId(), new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if(e==null){
                                        //Toast.makeText(SettingFragment.this.getContext(), "当前用户目标信息更新成功", Toast.LENGTH_SHORT).show();
                                    }else{
                                        //Toast.makeText(SettingFragment.this.getContext(), "当前用户目标信息更新失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }else{
                            WalkGoal walkgoal=new WalkGoal();
                            walkgoal.setUser(currentuser);
                            walkgoal.setGoalNum(WalkGoalNum);
                            walkgoal.save(new SaveListener<String>() {
                                @Override
                                public void done(String s, BmobException e) {
                                    if(e==null){
                                        //Toast.makeText(SettingFragment.this.getContext(), "当前用户目标信息创建成功", Toast.LENGTH_SHORT).show();
                                    }else{
                                        //Toast.makeText(SettingFragment.this.getContext(), "当前用户目标信息创建失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    }
                });
            }
        });


        ad.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog = ad.create();
        alertDialog.show();
    }

    private void setNumberPicker() {
        np_Goal.setMaxValue(100);
        np_Goal.setMinValue(0);
        np_Goal.setValue(20);

    }


    //创建设置偏好对话框，并对偏好表进行添加数据或更新数据操作
    private void createPreferenceDialog() {
        final AlertDialog alertDialog;
        AlertDialog.Builder ad = new AlertDialog.Builder(SettingFragment.this.getActivity());
        ad.setTitle("设置偏好");
        ad.setSingleChoiceItems(new String[]{"行走步数", "行走距离"}, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i==0){
                    //选中步数
                    StepsIsChecked = true;
                    DistanceIsChecked = false;
                }else if(i==1){
                    //选中距离
                    StepsIsChecked = false;
                    DistanceIsChecked = true;
                }
            }
        });
        ad.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MyUser currentuser2=BmobUser.getCurrentUser(MyUser.class);
                BmobQuery<Preference> query = new BmobQuery<Preference>();

                //查询Preference表中user为currentuser2的数据记录。
                query.addWhereEqualTo("user",currentuser2);

                //执行查询方法
                query.findObjects(new FindListener<Preference>() {
                    @Override
                    public void done(List<Preference> list, BmobException e) {
                        if(e==null){
                            //偏好表里存在当前用户，那就去更新设置
                            Preference pre=new Preference();
                            if(DistanceIsChecked){
                                pre.setShowDistance(true);
                                pre.setShowSteps(false);
                            }else if(StepsIsChecked){
                                pre.setShowSteps(true);
                                pre.setShowDistance(false);
                            }
                            pre.update(list.get(0).getObjectId(), new UpdateListener() {
                                @Override
                                public void done(BmobException e) {

                                }
                            });
                        }else{
                            //偏好表里不存在当前用户，那就去创建一条新的记录
                            Preference pre = new Preference();
                            MyUser currentUser1 = BmobUser.getCurrentUser(MyUser.class);
                            pre.setUser(currentUser1);
                            if(DistanceIsChecked){
                                pre.setShowDistance(true);
                                pre.setShowSteps(false);
                            }else if(StepsIsChecked){
                                pre.setShowSteps(true);
                                pre.setShowDistance(false);
                            }
                            pre.save(new SaveListener<String>() {
                                @Override
                                public void done(String s, BmobException e) {
                                    if(e==null){
                                        Toast.makeText(SettingFragment.this.getContext(), "success", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(SettingFragment.this.getContext(), "error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
        ad.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog = ad.create();
        alertDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }







}
