package com.goluk.a6.control.flux.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineScatterCandleRadarDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * 流量管理每辆车流量使用折线图LineChartRenderer
 */
public class FluxLineChartRenderer extends LineChartRenderer {
    private final static float OFFSET_TOP = Utils.convertDpToPixel(29F);

    private Path mHighlightLinePath = new Path();
    private Paint mPaint = new Paint();
    private int lightLineColor = 0xFF0A82CC;

    public FluxLineChartRenderer(LineDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);

        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void drawHighlightLines(Canvas c, float x, float y, ILineScatterCandleRadarDataSet set) {
        // set color and stroke-width
        mHighlightPaint.setColor(lightLineColor);
        mHighlightPaint.setStrokeWidth(set.getHighlightLineWidth());

        mPaint.setColor(lightLineColor);

        // draw highlighted lines (if enabled)
        mHighlightPaint.setPathEffect(set.getDashPathEffectHighlight());

        // 只绘制竖直方向Hilight线
        // draw vertical highlight lines
        if (set.isVerticalHighlightIndicatorEnabled()) {

            // create vertical path
            mHighlightLinePath.reset();
            mHighlightLinePath.moveTo(x, y - OFFSET_TOP);
            mHighlightLinePath.lineTo(x, mViewPortHandler.contentBottom());

            c.drawPath(mHighlightLinePath, mHighlightPaint);
        }

        // 高亮圆
        c.drawCircle(x, y, 10, mPaint);
        c.drawCircle(x, mViewPortHandler.contentBottom(), 10, mPaint);
    }

}
