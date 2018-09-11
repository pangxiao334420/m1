package com.goluk.a6.control.flux.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import com.goluk.a6.control.R;

/**
 * 圆形水波动画控件
 */
public class WaterWaveView extends View {
    // 圆环的画笔宽度
    private static final int CIRCLE_STROKE_WIDTH = 4;
    //波纹振幅与半径之比
    private static final float A = 0.12f;
    // 外面的两个圆线半径
    private int mCircleOne, mCircleTwo;
    //组件的宽，高
    private int mWidth, mHeight;
    // 最大值,当前值和进度
    private float mMaxValue, mCurrentValue;
    private int mProgress;
    // 绘制波浪的画笔
    private Paint mWavePaint;
    //绘制文字的画笔
    private Paint mTextPaint;
    //绘制边框的画笔
    private Paint mCirclePaint;
    // 中心点坐标
    private int mCenterX, mCenterY;
    //内圆所在的矩形
    private RectF mCircleRectF;
    private Rect textBounds = new Rect();
    //x方向偏移量
    private int xOffset;
    //绘制水波的路径
    private Path wavePath;
    //每一个像素对应的弧度数
    private float RADIANS_PER_X;
    //内圆半径
    private int mInerRadius;

    private Context mContext;

    public WaterWaveView(Context context) {
        super(context);
        init(context);
    }

    public WaterWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WaterWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mWavePaint = new Paint();
        mWavePaint.setColor(Color.parseColor("#33FFFFFF"));
        mWavePaint.setAntiAlias(true);
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setAntiAlias(true);
        mCirclePaint = new Paint();
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(Color.parseColor("#33F5F7F9"));
        autoRefresh();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        initMeasurement();

