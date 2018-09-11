package com.goluk.a6.cpstomp;

import com.goluk.a6.http.responsebean.ProtocolMessage;

import org.json.JSONException;
import org.json.JSONObject;

import likly.dollar.$;

/**
 * Websocket data sender
 */
public class DataSender {

    private CPStompClient mClient;
    private String mImei;

    public DataSender(CPStompClient client) {
        this.mClient = client;
    }

    public void connect(CPStompClient.StompConnListener stompConnListener) {
        if (mClient != null)
            mClient.connect(stompConnListener);
    }

    /**
     * 关注某个设备
     *
     * @param imei ImEI
     */
    public void topic(String imei) {
        if (mClient != null) {
            mClient.topic(imei);
            this.mImei = imei;
        }
    }


    /**
     * 唤醒设备
     */
    public void wakeup() {
        if (mClient != null) {
            ProtocolMessage message = new ProtocolMessage();
            message.clientId = mImei;
            message.cmd = SocketConst.CMD_WAKEUP;
            String data = $.json().toJson(message);
            mClient.send(data);
        }
    }

    /**
     * 开始直播
     *
     * @param frontCamera 是否前置摄像头
     */
    public void startLive(boolean frontCamera) {
        if (mClient != null) {
            ProtocolMessage message = new ProtocolMessage();
            message.clientId = mImei;
            message.cmd = SocketConst.CMD_LIVE;
            JSONObject object = new JSONObject();
            try {
                object.put("cameraNo", frontCamera ? "0" : "1");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            message.data = object.toString();
            String data = $.json().toJson(message);
            mClient.send(data);
        }
    }

    /**
     * 停止直播
     */
    public void stopLive() {
        ProtocolMessage message = new ProtocolMessage();
        message.clientId = mImei;
        message.cmd = SocketConst.CMD_STOP_LIVE;
        String data = $.json().toJson(message);
        mClient.send(data);
    }

    /**
     * 提取事件视频
     *
     * @param eventId     事件ID
     * @param videoName   视频名
     * @param frontCamera 是否前置摄像头
     */
    public void extractEventVideo(String eventId, String videoName, boolean frontCamera) {
        if (mClient != null) {
            ProtocolMessage message = new ProtocolMessage();
            message.clientId = mImei;
            message.cmd = SocketConst.CMD_EXTRACT_EVENT_VIDEO;
            JSONObject object = new JSONObject();
            try {
                object.put("eventId", eventId);
                object.put("videoName", videoName);
                object.put("cameraNo", frontCamera ? "0" : "1");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            message.data = object.toString();
            String data = $.json().toJson(message);
            mClient.send(data);
        }
    }

    public void destory() {
        if (mClient != null) {
            mClient.disconnect();
        }
    }

}
