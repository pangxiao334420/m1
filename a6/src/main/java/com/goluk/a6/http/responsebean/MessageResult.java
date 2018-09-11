package com.goluk.a6.http.responsebean;

public class MessageResult {
    public int code;
    public String msg;
    public MessageBean data;

    public static class MessageBean {
        public int emergencyAlert;
        public int exceptionAlert;
        public int snapAlert;
    }
}
