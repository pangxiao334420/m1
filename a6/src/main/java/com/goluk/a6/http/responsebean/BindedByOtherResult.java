package com.goluk.a6.http.responsebean;

import java.util.List;

/**
 * 被其他用户绑走的设备信息
 */
public class BindedByOtherResult {
    public int code;
    public String msg;
    public List<DeviceInfo> data;

    public static class DeviceInfo {
        public String name;
        public String username;
    }

}
