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
