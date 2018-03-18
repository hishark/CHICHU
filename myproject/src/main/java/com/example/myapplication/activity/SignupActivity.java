package com.example.myapplication.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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
import cn.bmob.v3.listener.SaveListener;

public class SignupActivity extends Activity {

    //1.各种控件
    EditText et_signupUsername,et_signupUserpassword,et_signupUserregion,et_signupUseremail;
    Button bt_signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//页面设置为无标题
        setContentView(R.layout.activity_signup);

        //2.初始化控件们嗯~ o(*￣▽￣*)o
        initView();

        //3.监听器
        bt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyUser user = new MyUser();
                user.setUsername(et_signupUsername.getText().toString());
                user.setPassword(et_signupUserpassword.getText().toString());
                user.setRegion(et_signupUserregion.getText().toString());
                //user.setEmail(et_signupUseremail.getText().toString());
                user.setSign("");
                //注意：不能用save方法进行注册
                user.signUp(new SaveListener<MyUser>() {
                    @Override
                    public void done(MyUser s, BmobException e) {
                        if(e==null){
                            Toast.makeText(getApplicationContext(),"注册成功",Toast.LENGTH_SHORT).show();
                            finish();

                        }else{
                            Toast.makeText(getApplicationContext(),"注册失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });



            }
        });
    }

    private void initView() {
        et_signupUsername=(EditText)this.findViewById(R.id.et_SignupUserName);
        et_signupUserpassword=(EditText)this.findViewById(R.id.et_SignupUserPassword);
        et_signupUserregion=(EditText)this.findViewById(R.id.et_SignupUserRegion);
        //et_signupUseremail=this.findViewById(R.id.et_SignupUserEmail);
        bt_signup=(Button)this.findViewById(R.id.bt_signup);
    }
}
