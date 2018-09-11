package com.goluk.a6.http.request;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.FamilyCheckResult;
import com.goluk.a6.http.responsebean.FamilyEventResult;

import java.util.HashMap;

public class FamilyEventRequest extends GolukFastjsonRequest<FamilyEventResult> {

    public FamilyEventRequest(int requestType, IRequestResultListener listener) {
        super(requestType, FamilyEventResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/carbox/share/events";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String uid, String operation, String index) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("commuid", uid);
        headers.put("operation", operation);
        headers.put("index", index);
        headers.put("pagesize", "20");
        get();
    }
}
