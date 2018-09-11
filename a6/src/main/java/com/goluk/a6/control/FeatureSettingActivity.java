package com.goluk.a6.control;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.DeviceSleepSettingGetRequest;
import com.goluk.a6.http.request.DeviceSleepSettingPostRequest;
import com.goluk.a6.http.request.ShareValiditySettingGetRequest;
import com.goluk.a6.http.request.ShareValiditySettingPostRequest;
import com.goluk.a6.http.responsebean.DeviceSleepSettingResult;
import com.goluk.a6.http.responsebean.ServerBaseResult;
import com.goluk.a6.http.responsebean.ShareValiditySettingResult;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.UserUtils;

/**
 * Created by goluk_lium on 2018/4/10.
 */

public class FeatureSettingActivity extends BaseActivity implements IRequestResultListener, View.OnClickListener, Handler.Callback {

    private static final int CODE_REQUEST_DEVICE_SLEEP_SETTING = 10001;
    private static final int CODE_REQUEST_SHARE_VALIDITY_TIME = 10002;
    private static final int CODE_POST_SETTING_SLEEP_TIME = 20001;
    private static final int CODE_POST_SETTING_SHARE_TIME = 20002;

    private static final String KEY_SET_SLEEP = "dormancyTime";
    private static final String KEY_SET_SHARE = "sharedLinkTime";

