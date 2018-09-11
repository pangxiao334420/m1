package com.goluk.a6.control.live;

import com.goluk.a6.cpstomp.CPStompClient;

/**
 * Created by goluk_lium on 2018/3/29.
 */

public interface LiveControl {
    void openLiveConnection();
    void closeLiveConnection(CPStompClient client);
    void prepareStartLive(int state, int camera,CPStompClient client, String IMEI);
    void onLive(String msg);
    void sendStopLiveCmd(CPStompClient client,String IMEI);
    void sendStartLiveCmd(CPStompClient client,String IMEI,int cameraNo);
    void sendWakeupDeviceCmd(CPStompClient client,String IMEI);

}
