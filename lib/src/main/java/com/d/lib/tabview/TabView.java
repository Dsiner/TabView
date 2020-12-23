package com.d.lib.tabview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * TabView
 * Created by D on 2017/3/8.
 */
public class TabView extends View {
    private int mWidth;
    private int mHeight;

    private Rect mRect;
    private RectF mRectF;
    private Paint mPaintA;
    private Paint mPaintB;
    private Paint mPaintTitle; // 仅用于普通文字的画笔
    private Paint mPaintTitleCur; // 仅用于当前选中文字的画笔

    private int mCount; // 总数量
    private float mWidthB; // 单个标题宽度block
    private float mTouchX, mTouchY;
    private int mLastIndex; // 上次的位置
    private int mCurIndex; // 当前的位置
    private int mDownIndex = 0; // ActionDown按压的位置
    private int mTouchSlop;
    private boolean mIsDragging;

    private float mRectRadius; // 圆角矩形弧度
    private String[] mTitles; // Variables 标题
    private int mTextSize; // Variables 标题文字大小
    private int mColorStroke, mColorStrokeBlank, mColorText, mColorTextCur; // Variables 颜色
    private float mPadding; // Variables 背景边框线宽度
    private float mReservedPadding; // Variables 两端预留间距side padding
    private int mDuration; // Variables 动画时长

    private ValueAnimator mAnimation;
    private float mFactor; // 进度因子: 0-1

    private OnTabSelectedListener mOnTabSelectedListener;

    public TabView(Context context) {
        this(context, null);
    }

