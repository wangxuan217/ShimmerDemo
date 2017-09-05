package com.xiaoxuan.shimmer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * @author xiaoxuan 参考网上实现思路，稍加整理修改 抽象调用方法，参照《Android源码设计模式》第三章Builder模式
 */
public abstract class AbsFocusBorder extends View implements FocusBorder, ViewTreeObserver.OnGlobalFocusChangeListener
{
    // 默认动画播放时间 ms
    private static final long DEFAULT_ANIM_DURATION_TIME = 300;
    
    // 默认流光时间 ms
    private static final long DEFAULT_SHIMMER_DURATION_TIME = 1000;
    
    protected long mAnimDuration = DEFAULT_ANIM_DURATION_TIME;
    
    protected long mShimmerDuration = DEFAULT_SHIMMER_DURATION_TIME;
    
    protected RectF mFrameRectF = new RectF();
    
    protected RectF mPaddingRectF = new RectF();
    
    protected RectF mPaddingOfsetRectF = new RectF();
    
    protected RectF mTempRectF = new RectF();
    
    private LinearGradient mShimmerLinearGradient;
    
    private Matrix mShimmerGradientMatrix;
    
    private Paint mShimmerPaint;
    
    private int mShimmerColor = 0x66FFFFFF;
    
    private float mShimmerTranslate = 0;
    
    private boolean mShimmerAnimating = false;
    
    private boolean mIsShimmerAnim = true;
    
    private ObjectAnimator mTranslationXAnimator;
    
    private ObjectAnimator mTranslationYAnimator;
    
    private ObjectAnimator mWidthAnimator;
    
    private ObjectAnimator mHeightAnimator;
    
    private ObjectAnimator mShimmerAnimator;
    
    private AnimatorSet mAnimatorSet;
    
    private WeakReference<View> mOldFocusView;
    
    private OnFocusCallback mOnFocusCallback;
    
    private boolean mIsVisible = false;
    
    protected AbsFocusBorder(Context context, int shimmerColor, long shimmerDuration, boolean isShimmerAnim,
        long animDuration, RectF paddingOfsetRectF)
    {
        super(context);
        
        this.mShimmerColor = shimmerColor;
        this.mShimmerDuration = shimmerDuration;
        this.mIsShimmerAnim = isShimmerAnim;
        this.mAnimDuration = animDuration;
        if (null != paddingOfsetRectF)
            this.mPaddingOfsetRectF.set(paddingOfsetRectF);
        // 关闭硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // 默认隐藏
        setVisibility(INVISIBLE);
        mShimmerPaint = new Paint();
        mShimmerGradientMatrix = new Matrix();
    }
    
    @Override
    public boolean isInEditMode()
    {
        return true;
    }
    
    /**
     * 绘制闪光
     * 
     * @param canvas
     */
    protected void onDrawShimmer(Canvas canvas)
    {
        if (mShimmerAnimating)
        {
            canvas.save();
            mTempRectF.set(mFrameRectF);
            mTempRectF.inset(2f, 2f);
            float shimmerTranslateX = mTempRectF.width() * mShimmerTranslate;
            float shimmerTranslateY = mTempRectF.height() * mShimmerTranslate;
            mShimmerGradientMatrix.setTranslate(shimmerTranslateX, shimmerTranslateY);
            mShimmerLinearGradient.setLocalMatrix(mShimmerGradientMatrix);
            canvas.drawRoundRect(mTempRectF, getRoundRadius(), getRoundRadius(), mShimmerPaint);
            canvas.restore();
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh)
        {
            mFrameRectF.set(mPaddingRectF.left, mPaddingRectF.top, w - mPaddingRectF.right, h - mPaddingRectF.bottom);
        }
    }
    
