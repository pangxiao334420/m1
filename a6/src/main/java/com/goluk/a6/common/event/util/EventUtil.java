package com.goluk.a6.common.event.util;

import org.greenrobot.eventbus.EventBus;

public class EventUtil {

    /* 设备连接成功 */
    private static final int EVENT_DEVICE_CONNECTED = 1;
    /* 发起开始连接指令 */
    private static final int EVENT_START_CONNECT = 2;
    /* 绑定设备指令 */
    private static final int EVENT_BIND_DEVICE = 3;
    /* 设备被其他用户绑走 */
    private static final int EVENT_DEVICE_BIND_BY_OTHER = 4;
    /* 绑定设备成功 */
    private static final int EVENT_BIND_DEVICE_SUCCESS = 5;
    /* 默认设备发生变化 */
    private static final int EVENT_DEFAULT_DEVICE_CHANGED = 6;
    /* 设备SIM状态变化 */
    private static final int EVENT_SIM_STATE_CHANGED = 6;

    /////////////////////////////////////////////////////////////////////////

    /**
     * 发送设备连接成功Event
     */
    public static void sendConnectedEvent(String imei) {
        Event event = Event.create(EVENT_DEVICE_CONNECTED, imei);
        EventBus.getDefault().post(event);
    }

    public static boolean isConnectedEvent(Event event) {
        return event != null && event.type == EVENT_DEVICE_CONNECTED;
    }

    /**
     * 发起开始连接指令
     */
    public static void sendStartConnectEvent() {
        Event event = Event.create(EVENT_START_CONNECT);
        EventBus.getDefault().post(event);
    }

    public static boolean isStartConnectEvent(Event event) {
        return event != null && event.type == EVENT_START_CONNECT;
    }

    /**
     * 绑定设备指令
     */
    public static void sendBindDeviceEvent(String imei) {
        Event event = Event.create(EVENT_BIND_DEVICE, imei);
        EventBus.getDefault().post(event);
    }

    public static boolean isBindDeviceEvent(Event event) {
        return event != null && event.type == EVENT_BIND_DEVICE;
    }

    /**
     * 设备被其他用户绑走
     */
    public static void sendBindedByOtherEvent() {
        Event event = Event.create(EVENT_DEVICE_BIND_BY_OTHER);
        EventBus.getDefault().post(event);
    }

    public static boolean isBindedByOtherEvent(Event event) {
        return event != null && event.type == EVENT_DEVICE_BIND_BY_OTHER;
    }

    /**
     * 绑定设备成功
     */
    public static void sendBindDeviceSuccessEvent() {
        Event event = Event.create(EVENT_BIND_DEVICE_SUCCESS);
        EventBus.getDefault().post(event);
    }

    public static boolean isBindDeviceSuccessEvent(Event event) {
        return event != null && event.type == EVENT_BIND_DEVICE_SUCCESS;
    }

    /**
     * 默认设备发生变化
     */
    public static void sendDefaultDeviceChangedEvent() {
        Event event = Event.create(EVENT_DEFAULT_DEVICE_CHANGED);
        EventBus.getDefault().post(event);
    }

    public static boolean isDefaultDeviceChangedEvent(Event event) {
        return event != null && event.type == EVENT_DEFAULT_DEVICE_CHANGED;
    }

    public static void sendSIMStateChangedEvent() {
        Event event = Event.create(EVENT_SIM_STATE_CHANGED);
        EventBus.getDefault().post(event);
    }

    public static boolean isSIMStateChangedEvent(Event event) {
        return event != null && event.type == EVENT_SIM_STATE_CHANGED;
    }

}
