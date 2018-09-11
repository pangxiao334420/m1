package com.goluk.a6.http.request;

/**
 * Created by goluk_lium on 2017/11/9.
 */

public class GprsMsgBean<T> {

    public String msg;
    public int code;
    public T data;

    public static class Extra{
        public int xieyi;
    }
}
