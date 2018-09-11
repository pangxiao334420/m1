package com.goluk.a6.http.responsebean;

public class FamilyShareMeInfoResult {
    public int code;
    public String msg;
    public FamilyMeInfoBean data;

    public static class FamilyMeInfoBean {
        public String uid;
        public String name;
        public String avatar;
        public FamilyMeInfoAccount account;
    }

    public static class FamilyMeInfoAccount {
        public String phone;
        public String email;
    }
}
