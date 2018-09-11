package com.goluk.a6.common.util;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zenghao on 2016/5/24.
 */
public class JavaScriptInterface {
    /*
         * 绑定的object对象
         * */
    private Context mContext;

    public JavaScriptInterface(Context context) {
        this.mContext = context;
    }

    @JavascriptInterface
    public String getAppCommData() {
        return "";
    }

}
