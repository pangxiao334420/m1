package com.goluk.a6.control.flux.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goluk_lium on 2017/11/21.
 */

public class IndicatorLineChar extends View implements GestureDetector.OnGestureListener {

    public IndicatorLineChar(Context context) {
        super(context);
    }

    public IndicatorLineChar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IndicatorLineChar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private RectF bgRectF;

    private int height, width;

    private Paint bgPaint;

    private Paint mTextPaint;

    private Paint mIndicatorPaint;

    private Paint mLinePaint;

    private int mSelectorIndex;

    private int mHighLightColor = Color.BLUE;

    private int mTextColor = Color.GRAY;

    private int mLineColor = Color.RED;

    private List<String> values = new ArrayList<>();

    private String currentValue;

    private float totalLength;

    private boolean mFling = false;

    private  float interval;

    private float mTextSize;

    private float mLineWidth;

    private float xPointRadius;

    private float pointRadius;

    private float density;

    private int ox,oy;

    private int textMarginAxisX;

    private float offsetX;

    private boolean isTouch;

    private int mRight;

    private float lastMoveX;

    private float perPxValue = .0f;
    private int pointMarginMarker_dp = 31;
    private int markerHeight_dp = 24;

    /**
     * tool
     */
    private Scroller mScroller;
    private GestureDetectorCompat mGestureDetectorCompat;

    private ValueAnimator valueAnimator;
    private VelocityTracker velocityTracker = VelocityTracker.obtain();

    private void init(){
        DisplayMetrics displayMetrics  = getResources().getDisplayMetrics();
        density = displayMetrics.density;

        //init interval
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display  = wm.getDefaultDisplay();
        Point size  = new Point();
        display.getSize(size);
        interval = size.x/10;
        //typedArray
        //
        mGestureDetectorCompat = new GestureDetectorCompat(getContext(),this);
        mScroller = new Scroller(getContext());

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.WHITE);
        bgPaint.setStyle(Paint.Style.FILL);

        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setColor(mHighLightColor);
        mIndicatorPaint.setStyle(Paint.Style.FILL);
        mIndicatorPaint.setStrokeWidth(mLineWidth);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setStyle(Paint.Style.FILL);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(mLineWidth);
        mLinePaint.setColor(mLineColor);
        mLinePaint.setStyle(Paint.Style.FILL);

        if (values.size() > 0)setSelectedIndex(values.size()-1);

