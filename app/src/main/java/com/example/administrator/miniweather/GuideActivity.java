package com.example.administrator.miniweather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.administrator.adapter.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class GuideActivity extends Activity implements ViewPager.OnPageChangeListener {

    private ViewPagerAdapter vpAdapter;
    private ViewPager vp;
    private List<View> views;

    private ImageView[] ivs;
    private int[] ids = {R.id.guide_iv1,R.id.guide_iv2,R.id.guide_iv3};

    private Button endBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide);
        //第一次显示引导界面，之后直接跳转到主界面
        SharedPreferences sharedPreferences = getSharedPreferences("isNew",MODE_PRIVATE);
        int isUsed = sharedPreferences.getInt("isUsed",0);
        if(isUsed>0){
            startActivity(new Intent(GuideActivity.this,MainActivity.class));
            finish();
        }
        else{
            initView();
            initDots();
            SharedPreferences shared = getSharedPreferences("isNew",MODE_PRIVATE);
            SharedPreferences.Editor editor = shared.edit();
            editor.putInt("isUsed",1);
            editor.commit();
        }

    }

    public void initView(){
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();

        views.add(inflater.inflate(R.layout.guide_page1,null));
        views.add(inflater.inflate(R.layout.guide_page2,null));
        views.add(inflater.inflate(R.layout.guide_page3,null));
        vpAdapter = new ViewPagerAdapter(views,this);
        vp = (ViewPager)findViewById(R.id.guide_viewpager);

        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this);

        endBtn = (Button)views.get(2).findViewById(R.id.end_guide_btn);
        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GuideActivity.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
    public void initDots(){
        ivs = new ImageView[views.size()];
        for(int i=0;i<views.size();i++){
            ivs[i] = (ImageView)findViewById(ids[i]);
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
