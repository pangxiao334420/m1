package com.goluk.a6.http.request;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.ServerBaseResult;

import java.util.HashMap;

/**
 * Created by goluk_lium on 2018/4/10.
 */
public class ShareValiditySettingPostRequest extends GolukFastjsonRequest<ServerBaseResult> {

    public ShareValiditySettingPostRequest(int requestType, IRequestResultListener listener) {
        super(requestType, ServerBaseResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/system/app/setting";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void post(String commuid,String key,int value) {
        HashMap<String, String> headers = (HashMap<String, String>) getParam();
        headers.put("commuid", commuid);
        headers.put("type",key);
        headers.put("value",value+"");
        headers.put("xieyi", "100");
        post();
    }
}