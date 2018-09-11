package com.goluk.a6.internation.login;


import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.bean.TokenRetBean;

import java.util.HashMap;

public class MyTokenRequest extends GolukFastjsonRequest<TokenRetBean> {

    public MyTokenRequest(int requestType, IRequestResultListener listener) {
        super(requestType, TokenRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/user/my/token";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public boolean send() {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("commuid", CarControlApplication.getInstance().getMyInfo().uid);
        get();
        return true;
    }
}
