package com.goluk.a6.http;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.control.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class UrlHostManager {
    public static final String Scheme_Splitter = "://";
    public static final String WSScheme = "ws://";
    private static final String LIVE_SOCKET = "/message/websocket/websocket";
    UrlHostManager() {
    }

    public static String getEncodedUrlParams(Map<String, String> params)
            throws AuthFailureError {

        StringBuilder encodedParams = new StringBuilder();
        String paramsEncoding = "UTF-8";
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (null == entry.getValue()) {
                    continue;
                }
                encodedParams.append(URLEncoder.encode(entry.getKey(),
                        paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(),
                        paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: "
                    + paramsEncoding, uee);
        }
    }

    public String getHost() {
        return CarControlApplication.getInstance().getString(R.string.base_scheme) + Scheme_Splitter + CarControlApplication.getInstance().getString(R.string.base_url);
    }

    public static String getBaseUrl() {
        return CarControlApplication.getInstance().getString(R.string.base_scheme) + Scheme_Splitter + CarControlApplication.getInstance().getString(R.string.base_url);
    }

    public static String getWebPageHost() {
        return CarControlApplication.getInstance().getString(R.string.base_scheme) + Scheme_Splitter + CarControlApplication.getInstance().getString(R.string.base_web_url);
    }

    public static String getLiveWebSocketAddress() {
        return WSScheme + CarControlApplication.getInstance().getString(R.string.base_url) + LIVE_SOCKET;
    }
}
