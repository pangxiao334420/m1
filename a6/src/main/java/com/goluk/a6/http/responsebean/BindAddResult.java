package com.goluk.a6.http.responsebean;

public class BindAddResult {
    public int code;
    public String msg;
    public BindBean data;

    public static class BindBean {
        public String bindId;
        public String imei;
        public String name;
        public String sn;
        public int index;
        public String iccid;
    }
}
