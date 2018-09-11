package com.goluk.a6.common.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.control.R;
import com.goluk.a6.control.dvr.RemoteCameraConnectManager;
import com.goluk.a6.control.util.GPSFile;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
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
import com.media.tool.GPSData;
import com.media.tool.MediaPlayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConcreteMapTrackView extends MapTrackView implements OnMapReadyCallback {

    private static final String TAG = "CarSvc_BaiduTrackView";

    private static final String LAST_LOCATION_LONGITUDE = "last_location_longitude";
    private static final String LAST_LOCATION_LATITUDE = "last_location_latitude";
    public static final String LAST_LOCATION_CITY = "last_location_city";

    //分割时间，两个gps数据间隔大于此时间表示两段路程
    private static final int DIVISION_TIME = 30 * 60; //30分钟
    private static final int COLORS[] = {
            0xFF000000,
            0xFFFF0000,
            0xFF9400D3
    };
    private static final int MAX_POINT = 36000;
    //两个点的经度或者纬度大于此值时表示第二个点无效
    private static final double THRESHOLD_VALUE = 1;

    private PolylineOptions mTrackLineOverlayData;
    private TextView mTimeView;
    private GoogleMap mMap;
    boolean mSaveLocation = false;// 是否保存了当前位置
    boolean isFirstLoc = false;// 是否应用首次定位
    boolean isFirstCarLoc = true;//是否首次获取车的位置
    private int gpsType = -1;
    private Marker mTrackCarMar;
    private BitmapDescriptor mCarBitmapDescriptor;
    private BitmapDescriptor mCarBitmapStart;
    private BitmapDescriptor mCarBitmapEnd;
    private Handler mHandler = new Handler();
    private boolean mShowCarInfo = true;
    private boolean mShowCarInfoTime = true;
    boolean mTrackDraw = false;
    private boolean mapReady = false;
    private boolean googleAvailable = false;

    public ConcreteMapTrackView(Context context) {
        super(context);
        initView();
    }

    public ConcreteMapTrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ConcreteMapTrackView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    @Override
    public boolean isMapAvailable() {
        return googleAvailable;
    }

    @Override
    public void setGPSDataFromType(int value) {
        gpsType = value;
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    }

    @Override
    public void onDestroy() {
        if (!mapReady) {
            return;
        }
        mMap.setMyLocationEnabled(false);
        mMap = null;
        mContext = null;
    }

    @Override
    public void clear() {
        mTrackCarMar = null;
        setTimeText(Long.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    //GPSData 为标准GPS数据
    @SuppressWarnings("unchecked")
    @Override
    public void drawTrackLine(List<GPSData> list) {
        doDrawTrackLine(list);
        if (mMapListener != null)
            mMapListener.onAfterDrawLineTrack(list);
    }

    //画远程视频的GPS轨迹，数据从记录仪获取
    @Override
    public void drawRemoteTrackLine(String url) {
        new DrawRemoteTrackTask().execute(url);
    }

    @Override
    public void drawTrackCar(GPSData data, boolean center, boolean connectLine) {
        LatLng ll = new LatLng(data.latitude, data.longitude);
        if (mTrackCarMar != null) {
            LatLng old = mTrackCarMar.getPosition();
            mTrackCarMar.setPosition(ll);
            mTrackCarMar.setRotation(data.angle + 90);
        }
    }

    //GPSData 为标准GPS数据
    @Override
    public void drawTrackCar(GPSData data, boolean center) {
        drawTrackCar(data, center, false);
    }

    @Override
    public void resetFirstCarLoc() {
        isFirstCarLoc = true;
    }

    @Override
    public void setLocationEnabled(boolean enable) {

    }

    @Override
    public void setShowCarInfo(boolean show) {
        mShowCarInfo = show;
    }

    @Override
    public void setShowCarInfoTime(boolean show) {
        mShowCarInfoTime = show;
    }

    private void initView() {
        googleAvailable = MapsInitializer.initialize(CarControlApplication.getInstance().getApplicationContext()) == 0;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.google_track_view, this);
        SupportMapFragment mapFragment = (SupportMapFragment) ((FragmentActivity) getContext()).getSupportFragmentManager()
                .findFragmentById(R.id.map);
        try {
            mCarBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.image_mycar);
            mCarBitmapStart = BitmapDescriptorFactory.fromResource(R.drawable.icon_starting);
            mCarBitmapEnd = BitmapDescriptorFactory.fromResource(R.drawable.pos_end);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;
    }


    //远程视频，需要播放的时候解析gps数据，然后重新画轨迹
    private List<GPSData> doDrawTrackLine(List<GPSData> list) {
        if (list == null) {
            return list;
        }
        if (mTrackDraw) {
            return list;
        }
        if (!mapReady || mMap == null) {
            return list;
        }
        mMap.clear();
        List<GPSData> gpsDataList = new ArrayList<GPSData>();
        mTrackCarMar = null;
        mTrackLineOverlayData = new PolylineOptions();
        List<LatLng> points = new ArrayList<LatLng>();
        List<GPSData> gpsDatas = new ArrayList<GPSData>();
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        GPSData preData = null;
        //int time = list.size() > 0? list.get(0).time : 0;
        int increase = 1;
        int interval = 0;
        if (list.size() > MAX_POINT) {
            increase = list.size() / MAX_POINT;
            int total = list.size() / increase;
            interval = total / (total % MAX_POINT);
        }
        Log.i(TAG, "increase = " + increase);
        Log.i(TAG, "interval = " + interval);
        for (int i = 0, count = 1; i < list.size(); i += increase, count++) {
            if (interval != 0 && count % interval == 0)
                continue;
            GPSData data = list.get(i);
            if ((data.latitude == 0 && data.longitude == 0) || data.time == 0)
                continue;
            LatLng ll = new LatLng(data.latitude, data.longitude);
//            if (data.coordType == GPSData.COORD_TYPE_GPS) {
            data.latitude = ll.latitude;
            data.longitude = ll.longitude;
            data.coordType = GPSData.COORD_TYPE_GPS;
//            } else {
            //因为百度定位坐标不准，轨迹不画出来
//                continue;
//            }
            //当时间间隔大于DIVISION_TIME时认为是新的一段路，用不同的颜色画轨迹
            if (preData != null && ((data.time - preData.time) > DIVISION_TIME)) {
                //一段轨迹的点必须大于等于两个
                if (points.size() >= 2) {
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .width(10)
                            .color(getContext().getResources().getColor(R.color.blue));
                    mTrackLineOverlayData = polylineOptions;
                    gpsDataList.addAll(gpsDatas);
                    for (LatLng l : points)
                        builder.include(l);
                    mTrackLineOverlayData.addAll(points);
                }
                points = new ArrayList<LatLng>();
                gpsDatas = new ArrayList<GPSData>();
            }
            if (preData == null || (Math.abs(ll.latitude - preData.latitude) < THRESHOLD_VALUE
                    && Math.abs(ll.longitude - preData.longitude) < THRESHOLD_VALUE)) {
                points.add(ll);
                gpsDatas.add(data);
            }
            preData = data;
        }
        if (points.size() >= 1) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .width(6)
                    .color(getContext().getResources().getColor(R.color.blue));
            mTrackLineOverlayData = polylineOptions;
            gpsDataList.addAll(gpsDatas);
            for (LatLng l : points)
                builder.include(l);
            mTrackLineOverlayData.addAll(points);
            mMap.addPolyline(mTrackLineOverlayData);
            mMap.addMarker(new MarkerOptions().icon(mCarBitmapStart).position(points.get(0)));
            mTrackCarMar = mMap.addMarker(new MarkerOptions().position(points.get(0)).icon(mCarBitmapDescriptor).anchor(0.5f, 0.5f).flat(true));
            mMap.addMarker(new MarkerOptions().icon(mCarBitmapEnd).position(points.get(points.size() - 1)));
            try {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 16 * 2));
            } catch (IllegalStateException e) {
                //https://stackoverflow.com/questions/13692579/movecamera-with-cameraupdatefactory-newlatlngbounds-crashes
                final View mapView = ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentById(R.id.map).getView();
                if (mapView.getViewTreeObserver().isAlive()) {
                    mapView.getViewTreeObserver().addOnGlobalLayoutListener(
                            new ViewTreeObserver.OnGlobalLayoutListener() {
                                @SuppressWarnings("deprecation")
                                @SuppressLint("NewApi")
                                // We check which build version we are using.
                                @Override
                                public void onGlobalLayout() {
                                    mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 16 * 2));
                                }
                            });
                }
            }
            mTrackDraw = true;
        }
        return gpsDataList;
    }


    class DrawTrackTask extends AsyncTask<List<GPSData>, Void, List<GPSData>> {

        @Override
        protected List<GPSData> doInBackground(List<GPSData>... params) {
            List<GPSData> list = params[0];
            return doDrawTrackLine(list);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mMapListener != null)
                mMapListener.onPreDrawLineTrack();
        }

        @Override
        protected void onPostExecute(List<GPSData> result) {
            super.onPostExecute(result);
            if (mMapListener != null)
                mMapListener.onAfterDrawLineTrack(result);
        }
    }

    class DrawRemoteTrackTask extends AsyncTask<String, Void, List<GPSData>> {

        @Override
        protected List<GPSData> doInBackground(String... params) {
            if (TraceCacheManager.instance() == null) return null;
            String path = params[0];
            byte[] cache = TraceCacheManager.instance().getTraceByKey(path);
            if (cache != null) {
                List<GPSData> result = null;
                byte original[] = null;
                if (gpsType == GPS_REMOTE_MP4) {
                    try {
                        File tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/temp");
                        if (tempFile.exists()) {
                            tempFile.delete();
                        }
                        tempFile.createNewFile();
                        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                        fileOutputStream.write(cache);
                        original = MediaPlayer.findMP4GPSData(tempFile.getAbsolutePath());
                        fileOutputStream.close();
                        tempFile.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    original = cache;
                }
                List<GPSData> list = GPSFile.parseGPSList(original, false, true, false);
                if (list != null)
                    result = list;
                return result;
            } else {
                String strUrl = null;
                HttpURLConnection urlConnection = null;
                URL url = null;
                InputStream input = null;
                List<GPSData> result = null;
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                if (RemoteCameraConnectManager.HTTP_SERVER_IP == null || RemoteCameraConnectManager.HTTP_SERVER_IP.length() <= 0)
                    return null;
                try {
                    strUrl = "http://" + RemoteCameraConnectManager.HTTP_SERVER_IP + ":" + RemoteCameraConnectManager.HTTP_SERVER_PORT +
                            "/cgi-bin/Config.cgi?action=download&property=path&value=" + URLEncoder.encode(path, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }

                try {
                    url = new URL(strUrl);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    if (gpsType == GPS_REMOTE_MP4) {
                        urlConnection.setRequestProperty("Range", "bytes=0-819199");
                    }
                    int code = urlConnection.getResponseCode();
                    Log.i(TAG, "code = " + code);
                    if (code < 200 || code > 206)
                        return null;
                    input = urlConnection.getInputStream();
                    int total = urlConnection.getContentLength();
                    int count = 0;
                    byte[] buffer = new byte[1024];
                    int len = input.read(buffer);
                    while (len != -1) {
                        output.write(buffer, 0, len);
                        count += len;
                        len = input.read(buffer);
                    }
                    output.flush();
                    Log.i(TAG, "total = " + total);
                    Log.i(TAG, "count = " + count);
                    if (total == count) {
                        TraceCacheManager.instance().putTraceToCache(path,
                                output.toByteArray());
                        byte original[] = null;
                        if (gpsType == GPS_REMOTE_MP4) {
                            File tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/temp");
                            if (tempFile.exists()) {
                                tempFile.delete();
                            }
                            tempFile.createNewFile();
                            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                            output.writeTo(fileOutputStream);
                            original = MediaPlayer.findMP4GPSData(tempFile.getAbsolutePath());
                            fileOutputStream.close();
                            tempFile.delete();
                        } else {
                            original = output.toByteArray();
                        }
                        List<GPSData> list = GPSFile.parseGPSList(original, false, true, false);
                        if (list != null)
                            result = list;
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();

                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (output != null) {
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                return result;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mMapListener != null)
                mMapListener.onPreDrawLineTrack();
        }

        @Override
        protected void onPostExecute(List<GPSData> result) {
            super.onPostExecute(result);
            try {
                doDrawTrackLine(result);
                if (mMapListener != null)
                    mMapListener.onAfterDrawLineTrack(result);
            } catch (Exception ex) {
            }
        }
    }


    private void setTimeText(final long time, final int altitude, final int speed) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mShowCarInfo)
                    return;

                if (time == Long.MIN_VALUE && altitude == Integer.MIN_VALUE && speed == Integer.MIN_VALUE) {
                    if (mTimeView.getVisibility() == VISIBLE) {
                        mTimeView.setAnimation(AnimationUtils.loadAnimation(getContext(),
                                R.anim.alpha_dismiss));
                        mTimeView.setVisibility(INVISIBLE);
                    }
                } else {
                    if (mTimeView.getVisibility() != VISIBLE) {
                        mTimeView.setAnimation(AnimationUtils.loadAnimation(getContext(),
                                R.anim.alpha_show));
                        mTimeView.setVisibility(View.VISIBLE);
                    }
                }

                String text = "";
                if (mShowCarInfoTime) {
                    text += DateFormat.format("yyyy-MM-dd HH:mm", new Date(time)).toString();
                }
                DecimalFormat df = new DecimalFormat();
                df.applyPattern("0.0");
                mTimeView.setText(text + "   " + getContext().getString(R.string.altitude) + ":" +
                        altitude + "m   " + getContext().getString(R.string.speed) + ":"
                        + df.format(speed * 3.6f) + "km/h");
            }
        });
    }

}
