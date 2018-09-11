package com.goluk.a6.cpstomp;

/**
 * Created by leege100 on 2017/1/5.
 */

public class CPStompResultBean<T> {
    private int code;
    private T data;
    private String msg;
    private CPStompExtraBean extra;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public CPStompExtraBean getExtra() {
        return extra;
    }

    public void setExtra(CPStompExtraBean extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("code=" + code + " msg=" + msg);
//        if(null != extra) {
//            sb.append("extra= " + extra.toString());
//        }
        if (null != data) {
            sb.append(" data:" + data.toString());
        }
        return sb.toString();
    }
}
