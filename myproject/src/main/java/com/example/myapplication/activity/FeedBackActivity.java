package com.example.myapplication.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.bean.Advice;
import com.example.myapplication.fragment.SettingFragment;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class FeedBackActivity extends AppCompatActivity {

    private Button commit,bt_return;
    private EditText input;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);
        getSupportActionBar().hide();
        init();
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(input.getText().equals("")){
                    Toast.makeText(getApplicationContext(),"建议不能为空",Toast.LENGTH_SHORT).show();
                }else{
                    sendFeedBack(input.getText().toString().trim());

                }
            }
        });
        bt_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.animator.in_from_right, R.animator.out_to_left);
            }
        });

    }

    private void sendFeedBack(String msg) {
        Advice advice=new Advice();
        advice.setAdvices(msg);
        advice.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    Toast.makeText(getApplicationContext(),"我们已收到您的反馈，谢谢",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),"抱歉，您的反馈被网络吞了",Toast.LENGTH_SHORT).show();
                }
                //Intent intent=new Intent(FeedBackActivity.this, SettingFragment.class);
                //startActivity(intent);
            }
        });

    }


    private void init() {
        input=(EditText)this.findViewById(R.id.et_advice);
        commit=(Button)this.findViewById(R.id.bt_advice);
        bt_return=(Button)this.findViewById(R.id.bt_feedback_back);
    }
}
