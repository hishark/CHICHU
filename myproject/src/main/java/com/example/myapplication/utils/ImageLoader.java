package com.example.myapplication.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.widget.ImageView;
import com.example.myapplication.R;
import com.example.myapplication.adapter.PostAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by 程洁 on 2017/12/19.
 */


public class ImageLoader {

    private ImageView mImageView;
    private String mUrl;

    //创建cache
    private LruCache<String,Bitmap> mCaches;//我们需要缓存图像的名字

    public ImageLoader(){


        //获取最大可用内存
        int maxMerory=(int)Runtime.getRuntime().maxMemory();
        int cacheSize=maxMerory/4;
        //初始化mchache
        mCaches=new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
               //加载正确的内存大小，不然默认返回元素的个数
                //在每次存入缓存的时候调用
                return value.getByteCount();
            }
        };
    }
    //增加到缓存
    public void addBitmapToCache(String url,Bitmap bitmap){
        if(getBitmapFromCache(url)==null){//校验缓存中是否存在
            mCaches.put(url,bitmap);
        }

    }
    //从缓存中获取数据
    public Bitmap getBitmapFromCache(String url){
        return mCaches.get(url);
    }


    private Handler mHandler=new Handler() {
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(mImageView.getTag().equals(mUrl))//避免缓存的图片对正确的图片产生影响
                mImageView.setImageBitmap((Bitmap) msg.obj);
        }
    };


    public void showImageByThread(ImageView ImageView, final String url){
        mImageView=ImageView;
        mUrl=url;
        new Thread(){
            public void run(){
                super.run();
                Bitmap bitmap=getBitmapFromUrl(url);
                Message message= Message.obtain();
                message.obj=bitmap;
                mHandler.sendMessage(message);
            }

        }.start();
    }


    public Bitmap getBitmapFromUrl(String path){
        Bitmap bm=null;
        try{
            URL url=new URL(path);
            URLConnection connection=url.openConnection();
            connection.connect();
            InputStream inputStream=connection.getInputStream();
            bm= BitmapFactory.decodeStream(inputStream);
            /*HttpURLConnection connection=(HttpURLConnection)url.openConnection();
            InputStream is=new BufferedInputStream(connection.getInputStream());
            bm= BitmapFactory.decodeStream(is);
            connection.disconnect();*/
            //return bm;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bm;
    }






    public void showImageByAsyncTask(ImageView imageView,final String url){
        //判断缓存中是否存在该图片
        //从缓存中取出对应的图片
        Bitmap bitmap=getBitmapFromCache(url);
        //如果缓存中没有则去网上下载
        if(bitmap==null)
           new NewAsyncTask(imageView,url).execute(url);

        else
            imageView.setImageBitmap(bitmap);
        //new NewAsyncTask(imageView,url).execute(url);
    }


    private class NewAsyncTask extends AsyncTask<String,Void,Bitmap>{

        private ImageView mImageView;
        private String mUrl;

        public NewAsyncTask(ImageView imageView,String url) {
            mImageView=imageView;
            mUrl=url;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(mImageView.getTag().equals(mUrl))
                mImageView.setImageBitmap(bitmap);

        }

        @Override
        protected Bitmap doInBackground(String... params) {
            //return getBitmapFromUrl(params[0]);//没有缓存机制的写法
            String url=params[0];
            //从网络获取图片
            Bitmap bitmap=getBitmapFromUrl(url);
            if(bitmap!=null)
                //将不在缓存的图片加入缓存
                addBitmapToCache(url,bitmap);
            return bitmap;
        }
    }


}