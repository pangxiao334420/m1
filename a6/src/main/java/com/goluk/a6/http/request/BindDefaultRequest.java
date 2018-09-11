package com.goluk.a6.http.request;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.BindAddResult;
import com.goluk.a6.http.responsebean.ServerBaseResult;

import java.util.HashMap;

public class BindDefaultRequest extends GolukFastjsonRequest<ServerBaseResult> {

    public BindDefaultRequest(int requestType, IRequestResultListener listener) {
        super(requestType, ServerBaseResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/carbox/app/binding/default";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String uid, String bindid) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("commuid", uid);
        headers.put("bindId", bindid);
        post();
    }
}
