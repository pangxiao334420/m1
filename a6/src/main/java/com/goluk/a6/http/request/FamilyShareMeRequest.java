package com.goluk.a6.http.request;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.FamilyCheckResult;
import com.goluk.a6.http.responsebean.FamilyShareMeResult;

import java.util.HashMap;

public class FamilyShareMeRequest extends GolukFastjsonRequest<FamilyShareMeResult> {

    public FamilyShareMeRequest(int requestType, IRequestResultListener listener) {
        super(requestType, FamilyShareMeResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/carbox/share/invitation";
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
