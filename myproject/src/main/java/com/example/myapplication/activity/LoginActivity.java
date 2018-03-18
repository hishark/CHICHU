package com.example.myapplication.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.bean.MyUser;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class LoginActivity extends Activity {

    //1.各种控件
    EditText et_loginUsername,et_loginUserpassword;
    Button bt_login;
    TextView startAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//页面设置为无标题
        setContentView(R.layout.activity_login);

        //2.初始化控件
        initView();

        //3.监听器
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyUser user = new MyUser();
                user.setUsername(et_loginUsername.getText().toString());
                user.setPassword(et_loginUserpassword.getText().toString());
                //注意：不能用save方法进行注册
                user.login(new SaveListener<MyUser>() {
                    @Override
                    public void done(MyUser myUser, BmobException e) {
                        if(e==null){
                            Toast.makeText(getApplicationContext(),"登录成功",Toast.LENGTH_SHORT).show();
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(),"用户不存在，请注册",Toast.LENGTH_SHORT).show();

                            Intent intent=new Intent(LoginActivity.this,SignupActivity.class);
                            startActivity(intent);
                            finish();

                        }
                    }
                });
            }
        });
        startAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        et_loginUsername=(EditText)this.findViewById(R.id.et_loginUserName);
        et_loginUserpassword=(EditText)this.findViewById(R.id.et_loginUserPassword);
        bt_login=(Button)this.findViewById(R.id.bt_login);
        startAccount=(TextView)this.findViewById(R.id.text_login);
    }
}
