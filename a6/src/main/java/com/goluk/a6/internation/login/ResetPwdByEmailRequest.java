package com.goluk.a6.internation.login;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.bean.ResetPwdByEmailRetBean;

import java.util.HashMap;

/**
 * Created by leege100 on 2017/1/17.
 */

public class ResetPwdByEmailRequest extends GolukFastjsonRequest<ResetPwdByEmailRetBean> {

    public ResetPwdByEmailRequest(int requestType, IRequestResultListener listener) {
        super(requestType, ResetPwdByEmailRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/user/password/email";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public boolean send(String email, String pwd, String vcode) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", "200");
        headers.put("email", email);
        headers.put("pwd", GolukUtils.sha256Encrypt(pwd));
        headers.put("vcode", vcode);
        put();
        return true;
    }
}
