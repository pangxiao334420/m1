package com.goluk.a6.http.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by goluk_lium on 2017/11/10.
 */

public class SimUserInfo {

    public String iccid;
    public String code;
    public String status;
    @SerializedName("is_identity")
    public String isIdentity;

}
