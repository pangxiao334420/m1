package com.goluk.a6.control.live;

/**
 * Created by goluk_lium on 2018/3/29.
 */

public class LiveConstant {
    public static final String CMD_WAKE_UP = "WAKEUP";
    public static final String CMD_WAKE_UP_ACK = "WAKEUP_ACK";
    public static final String CMD_LIVE = "LIVE";
    public static final String CMD_LIVE_ACK = "LIVE_ACK";
    public static final String CMD_LIVE_STOP = "LIVE_STOP";
    public static final String CMD_LIVE_STOP_ACK = "LIVE_STOP_ACK";
    public static final String CMD_ONLINE = "ONLINE";
    public static final String CMD_CLOSE = "CLOSE";
    public static final String CMD_LIVE_CONT_ACK = "LIVE_CONT_ACK";

    public static final String KEY_CAR_IMEI = "car_imei";
    public static final String KEY_CAR_STATE = "car_state";
    public static final String KEY_CAR_LOCATION = "car_location";
    public static final String KEY_DEVICE_CAMERA_NUMBER = "device_camera_number";
    public static final String KEY_USER_NAME = "user_name_";

    public static final int STATE_OFFLINE = 0;
    public static final int STATE_ONLINE = 1;
    public static final int STATE_SLEEP = 2;

    public static final String KEY_JSON_CAMERA_NO = "cameraNo";
}
