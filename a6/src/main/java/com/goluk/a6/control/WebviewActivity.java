package com.goluk.a6.control;

import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.goluk.a6.common.util.H5Util;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.UrlHostManager;
import com.goluk.a6.http.request.CollectGetRequest;
import com.goluk.a6.http.request.CollectRequest;
import com.goluk.a6.http.request.MessageDataBean;
import com.goluk.a6.http.responsebean.CollectResult;
import com.goluk.a6.http.responsebean.ServerBaseResult;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.UserUtils;

public class WebviewActivity extends BaseActivity implements DownloadListener, IRequestResultListener {
    public static final String KEY_WEBVIEW_TITLE = "webview_title";
    public static final String KEY_EXTRAL = "extral";
    public static final String KEY_WEBVIEW_URL = "webview_url";
    public static final String KEY_BUTTON = "webview_button";
    public static final String KEY_COLLECT = "webview_collect";
    private boolean mErrorState = false;
    private String mWebUrl;
    private String mWebTitle;
    private LinearLayout mBlankPageLL, ll_button;
    private MyProgressWebview mWebview;
    private ProgressBar mBlankPageProgressbar;
    private TextView mBlankPageTV;
    private TextView mTvCollect;
    private ImageView mIvCollect;
    public boolean showButton;
    public boolean showCollect;
    public String eventId;
    public boolean haveCollected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        initData();
        setupView();
    }

    private void initData() {
        mWebUrl = getIntent().getStringExtra(KEY_WEBVIEW_URL);
        mWebTitle = getIntent().getStringExtra(KEY_WEBVIEW_TITLE);
        eventId = getIntent().getStringExtra(KEY_EXTRAL);
        showButton = getIntent().getBooleanExtra(KEY_BUTTON, false);
        showCollect = getIntent().getBooleanExtra(KEY_COLLECT, false);
        setTitle(mWebTitle);
        showBack(true);
        mBlankPageLL = (LinearLayout) findViewById(R.id.ll_blank_page);
        ll_button = (LinearLayout) findViewById(R.id.ll_button);
        mIvCollect = (ImageView) findViewById(R.id.iv_collect);
        mWebview = (MyProgressWebview) findViewById(R.id.webview);
        mBlankPageProgressbar = (ProgressBar) findViewById(R.id.progressbar_blank_page);
        mBlankPageTV = (TextView) findViewById(R.id.tv_blank_page);
        mTvCollect = (TextView) findViewById(R.id.tv_action);
        if (showButton) {
            ll_button.setVisibility(View.VISIBLE);
            ll_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!UserUtils.isNetDeviceAvailable(WebviewActivity.this)) {
                        GolukUtils.showToast(WebviewActivity.this, WebviewActivity.this.getResources().getString(R.string.user_net_unavailable));
                    } else {
                        final CollectRequest request = new CollectRequest(11, WebviewActivity.this);
                        request.get(mApp.getMyInfo().uid, eventId, !haveCollected);
                    }
                }
            });
            CollectGetRequest request = new CollectGetRequest(0, this);
            request.get(mApp.getMyInfo().uid, eventId);
        }
        mBlankPageLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!GolukUtils.isNetworkConnected(WebviewActivity.this)) {
                    GolukUtils.showToast(WebviewActivity.this, getResources().getString(R.string.user_net_unavailable));
                    return;
                }
                mWebview.loadUrl(mWebUrl);
            }
        });
    }

    private void setupView() {
        String currUa = mWebview.getSettings().getUserAgentString();
        mWebview.getSettings().setUserAgentString(currUa + "/ Web /" + "golukAndroid /");
        mWebview.getSettings().setJavaScriptEnabled(true);
        WebSettings webSettings = mWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mWebview.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        mWebview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); // 设置
        // 缓存模式
        mWebview.setDownloadListener(this);
        mWebview.setWebViewClient(new WebViewClient() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                //super.onReceivedError(view, request, error);
                view.stopLoading();
                if (request.getUrl().toString().contains("maps.googleapis"))
                    return;
                view.loadUrl("about:blank");
                mBlankPageLL.setVisibility(View.VISIBLE);
                mBlankPageProgressbar.setVisibility(View.GONE);
                mBlankPageTV.setVisibility(View.VISIBLE);
                mErrorState = true;
            }

            @Deprecated
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.loadUrl("about:blank");
                mBlankPageLL.setVisibility(View.VISIBLE);
                mErrorState = true;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                mErrorState = false;
                mBlankPageLL.setVisibility(View.GONE);
                super.onLoadResource(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (showCollect) {
                    return H5Util.h5ShouldOverrideUrlLoading(WebviewActivity.this, url);
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                handler.proceed();
            }
        });
        mWebview.loadUrl(mWebUrl);

        mWebview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWebview.clearView();
                mWebview.setVisibility(View.VISIBLE);
                mWebview.reload();
            }
        });
    }

    private String getUrl(MessageDataBean data) {
        String params = "?xieyi=100&commuid=" + CarControlApplication.getInstance().getMyInfo().uid + "&imei=" + CarControlApplication.getInstance().serverImei + "&eventId=" + data.eventId + "&car_id" + data.car_id + "&locale=" + GolukUtils.getLanguageAndCountry();
        return UrlHostManager.getBaseUrl() + "/m1/event-details.html?" + params;
    }

    @Override
    public void onDownloadStart(String s, String s1, String s2, String s3, long l) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mErrorState) {
                    finish();
                    mErrorState = false;
                    return true;
                }
                if (mWebview.canGoBack()) {
                    mWebview.goBack();
                } else {
                    finish();
                }
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == 11) {
            ServerBaseResult result1 = (ServerBaseResult) result;
            if (result1 != null && result1.code == 0) {
                if (!haveCollected) {
                    showToast(getString(R.string.collect_success));
                } else {
                    showToast(getString(R.string.cancel_collect_success));
                }
                haveCollected = !haveCollected;
            } else {
                showToast(getString(R.string.collect_error));
            }
        } else if (requestType == 0) {
            CollectResult result1 = (CollectResult) result;
            if (result1 != null && result1.code == 0 && result1.data != null) {
                haveCollected = !TextUtils.isEmpty(result1.data.collectionId);
            }
        }
        updateView();
    }

    public void updateView() {
        if (haveCollected) {
            mTvCollect.setText(R.string.save_event_cancel);
            mIvCollect.setImageResource(R.drawable.collected);
        } else {
            mTvCollect.setText(R.string.save_event);
            mIvCollect.setImageResource(R.drawable.collection);
        }
    }
}
