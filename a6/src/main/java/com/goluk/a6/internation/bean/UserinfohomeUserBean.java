package com.goluk.a6.internation.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class UserinfohomeUserBean {

    /**
     * 用户唯一id
     **/
    @JSONField(name = "uid")
    public String uid;

    /**
     * 性别
     **/
    public int sex;

    /**
     * 用户昵称
     **/
    @JSONField(name = "name")
    public String name;

    @JSONField(name = "avatar")
    public String avatar;

    /**
     * 个性签名
     **/
    @JSONField(name = "description")
    public String description;

    public String emgContactName;
    public String emgContactCode;
    public String emgContactPhone;

    public Account account;

    public static class Account {
        public String phone;
        public String email;
    }

}
