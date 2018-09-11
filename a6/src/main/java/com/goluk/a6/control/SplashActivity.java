package com.goluk.a6.control;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.goluk.a6.control.R;
import com.goluk.a6.internation.SharedPrefUtil;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class SplashActivity extends BaseActivity {
    private static final String TAG = "CarSvc_SplashActivity";
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XGPushClickedResult click = XGPushManager.onActivityStarted(this);
        if (click != null) {
            //从推送通知栏打开-Service打开Activity会重新执行Laucher流程
            if (isTaskRoot()) {
                return;
            }
            finish();
            return;
        }
        setContentView(R.layout.activity_splash);
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (!SharedPrefUtil.getWelcome()) {
                    SplashActivity.this.startActivity(new Intent(SplashActivity.this, IntroActivity.class));
                    SplashActivity.this.finish();
                } else {
                    SplashActivity.this.startActivity(new Intent(SplashActivity.this, CarControlActivity.class));
                    SplashActivity.this.finish();
                }
            }
        }, 1500);
        if (mApp.getMyInfo() != null) {
            mApp.mUser.initAutoLogin();
        }
        CarControlApplication.getInstance().getImei();
        CarControlApplication.getInstance().getShareValidity();
    }

    @Override
    public void onBackPressed() {

    }
}
