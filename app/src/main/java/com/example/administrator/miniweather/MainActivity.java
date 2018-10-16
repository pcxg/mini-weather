package com.example.administrator.miniweather;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.bean.TodayWeather;
import com.example.administrator.util.NetUtil;
import com.example.administrator.util.PinYinUtil;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.w3c.dom.Text;
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
    private static final String TAG = "MiniWeather";
    
    private ImageView mUpdateBtn;
    private ImageView mSelectCityBtn;
    private LinearLayout pm25Layout;

    private TextView cityTv,timeTv,temperatureTv,climateTv,humidityTv,weekdayTv,pmDataTv,pmQualityTv,windTv,cityNameTv,tempRangeTv;
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
        pm25Layout = (LinearLayout)findViewById(R.id.pm25);

        mUpdateBtn = (ImageView)findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        mSelectCityBtn = (ImageView)findViewById(R.id.title_city_manager);
        mSelectCityBtn.setOnClickListener(this);

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
        tempRangeTv = (TextView)findViewById(R.id.today_temp_range);

        weatherImg = (ImageView)findViewById(R.id.today_weather_img);
        pmImg = (ImageView)findViewById(R.id.pm25_level_img);

        //初始化
        SharedPreferences lastData = getSharedPreferences("lastMsg",MODE_PRIVATE);
        String city  = lastData.getString("city","N/A");
        String updateTime = lastData.getString("updatetime","N/A");
        String wendu = lastData.getString("wendu","N/A");
        String wencha = lastData.getString("wencha","N/A");
        String shidu = lastData.getString("shidu","N/A");
        String pm25 = lastData.getString("pm25","N/A");
        String pmQ = lastData.getString("pmQ","N/A");
        String date = lastData.getString("weekday","N/A");
        String type = lastData.getString("type","N/A");
        String wind = lastData.getString("wind","N/A");

        int pmImgId = lastData.getInt("pmImg",getResources().getIdentifier("biz_plugin_weather_0_50","drawable",getPackageName()));
        int whrImgId = lastData.getInt("weatherImg",getResources().getIdentifier("biz_plugin_weather_qing","drawable",getPackageName()));

        if(pmImgId == 0){
            pm25Layout.setVisibility(View.GONE);
        }
        else{
            pm25Layout.setVisibility(View.VISIBLE);
            pmImg.setImageDrawable(getResources().getDrawable(pmImgId));
        }

        cityNameTv.setText(city+"天气");
        cityTv.setText(city);
        timeTv.setText(updateTime+"发布");
        temperatureTv.setText("温度："+wendu+"°C");
        tempRangeTv.setText(wencha);
        humidityTv.setText("湿度："+shidu);
        pmDataTv.setText(pm25);
        pmQualityTv.setText(pmQ);
        weekdayTv.setText(date);
        climateTv.setText(type);
        windTv.setText(wind);

        weatherImg.setImageDrawable(getResources().getDrawable(whrImgId));

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
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCnt++;
                            }
                            else if(xmlPullParser.getName().equals("low") && lowCnt == 0){//最低温
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
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
    //解析url
    private void queryWeatherCode(String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;

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
        temperatureTv.setText("温度："+todayWeather.getWendu()+"°C");
        tempRangeTv.setText(todayWeather.getLow()+"~"+todayWeather.getHigh());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力："+todayWeather.getFengli());

        //Log.d(TAG, "updateWeatherInfo: pm25:"+todayWeather.getPm25());
        int pmImgId = 0;
        if(todayWeather.getPm25() == null){
            pm25Layout.setVisibility(View.GONE);
        }
        else{
            pm25Layout.setVisibility(View.VISIBLE);
            int pm25_int = Integer.parseInt(todayWeather.getPm25());
            pmImgId = R.drawable.biz_plugin_weather_0_50;//保存pm25图片id
            //按pm25数值设置图片
            if(pm25_int <= 50){
                pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_0_50));
            }
            else if(pm25_int >50 && pm25_int <=100){
                pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_51_100));
                pmImgId = R.drawable.biz_plugin_weather_51_100;
            }
            else if(pm25_int >100 && pm25_int <=150){
                pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_101_150));
                pmImgId = R.drawable.biz_plugin_weather_101_150;
            }
            else if(pm25_int >150 && pm25_int <=200){
                pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_151_200));
                pmImgId = R.drawable.biz_plugin_weather_151_200;
            }
            else if(pm25_int >200 && pm25_int <=300){
                pmImg.setImageDrawable(getResources().getDrawable(R.drawable.biz_plugin_weather_201_300));
                pmImgId = R.drawable.biz_plugin_weather_201_300;
            }
        }

        //按天气状态的拼音设置图片
        String climatePinyin = null;
        try {
            climatePinyin = new PinYinUtil().toPinYin(todayWeather.getType(),"",PinYinUtil.PinYinType.LOWERCASE);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        weatherImg.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("biz_plugin_weather_"+climatePinyin,"drawable",getPackageName())));

        //保存这次的更新信息
        SharedPreferences lastSharedData = getSharedPreferences("lastMsg",MODE_PRIVATE);
        SharedPreferences.Editor editor = lastSharedData.edit();
        editor.putString("city",todayWeather.getCity());
        editor.putString("updatetime",todayWeather.getUpdatetime());
        editor.putString("shidu",todayWeather.getShidu());
        editor.putString("wendu",todayWeather.getWendu());
        editor.putString("wencha",todayWeather.getLow()+"~"+todayWeather.getHigh());
        editor.putString("pm25",todayWeather.getPm25());
        editor.putString("pmQ",todayWeather.getQuality());
        editor.putString("weekday",todayWeather.getDate());
        editor.putString("type",todayWeather.getType());
        editor.putString("wind",todayWeather.getFengli());
        editor.putInt("pmImg",pmImgId);
        editor.putInt("weatherImg",getResources().getIdentifier("biz_plugin_weather_"+climatePinyin,"drawable",getPackageName()));

        editor.commit();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.title_update_btn){
            SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code","101010100");
            Log.d("miniWeather",cityCode);

            if(NetUtil.getNetworkState(this) != NetUtil.NET_NONE){

                queryWeatherCode(cityCode);
            }
            else{
                Log.d("miniWeather","FAIL");
                Toast.makeText(MainActivity.this,"FAIL",Toast.LENGTH_LONG).show();
            }
        }
        if(v.getId() == R.id.title_city_manager){
            Intent intent = new Intent(this,SelectCityActivity.class);
            startActivityForResult(intent,1);
        }
    }
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode == 1 && resultCode == RESULT_OK){//选择城市的代码
            String newCityCode = data.getStringExtra("cityCode");
            Log.d(TAG, "onActivityResult: "+newCityCode);

            //保存这次的城市代码
            SharedPreferences cityInfo = getSharedPreferences("savedCityInfo",MODE_PRIVATE);
            SharedPreferences.Editor editor = cityInfo.edit();
            editor.putString("city_code",newCityCode);
            editor.commit();


            SharedPreferences codeInfo = getSharedPreferences("savedCityInfo",MODE_PRIVATE);
            String cityCode = codeInfo.getString("city_code","");
            Toast.makeText(MainActivity.this,cityCode,Toast.LENGTH_LONG).show();

            if(NetUtil.getNetworkState(this) != NetUtil.NET_NONE){

                queryWeatherCode(newCityCode);
            }
            else{
                Log.d("miniWeather","FAIL");
                Toast.makeText(MainActivity.this,"FAIL",Toast.LENGTH_LONG).show();
            }
        }
    }
}
