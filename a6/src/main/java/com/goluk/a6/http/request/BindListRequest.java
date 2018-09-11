package com.goluk.a6.http.request;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.BindAddResult;
import com.goluk.a6.http.responsebean.BindListResult;

import java.util.HashMap;

public class BindListRequest extends GolukFastjsonRequest<BindListResult> {

    public BindListRequest(int requestType, IRequestResultListener listener) {
        super(requestType, BindListResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/carbox/app/bindings";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String uid) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("commuid", uid);
        headers.put("operation", "0");
        headers.put("index", "0");
        headers.put("pagesize", "20");
        post();
    }
}
