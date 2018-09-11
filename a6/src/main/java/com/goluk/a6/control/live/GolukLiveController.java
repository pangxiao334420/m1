package com.goluk.a6.control.live;

import android.text.TextUtils;
import android.util.Log;

import com.goluk.a6.common.util.GsonUtils;
import com.goluk.a6.cpstomp.CPStompClient;
import com.goluk.a6.http.responsebean.LiveBean;
import com.goluk.a6.http.responsebean.ProtocolMessage;
import com.google.gson.JsonObject;

/**
 * Created by goluk_lium on 2018/3/29.
 */

public class GolukLiveController implements LiveControl {

    private ILiveView mLiveView;

    public GolukLiveController(ILiveView liveView) {
        this.mLiveView = liveView;
    }

    @Override
    public void openLiveConnection() {

    }

    @Override
    public void closeLiveConnection(CPStompClient client) {
        if (client != null) {
            client.disconnect();
        }
    }

    @Override
    public void prepareStartLive(int currentCarState, int camera, CPStompClient client, String IMEI) {
        mLiveView.onPrepareLive();
        if (currentCarState == LiveConstant.STATE_SLEEP) {
            sendWakeupDeviceCmd(client, IMEI);
        } else if (currentCarState == LiveConstant.STATE_OFFLINE) {
            mLiveView.showDeviceOffline();
        } else if (currentCarState == LiveConstant.STATE_ONLINE) {
            sendStartLiveCmd(client, IMEI, camera);
        } else {
            throw new IllegalStateException("car\'s state was error value");
        }
    }

    public void sendStartLiveCmd(CPStompClient client, String IMEI, int cameraNo) {
        ProtocolMessage message = new ProtocolMessage();
        message.clientId = IMEI;
        message.cmd = LiveConstant.CMD_LIVE;
        JsonObject object = new JsonObject();
        object.addProperty(LiveConstant.KEY_JSON_CAMERA_NO, cameraNo);
        message.data = object.toString();
        if (client != null) {
            client.send(GsonUtils.toJsonString(message));
            mLiveView.onLiveTimeout();
        }
        Log.e("GolukLiveActivity", "sendStartLiveCmd: " + GsonUtils.toJsonString(message));
    }


    public void sendStopLiveCmd(CPStompClient client, String IMEI) {
        ProtocolMessage message = new ProtocolMessage();
        message.clientId = IMEI;
        message.cmd = LiveConstant.CMD_LIVE_STOP;
        if (client != null) {
            client.send(GsonUtils.toJsonString(message));
        }

    }

    public void sendWakeupDeviceCmd(CPStompClient client, String IMEI) {
        ProtocolMessage message = new ProtocolMessage();
        message.clientId = IMEI;
        message.cmd = LiveConstant.CMD_WAKE_UP;
        if (client != null) {
            client.send(GsonUtils.toJsonString(message));
            mLiveView.onWakeUpTimeout();
            mLiveView.onDeviceWakeup();
        }
        Log.e("GolukLiveActivity", "sendStartLiveCmd: " + GsonUtils.toJsonString(message));
    }


    @Override
    public void onLive(String msg) {
        LiveBean liveBean = str2LiveBean(msg);
        if (liveBean != null) {
            if (liveBean.code == 0) {
                String cmd = liveBean.beanData.cmd;
                if (cmd.equals(LiveConstant.CMD_LIVE_CONT_ACK)) {
                    return;
                }
                if (liveBean.beanData.code != 0) {
                    mLiveView.onLiveError();
                    return;
                }
                if (liveBean.beanData.code == 2) {
                    switch (cmd) {
                        case LiveConstant.CMD_LIVE:
                        case LiveConstant.CMD_LIVE_ACK:
                            mLiveView.onServerReceivedLiveCmd();
                            break;
                        case LiveConstant.CMD_WAKE_UP:
                        case LiveConstant.CMD_WAKE_UP_ACK:
                            mLiveView.onServerReceivedWakeupCmd();
                            break;
                    }
                    return;
                }

                switch (cmd) {
                    case LiveConstant.CMD_LIVE:
                    case LiveConstant.CMD_LIVE_ACK:
                        mLiveView.onServerReceivedLiveCmd();
                        mLiveView.onStartLive(liveBean.beanData.beanData.rtmpUrl);
                        break;
                    case LiveConstant.CMD_WAKE_UP:
                    case LiveConstant.CMD_WAKE_UP_ACK:
//                        mLiveView.onDeviceWakeup();
                        mLiveView.onServerReceivedWakeupCmd();
                        break;
                    case LiveConstant.CMD_ONLINE:
                        mLiveView.updateDeviceState(LiveConstant.STATE_ONLINE);
                        mLiveView.onDeviceWakeupSuccess();
                        break;
                    case LiveConstant.CMD_LIVE_STOP:
                    case LiveConstant.CMD_LIVE_STOP_ACK:
                        mLiveView.onLiveStop();
                        break;
                    case LiveConstant.CMD_CLOSE:
                        mLiveView.onTcpConnectionClosed();
                        break;
                    case LiveConstant.CMD_LIVE_CONT_ACK:
                        break;
                }
            } else if (liveBean.code == 20013) { //离线码
                String cmd = liveBean.beanData.cmd;
                switch (cmd) {
                    case LiveConstant.CMD_LIVE:
                    case LiveConstant.CMD_LIVE_ACK:
                        mLiveView.onServerReceivedLiveCmd();
                        break;
                    case LiveConstant.CMD_WAKE_UP:
                    case LiveConstant.CMD_WAKE_UP_ACK:
                        mLiveView.onServerReceivedWakeupCmd();
                        break;
                }
                if (liveBean.beanData.cmd.equals(LiveConstant.CMD_WAKE_UP)) {
                    return;
                }
                mLiveView.updateDeviceState(LiveConstant.STATE_OFFLINE);
                mLiveView.showDeviceOffline();
            } else {
                //数据错误
            }
        }

    }

    private LiveBean str2LiveBean(String json) {
        LiveBean liveBean = GsonUtils.toBean(json, LiveBean.class);
        if (!TextUtils.isEmpty(liveBean.data)) {
            liveBean.beanData = GsonUtils.toBean(liveBean.data, LiveBean.LiveDataBean.class);
        }
        if (liveBean.beanData != null && !TextUtils.isEmpty(liveBean.beanData.data)
                && !liveBean.beanData.cmd.equals(LiveConstant.CMD_CLOSE)
                && !liveBean.beanData.cmd.equals(LiveConstant.CMD_ONLINE)) {
            liveBean.beanData.beanData = GsonUtils.toBean(liveBean.beanData.data, LiveBean.LiveDataInfoBean.class);
        }
        return liveBean;
    }
}
