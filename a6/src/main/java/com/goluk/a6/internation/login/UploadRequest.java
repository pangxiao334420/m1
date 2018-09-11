package com.goluk.a6.internation.login;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.bean.SignRetBean;

import java.util.HashMap;

public class UploadRequest extends GolukFastjsonRequest<SignRetBean> {

    public UploadRequest(int requestType, IRequestResultListener listener) {
        super(requestType, SignRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/system/cloud/sign";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public boolean send() {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("type", "2");
        get();
        return true;
    }
}
