package com.goluk.a6.http.responsebean;

import java.util.List;

/**
 * 设备状态信息
 */
public class DeviceStatus {

    public String carboxId;
    public String uid;
    public String licencePlate;
    public String name;
    public String imei;
    public String sn;
    public String lastPhoto;
    public String trackId;
    /* 0: 离线；1: 在线；2: 休眠 */
    public int state;
    public double lastLon;
    public double lastLat;
    public List<String> cameraSN;
    /* 最后定位时间 */
    public long pointTime;

}
