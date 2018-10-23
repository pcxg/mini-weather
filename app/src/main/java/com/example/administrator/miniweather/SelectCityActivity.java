package com.example.administrator.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.adapter.CityAdapter;
import com.example.administrator.app.MyApplication;
import com.example.administrator.bean.City;
import com.example.administrator.widget.ClearEditText;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * 选择城市界面Activity
 */
public class SelectCityActivity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;
    private TextView currentCity;
    private ListView mList;
    private List<City> mCitylist;
    private CityAdapter cityAdapter;
    private ClearEditText mClearEditText;
    private List<City> filterDataList;

    private String cityCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        initView();
    }

    private void initView() {
        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        currentCity = (TextView)findViewById(R.id.title_name);

        mClearEditText = (ClearEditText)findViewById(R.id.search_city);

        mList = (ListView)findViewById(R.id.city_list);
        MyApplication myApplication = (MyApplication)getApplication();
        mCitylist = myApplication.getmCityList();

        //filterDataList存放筛选后的数据，初始化存放全部
        filterDataList = new ArrayList<City>();
        for(City city:mCitylist){
            filterDataList.add(city);
        }
        //设置适配器
        cityAdapter = new CityAdapter(this,mCitylist);
        mList.setAdapter(cityAdapter);

        mClearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            //文本变化时修改filterDataList
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());

                mList.setAdapter(cityAdapter);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City city = filterDataList.get(position);
                currentCity.setText("当前城市："+city.getCity());
                cityCode = city.getNumber();
                //view.setBackgroundColor(Color.BLUE);
            }
        });

    }
    //过滤数据
    private void filterData(String filterStr) {
        //filterDataList = new ArrayList<City>();
        if(TextUtils.isEmpty(filterStr)){//输入文本为空
            mClearEditText.setShakeAnimation();
            for(City city:mCitylist){
                filterDataList.add(city);
            }
        }
        else{//输入文本不为空：清空filterDataList，遍历mCityList
            filterDataList.clear();
            for(City city:mCitylist){
                if(city.getCity().contains(filterStr.toString()) || city.getAllPY().contains(filterStr.toString().toUpperCase())
                    || city.getAllFirstPY().contains(filterStr.toString().toUpperCase()) || city.getFirstPY().contains(filterStr.toString().toUpperCase())){
                    filterDataList.add(city);
                }
            }
        }
        Log.d(TAG, "filterData: "+filterDataList.size());
        cityAdapter.update(filterDataList);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            default:
                break;
            case R.id.title_back:

                //Log.d(TAG, "onClick: "+cityCode);
                if(cityCode != null){
                    Intent intent = new Intent();
                    intent.putExtra("cityCode",cityCode);
                    setResult(RESULT_OK,intent);
                }

                finish();
                break;


        }
    }
}
