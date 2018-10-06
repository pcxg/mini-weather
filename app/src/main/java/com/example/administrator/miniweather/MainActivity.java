package com.example.administrator.miniweather;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.administrator.util.NetUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity implements View.OnClickListener{

    private ImageView mUpdateBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        mUpdateBtn = (ImageView)findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        /**
         * 判断网络是否连接
         */

        if(NetUtil.getNetworkState(this) != NetUtil.NET_NONE){
            Log.d("miniWeather","OK");
            Toast.makeText(MainActivity.this,"OK",Toast.LENGTH_LONG).show();
        }
        else{
            Log.d("miniWeather","FAIL");
            Toast.makeText(MainActivity.this,"FAIL",Toast.LENGTH_LONG).show();
        }


    }

    //解析xml
    private void parseXML(String xmldata){
        int fengxiangCnt = 0;
        int fengliCnt = 0;
        int dateCnt = 0;
        int highCnt = 0;
        int lowCnt = 0;
        int typeCnt = 0;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();

            Log.d("miniWeather","parseXML");

            while(eventType != XmlPullParser.END_DOCUMENT){
                switch(eventType){
                    case XmlPullParser.START_DOCUMENT://文档开始
                        break;
                    case XmlPullParser.START_TAG://标签元素开始事件
                        if(xmlPullParser.getName().equals("city")){//城市名
                            eventType = xmlPullParser.next();
                            Log.d("miniWeather","city:"+xmlPullParser.getText());
                        }
                        else if(xmlPullParser.getName().equals("updatetime")){//更新时间
                            eventType = xmlPullParser.next();
                            Log.d("miniWeather","updatetime:"+xmlPullParser.getText());
                        }
                        else if(xmlPullParser.getName().equals("shidu")){//湿度
                            eventType = xmlPullParser.next();
                            Log.d("miniWeather","shidu:"+xmlPullParser.getText());
                        }
                        else if(xmlPullParser.getName().equals("wendu")){//温度
                            eventType = xmlPullParser.next();
                            Log.d("miniWeather","wendu:"+xmlPullParser.getText());
                        }
                        else if(xmlPullParser.getName().equals("pm25")){//PM2.5
                            eventType = xmlPullParser.next();
                            Log.d("miniWeather","pm25:"+xmlPullParser.getText());
                        }
                        else if(xmlPullParser.getName().equals("quality")){//空气质量
                            eventType = xmlPullParser.next();
                            Log.d("miniWeather","quality:"+xmlPullParser.getText());
                        }
                        else if(xmlPullParser.getName().equals("fengxiang") && fengxiangCnt == 0){//风向
                            eventType = xmlPullParser.next();
                            Log.d("miniWeather","fengxiang:"+xmlPullParser.getText());
                            fengxiangCnt++;
                        }
                        else if(xmlPullParser.getName().equals("fengli") && fengliCnt == 0){//风力等级
                            eventType = xmlPullParser.next();
                            Log.d("miniWeather","fengli:"+xmlPullParser.getText());
                            fengliCnt++;
                        }
                        else if(xmlPullParser.getName().equals("date") && dateCnt == 0){//日期
                            eventType = xmlPullParser.next();
                            Log.d("miniWeather","date:"+xmlPullParser.getText());
                            dateCnt++;
                        }
                        else if(xmlPullParser.getName().equals("high") && highCnt == 0){//最高温
                            eventType = xmlPullParser.next();
                            Log.d("miniWeather","high:"+xmlPullParser.getText());
                            highCnt++;
                        }
                        else if(xmlPullParser.getName().equals("low") && lowCnt == 0){//最低温
                            eventType = xmlPullParser.next();
                            Log.d("miniWeather","low:"+xmlPullParser.getText());
                            lowCnt++;
                        }
                        else if(xmlPullParser.getName().equals("type") && typeCnt == 0){//类别
                            eventType = xmlPullParser.next();
                            Log.d("miniWeather","type:"+xmlPullParser.getText());
                            typeCnt++;
                        }
                        break;
                    case XmlPullParser.END_TAG://标签元素结束事件
                        break;
                }
                eventType = xmlPullParser.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void queryWeatherCode(String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("miniWeather",address);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try{
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("miniWeather",str);
                    }
                    String responseStr = response.toString();
                    Log.d("miniWeather",responseStr);

                    parseXML(responseStr);//解析xml
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.title_update_btn){
            SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code","101010100");
            Log.d("miniWeather",cityCode);

            if(NetUtil.getNetworkState(this) != NetUtil.NET_NONE){
                Log.d("miniWeather","OK");
                queryWeatherCode(cityCode);
            }
            else{
                Log.d("miniWeather","FAIL");
                Toast.makeText(MainActivity.this,"FAIL",Toast.LENGTH_LONG).show();
            }
        }
    }
}
