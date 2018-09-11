package com.goluk.a6.http.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by goluk_lium on 2017/11/6.
 */

public class WXPayEntity {
    @SerializedName("appid")
    public String appId;
    @SerializedName("partnerid")
    public String partnerId;
    @SerializedName("prepayid")
    public String prepayId;
    @SerializedName("noncestr")
    public String nonceStr;
    @SerializedName("timestamp")
    public String timeStamp;
    @SerializedName("package")
    public String packageValue;
    public String sign;

    @Override
    public String toString() {
        return "WXPayEntity{" +
                "app_id='" + appId + '\'' +
                ", partner_id='" + partnerId + '\'' +
                ", prepay_id='" + prepayId + '\'' +
                ", nonceStr='" + nonceStr + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", packageValue='" + packageValue + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
