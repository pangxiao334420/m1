package com.goluk.a6.internation.login;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.bean.UserResult;

import java.net.URLEncoder;
import java.util.HashMap;


public class UserloginBeanRequest extends GolukFastjsonRequest<UserResult> {
    private boolean email;

    public UserloginBeanRequest(boolean email, int requestType, IRequestResultListener listener) {
        super(requestType, UserResult.class, listener);
        this.email = email;
    }

    public void setEmail(boolean email) {
        this.email = email;
    }

    @Override
    protected String getPath() {
        if (email) {
            return "/user/login/email";
        } else {
            return "/user/login/phone";
        }
    }

    @Override
    protected String getMethod() {
        return "getLogin";
    }

    public void loginByPhone(String phone, String dialingcode, String pwd, String uid) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("dialingcode", dialingcode);
        headers.put("phone", phone);
        headers.put("pwd", GolukUtils.sha256Encrypt(pwd));
        headers.put("commuid", uid);
        post();
    }


    public void loginByEmail(String email, String pwd, String uid) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        try {
            headers.put("email", URLEncoder.encode(email, "UTF-8"));
        } catch (Exception ex) {
        }
        headers.put("pwd", GolukUtils.sha256Encrypt(pwd));
        headers.put("commuid", uid);
        post();
    }

}
