package com.goluk.a6.http.request;

import java.util.List;

public class DeviceStatusBean {
    public int code;
    public String msg;
    public DeviceStatusDataBean data;

    public class DeviceStatusDataBean {
        public String carboxId;
        public String uid;
        public String licencePlate;
        public String name;
        public String imei;
        public String sn;
        public String lastPhoto;
        public String trackId;
        /**
         * 0: 离线；1: 在线；2: 休眠
         */
        public int state;
        public double lastLon;
        public double lastLat;
        public List<String> cameraSN;
    }
}

