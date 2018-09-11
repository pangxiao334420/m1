package com.goluk.a6.control.live;

/**
 * Created by goluk_lium on 2018/3/29.
 */

public interface ILiveView {
    void onPrepareLive();
    void onStartLive(String url);
    void showDeviceOffline();
    void onDeviceWakeup();
    void onDeviceWakeupSuccess();
    void updateDeviceState(int state);
    void onLiving();
    void onLiveError();
    void onLiveTimeout();
    void onWakeUpTimeout();
    void onLivePause();
    void onLiveResume();
    void onLiveStop();
    void onLiveFinish();
    void onTcpConnectionClosed();
    void onServerReceivedWakeupCmd();
    void onServerReceivedLiveCmd();
}
