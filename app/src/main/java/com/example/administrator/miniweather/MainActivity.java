package com.example.administrator.miniweather;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.bean.TodayWeather;
import com.example.administrator.util.NetUtil;
import com.example.administrator.util.PinYinUtil;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity implements View.OnClickListener{

    private static final int UPDATE_TODAY_WEATHER = 1;

    private ImageView mUpdateBtn;
    private TextView cityTv,timeTv,temperatureTv,climateTv,humidityTv,weekdayTv,pmDataTv,pmQualityTv,windTv,cityNameTv;
    private ImageView weatherImg,pmImg;

    private Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg){
            switch(msg.what){
                case UPDATE_TODAY_WEATHER:
                    updateWeatherInfo((TodayWeather)msg.obj);
                    break;
            }
        }
    };

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

        initView();
    }
    //初始化界面
    void initView(){
        cityTv = (TextView)findViewById(R.id.today_city);
        timeTv = (TextView)findViewById(R.id.today_time);
        temperatureTv = (TextView)findViewById(R.id.today_temperature);
        climateTv = (TextView)findViewById(R.id.today_description);
        humidityTv = (TextView)findViewById(R.id.today_humidity);
        weekdayTv = (TextView)findViewById(R.id.today_weekday);
        pmDataTv = (TextView)findViewById(R.id.pm25_level_num);
        pmQualityTv = (TextView)findViewById(R.id.pm25_level);
        windTv = (TextView)findViewById(R.id.today_wind);
        cityNameTv = (TextView)findViewById(R.id.title_city_name);

        weatherImg = (ImageView)findViewById(R.id.today_weather_img);
        pmImg = (ImageView)findViewById(R.id.pm25_level_img);

        //初始化

    }

    //解析xml
    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
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
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather !=null){
                            if(xmlPullParser.getName().equals("city")){//城市名
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            }
                            else if(xmlPullParser.getName().equals("updatetime")){//更新时间
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            }
                            else if(xmlPullParser.getName().equals("shidu")){//湿度
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            }
                            else if(xmlPullParser.getName().equals("wendu")){//温度
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            }
                            else if(xmlPullParser.getName().equals("pm25")){//PM2.5
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            }
                            else if(xmlPullParser.getName().equals("quality")){//空气质量
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            }
                            else if(xmlPullParser.getName().equals("fengxiang") && fengxiangCnt == 0){//风向
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCnt++;
                            }
                            else if(xmlPullParser.getName().equals("fengli") && fengliCnt == 0){//风力等级
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCnt++;
                            }
                            else if(xmlPullParser.getName().equals("date") && dateCnt == 0){//日期
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCnt++;
                            }
                            else if(xmlPullParser.getName().equals("high") && highCnt == 0){//最高温
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText());
                                highCnt++;
                            }
                            else if(xmlPullParser.getName().equals("low") && lowCnt == 0){//最低温
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText());
                                lowCnt++;
                            }
                            else if(xmlPullParser.getName().equals("type") && typeCnt == 0){//类别
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCnt++;
                            }
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

        return todayWeather;
    }

    private void queryWeatherCode(String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("miniWeather",address);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                TodayWeather todayWeather = null;
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

                    todayWeather = parseXML(responseStr);//解析xml
                    if(todayWeather !=null){
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        handler.sendMessage(msg);
                    }
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
    //更新天气界面里的信息
    void updateWeatherInfo(TodayWeather todayWeather){
        cityNameTv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+"发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekdayTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力："+todayWeather.getFengli());

        
        int pm25_int = Integer.parseInt(todayWeather.getPm25());
        //按pm25数值设置图片
        if(pm25_int <= 50){
            pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_0_50));
        }
        else if(pm25_int >50 && pm25_int <=100){
            pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_51_100));
        }
        else if(pm25_int >100 && pm25_int <=150){
            pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_101_150));
        }
        else if(pm25_int >150 && pm25_int <=200){
            pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_151_200));
        }
        else if(pm25_int >200 && pm25_int <=300){
            pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_201_300));
        }
        //按天气状态的拼音设置图片
        String climatePinyin = null;
        try {
            climatePinyin = new PinYinUtil().toPinYin(todayWeather.getType(),"",PinYinUtil.PinYinType.LOWERCASE);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        weatherImg.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("biz_plugin_weather_"+climatePinyin,"drawable",getPackageName())));
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
