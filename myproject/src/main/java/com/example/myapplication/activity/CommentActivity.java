package com.example.myapplication.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.adapter.CommentAdapter;
import com.example.myapplication.bean.Comment;
import com.example.myapplication.bean.MyUser;
import com.example.myapplication.bean.Post;
import com.example.myapplication.constant.Constant;
import com.example.myapplication.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class CommentActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView commentList;
    private TextView loadmore;
    private CommentAdapter mAdapter;
    private List<Comment> comments = new ArrayList<Comment>();

    private Post post;
    private String commentEdit="";
    private int pageNum;

    private EditText commentContent;
    private Button commentCommit,bt_comment_back;
    private TextView userName;
    private TextView commentItemContent;
    private ImageView userLogo;
    private TextView publicTime;
    private TextView comment;
    private TextView love;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_comment);
        initView();
        setupView();
        setListener();//为点赞、评论设置监听事件
        loadMoreDatas();//加载评论
    }

    private void setListener() {
        loadmore.setOnClickListener(this);
        commentCommit.setOnClickListener(this);
        bt_comment_back.setOnClickListener(this);
    }
    public void onClick(View v){
        switch(v.getId()){
            case R.id.loadmore:
                loadMoreDatas();
                break;
            case R.id.comment_commit:
                onClickCommit();
                break;
            case R.id.item_action_comment:
                onClickComment();
                break;
            case R.id.bt_commentback:
                onClickBack();
                break;
        }
    }

    private void onClickBack() {
        finish();
    }


    //TODO 没起作用，为啥？？？
    private void onClickComment() {
        commentContent.requestFocus();
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(commentContent, 0);
    }

    private void onClickCommit() {
        MyUser user= BmobUser.getCurrentUser(MyUser.class);
        //if (user != null) {// 已登录
            commentEdit = commentContent.getText().toString().trim();
            if (commentEdit.equals("")) {
                Toast.makeText(getApplicationContext(), "评论内容不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
        publishComment(user, commentEdit);

    }
    //private ImageLoader mImageLoader=new ImageLoader();
    private void publishComment(final MyUser user, String content) {
        final Comment comment = new Comment();
        //TODO 将评论和帖子进行关联，并同时和当前用户进行关联，表明是当前用户对该帖子进行评论
        comment.setUser(user);
        comment.setCommentContent(content);
        comment.setPost(post);
        comment.save(new SaveListener() {
            @Override
            public void done(Object o, BmobException e) {
                if(e==null){
                    Toast.makeText(getApplicationContext(),"评论成功",Toast.LENGTH_SHORT).show();
                    if (comments.size() < Constant.NUMBERS_PER_PAGE) {
                        comments.add(comment);
                        mAdapter.notifyDataSetChanged();
                        setListViewHeightBasedOnChildren(commentList);
                    }
                    commentContent.setText("");

                    //控制软键盘的状态
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(commentContent.getWindowToken(), 0);

                    post.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if(e==null){
                                Log.i("bmob","一对多关联添加成功");

                            }else{
                                Log.i("bmob","失败："+e.getMessage());
                            }
                        }
                    });
                }else{
                    Toast.makeText(getApplicationContext(),"评论失败",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void loadMoreDatas() {
        BmobQuery<Comment> query = new BmobQuery<>();//用此方式可以构造一个BmobPointer对象。
        //TODO  addWhereEqualTo对BmobPonter类型的一对多的关联查询
        //TODO 查询出某个帖子的所有评论,同时将该评论的作者的信息也查询出来
        query.addWhereEqualTo("post",new BmobPointer(post));//"post"为评论表的列名
        //希望同时查询该评论的发布者的信息，以及该帖子的作者的信息，这里用到上面`include`的并列对象查询和内嵌对象的查询
        query.include("user");

        query.order("createdAt");
        query.setLimit(Constant.NUMBERS_PER_PAGE);
        query.setSkip(Constant.NUMBERS_PER_PAGE * (pageNum++));//setSkip获取下一页数据

        query.findObjects(new FindListener<Comment>() {

            @Override
            public void done(List<Comment> list, BmobException e) {
                if(e==null){
                    if(list.size() != 0 && list.get(list.size()-1) != null){
                        if (list.size() < Constant.NUMBERS_PER_PAGE) {
                            Toast.makeText(getApplicationContext(), "已加载完所有评论~", Toast.LENGTH_SHORT).show();
                            loadmore.setText("暂无更多评论~");
                        }
                        comments.addAll(list);
                        mAdapter.notifyDataSetChanged();
                        setListViewHeightBasedOnChildren(commentList);//TODO 动态设置listview的高度 item
                    }else{
                        Toast.makeText(getApplicationContext(), "暂无更多评论", Toast.LENGTH_SHORT).show();
                        loadmore.setText("暂无更多评论~");
                        pageNum--;
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "暂无更多评论", Toast.LENGTH_SHORT).show();
                    loadmore.setText("暂无更多评论~");
                    pageNum--;
                }

            }
        });
    }

    private void setupView() {
        /*
        *TODO   SOFT_INPUT_ADJUST_RESIZE整个Layout重新编排,重新分配多余空间
        */
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        post = (Post) getIntent().getSerializableExtra("data");//来自PostAdapter传来的
        pageNum = 0;
        mAdapter=new CommentAdapter(getApplicationContext(),comments);
        commentList.setAdapter(mAdapter);
        setListViewHeightBasedOnChildren(commentList);//动态设置评论列表的高度
        commentList.setCacheColorHint(0);
        commentList.setScrollingCacheEnabled(false);
        commentList.setScrollContainer(false);
        commentList.setFastScrollEnabled(true);
        commentList.setSmoothScrollbarEnabled(true);
        initMoodView(post);
    }
    public  ImageLoader comImageLoader=new ImageLoader();
    private void initMoodView(Post post) {
        if(post==null){
            return;
        }
        if(post!=null){
            MyUser user=post.getAuthor();

            String url=user.getAvatarUrl();
            if(user.getAvatarUrl()==null){
                userLogo.setImageResource(R.drawable.avatar1);
            }
            else {
                userLogo.setTag(url);
                comImageLoader.showImageByAsyncTask(userLogo, url);
            }
            userName.setText(post.getAuthor().getUsername());
            publicTime.setText(post.getCreatedAt());
            commentItemContent.setText(post.getContent());
        }

        love.setText(post.getLove() + "");
        if (post.isMyLove()) {
            love.setTextColor(Color.parseColor("#D95555"));
        } else {
            love.setTextColor(Color.parseColor("#000000"));
        }
    }

    /*
    *TODO 动态设置listview的高度 item 总布局必须是linearLayout
    */
    private void setListViewHeightBasedOnChildren(ListView commentList) {
        ListAdapter listAdapter = commentList.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, commentList);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = commentList.getLayoutParams();
        params.height = totalHeight
                + (commentList.getDividerHeight() * (listAdapter.getCount() - 1))
                + 15;
        commentList.setLayoutParams(params);
    }

    private void initView() {

        bt_comment_back=(Button) findViewById(R.id.bt_commentback);//在这里发现一个坑，居然不可以这样命名R.id.bt_comment_back
        commentList = (ListView) findViewById(R.id.comment_list);
        loadmore = (TextView) findViewById(R.id.loadmore);

        commentContent = (EditText) findViewById(R.id.comment_content);
        commentCommit = (Button) findViewById(R.id.comment_commit);
        userName = (TextView) findViewById(R.id.user_name);
        commentItemContent = (TextView) findViewById(R.id.content_text);
        userLogo = (ImageView) findViewById(R.id.user_logo);
        publicTime = (TextView) findViewById(R.id.item_public_time);
        comment = (TextView) findViewById(R.id.item_action_comment);
        love = (TextView) findViewById(R.id.item_action_love);
    }


}
