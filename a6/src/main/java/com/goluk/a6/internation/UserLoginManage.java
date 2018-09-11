package com.goluk.a6.internation;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.alibaba.fastjson.JSON;
import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.control.R;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.internation.bean.UserData;
import com.goluk.a6.internation.bean.UserInfo;
import com.goluk.a6.internation.bean.UserResult;
import com.goluk.a6.internation.login.OtherUserloginBeanRequest;
import com.goluk.a6.internation.login.UserloginBeanRequest;

import org.json.JSONObject;

import java.util.HashMap;

import likly.dollar.$;

/**
 * 登录管理类
 *
 * @author mobnote
 */
public class UserLoginManage implements IRequestResultListener {

    private CarControlApplication mApp = null;
    private SharedPreferences mSharedPreferences = null;
    private Editor mEditor = null;
    private UserLoginInterface mLoginInterface = null;

    private UserloginBeanRequest userloginBean = null;

    private OtherUserloginBeanRequest otherloginBean = null;

    private String mPwd = "";

    /**
     * 用户信息
     **/
    private String mPhone = "";
    private String mEmail = "";
    private String platform = "";
    /**
     * 输入密码错误限制
     */
    public int countErrorPassword = 1;

    public UserLoginManage(CarControlApplication mApp) {
        super();
        this.mApp = mApp;
    }

    public void setUserLoginInterface(UserLoginInterface mInterface) {
        this.mLoginInterface = mInterface;
    }

    public void loginStatusChange(int mStatus) {
        mApp.loginStatus = mStatus;
        if (mLoginInterface != null) {
            mLoginInterface.loginCallbackStatus();
        }
    }

    /**
     * 登陆 当帐号和密码输入框都有内容时,激活为可点击状态
     */
    public void loginByPhone(String phone, String code, String pwd, String uid) {
        userloginBean = new UserloginBeanRequest(false, IPageNotifyFn.PageType_Login, this);
        mPhone = phone;
        mEmail = "";
        mPwd = pwd;
        platform = "phone";
        userloginBean.loginByPhone(phone, code, mPwd, uid);
    }

    public void loginByEmail(String email, String pwd, String uid) {
        userloginBean = new UserloginBeanRequest(true, IPageNotifyFn.LOGIN_BY_EMAIL, this);
        mEmail = email;
        mPhone = "";
        mPwd = pwd;
        platform = "email";
        userloginBean.loginByEmail(email, mPwd, uid);
    }

    public void loginBy3rdPlatform(HashMap<String, String> info) {
        otherloginBean = new OtherUserloginBeanRequest(IPageNotifyFn.PageType_OauthLogin, this);
        platform = "";
        otherloginBean.get(info);
    }

    /**
     * 同步获取用户信息
     */
    @SuppressWarnings("unused")
    public void initData() {

        UserInfo myInfo = null;
        String userInfo = SharedPrefUtil.getUserInfo();

        if (null != userInfo && !"".equals(userInfo) && myInfo != null && myInfo.phone != null
                && !"".equals(myInfo.phone)) {
            myInfo = JSON.parseObject(userInfo, UserInfo.class);
            // 退出登录后，将信息存储
            mSharedPreferences = mApp.getSharedPreferences("setup", Context.MODE_PRIVATE);
            mEditor = mSharedPreferences.edit();
            mEditor.putString("setupPhone", UserUtils.formatSavePhone(myInfo.phone));
            mEditor.putBoolean("noPwd", false);
            mEditor.putString("uid", myInfo.uid);
            mEditor.commit();
        }

    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == IPageNotifyFn.PageType_Login
                || IPageNotifyFn.PageType_OauthLogin == requestType
                || IPageNotifyFn.LOGIN_BY_EMAIL == requestType) {
            try {
                UserResult userresult = (UserResult) result;
                if (userresult == null) {
                    loginStatusChange(4);
                    return;
                }
                int code = userresult.code;
                switch (code) {
                    case 0:
                        // 登录成功后，存储用户的登录信息
                        UserData userdata = userresult.data;
                        userdata.email = mEmail;
                        userdata.phone = mPhone;
                        String uid = userdata.uid;
                        mSharedPreferences = mApp.getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
                        mEditor = mSharedPreferences.edit();
                        mEditor.putBoolean("FirstLogin", false);
                        // 提交
                        mEditor.commit();
                        userdata.platform = platform;
                        mSharedPreferences = mApp.getSharedPreferences("setup", Context.MODE_PRIVATE);
                        mEditor = mSharedPreferences.edit();
                        mEditor.putString("uid", uid);
                        mEditor.commit();
                        String userdataString = com.alibaba.fastjson.JSONObject.toJSONString(userdata);
                        UserInfo userInfo = $.json().toBean(userdataString, UserInfo.class);
                        mApp.setUserInfo(userInfo);
                        //SharedPrefUtil.saveUserInfo(userdataString);
                        SharedPrefUtil.saveUserToken(userdata.token);
                        JSONObject json = new JSONObject();

                        if (!"".equals(mPhone)) {
                            json.put("phone", mPhone);
                        }
                        if (!"".equals(mPwd)) {
                            json.put("pwd", mPwd);
                        }
                        json.put("uid", userdata.uid);
                        SharedPrefUtil.saveUserPwd(json.toString());
                        // 登录成功跳转
                        if (mApp.registStatus != 2) {
                            GolukUtils.showToast(mApp, mApp.getResources().getString(R.string.user_login_success));
                        }
                        loginStatusChange(1);// 登录成功
                        mApp.isUserLoginSucess = true;
                        mApp.parseLoginData(userdata);
                        setCommHeadToLogic();

                        // 保存登录名
                        $.config().putString("phone", mPhone);
                        $.config().putString("email", mEmail);
                        break;
                    default:
                        loginStatusChange(2);
                        GolukUtils.showToast(mApp, mApp.getResources().getString(R.string.hint_msg_user_error));
                        break;
                }
            } catch (Exception ex) {
                loginStatusChange(2);
                ex.printStackTrace();
            }
        }
    }

    public void setCommHeadToLogic() {
    }

}
