package com.goluk.a6.internation.login;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.bean.RegistBean;

import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by leege100 on 2017/1/16.
 */

public class InternationalEmailRegisterRequest extends GolukFastjsonRequest<RegistBean> {

    public InternationalEmailRegisterRequest(int requestType, IRequestResultListener listener) {
        super(requestType, RegistBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/user/register/email";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public boolean get(String email, String pwd, String name) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", "200");
        try {
            headers.put("email", URLEncoder.encode(email, "UTF-8"));
            headers.put("name", URLEncoder.encode(name, "UTF-8"));
        } catch (Exception ex) {
        }
        headers.put("pwd", GolukUtils.sha256Encrypt(pwd));
        post();
        return true;
    }
}