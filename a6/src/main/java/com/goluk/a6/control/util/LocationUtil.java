package com.goluk.a6.control.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Location Util
 */
public class LocationUtil implements LocationListener {

    private static final int MIN_DISTANCE = 5;
    private static final int TIME_INTERVAL = 10 * 1000;

    private Context mContext;
    private LocationManager mLocationManager;
    private boolean mIsRunning;
    private OnLocationListener mListener;

    public LocationUtil(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
    }

    public void setListener(OnLocationListener listener) {
        mListener = listener;
    }

    public void startLocation() {
        if (mIsRunning)
            return;

        if (PermissionUtils.hasLocationgPermission(mContext)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_INTERVAL, MIN_DISTANCE, this);
            mIsRunning = true;
        }
    }

    public void pauseLocation() {
        if (mIsRunning) {
            mLocationManager.removeUpdates(this);
            mIsRunning = false;
        }
    }

    public void stopLocation() {
        mLocationManager.removeUpdates(this);
        mLocationManager = null;
        mContext = null;
        mListener = null;
        mIsRunning = false;
    }

    /**
     * 获取最后一次可用位置信息
     *
     * @return Location
     */
    public Location getLstknownLocation() {
        if (!PermissionUtils.hasLocationgPermission(mContext))
            return null;

        try {
            mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            String provider = judgeProvider(mLocationManager);
            if (provider == null) {
                return null;
            }
            return mLocationManager.getLastKnownLocation(provider);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String judgeProvider(LocationManager locationManager) {
        List<String> prodiverlist = locationManager.getProviders(true);
        if (prodiverlist.contains(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        } else if (prodiverlist.contains(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        } else {
            // no avalible location provider
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mListener != null)
            mListener.onLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    public interface OnLocationListener {
        void onLocation(Location location);
    }

}
