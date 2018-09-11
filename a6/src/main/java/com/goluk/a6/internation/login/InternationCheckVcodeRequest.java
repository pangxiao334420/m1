package com.goluk.a6.internation.login;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.bean.CheckVcodeBean;

import java.util.HashMap;

/**
 * Created by lily on 16-6-24.
 */
public class InternationCheckVcodeRequest extends GolukFastjsonRequest<CheckVcodeBean> {

    public InternationCheckVcodeRequest(int requestType, IRequestResultListener listener) {
        super(requestType, CheckVcodeBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/user/vcode/check";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public boolean get(String phone, String vcode , String dialingcode) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("phone", phone);
        headers.put("vcode", vcode);
        headers.put("dialingcode", dialingcode);
        headers.put("commostag", "android");
        headers.put("xieyi", "200");
        get();
        return true;
    }

}
