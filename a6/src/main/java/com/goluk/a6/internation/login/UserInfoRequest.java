package com.goluk.a6.internation.login;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.bean.UserInfoBean;
import com.goluk.a6.internation.bean.UserResult;

import java.util.HashMap;


public class UserInfoRequest extends GolukFastjsonRequest<UserInfoBean> {

    public UserInfoRequest(int requestType, IRequestResultListener listener) {
        super(requestType, UserInfoBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/user/info";
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
