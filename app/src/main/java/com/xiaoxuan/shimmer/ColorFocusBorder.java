package com.xiaoxuan.shimmer;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.ViewGroup;

/**
 * @author xiaoxuan 2017.09.05 实现抽象类AbsFocusBorder的具体执行方法 ColorFocusBorder为具体的实现类
 */
public class ColorFocusBorder extends AbsFocusBorder
{
    // 0为矩形
    private float mRoundRadius = 0;
    
    // 绘制矩形区域的动画
    private ObjectAnimator mRoundRadiusAnimator;
    
    // 构造函数
    private ColorFocusBorder(Context context, int shimmerColor, long shimmerDuration, boolean isShimmerAnim,
        long animDuration, RectF paddingOfsetRectF)
    {
        super(context, shimmerColor, shimmerDuration, isShimmerAnim, animDuration, paddingOfsetRectF);
    }
    
    // 设置圆角，0为无圆角因此为矩形，为什么是矩形看抽象类AbsFocusBorder中定义了RectF类
    protected void setRoundRadius(float roundRadius)
    {
        if (mRoundRadius != roundRadius)
        {
            mRoundRadius = roundRadius;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
    
    // 获取圆角
    @Override
    public float getRoundRadius()
    {
        return mRoundRadius;
    }
    
    // 获取动画执行的对象
    @Override
    List<Animator> getTogetherAnimators(float newX, float newY, int newWidth, int newHeight,
        AbsFocusBorder.Options options)
    {
        if (options instanceof Options)
        {
            final Options rawOptions = (Options)options;
            List<Animator> animators = new ArrayList<>();
            animators.add(getRoundRadiusAnimator(rawOptions.roundRadius));
            return animators;
        }
        return null;
    }
    
    @Override
    List<Animator> getSequentiallyAnimators(float newX, float newY, int newWidth, int newHeight,
        AbsFocusBorder.Options options)
    {
        return null;
    }
    
    // 获取圆角动画的绘制对象
    private ObjectAnimator getRoundRadiusAnimator(float roundRadius)
    {
        if (null == mRoundRadiusAnimator)
        {
            mRoundRadiusAnimator = ObjectAnimator.ofFloat(this, "roundRadius", getRoundRadius(), roundRadius);
        }
        else
        {
            mRoundRadiusAnimator.setFloatValues(getRoundRadius(), roundRadius);
        }
        return mRoundRadiusAnimator;
    }
    
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
    }
    
    // 根据指令下达进行战术分析
    public static class Options extends AbsFocusBorder.Options
    {
        // 默认圆角角度为0，则为矩形
        private float roundRadius = 0f;
        
        Options()
        {
            super();
        }
        
        // Options单例执行对象
        private static class OptionsHolder
        {
            private static final Options INSTANCE = new Options();
        }
        
        // 接受圆角角度
        public static Options get(float roundRadius)
        {
            OptionsHolder.INSTANCE.roundRadius = roundRadius;
            return OptionsHolder.INSTANCE;
        }
    }
    
    public final static class Builder extends AbsFocusBorder.Builder
    {
        // 设置流光的主基色
        @Override
        public AbsFocusBorder.Builder shimmerColor(int color)
        {
            return super.shimmerColor(color);
        }
        
        // 构造函数
        @Override
        public FocusBorder build(Activity activity)
        {
            if (null == activity)
            {
                throw new NullPointerException("The activity cannot be null");
            }
            return build((ViewGroup)activity.findViewById(android.R.id.content));
        }
        
        // 构造函数
        @Override
        public FocusBorder build(ViewGroup parent)
        {
            if (null == parent)
            {
                throw new NullPointerException("The FocusBorder parent cannot be null");
            }
            // 实例话ColorFocusBorder类对象
            final ColorFocusBorder boriderView = new ColorFocusBorder(parent.getContext(), mShimmerColor,
                mShimmerDuration, mIsShimmerAnim, mAnimDuration, mPaddingOfsetRectF);
            final ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(1, 1);
            parent.addView(boriderView, lp);
            return boriderView;
        }
    }
}
