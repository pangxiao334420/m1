package com.goluk.a6.http.request;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.AppStrartResult;
import com.goluk.a6.http.responsebean.ServerBaseResult;

import java.util.HashMap;

public class PushTokenRequest extends GolukFastjsonRequest<ServerBaseResult> {

    public PushTokenRequest(int requestType, IRequestResultListener listener) {
        super(requestType, ServerBaseResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/user/tid";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String token,String uid) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("commuid", uid);
        headers.put("tid", token);
        post();
    }
}
