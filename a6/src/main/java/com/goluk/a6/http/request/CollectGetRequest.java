package com.goluk.a6.http.request;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.CollectResult;
import com.goluk.a6.http.responsebean.ServerBaseResult;

import java.util.HashMap;

public class CollectGetRequest extends GolukFastjsonRequest<CollectResult> {

    public CollectGetRequest(int requestType, IRequestResultListener listener) {
        super(requestType, CollectResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/record/event/collection";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String uid, String eventId) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("commuid", uid);
        headers.put("eventId", eventId);
        get();
    }
}
