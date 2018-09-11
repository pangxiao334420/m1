package com.goluk.a6.control;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.VolleyLog;
import com.goluk.a6.api.HttpManager;
import com.goluk.a6.common.ThumbnailCacheManager;
import com.goluk.a6.common.event.EventConfig;
import com.goluk.a6.common.event.EventUserLoginRet;
import com.goluk.a6.common.event.ImeiUpdateEvent;
import com.goluk.a6.common.event.util.EventUtil;
import com.goluk.a6.common.map.TraceCacheManager;
import com.goluk.a6.control.util.HttpDownloadManager;
import com.goluk.a6.control.util.HttpRequestManager;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.AppUpgradeRequest;
import com.goluk.a6.http.request.BindListRequest;
import com.goluk.a6.http.request.ShareValiditySettingGetRequest;
import com.goluk.a6.http.responsebean.AppUpgradeResult;
import com.goluk.a6.http.responsebean.BindAddResult;
import com.goluk.a6.http.responsebean.BindListResult;
import com.goluk.a6.http.responsebean.ShareValiditySettingResult;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.SharedPrefUtil;
import com.goluk.a6.internation.TimerManage;
import com.goluk.a6.internation.User;
import com.goluk.a6.internation.UserIdentifyManage;
import com.goluk.a6.internation.UserLoginManage;
import com.goluk.a6.internation.UserUtils;
import com.goluk.a6.internation.bean.UserData;
import com.goluk.a6.internation.bean.UserInfo;
import com.goluk.a6.internation.login.UserRegistAndRepwdManage;
import com.google.firebase.FirebaseApp;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.Logger;
import com.tencent.bugly.crashreport.CrashReport;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Calendar;

import likly.dollar.$;

public class CarControlApplication extends MultiDexApplication implements IRequestResultListener {
    private static final String TAG = "CarSvc_CarControlApplication";
    private static final int CODE_REQUEST_SHARE_VALIDITY_TIME = 10002;
    public UserIdentifyManage mIdentifyManage = null;

    private static CarControlApplication mInstance;
    public TimerManage mTimerManage = null;
    public UserLoginManage mLoginManage = null;
    public boolean isUserLoginSucess;
    public int loginStatus;
    public int registStatus;

    public int identifyStatus;
    public UserRegistAndRepwdManage mRegistAndRepwdManage = null;
    public int autoLoginStatus;
    public boolean loginoutStatus;
    public User mUser = null;
    public BindListResult.BindListBean bindListBean;
    public String currentImei;
    public String serverImei;
    public String defaultDeviceIccid;
    BindListRequest request;
    public boolean appStarted = false;
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;//50MB
    private boolean loadImei = false;
    AppUpgradeRequest requestUpgrade;
    private CarControlActivity carControlActivity;

    private ShareValiditySettingGetRequest mShareValiditySettingGetRequest;
    public int userLiveValidity = 120;

