package com.goluk.a6.internation.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class UserinfohomeRetBean {
    /**请求返回码**/
    @JSONField(name="code")
    public int code;

    /**请求是否成功**/
    @JSONField(name="state")
    public String state;

    /**请求返回数据 **/
    @JSONField(name="data")
    public UserinfohomeUserBean data;

    /**返回调试信息**/
    @JSONField(name="msg")
    public String msg;


}
