package com.goluk.a6.cpstomp;

/**
 * WebSocket Const
 */
public class SocketConst {

    public static final int RESULT_CODE_OK = 0;
    public static final int RESULT_CODE_OFFLINE = 20013;

    public static final int ERROR_CODE_FILENOTEXIST = 10001;
    public static final int ERROR_CODE_SIGN_FAILED = 20001;
    public static final int ERROR_CODE_UPLOAD_FAILED = 21001;

    /* 心跳间隔 */
    public static final int HOLD_CONNECT_TIME = 50 * 1000;

    /* 直播 */
    public static final String CMD_LIVE = "LIVE";
    public static final String CMD_LIVE_ACK = "LIVE_ACK";
    /* 停止直播 */
    public static final String CMD_STOP_LIVE = "LIVE_STOP";
    public static final String CMD_STOP_LIVE_ACK = "LIVE_STOP_ACK";
    /* 唤醒 */
    public static final String CMD_WAKEUP = "WAKEUP";
    public static final String CMD_WAKEUP_ACK = "WAKEUP_ACK";
    /* 唤醒成功状态回复 */
    public static final String ONLINE = "ONLINE";
    /* 停止直播 */
    public static final String CMD_LIVE_STOP = "LIVE_STOP";
    public static final String CMD_LIVE_STOP_ACK = "LIVE_STOP_ACK";
    /* 按天查询视频时间段 */
    public static final String CMD_LOOP_VIDEO = "LOOP_VIDEO";
    public static final String CMD_LOOP_VIDEO_ACK = "LOOP_VIDEO_ACK";
    /* 提取循环视频 */
    public static final String CMD_EXTRACT_LOOP_VIDEO = "EXTRACT_VIDEO";
    public static final String CMD_EXTRACT_LOOP_VIDEO_ACK = "EXTRACT_VIDEO_ACK";
    /* 提取事件视频 */
    public static final String CMD_EXTRACT_EVENT_VIDEO = "VIDEO";
    public static final String CMD_EXTRACT_EVENT_VIDEO_ACK = "VIDEO_ACK";
    /* 信号强度/网速 */
    public static final String CMD_SIGNAL_STRANGTH_ACK = "SIGNAL_STRANGTH_ACK";
    /* 设置中控流量开关 */
    public static final String CMD_FLOW_SWITCH = "FLOW_SWITCH";
    public static final String CMD_FLOW_SWITCH_ACK = "FLOW_SWITCH_ACK";
    /* 获取循环有效视频天数 */
    public static final String CMD_LOOP_VIDEO_DAY = "LOOP_VIDEO_DAY";
    public static final String CMD_LOOP_VIDEO_DAY_ACK = "LOOP_VIDEO_DAY_ACK";

}
