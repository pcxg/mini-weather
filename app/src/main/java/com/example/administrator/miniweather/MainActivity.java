package com.example.administrator.miniweather;


import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.administrator.adapter.ViewPagerAdapter;
import com.example.administrator.app.MyApplication;
import com.example.administrator.bean.City;
import com.example.administrator.bean.TodayWeather;
import com.example.administrator.util.NetUtil;
import com.example.administrator.util.PinYinUtil;
import com.example.administrator.util.TimeUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import java.util.ArrayList;
import java.util.List;

//todo:应用商店发布
public class MainActivity extends Activity implements View.OnClickListener,ViewPager.OnPageChangeListener {

    private static final int UPDATE_TODAY_WEATHER = 1;
    public static final int FIND_CITY_LOC_INFO = 2;
    private static final String TAG = "MiniWeather";
    
    private ImageView mUpdateBtn;
    private ImageView mSelectCityBtn;
    private ImageView mLocationBtn;
    private LinearLayout pm25Layout;
    private ObjectAnimator updateAnim;

    private TextView cityTv,timeTv,temperatureTv,climateTv,humidityTv,weekdayTv,pmDataTv,pmQualityTv,windTv,cityNameTv,tempRangeTv;
    private ImageView weatherImg,pmImg;

    //ViewPager相关
    private List<View> views;
    private ViewPager vp;
    private ViewPagerAdapter vpAdapter;
    private ImageView[] ivs;
    private int[] ids = {R.id.six_days_iv1,R.id.six_days_iv2};


    private boolean isDay;
    private List<View> singleViews;


    //定位服务相关
    private LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    private List<City> mCityList;

