package com.goluk.a6.http.request;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.responsebean.BindedByOtherResult;

/**
 * 查询被其他用户绑走的设备信息
 */
public class BindedByOtherRequest extends GolukFastjsonRequest<BindedByOtherResult> {

    public BindedByOtherRequest(int requestType, IRequestResultListener listener) {
        super(requestType, BindedByOtherResult.class, listener);
    }

    @Override
    protected String getPath() {
        return "/carbox/binding/notice";
    }

    @Override
    protected String getMethod() {
        return "";
    }

    public void request() {
        //xieyi
        //commuid
        get();
    }

}
