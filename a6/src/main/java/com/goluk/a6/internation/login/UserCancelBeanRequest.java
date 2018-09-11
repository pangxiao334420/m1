package com.goluk.a6.internation.login;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.bean.CancelResult;

import java.util.HashMap;

public class UserCancelBeanRequest extends GolukFastjsonRequest<CancelResult> {

    public UserCancelBeanRequest(int requestType, IRequestResultListener listener) {
        super(requestType, CancelResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/user/logout";
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
