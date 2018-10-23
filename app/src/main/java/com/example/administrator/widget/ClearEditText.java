package com.example.administrator.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;

import com.example.administrator.miniweather.R;

public class ClearEditText extends android.support.v7.widget.AppCompatEditText implements View.OnFocusChangeListener,TextWatcher {
    private Drawable mClearDrawable;

    public ClearEditText(Context context) {
        this(context,null);
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        this(context,attrs,android.R.attr.editTextStyle);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //获取editText的DrawableRight,若没有设置则使用默认图片
        mClearDrawable = getCompoundDrawables()[2];
        if(mClearDrawable == null){
            mClearDrawable = getResources().getDrawable(R.drawable.emotionstore_progresscancelbtn);

        }
        mClearDrawable.setBounds(0,0,mClearDrawable.getIntrinsicWidth(),mClearDrawable.getIntrinsicHeight());
        setClearIconVisible(false);
        setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }

    //水平方向上判断是否点击删除图标
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(getCompoundDrawables()[2]!=null){
            if(event.getAction() == MotionEvent.ACTION_UP){
                boolean touchable = event.getX()>(getWidth()-getPaddingRight()-mClearDrawable.getIntrinsicWidth())
                        &&(event.getX()<(getWidth()-getPaddingRight()));
                if (touchable)
                    this.setText("");
            }
        }
        return super.onTouchEvent(event);
    }
    //设置删除图片的显示与隐藏
    private void setClearIconVisible(boolean visible) {
        Drawable right = visible?mClearDrawable:null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1],
                right,
                getCompoundDrawables()[3]);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }
    //输入框内内容变化时回调的方法
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        setClearIconVisible(s.length()>0);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            setClearIconVisible(getText().length()>0);
        }
        else
            setClearIconVisible(false);
    }

    /*
    * 设置晃动动画*/
    public void setShakeAnimation(){
        this.startAnimation(shakeAnimation(5));
    }

    private Animation shakeAnimation(int count) {
        Animation animation = new TranslateAnimation(0,10,0,0);
        animation.setInterpolator(new CycleInterpolator(count));
        animation.setDuration(1000);
        return animation;
    }
}
