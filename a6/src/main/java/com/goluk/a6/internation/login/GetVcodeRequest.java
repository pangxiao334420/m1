package com.goluk.a6.internation.login;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.bean.CheckVcodeBean;

import java.util.HashMap;

public class GetVcodeRequest extends GolukFastjsonRequest<CheckVcodeBean> {
    /**
     * 新手机号（注册、绑定、更换）
     */
    public static final String GET_NEW_CODE = "1";
    /**
     * 已注册手机号（重置/修改密码）
     */
    public static final String GET_RESET_CODE = "2";


    public GetVcodeRequest(int requestType, IRequestResultListener listener) {
        super(requestType, CheckVcodeBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/user/vcode";
    }

    @Override
    protected String getMethod() {
        return null;
    }

    public boolean get(String phone, String dialingcode, String type) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("phone", phone);
        headers.put("type", type);
        headers.put("dialingcode", dialingcode);
        headers.put("commostag", "android");
        headers.put("xieyi", "200");
        get();
        return true;
    }

}
