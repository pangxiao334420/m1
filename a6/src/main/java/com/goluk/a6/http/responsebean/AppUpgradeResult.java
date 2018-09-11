package com.goluk.a6.http.responsebean;

import java.util.Date;

public class AppUpgradeResult {
    public int code;
    public String msg;
    public AppUpgradeBean data;

    public static class AppUpgradeBean {
        public AppInfoBean app;
        public AppInfoBean apk;
        public AppInfoBean ipc;
    }

    public static class AppInfoBean {
        public String platform;
        public String version;
        public String packageprefix;
        public String description;
        public String fileurl;
        public int filesize;
        public int isforce;
        public String md5;
        public Date releasetime;
    }
}
