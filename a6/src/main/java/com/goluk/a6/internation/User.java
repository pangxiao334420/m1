package com.goluk.a6.internation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;


import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.internation.bean.UserResult;
import com.goluk.a6.internation.login.UserloginBeanRequest;

import java.util.Timer;
import java.util.TimerTask;


/**
 * 自动登录
 *
 * @author mobnote
 */
@SuppressLint("HandlerLeak")
public class User implements IRequestResultListener {

    /**
     * 记录登录状态
     **/
    public SharedPreferences mPreferencesAuto;
    public boolean isFirstLogin;
    /**
     * 设置网络5分钟自动重试机制的定时器
     */
    private Handler mHandler = null;
    private Timer mTimer = null;
    private Context mContext = null;

    private CarControlApplication mApp = null;
    private UserInterface mUserInterface = null;
    /**
     * APP退出后不再进行自动登录
     **/
    public boolean mForbidTimer = false;

    private UserloginBeanRequest userloginBean = null;

    public User(CarControlApplication mApp) {
        this.mApp = mApp;
        mContext = mApp.getApplicationContext();
        userloginBean = new UserloginBeanRequest(false, IPageNotifyFn.PageType_AutoLogin, this);
        mForbidTimer = false;

        //初始化Handler
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {//  1是处理5分钟超时的处理
                    timerCancel();
                    initAutoLogin();
                }
                super.handleMessage(msg);
            }
        };
    }

    public void setUserInterface(UserInterface mInterfaces) {
        this.mUserInterface = mInterfaces;
    }

    /**
     * 不是第一次登录的话，调用自动登录
     * 当用户使用到需要『登录在线』条件下登录权限的功能时
     * ——判断用户是否在自动登录，若是，则客户端使用系统 loading 提示：正在为您登录，请稍后…
     */
    public void initAutoLogin() {
        String userinfo = SharedPrefUtil.getUserPwd();
        if (userinfo != null && !"".equals(userinfo)) {
            StatusChange(2);
            mApp.loginoutStatus = false;
            mApp.isUserLoginSucess = true;
        } else {
            StatusChange(3);
        }
    }

    public void StatusChange(int aStatus) {
        mApp.autoLoginStatus = aStatus;
        if (mUserInterface != null) {
            mUserInterface.statusChange();
        }
    }


    /**
     * 设置网络5分钟自动重试机制的定时器
     * 1000x60x5=300000
     */
    public void timerTask() {
        timerCancel();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                mHandler.sendEmptyMessage(1);
            }
        }, 300000);
    }

    public void timerCancel() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public void exitApp() {
        mForbidTimer = true;
        timerCancel();
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (IPageNotifyFn.PageType_AutoLogin == requestType) {
            if (mForbidTimer) {
                return;
            }
            timerCancel();
            try {
                UserResult userresult = (UserResult) result;
                int code =userresult.code;

                String loginMsg = GolukFastJsonUtil.setParseObj(userresult);

                switch (code) {
                    case 0:
                        StatusChange(2);// 自动登录成功
                        mApp.loginoutStatus = false;
                        mApp.isUserLoginSucess = true;
                        mApp.parseLoginData(userresult.data);
                        SharedPrefUtil.saveUserInfo(com.alibaba.fastjson.JSONObject.toJSONString(userresult.data));
                        break;
                    // 自动登录的一切异常都不进行提示
                    default:
                        StatusChange(3);// 自动登录失败
                        break;
                }
            } catch (Exception e) {
                StatusChange(3);// 自动登录失败
                e.printStackTrace();
            }
        }
    }
}
