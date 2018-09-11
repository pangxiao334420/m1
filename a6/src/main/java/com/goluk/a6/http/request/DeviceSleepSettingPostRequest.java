package com.goluk.a6.http.request;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.BindAddResult;
import com.goluk.a6.http.responsebean.ServerBaseResult;

import java.util.HashMap;

public class DeviceSleepSettingPostRequest extends GolukFastjsonRequest<ServerBaseResult> {

    public DeviceSleepSettingPostRequest(int requestType, IRequestResultListener listener) {
        super(requestType, ServerBaseResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/message/device/setting";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void post(String imei, String key,int value) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("xieyi","100");
        headers.put("imei", imei);
        headers.put("type", key);
        headers.put("value",value+"");
        post();
    }
}