    public TabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTypedArray(context, attrs);
        init(context);
    }

    private void initTypedArray(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TabView);
        String title = typedArray.getString(R.styleable.TabView_tabv_title);
        mTitles = title != null ? title.split(";") : null;
        mTextSize = (int) typedArray.getDimension(R.styleable.TabView_tabv_textSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics()));
        mColorStroke = mColorText = typedArray.getColor(R.styleable.TabView_tabv_colorMain, Color.parseColor("#FF4081"));
        mColorStrokeBlank = mColorTextCur = typedArray.getColor(R.styleable.TabView_tabv_colorSub, Color.parseColor("#ffffff"));
        mPadding = (int) typedArray.getDimension(R.styleable.TabView_tabv_padding, 2);
        mReservedPadding = (int) typedArray.getDimension(R.styleable.TabView_tabv_paddingSide, -1);
        mDuration = typedArray.getInteger(R.styleable.TabView_tabv_duration, 250);
        typedArray.recycle();
    }

    private void init(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mCount = mTitles != null ? mTitles.length : 0;

        mRect = new Rect();
        mRectF = new RectF();
        mPaintA = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintA.setColor(mColorStroke);

        mPaintB = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintB.setColor(mColorStrokeBlank);

        mPaintTitle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTitle.setTextSize(mTextSize);
        mPaintTitle.setTextAlign(Paint.Align.CENTER);
        mPaintTitle.setColor(mColorText);

        mPaintTitleCur = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintTitleCur.setTextSize(mTextSize);
        mPaintTitleCur.setTextAlign(Paint.Align.CENTER);
        mPaintTitleCur.setColor(mColorTextCur);

        mAnimation = ValueAnimator.ofFloat(0f, 1f);
        mAnimation.setDuration(mDuration);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFactor = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mFactor == 1 && mOnTabSelectedListener != null) {
                    // 选中回调
                    mOnTabSelectedListener.onTabSelected(mCurIndex);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCount <= 0) {
            return;
        }
        mRect.set(0, 0, mWidth, mHeight);
        mRectF.set(mRect);
        // Step 1-1: Draw圆角矩形-边框线颜色
        canvas.drawRoundRect(mRectF, mRectRadius, mRectRadius, mPaintA);

        mRect.set((int) mPadding, (int) mPadding, (int) (mWidth - mPadding), (int) (mHeight - mPadding));
        mRectF.set(mRect);
        // Step 1-2: Draw圆角矩形-背景颜色
        canvas.drawRoundRect(mRectF, mRectRadius, mRectRadius, mPaintB);
        // Step 1-3: 带边框的背景绘制完毕

        float start = mReservedPadding + mWidthB * mLastIndex; // 本次动画开始前的起始位置横坐标
        float end = mReservedPadding + mWidthB * mCurIndex; // 本次动画结束时的预计位置横坐标
        float offsetX = start + (end - start) * mFactor; // 通过属性动画因子，计算此瞬间滑块的其实横坐标

        /*
         * 起始坐标offsetX = 圆角矩形的left横坐标 + 预留间距withP
         * left   offsetX - withP
         * top    0
         * right  offsetX + withB + withP
         * bottom height
         */
        mRect.set((int) (offsetX - mReservedPadding), 0, (int) (offsetX + mWidthB + mReservedPadding), mHeight);
        mRectF.set(mRect);

        // Step 2: Draw当前圆角矩形滑块
        canvas.drawRoundRect(mRectF, mRectRadius, mRectRadius, mPaintA);

        int textheight = (int) getTextHeight(mPaintTitle); // 获取标题的高度px
        int starty = (mHeight + textheight) / 2; // 标题的绘制y坐标,即标题底部中心点y坐标

        // Step 3: 遍历绘制所有标题
        for (int i = 0; i < mCount; i++) {
            float startx = mReservedPadding + mWidthB * i + mWidthB / 2; // 标题的绘制x坐标，即标题底部中心点x坐标
            float cursor = (offsetX + mWidthB / 2) - mReservedPadding;
            if (cursor < 0) {
                cursor = 0;
            }
            int offsetCur = (int) (cursor / mWidthB);
            if (offsetCur == i && (offsetCur == mCurIndex || offsetCur == mLastIndex)) {
                // 当前滑块位置位于动画起始index或终止index时，文字高亮
                canvas.drawText(mTitles[i], startx, starty, mPaintTitleCur);
            } else {
                canvas.drawText(mTitles[i], startx, starty, mPaintTitle);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mRectRadius = (mHeight + 0.5f) / 2;
        mReservedPadding = mReservedPadding == -1 ? (int) (mRectRadius * 0.85f) : mReservedPadding;
        mWidthB = (mWidth - mReservedPadding * 2) / (mCount > 0 ? mCount : 1);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mCount <= 0 || !(mFactor == 0 || mFactor == 1)) {
            return false;
        }
        final int action = ev.getActionMasked();
        final int actionIndex = ev.getActionIndex();
        final float x = ev.getX();
        final float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = x;
                mTouchY = y;
                mDownIndex = (int) ((x - mReservedPadding) / mWidthB);
                mDownIndex = Math.max(mDownIndex, 0);
                mDownIndex = Math.min(mDownIndex, mCount - 1);
                mIsDragging = false;
                return mDownIndex != mCurIndex;

            case MotionEvent.ACTION_MOVE:
                if (!mIsDragging && (Math.abs(x - mTouchX) > mTouchSlop
                        || Math.abs(y - mTouchY) > mTouchSlop)) {
                    mIsDragging = true;
                }
                return !mIsDragging;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mIsDragging
                        || x < 0 || x > mWidth
                        || y < 0 || y > mHeight) {
                    return false;
                }
                int upIndex = (int) ((x - mReservedPadding) / mWidthB);
                upIndex = Math.max(upIndex, 0);
                upIndex = Math.min(upIndex, mCount - 1);
                if (upIndex == mDownIndex) {
                    mLastIndex = mCurIndex;
                    mCurIndex = mDownIndex;
                    startAnim();
                    return true;
                }
                return false;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 获取字体高度
     */
    private float getTextHeight(Paint p) {
        Paint.FontMetrics fm = p.getFontMetrics();
        return (float) ((Math.ceil(fm.descent - fm.top) + 2) / 2);
    }

    /**
     * 开始动画
     */
    private void startAnim() {
        stopAnim();
        if (mAnimation != null) {
            mAnimation.start();
        }
    }

    /**
     * 停止动画
     */
    private void stopAnim() {
        if (mAnimation != null) {
            mAnimation.cancel();
        }
    }

    public void setTitle(String[] title) {
        if (title == null || title.length <= 0) {
            return;
        }
        mTitles = title;
        mCount = mTitles.length;
        mWidthB = (mWidth - mReservedPadding * 2) / (mCount > 0 ? mCount : 1);
        postInvalidate();
    }

    /**
     * 切换当前Tab
     *
     * @param index    Index
     * @param withAnim With animation
     */
    public void select(int index, boolean withAnim) {
        if (index == mCurIndex) {
            return;
        }
        mLastIndex = mCurIndex;
        mCurIndex = index;
        if (withAnim) {
            startAnim();
        } else {
            mFactor = 1f;
            invalidate();
        }
    }

    public void setOnTabSelectedListener(OnTabSelectedListener l) {
        this.mOnTabSelectedListener = l;
    }

    public interface OnTabSelectedListener {

        /**
         * @param index Index
         */
        void onTabSelected(int index);
    }
}