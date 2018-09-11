package com.goluk.a6.internation.login;


import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.GolukFastjsonRequest;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.bean.UpNameResult;
import com.goluk.a6.internation.bean.UserUpdateRetBean;

import java.net.URLEncoder;
import java.util.HashMap;


public class UpdUserNameBeanRequest extends GolukFastjsonRequest<UserUpdateRetBean> {

    public UpdUserNameBeanRequest(int requestType, IRequestResultListener listener) {
        super(requestType, UserUpdateRetBean.class, listener);
    }

    @Override
    protected String getPath() {
        return "/user/my/basic";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String uid, String nickname, String avatar, String description, String emgContactName, String emgContactPhone, String emgCode) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("commuid", uid);
        try {
            headers.put("name", URLEncoder.encode(nickname, "UTF-8"));
        } catch (Exception ex) {
        }
        headers.put("avatar", avatar);
        headers.put("xieyi", "100");
        headers.put("description", description);
        try {
            headers.put("emgContactName", URLEncoder.encode(emgContactName, "UTF-8"));
        } catch (Exception ex) {
        }
        headers.put("emgContactPhone", emgContactPhone);
        headers.put("emgContactCode", emgCode);
        post();
    }
}
