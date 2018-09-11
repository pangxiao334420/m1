package com.goluk.a6.control.flux.widget;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * 流量使用情况X轴Renderer
 */
public class FluxXAxisRenderer extends XAxisRenderer {

    private Paint mCirclePaint;

    public FluxXAxisRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans) {
        super(viewPortHandler, xAxis, trans);
        mCirclePaint = new Paint();
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(ColorTemplate.rgb("#CBCBCB"));
    }

    @Override
    protected void drawLabel(Canvas c, String formattedLabel, float x, float y, MPPointF anchor, float angleDegrees) {
        super.drawLabel(c, formattedLabel, x, y, anchor, angleDegrees);
        c.drawCircle(x, mViewPortHandler.contentBottom(), 8, mCirclePaint);
    }

}
