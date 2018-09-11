package com.goluk.a6.http.request;


import android.view.View;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.FamilyCheckResult;
import com.goluk.a6.http.responsebean.ServerBaseResult;

import java.util.HashMap;

public class FamilyAccpetRequest extends GolukFastjsonRequest<ServerBaseResult> {

    public FamilyAccpetRequest(int requestType, IRequestResultListener listener) {
        super(requestType, ServerBaseResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/carbox/share";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String uid, String code) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("commuid", uid);
        headers.put("code", code);
        post();
    }
}
