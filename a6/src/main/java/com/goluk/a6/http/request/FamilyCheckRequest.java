package com.goluk.a6.http.request;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.AppStrartResult;
import com.goluk.a6.http.responsebean.FamilyCheckResult;

import java.util.HashMap;

public class FamilyCheckRequest extends GolukFastjsonRequest<FamilyCheckResult> {

    public FamilyCheckRequest(int requestType, IRequestResultListener listener) {
        super(requestType, FamilyCheckResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/carbox/shares";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String uid) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("commuid", uid);
        get();
    }
}
