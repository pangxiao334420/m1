package com.goluk.a6.api;

import android.util.Log;

import com.goluk.a6.control.BuildConfig;
import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.http.UrlHostManager;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.bean.UserInfo;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import likly.reverse.HttpMethod;
import likly.reverse.OnServiceInvokeListener;
import likly.reverse.RequestHolder;

/**
 * @author Created by likly on 2017/3/22.
 * @version 1.0
 */

public class OnApiServiceRequestListener implements OnServiceInvokeListener {

    private String TOKEN_KEY;

    @Override
    public RequestHolder onServiceInvoke(Method method, RequestHolder requestHolder) {
        // 这里处于发送请求前,可以对请求Url,参数,Header作处理

        requestHolder.baseUrl(UrlHostManager.getBaseUrl());
        // 添加通用参数,统一都放到Header
        Map<String, String> params = null;
        Object body = requestHolder.body();

        if (requestHolder.method() == HttpMethod.GET || requestHolder.method() == HttpMethod.PUT) {
            params = requestHolder.headers();
            if (body != null && body instanceof Map) {
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) body).entrySet()) {
                    params.put(entry.getKey(), entry.getValue().toString());
                }
            }
        } else {
            if (body != null && body instanceof Map) {
                params = (Map<String, String>) body;
            } else {
                params = new HashMap<>();
            }
        }

//        commversion	应用版本	是	String		0
//        commipcversion	连接设备IPC版本号	是	String			获取设备IPC更新版本时为必须！
//        commapkversion	连接设备APK版本号	是	String			获取设备APK更新版本时为必须！
//        commhdtype	连接设备类型	是	String			G1、G2、T1
//        commmid	手机设备id	是	String
//        commdevmodel	手机设备型号	是	String
//        commostag	手机操作系统类型	是	String			操作系统：ios, android, windows （小写）
//        commosversion	手机操作系统版本	是	String
//        commappversion	手机App版本	是	String			产品版本号，例：2.8.8 获取产品更新版本时为必须
//        commuid	用户ID	否	String
//        commlon	经度	是	Double	[-90.0, 90.0]	0
//        commlat	维度	是	Double	[-180.0, 180.0]	0
//        commlocale	语言代码_国家地区代码	是	String		zh_CN	Locale；语言代码：ISO 639-1，小写两位代码；国家代码：ISO 3166-2，大写两位代码
//        commticket	访问Ticket	否	String			用于访问控制的Ticket，授权成功后由服务端提供token，以token为key计算签名。
//        commtimestamp	客户端当前时间戳	否	String			防止Replay攻击。格式：yyyyMMddHHmmssSSS
        if (params != null) {
            params.put("commuid", getUserId());
            params.put("commmid", "" + GolukUtils.getMobileId());
            params.put("commostag", "android");
            params.put("xieyi", "100");
            params.put("commosversion", android.os.Build.VERSION.RELEASE);
            params.put("commversion", BuildConfig.BRANCH_CHINA ? String.valueOf(0) : String.valueOf(1));
            params.put("commlocale", GolukUtils.getLanguageAndCountryWeb());

            if (requestHolder.method() == HttpMethod.GET || requestHolder.method() == HttpMethod.PUT) {
                requestHolder.body(null);
                requestHolder.headers(params);
            } else {
                if (body != null && body instanceof Map) {
                    // body中是键值对参数
                    requestHolder.body(params);
                } else {
                    params.putAll(requestHolder.headers());
                    requestHolder.headers(params);
                }
                requestHolder.headers().put("Content-Type", "application/json; charset=UTF-8");
            }
        }

        String url = requestHolder.url();
        Log.e("Http", "Url: " + requestHolder.url());

        return requestHolder;
    }

    private String getUserId() {
        UserInfo userInfo = CarControlApplication.getInstance().getMyInfo();
        if (userInfo != null)
            return userInfo.uid;

        return "";
    }

}
