package com.goluk.a6.api;

import org.json.JSONException;
import org.json.JSONObject;

import likly.dollar.$;
import likly.reverse.JsonParseException;
import likly.reverse.OnCallExecuteListener;
import likly.reverse.Response;

/**
 * @author Created by likly on 2017/3/24.
 * @version 1.0
 */

public class ApiCallExecuteListener implements OnCallExecuteListener {
    @Override
    public void onStart() {
    }

    @Override
    public void onResponse(Response response) {
    }

    @Override
    public void onParseResponseStart() {
    }

    @Override
    public String onParseJson(String s) throws JsonParseException {
        try {
            return parseJsonData(s);
        } catch (JSONException e) {
            $.debug().tag("ERROR:").e(e.toString());
            throw new JsonParseException(e);
        }
    }

    @Override
    public void onResponseResult(Object o) {
    }

    @Override
    public void onParseResponseFinish() {
    }

    @Override
    public void onFinish() {
    }

    @Override
    public void onCancel() {
    }

    /**
     * 解析返回数据
     */
    private String parseJsonData(String json) throws JSONException {
        $.debug().e("Response: " + json);
        JSONObject jsonObject = new JSONObject(json);
        final int code = jsonObject.optInt("code");
        if (code != 0) {
            final String errorMsg = jsonObject.optString("msg");
            throw new ServiceError(code, errorMsg);
        }
        final String data = jsonObject.optString("data");

        return data;
    }

}
