package com.example.administrator.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.administrator.adapter.CityAdapter;
import com.example.administrator.app.MyApplication;
import com.example.administrator.bean.City;
import com.example.administrator.widget.ClearEditText;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class SelectCityActivity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;
    private ListView mList;
    private List<City> mCitylist;
    private CityAdapter cityAdapter;
    private ClearEditText mClearEditText;
    private List<City> filterDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);



        initView();
    }

    private void initView() {
        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        mClearEditText = (ClearEditText)findViewById(R.id.search_city);

        mList = (ListView)findViewById(R.id.city_list);
        MyApplication myApplication = (MyApplication)getApplication();
        mCitylist = myApplication.getmCityList();

        filterDataList = new ArrayList<City>();
        for(City city:mCitylist){
            filterDataList.add(city);
        }
        cityAdapter = new CityAdapter(this,mCitylist);
        mList.setAdapter(cityAdapter);

        mClearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

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
                Intent intent = new Intent();
                City city = filterDataList.get(position);
                intent.putExtra("cityCode",city.getNumber());
                setResult(RESULT_OK,intent);
                finish();
            }
        });

    }
//过滤数据
    private void filterData(String filterStr) {
        //filterDataList = new ArrayList<City>();
        if(TextUtils.isEmpty(filterStr)){
            for(City city:mCitylist){
                filterDataList.add(city);
            }
        }
        else{
            filterDataList.clear();
            for(City city:mCitylist){
                if(city.getCity().indexOf(filterStr.toString())!=-1){
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
                Intent intent = new Intent();
                intent.putExtra("cityCode","101160101");
                setResult(RESULT_OK,intent);
                finish();
                break;


        }
    }
}
