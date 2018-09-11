package com.goluk.a6.http.request;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.AutoSyncVideoResult;

import java.util.HashMap;

/**
 * Created by goluk_lium on 2018/3/1.
 */

public class QueryAutoSyncVideoRequest extends GolukFastjsonRequest<AutoSyncVideoResult> {

    public QueryAutoSyncVideoRequest(int requestType, IRequestResultListener listener) {
        super(requestType, AutoSyncVideoResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/system/auto/upload/event/video";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String commuid, String imei) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("commuid", commuid);
        headers.put("imei", imei);
        headers.put("xieyi", "100");
        get();
    }
}
