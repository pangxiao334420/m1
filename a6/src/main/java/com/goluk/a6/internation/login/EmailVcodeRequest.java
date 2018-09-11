package com.goluk.a6.internation.login;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.bean.EmailVcodeRetBean;

import java.util.HashMap;

/**
 * Created by leege100 on 2017/1/17.
 * 获取邮箱验证码
 */

public class EmailVcodeRequest extends GolukFastjsonRequest<EmailVcodeRetBean> {

    public EmailVcodeRequest(int requestType, IRequestResultListener listener) {
        super(requestType, EmailVcodeRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/user/vcode/email";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public boolean send(String email, String type) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("email", email);
        headers.put("type", type);
        headers.put("xieyi","100");
        get();
        return true;
    }
}
