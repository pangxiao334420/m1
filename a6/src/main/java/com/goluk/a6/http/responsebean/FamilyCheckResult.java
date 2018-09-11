package com.goluk.a6.http.responsebean;

import java.io.Serializable;
import java.util.List;

public class FamilyCheckResult {
    public int code;
    public String msg;
    public FamilyBean data;

    public static class FamilyBean {
        public List<FamilyUserBean> list;
        public int size;
    }

    public static class FamilyUserBean implements Serializable{
        public String uid;
        public String name;
        public String avatar;
        public String imei;
    }
}
