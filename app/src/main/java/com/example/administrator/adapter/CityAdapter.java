package com.example.administrator.adapter;

import android.content.Context;
import android.widget.TextView;

import com.example.administrator.bean.City;
import com.example.administrator.miniweather.R;

import java.util.List;

/**
 * 城市列表的ListView的适配器类
 */
public class CityAdapter extends ListViewAdapter<City> {

    public CityAdapter(Context context, List<City> data) {
        super(context, data, R.layout.city_item);
    }


    @Override
    public void convert(ViewHolder holder, City city) {
        ((TextView)holder.getView(R.id.city_item_name)).setText(city.getCity());
        ((TextView)holder.getView(R.id.city_item_code)).setText(city.getNumber());
    }

    public void update(List<City> newData){
        this.mData = newData;
    }
}
