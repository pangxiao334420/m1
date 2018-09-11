package com.goluk.a6.http.request;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.control.Config;
import com.goluk.a6.http.HttpManager;
import com.goluk.a6.http.UrlHostManager;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.bean.UserInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;


import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by goluk_lium on 2017/11/2.
 */

public class FluxCurrentStatusRequest{

    private final static String SIM_CODE_PATH = "/flow/getcardinfo";
    private final static String GPRS_PLAN_PATH = "/flow/getusedpackinfo";
    private final static String GPRS_MONTH_PATH = "/flow/getmonthusedinfo";
    private final static String PRE_ORDER_PATH = "/flow/buildorder";
    private final static int REQUEST_SUCCESS = 0;
    private final static int VOLLEY_ERROR_CODE = -10001;
    private final static int UNKNOW_ERROR_CODE = -10002;
    public final static String IS_NOT_FEIMAO_CARD = "is_not_fei_mao_card";
    private static final String TAG = "FluxCurrentStatusReques";
    private Map<String, String> mParams = new HashMap<>();
    private GolukRetryPolicy mDefaultRetryPolicy;
    private FluxRequestListener mFluxRequestListener;

    private HttpManager httpManager;
    private String iccid;
    private String code;
    public FluxCurrentStatusRequest(FluxRequestListener listener) {
        this.mFluxRequestListener = listener;
        this.httpManager = HttpManager.getInstance();
        this.mDefaultRetryPolicy = new GolukRetryPolicy();
        setDefaultParams(mParams);
    }

