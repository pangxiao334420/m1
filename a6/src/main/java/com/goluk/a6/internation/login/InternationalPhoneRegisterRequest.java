package com.goluk.a6.internation.login;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.bean.RegistBean;

import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by lily on 16-6-27.
 */
public class InternationalPhoneRegisterRequest extends GolukFastjsonRequest<RegistBean> {

    public InternationalPhoneRegisterRequest(int requestType, IRequestResultListener listener) {
        super(requestType, RegistBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/user/register/phone";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public boolean get(String name, String phone, String pwd, String vcode, String dialingcode, String step2Code) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("xieyi", "200");
        headers.put("phone", phone);
        try {
            headers.put("name", URLEncoder.encode(name, "UTF-8"));
        } catch (Exception ex) {
        }
        headers.put("pwd", GolukUtils.sha256Encrypt(pwd));
        headers.put("vcode", step2Code);
        headers.put("dialingcode", dialingcode);
        post();
        return true;
    }
}
