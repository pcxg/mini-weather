package com.example.administrator.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

//网络状态检查
public class NetUtil {
    public static final int NET_NONE=0;
    public static final int NET_WIFI=1;
    public static final int NET_MOBILE=2;

    public static int getNetworkState(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null){
            return NET_NONE;
        }
        int nType = networkInfo.getType();
        if(nType == ConnectivityManager.TYPE_MOBILE){
            return NET_MOBILE;
        }
        else if(nType == ConnectivityManager.TYPE_WIFI){
            return NET_WIFI;
        }
        return NET_NONE;
    }

}
