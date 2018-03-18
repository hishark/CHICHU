package com.example.myapplication.database;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.myapplication.bean.MyUser;
import com.example.myapplication.bean.Post;

import java.util.Iterator;
import java.util.List;

import cn.bmob.v3.BmobUser;

/**
 * Created by 程洁 on 2017/12/27.
 */

public class DatabaseUtil {
    private static final String TAG = "DatabaseUtil";
    private static DatabaseUtil instance;

    /**
     * 数据库帮助类
     **/
    private DBHelper dbHelper;

    public synchronized static DatabaseUtil getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseUtil(context);
        }
        return instance;
    }

    /**
     * 初始化
     *
     * @param context
     */
    private DatabaseUtil(Context context) {
        dbHelper = new DBHelper(context);
    }

    /**
     * 销毁
     */
    public static void destory() {
        if (instance != null) {
            instance.onDestory();
        }
    }

    /**
     * 销毁
     */
    public void onDestory() {
        instance = null;
        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }
    }
    Context context;
    public boolean isLoved(Post mouse) {
        Cursor cursor = null;

        String where = DBHelper.FavTable.USER_ID + " = '" + DatabaseUtil.getInstance(context).getCurrentUser().getObjectId()
                + "' AND " + DBHelper.FavTable.OBJECT_ID + " = '" + mouse.getObjectId() + "'";
        Log.i(TAG,"------------------------------------"+where);
        cursor = dbHelper.query(DBHelper.TABLE_NAME, null, where, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            if (cursor.getInt(cursor.getColumnIndex(DBHelper.FavTable.IS_LOVE)) == 1) {
                Log.i(TAG,"------------------------------------"+"到这里了");
                return true;
            }
        }
        return false;
    }


    public MyUser getCurrentUser() {
        MyUser user = BmobUser.getCurrentUser(MyUser.class);
        if(user!=null){
            return user;
        }
        return null;
    }
}
