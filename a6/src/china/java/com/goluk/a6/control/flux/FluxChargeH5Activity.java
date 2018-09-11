package com.goluk.a6.control.flux;

import android.app.ProgressDialog;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.a6.common.WXConstants;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.Config;
import com.goluk.a6.control.MyProgressWebview;
import com.goluk.a6.control.R;
import com.goluk.a6.http.request.FluxCurrentStatusRequest;
import com.goluk.a6.http.request.WXPayEntity;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.UserUtils;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by goluk_lium on 2017/11/6.
 */

public class FluxChargeH5Activity extends BaseActivity implements FluxCurrentStatusRequest.FluxRequestListener,View.OnClickListener {

    private IWXAPI wxApi;
    private WXPayEntity wxPayEntity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flux_charge);
        onCreateTmp(savedInstanceState);
    }
    private void initWX(){
        if (wxApi==null){
            wxApi = WXAPIFactory.createWXAPI(this, WXConstants.APP_ID);
            wxApi.registerApp(WXConstants.APP_ID);
        }
    }
    private void onWXPay(WXPayEntity payEntity){
        PayReq req = new PayReq();
        req.appId = payEntity.appId;
        req.partnerId = payEntity.partnerId;
        req.prepayId = payEntity.prepayId;
        req.nonceStr = payEntity.nonceStr;
        req.timeStamp = payEntity.timeStamp;
        req.packageValue = payEntity.packageValue;
        req.sign = payEntity.sign;
        wxApi.sendReq(req);
    }

    /**
     * wxpay start
     */
    private WXPayEntity payEntity = null;
    private ProgressDialog progressDialog;
    private void initProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在生成订单...");
    }
    private void showProgress(){
        if (progressDialog!=null&&!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgress(){
        if (progressDialog!=null&&progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private boolean isWXAppInstalled(){
        return wxApi.isWXAppInstalled();
    }

    private boolean isWXAppSupportPay(){
        return wxApi.isWXAppSupportAPI();
    }

    private void generateOrderByServer(String params) {
        try{
            JSONObject paramsJson = new JSONObject(params);
            JSONObject data = paramsJson.getJSONObject("data");
            String iccid = data.getString("iccid");
            String pid = data.getString("pid");
            FluxCurrentStatusRequest request = new FluxCurrentStatusRequest(this);
            request.requestPreOrder(iccid,pid);
            showProgress();
        } catch (JSONException e){
        }
    }

    /**
     * start
     */

    private String url;
    private int titleRes = R.string.flux_manager;
    private void onCreateTmp(Bundle savedInstanceState){
        if (getIntent()!=null) {
            url = getIntent().getStringExtra(FluxManagerActivity.WEB_PAGE_URL);
            titleRes = getIntent().getIntExtra(FluxManagerActivity.WEB_PAGE_TITLE,R.string.flux_manager);
        }
        initWX();
        initWebView();
        initProgressDialog();
        mWebView.loadUrl(url);
    }
    private MyProgressWebview mWebView;
    private TextView textWebTitle;
    private ImageButton iBtnBack;

    private static final String CHARGE_SCHEME = "protocol://recharge?data=";
    private void initWebView(){
        mWebView = (MyProgressWebview) findViewById(R.id.chargewebview);
        textWebTitle = (TextView) findViewById(R.id.web_title_text);
        iBtnBack = (ImageButton) findViewById(R.id.back_ibtn);
        textWebTitle.setText(titleRes);
        iBtnBack.setOnClickListener(this);
        WebSettings mWebViewSettings = mWebView.getSettings();
        String currentUA = mWebViewSettings.getUserAgentString();
        mWebViewSettings.setUserAgentString(currentUA+"/Web/golukAndroid");
        mWebViewSettings.setJavaScriptEnabled(true);
        mWebViewSettings.setLoadWithOverviewMode(true);
        mWebViewSettings.setUseWideViewPort(true);
        mWebViewSettings.setSupportZoom(true);
        mWebViewSettings.setBuiltInZoomControls(false);
        mWebViewSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebViewSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebViewSettings.setDomStorageEnabled(true);
        if (android.os.Build.VERSION.SDK_INT>= android.os.Build.VERSION_CODES.JELLY_BEAN)
            mWebViewSettings.setAllowFileAccessFromFileURLs(true);
        mWebView.setScrollbarFadingEnabled(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        mWebView.setWebViewClient(new ChargeHandlerWebViewClient());
        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()==KeyEvent.ACTION_DOWN){
                    if (keyCode==KeyEvent.KEYCODE_BACK&&mWebView.canGoBack()){
                        mWebView.goBack();
                        return true;
                    }
                }
                return false;
            }
        });
    }


    @Override
    public void onGprsPlanInfo(com.goluk.a6.http.request.GprsPlanBean gprsPlanBean) {

    }

    @Override
    public void onGprsMonthInfo(List<com.goluk.a6.http.request.GprsMonthInfoBean> gprsMonthInfoBeanList) {

    }

    @Override
    public void onSimCode(String code) {}
    @Override
    public void onFailure(int errCode) {
        hideProgress();
        switch (errCode){
            case Config.SERVER_TOKEN_DEVICE_INVALID:
                GolukUtils.showToast(this,this.getResources().getString(R.string.server_token_device_invalid));
                break;
            case Config.SERVER_TOKEN_EXPIRED:
            case Config.SERVER_TOKEN_INVALID:
                GolukUtils.showToast(this,this.getResources().getString(R.string.server_token_expired));
                break;
            case Config.CODE_VOLLEY_NETWORK_ERROR:
                GolukUtils.showToast(this,this.getResources().getString(R.string.network_invalid));
                break;
            default:
                GolukUtils.showToast(this,"未知错误，请稍后再试");
                break;
        }

    }

    @Override
    public void onGenerateOrder(com.goluk.a6.http.request.WXPayEntity entity) {
        hideProgress();
        onWXPay(entity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_ibtn:
                if (mWebView.canGoBack()) {mWebView.goBack();return;}
                onBackPressed();
                break;
            default:
                break;
        }
    }

    private class ChargeHandlerWebViewClient extends WebViewClient{
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(CHARGE_SCHEME)){
                String data = url.substring(CHARGE_SCHEME.length());
                if (!GolukUtils.isNetworkConnected(FluxChargeH5Activity.this)) {
                    GolukUtils.showToast(FluxChargeH5Activity.this, getResources().getString(R.string.user_net_unavailable));
                    return true;
                }
                if (!isWXAppInstalled()){
                    GolukUtils.showToast(FluxChargeH5Activity.this,"请检查微信是否已安装");
                    return true;
                }
                if (!isWXAppSupportPay()){
                    GolukUtils.showToast(FluxChargeH5Activity.this,"该微信版本不支持微信支付");
                    return true;
                }
                generateOrderByServer(data);
                return true;
            }else{

            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
        }
    }
    /**
     *end
     */


}
