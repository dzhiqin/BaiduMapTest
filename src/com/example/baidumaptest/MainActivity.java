package com.example.baidumaptest;

import java.util.List;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity implements OnGetGeoCoderResultListener{

	private MapView mapView;
	/**
	 * 实现BaiduMap类
	 */
	private BaiduMap baiduMap;
	/**
	 * 定位管理器
	 */
	private LocationManager locationManager;
	/**
	 * 地理编码器
	 */
	private GeoCoder geoCoder;
	private String provider;
	private boolean isFirstLocate=true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//在使用SDK各组件之前初始化context信息，传入ApplicationContext  
        //注意该方法要再setContentView方法之前实现  
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		mapView=(MapView)findViewById(R.id.map_view);
		//实例化baiduMap
		baiduMap=mapView.getMap();
		//允许定位功能
		baiduMap.setMyLocationEnabled(true);
		geoCoder=GeoCoder.newInstance();
		//设置获得地理编码结果的监听，MainActivity实现了OnGetGeoCodeResultListener()接口，
		//所以setOnGetGeoCodeResultListener(...)里的参数可以直接用this
		geoCoder.setOnGetGeoCodeResultListener(this);
		locationManager =(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		//获取可用的位置提供器，先检查是否用GPS定位，再检查是否用网络定位
		List<String> providerList=locationManager.getProviders(true);
		if(providerList.contains(LocationManager.GPS_PROVIDER)){
			provider=LocationManager.GPS_PROVIDER;
		}else if(providerList.contains(LocationManager.NETWORK_PROVIDER)){
			provider=LocationManager.NETWORK_PROVIDER;
		}else{//没有设置定位方式就退出
			Toast.makeText(this, "No locationProvider to use", Toast.LENGTH_SHORT).show();
			return;
		}
		//获得location信息
		Location location=locationManager.getLastKnownLocation(provider);		
		if(location!=null){
			//由location信息定位到当前位置
			navigateTo(location);
		}
		//为locationManager设置定位监听，5000ms获取一次数据，当距离移动1米就触发locationListener
		locationManager.requestLocationUpdates(provider, 5000, 1, locationListener);
	}
/**
 * 由location信息定位到当前位置
 * @param location
 */
	private void navigateTo(Location location){
		if(isFirstLocate){
			//获得包含经纬度信息的LatLng的类，然后包装为MapStatusUpdate类的实例，给baiduMap这个实例使用
			LatLng latlng=new LatLng(location.getLatitude(),location.getLongitude());
			MapStatusUpdate update=MapStatusUpdateFactory.newLatLng(latlng);
			baiduMap.animateMapStatus(update);
			//缩放地图
			update=MapStatusUpdateFactory.zoomTo(16f);
			baiduMap.animateMapStatus(update);
			//反Geo搜索，根据当前多的的经纬度信息，进行反向地理编码得到位置信息，得到的结果由监听事件处理
			geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latlng));
			isFirstLocate=false;
		}
		//在当前的地图上出现表示我的小圆点，实时更新
		MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
		locationBuilder.latitude(location.getLatitude());
		locationBuilder.longitude(location.getLongitude());
		MyLocationData locationData=locationBuilder.build();
		baiduMap.setMyLocationData(locationData);
	}
	LocationListener locationListener=new LocationListener(){

		@Override
		public void onLocationChanged(Location location) {
			if(location!=null){
				navigateTo(location);
			}			
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO 自动生成的方法存根			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO 自动生成的方法存根			
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO 自动生成的方法存根			
		}		
	};
	/**
	 * 重写返回退出时要销毁的逻辑，mapView，geoCoder都要销毁
	 * 定位功能要取消，定位监听要取消,实现地图生命周期管理 
	 */
	@Override
	protected void onDestroy(){
		super.onDestroy();
		mapView.onDestroy();
		geoCoder.destroy();
		baiduMap.setMyLocationEnabled(false);
		if(locationManager!=null){
			//关闭程序时将监听器移出
			locationManager.removeUpdates(locationListener);
		}
	}
	@Override
	protected void onPause(){
		super.onPause();
		//在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mapView.onPause();
	}
	@Override
	protected void onResume(){
		super.onResume();
		 //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理  
		mapView.onResume();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
/**
 * 当获得地理编码信息时进入这里，得到经纬度信息
 */
	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MainActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG).show();					
			return;
		}
		String strInfo = String.format("纬度：%f 经度：%f",
				result.getLocation().latitude, result.getLocation().longitude);
		Toast.makeText(MainActivity.this, strInfo, Toast.LENGTH_LONG).show();
	}
/**
 * 当获得反向地理编码的结果时进入这里，得到位置信息
 */
	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if(result==null||result.error!=SearchResult.ERRORNO.NO_ERROR){
			Toast.makeText(MainActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
			.show();
			return;
		}
		Toast.makeText(MainActivity.this, result.getAddress(),
				Toast.LENGTH_LONG).show();
	}
}