    public void setImei(String imei) {
        boolean hasChanged = !TextUtils.equals(serverImei, imei);

        SharedPrefUtil.saveImei(imei);
        serverImei = imei;
        currentImei = imei;

        if (hasChanged) {
            // 设备发生变化
            EventUtil.sendDefaultDeviceChangedEvent();
        }
    }
    public void setIccid(String iccid) {
        SharedPrefUtil.saveIccid(iccid);
        defaultDeviceIccid = iccid;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mTimerManage = new TimerManage(this);
        CrashReport.initCrashReport(getApplicationContext(), getString(R.string.bugly_id), false);
        ThumbnailCacheManager.initialize(this);
        HttpDownloadManager.create();
        HttpRequestManager.create();
        File file = new File(Config.CARDVR_CACHE_PATH);
        if (!file.exists()) file.mkdirs();
        file = new File(Config.CARDVR_AD_PATH);
        if (!file.exists()) file.mkdirs();
        mLoginManage = new UserLoginManage(this);
        mIdentifyManage = new UserIdentifyManage(this);
        mRegistAndRepwdManage = new UserRegistAndRepwdManage(this);
        mUser = new User(this);
        TraceCacheManager.create(this, (int) DISK_CACHE_SIZE);
        if (BuildConfig.DEBUG) {
            VolleyLog.DEBUG = true;
        }
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });
        Logger.addLogAdapter(new DiskLogAdapter());
        serverImei = SharedPrefUtil.getImei();
        request = new BindListRequest(0, this);
        requestUpgrade = new AppUpgradeRequest(1, this);

        HttpManager.initHttp(this);
        $.initialize(this);

        // Firebase
        FirebaseApp.initializeApp(this);
    }

    public static CarControlApplication getInstance() {
        return mInstance;
    }

    public void parseLoginData(UserData userdata) {
        if (userdata != null) {
            isUserLoginSucess = true;
            EventBus.getDefault().post(new EventUserLoginRet(EventConfig.USER_LOGIN_RET, true, 0));
        }
    }

    public void setContext(Context mContext, String userLogin) {
    }

    public boolean isUserLoginToServerSuccess() {
        if (getMyInfo() == null) {
            return false;
        }
        return (isUserLoginSucess) || (loginStatus == 1) || (autoLoginStatus == 2) || (autoLoginStatus == 1);
    }

    private UserInfo mUserInfo;

    public UserInfo getMyInfo() {
        if (mUserInfo != null)
            return mUserInfo;

        try {
            String json = SharedPrefUtil.getUserInfo();
            if (!TextUtils.isEmpty(json)) {
                mUserInfo = JSON.parseObject(json, UserInfo.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mUserInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        mUserInfo = userInfo;
        String jsonData = "";
        if (userInfo != null)
            jsonData = JSONObject.toJSONString(mUserInfo);
        SharedPrefUtil.saveUserInfo(jsonData);
    }

    public void checkUpgrade(CarControlActivity carControlActivity) {
        this.carControlActivity = carControlActivity;
        if (!UserUtils.isNetDeviceAvailable(this) || GolukUtils.todayChecked() || appStarted) {
            return;
        }

        String uid = "";
        String version = GolukUtils.getAppVersion(this);
//        if (!isUserLoginToServerSuccess()) {
//            return;
//        }
        requestUpgrade.get(uid, version);
    }


    public void killApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    public void getImei() {
        //setImei("");
        if (CarControlApplication.getInstance().isUserLoginToServerSuccess()) {
            request.get(CarControlApplication.getInstance().getMyInfo().uid);
        }
    }

    public void getShareValidity(){
        if (mShareValiditySettingGetRequest==null){
            mShareValiditySettingGetRequest = new ShareValiditySettingGetRequest(CODE_REQUEST_SHARE_VALIDITY_TIME,this);
        }
        if (CarControlApplication.getInstance().isUserLoginToServerSuccess()) {
            mShareValiditySettingGetRequest.get(CarControlApplication.getInstance().getMyInfo().uid);
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == 0) {
            BindListResult bean = (BindListResult) result;
            // 是否有设备被其他用户绑走
            if (bean != null && bean.data.notice != 0) {
                EventUtil.sendBindedByOtherEvent();
            }

            // 更新Default IMEI
            if (bean != null && bean.code == 0 && bean.data != null && !TextUtils.isEmpty(bean.data.defaultId)) {
                bindListBean = bean.data;
                for (BindAddResult.BindBean temp : bean.data.list) {
                    if (temp.bindId.equals(bean.data.defaultId)) {
                        setImei(temp.imei);
                        setIccid(temp.iccid);
                        EventBus.getDefault().post(new ImeiUpdateEvent());
                        return;
                    }
                }
            }
            setImei("");
            setIccid("");
            EventBus.getDefault().post(new ImeiUpdateEvent());
        } else if (requestType == 1) {
            final AppUpgradeResult app = (AppUpgradeResult) result;
            final long today = Calendar.getInstance().getTime().getTime() / (24 * 60 * 60 * 1000);
            if (carControlActivity != null && app != null && app.code == 0 && app.data != null && app.data.app != null) {
                AlertDialog dialog = new AlertDialog.Builder(carControlActivity)
                        .setTitle(R.string.new_app)
                        .setMessage(app.data.app.description)
                        .setPositiveButton(R.string.upgrade_app, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                startdownload(app.data.app.fileurl);
                            }
                        })
                        .setCancelable(false)
                        .setNegativeButton(R.string.later_upgrade, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                SharedPrefUtil.saveDay(today);
                            }
                        }).create();
                dialog.show();
            }
        }else if (requestType==CODE_REQUEST_SHARE_VALIDITY_TIME){
            ShareValiditySettingResult shareValidity = (ShareValiditySettingResult) result;
            if (shareValidity!=null&&shareValidity.code==0){
                ShareValiditySettingResult.ShareValiditySettingBean bean = shareValidity.data;
                userLiveValidity = bean.sharedLinkTime;
            }
        }
    }

    private void startdownload(String url) {
        if (!BuildConfig.BRANCH_CHINA) {
            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
            Intent intent = null;
            try {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (android.content.ActivityNotFoundException anfe) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else {
            try {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(url);
                intent.setData(content_url);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
            } catch (ActivityNotFoundException anfe) {
                Toast.makeText(this,
                        R.string.cannot_open_broswer,
                        Toast.LENGTH_SHORT).show();
                anfe.printStackTrace();
            }
        }
    }


    public boolean isBoundIMei() {
        return isBoundIMei(false);
    }

    public boolean isBoundIMei(boolean reload) {
        boolean res = isUserLoginToServerSuccess() && !TextUtils.isEmpty(serverImei);
        if (!res && reload) {
            getImei();
        }
        return res;
    }


    public boolean haveBound(String imei) {
        if (!isUserLoginToServerSuccess()) {
            return false;
        }
        if (bindListBean != null && bindListBean.list != null) {
            for (BindAddResult.BindBean bean : bindListBean.list) {
                if (imei.equals(bean.imei)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 是否曾经成功绑定过设备，并把个人信息与设备信息都上传到服务器上
     */
    public boolean haveBoundSuccess() {
        return !TextUtils.isEmpty(serverImei);
    }
}
