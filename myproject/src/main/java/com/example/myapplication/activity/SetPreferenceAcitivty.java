package com.example.myapplication.activity;

import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.bean.MyUser;
import com.example.myapplication.bean.Preference;
import com.example.myapplication.bean.WalkInfo;
import com.example.myapplication.fragment.RecordFragment;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class SetPreferenceAcitivty extends AppCompatActivity {

    RadioGroup rg;
    RadioButton rb_walksteps,rb_walkdistance;
    boolean StepsIsChecked = false;
    boolean DistanceIsChecked = false;
    Button bt_setpre;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_preference_acitivty);

        initView();

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                //如果选中了行走距离，就让DistanceIsChecked==true;
                if(rb_walkdistance.getId()==i){
                    DistanceIsChecked = true;
                    StepsIsChecked = false;
                }else if(rb_walksteps.getId()==i){
                    StepsIsChecked = true;
                    DistanceIsChecked = false;
                }
            }
        });

        bt_setpre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                            Toast.makeText(SetPreferenceAcitivty.this, "偏好表里存在当前用户", Toast.LENGTH_SHORT).show();
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
                                    if(e==null){
                                        Toast.makeText(SetPreferenceAcitivty.this, "偏好表更新成功", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(SetPreferenceAcitivty.this, "偏好表更新失败", Toast.LENGTH_SHORT).show();
                                    }
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
                                        Toast.makeText(SetPreferenceAcitivty.this, "success", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(SetPreferenceAcitivty.this, "error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });






    }

    private void initView() {
        rg = (RadioGroup) this.findViewById(R.id.rg_showpre);
        rb_walksteps = (RadioButton) this.findViewById(R.id.rb_walksteps);
        rb_walkdistance = (RadioButton) this.findViewById(R.id.rb_walkdistance);
        bt_setpre = (Button) this.findViewById(R.id.bt_setpre);
    }
}
