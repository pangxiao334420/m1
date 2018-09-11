package com.goluk.a6.cpstomp;

import java.io.Serializable;

/**
 * WebSocket 返回数据
 */
public class MessageResult implements Serializable {

    public int code;
    public String msg;
    public String data;

    public static class Data implements Serializable {
        public String clientId;
        public String cmd;
        public String session;
        public String data;
        public int code;
    }

}
