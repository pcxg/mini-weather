package com.example.administrator.miniweather;

import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;

import static android.content.ContentValues.TAG;

public class MyLocationListener extends BDAbstractLocationListener {



    private String addr;
    private String cityName;
    @Override
    public void onReceiveLocation(BDLocation location){
        //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
        //以下只列举部分获取经纬度相关（常用）的结果信息



        addr = location.getAddrStr();
        cityName = location.getCity().replace("市","");
        int error = location.getLocType();
        Log.d(TAG, "ADDR: "+addr+"   error:"+error+"   city:"+cityName);
    }
    public String getAddr() {
        return addr;
    }
    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
