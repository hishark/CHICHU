package com.example.myapplication.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.bean.MyUser;
import com.example.myapplication.fragment.SettingFragment;
import com.example.myapplication.utils.BitmapUtil;
import com.example.myapplication.utils.ImageLoader;

import org.w3c.dom.Text;

import java.io.File;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class ModifyDataActivity extends AppCompatActivity {

    //1.定义变量
    EditText et_Sign,et_Password,et_Region,et_Gender;
    ImageView img_Gender;
    Button btSaveData;
    CircleImageView avatar;
    String path="";
    private File mFile;
    //File file;
    private Uri imageUri; //图片路径
    private String filename; //图片名称
    TextView tv_cur_username;
    Button bt_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_data);
        getSupportActionBar().hide();
        //2.初始化控件
        init();
        initData();

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = "选择获取图片方式";
                String[] items = new String[]{"拍照", "相册"};

                new AlertDialog.Builder(ModifyDataActivity.this)
                        .setTitle(title)
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                switch (which) {
                                    case 0:
                                        //选择拍照
                                        pickImageFromCamera();
                                        break;
                                    case 1:
                                        //选择相册
                                        pickImageFromAlbum();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).show();
            }

        });
        /*
        * 设置保存按钮监听事件，实现更新用户资料*/
        btSaveData.setOnClickListener(new saveDate());

        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.animator.in_from_bottom, R.animator.out_to_top);
            }
        });

    }

    //相册
    private void pickImageFromAlbum() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.setType("image/*");//图片
        startActivityForResult(galleryIntent, 0);
        //Intent picIntent = new Intent(Intent.ACTION_PICK, null);
        //picIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        //startActivityForResult(picIntent, 0);

    }
    //拍照
    private void pickImageFromCamera() {
        Intent cameraIntent = new Intent(
                "android.media.action.IMAGE_CAPTURE");
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!file.exists()) {
            file.mkdirs();
        }
        mFile = new File(file, System.currentTimeMillis() + ".jpg");
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));//指定图片输出地址
        cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(cameraIntent, 1);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        } else {
            switch (requestCode) {
                case 0:
                    Uri originalUri=data.getData();//获取图片uri
                    startPhotoZoom(originalUri);
                    //下面方法将获取的uri转为String类型哦！
                    String []imgs1={MediaStore.Images.Media.DATA};//将图片URI转换成存储路径
                    //android多媒体数据库的封装接口
                    Cursor cursor=this.managedQuery(originalUri, imgs1, null, null, null);
                    int index=cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();//将光标移到开头
                    path=cursor.getString(index);//最后根据索引值获取图片路径
                    //upload(path);

                    break;
                case 1:
                    if (isSdcardExisting()) {
                        startPhotoZoom(Uri.fromFile(mFile));
                        path = mFile.getPath();
                    } else {
                        Toast.makeText(ModifyDataActivity.this, "未找到存储卡，无法存储照片！",
                                Toast.LENGTH_LONG).show();
                    }
                    break;

                case 2:
                    if (data != null) {
                        showResizeImage(data);
                        upload(path);
                    }
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean isSdcardExisting() {//判断SD卡是否存在
        final String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }
    private void showResizeImage(Intent data) {//显示图片
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            avatar.setImageBitmap(photo);
        }
    }

    /**
     * 将图片上传
     * @param imgpath
     */
    private void upload(String imgpath){
        final BmobFile icon = new BmobFile(new File(imgpath));
        icon.upload(new UploadFileListener() {

            @Override
            public void done(BmobException e) {
                if(e==null){
                    MyUser m=new MyUser();
                    //m.setImage(icon);
                    String fileUrl = icon.getFileUrl();
                    m.setAvatarUrl(fileUrl);
                    final MyUser user = BmobUser.getCurrentUser(MyUser.class);
                    m.update(user.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if(e==null){
                                Toast.makeText(getApplicationContext(),"上传头像成功",Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getApplicationContext(),"上传头像失败:" + e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(getApplicationContext(),
                            "上传头像失败",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    /**
     * 打开系统图片裁剪功能
     *
     */
    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);//可以裁剪
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("scale", true); //黑边
        intent.putExtra("scaleUpIfNeeded", true); //黑边
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,  uri);
        startActivityForResult(intent, 2);
    }
    private class saveDate implements View.OnClickListener {
        public void onClick(View view) {
            MyUser newUser=new MyUser();


            if(et_Sign.getText().toString().equals("")){
                newUser.setSign("这个用户很懒，什么也没留下~");
            }else{
                newUser.setSign(et_Sign.getText().toString());
            }

            if(et_Region.getText().toString().equals("")){
                newUser.setRegion("地球");
            }else{
                newUser.setRegion(et_Region.getText().toString());
            }

            if(et_Gender.getText().toString().equals("女")){
                newUser.setSex("女");
                img_Gender.setImageResource(R.drawable.female);
            }else if(et_Gender.getText().toString().equals("男")){
                newUser.setSex("男");
                img_Gender.setImageResource(R.drawable.man);
            }else{
                newUser.setSex("");
                img_Gender.setImageResource(R.drawable.gender);
            }


            final MyUser user = BmobUser.getCurrentUser(MyUser.class);

            newUser.update(user.getObjectId(), new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if(e==null){
                        Toast.makeText(getApplicationContext(),"修改资料成功",Toast.LENGTH_SHORT).show();
                        finish();
                        overridePendingTransition(R.animator.in_from_bottom, R.animator.out_to_top);

                    }else{
                        Toast.makeText(getApplicationContext(),"修改资料失败:" + e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            });



        }
    }
    private ImageLoader modImageLoader=new ImageLoader();
    private void initData() {
        final MyUser user=BmobUser.getCurrentUser(MyUser.class);
        if(user==null)
            return;
        String url=user.getAvatarUrl();
        if(url==null){
            avatar.setImageResource(R.drawable.avatar1);
        }
        else {
            avatar.setTag(url);
            modImageLoader.showImageByAsyncTask(avatar,url);
            /*new AsyncTask<String, String, Bitmap>() {
                @Override
                protected Bitmap doInBackground(String... arg0) {
                    String url=arg0[0];
                    Bitmap bm= BitmapUtil.getPicture(url);
                    return bm;
                }

                @Override
                protected void onPostExecute(Bitmap result) {
                    avatar.setImageBitmap(result);
                }
            }.execute(url);*/
        }
        et_Sign.setText(user.getSign());
        et_Region.setText(user.getRegion());

        et_Gender.setText(user.getSex());
        if(user.getSex()==null)
        {
            img_Gender.setImageResource(R.drawable.gender);
        }else if(user.getSex().equals("男")){
            img_Gender.setImageResource(R.drawable.man);
        }else{
            img_Gender.setImageResource(R.drawable.female);
        }

        tv_cur_username.setText(user.getUsername());
    }

    private void init(){
        avatar=(CircleImageView)this.findViewById(R.id.user_avatar);
        et_Sign=(EditText)this.findViewById(R.id.et_sign);
        //et_Password=(EditText)this.findViewById(R.id.et_password);
        et_Region=(EditText)this.findViewById(R.id.et_region);
        et_Gender=(EditText)this.findViewById(R.id.et_gender);
        img_Gender=(ImageView)this.findViewById(R.id.img_gender);
        btSaveData=(Button)this.findViewById(R.id.bt_savedata);
        tv_cur_username=(TextView)this.findViewById(R.id.modify_CurrentUser_Name);
        bt_back=(Button)this.findViewById(R.id.bt_modify_back);
    }
}
