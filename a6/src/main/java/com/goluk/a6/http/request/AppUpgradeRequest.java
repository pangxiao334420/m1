package com.goluk.a6.http.request;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.AppStrartResult;
import com.goluk.a6.http.responsebean.AppUpgradeResult;

import java.util.HashMap;

public class AppUpgradeRequest extends GolukFastjsonRequest<AppUpgradeResult> {

    public AppUpgradeRequest(int requestType, IRequestResultListener listener) {
        super(requestType, AppUpgradeResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/system/upgrade";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String uid, String commappversion) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("commuid", uid);
        headers.put("commappversion", commappversion);
        headers.put("commapkversion", "");
        headers.put("commipcversion", "");
        get();
    }
}
