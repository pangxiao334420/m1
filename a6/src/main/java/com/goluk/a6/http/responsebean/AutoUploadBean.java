package com.goluk.a6.http.responsebean;

/**
 * Created by goluk_lium on 2018/3/6.
 */

public class AutoUploadBean {
    public int code;
    public String msg;
    public String data;
    public AutoUploadDataBean beanData;

    public static class AutoUploadDataBean {
        public String clientId;
        public String cmd;
        public String session;
        public int code;
        public String data;
        public AutoUploadDataInfoBean dataInfo;
    }


    public static class AutoUploadDataInfoBean {
        public String flag;
        public String imei;
    }

}
