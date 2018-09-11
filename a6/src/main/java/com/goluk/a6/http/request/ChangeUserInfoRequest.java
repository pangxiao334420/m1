package com.goluk.a6.http.request;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.ServerBaseResult;

import java.util.HashMap;

/**
 * 修改邮箱
 */
public class ChangeUserInfoRequest extends GolukFastjsonRequest<ServerBaseResult> {

    public ChangeUserInfoRequest(int requestType, IRequestResultListener listener) {
        super(requestType, ServerBaseResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/user/login/info";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    /**
     * 修改邮箱
     *
     * @param email 邮箱
     */
    public void get(String email) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("type", "2");
        headers.put("email", email);
        put();
    }

    /**
     * 修改手机号
     *
     * @param phone       手机号
     * @param dialingcode 地区
     * @param vcode       验证码
     */
    public void get(String phone, String dialingcode, String vcode) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("type", "1");
        headers.put("phone", phone);
        headers.put("dialingcode", dialingcode);
        headers.put("vcode", vcode);
        put();
    }

}
