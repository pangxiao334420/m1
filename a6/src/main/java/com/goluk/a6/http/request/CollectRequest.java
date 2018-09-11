package com.goluk.a6.http.request;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.FamilyEventResult;
import com.goluk.a6.http.responsebean.ServerBaseResult;

import java.util.HashMap;

public class CollectRequest extends GolukFastjsonRequest<ServerBaseResult> {

    public CollectRequest(int requestType, IRequestResultListener listener) {
        super(requestType, ServerBaseResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/record/event/collection";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String uid, String eventId, boolean add) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("commuid", uid);
        headers.put("eventId", eventId);
        headers.put("type", add ? "1" : "0");
        post();
    }
}
