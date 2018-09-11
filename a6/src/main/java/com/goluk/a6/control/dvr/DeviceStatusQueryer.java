package com.goluk.a6.control.dvr;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.goluk.a6.api.ApiUtil;
import com.goluk.a6.api.Callback;
import com.goluk.a6.common.event.util.EventUtil;
import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.control.util.CollectionUtils;
import com.goluk.a6.control.util.DeviceUtil;
import com.goluk.a6.http.responsebean.BindAddResult;
import com.goluk.a6.http.responsebean.BindList;
import com.goluk.a6.http.responsebean.DeviceStatus;
import com.goluk.a6.http.responsebean.TrackDetail;

/**
 * 轮休设备状态Util
 */
public class DeviceStatusQueryer {

    private static final int MSG_TYPE_QUERY_STATUS = 1;

    private Handler mHandler;
    private DeviceStatusCallback mCallback;
    private String mImei;

    // 当前实时轨迹Id
    private String mTrackId;
    // 上次轨迹点offset
    private int mTrackOffset;

    public DeviceStatusQueryer(DeviceStatusCallback callback) {
        mCallback = callback;

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_TYPE_QUERY_STATUS) {
                    queryBindList();
                    sendEmptyMessageDelayed(MSG_TYPE_QUERY_STATUS, 5 * 1000);
                }
            }
        };
    }

    private void queryBindList() {
        if (!CarControlApplication.getInstance().isUserLoginSucess) {
            if (mCallback != null)
                mCallback.onNoDeviceBind();
            return;
        }

        ApiUtil.apiService().queryBindList(0, 0, 20,
                new Callback<BindList>() {
                    @Override
                    protected void onError(int code, String msg) {
                    }

                    @Override
                    public void onResponse(BindList response) {
                        super.onResponse(response);
                        parseBindListData(response);
                    }
                });
    }

    /**
     * 查询设备状态
     */
    private void queryDeviceStatus() {
        if (TextUtils.isEmpty(mImei))
            return;

        ApiUtil.apiService().queryDeviceStatus(mImei, 0, new Callback<DeviceStatus>() {
            @Override
            protected void onError(int code, String msg) {
            }

            @Override
            public void onResponse(DeviceStatus deviceStatus) {
                if (mCallback != null)
                    mCallback.onQueryDeviceStatus(deviceStatus);

                if (deviceStatus != null) {
                    // trackId 相同,说明是同一条轨迹,否则车辆重新点火,是另外一条轨迹
                    boolean isOnline = DeviceUtil.isOnline(deviceStatus);
                    if (isOnline) {
                        if (TextUtils.isEmpty(mTrackId)) {
                            // trackId 为空,代表没有查询过轨迹详情
                            queryTrackDetail(deviceStatus.trackId, true);
                        } else {
                            if (!TextUtils.isEmpty(deviceStatus.trackId)) {
                                boolean trackChanged = !TextUtils.equals(mTrackId, deviceStatus.trackId);
                                int trackOffset = trackChanged ? 0 : mTrackOffset;
                                // 轨迹续加
                                queryRealtimeTrack(deviceStatus.trackId, trackOffset, trackChanged);
                            }
                        }
                    }

                    mTrackId = deviceStatus.trackId;
                }
            }
        });
    }

    /**
     * 查询轨迹详情(只查询一次)
     *
     * @param trackId      轨迹Id
     * @param trackChanged 轨迹是否是同一条
     */
    private void queryTrackDetail(final String trackId, final boolean trackChanged) {
        ApiUtil.apiService().trackDetail(trackId, new Callback<TrackDetail>() {
            @Override
            protected void onError(int code, String msg) {
            }

            @Override
            public void onResponse(TrackDetail trackDetail) {
                mTrackOffset = trackDetail.offset;
                if (mCallback != null)
                    mCallback.onTrackQueryed(trackDetail, trackChanged);
            }
        });
    }

    /**
     * 实时轨迹
     *
     * @param trackId 轨迹Id
     * @param offset  最后轨迹点偏移量
     */
    private void queryRealtimeTrack(String trackId, int offset, final boolean trackChanged) {
        ApiUtil.apiService().realtimeTrack(trackId, offset, new Callback<TrackDetail>() {
            @Override
            protected void onError(int code, String msg) {
            }

            @Override
            public void onResponse(TrackDetail trackDetail) {
                if (trackDetail == null || CollectionUtils.isEmpty(trackDetail.points))
                    return;

                mTrackOffset = trackDetail.offset;
                if (mCallback != null)
                    mCallback.onTrackQueryed(trackDetail, trackChanged);
            }
        });
    }

    private void parseBindListData(BindList bindList) {
        if (bindList == null)
            return;

        // 是否有设备被其他用户绑走
        if (bindList.notice != 0) {
            EventUtil.sendBindedByOtherEvent();
        }

        if (TextUtils.isEmpty(bindList.defaultId) || CollectionUtils.isEmpty(bindList.list)) {
            // 无绑定设备
            saveImeiInfo("", "");

            if (mCallback != null)
                mCallback.onNoDeviceBind();
            return;
        }

        for (BindAddResult.BindBean bind : bindList.list) {
            if (TextUtils.equals(bind.bindId, bindList.defaultId)) {
                saveImeiInfo(bind.imei, bind.iccid);
                queryDeviceStatus();
                break;
            }
        }
    }

    private void saveImeiInfo(String imei, String iccid) {
        mImei = imei;
        CarControlApplication.getInstance().setImei(imei);
        CarControlApplication.getInstance().setIccid(iccid);
    }

    public void start() {
        if (mHandler != null) {
            mHandler.removeMessages(MSG_TYPE_QUERY_STATUS);
            mHandler.sendEmptyMessage(MSG_TYPE_QUERY_STATUS);
        }
    }

    public void pause() {
        if (mHandler != null)
            mHandler.removeMessages(MSG_TYPE_QUERY_STATUS);
    }

    public void stop() {
        pause();

        if (mHandler != null)
            mHandler = null;
        if (mCallback != null)
            mCallback = null;
    }

    public interface DeviceStatusCallback {

        /**
         * 查询到设备状态
         *
         * @param deviceStatus DeviceStatus
         */
        void onQueryDeviceStatus(DeviceStatus deviceStatus);

        /**
         * 当前无绑定设备
         */
        void onNoDeviceBind();

        void onTrackQueryed(TrackDetail trackDetail, boolean trackChanged);

    }

}
