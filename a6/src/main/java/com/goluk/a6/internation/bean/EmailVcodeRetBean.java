package com.goluk.a6.internation.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by leege100 on 2017/1/17.
 */

public class EmailVcodeRetBean {
    @JSONField(name="code")
    public int code;
    @JSONField(name="data")
    public EmailVcodeDataBean data;
    @JSONField(name="msg")
    public String msg;
    // V1: 100; V2: 200
    @JSONField(name="xieyi")
    public int xieyi;
}
