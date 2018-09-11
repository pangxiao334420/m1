package com.goluk.a6.http.responsebean;

import java.util.List;

public class FamilyShareMeResult {
    public int code;
    public String msg;
    public FamilyMeBean data;

    public static class FamilyMeBean {
        public String url;
    }
}
