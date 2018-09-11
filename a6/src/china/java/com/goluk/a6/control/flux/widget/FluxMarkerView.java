
package com.goluk.a6.control.flux.widget;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.goluk.a6.control.R;
import com.goluk.a6.internation.GolukUtils;

/**
 * 流量使用折线图MarkerView
 */
public class FluxMarkerView extends MarkerView {

    private TextView tvContent;
    private String mUnitFlux;
    private float yOffset = Utils.convertDpToPixel(23f);

    public FluxMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        tvContent = (TextView) findViewById(R.id.tv_flux_value);

        mUnitFlux = getResources().getString(R.string.unit_mb);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
//        tvContent.setText("" + Utils.formatNumber(e.getY(), 0, true) + " " + mUnitFlux);
        String content = GolukUtils.converter(2,e.getY());
        tvContent.setText(content);
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight()-yOffset);
    }

    private MPPointF mOffset2 = new MPPointF();

    @Override
    public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {

        MPPointF offset = getOffset();
        mOffset2.x = offset.x;
        mOffset2.y = offset.y;

        Chart chart = getChartView();

        float width = getWidth();
        float height = getHeight();

        if (posX + mOffset2.x < 0) {
            mOffset2.x = -posX;
        } else if (chart != null && posX + width + mOffset2.x > chart.getWidth()) {
            mOffset2.x = chart.getWidth() - posX - width;
        }

        if (posY + mOffset2.y < 0) {
            mOffset2.y = -posY;
        } else if (chart != null && posY + height + mOffset2.y > chart.getHeight()) {
            mOffset2.y = chart.getHeight() - posY - height;
        }

        return mOffset2;
    }



}
