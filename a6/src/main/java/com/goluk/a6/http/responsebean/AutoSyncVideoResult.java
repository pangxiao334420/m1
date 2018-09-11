package com.goluk.a6.http.responsebean;

import java.util.List;

/**
 * Created by goluk_lium on 2018/3/1.
 */

public class AutoSyncVideoResult {
    public int code;
    public String msg;
    public List<AutoSyncVideoBean> data;

    public static class AutoSyncVideoBean{
        public int flag;
        public String imei;
    }
}