        //绘制外层两个圆形
        canvas.drawCircle(mCenterX, mCenterY, mCircleOne, mCirclePaint);
        canvas.drawCircle(mCenterX, mCenterY, mCircleTwo, mCirclePaint);
        //绘制两条水波曲线
        canvas.drawPath(getWavePath(xOffset), mWavePaint);
        canvas.drawPath(getWavePath(xOffset + 150), mWavePaint);
        //绘制文字
        mTextPaint.setTextSize(dp2px(36));
//        String text1 = String.valueOf(mCurrentValue);
        String text1 = converterWithoutUnit(2,mCurrentValue);
        float w1 = mTextPaint.measureText(text1);
        mTextPaint.getTextBounds(text1, 0, text1.length(), textBounds);
        float h1 = textBounds.height();
        float extraW = mTextPaint.measureText(UNITS[unitIndex]) / 6;
        canvas.drawText(text1, mCenterX - w1 / 2 - extraW, mCenterY + mInerRadius * (5 / 16F), mTextPaint);
        mTextPaint.setTextSize(dp2px(14));
        mTextPaint.getTextBounds(UNITS[unitIndex], 0, 2, textBounds);
        float h2 = textBounds.height();
        canvas.drawText(UNITS[unitIndex], mCenterX + w1 / 2 - extraW + 5, mCenterY + mInerRadius * (5 / 16F), mTextPaint);
        String prefixString = mContext.getResources().getString(R.string.total_flux);
//        String text3 = prefixString+"：" + String.valueOf(mMaxValue) + "MB";
        String text3 = prefixString+"：" +converter(2,mMaxValue);
        float w3 = mTextPaint.measureText(text3, 0, text3.length());
        mTextPaint.getTextBounds("MB", 0, 1, textBounds);
        float h3 = textBounds.height();
        canvas.drawText(text3, mCenterX - w3 / 2, mCenterY + mInerRadius * (2 / 3F) - h3 / 2, mTextPaint);
        String text4 = mContext.getResources().getString(R.string.remaind_flux);
        float w4 = mTextPaint.measureText(text4, 0, text4.length());
        mTextPaint.getTextBounds(text4, 0, text4.length(), textBounds);
        float h4 = textBounds.height();
        canvas.drawText(text4, mCenterX - w4 / 2, mCenterY - mInerRadius * (2 / 45F) - h4 / 2, mTextPaint);
    }

    /**
     * 根据宽高计算各个尺寸
     */
    private void initMeasurement() {
        if (mWidth == 0 || mHeight == 0) {
            mWidth = getWidth();
            mHeight = getHeight();
            //计算圆弧半径和圆心点
            int circleRadius = Math.min(mWidth, mHeight) >> 1;
            mCirclePaint.setStrokeWidth(CIRCLE_STROKE_WIDTH);
            mCenterX = mWidth / 2;
            mCenterY = mHeight / 2;
            mInerRadius = (int) (circleRadius * (9 / 11F));
            mCircleOne = (int) (circleRadius * (46 / 55F));
            mCircleTwo = circleRadius - (CIRCLE_STROKE_WIDTH + 1);
            RADIANS_PER_X = (float) (Math.PI / mInerRadius);
            mCircleRectF = new RectF(mCenterX - mInerRadius, mCenterY - mInerRadius,
                    mCenterX + mInerRadius, mCenterY + mInerRadius);
        }
    }

    /**
     * 获取水波曲线（包含圆弧部分）的Path
     *
     * @param xOffset x方向像素偏移量.
     */
    private Path getWavePath(int xOffset) {
        if (wavePath == null) {
            wavePath = new Path();
        } else {
            wavePath.reset();
        }
        float[] startPoint = new float[2]; //波浪线起点
        float[] endPoint = new float[2]; //波浪线终点
        for (int i = 0; i <= mInerRadius * 2; i += 2) {
            float x = mCenterX - mInerRadius + i;
            float y = (float) (mCenterY + mInerRadius * (1.0f + A) * 2 * (0.5f - mCurrentValue / mMaxValue)
                    + mInerRadius * A * Math.sin((xOffset + i) * RADIANS_PER_X));
            //只计算内圆内部的点，边框上的忽略
            if (calDistance(x, y, mCenterX, mCenterY) > mInerRadius) {
                if (x < mCenterX) {
                    continue; //左边框,继续循环
                } else {
                    break; //右边框,结束循环
                }
            }
            //第1个点
            if (wavePath.isEmpty()) {
                startPoint[0] = x;
                startPoint[1] = y;
                wavePath.moveTo(x, y);
            } else {
                wavePath.lineTo(x, y);
            }
            endPoint[0] = x;
            endPoint[1] = y;
        }
        if (wavePath.isEmpty()) {
            if (mCurrentValue / mMaxValue >= 0.5f) {
                //满格
                wavePath.moveTo(mCenterX, mCenterY - mInerRadius);
                wavePath.addCircle(mCenterX, mCenterY, mInerRadius, Path.Direction.CW);
            } else {
                //空格
                return wavePath;
            }
        } else {
            //添加圆弧部分
            float startDegree = calDegreeByPosition(startPoint[0], startPoint[1]); //0~180
            float endDegree = calDegreeByPosition(endPoint[0], endPoint[1]); //180~360
            wavePath.arcTo(mCircleRectF, endDegree - 360, startDegree - (endDegree - 360));
        }
        return wavePath;
    }

    private float calDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    //根据当前位置，计算出进度条已经转过的角度。
    private float calDegreeByPosition(float currentX, float currentY) {
        float a1 = (float) (Math.atan(1.0f * (mCenterX - currentX) / (currentY - mCenterY)) / Math.PI * 180);
        if (currentY < mCenterY) {
            a1 += 180;
        } else if (currentY > mCenterY && currentX > mCenterX) {
            a1 += 360;
        }
        return a1 + 90;
    }

    /**
     * 设置最大值和当前值
     *
     * @param maxValue     最大值
     * @param currentValue 当前值
     */
    public void setmValues(float maxValue, float currentValue) {
        this.mMaxValue = maxValue;
        this.mCurrentValue = currentValue;
        // 计算当前进度并重绘
        this.mProgress = (int) (100 * mCurrentValue / mMaxValue);
        invalidate();
    }

    /**
     * 自动刷新页面，创造水波效果。组件销毁后该线城将自动停止。
     */
    private void autoRefresh() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!detached) {
                    xOffset += (mInerRadius >> 4);
                    SystemClock.sleep(100);
                    postInvalidate();
                }
            }
        }).start();
    }

    //标记View是否已经销毁
    private boolean detached = false;

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        detached = true;
    }

    private int dp2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }




    private static final String FORMAT_F_UNIT = "%1$-1.2f%2$s";
    private static final String FORMAT_F = "%1$-1.2f";
    private static final String[] UNITS = new String[]{
            "B","KB","MB","GB","TB","PB","**"
    };
    private static final int LAST_IDX = UNITS.length-1;
    private int unitIndex;
    private  String converter(int unit, float size) {
        int unitIdx = unit;
        while (size > 1024) {
            unitIdx++;
            size /= 1024;
        }
        int idx = unitIdx < LAST_IDX ? unitIdx : LAST_IDX;
        return String.format(FORMAT_F_UNIT, size, UNITS[idx]);
    }

    private  String converterWithoutUnit(int unit, float size) {
        int unitIdx = unit;
        while (size > 1024) {
            unitIdx++;
            size /= 1024;
        }
        int idx = unitIdx < LAST_IDX ? unitIdx : LAST_IDX;
        unitIndex=idx;
        return String.format(FORMAT_F, size, UNITS[idx]);
    }

}
