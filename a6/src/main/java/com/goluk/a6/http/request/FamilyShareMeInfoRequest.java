package com.goluk.a6.http.request;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.FamilyShareMeInfoResult;
import com.goluk.a6.http.responsebean.FamilyShareMeResult;

import java.util.HashMap;

public class FamilyShareMeInfoRequest extends GolukFastjsonRequest<FamilyShareMeInfoResult> {

    public FamilyShareMeInfoRequest(int requestType, IRequestResultListener listener) {
        super(requestType, FamilyShareMeInfoResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/carbox/share/invitation/info";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String code) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("code", code);
        get();
    }
}
