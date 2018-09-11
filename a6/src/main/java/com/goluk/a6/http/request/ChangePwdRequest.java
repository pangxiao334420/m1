package com.goluk.a6.http.request;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.ServerBaseResult;
import com.goluk.a6.internation.GolukUtils;

import java.util.HashMap;

/**
 * 修改密码
 */
public class ChangePwdRequest extends GolukFastjsonRequest<ServerBaseResult> {

    public ChangePwdRequest(int requestType, IRequestResultListener listener) {
        super(requestType, ServerBaseResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/user/reset/password";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String oldPwd, String newPwd) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("oldpwd", GolukUtils.sha256Encrypt(oldPwd));
        headers.put("newpwd", GolukUtils.sha256Encrypt(newPwd));
        put();
    }
}
