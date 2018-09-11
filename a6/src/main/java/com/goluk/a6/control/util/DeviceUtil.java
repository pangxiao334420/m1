package com.goluk.a6.control.util;

import com.goluk.a6.control.R;
import com.goluk.a6.control.live.LiveConstant;
import com.goluk.a6.http.responsebean.DeviceStatus;

/**
 * 设备Util
 */
public class DeviceUtil {

    public static boolean isOffline(DeviceStatus deviceStatus) {
        return deviceStatus != null && deviceStatus.state == LiveConstant.STATE_OFFLINE;
    }

    public static boolean isOnline(DeviceStatus deviceStatus) {
        return deviceStatus != null && deviceStatus.state == LiveConstant.STATE_ONLINE;
    }

    public static boolean isDormant(DeviceStatus deviceStatus) {
        return deviceStatus != null && deviceStatus.state == LiveConstant.STATE_SLEEP;
    }

    public static boolean isOnlineOrDormant(DeviceStatus deviceStatus) {
        return deviceStatus != null &&
                (deviceStatus.state == LiveConstant.STATE_ONLINE || deviceStatus.state == LiveConstant.STATE_SLEEP);
    }

    public static int getStateStringResidByState(DeviceStatus deviceStatus) {
        if (deviceStatus == null)
            return -1;
        if (isOnline(deviceStatus))
            return R.string.device_state_online;
        if (isOffline(deviceStatus))
            return R.string.device_state_offline;
        if (isDormant(deviceStatus))
            return R.string.device_state_dormant;

        return -1;
    }

}
