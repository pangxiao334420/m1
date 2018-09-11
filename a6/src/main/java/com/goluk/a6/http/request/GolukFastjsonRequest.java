package com.goluk.a6.http.request;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.goluk.a6.control.BuildConfig;
import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.http.HttpManager;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.bean.UserInfo;

import java.util.HashMap;
import java.util.Map;


public abstract class GolukFastjsonRequest<T> {
    private Class<T> mClazz;
    private Map<String, String> mHeaders = new HashMap<String, String>();
    private Map<String, String> mParams = new HashMap<String, String>();
    private IRequestResultListener mListener;
    private Object mTag;
    private boolean bCache = false;
    private int mRequestType;// requestType for call back
    private GolukRetryPolicy mDefaultRetryPolicy;

    public GolukFastjsonRequest(int requestType, Class<T> clazz, IRequestResultListener listener) {
        mClazz = clazz;
        mListener = listener;
        mRequestType = requestType;
        mDefaultRetryPolicy = new GolukRetryPolicy();
        addDefaultHeader();
        addDefaultParam();
    }

    protected abstract String getPath();

    protected abstract String getMethod();

    /**
     * 继承实现，添加不变的参数，可变的参数在addParam中添加
     */
    protected void addDefaultParam() {
        String Method = getMethod();
        if (!TextUtils.isEmpty(Method))
            mParams.put("method", getMethod());
    }

    /**
     * 继承实现，添加不变的Header,Header中要变化的参数在addHeader中添加
     */
    protected void addDefaultHeader() {
        mHeaders.put("commuid", getUserId());
        mHeaders.put("commmid", "" + GolukUtils.getMobileId());
        mHeaders.put("commostag", "android");
        mHeaders.put("xieyi", "100");
        mHeaders.put("commosversion", android.os.Build.VERSION.RELEASE);
        mHeaders.put("commversion", BuildConfig.BRANCH_CHINA ? String.valueOf(0) : String.valueOf(1));
        mHeaders.put("commlocale", GolukUtils.getLanguageAndCountryWeb());
    }

    private String getUserId() {
        UserInfo userInfo = CarControlApplication.getInstance().getMyInfo();
        if (userInfo != null)
            return userInfo.uid;

        return "";
    }

    protected Map<String, String> getHeader() {
        return mHeaders;
    }

    protected Map<String, String> getParam() {
        return mParams;
    }

    public void setTag(Object tag) {
        mTag = tag;
    }

    public void setCache(boolean b) {
        bCache = b;
    }

    protected void setCurrentTimeout(int timeout) {
        mDefaultRetryPolicy.setCurrentTimeout(timeout);
    }

    protected void setCurrentRetryCount(int retryCount) {
        mDefaultRetryPolicy.setCurrentRetryCount(retryCount);
    }

    protected void get() {
        addRequest(Request.Method.GET);
    }

    protected void put() {
        addRequest(Request.Method.PUT);
    }

    protected void post() {
        addRequest(Request.Method.POST);
    }

    protected void postS() {
        addSRequest(Request.Method.POST);
    }

    protected void delete() {
        addRequest(Request.Method.DELETE);
    }


    private void addRequest(int type) {
        String url = HttpManager.getInstance().getUrl(type, getPath(), mParams);
        Log.e("url", url );
        if (type == Request.Method.GET) {
            mParams = null;
        }

        FastjsonRequest<T> request = new FastjsonRequest<T>(type, url, mClazz, mHeaders, mParams, new Response.Listener<T>() {

            @Override
            public void onResponse(T response) {
                // TODO Auto-generated method stub
                if (mListener != null) {
                    mListener.onLoadComplete(mRequestType, response);
                }
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                if (mListener != null) {
                    mListener.onLoadComplete(mRequestType, null);
                }
            }

        });
        if (mTag == null) {
            mTag = mListener;
        }
        request.setTag(mTag);
        request.setShouldCache(bCache);
        request.setRetryPolicy(mDefaultRetryPolicy);
        HttpManager.getInstance().add(request);
    }


    private void addSRequest(int type) {
        String url = HttpManager.getInstance().getUrl(type, getPath(), mParams);
        if (type == Request.Method.GET) {
            mParams = null;
        }
        FastjsonRequestS<T> request = new FastjsonRequestS<T>(type, url, mClazz, null, mParams, new Response.Listener<T>() {

            @Override
            public void onResponse(T response) {
                // TODO Auto-generated method stub
                if (mListener != null) {
                    mListener.onLoadComplete(mRequestType, response);
                }
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                if (mListener != null) {
                    mListener.onLoadComplete(mRequestType, null);
                }
            }

        });

        if (mTag == null) {
            mTag = mListener;
        }
        request.setTag(mTag);
        request.setShouldCache(bCache);
        request.setRetryPolicy(mDefaultRetryPolicy);
        HttpManager.getInstance().add(request);
    }
}
