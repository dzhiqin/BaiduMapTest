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
	 * ʵ��BaiduMap��
	 */
	private BaiduMap baiduMap;
	/**
	 * ��λ������
	 */
	private LocationManager locationManager;
	/**
	 * ���������
	 */
	private GeoCoder geoCoder;
	private String provider;
	private boolean isFirstLocate=true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//��ʹ��SDK�����֮ǰ��ʼ��context��Ϣ������ApplicationContext  
        //ע��÷���Ҫ��setContentView����֮ǰʵ��  
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		mapView=(MapView)findViewById(R.id.map_view);
		//ʵ����baiduMap
		baiduMap=mapView.getMap();
		//����λ����
		baiduMap.setMyLocationEnabled(true);
		geoCoder=GeoCoder.newInstance();
		//���û�õ���������ļ�����MainActivityʵ����OnGetGeoCodeResultListener()�ӿڣ�
		//����setOnGetGeoCodeResultListener(...)��Ĳ�������ֱ����this
		geoCoder.setOnGetGeoCodeResultListener(this);
		locationManager =(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		//��ȡ���õ�λ���ṩ�����ȼ���Ƿ���GPS��λ���ټ���Ƿ������綨λ
		List<String> providerList=locationManager.getProviders(true);
		if(providerList.contains(LocationManager.GPS_PROVIDER)){
			provider=LocationManager.GPS_PROVIDER;
		}else if(providerList.contains(LocationManager.NETWORK_PROVIDER)){
			provider=LocationManager.NETWORK_PROVIDER;
		}else{//û�����ö�λ��ʽ���˳�
			Toast.makeText(this, "No locationProvider to use", Toast.LENGTH_SHORT).show();
			return;
		}
		//���location��Ϣ
		Location location=locationManager.getLastKnownLocation(provider);		
		if(location!=null){
			//��location��Ϣ��λ����ǰλ��
			navigateTo(location);
		}
		//ΪlocationManager���ö�λ������5000ms��ȡһ�����ݣ��������ƶ�1�׾ʹ���locationListener
		locationManager.requestLocationUpdates(provider, 5000, 1, locationListener);
	}
/**
 * ��location��Ϣ��λ����ǰλ��
 * @param location
 */
	private void navigateTo(Location location){
		if(isFirstLocate){
			//��ð�����γ����Ϣ��LatLng���࣬Ȼ���װΪMapStatusUpdate���ʵ������baiduMap���ʵ��ʹ��
			LatLng latlng=new LatLng(location.getLatitude(),location.getLongitude());
			MapStatusUpdate update=MapStatusUpdateFactory.newLatLng(latlng);
			baiduMap.animateMapStatus(update);
			//���ŵ�ͼ
			update=MapStatusUpdateFactory.zoomTo(16f);
			baiduMap.animateMapStatus(update);
			//��Geo���������ݵ�ǰ��ĵľ�γ����Ϣ�����з���������õ�λ����Ϣ���õ��Ľ���ɼ����¼�����
			geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latlng));
			isFirstLocate=false;
		}
		//�ڵ�ǰ�ĵ�ͼ�ϳ��ֱ�ʾ�ҵ�СԲ�㣬ʵʱ����
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
			// TODO �Զ����ɵķ������			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO �Զ����ɵķ������			
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO �Զ����ɵķ������			
		}		
	};
	/**
	 * ��д�����˳�ʱҪ���ٵ��߼���mapView��geoCoder��Ҫ����
	 * ��λ����Ҫȡ������λ����Ҫȡ��,ʵ�ֵ�ͼ�������ڹ��� 
	 */
	@Override
	protected void onDestroy(){
		super.onDestroy();
		mapView.onDestroy();
		geoCoder.destroy();
		baiduMap.setMyLocationEnabled(false);
		if(locationManager!=null){
			//�رճ���ʱ���������Ƴ�
			locationManager.removeUpdates(locationListener);
		}
	}
	@Override
	protected void onPause(){
		super.onPause();
		//��activityִ��onDestroyʱִ��mMapView.onDestroy()��ʵ�ֵ�ͼ�������ڹ���
		mapView.onPause();
	}
	@Override
	protected void onResume(){
		super.onResume();
		 //��activityִ��onResumeʱִ��mMapView. onResume ()��ʵ�ֵ�ͼ�������ڹ���  
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
 * ����õ��������Ϣʱ��������õ���γ����Ϣ
 */
	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MainActivity.this, "��Ǹ��δ���ҵ����", Toast.LENGTH_LONG).show();					
			return;
		}
		String strInfo = String.format("γ�ȣ�%f ���ȣ�%f",
				result.getLocation().latitude, result.getLocation().longitude);
		Toast.makeText(MainActivity.this, strInfo, Toast.LENGTH_LONG).show();
	}
/**
 * ����÷���������Ľ��ʱ��������õ�λ����Ϣ
 */
	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if(result==null||result.error!=SearchResult.ERRORNO.NO_ERROR){
			Toast.makeText(MainActivity.this, "��Ǹ��δ���ҵ����", Toast.LENGTH_LONG)
			.show();
			return;
		}
		Toast.makeText(MainActivity.this, result.getAddress(),
				Toast.LENGTH_LONG).show();
	}
}
