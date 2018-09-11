package com.goluk.a6.http.request;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.ServerBaseResult;

import java.util.HashMap;

public class DeviceStatusRequest extends GolukFastjsonRequest<DeviceStatusBean> {

    public DeviceStatusRequest(int requestType, IRequestResultListener listener) {
        super(requestType, DeviceStatusBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/carbox/info";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String commuid, String imei) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("commuid", commuid);
        headers.put("imei", imei);
        headers.put("type", "0");
        get();
    }
}
