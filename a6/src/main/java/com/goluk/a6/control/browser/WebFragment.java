package com.goluk.a6.control.browser;

import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goluk.a6.control.CarControlActivity;
import com.goluk.a6.control.MyProgressWebview;
import com.goluk.a6.control.R;
import com.goluk.a6.internation.GolukUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link WebFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WebFragment extends Fragment implements DownloadListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mUrl;
    private String mTitle;

    private boolean mErrorState = false;
    private LinearLayout mBlankPageLL;
    private MyProgressWebview mWebview;
    private TextView mBlankPageTV;
    private boolean canFinish = false;
    private IWebReload webReloadListener;

    public MyProgressWebview getWebView() {
        return mWebview;
    }

    public WebFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WebFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WebFragment newInstance(String param1, String param2) {
        WebFragment fragment = new WebFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUrl = getArguments().getString(ARG_PARAM1);
            mTitle = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web, container, false);
        initData(view);
        return view;
    }

    private void initData(View view) {
        mBlankPageLL = (LinearLayout) view.findViewById(R.id.ll_blank_page);
        mWebview = (MyProgressWebview) view.findViewById(R.id.webview);
        mBlankPageTV = (TextView) view.findViewById(R.id.tv_blank_page);
        FragmentActivity activity = getActivity();
        if (activity instanceof CarControlActivity) {
            canFinish = false;
        } else {
            canFinish = true;
        }
        setupView(mUrl);
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mErrorState) {
                        if (canFinish) {
                            getActivity().finish();
                        }
                        mErrorState = false;
                        return true;
                    }
                    if (mWebview.canGoBack()) {
                        mWebview.goBack();
                    } else {
                        if (canFinish) {
                            getActivity().finish();
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        mBlankPageLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!GolukUtils.isNetworkConnected(getContext())) {
                    GolukUtils.showToast(getContext(), getResources().getString(R.string.user_net_unavailable));
                    return;
                }
                mWebview.clearCache(true);
                mWebview.reload();
                mWebview.loadUrl(mUrl);
                Log.e("WebFragment", "onClick:-------" + mUrl);
            }
        });
    }

    public View getNoNetView() {
        return mBlankPageLL;
    }

    public void setupView(String url) {
        this.mUrl = url;
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebview.setWebViewClient(new MyWebClientWebView());
        mWebview.setDownloadListener(this);
        mWebview.loadUrl(url);

//        WebSettings settings = mWebview.getSettings();
//        settings.setJavaScriptEnabled(true);
//        settings.setLoadWithOverviewMode(true);
//        settings.setUseWideViewPort(true);
//        settings.setSupportZoom(true);
//        settings.setBuiltInZoomControls(false);
//        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
//        settings.setDomStorageEnabled(true);
//        mWebview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
//        mWebview.setScrollbarFadingEnabled(true);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            mWebview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        } else {
//            mWebview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        }
        mWebview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWebview.setVisibility(View.VISIBLE);
                mWebview.reload();
            }
        });
    }

    public void JSCall(String url) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            mWebview.evaluateJavascript(url + ";", null);
        } else {
            mWebview.loadUrl("javascript:" + url + ";");
        }
    }

    @Override
    public void onDownloadStart(String s, String s1, String s2, String s3, long l) {

    }

    public IWebReload getWebReloadListener() {
        return webReloadListener;
    }

    public void setWebReloadListener(IWebReload webReloadListener) {
        this.webReloadListener = webReloadListener;
    }

    public interface IWebReload {
        boolean shouldOverrideUrlLoading(WebView view, String request);
    }

    public void loadUrl(String url) {
        if (mWebview != null) {
            mWebview.loadUrl(url);
        }
        mUrl = url;
        Log.e("WebFragment", "loadUrl: " + mUrl);
    }

    public class MyWebClientWebView extends WebViewClient {
        private boolean mError;

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            view.loadUrl("about:blank");
            mBlankPageLL.setVisibility(View.VISIBLE);
            mError = true;
            mErrorState = true;
            if (mReceivedErrorHandler != null)
                mReceivedErrorHandler.handleReceivedError();
        }

        @Deprecated
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            view.loadUrl("about:blank");
            mBlankPageLL.setVisibility(View.VISIBLE);
            mError = true;
            mErrorState = true;
            if (mReceivedErrorHandler != null)
                mReceivedErrorHandler.handleReceivedError();
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            // Notice Here.
            mError = false;
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
            if (webReloadListener != null) {
                return webReloadListener.shouldOverrideUrlLoading(view, url);
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            //closeLoading();
            if (!mError) {
                mBlankPageLL.setVisibility(View.GONE);
            }
            if (mReceivedErrorHandler != null) {
                mReceivedErrorHandler.onWebLoadComplete();
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError
                error) {
            super.onReceivedSslError(view, handler, error);
            handler.proceed();
        }
    }

    public interface IReceivedError {

        void handleReceivedError();

        void onWebLoadComplete();

    }

    private IReceivedError mReceivedErrorHandler;

    public void setReceivedErrorHandler(IReceivedError handler) {
        this.mReceivedErrorHandler = handler;
    }
}
