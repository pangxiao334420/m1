package com.goluk.a6.control;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.a6.control.util.HttpRequestManager;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.AppUpgradeRequest;
import com.goluk.a6.http.responsebean.AppUpgradeResult;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.SharedPrefUtil;
import com.goluk.a6.internation.UserUtils;

import java.util.Calendar;
import java.util.Date;

public class AppVersionActivity extends BaseActivity implements IRequestResultListener {
    private TextView mAppVersion;
    private TextView mAppVersionDesc;

    private ProgressDialog progressDialog;
    AppUpgradeRequest request;
    String version = "";
    AppUpgradeResult app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_version);
        initView();
    }

    protected void initView() {
        setTitle(R.string.version_info);
        showBack(true);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.check_upgrade));
        progressDialog.setTitle(getString(R.string.hint));

        mAppVersion = (TextView) findViewById(R.id.tv_app_version);
        mAppVersionDesc = (TextView) findViewById(R.id.tv_app_version_desc);

        version = GolukUtils.getAppVersion(mApp);
        mAppVersion.setText(getString(R.string.app_version, version));
        request = new AppUpgradeRequest(0, this);
        requestUpgradeCheck();
    }

    private void requestUpgradeCheck() {
        String uid = "";
        if (mApp.isUserLoginToServerSuccess()) {
            uid = mApp.getMyInfo().uid;
        }
        if (!UserUtils.isNetDeviceAvailable(this)) {
            UserUtils.hideSoftMethod(this);
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
        } else {
            request.get(uid, version);
            progressDialog.show();
        }
    }

    private void startdownload() {
        if (BuildConfig.BRANCH_CHINA) {
            try {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(app.data.app.fileurl);
                intent.setData(content_url);
                AppVersionActivity.this.startActivity(intent);
            } catch (ActivityNotFoundException anfe) {
                Toast.makeText(AppVersionActivity.this,
                        R.string.cannot_open_broswer,
                        Toast.LENGTH_SHORT).show();
                anfe.printStackTrace();
            }
        } else {
            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        progressDialog.dismiss();
        app = (AppUpgradeResult) result;
        showInfo();
    }

    private void showInfo() {
        if (app != null && app.code == 0 && app.data != null && app.data.app != null) {
            final long today = Calendar.getInstance().getTime().getTime() / (24 * 60 * 60 * 1000);
            AlertDialog dialog = new AlertDialog.Builder(AppVersionActivity.this)
                    .setTitle(R.string.new_app)
                    .setMessage(app.data.app.description)
                    .setPositiveButton(R.string.upgrade_app, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            mAppVersionDesc.setText("");
                            startdownload();
                        }
                    })
                    .setCancelable(false)
                    .setNegativeButton(R.string.later_upgrade, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            SharedPrefUtil.saveDay(today);
                            mAppVersionDesc.setText("");
                        }
                    }).create();
            if (!mActivityDestroyed) {
                dialog.show();
            }
        }
    }
}
