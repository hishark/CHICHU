package com.example.myapplication.map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.myapplication.R;
import com.example.myapplication.activity.ShowWalkInfoActivity;
import com.example.myapplication.activity.StepCounterActivity;
import com.example.myapplication.bean.MyUser;
import com.example.myapplication.bean.WalkInfo;
import com.example.myapplication.service.StepCounterService;
import com.example.myapplication.utils.StepDetector;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 此demo实现时时动态画运动轨迹
 * author zhh
 */
public class DynamicDemo extends Activity implements SensorEventListener {

	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private int mCurrentDirection = 0;
	private double mCurrentLat = 0.0;
	private double mCurrentLon = 0.0;

	MapView mMapView;
	BaiduMap mBaiduMap;

	private TextView info;
	private RelativeLayout progressBarRl;

	boolean isFirstLoc = true; // 是否首次定位
	private MyLocationData locData;
	float mCurrentZoom = 18f;//默认地图缩放比例值

	private SensorManager mSensorManager;

	//起点图标
	BitmapDescriptor startBD = BitmapDescriptorFactory.fromResource(R.drawable.qidian28);
	//终点图标
	BitmapDescriptor finishBD = BitmapDescriptorFactory.fromResource(R.drawable.zhongdian28);

	List<LatLng> points = new ArrayList<LatLng>();//位置点集合
	Polyline mPolyline;//运动轨迹图层
	LatLng last = new LatLng(0, 0);//上一个定位点
	MapStatus.Builder builder;

	//------------------计步服务要用到的东西-----------------------
	private long timer = 0;// 运动时间
	private  long startTimer = 0;// 开始时间

	private  long tempTime = 0;

	private Double distance = 0.0;// 路程：米
	private Double calories = 0.0;// 热量：卡路里
	private Double velocity = 0.0;// 速度：米每秒

	private int step_length = 0;  //步长
	private int weight = 0;       //体重
	private int total_step = 0;   //走的总步数

	private Thread thread;  //定义线程对象

	// 当创建一个新的Handler实例时, 它会绑定到当前线程和消息的队列中,开始分发数据
	// Handler有两个作用, (1) : 定时执行Message和Runnalbe 对象
	// (2): 让一个动作,在不同的线程中执行.

	Handler handler = new Handler() {
		// Handler对象用于更新当前步数,定时发送消息，调用方法查询数据用于显示？？？？？？？？？？
		//主要接受子线程发送的数据, 并用此数据配合主线程更新UI
		//Handler运行在主线程中(UI线程中), 它与子线程可以通过Message对象来传递数据,
		//Handler就承担着接受子线程传过来的(子线程用sendMessage()方法传递Message对象，(里面包含数据)
		//把这些消息放入主线程队列中，配合主线程进行更新UI。

		@Override                  //这个方法是从父类/接口 继承过来的，需要重写一次
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);        // 此处可以更新UI

			countDistance();     //调用距离方法，看一下走了多远

			if (timer != 0 && distance != 0.0) {

				// 体重、距离
				// 跑步热量（kcal）＝体重（kg）×距离（公里）×1.036
				calories = weight * distance * 0.001;
				//速度velocity
				velocity = distance * 1000 / timer;
			} else {
				calories = 0.0;
				velocity = 0.0;
			}

