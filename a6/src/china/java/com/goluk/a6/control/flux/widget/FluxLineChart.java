package com.goluk.a6.control.flux.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.goluk.a6.control.R;

/**
 * 流量使用情况折线图
 */
public class FluxLineChart extends LineChart {

    private FluxLineChartRenderer mChartRenderer;
    private FluxXAxisRenderer mXAxisRenderer;

    private int mColorGray;

    public FluxLineChart(Context context) {
        this(context, null);
    }

    public FluxLineChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FluxLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initDefault();
    }

    private void initDefault() {
        mColorGray = getResources().getColor(R.color.flux_chart_gray);

        mChartRenderer = new FluxLineChartRenderer(this, mAnimator, mViewPortHandler);
        mXAxisRenderer = new FluxXAxisRenderer(mViewPortHandler, mXAxis, mLeftAxisTransformer);
        setRenderer(mChartRenderer);
        setXAxisRenderer(mXAxisRenderer);

        setNoDataText("暂无本月流量数据");
        setDrawGridBackground(false);
        setDoubleTapToZoomEnabled(false);
        getDescription().setEnabled(false);
        setTouchEnabled(true);
        setDragEnabled(true);
        setScaleEnabled(true);
        setPinchZoom(false);
        getAxisLeft().setEnabled(false);
        getAxisRight().setEnabled(false);
        getLegend().setEnabled(false);
        getAxisLeft().setAxisMinimum(0);

        FluxMarkerView markerView = new FluxMarkerView(getContext(), R.layout.flux_marker_view);
        markerView.setChartView(this);
        setMarker(markerView);

        mXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        mXAxis.setDrawGridLines(false);
        mXAxis.setGranularity(1);
        mXAxis.setLabelCount(15);
        mXAxis.setAxisLineColor(mColorGray);
        mXAxis.setTextColor(mColorGray);
        mXAxis.setAxisLineWidth(1F);

        initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // if highlighting is enabled
        if (valuesToHighlight())
            mRenderer.drawHighlighted(canvas, mIndicesToHighlight);

        Log.e(TAG, "onDraw: "+getData());
        if (getData()!=null){
            drawIndicator(canvas);
        }


    }

    private static final String TAG = "FluxLineChart";
    private Paint indicatorPaint;
    private int indicatorColor = 0xFF0A82CC;
    private final static float OFFSET_TOP = Utils.convertDpToPixel(33F);
    private void initPaint(){
        indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setColor(Color.BLACK);
//        indicatorPaint.setStrokeWidth(this.getLineData().getDataSets().get(0).getHighlightLineWidth());
    }

    private Entry handleData(){
        LineData data = this.getData();
        LineDataSet lineDataSet = (LineDataSet) data.getDataSets().get(0);



        return lineDataSet.getEntryForIndex(lineDataSet.getEntryCount()-2);

    }

    private void drawIndicator(Canvas canvas){
        Entry entry = handleData();
        Path path = new Path();

        MPPointF pointF = getPosition(entry,getData().getDataSets().get(0).getAxisDependency());

        path.moveTo(pointF.x,pointF.y);
        path.lineTo( pointF.x,mViewPortHandler.contentBottom());

        canvas.drawPath(path,indicatorPaint);

    }


    /**
     * 设置最后一个条数据高亮
     */
    public void setLastValueHighlight() {
        ILineDataSet dataSet = getData().getDataSetByIndex(0);
        float xLastValue = dataSet.getEntryForIndex(dataSet.getEntryCount() - 1).getX();
        highlightValue(xLastValue, 0);
    }

}
