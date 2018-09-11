package com.goluk.a6.common.map;

import android.content.Context;
import android.util.AttributeSet;

import com.goluk.a6.http.responsebean.DeviceStatus;
import com.goluk.a6.http.responsebean.TrackDetail;
import com.goluk.a6.http.responsebean.TrackPoint;

import java.util.List;

/**
 * 我的爱车--地图View
 */
public class MyCarMapViewImpl extends MyCarMapView {
    public MyCarMapViewImpl(Context context) {
        super(context);
    }

    public MyCarMapViewImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyCarMapViewImpl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void updateUserLocation(Point point, boolean moveCamera) {

    }

    @Override
    public void updateCarStatus(DeviceStatus deviceStatus) {

    }

    @Override
    public void drawCarPosition(Point point, boolean isOnline) {

    }

    @Override
    public void drawTrackLine(List<TrackPoint> points) {

    }

    @Override
    public void onTrackQueryed(TrackDetail trackDetail, boolean trackChanged) {

    }

}
