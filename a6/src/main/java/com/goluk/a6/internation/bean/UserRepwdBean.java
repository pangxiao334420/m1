package com.goluk.a6.internation.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by hanzheng on 2016/6/29.
 */
public class UserRepwdBean {

    /** 请求返回码  */
    @JSONField(name="code")
    public String code;

    /** 请求是否成功 true: 成功; false: 失败 */
    @JSONField(name="state")
    public String state;

    /** 请求类型  */
    @JSONField(name="type   ")
    public String type;

    public String msg;
}
