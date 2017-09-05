package com.xiaoxuan.shimmer;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by xiaoxuan on 2017/9/5.
 */

public class MainActivity extends Activity implements FocusBorder.OnFocusCallback
{
    // 流光特效
    private FocusBorder mFocusBorder;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 实例话流光特效控件 shimmerColor()流光的主基色
        mFocusBorder = new FocusBorder.Builder().asColor().shimmerColor(0x8FFFFFFF).build(this);
        // 绑定流光特效回调
        mFocusBorder.boundGlobalFocusListener(this);
        ImageView imageView1 = (ImageView)findViewById(R.id.iv_1);
        ImageView imageView2 = (ImageView)findViewById(R.id.iv_2);
    }
    
    @Override
    public FocusBorder.Options onFocus(View oldFocus, View newFocus)
    {
        if (newFocus != null && oldFocus != null)
        {
            switch (newFocus.getId())
            {
                case R.id.iv_1:
                    return FocusBorder.OptionsFactory.get(0);
                case R.id.iv_2:
                    return FocusBorder.OptionsFactory.get(0);
                default:
                    break;
            }
            mFocusBorder.setVisible(false);
        }
        return null;
    }
}