    private TextView mTvSleepTime;
    private TextView mTvShareValidity;
    private Handler mHandler;
    private DeviceSleepSettingGetRequest mDeviceSleepSettingGetRequest;
    private ShareValiditySettingGetRequest mShareValiditySettingGetRequest;
    private DeviceSleepSettingPostRequest mDeviceSleepSettingPostRequest;
    private ShareValiditySettingPostRequest mShareValiditySettingPostRequest;
    private String currentSleepValue, currentShareValue;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_layout);
        setTitle(R.string.str_setting);
        showBack(true);
        mHandler = new Handler(Looper.getMainLooper(), this);
        initView();
        requestSettingData();
    }

    private void initView() {
        mTvSleepTime = (TextView) findViewById(R.id.tv_sleep_time);
        mTvShareValidity = (TextView) findViewById(R.id.tv_share_validity_time);
        findViewById(R.id.layout_sleep_time).setOnClickListener(this);
        findViewById(R.id.layout_share_validity).setOnClickListener(this);
        mProgressDialog = new ProgressDialog(this);
    }

    private void requestSettingData() {
        if (!UserUtils.isNetDeviceAvailable(null)) {
            GolukUtils.showToast(null, getString(R.string.user_net_unavailable));
            return;
        }
        mDeviceSleepSettingGetRequest = new DeviceSleepSettingGetRequest(CODE_REQUEST_DEVICE_SLEEP_SETTING, this);
        mShareValiditySettingGetRequest = new ShareValiditySettingGetRequest(CODE_REQUEST_SHARE_VALIDITY_TIME, this);
        mDeviceSleepSettingGetRequest.get(CarControlApplication.getInstance().serverImei);
        mShareValiditySettingGetRequest.get(CarControlApplication.getInstance().getMyInfo().uid);
        mShareValiditySettingPostRequest = new ShareValiditySettingPostRequest(CODE_POST_SETTING_SHARE_TIME, this);
        mDeviceSleepSettingPostRequest = new DeviceSleepSettingPostRequest(CODE_POST_SETTING_SLEEP_TIME, this);
    }

    private int toPage;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_sleep_time:
                toPage = TimeChooseListActivity.PAGE_SLEEP_SETTING;
                break;
            case R.id.layout_share_validity:
                toPage = TimeChooseListActivity.PAGE_SHARE_SETTING;
                break;
        }
        startChooseActivity();
    }

    private void startChooseActivity() {
        Intent intent = new Intent(this, TimeChooseListActivity.class);
        intent.putExtra("currentActivity", toPage);
        if (toPage == TimeChooseListActivity.PAGE_SHARE_SETTING) {
            intent.putExtra("currentValue", currentShareValue);
        } else if (toPage == TimeChooseListActivity.PAGE_SLEEP_SETTING) {
            intent.putExtra("currentValue", currentSleepValue);
        }
        startActivityForResult(intent, toPage);
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
        switch (requestType) {
            case CODE_REQUEST_DEVICE_SLEEP_SETTING:
                DeviceSleepSettingResult baseResult = (DeviceSleepSettingResult) result;
                if (baseResult != null && baseResult.code == 0) {
                    DeviceSleepSettingResult.DeviceSleepSettingBean bean = baseResult.data;
                    currentSleepValue = bean.dormancyTime + "";
                    if (bean.dormancyTime == -1) {
                        mTvSleepTime.setText(getString(R.string.str_never));
                    } else {
                        mTvSleepTime.setText(bean.dormancyTime + " " + getString(R.string.min));
                    }
                } else {
                    GolukUtils.showToast(null, getString(R.string.str_get_sleep_time_fail));
                }
                break;
            case CODE_REQUEST_SHARE_VALIDITY_TIME:
                ShareValiditySettingResult baseResult2 = (ShareValiditySettingResult) result;
                if (baseResult2 != null && baseResult2.code == 0) {
                    ShareValiditySettingResult.ShareValiditySettingBean bean = baseResult2.data;
                    int value = bean.sharedLinkTime;
                    if(value != -1)
                        value = value / 60;
                    currentShareValue = value + "";
                    if (bean.sharedLinkTime == -1) {
                        mTvShareValidity.setText(getString(R.string.str_longtime));
                    } else {
                        mTvShareValidity.setText(bean.sharedLinkTime / 60 + " " + getString(R.string.str_unit_hour));
                    }
                } else {
                    GolukUtils.showToast(null, getString(R.string.str_get_share_time_fail));
                }
                break;
            case CODE_POST_SETTING_SHARE_TIME:
                ServerBaseResult baseResult3 = (ServerBaseResult) result;
                if (baseResult3 != null && baseResult3.code == 0) {
                    CarControlApplication.getInstance().userLiveValidity = value;
                    if (value == -1) {
                        mTvShareValidity.setText(getString(R.string.str_longtime));
                    } else {
                        value = value / 60;
                        mTvShareValidity.setText(value+ " " + getString(R.string.str_unit_hour));
                    }
                    currentShareValue = value + "";
                    GolukUtils.showToast(null, getString(R.string.setting_ok));
                } else {
                    GolukUtils.showToast(null, getString(R.string.str_setting_fail));
                }
                break;
            case CODE_POST_SETTING_SLEEP_TIME:
                ServerBaseResult baseResult4 = (ServerBaseResult) result;
                if (baseResult4 != null && baseResult4.code == 0) {
                    if (value == -1) {
                        mTvSleepTime.setText(getString(R.string.str_never));
                    } else {
                        mTvSleepTime.setText(value + " " + getString(R.string.min));
                    }
                    currentSleepValue = value + "";
                    GolukUtils.showToast(null, getString(R.string.setting_ok));
                } else {
                    GolukUtils.showToast(null, getString(R.string.str_setting_fail));
                }
                break;
        }
    }

    private int value = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String result = data.getStringExtra("result");
            if (result == null || TextUtils.isEmpty(result)) return;
            value = Integer.parseInt(result);
            if (requestCode == TimeChooseListActivity.PAGE_SLEEP_SETTING) {
                postSelectSleepTime(value);
            } else if (requestCode == TimeChooseListActivity.PAGE_SHARE_SETTING) {
                if (value != -1)
                    value = value * 60;
                postSelectShareTime(value);
            }
        }
    }

    private void postSelectSleepTime(int value) {
        if (!UserUtils.isNetDeviceAvailable(null)) {
            GolukUtils.showToast(null, getString(R.string.user_net_unavailable));
            return;
        }
        mDeviceSleepSettingPostRequest.post(CarControlApplication.getInstance().serverImei, KEY_SET_SLEEP, value);
        mProgressDialog.show();
    }

    private void postSelectShareTime(int value) {
        if (!UserUtils.isNetDeviceAvailable(null)) {
            GolukUtils.showToast(null, getString(R.string.user_net_unavailable));
            return;
        }
        mShareValiditySettingPostRequest.post(CarControlApplication.getInstance().getMyInfo().uid, KEY_SET_SHARE, value);
        mProgressDialog.show();
    }

    @Override
    public boolean handleMessage(Message msg){
        return false;
    }
}
