package com.goluk.a6.http.responsebean;

public class AppStrartResult {
    public int code;
    public String msg;
    public AppStartResultBean data;

    public static class AppStartResultBean {
        public String splashscreen;
        public String imei;
    }
}
