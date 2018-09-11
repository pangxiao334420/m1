package com.goluk.a6.internation;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.bean.UserinfohomeRetBean;

import java.util.HashMap;

public class UserInfohomeRequest extends GolukFastjsonRequest<UserinfohomeRetBean> {
    public UserInfohomeRequest(int requestType, IRequestResultListener listener) {
        super(requestType, UserinfohomeRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/user/my/basic";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public void get(String xieyi, String uid) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", xieyi);
        headers.put("commuid", uid);
        get();
    }
}

