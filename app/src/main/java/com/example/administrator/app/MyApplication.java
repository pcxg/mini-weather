package com.example.administrator.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.example.administrator.bean.City;
import com.example.administrator.db.CityDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    private static MyApplication myApplication;
    private CityDB mCityDB;
    private List<City> mCityList;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "CreateApplication");

        myApplication = this;
        mCityDB = openCityDB();
        initCityList();
    }

    private void initCityList() {
        mCityList = new ArrayList<City>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareCityList();
            }
        }).start();
    }

    private boolean prepareCityList() {
        mCityList = mCityDB.getAllCity();
        int i = 0;
        for(City city: mCityList ){
            i++;
            String cityName = city.getCity();
            String cityCode = city.getNumber();
            String py = city.getAllPY();
            //Log.d(TAG, "prepareCityList: "+py);
            //Log.d(TAG, "prepareCityList: "+cityCode+":"+cityName);
        }
        Log.d(TAG, "prepareCityList: "+i);
        return true;
    }

    public static MyApplication getInstance(){
        return myApplication;
    }

    //打开数据库文件存放位置，若不存在则新建，将city.db内容复制到指定位置
    private CityDB openCityDB(){
        String path = "/data"+Environment.getDataDirectory().getAbsolutePath()
                +File.separator+getPackageName()
                +File.separator+"databases1"
                +File.separator+CityDB.CITY_DB_NAME;
        File db = new File(path);

        Log.d(TAG, "openCityDB: "+path);
        if(!db.exists()){
            String pathfolder = "/data"+Environment.getDataDirectory().getAbsolutePath()
                    +File.separator+getPackageName()
                    +File.separator+"databases1"
                    +File.separator;
            File dirFirstFolder = new File(pathfolder);
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdir();
                Log.i(TAG, "mkdirs");
            }
            Log.i(TAG, "db isnt exist");
            try {
                InputStream is = getAssets().open("city.db");
                FileOutputStream fos = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while((len = is.read(buffer))!= -1){
                    fos.write(buffer,0,len);
                    fos.flush();
                }
                fos.close();
            }catch (IOException e){
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this,path);
    }

    public List<City> getmCityList() {
        return mCityList;
    }
}
