package com.goluk.a6.http.request;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.ServerBaseResult;

import java.util.HashMap;

public class FamilyDeleteRequest extends GolukFastjsonRequest<ServerBaseResult> {

    public FamilyDeleteRequest(int requestType, IRequestResultListener listener) {
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

    public void get(String uid, String otherId) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("commuid", uid);
        headers.put("otheruid", otherId);
        delete();
    }
}
