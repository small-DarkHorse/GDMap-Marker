# GDMap-Marker
# Android高德地图中根据缩放级别显示Marker
给大家分享三种清除地图Marker的方式，后面还有完整的例子哦~

效果图如下：
![隐蔽marker](https://img-blog.csdnimg.cn/20190820153615949.gif)
**方法一：清除地图上所有的Marker** 
说明：这种方式清除marker可能会出现设置了icons模拟gif动画的marker显示不了，这时候就需要用第二种方法了。

AMap aMap = mapView.getMap();
aMap.clear();

**方法二：添加，删除指定的Marker,这样就不用清除地图上所有的Marker了 
说明：如果数据量大，频繁地add，remove marker刷新地图，可能会造成主线程阻塞，出现ANR；**

//添加Marker,需要指定title，要不然可能会出现显示不了Marker的情况
MarkerOptions options = new MarkerOptions();
options.title("xxx").position(xxx).icon(xxx);
Marker marker = aMap.addMarker(options);
//这行关键，标记Marker的类型xxx
marker.setObject(xxx);

//删除指定Marker
private void clearMarkers() {
        //获取地图上所有Marker
        List<Marker> mapScreenMarkers = aMap.getMapScreenMarkers();
        for (int i = 0; i < mapScreenMarkers.size(); i++) {
            Marker marker = mapScreenMarkers.get(i);
            if (marker.getObject() instanceof xxx) {
                marker.remove();//移除当前Marker
            }
        }
        aMap.invalidate();//刷新地图
}

**方法三，利用集合分类管理marker,通过visiable设置marker显示或则隐藏**

 Arraylist<Marker> list = new Arraylist ();
 MarkerOptions options = new MarkerOptions();
 options.title("xxx").position(xxx).icon(xxx);
 Marker marker = aMap.addMarker(options);
 marker.setObject(xxx);
 list.add(marker);

 for(int i=0;i<list.size();i++){
     Marker marker = list.get(i);
     marker.setvisiable(xxx);
 }
 
 ***接下来是一个根据地图缩放级别显示Marker的案例：***
 

 1. 首先在项目中导入地图用到的jar包，这里我放在了libs文件夹下面 

 ![在这里插入图片描述](https://img-blog.csdnimg.cn/20190820141550384.png)
 添加依赖：
 ```java
  implementation files('libs/AMap3DMap_6.9.2_AMapSearch_6.9.2_20190709.jar')
  implementation files('libs/AMap_Location_V4.7.0_20190708.jar')
  ```
 
2. MakeMarker.java中已经封装好了初始化地图的Marker，随机设置在不同的地点

```java
package com.example.clearmapmarker;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MakeMarker {

    //建筑物图片的路径
    public static int[] buildings_bitmap = new int[]{
            R.drawable.map_buildings,
            R.drawable.map_buildings,
            R.drawable.map_buildings,
            R.drawable.map_buildings};

    /**
     * 地图上添加建筑物marker
     * **/
    public static List<Marker> addMarker_buildings(AMap aMap) {
        List<Marker> lists;
        final ArrayList<MarkerOptions> markerOptions_buildings = new ArrayList<MarkerOptions>();
        final double[] coordinatearr={34.741612,113.642578,
                34.343436,108.984375,
                30.619005,114.301758,
                30.600094,104.150391

        };
        String[] buildings={"郑州","西安","武汉","成都"};

        ArrayList<BitmapDescriptor> bitmaps=new ArrayList<BitmapDescriptor>();

        for(int i=0;i<4;i++){
            BitmapDescriptor bitmap = BitmapDescriptorFactory//构建Marker图标_现教A
                    .fromResource(buildings_bitmap[i]);
            bitmaps.add(bitmap);
        }

        for (int j=1;j<=4;j++) {

                double longitude=coordinatearr[2*j-2];
                double latitude=coordinatearr[2*j-1];
                MarkerOptions markerOption = new MarkerOptions();
                markerOption.position(new LatLng(longitude, latitude));
                markerOption.title(buildings[j-1]);
                markerOption.draggable(true);
                markerOption.snippet("当前附近区域有["+j+"]个加油站");
                markerOption.icon(bitmaps.get(j - 1));
                markerOption.zIndex(-1);
                markerOptions_buildings.add(markerOption);
        }

        lists=aMap.addMarkers(markerOptions_buildings, true);
        return lists;
    }

    /**
     * func:设置地图上所有marker是否显示
     * */
    public static void Buildings_MarkVisible(Boolean bool,List<Marker> mList_buildings){
        //获取地图上所有的建筑物marker设置是否显示
        for(int i = 0; i< mList_buildings.size(); i++){
            Marker marker = mList_buildings.get(i);
            marker.setVisible(bool);
        }
    }
}

```
3. 设置地图上所有marker是否显示

```java
/**
     * func:设置地图上所有marker是否显示
     * */
    public static void Buildings_MarkVisible(Boolean bool,List<Marker> mList_buildings){
        //获取地图上所有的建筑物marker设置是否显示
        for(int i = 0; i< mList_buildings.size(); i++){
            Marker marker = mList_buildings.get(i);
            marker.setVisible(bool);
        }
    }
```
4. Mainactivity.java中当缩放级别改变时候决定Marker是否出现

```java
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
```
