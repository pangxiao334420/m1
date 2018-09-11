package com.goluk.a6.http.request;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.AppStrartResult;
import com.goluk.a6.http.responsebean.BindAddResult;

import java.util.HashMap;

public class BindAddRequest extends GolukFastjsonRequest<BindAddResult> {

    public BindAddRequest(int requestType, IRequestResultListener listener) {
        super(requestType, BindAddResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/carbox/app/binding";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String uid, String bindid, String name, String imei, String sn) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("commuid", uid);
        headers.put("bindId", bindid);
        headers.put("name", name);
        headers.put("imei", imei);
        headers.put("sn", sn);
        post();
    }
}