        valueAnimator = new ValueAnimator();
    }

    private boolean isFirstShow = true;
    public void setSelectedIndex(int index) {

    }

    private float getIndexOffsetX(int index){
        return interval*index;
    }

    private void initAxisYPerPxValue(){
        float maxValue = 0;
        for (String s:values){
            float tmp = Float.parseFloat(s);
            if (tmp>maxValue) maxValue = tmp;
        }
        perPxValue = maxValue/(height-dpToPx(pointMarginMarker_dp)-dpToPx(markerHeight_dp));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = measureWidth(widthMeasureSpec);
        height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width,height);
    }

    private int measureWidth(int widthMeasureSpec){
        int measureMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureSize = MeasureSpec.getSize(widthMeasureSpec);
        int result = getSuggestedMinimumWidth();
        switch (measureMode){
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = measureSize;
                break;
            default:
                break;
        }
        return result;
    }

    private int measureHeight(int heightMeasureSpec){
        int measureMode = MeasureSpec.getMode(heightMeasureSpec);
        int measureSize = MeasureSpec.getSize(heightMeasureSpec);
        int result = getSuggestedMinimumHeight();
        switch (measureMode){
            case MeasureSpec.AT_MOST:
                result = Math.min(result,measureSize);
            case MeasureSpec.EXACTLY:
                result = Math.max(result,measureSize);
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed){
            width = getWidth();
            height = getHeight();
            Rect rect = new Rect();
            mTextPaint.getTextBounds("00",0,2,rect);
            int textHeight = rect.height();
            int textWidth = rect.width();
            oy = height - textMarginAxisX - textHeight;
            ox = textWidth/2;
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBg(canvas);
        drawAxisX(canvas);
        drawChartLine(canvas);
        drawIndicator();
    }

    private void drawIndicator() {
    }

    private void drawChartLine(Canvas canvas){
        Path path  = new Path();
        float x = ox + interval*0;
        float y = oy ;
        path.moveTo(x,y);
        for ( int i = 1;i<values.size();i++){
            x = ox+interval*i;
            y = oy;
            path.lineTo(x,y);
        }
        canvas.drawPath(path,mLinePaint);
    }

    private int leftScroll;
    private int rightScroll;

    private void drawAxisX(Canvas canvas) {
        int num1;
        float num2;
        // TODO: 2017/11/24 初始化位置
        if (isFirstShow){
            offsetX = getIndexOffsetX(0);
            lastMoveX = offsetX;
            isFirstShow = false;
        }

        num1 = (int) (offsetX/interval);
        num2 = offsetX%interval;
        canvas.save();
        if (!isTouch){
            num2 = offsetX % interval;
            leftScroll = (int) Math.abs(num2);
            rightScroll = (int)(interval-Math.abs(num2));
            final float offsetWithIndicator = num2<=interval?offsetX-leftScroll:offsetX+rightScroll;

            if (valueAnimator!=null&&!valueAnimator.isRunning()){
                valueAnimator = valueAnimator.ofFloat(offsetX,offsetWithIndicator);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        offsetX = (float) animation.getAnimatedValue();
                        lastMoveX = offsetX;
                        invalidate();
                    }
                });
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });
                valueAnimator.setDuration(300);
                valueAnimator.start();
                isTouch = true;
            }
            num1 = (int) (offsetX/interval);
            num2 = offsetX%interval;
        }
        currentValue = values.get(num1);
        while(mRight<width){
            // TODO: 2017/11/24  draw line
            canvas.drawLine(ox,oy,ox+width,oy,mLinePaint);
            for (int i = 0;i<values.size();i++){
                float x = ox+i*interval;
                String axisXTest = values.get(i);
                canvas.drawCircle(x,oy,xPointRadius,mLinePaint);

                Rect textRect = new Rect();
                mTextPaint.getTextBounds(axisXTest,0,axisXTest.length(),textRect);
                int textWidth = textRect.width();
                int textHeight = textRect.height();
                canvas.drawText(axisXTest,x-textWidth/2,oy,mTextPaint);
            }

        }

        canvas.restore();
        drawMarkerView(canvas,"",0,0);


    }

    private void drawMarkerView(Canvas canvas,String text,float x,float y){

        int paddingHorizontal = dpToPx(8);
        int paddingVertical = dpToPx(2);
        Rect temp = new Rect();
        mIndicatorPaint.getTextBounds(text,0,text.length(),temp);
        int textWidth = temp.width();
        int textHeight = temp.height();
        RectF rectF = new RectF(x-textWidth/2-paddingHorizontal,
                y-textHeight-paddingVertical*2,
                x+textWidth/2+paddingHorizontal,
                y);
        mIndicatorPaint.setColor(Color.BLUE);
        mIndicatorPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rectF,20,20,mIndicatorPaint);
        mIndicatorPaint.setColor(Color.WHITE);
        mIndicatorPaint.setStrokeWidth(5);
        canvas.drawText(text,x-textWidth/2,y-textHeight-paddingVertical,mIndicatorPaint);


    }

    private void drawBg(Canvas canvas) {
        bgRectF = new RectF(0,0,width,height);
        canvas.drawRect(bgRectF,bgPaint);
    }

    private float touchCurrentX;
    private float downX;
    private int xVelocity;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchCurrentX = event.getX();
        isTouch = true;
        velocityTracker.computeCurrentVelocity(500);
        velocityTracker.addMovement(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (valueAnimator!=null&&valueAnimator.isRunning()){
                    valueAnimator.end();
                    valueAnimator.cancel();
                }
                downX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                offsetX = touchCurrentX - downX + lastMoveX;
                if (offsetX>=width/2) offsetX = width/2;
                else if (offsetX<=getIndexOffsetX(values.size()-1))
                    offsetX = getIndexOffsetX(values.size()-1);
                break;
            case MotionEvent.ACTION_UP:
                lastMoveX = offsetX;
                xVelocity = (int) velocityTracker.getXVelocity();
                handleInertance(xVelocity);
                velocityTracker.clear();
                break;
        }
        invalidate();
        return true;
    }

    //处理滑动惯性
    private void handleInertance(int velocity){
        if (Math.abs(velocity)<50){
            isTouch = false;
            return;
        }
        if (valueAnimator.isRunning()) return;

        valueAnimator = ValueAnimator.ofInt(0,velocity/20).setDuration(Math.abs(velocity/10));
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                offsetX += (int)animation.getAnimatedValue();
                if (offsetX>=width/2) offsetX = width/2;
                else if (offsetX<=getIndexOffsetX(values.size()-1))
                    offsetX =getIndexOffsetX(values.size()-1);
                lastMoveX = offsetX;
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isTouch = false;
                invalidate();
            }
        });
        valueAnimator.start();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f * (dp >= 0 ? 1 : -1));
    }
}
