package com.goluk.a6.common.map;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.goluk.a6.common.map.util.GpsConvert;
import com.goluk.a6.control.R;
import com.goluk.a6.control.util.CollectionUtils;
import com.goluk.a6.control.util.DeviceUtil;
import com.goluk.a6.http.responsebean.DeviceStatus;
import com.goluk.a6.http.responsebean.TrackDetail;
import com.goluk.a6.http.responsebean.TrackPoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的爱车--地图View
 */
public class MyCarMapViewImpl extends MyCarMapView implements OnMapReadyCallback {

    private GoogleMap mMap;
    private BitmapDescriptor mIconLocation, mIconCarOnline, mIconCarOffline, mIconCarBg;
    private Marker mMarkerLocation, mMarkerCar, mMarkerCarBg;

    public MyCarMapViewImpl(Context context) {
        super(context);
        init();
    }

    public MyCarMapViewImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public MyCarMapViewImpl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();

    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.google_track_view, this);
        SupportMapFragment mapFragment = (SupportMapFragment) ((FragmentActivity) getContext()).getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        try {
            mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location);
            mIconCarOnline = BitmapDescriptorFactory.fromResource(R.drawable.ic_car_online);
            mIconCarOffline = BitmapDescriptorFactory.fromResource(R.drawable.ic_car_offline);
            mIconCarBg = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_location_bg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap == null)
            return;

        mMap = googleMap;

        if (mStateListener != null) {
            mStateListener.onMapReady();
        }
    }

    @Override
    public void updateUserLocation(Point point, boolean moveCamera) {
        if (mMap == null)
            return;
        if (point == null)
            return;
        LatLng latLng = GpsConvert.convertGps(point.latitude, point.longitude);
        if (latLng == null)
            return;

        if (mMarkerLocation == null)
            mMarkerLocation = mMap.addMarker(new MarkerOptions().position(latLng).icon(mIconLocation).anchor(0.5f, 0.5f).flat(true));
        else
            mMarkerLocation.setPosition(latLng);

        if (moveCamera)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
    }

    @Override
    public void updateCarStatus(DeviceStatus deviceStatus) {
        if (mMap == null)
            return;

        if (deviceStatus == null)
            return;

        if (!DeviceUtil.isOnline(deviceStatus)) {
            // 清除轨迹
            if (mRealtimePolyline != null) {
                mMap.clear();
                mRealtimePolyline = null;
                mRealtimeStartMarker = null;
                mRealtimeEndMarker = null;
                mRealtimeEndMarkerBg = null;
            }
        }

        if (mRealtimePolyline == null)
            drawCarPosition(new Point(deviceStatus.lastLat, deviceStatus.lastLon), DeviceUtil.isOnlineOrDormant(deviceStatus));
    }

    @Override
    public void drawCarPosition(Point point, boolean isOnline) {
        if (mMap == null)
            return;

        LatLng position = GpsConvert.convertGps(point.latitude, point.longitude);
        if (position == null)
            return;
        MarkerOptions carMarkerOptions = new MarkerOptions().position(position).anchor(0.5f, 0.5f).flat(true);
        carMarkerOptions.icon(isOnline ? mIconCarOnline : mIconCarOffline);
        if (mMarkerCar == null || !mMarkerCar.isVisible()) {
            mMarkerCar = mMap.addMarker(carMarkerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14));
        } else {
            mMarkerCar.setIcon(carMarkerOptions.getIcon());
            mMarkerCar.setPosition(position);
        }

        if (mMarkerCarBg == null || !mMarkerCarBg.isVisible())
            mMarkerCarBg = mMap.addMarker(new MarkerOptions().position(position).icon(mIconCarBg).anchor(0.5f, 0.5f).flat(true));
        else
            mMarkerCarBg.setPosition(position);
    }

    @Override

    public void drawTrackLine(List<TrackPoint> trackPoints) {
        if (mMap == null)
            return;

        if (CollectionUtils.isEmpty(trackPoints))
            return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        PolylineOptions mTrackLineOverlayData = new PolylineOptions()
                .width(10)
                .color(Color.parseColor("#EE248CCC"));

        List<LatLng> points = new ArrayList<LatLng>();
        LatLng tempLatlng;
        for (TrackPoint trackPoint : trackPoints) {
            tempLatlng = convertGps(trackPoint.lat, trackPoint.lon);
            points.add(tempLatlng);
            builder.include(tempLatlng);
            trackPoint.lat = tempLatlng.latitude;
            trackPoint.lon = tempLatlng.longitude;
        }

        mTrackLineOverlayData.addAll(points);

        mMap.clear();
        // 轨迹线
        mMap.addPolyline(mTrackLineOverlayData);
        // 起点终点
        TrackPoint startTrackPoint = trackPoints.get(0);
        LatLng startPoint = new LatLng(startTrackPoint.lat, startTrackPoint.lon);
        BitmapDescriptor startIcon = BitmapDescriptorFactory.fromResource(R.drawable.icon_starting);
        mMap.addMarker(new MarkerOptions().position(startPoint).icon(startIcon).flat(true));
        TrackPoint endTrackPoint = trackPoints.get(trackPoints.size() - 1);
        LatLng endPoint = new LatLng(endTrackPoint.lat, endTrackPoint.lon);
        BitmapDescriptor endIcon = BitmapDescriptorFactory.fromResource(R.drawable.pos_end);
        mMap.addMarker(new MarkerOptions().position(endPoint).icon(endIcon).flat(true));

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 400));
    }

    private Polyline mRealtimePolyline;
    private Marker mRealtimeStartMarker, mRealtimeEndMarker, mRealtimeEndMarkerBg;
    private TrackPoint mRealtimeLastPoint;
    private boolean mIsFirstMoveToCenter;

    @Override
    public void onTrackQueryed(TrackDetail trackDetail, boolean trackChanged) {
        if (mMap == null)
            return;

        if (trackDetail == null || trackDetail.points.isEmpty())
            return;

        LatLng convertPoint = null;
        List<TrackPoint> trackPoints = trackDetail.points;
        for (TrackPoint trackPoint : trackPoints) {
            convertPoint = convertGps(trackPoint.lat, trackPoint.lon);
            if (convertPoint != null) {
                trackPoint.lat = convertPoint.latitude;
                trackPoint.lon = convertPoint.longitude;
            }
        }

        if (trackChanged) {
            if (mRealtimePolyline != null) {
                mRealtimePolyline.remove();
            }
            removeMarker(mRealtimeStartMarker);
            mIsFirstMoveToCenter = false;
            mRealtimeLastPoint = null;

            mMap.clear();
        }

        removeMarker(mRealtimeEndMarker);
        removeMarker(mRealtimeEndMarkerBg);
        removeMarker(mMarkerCar);
        removeMarker(mMarkerCarBg);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        PolylineOptions mTrackLineOverlayData = new PolylineOptions()
                .width(14)
                .color(Color.parseColor("#EE248CCC"));

        List<LatLng> points = new ArrayList<LatLng>();
        if (!trackChanged && mRealtimeLastPoint != null) {
            points.add(new LatLng(mRealtimeLastPoint.lat, mRealtimeLastPoint.lon));
        }
        if (!trackChanged && mRealtimePolyline != null) {
            List<LatLng> latLngs = mRealtimePolyline.getPoints();
            if (!CollectionUtils.isEmpty(latLngs))
                points.add(latLngs.get(latLngs.size() - 1));
        }

        LatLng tempLatlng;
        for (TrackPoint trackPoint : trackPoints) {
            tempLatlng = new LatLng(trackPoint.lat, trackPoint.lon);
            points.add(tempLatlng);
            builder.include(tempLatlng);
        }
        mTrackLineOverlayData.addAll(points);
        // 轨迹线
        mRealtimePolyline = mMap.addPolyline(mTrackLineOverlayData);

        // 起点终点
        if (trackChanged) {
            TrackPoint startTrackPoint = trackPoints.get(0);
            LatLng startPoint = new LatLng(startTrackPoint.lat, startTrackPoint.lon);
            BitmapDescriptor startIcon = BitmapDescriptorFactory.fromResource(R.drawable.icon_starting);
            mRealtimeStartMarker = mMap.addMarker(new MarkerOptions().position(startPoint).icon(startIcon).flat(true));
        }

        int endIndex = trackPoints.size() - 1;
        endIndex = (endIndex < 0) ? 0 : endIndex;
        TrackPoint endTrackPoint = trackPoints.get(endIndex);
        LatLng endPoint = new LatLng(endTrackPoint.lat, endTrackPoint.lon);
        float direction = trackDetail.direction;
        mRealtimeEndMarker = mMap.addMarker(new MarkerOptions().position(endPoint).icon(mIconCarOnline).anchor(0.5f, 0.5f).flat(true).rotation(direction));
        mRealtimeEndMarkerBg = mMap.addMarker(new MarkerOptions().position(endPoint).icon(mIconCarBg).anchor(0.5f, 0.5f).flat(true));

        mRealtimeLastPoint = endTrackPoint;

        if (trackChanged) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
        } else {
            if (!mIsFirstMoveToCenter)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(endPoint, 16));
            else
                mMap.animateCamera(CameraUpdateFactory.newLatLng(endPoint));

            mIsFirstMoveToCenter = true;
        }
    }

    /**
     * 坐标点纠偏
     */
    private LatLng convertGps(double latitude, double longitude) {
        return GpsConvert.convertGps(latitude, longitude);
    }

    /**
     * 移除指定Marker
     *
     * @param marker Marker
     */
    private void removeMarker(Marker marker) {
        if (marker != null && marker.isVisible()) {
            marker.remove();
            marker = null;
        }
    }

}
