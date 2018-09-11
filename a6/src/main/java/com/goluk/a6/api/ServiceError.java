package com.goluk.a6.api;

/**
 * 服务器返回错误定义
 */
public class ServiceError extends RuntimeException {

    public int code;
    public String msg;

    public ServiceError(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