    private Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg){
            switch(msg.what){
                case UPDATE_TODAY_WEATHER://更新主页的天气信息
                    updateAnim.end();
                    //Log.d(TAG, "handleMessage:UpdateFinish");
                    updateWeatherInfo((TodayWeather)msg.obj);
                    break;
                case FIND_CITY_LOC_INFO:
                    if(msg.obj != null){
                        Toast.makeText(MainActivity.this,"当前定位: "+msg.obj,Toast.LENGTH_LONG).show();
                        updateWeatherInfoByName((String)msg.obj);
                    }
                    else{
                        Toast.makeText(MainActivity.this,"出了点小问题",Toast.LENGTH_LONG).show();
                    }
                    myListener.setCityName(null);
                    break;
            }
        }
    };
    //根据城市名遍历list更新界面
    private void updateWeatherInfoByName(String name) {
        for(City city:mCityList){
            if(city.getCity().equals(name)){
                queryWeatherCode(city.getNumber());
                return;
            }
        }
        Toast.makeText(MainActivity.this,"没有当前定位的天气信息！",Toast.LENGTH_LONG).show();
    }

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
        initLoc();
        initView();
    }
    //初始化定位服务
    void initLoc(){
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);

        //配置参数
        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度；
        //LocationMode. Battery_Saving：低功耗；
        //LocationMode. Device_Sensors：仅使用设备；

        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认GCJ02
        //GCJ02：国测局坐标；
        //BD09ll：百度经纬度坐标；
        //BD09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回WGS84类型坐标

        option.setScanSpan(1000);
        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效

        option.setOpenGps(true);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true

        option.setLocationNotify(true);
        //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false

        option.setIgnoreKillProcess(false);
        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)

        option.SetIgnoreCacheException(false);
        //可选，设置是否收集Crash信息，默认收集，即参数为false

        option.setWifiCacheTimeOut(5*60*1000);
        //可选，V7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位

        option.setEnableSimulateGps(false);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

        option.setIsNeedAddress(true);//是否需要返回地址信息
        option.setIsNeedLocationDescribe(true);
        mLocationClient.setLocOption(option);
        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用

        mLocationClient.start();
    }
    //初始化界面
    void initView(){
        MyApplication myApplication = (MyApplication)getApplication();
        mCityList = myApplication.getmCityList();

        pm25Layout = (LinearLayout)findViewById(R.id.pm25);

        mUpdateBtn = (ImageView)findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        mLocationBtn = (ImageView)findViewById(R.id.title_location);
        mLocationBtn.setOnClickListener(this);

        updateAnim = ObjectAnimator.ofFloat(mUpdateBtn,"rotation",0,360);
        updateAnim.setDuration(3000);
        updateAnim.setRepeatCount(5);
        updateAnim.setRepeatMode(ObjectAnimator.RESTART);

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

        //viewPager
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();

        views.add(inflater.inflate(R.layout.six_days_wtr1,null));
        views.add(inflater.inflate(R.layout.six_days_wtr2,null));

        vpAdapter = new ViewPagerAdapter(views,this);
        vp = (ViewPager)findViewById(R.id.six_days_viewpager);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this);

        ivs = new ImageView[views.size()];
        for(int i=0;i<views.size();i++){
            ivs[i] = (ImageView)findViewById(ids[i]);
        }

        isDay = TimeUtil.isCurrentInTimeScope(6,0,18,0);//判断是白天还是晚上
        singleViews = new ArrayList<View>();
        singleViews.add(views.get(0).findViewById(R.id.single_day1));
        singleViews.add(views.get(0).findViewById(R.id.single_day2));
        singleViews.add(views.get(0).findViewById(R.id.single_day3));
        singleViews.add(views.get(1).findViewById(R.id.single_day4));
        singleViews.add(views.get(1).findViewById(R.id.single_day5));
        singleViews.add(views.get(1).findViewById(R.id.single_day6));



        //用最后一次打开时的数据进行初始化
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

        Gson gson = new Gson();
        List<String> dates = gson.fromJson(lastData.getString("dates",""),new TypeToken<List<String>>(){}.getType());
        List<String> fenglis = gson.fromJson(lastData.getString("fenglis",""),new TypeToken<List<String>>(){}.getType());
        List<String> types = gson.fromJson(lastData.getString("types",""),new TypeToken<List<String>>(){}.getType());
        List<Integer> type_ids = gson.fromJson(lastData.getString("type_ids",""),new TypeToken<List<Integer>>(){}.getType());
        List<String> temp_ranges = gson.fromJson(lastData.getString("temp_ranges",""),new TypeToken<List<String>>(){}.getType());


        int pmImgId = lastData.getInt("pmImg",getResources().getIdentifier("biz_plugin_weather_0_50","drawable",getPackageName()));
        int whrImgId = lastData.getInt("weatherImg",getResources().getIdentifier("biz_plugin_weather_qing","drawable",getPackageName()));
        //若该城市没有pm25指标，则不显示pm25Layout
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
        windTv.setText("风力："+wind);

        weatherImg.setImageDrawable(getResources().getDrawable(whrImgId));

        for(int i=0;i<6;i++){
            singleViews.get(i).setVisibility(View.VISIBLE);
            if(dates == null){
                singleViews.get(i).setVisibility(View.INVISIBLE);
            }
            else{

                TextView dateText = singleViews.get(i).findViewById(R.id.day_date);
                dateText.setText(dates.get(i));

                TextView fengliText = singleViews.get(i).findViewById(R.id.day_wind);
                fengliText.setText(fenglis.get(i));

                TextView wenchaText = singleViews.get(i).findViewById(R.id.day_temp_range);
                wenchaText.setText(temp_ranges.get(i));

                TextView typeText = singleViews.get(i).findViewById(R.id.day_desc);
                typeText.setText(types.get(i));

                ImageView typeImg = singleViews.get(i).findViewById(R.id.day_weather_img);
                typeImg.setImageDrawable(getResources().getDrawable(type_ids.get(i)));
            }
        }

    }

    //解析xml
    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
        List<String> dates = new ArrayList<String>();
        List<String> types = new ArrayList<String>();
        List<String> highs = new ArrayList<String>();
        List<String> lows = new ArrayList<String>();
        List<String> fenglis = new ArrayList<String>();

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
                            else if(xmlPullParser.getName().equals("date_1")){//昨天日期
                                eventType = xmlPullParser.next();
                                dates.add(xmlPullParser.getText().substring(xmlPullParser.getText().length()-3));

                            }
                            else if(xmlPullParser.getName().equals("high_1")){//昨天高温
                                eventType = xmlPullParser.next();
                                highs.add(xmlPullParser.getText().substring(2).trim());

                            }
                            else if(xmlPullParser.getName().equals("low_1")){//昨天低温
                                eventType = xmlPullParser.next();
                                lows.add(xmlPullParser.getText().substring(2).trim());

                            }
                            else if(xmlPullParser.getName().equals("fl_1")){//昨天风力
                                eventType = xmlPullParser.next();
                                fenglis.add(xmlPullParser.getText());
                            }
                            else if(xmlPullParser.getName().equals("type_1")){//昨天类型
                                eventType = xmlPullParser.next();
                                types.add(xmlPullParser.getText());

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
                            else if(xmlPullParser.getName().equals("fengli")){//风力等级
                                eventType = xmlPullParser.next();
                                if(fengliCnt == 0){
                                    todayWeather.setFengli(xmlPullParser.getText());
                                }
                                fenglis.add(xmlPullParser.getText());
                                fengliCnt++;
                            }
                            else if(xmlPullParser.getName().equals("date")){//日期
                                eventType = xmlPullParser.next();
                                String date = xmlPullParser.getText();
                                if(dateCnt == 0){
                                    todayWeather.setDate(date);
                                }
                                dates.add(date.substring(date.length()-3));
                                Log.d(TAG, "parseXML: "+dates);
                                dateCnt++;
                            }
                            else if(xmlPullParser.getName().equals("high")){//最高温
                                eventType = xmlPullParser.next();
                                if(highCnt == 0){
                                    todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                }
                                highs.add(xmlPullParser.getText().substring(2).trim());
                                highCnt++;
                            }
                            else if(xmlPullParser.getName().equals("low")){//最低温
                                eventType = xmlPullParser.next();
                                if(lowCnt == 0){
                                    todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                }
                                lows.add(xmlPullParser.getText().substring(2).trim());
                                Log.d(TAG, "parseXML: "+lows);
                                lowCnt++;
                            }
                            else if(xmlPullParser.getName().equals("type")){//类别
                                eventType = xmlPullParser.next();
                                if(typeCnt == 0){
                                    todayWeather.setType(xmlPullParser.getText());
                                }
                                types.add(xmlPullParser.getText());

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

        if(dates.size()>0){
            todayWeather.setDates(dates);
            todayWeather.setTypes(types);
            todayWeather.setHighs(highs);
            todayWeather.setLows(lows);
            todayWeather.setFenglis(fenglis);
        }
        return todayWeather;
    }
    //解析url
    private void queryWeatherCode(String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;//通过该接口获取天气信息

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                TodayWeather todayWeather = null;
                try{
                    //建立http连接，获取response数据
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
                    Log.d(TAG, "city: "+todayWeather.getCity());
                    if(todayWeather.getCity() !=null){
                        //返回数据给handler
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        handler.sendMessage(msg);
                    }
                    else
                        Toast.makeText(MainActivity.this,"无法获取天气信息!",Toast.LENGTH_LONG).show();
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
        int pmImgId = 0;//默认设pm25指标不存在
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
            //Log.d(TAG, "updateWeatherInfo:"+todayWeather.getType()+"  "+climatePinyin);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        weatherImg.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("biz_plugin_weather_"+climatePinyin,"drawable",getPackageName())));

        //更新六天天气信息
        //根据白天黑夜来设置天气类型和风力
        int start_index = isDay ? 0:1;
        List<String> dates = todayWeather.getDates();
        List<String> highs = todayWeather.getHighs();
        List<String> lows = todayWeather.getLows();
        List<String> types = todayWeather.getTypes();
        List<String> fenglis = todayWeather.getFenglis();

        List<String> temp_ranges = new ArrayList<>();
        List<String> types_real = new ArrayList<>();
        List<String> fenglis_real = new ArrayList<>();
        List<Integer> type_ids = new ArrayList<>();

        for(int i=0;i<6;i++){
            TextView dayText = (TextView)singleViews.get(i).findViewById(R.id.day_date);
            dayText.setText(dates.get(i));
            TextView tempText = (TextView)singleViews.get(i).findViewById(R.id.day_temp_range);
            String temp_range = lows.get(i)+"~"+highs.get(i);
            tempText.setText(temp_range);
            temp_ranges.add(temp_range);

            TextView fengliText = (TextView)singleViews.get(i).findViewById(R.id.day_wind);
            String fengli  = fenglis.get(1+start_index+i*2);
            fengliText.setText(fengli);
            fenglis_real.add(fengli);

            ImageView typeImg = (ImageView)singleViews.get(i).findViewById(R.id.day_weather_img);
            TextView typeText = (TextView)singleViews.get(i).findViewById(R.id.day_desc);
            String desc = types.get(start_index+i*2);
            typeText.setText(desc);
            types_real.add(desc);

            String pinyin = null;
            try {
                pinyin = new PinYinUtil().toPinYin(desc,"",PinYinUtil.PinYinType.LOWERCASE);
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }

            typeImg.setImageDrawable(getResources().getDrawable(getResources().getIdentifier("biz_plugin_weather_"+pinyin,"drawable",getPackageName())));
            type_ids.add(getResources().getIdentifier("biz_plugin_weather_"+climatePinyin,"drawable",getPackageName()));
            //Log.d(TAG, "updateWeatherInfo: "+singleViews.get(i).findViewById(R.id.day_date).toString());
        }
        Gson gson = new Gson();
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

        editor.putString("dates",gson.toJson(dates));
        editor.putString("temp_ranges",gson.toJson(temp_ranges));
        editor.putString("fenglis",gson.toJson(fenglis_real));
        editor.putString("types",gson.toJson(types_real));
        editor.putString("type_ids",gson.toJson(type_ids));

        editor.commit();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.title_update_btn){//点击更新按钮则更新信息

            SharedPreferences sharedPreferences = getSharedPreferences("savedCityInfo",MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("city_code","101010100");

            updateAnim.start();//点击更新后旋转图片

            if(NetUtil.getNetworkState(this) != NetUtil.NET_NONE){

                queryWeatherCode(cityCode);
                //updateAnim.end();
            }
            else{
                Toast.makeText(MainActivity.this,"FAIL",Toast.LENGTH_LONG).show();
            }
        }
        if(v.getId() == R.id.title_city_manager){
            Intent intent = new Intent(this,SelectCityActivity.class);
            startActivityForResult(intent,1);
        }
        if(v.getId() == R.id.title_location){
            mLocationClient.start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        /*
                        *while(myListener.getAddr() == null){
                            Thread.sleep(1000);
                        }
                        * */
                        if(myListener.getCityName() != null){
                            Message msg = new Message();
                            msg.what = FIND_CITY_LOC_INFO;
                            Log.d(TAG, "run: "+myListener.getCityName());
                            msg.obj = myListener.getCityName();
                            handler.sendMessage(msg);
                            mLocationClient.stop();
                        }
                        else Log.d(TAG, "run: not able to get location");
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }).start();
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


            if(NetUtil.getNetworkState(this) != NetUtil.NET_NONE){

                queryWeatherCode(newCityCode);
            }
            else{
                Log.d("miniWeather","FAIL");
                Toast.makeText(MainActivity.this,"FAIL",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        for(int j=0;j<ids.length;j++){
            if(j==i){
                ivs[j].setImageResource(R.drawable.page_indicator_focused);
            }
            else ivs[j].setImageResource(R.drawable.page_indicator_unfocused);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }



}
