package com.goluk.a6.common.util;

import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.control.R;
import com.goluk.a6.control.WebviewActivity;
import com.goluk.a6.http.UrlHostManager;
import com.goluk.a6.http.request.MessageDataBean;
import com.goluk.a6.http.request.MessageEventBean;
import com.goluk.a6.http.request.MyCarTrackBean;
import com.goluk.a6.http.request.MyCarTrackDataBean;
import com.goluk.a6.http.request.TrackDetailBean;
import com.goluk.a6.http.responsebean.FamilyEventResult;
import com.goluk.a6.internation.GolukUtils;

public class H5Util {
    public static boolean h5ShouldOverrideUrlLoading(Context context, String url) {
        if (url.startsWith("protocol://event-detail")) {
            try {
                int index = url.indexOf("data=");
                if (index > -1) {
                    int start = index + 5;
                    int end = url.length();
                    String data = url.substring(start, end);
                    MessageEventBean bean = null;
                    try {
                        bean = JSON.parseObject(data, MessageEventBean.class);
                    } catch (Exception ex) {
                    }
                    if (bean != null && bean.data != null) {
                        if (!CarControlApplication.getInstance().isUserLoginToServerSuccess()) {
//                            Intent intent = new Intent(context, InternationUserLoginActivity.class);
//                            context.startActivity(intent);
                            return true;
                        }
                        Intent intent = new Intent(context, WebviewActivity.class);
                        intent.putExtra(WebviewActivity.KEY_WEBVIEW_TITLE, context.getString(R.string.event_detail));
                        intent.putExtra(WebviewActivity.KEY_WEBVIEW_URL, getMessageDetailUrl(bean.data));
                        intent.putExtra(WebviewActivity.KEY_EXTRAL, bean.data.eventId);
                        intent.putExtra(WebviewActivity.KEY_BUTTON, true);
                        context.startActivity(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (url.startsWith("protocol://track-list")) {
            try {
                int index = url.indexOf("data=");
                if (index > -1) {
                    int start = index + 5;
                    int end = url.length();
                    String data = url.substring(start, end);
                    MyCarTrackBean bean = JSON.parseObject(data, MyCarTrackBean.class);
                    if (bean != null && bean.data != null) {
                        Intent intent = new Intent(context, WebviewActivity.class);
                        intent.putExtra(WebviewActivity.KEY_WEBVIEW_TITLE, context.getString(R.string.track_list));
                        intent.putExtra(WebviewActivity.KEY_WEBVIEW_URL, getTrackListUrl(bean.data));
                        intent.putExtra(WebviewActivity.KEY_COLLECT, true);
                        context.startActivity(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (url.startsWith("protocol://track-details")) {
            try {
                int index = url.indexOf("data=");
                if (index > -1) {
                    int start = index + 5;
                    int end = url.length();
                    String data = url.substring(start, end);
                    TrackDetailBean bean = JSON.parseObject(data, TrackDetailBean.class);
                    if (bean != null && bean.data != null) {
                        Intent intent = new Intent(context, WebviewActivity.class);
                        intent.putExtra(WebviewActivity.KEY_WEBVIEW_TITLE, context.getString(R.string.track_detail));
                        intent.putExtra(WebviewActivity.KEY_WEBVIEW_URL, getTrackDetailUrl(bean.data.partid, bean.data.commuid, ""));
                        context.startActivity(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }


    public static String getMessageDetailUrl(MessageDataBean data) {
        String params = "xieyi=100&commuid=" + CarControlApplication.getInstance().getMyInfo().uid + "&imei=" + CarControlApplication.getInstance().serverImei + "&eventId=" + data.eventId + "&car_id" + data.car_id + "&language=" + GolukUtils.getLanguageAndCountryWeb();
        return UrlHostManager.getWebPageHost() + CarControlApplication.getInstance().getString(R.string.server_flag) + "/event-details.html?" + params;
    }


    public static String getMessageDetailUrl(String imei, String eventid) {
        String params = "xieyi=100&commuid=" + CarControlApplication.getInstance().getMyInfo().uid + "&imei=" + imei + "&eventId=" + eventid + "&language=" + GolukUtils.getLanguageAndCountryWeb();
        return UrlHostManager.getWebPageHost() + CarControlApplication.getInstance().getString(R.string.server_flag) + "/event-details.html?" + params;
    }


    public static String getMessageDetailUrl(FamilyEventResult.FamilyEventDetailBean data) {
        String params = "xieyi=100&commuid=" + data.user.uid + "&imei=" + data.event.imei + "&eventId=" + data.event.eventId + "&language=" + GolukUtils.getLanguageAndCountryWeb();
        return UrlHostManager.getWebPageHost() + CarControlApplication.getInstance().getString(R.string.server_flag) + "/event-details.html?" + params;
    }


    public static String getTrackListUrl(MyCarTrackDataBean data) {
        String params = "xieyi=100&commuid=" + data.commuid + "&imei=" + data.imei + "&language=" + GolukUtils.getLanguageAndCountryWeb();
        return UrlHostManager.getWebPageHost() + CarControlApplication.getInstance().getString(R.string.server_flag) + "/historical-track.html?" + params;
    }

    public static String getTrackDetailUrl(String partid, String uid, String imei) {
        String params = "xieyi=100&commuid=" + uid + "&partid=" + partid + "&imei=" + imei + "&language=" + GolukUtils.getLanguageAndCountryWeb();
        return UrlHostManager.getWebPageHost() + CarControlApplication.getInstance().getString(R.string.server_flag) + "/track-details.html?" + params;
    }

    public static String getMessageUrl(boolean collect) {
        String uid = "";
        if (CarControlApplication.getInstance().isUserLoginToServerSuccess()) {
            uid = CarControlApplication.getInstance().getMyInfo().uid;
        }
        String params = "xieyi=100&commuid=" + uid + "&imei=" + CarControlApplication.getInstance().serverImei + "&collect=" + String.valueOf(collect ? 1 : 0) + "&language=" + GolukUtils.getLanguageAndCountryWeb();
        return UrlHostManager.getWebPageHost() + CarControlApplication.getInstance().getString(R.string.server_flag) + "/message-list.html?" + params;
    }

    public static String getMyCarUrl() {
        String uid = "";
        if (CarControlApplication.getInstance().isUserLoginToServerSuccess()) {
            uid = CarControlApplication.getInstance().getMyInfo().uid;
        }
        String params = "xieyi=100&commuid=" + uid + "&imei=" + CarControlApplication.getInstance().serverImei + "&car_id=" + String.valueOf("") + "&language=" + GolukUtils.getLanguageAndCountryWeb();
        return UrlHostManager.getWebPageHost() + CarControlApplication.getInstance().getString(R.string.server_flag) + "/my-car.html?" + params;
    }


    public static String getProduct() {
        return UrlHostManager.getWebPageHost() + "/article/product/index.html?language=" + GolukUtils.getLanguageAndCountryWeb();
    }

    public static String getHelp() {
        return UrlHostManager.getWebPageHost() + "/article/userguide/index.html?language=" + GolukUtils.getLanguageAndCountryWeb();
    }


    public static String getUser() {
        return UrlHostManager.getWebPageHost() + "/article/agreement/index.html?language=" + GolukUtils.getLanguageAndCountryWeb();
    }


    public static String getPrivacy() {
        return UrlHostManager.getWebPageHost() + "/article/privacy/index.html?language=" + GolukUtils.getLanguageAndCountryWeb();
    }

    public static String getShareUrl(String url) {
        return url + "&xieyi=100&language=" + GolukUtils.getLanguageAndCountryWeb();
    }

}