    public void setDefaultParams(Map<String,String> params){
        UserInfo userInfo = CarControlApplication.getInstance().getMyInfo();
        if (userInfo==null) return;
        String timeStamp = System.currentTimeMillis()+"";
        String token = userInfo.token;
        String data = String.format("u=%s&m=%s&t=%s",
                userInfo.uid,
                GolukUtils.getMobileId(),
                timeStamp);
        String commticket = null;
        try {
            commticket = GolukUtils.encodeHmacSHA256(token,data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (commticket==null) return;
        params.put("xieyi","100");
        params.put("commuid", userInfo.uid);
        params.put("commmid", GolukUtils.getMobileId());
        params.put("commtimestamp",timeStamp);
        params.put("commticket",commticket);
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void requestSimCode(String iccid){
        mParams.put("iccid",iccid);
        String url = buildUrl(Request.Method.GET,SIM_CODE_PATH,mParams);
        StringRequest simCodeRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "requestSimCode-onResponse: "+response );
                        Type simInfoType = new TypeToken<GprsMsgBean<SimUserInfo>>(){}.getType();
                        Gson gson = new Gson();
                        GprsMsgBean<SimUserInfo> msgBean = gson.fromJson(response,simInfoType);
                        if (msgBean.code==REQUEST_SUCCESS){
                            if (msgBean.data.iccid!=null
                                    &&!msgBean.data.iccid.equals("")
                                    &&msgBean.data.code!=null
                                    &&!msgBean.data.code.equals("")){
                                mFluxRequestListener.onSimCode(msgBean.data.code);
                            }else {
                                mFluxRequestListener.onSimCode(IS_NOT_FEIMAO_CARD);
                            }
                          }else {
                            handleResponseError(msgBean.code);
                        }
                    }
                }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            handleVolleyFailureError(error);}});
        httpManager.add(simCodeRequest);
    }
    public void requestGprsPlan(String iccid,String code){
        mParams.put("iccid",iccid);
        mParams.put("code",code);
        String url = buildUrl(Request.Method.GET,GPRS_PLAN_PATH,mParams);
        StringRequest gprsPlan  = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "onResponse: "+response);
                Type gprsPlanType = new TypeToken<GprsMsgBean<GprsPlanBean>>(){}.getType();
                Gson gson = new Gson();
                GprsMsgBean<GprsPlanBean> msgBean = gson.fromJson(response,gprsPlanType);
                if (msgBean.code==REQUEST_SUCCESS){
                    GprsPlanBean data = msgBean.data;
                    if (data!=null)mFluxRequestListener.onGprsPlanInfo(data);
                }else {
                    handleResponseError(msgBean.code);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleVolleyFailureError(error);
            }
        });
        httpManager.add(gprsPlan);
    }

    public void requestMonthGprsInfo(String iccid,String code){
        mParams.put("iccid",iccid);
        mParams.put("code",code);
        String url = buildUrl(Request.Method.GET,GPRS_MONTH_PATH,mParams);
        StringRequest monthGprsInfoRequest  = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "onResponse: "+response);
                int responseCode = jsonResponseCode(response);
                if (jsonResponseCode(response)==REQUEST_SUCCESS){
                    Type everydaysFlow = new TypeToken<GprsMsgBean<List<GprsMonthInfoBean>>>(){}.getType();
                    Gson gson = new Gson();
                    GprsMsgBean<List<GprsMonthInfoBean>> msgBean = gson.fromJson(response,everydaysFlow);
                    List<GprsMonthInfoBean> data = msgBean.data;
                    if (data!=null) mFluxRequestListener.onGprsMonthInfo(data);
                }else {
                    handleResponseError(responseCode);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleVolleyFailureError(error);
            }
        });
        monthGprsInfoRequest.setRetryPolicy(new DefaultRetryPolicy(10000,5,1));
        monthGprsInfoRequest.setShouldCache(false);
        httpManager.add(monthGprsInfoRequest);
    }

    private int jsonResponseCode(String response){
         JsonObject jsonObj = new JsonParser().parse(response).getAsJsonObject();
         return jsonObj.get("code").getAsInt();
    }


    private String buildUrl(int type, String path, Map<String,String> params){
        return httpManager.getUrl(type,path,params);
    }


    public void requestPreOrder(final String iccid, String pid){
        mParams.put("iccid",iccid);
        mParams.put("pid",pid);
        String url = buildUrl(Request.Method.GET,PRE_ORDER_PATH,mParams);


        StringRequest orderRequest  = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "onResponse: "+response);
                Type wxPayType= new TypeToken<GprsMsgBean<WXPayEntity>>(){}.getType();
                Gson gson = new Gson();
                GprsMsgBean<WXPayEntity> msgBean = gson.fromJson(response,wxPayType);
                if (msgBean.code==REQUEST_SUCCESS){
                    WXPayEntity data = msgBean.data;
                    if (data!=null) mFluxRequestListener.onGenerateOrder(msgBean.data);
                }else {
                    handleResponseError(msgBean.code);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleVolleyFailureError(error);
            }
        });
        httpManager.add(orderRequest);
    }

    private void handleResponseError(int code){
        mFluxRequestListener.onFailure(code);
    }
    private void handleVolleyFailureError(VolleyError error){
        String errMsg = error.getLocalizedMessage();
        if (errMsg==null){
            mFluxRequestListener.onFailure(Config.CODE_UNKNOW_ERROR);
            return;
        }
        if (errMsg.startsWith("java.net.ConnectException")
                ||errMsg.startsWith("java.net.UnknownHostException")){
            mFluxRequestListener.onFailure(Config.CODE_VOLLEY_NETWORK_ERROR);
        }else {
            mFluxRequestListener.onFailure(Config.CODE_VOLLEY_OTHER_ERROR);
        }
    }

    public String buildWebUrl(String iccid,String code,String path){
        mParams.put("iccid",iccid);
        mParams.put("code",code);
        String url = httpManager.getWebH5Host();
        if (!TextUtils.isEmpty(path)) {
            url += path;
        }

        if (mParams != null && (mParams.size() != 0)) {
            String encodedParams = null;
            try {
                encodedParams = UrlHostManager.getEncodedUrlParams(mParams);
            } catch (AuthFailureError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String extra = "";
            if (encodedParams != null && encodedParams.length() > 0) {
                if (!url.endsWith("?")) {
                    extra += "?";
                }
                extra += encodedParams;
            }
            url = url + extra;
        }
        return url;
    }


    public interface FluxRequestListener{
        void onGprsPlanInfo(GprsPlanBean gprsPlanBean);
        void onGprsMonthInfo(List<GprsMonthInfoBean> gprsMonthInfoBeanList);
        void onSimCode(String code);
        void onFailure(int errCode);
        void onGenerateOrder(WXPayEntity entity);
    }

}
