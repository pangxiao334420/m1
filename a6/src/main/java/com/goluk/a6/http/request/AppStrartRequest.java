package com.goluk.a6.http.request;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.AppStrartResult;

import java.util.HashMap;

public class AppStrartRequest extends GolukFastjsonRequest<AppStrartResult> {

    public AppStrartRequest(int requestType, IRequestResultListener listener) {
        super(requestType, AppStrartResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/system/app/boot";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String uid) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("commuid", uid);
        get();
    }
}
