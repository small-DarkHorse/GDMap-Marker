package com.example.clearmapmarker;

import android.content.Context;
import android.util.Log;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MapStyleSetting {

    /**
     * create by 智辉:2019/8/20
     * func:地图样式变化;
     *      地图缩放动画的效果;
     * **/

    /**
     * 切换地图样式文件
     * */
    public static void setMapCustomStyleFile(Context context, AMap aMap) {
        String styleName = "style.data";
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        String filePath = null;
        try {
            inputStream = context.getAssets().open(styleName);
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);

            filePath = context.getFilesDir().getAbsolutePath();
            File file = new File(filePath + "/" + styleName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            outputStream = new FileOutputStream(file);
            outputStream.write(b);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();

                if (outputStream != null)
                    outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i("MapStyleSetting", "setMapCustomStyleFile_307: "+filePath+styleName);
        aMap.setCustomMapStylePath(filePath);

        /*aMap.setMapCustomEnable(true);
        aMap.showMapText(true);*/
        //该方法在AMap类中提供
        // aMap.setCustomMapStylePath("/sdcard/custom_config");

    }

    /**
     * 地图缩放按钮，动画效果
     * */
    public static void ZoomAnimation(CameraUpdate update, AMap.CancelableCallback callback, AMap aMap){
        aMap.animateCamera(update, 1000, callback);
    }

}
