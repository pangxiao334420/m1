package com.goluk.a6.http.request;

import com.android.volley.Response;

/**
 * Created by goluk_lium on 2017/11/8.
 */

public class GprsPlanRequest extends GsonRequest{

    public GprsPlanRequest(int method, String url, Class clazz, Response.Listener listener, Response.ErrorListener errorListener) {
        super(method, url, clazz, listener, errorListener);
    }

    public GprsPlanRequest(String url, Class clazz, Response.Listener listener, Response.ErrorListener errorListener) {
        super(url, clazz, listener, errorListener);
    }

}
