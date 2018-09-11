package com.goluk.a6.http.request;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.MessageResult;
import com.goluk.a6.http.responsebean.ServerBaseResult;

import java.util.HashMap;

public class MessageSetRequest extends GolukFastjsonRequest<ServerBaseResult> {

    public MessageSetRequest(int requestType, IRequestResultListener listener) {
        super(requestType, ServerBaseResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/system/app/setting";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String uid, String type, int value) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("commuid", uid);
        headers.put("type", type);
        headers.put("value", String.valueOf(value));
        post();
    }
}
