package com.goluk.a6.common.map;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.goluk.a6.http.responsebean.DeviceStatus;
import com.goluk.a6.http.responsebean.TrackDetail;
import com.goluk.a6.http.responsebean.TrackPoint;

import java.util.List;

/**
 * 我的爱车--地图View
 */
public abstract class MyCarMapView extends RelativeLayout {

    private Context mContext;

    protected MapStateListener mStateListener;

    public MyCarMapView(Context context) {
        super(context);
        mContext = context;
    }

    public MyCarMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

    }

    public MyCarMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;

    }

    public static MyCarMapView create(Context context) {
        MyCarMapView view = null;
        LayoutParams lp = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        view = new MyCarMapViewImpl(context);
        view.setLayoutParams(lp);
        return view;
    }

    public void setStateListener(MapStateListener listener) {
        mStateListener = listener;
    }

    /**
     * 更新用户(手机)位置信息
     */
    public abstract void updateUserLocation(Point point, boolean moveCamera);

    /**
     * 更新车辆状态及位置信息
     */
    public abstract void updateCarStatus(DeviceStatus deviceStatus);

    /**
     * 画车辆位置
     *
     * @param point    位置
     * @param isOnline 是否在线(控制车辆颜色)
     */
    public abstract void drawCarPosition(Point point, boolean isOnline);

    /**
     * 画历史轨迹
     *
     * @param points List<TrackPoint>
     */
    public abstract void drawTrackLine(List<TrackPoint> points);

    /**
     * 绘制实时轨迹
     *
     * @param trackDetail  轨迹信息
     * @param trackChanged 轨迹是否变化了(跟之前不是同一条轨迹)
     */
    public abstract void onTrackQueryed(TrackDetail trackDetail, boolean trackChanged);

}
