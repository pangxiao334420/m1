package com.goluk.a6.http.request;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.FamilyShareMeResult;

import java.util.HashMap;

public class ShareLiveRequest extends GolukFastjsonRequest<FamilyShareMeResult> {

    public ShareLiveRequest(int requestType, IRequestResultListener listener) {
        super(requestType, FamilyShareMeResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/carbox/share/link";
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
