package com.goluk.a6.internation.login;


import com.goluk.a6.control.BuildConfig;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.bean.UserResult;

import java.util.HashMap;


public class OtherUserloginBeanRequest extends GolukFastjsonRequest<UserResult> {

    public OtherUserloginBeanRequest(int requestType, IRequestResultListener listener) {
        super(requestType, UserResult.class, listener);
    }

    @Override
    protected String getPath() {
        if(BuildConfig.BRANCH_CHINA) {
            return "/user/authlogin?platform=weixin&xieyi=100&commmid=" + GolukUtils.getMobileId() + "&commostag=android";
        }else{
            return "/user/authlogin?platform=facebook&xieyi=100&commmid=" + GolukUtils.getMobileId() + "&commostag=android";
        }
    }

    @Override
    protected String getMethod() {
        return "";
    }


    public void get(HashMap<String, String> other) {
        HashMap<String, String> body = (HashMap<String, String>) getParam();
        HashMap<String, String> header = (HashMap<String, String>) getHeader();
        body.putAll(other);
        header.put("Content-Type", "application/json; charset=utf-8");
        postS();
    }
}
