package com.goluk.a6.http.responsebean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 用户当前绑定列表
 */
public class BindList {

    @SerializedName("default")
    public String defaultId;
    public int size;
    public String defaultImei;
    public int notice;
    public List<BindAddResult.BindBean> list;

}
