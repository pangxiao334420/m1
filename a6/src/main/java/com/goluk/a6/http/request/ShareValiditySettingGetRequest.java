package com.goluk.a6.http.request;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.ShareValiditySettingResult;

import java.util.HashMap;

/**
 * Created by goluk_lium on 2018/4/10.
 */
public class ShareValiditySettingGetRequest extends GolukFastjsonRequest<ShareValiditySettingResult> {

    public ShareValiditySettingGetRequest(int requestType, IRequestResultListener listener) {
        super(requestType, ShareValiditySettingResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/system/app/setting";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void get(String commuid) {
        HashMap<String, String> headers = (HashMap<String, String>) getHeader();
        headers.put("commuid", commuid);
        headers.put("xieyi", "100");
        get();
    }
}