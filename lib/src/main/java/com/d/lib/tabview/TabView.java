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
    private int width;
    private int height;

    private Rect rect;
    private RectF rectF;
    private Paint paintA;
    private Paint paintB;
    private Paint paintTitle; // 仅用于普通文字的画笔
    private Paint paintTitleCur; // 仅用于当前选中文字的画笔

    private int count; // 总数量
    private float widthB; // 单个标题宽度block
    private float dX, dY;
    private int lastIndex; // 上次的位置
    private int curIndex; // 当前的位置
    private int dIndex = 0; // ActionDown按压的位置
    private int touchSlop;
    private boolean isClickValid; // 点击是否有效

    private float rectRadius; // 圆角矩形弧度
    private String[] TITLES; // Variables 标题
    private int textSize; // Variables 标题文字大小
    private int colorStroke, colorStrokeBlank, colorText, colorTextCur; // Variables 颜色
    private float padding; // Variables 背景边框线宽度
    private float widthP; // Variables 两端预留间距side padding
    private int duration; // Variables 动画时长

    private ValueAnimator animation;
    private float factor; // 进度因子: 0-1

    private OnTabSelectedListener listener;

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
        TITLES = title != null ? title.split(";") : null;
        textSize = (int) typedArray.getDimension(R.styleable.TabView_tabv_textSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics()));
        colorStroke = colorText = typedArray.getColor(R.styleable.TabView_tabv_colorMain, Color.parseColor("#FF4081"));
        colorStrokeBlank = colorTextCur = typedArray.getColor(R.styleable.TabView_tabv_colorSub, Color.parseColor("#ffffff"));
        padding = (int) typedArray.getDimension(R.styleable.TabView_tabv_padding, 2);
        widthP = (int) typedArray.getDimension(R.styleable.TabView_tabv_paddingSide, -1);
        duration = typedArray.getInteger(R.styleable.TabView_tabv_duration, 250);
        typedArray.recycle();
    }

    private void init(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        count = TITLES != null ? TITLES.length : 0;

        rect = new Rect();
        rectF = new RectF();
        paintA = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintA.setColor(colorStroke);

        paintB = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintB.setColor(colorStrokeBlank);

        paintTitle = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTitle.setTextSize(textSize);
        paintTitle.setTextAlign(Paint.Align.CENTER);
        paintTitle.setColor(colorText);

        paintTitleCur = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTitleCur.setTextSize(textSize);
        paintTitleCur.setTextAlign(Paint.Align.CENTER);
        paintTitleCur.setColor(colorTextCur);

        animation = ValueAnimator.ofFloat(0f, 1f);
        animation.setDuration(duration);
        animation.setInterpolator(new LinearInterpolator());
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                factor = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (factor == 1 && listener != null) {
                    // 选中回调
                    listener.onTabSelected(curIndex);
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
        if (count <= 0) {
            return;
        }
        rect.set(0, 0, width, height);
        rectF.set(rect);
        // Step 1-1: Draw圆角矩形-边框线颜色
        canvas.drawRoundRect(rectF, rectRadius, rectRadius, paintA);

        rect.set((int) padding, (int) padding, (int) (width - padding), (int) (height - padding));
        rectF.set(rect);
        // Step 1-2: Draw圆角矩形-背景颜色
        canvas.drawRoundRect(rectF, rectRadius, rectRadius, paintB);
        // Step 1-3: 带边框的背景绘制完毕

        float start = widthP + widthB * lastIndex; // 本次动画开始前的起始位置横坐标
        float end = widthP + widthB * curIndex; // 本次动画结束时的预计位置横坐标
        float offsetX = start + (end - start) * factor; // 通过属性动画因子，计算此瞬间滑块的其实横坐标

        /*
         * 起始坐标offsetX = 圆角矩形的left横坐标 + 预留间距withP
         * left   offsetX - withP
         * top    0
         * right  offsetX + withB + withP
         * bottom height
         */
        rect.set((int) (offsetX - widthP), 0, (int) (offsetX + widthB + widthP), height);
        rectF.set(rect);

        // Step 2: Draw当前圆角矩形滑块
        canvas.drawRoundRect(rectF, rectRadius, rectRadius, paintA);

        int textheight = (int) getTextHeight(paintTitle); // 获取标题的高度px
        int starty = (height + textheight) / 2; // 标题的绘制y坐标,即标题底部中心点y坐标

        // Step 3: 遍历绘制所有标题
        for (int i = 0; i < count; i++) {
            float startx = widthP + widthB * i + widthB / 2; // 标题的绘制x坐标，即标题底部中心点x坐标
            float cursor = (offsetX + widthB / 2) - widthP;
            if (cursor < 0) {
                cursor = 0;
            }
            int offsetCur = (int) (cursor / widthB);
            if (offsetCur == i && (offsetCur == curIndex || offsetCur == lastIndex)) {
                // 当前滑块位置位于动画起始index或终止index时，文字高亮
                canvas.drawText(TITLES[i], startx, starty, paintTitleCur);
            } else {
                canvas.drawText(TITLES[i], startx, starty, paintTitle);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        rectRadius = (height + 0.5f) / 2;
        widthP = widthP == -1 ? (int) (rectRadius * 0.85f) : widthP;
        widthB = (width - widthP * 2) / (count > 0 ? count : 1);
        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (count <= 0 || !(factor == 0 || factor == 1)) {
            return false;
        }
        float eX = event.getX();
        float eY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dX = eX;
                dY = eY;
                dIndex = (int) ((eX - widthP) / widthB);
                dIndex = Math.max(dIndex, 0);
                dIndex = Math.min(dIndex, count - 1);
                isClickValid = true;
                return dIndex != curIndex;
            case MotionEvent.ACTION_MOVE:
                if (isClickValid && (Math.abs(eX - dX) > touchSlop || Math.abs(eY - dY) > touchSlop)) {
                    isClickValid = false;
                }
                return isClickValid;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!isClickValid || eX < 0 || eX > width || eY < 0 || eY > height) {
                    return false;
                }
                int uIndex = (int) ((eX - widthP) / widthB);
                uIndex = Math.max(uIndex, 0);
                uIndex = Math.min(uIndex, count - 1);
                if (uIndex == dIndex) {
                    lastIndex = curIndex;
                    curIndex = dIndex;
                    start();
                    return true;
                }
                return false;
        }
        return super.onTouchEvent(event);
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
    private void start() {
        stop();
        if (animation != null) {
            animation.start();
        }
    }

    /**
     * 停止动画
     */
    private void stop() {
        if (animation != null) {
            animation.cancel();
        }
    }

    public void setTitle(String[] title) {
        if (title == null || title.length <= 0) {
            return;
        }
        TITLES = title;
        count = TITLES.length;
        widthB = (width - widthP * 2) / (count > 0 ? count : 1);
        postInvalidate();
    }

    /**
     * 切换当前Tab
     *
     * @param index    Index
     * @param withAnim With animation
     */
    public void select(int index, boolean withAnim) {
        if (index == curIndex) {
            return;
        }
        lastIndex = curIndex;
        curIndex = index;
        if (withAnim) {
            start();
        } else {
            factor = 1f;
            invalidate();
        }
    }

    public interface OnTabSelectedListener {

        /**
         * @param index Index
         */
        void onTabSelected(int index);
    }

    public void setOnTabSelectedListener(OnTabSelectedListener l) {
        this.listener = l;
    }
}