			countStep();          //调用步数方法


		}



	};






	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dyn);

		//初始化数据1
		init1();

		//初始化数据2
		init();


		//--------------计步器要用的---------
		if (thread == null) {

			thread = new Thread() {// 子线程用于监听当前步数的变化

				@Override
				public void run() {
					// TODO Auto-generated method stub
					super.run();
					int temp = 0;
					while (true) {
						try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (StepCounterService.FLAG) {
							Message msg = new Message();
							if (temp != StepDetector.CURRENT_SETP) {
								temp = StepDetector.CURRENT_SETP;
							}
							if (startTimer != System.currentTimeMillis()) {
								timer = tempTime + System.currentTimeMillis()
										- startTimer;
							}
							handler.sendMessage(msg);// 通知主线程
						}
					}
				}
			};
			thread.start();
		}



		//-------------------------------------------------



		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);// 获取传感器管理服务

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);

		mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
				com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.FOLLOWING, true, null));

		/**
		 * 添加地图缩放状态变化监听，当手动放大或缩小地图时，拿到缩放后的比例，然后获取到下次定位，
		 *  给地图重新设置缩放比例，否则地图会重新回到默认的mCurrentZoom缩放比例
		 */
		mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {

			@Override
			public void onMapStatusChangeStart(MapStatus arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMapStatusChangeFinish(MapStatus arg0) {
				mCurrentZoom = arg0.zoom;
			}

			@Override
			public void onMapStatusChange(MapStatus arg0) {
				// TODO Auto-generated method stub

			}
		});

		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);//只用gps定位，需要在室外定位。
		option.setOpenGps(true); // 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);

	}

	private void initView() {

		Button start = (Button) findViewById(R.id.buttonStart);
		Button finish = (Button) findViewById(R.id.buttonFinish);
        Button watch = (Button) findViewById(R.id.buttonWatch);
		info = (TextView) findViewById(R.id.info);
		progressBarRl = (RelativeLayout) findViewById(R.id.progressBarRl);
		//实例化一个intent用来启动实时计步服务
		final Intent service = new Intent(this, StepCounterService.class);


		/**
		 * 开始按钮的点击事件
		 * 点击开始立马开启计步服务，并且开始gps定位
		 *
		 */
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				startService(service);
				startTimer = System.currentTimeMillis();
				tempTime = timer;

				if (mLocClient != null && !mLocClient.isStarted()) {



					mLocClient.start();
					progressBarRl.setVisibility(View.VISIBLE);
					info.setText("GPS信号搜索中，请稍后...");
					mBaiduMap.clear();
				}
			}
		});

		/**
		 * 结束按钮的点击事件
		 * 点击结束，总行走次数加一
		 * 传数据的事情交给查看按钮来做
		 */
		finish.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {




				if (mLocClient != null && mLocClient.isStarted()) {
					stopService(service);
					//----------------------计步器-------------------------------------------------
					Toast.makeText(getApplicationContext(), String.valueOf(total_step), Toast.LENGTH_SHORT).show();
					//更新WalkInfo表
					upgradeWalkInfo();

					//------------------------------------------------------------------------------



					mLocClient.stop();

					progressBarRl.setVisibility(View.GONE);

					if (isFirstLoc) {
						points.clear();
						last = new LatLng(0, 0);
						return;
					}

					MarkerOptions oFinish = new MarkerOptions();// 地图标记覆盖物参数配置类
					oFinish.position(points.get(points.size() - 1));
					oFinish.icon(finishBD);// 设置覆盖物图片
					mBaiduMap.addOverlay(oFinish); // 在地图上添加此图层

					//复位
					points.clear();
					last = new LatLng(0, 0);
					isFirstLoc = true;

				}
			}
		});

		watch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				//String str="行走距离："+formatDouble(distance)+"卡路里"+formatDouble(calories)+"速度"+formatDouble(velocity);
				//Toast.makeText(getApplicationContext(),str, Toast.LENGTH_LONG).show();
				String walksteps = String.valueOf(total_step);
				String walktime = getFormatTime(timer);
				String walkdistance = formatDouble(distance/1000);
				String walkcalories = formatDouble(calories);
				String walkvelocity = formatDouble(velocity);



				Intent intent = new Intent(DynamicDemo.this,ShowWalkInfoActivity.class);
				intent.putExtra("WalkSteps",walksteps);
				intent.putExtra("WalkTime",walktime);
				intent.putExtra("Distance",walkdistance);
				intent.putExtra("Calories",walkcalories);
				intent.putExtra("Velocity",walkvelocity);


				startActivity(intent);
				overridePendingTransition(R.animator.in_from_left, R.animator.out_to_right);
				finish();
			}
		});

	}

	double lastX;

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		double x = sensorEvent.values[SensorManager.DATA_X];

		if (Math.abs(x - lastX) > 1.0) {
			mCurrentDirection = (int) x;

			if (isFirstLoc) {
				lastX = x;
				return;
			}

			locData = new MyLocationData.Builder().accuracy(0)
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(mCurrentDirection).latitude(mCurrentLat).longitude(mCurrentLon).build();
			mBaiduMap.setMyLocationData(locData);
		}
		lastX = x;

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(final BDLocation location) {

			if (location == null || mMapView == null) {
				return;
			}

			//注意这里只接受gps点，需要在室外定位。
			if (location.getLocType() == BDLocation.TypeGpsLocation) {

				info.setText("GPS信号弱，请稍后...");

				if (isFirstLoc) {//首次定位
					//第一个点很重要，决定了轨迹的效果，gps刚开始返回的一些点精度不高，尽量选一个精度相对较高的起始点
					LatLng ll = null;

					ll = getMostAccuracyLocation(location);
					if(ll == null){
						return;
					}
					isFirstLoc = false;
					points.add(ll);//加入集合
					last = ll;

					//显示当前定位点，缩放地图
					locateAndZoom(location, ll);

					//标记起点图层位置
					MarkerOptions oStart = new MarkerOptions();// 地图标记覆盖物参数配置类
					oStart.position(points.get(0));// 覆盖物位置点，第一个点为起点
					oStart.icon(startBD);// 设置覆盖物图片
					mBaiduMap.addOverlay(oStart); // 在地图上添加此图层

				    progressBarRl.setVisibility(View.GONE);

					return;//画轨迹最少得2个点，首地定位到这里就可以返回了
				}

				//从第二个点开始
				LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
				//sdk回调gps位置的频率是1秒1个，位置点太近动态画在图上不是很明显，可以设置点之间距离大于为5米才添加到集合中
				if (DistanceUtil.getDistance(last, ll) < 5) {
					return;
				}

				points.add(ll);//如果要运动完成后画整个轨迹，位置点都在这个集合中

				last = ll;

				//显示当前定位点，缩放地图
				locateAndZoom(location, ll);

				//清除上一次轨迹，避免重叠绘画
				mMapView.getMap().clear();

				//起始点图层也会被清除，重新绘画
				MarkerOptions oStart = new MarkerOptions();
				oStart.position(points.get(0));
				oStart.icon(startBD);
				mBaiduMap.addOverlay(oStart);

				//将points集合中的点绘制轨迹线条图层，显示在地图上
				OverlayOptions ooPolyline = new PolylineOptions().width(13).color(0xFF8FBC8F).points(points);
				mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
			}
		}

	}

	private void locateAndZoom(final BDLocation location, LatLng ll) {
		mCurrentLat = location.getLatitude();
		mCurrentLon = location.getLongitude();
		locData = new MyLocationData.Builder().accuracy(0)
				// 此处设置开发者获取到的方向信息，顺时针0-360
				.direction(mCurrentDirection).latitude(location.getLatitude())
				.longitude(location.getLongitude()).build();
		mBaiduMap.setMyLocationData(locData);

		builder = new MapStatus.Builder();
		builder.target(ll).zoom(mCurrentZoom);
		mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
	}

	/**
	 * 首次定位很重要，选一个精度相对较高的起始点
	 * 注意：如果一直显示gps信号弱，说明过滤的标准过高了，
	 你可以将location.getRadius()>25中的过滤半径调大，比如>40，
	 并且将连续5个点之间的距离DistanceUtil.getDistance(last, ll ) > 5也调大一点，比如>10，
	 这里不是固定死的，你可以根据你的需求调整，如果你的轨迹刚开始效果不是很好，你可以将半径调小，两点之间距离也调小，
	 gps的精度半径一般是10-50米
	 */
	private LatLng getMostAccuracyLocation(BDLocation location){

		if (location.getRadius()>40) {//gps位置精度大于40米的点直接弃用
			return null;
		}

		LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());

		if (DistanceUtil.getDistance(last, ll ) > 10) {
			last = ll;
			points.clear();//有任意连续两点位置大于10，重新取点
			return null;
		}
		points.add(ll);
		last = ll;
		//有5个连续的点之间的距离小于10，认为gps已稳定，以最新的点为起始点
		if(points.size() >= 5){
			points.clear();
			return ll;
		}
		return null;
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
		// 为系统的方向传感器注册监听器
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_UI);

		//下面两个init方法放到oncreate去
		//init1();

		//初始化数据
		//init();

		//三个按钮的初始化以及点击事件
		initView();


	}

	private void init1() {
		Intent service = new Intent(this, StepCounterService.class);
		stopService(service);
		StepDetector.CURRENT_SETP = 0;
		tempTime = timer = 0;
		handler.removeCallbacks(thread);
	}

	@Override
	protected void onStop() {
		// 取消注册传感器监听
		mSensorManager.unregisterListener(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mLocClient.unRegisterLocationListener(myListener);
		if (mLocClient != null && mLocClient.isStarted()) {
			mLocClient.stop();
		}
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.getMap().clear();
		mMapView.onDestroy();
		mMapView = null;
		startBD.recycle();
		finishBD.recycle();
		super.onDestroy();
	}


	//-------------计步器------------------
	/**
	 * 计算行走的距离
	 */
	private void countDistance() {
		if (StepDetector.CURRENT_SETP % 2 == 0) {
			distance = (StepDetector.CURRENT_SETP / 2) * 3 * step_length * 0.01;
		} else {
			distance = ((StepDetector.CURRENT_SETP / 2) * 3 + 1) * step_length * 0.01;
		}
	}

	/**
	 * 实际的步数
	 */
	private void countStep() {
		if (StepDetector.CURRENT_SETP % 2 == 0) {
			total_step = StepDetector.CURRENT_SETP;
		} else {
			total_step = StepDetector.CURRENT_SETP +1;
		}
		total_step = StepDetector.CURRENT_SETP;
	}


	private void upgradeWalkInfo() {
		MyUser currentuser2= BmobUser.getCurrentUser(MyUser.class);
		BmobQuery<WalkInfo> query = new BmobQuery<WalkInfo>();

		//查询WalkInfo表中user为currentuser2的数据记录。
		query.addWhereEqualTo("user",currentuser2);

		//执行查询方法
		query.findObjects(new FindListener<WalkInfo>() {
			@Override
			public void done(List<WalkInfo> list, BmobException e) {
				if(e==null){
					//找到了当前用户的记录 直接加一
					WalkInfo walkinfo = new WalkInfo();
					walkinfo.setWalkTotalNum(list.get(0).getWalkTotalNum()+1);
					walkinfo.update(list.get(0).getObjectId(), new UpdateListener() {
						@Override
						public void done(BmobException e) {
							if(e==null){

							}
						}
					});
				}else{

				}
			}
		});



	}


	/**
	 * 计算并格式化doubles数值，保留两位有效数字
	 *
	 * @param doubles
	 * @return 返回当前路程
	 */
	@TargetApi(24)
	private String formatDouble(Double doubles) {
		DecimalFormat format = new DecimalFormat("####.##");
		String distanceStr = format.format(doubles);
		return distanceStr.equals("0") ? "0.0"
				: distanceStr;
	}


	/**
	 * 得到一个格式化的时间
	 *
	 * @param time
	 *            时间 毫秒
	 * @return 时：分：秒：毫秒
	 */
	private String getFormatTime(long time) {
		time = time / 1000;

		long second = time % 60;
		long minute = (time % 3600) / 60;
		long hour = time / 3600;


		// 秒显示两位
		String strSecond = ("00" + second)
				.substring(("00" + second).length() - 2);
		// 分显示两位
		String strMinute = ("00" + minute)
				.substring(("00" + minute).length() - 2);
		// 时显示两位
		String strHour = ("00" + hour).substring(("00" + hour).length() - 2);

		return strHour + ":" + strMinute + ":" + strSecond;

	}

	/**
	 * 初始化界面
	 */
	private void init() {


		step_length = 70;
		weight = 50;

		countDistance();
		countStep();

		if ((timer += tempTime) != 0 && distance != 0.0) {
			//tempTime记录运动的总时间，timer记录每次运动时间
			// 体重、距离
			// 跑步热量（kcal）＝体重（kg）×距离（公里）×1.036，换算一下
			calories = weight * distance * 0.001;

			velocity = distance * 1000 / timer;
		} else {
			calories = 0.0;
			velocity = 0.0;
		}



	}

}
