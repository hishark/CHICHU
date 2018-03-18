package com.example.myapplication.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.bean.MyUser;
import com.example.myapplication.bean.Post;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class SendPostActivity extends AppCompatActivity {

    private EditText content;
    private Button bt1,bt2;
    private Button btreturn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_post);
        getSupportActionBar().hide();
        content = (EditText) findViewById(R.id.edit_content);
        bt1=(Button)findViewById(R.id.bt_fatie);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"正在提交.......",Toast.LENGTH_SHORT).show();
                sendAction();
            }
        });

        /*bt2=(Button)findViewById(R.id.bt_tuichu);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO:为啥不能用Intent跳转？？？？
                finish();
            }
        });*/
        btreturn=(Button)findViewById(R.id.bt_sendpost_back);
        btreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.animator.in_from_right, R.animator.out_to_left);
            }
        });
    }


    private void sendAction(){
        String commitContent = content.getText().toString();
        if (TextUtils.isEmpty(commitContent)) {
            Toast.makeText(getApplicationContext(),"内容不能为空~",Toast.LENGTH_SHORT).show();
            return;
        }
        MyUser user= BmobUser.getCurrentUser(MyUser.class);
        Post posts=new Post();
        posts.setAuthor(user);
        posts.setContent(commitContent);
        posts.setComment(0);
        posts.setLove(0);
        posts.save(new SaveListener() {
            @Override
            public void done(Object o, BmobException e) {
                if(e==null){
                    Toast.makeText(getApplicationContext(),"发表成功",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"发表失败",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
