package com.goluk.a6.internation;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.bean.UserRegistBean;

import java.util.HashMap;

/**
 * Created by hanzheng on 2016/6/29.
 */
public class UserRegistRequest extends GolukFastjsonRequest<UserRegistBean> {

    public UserRegistRequest(int requestType, IRequestResultListener listener) {
        super(requestType, UserRegistBean.class, listener);
    }
    @Override
    protected String getPath() {
        return "/cdcRegister/getPhoneRegister.htm";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String phone, String pwd, String vcode, String dialingcode) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("phone", phone);
        headers.put("pwd", pwd);
        headers.put("vcode", vcode);
        headers.put("dialingcode", dialingcode);
        get();
    }
}
