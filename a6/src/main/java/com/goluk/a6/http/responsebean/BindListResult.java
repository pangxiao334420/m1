package com.goluk.a6.http.responsebean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class BindListResult {
    public int code;
    public String msg;
    public BindListBean data;

    public static class BindListBean {
        public List<BindAddResult.BindBean> list;
        @JSONField(name = "default")
        public String defaultId;
        // 0=无，1=设备被绑定走提示，其他值未定义
        public int notice;
    }
}
