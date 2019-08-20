package com.example.clearmapmarker;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationSource, AMapLocationListener, AMap.OnMarkerClickListener {

    /**
     * 高德地图
     * */
    private MapView mMapView;//高德地图的控件
    private AMap aMap;//高德地图对象

    /**
     * 定位需要声明的全局变量
     */
    private AMapLocationClient mLocationClient = null;//定位发起端
    private AMapLocationClientOption mLocationOption = null;//定位参数
    private LocationSource.OnLocationChangedListener mListener = null;//定位监听器
    public boolean isFirstLoc=true;//用于判断是否首次定位

    /**
     * 标识，用于判断是否只显示一次定位信息和用户重新定位*/
    private LatLng latLng;//用户自己的定位位置Latlng
    private boolean followMove=true;//判断用户是否第一次定位
    private BitmapDescriptor my_view;//自己的头像

    private List<Marker> mList_buildings =new ArrayList<Marker>();//地图上自定义的建筑物Marker

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView)findViewById(R.id.map); //获取地图控件引用
        mMapView.onCreate(savedInstanceState);  //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图

        //手机相关开启权限
        if (PhonePermission.phonepermission(getApplicationContext())!=null) {
            ActivityCompat.requestPermissions(this,PhonePermission.phonepermission(getApplicationContext()), 1);
        } else {
            initMap();//初始化高德地图
            initClick();//初始化点击事件
        }
}
    /**
     * 首次安装app时候，手动开启权限
     * */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能进入", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    initMap();//初始化高德地图
                    initClick();//初始化点击事件
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    /**
     * func:初始化AMap对象;
     */
    private void initMap() {

        mMapView = (MapView)findViewById(R.id.map);//显示地图，获取地图控件
        aMap = mMapView.getMap(); //获取地图对象
        aMap.setLocationSource(this);//设置定位监听
        aMap.setMyLocationEnabled(true);//设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

        UiSettings settings = aMap.getUiSettings();//地图控件的设置
        settings.setMyLocationButtonEnabled(false);// 是否显示定位按钮
        settings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_BUTTOM);//设置缩放按钮位置
        settings.setZoomControlsEnabled(false);//是否显示地图自带的缩放按钮

        MyLocationStyle myLocationStyle = new MyLocationStyle();//小蓝点的精度圈样式
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.location_myself));//替换小蓝点定位的图标
        myLocationStyle.radiusFillColor(android.R.color.transparent);//小蓝点的精度圈填充的颜色
        myLocationStyle.strokeColor(android.R.color.transparent);//小蓝点的精度圈颜色
        myLocationStyle.showMyLocation(false);//设置是否显示定位小蓝点，用于满足只想使用定位，不想使用定位小蓝点的场景，设置false以后图面上不再有定位蓝点的概念，但是会持续回调位置信息。
        aMap.setMyLocationStyle(myLocationStyle);//将设置好的样式文件运用在地图对象中

        AMap.OnMyLocationChangeListener myLocationChangeListener= new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                latLng =new LatLng(latitude,longitude);
                if(followMove){
                    aMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        };
        aMap.setOnMyLocationChangeListener(myLocationChangeListener);
        aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                followMove =false;//用户拖动地图后，不再跟随移动，直到用户点击定位按钮
            }
        });

        initLocation();//开始定位
        initSetMarker();//初始化地图上的Marker

    }

    /**
     *开始定位
     * */
    private void initLocation(){

        mLocationClient = new AMapLocationClient(getApplicationContext()); //初始化定位
        mLocationClient.setLocationListener(this); //设置定位回调监听
        mLocationOption = new AMapLocationClientOption(); //初始化定位参数
        //设置定位模式Hight_Accuracy为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setNeedAddress(true);  //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setOnceLocation(false);//设置是否只定位一次,默认为false
        mLocationOption.setWifiActiveScan(true);//设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setMockEnable(false); //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setInterval(2000); //设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
        mLocationClient.startLocation();//启动定位
    }

    /**
     * 地图上初始化Marker,当缩放级别改变时候marker决定marker是否出现
     * */
    public void  initSetMarker() {
        aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
        mList_buildings=MakeMarker.addMarker_buildings(aMap);// 往地图上添加建筑物的Marker

        //当缩放级别改变时候marker决定marker是否出现
        //aMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                //地图状态改变过程中，调用
            }
            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                float zoom = aMap.getCameraPosition().zoom;
                if(zoom<5){
                    MakeMarker.Buildings_MarkVisible(false,mList_buildings);//不显示
                }
                if(zoom>=5){
                    MakeMarker.Buildings_MarkVisible(true,mList_buildings);//显示
                }
            }
        });
    }

    /**
     * func:初始化oneFragment页面中的控件
     * */
    public void initClick(){
        ImageView img_location = (ImageView)findViewById(R.id.img_location);//定位图标
        img_location.setOnClickListener(this);

        Button zoomIn = (Button)findViewById(R.id.zoom_in);//缩小按钮
        zoomIn.setOnClickListener(this);

        Button zoomOut = (Button)findViewById(R.id.zoom_out);//放大按钮
        zoomOut.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_location://定位图标
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
                aMap.moveCamera(CameraUpdateFactory.zoomTo(6));
                aMap.moveCamera(CameraUpdateFactory.changeTilt(60));
                break;
            case R.id.zoom_in://缩小图标
                MapStyleSetting.ZoomAnimation(CameraUpdateFactory.zoomIn(), null,aMap);
                if(aMap.getCameraPosition().zoom>=5) {
                    MakeMarker.Buildings_MarkVisible(true,mList_buildings);
                }
                break;
            case R.id.zoom_out://放大图标
                MapStyleSetting.ZoomAnimation(CameraUpdateFactory.zoomOut(), null,aMap);
                if(aMap.getCameraPosition().zoom<=5) {
                    MakeMarker.Buildings_MarkVisible(false,mList_buildings);
                }
                break;
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(getApplicationContext());
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mLocationOption.setOnceLocation(true);//只定位一次
            mLocationOption.setHttpTimeOut(2000);
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();//开始定位
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            //定位成功回调信息，设置相关消息
            if (amapLocation.getErrorCode() == 0) {

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(amapLocation.getTime());
                df.format(date);//定位时间

                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    isFirstLoc = false;//定位后置位false
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(6));//设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.changeTilt(60));//设置旋转角度
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude())));//将地图移动到定位点
                    mListener.onLocationChanged(amapLocation);//点击定位按钮 能够将地图的中心移动到定位点

                    StringBuffer buffer = new StringBuffer(); //获取定位信息
                    buffer.append(amapLocation.getCountry() + "" + amapLocation.getProvince() + "" + amapLocation.getCity() + "" + amapLocation.getProvince() + "" + amapLocation.getDistrict() + "" + amapLocation.getStreet() + "" + amapLocation.getStreetNum());
                    Toast.makeText(MainActivity.this, buffer.toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Toast.makeText(MainActivity.this, "定位失败,错误码:["+amapLocation.getErrorCode()+"]"+amapLocation.getErrorInfo(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