    // 默认调用绘制流光效果
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        onDrawShimmer(canvas);
    }
    
    @Override
    protected void onDetachedFromWindow()
    {
        unBoundGlobalFocusListener();
        super.onDetachedFromWindow();
    }
    
    // 设置动画执行的参数，流光参数具体含义可参考LinearGradient的定义，参考博客http://blog.csdn.net/u012702547/article/details/50821044
    private void setShimmerAnimating(boolean shimmerAnimating)
    {
        mShimmerAnimating = shimmerAnimating;
        if (mShimmerAnimating)
        {
            mShimmerLinearGradient = new LinearGradient(0, 0, mFrameRectF.width(), mFrameRectF.height(),
                new int[] {0x00FFFFFF, 0x1AFFFFFF, mShimmerColor, 0x1AFFFFFF, 0x00FFFFFF},
                new float[] {0f, 0.2f, 0.5f, 0.8f, 1f}, Shader.TileMode.CLAMP);
            mShimmerPaint.setShader(mShimmerLinearGradient);
        }
    }
    
    protected void setShimmerTranslate(float shimmerTranslate)
    {
        if (mIsShimmerAnim && mShimmerTranslate != shimmerTranslate)
        {
            mShimmerTranslate = shimmerTranslate;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
    
    protected float getShimmerTranslate()
    {
        return mShimmerTranslate;
    }
    
    protected void setWidth(int width)
    {
        if (getLayoutParams().width != width)
        {
            getLayoutParams().width = width;
            requestLayout();
        }
    }
    
    protected void setHeight(int height)
    {
        if (getLayoutParams().height != height)
        {
            getLayoutParams().height = height;
            requestLayout();
        }
    }
    
    @Override
    public void setVisible(boolean visible)
    {
        if (mIsVisible != visible)
        {
            mIsVisible = visible;
            setVisibility(visible ? VISIBLE : INVISIBLE);
            
            if (!visible && null != mOldFocusView && null != mOldFocusView.get())
            {
                mOldFocusView.clear();
                mOldFocusView = null;
            }
        }
    }
    
    @Override
    public boolean isVisible()
    {
        return mIsVisible;
    }
    
    protected Rect findLocationWithView(View view)
    {
        return findOffsetDescendantRectToMyCoords(view);
    }
    
    protected Rect findOffsetDescendantRectToMyCoords(View descendant)
    {
        final ViewGroup root = (ViewGroup)getParent();
        final Rect rect = new Rect();
        if (descendant == root)
        {
            return rect;
        }
        
        ViewParent theParent = descendant.getParent();
        while ((theParent != null) && (theParent instanceof View) && (theParent != root))
        {
            rect.offset(descendant.getLeft() - descendant.getScrollX(), descendant.getTop() - descendant.getScrollY());
            descendant = (View)theParent;
            theParent = descendant.getParent();
        }
        
        if (theParent == root)
        {
            rect.offset(descendant.getLeft() - descendant.getScrollX(), descendant.getTop() - descendant.getScrollY());
        }
        
        return rect;
    }
    
    // 核心调用
    @Override
    public void onFocus(@NonNull View focusView, FocusBorder.Options options)
    {
        if (null != mOldFocusView && null != mOldFocusView.get())
        {
            mOldFocusView.clear();
        }
        
        if (options instanceof Options)
        {
            final Options baseOptions = (Options)options;
            if (baseOptions.isScale())
            {
                mOldFocusView = new WeakReference<>(focusView);
            }
            // 执行动画1
            runFocusAnimation(focusView, baseOptions);
        }
    }
    
    @Override
    public void boundGlobalFocusListener(@NonNull OnFocusCallback callback)
    {
        mOnFocusCallback = callback;
        getViewTreeObserver().addOnGlobalFocusChangeListener(this);
    }
    
    @Override
    public void unBoundGlobalFocusListener()
    {
        if (null != mOnFocusCallback)
        {
            mOnFocusCallback = null;
            getViewTreeObserver().removeOnGlobalFocusChangeListener(this);
        }
    }
    
    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus)
    {
        final Options options = null != mOnFocusCallback ? (Options)mOnFocusCallback.onFocus(oldFocus, newFocus) : null;
        if (null != options)
        {
            runFocusAnimation(newFocus, options);
        }
    }
    
    // 执行动画2
    private void runFocusAnimation(View focusView, Options options)
    {
        setVisible(true);
        // 移动边框的动画
        runBorderAnimation(focusView, options);
    }
    
    // 执行动画3 最终执行了
    protected void runBorderAnimation(View focusView, Options options)
    {
        if (null == focusView)
            return;
        if (null != mAnimatorSet)
        {
            mAnimatorSet.cancel();
        }
        createBorderAnimation(focusView, options);
        // 开始执行
        mAnimatorSet.start();
    }
    
    // 组织动画效果，子类中默认调用执行
    protected void createBorderAnimation(View focusView, Options options)
    {
        final float paddingWidth =
            mPaddingRectF.left + mPaddingRectF.right + mPaddingOfsetRectF.left + mPaddingOfsetRectF.right;
        final float paddingHeight =
            mPaddingRectF.top + mPaddingRectF.bottom + mPaddingOfsetRectF.top + mPaddingOfsetRectF.bottom;
        final int newWidth = (int)(focusView.getMeasuredWidth() * options.scaleX + paddingWidth);
        final int newHeight = (int)(focusView.getMeasuredHeight() * options.scaleY + paddingHeight);
        final Rect fromRect = findLocationWithView(this);
        final Rect toRect = findLocationWithView(focusView);
        final int x = toRect.left - fromRect.left;
        final int y = toRect.top - fromRect.top;
        final float newX = x - Math.abs(focusView.getMeasuredWidth() - newWidth) / 2f;
        final float newY = y - Math.abs(focusView.getMeasuredHeight() - newHeight) / 2f;
        
        final List<Animator> together = new ArrayList<>();
        final List<Animator> appendTogether = getTogetherAnimators(newX, newY, newWidth, newHeight, options);
        together.add(getTranslationXAnimator(newX));
        together.add(getTranslationYAnimator(newY));
        together.add(getWidthAnimator(newWidth));
        together.add(getHeightAnimator(newHeight));
        if (null != appendTogether && !appendTogether.isEmpty())
        {
            together.addAll(appendTogether);
        }
        
        final List<Animator> sequentially = new ArrayList<>();
        final List<Animator> appendSequentially = getSequentiallyAnimators(newX, newY, newWidth, newHeight, options);
        if (mIsShimmerAnim)
        {
            sequentially.add(getShimmerAnimator());
        }
        if (null != appendSequentially && !appendSequentially.isEmpty())
        {
            sequentially.addAll(appendSequentially);
        }
        
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.setInterpolator(new DecelerateInterpolator(1));
        mAnimatorSet.playTogether(together);
        mAnimatorSet.playSequentially(sequentially);
    }
    
    private ObjectAnimator getTranslationXAnimator(float x)
    {
        if (null == mTranslationXAnimator)
        {
            mTranslationXAnimator = ObjectAnimator.ofFloat(this, "translationX", x).setDuration(mAnimDuration);
        }
        else
        {
            mTranslationXAnimator.setFloatValues(x);
        }
        return mTranslationXAnimator;
    }
    
    private ObjectAnimator getTranslationYAnimator(float y)
    {
        if (null == mTranslationYAnimator)
        {
            mTranslationYAnimator = ObjectAnimator.ofFloat(this, "translationY", y).setDuration(mAnimDuration);
        }
        else
        {
            mTranslationYAnimator.setFloatValues(y);
        }
        return mTranslationYAnimator;
    }
    
    private ObjectAnimator getHeightAnimator(int height)
    {
        if (null == mHeightAnimator)
        {
            mHeightAnimator =
                ObjectAnimator.ofInt(this, "height", getMeasuredHeight(), height).setDuration(mAnimDuration);
        }
        else
        {
            mHeightAnimator.setIntValues(getMeasuredHeight(), height);
        }
        return mHeightAnimator;
    }
    
    private ObjectAnimator getWidthAnimator(int width)
    {
        if (null == mWidthAnimator)
        {
            mWidthAnimator = ObjectAnimator.ofInt(this, "width", getMeasuredWidth(), width).setDuration(mAnimDuration);
        }
        else
        {
            mWidthAnimator.setIntValues(getMeasuredWidth(), width);
        }
        return mWidthAnimator;
    }
    
    private ObjectAnimator getShimmerAnimator()
    {
        if (null == mShimmerAnimator)
        {
            mShimmerAnimator = ObjectAnimator.ofFloat(this, "shimmerTranslate", -1f, 1f);
            mShimmerAnimator.setInterpolator(new LinearInterpolator());
            mShimmerAnimator.setDuration(mShimmerDuration);
            mShimmerAnimator.setStartDelay(400);
            mShimmerAnimator.addListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationStart(Animator animation)
                {
                    setShimmerAnimating(true);
                }
                
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    setShimmerAnimating(false);
                }
            });
        }
        return mShimmerAnimator;
    }
    
    abstract float getRoundRadius();
    
    abstract List<Animator> getTogetherAnimators(float newX, float newY, int newWidth, int newHeight, Options options);
    
    abstract List<Animator> getSequentiallyAnimators(float newX, float newY, int newWidth, int newHeight,
        Options options);
    
    public static class Options extends FocusBorder.Options
    {
        protected float scaleX = 1f, scaleY = 1f;
        
        Options()
        {
        }
        
        private static class OptionsHolder
        {
            private static final Options INSTANCE = new Options();
        }
        
        public static Options get(float scaleX, float scaleY)
        {
            OptionsHolder.INSTANCE.scaleX = scaleX;
            OptionsHolder.INSTANCE.scaleY = scaleY;
            return OptionsHolder.INSTANCE;
        }
        
        public boolean isScale()
        {
            return scaleX != 1f || scaleY != 1f;
        }
    }
    
    // 这里都是些抽象的默认变量，子类中可直接调用
    public static abstract class Builder
    {
        protected int mShimmerColor = 0x8FFFFFFF;
        
        // 默认流光特效开启
        protected boolean mIsShimmerAnim = true;
        
        protected long mAnimDuration = AbsFocusBorder.DEFAULT_ANIM_DURATION_TIME;
        
        protected long mShimmerDuration = AbsFocusBorder.DEFAULT_SHIMMER_DURATION_TIME;
        
        protected RectF mPaddingOfsetRectF = new RectF();
        
        public Builder shimmerColor(int color)
        {
            this.mShimmerColor = color;
            return this;
        }
        
        public Builder shimmerDuration(long duration)
        {
            this.mShimmerDuration = duration;
            return this;
        }
        
        public Builder noShimmer()
        {
            this.mIsShimmerAnim = false;
            return this;
        }
        
        public Builder padding(float padding)
        {
            return padding(padding, padding, padding, padding);
        }
        
        public Builder padding(float left, float top, float right, float bottom)
        {
            this.mPaddingOfsetRectF.left = left;
            this.mPaddingOfsetRectF.top = top;
            this.mPaddingOfsetRectF.right = right;
            this.mPaddingOfsetRectF.bottom = bottom;
            return this;
        }
        
        public abstract FocusBorder build(Activity activity);
        
        public abstract FocusBorder build(ViewGroup viewGroup);
    }
}
