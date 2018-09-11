package com.goluk.a6.http.request;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.DeviceSleepSettingResult;

import java.util.HashMap;

/**
 * Created by goluk_lium on 2018/3/1.
 */

public class DeviceSleepSettingGetRequest extends GolukFastjsonRequest<DeviceSleepSettingResult> {

    public DeviceSleepSettingGetRequest(int requestType, IRequestResultListener listener) {
        super(requestType, DeviceSleepSettingResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/message/device/setting";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String imei) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("imei", imei);
        headers.put("xieyi", "100");
        get();
    }
}
