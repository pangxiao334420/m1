package com.goluk.a6.internation.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.a6.common.event.EventLoginSuccess;
import com.goluk.a6.common.event.UpdateBindListEvent;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.R;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.internation.CustomLoadingDialog;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.IPageNotifyFn;
import com.goluk.a6.internation.SharedPrefUtil;
import com.goluk.a6.internation.UserRegistAndRepwdInterface;
import com.goluk.a6.internation.UserUtils;
import com.goluk.a6.internation.bean.CheckVcodeBean;
import com.goluk.a6.internation.bean.EmailVcodeRetBean;
import com.goluk.a6.internation.bean.ResetPwdByEmailRetBean;
import com.goluk.a6.internation.bean.UserData;
import com.goluk.a6.internation.bean.UserResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import likly.dollar.$;

/**
 * 获取验证码
 *
 * @author mobnote
 */
public class InternationUserIdentifyActivity extends BaseActivity implements OnClickListener, UserRegistAndRepwdInterface, IRequestResultListener {

    public static final String IDENTIFY_DIFFERENT = "identify_different";
    public static final String IDENTIFY_PHONE = "identify_phone";
    public static final String IDENTIFY_PASSWORD = "identify_password";
    public static final String IDENTIFY_INTER_REGIST = "identify_inter_regist";
    public static final String IDENTIFY_REGISTER_CODE = "REGISTER_CODE";
    public static final String KEY_EMAIL_ADDRESS = "email_address";
    private static final String TAG = "lily";
    /**
     * Application & Context
     **/
    private Context mContext = null;
    private Button mBtnNext = null;
    private TextView mRetryGetCode = null;
    /**
     * 跳转注册页标识
     **/
    private String intentRegistInter = "";
    /**
     * 获取验证码
     **/
    private CustomLoadingDialog mCustomDialogIdentify = null;
    /**
     * 注册
     **/
    private CustomLoadingDialog mCustomDialogRegist = null;
    /**
     * 重置密码
     **/
    private CustomLoadingDialog mCustomDialogRepwd = null;
    private SharedPreferences mSharedPreferences = null;
    private Editor mEditor = null;
    /**
     * true/false 注册/重置密码标识
     **/
    private boolean justDifferent = false;
    private EditText mPwdEditText = null;
    private EditText mCodeEditText = null;

    private String mZone = null;
    /**
     * 发送验证码手机号
     **/
    private String mUserPhone = "";
    private String mEmail = "";
    private boolean isResetByPhone;

    /**
     * 密码
     **/
    private String intentPassword = "";
    /**
     * title
     **/
    private ImageButton mBtnBack;

    private TextView mCodeText, mTitleText;

    public UserloginBeanRequest userloginBean = null;
    public int countDown = 60;
    private Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            countDown--;
            if (countDown > 0) {
                mHandler.sendEmptyMessageDelayed(1, 1000);
                mRetryGetCode.setText(getString(R.string.resend_code_txt) + "(" + String.valueOf(countDown) + ")");
                mRetryGetCode.setEnabled(false);
            } else {
                mRetryGetCode.setEnabled(true);
                mRetryGetCode.setText(getString(R.string.resend_code_txt));
                countDown = 60;
            }
            return true;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getInfo();
        if (justDifferent) {
            setContentView(R.layout.internation_user_identify_layout);
        } else {
            getWindow().setContentView(R.layout.user_reset_pwd_layout);
            mTitleText = (TextView) findViewById(R.id.user_title_text);
            mTitleText.setText(getString(R.string.str_reset_pwd_title));
        }
        mContext = this;
        initView();
        mHandler.sendEmptyMessageDelayed(1, 1000);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApp.setContext(mContext, "UserIdentify");
        if (!justDifferent) {
            mPwdEditText.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    changeBtnColor();
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

                }

                @Override
                public void afterTextChanged(Editable arg0) {

                }
            });
        }
        mCodeEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                changeBtnColor();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable arg0) {

            }
        });

    }

    /**
     * 初始化view
     */
    public void initView() {
        mBtnNext = (Button) findViewById(R.id.user_identify_btn);
        mRetryGetCode = (TextView) findViewById(R.id.user_identify_retry);
        mRetryGetCode.setEnabled(false);
        mPwdEditText = (EditText) findViewById(R.id.user_login_pwd);
        mCodeEditText = (EditText) findViewById(R.id.user_identity_code);
        mBtnBack = (ImageButton) findViewById(R.id.back_btn);
        mCodeText = (TextView) findViewById(R.id.tv_code);
        mBtnNext.setOnClickListener(this);
        mRetryGetCode.setOnClickListener(this);
        mBtnBack.setOnClickListener(this);

        // 获取验证码
        if (null == mCustomDialogIdentify) {
            mCustomDialogIdentify = new CustomLoadingDialog(mContext, this.getResources().getString(
                    R.string.str_identify_loading));
        }
        // 注册
        if (null == mCustomDialogRegist) {
            mCustomDialogRegist = new CustomLoadingDialog(mContext, this.getResources().getString(
                    R.string.str_regist_loading));
        }
        // 重置密码
        if (null == mCustomDialogRepwd) {
            mCustomDialogRepwd = new CustomLoadingDialog(mContext, this.getResources().getString(
                    R.string.str_repwd_loading));
        }

        if (isResetByPhone && null != mZone) {
            int zoneCode = mZone.indexOf("+");
            String code = mZone.substring(zoneCode, mZone.length());
            mCodeText.setText(code + " " + mUserPhone);
        } else {
            mCodeText.setText(mEmail);
        }

    }

    /**
     * 获取信息
     */
    public void getInfo() {
        Intent it = getIntent();
        if (null == it) {
            return;
        }

        if (null != it.getStringExtra(IDENTIFY_PHONE)) {
            mUserPhone = it.getStringExtra(IDENTIFY_PHONE).toString();
        }
        mEmail = it.getStringExtra(KEY_EMAIL_ADDRESS);
        if (!TextUtils.isEmpty(mUserPhone)) {
            isResetByPhone = true;
        } else {
            isResetByPhone = false;
        }

        mZone = it.getStringExtra(InternationUserIdentifyActivity.IDENTIFY_REGISTER_CODE);
        justDifferent = it.getBooleanExtra(IDENTIFY_DIFFERENT, false);

        if (null != it.getStringExtra(IDENTIFY_PASSWORD)) {
            intentPassword = it.getStringExtra(IDENTIFY_PASSWORD).toString();
        }

        if (null != it.getStringExtra(IDENTIFY_INTER_REGIST)) {
            intentRegistInter = it.getStringExtra(IDENTIFY_INTER_REGIST).toString();
        }
    }

    @Override
    public void onClick(View view) {
        if (R.id.back_btn == view.getId()) {
            finish();
        } else if (R.id.user_identify_retry == view.getId()) {
            if (!UserUtils.isNetDeviceAvailable(this)) {
                GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
                return;
            }
            // 重新获取验证码
            if (isResetByPhone) {
                getUserIdentify();
            } else {
                requestEmailVcode();
            }
        } else if (R.id.user_identify_btn == view.getId()) {
            String pwd = "";
            String code = mCodeEditText.getText().toString();
            if (!justDifferent) {
                pwd = mPwdEditText.getText().toString();
                if ("".equals(pwd) && pwd.length() < 0) {
                    GolukUtils.showToast(this, this.getResources().getString(R.string.user_no_getidentify));
                    return;
                }
                intentPassword = pwd;
            }
            if (!UserUtils.isNetDeviceAvailable(mContext)) {
                GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
                return;
            }
            if (isResetByPhone) {
                toRegistAndRepwd(justDifferent, mUserPhone, intentPassword, code);
            } else {
                resetPwdByEmail(intentPassword, code);
            }
        }

    }

    private void resetPwdByEmail(String pwd, String vcode) {
        new ResetPwdByEmailRequest(IPageNotifyFn.RESET_PWD_BY_EMAIL, this).send(mEmail, pwd, vcode);
    }

    /**
     * 重新获取验证码
     */
    private void getUserIdentify() {
        int zoneCode = mZone.indexOf("+");
        String code = mZone.substring(zoneCode + 1, mZone.length());
        getVCode(mUserPhone, code);
        UserUtils.hideSoftMethod(this);
        mCustomDialogIdentify.show();
        mHandler.sendEmptyMessageDelayed(1, 1000);
    }

    private void requestEmailVcode() {
        UserUtils.hideSoftMethod(this);
        mCustomDialogIdentify.show();
        new EmailVcodeRequest(IPageNotifyFn.SEND_EMAIL_VCODE, this).send(mEmail, "2");
    }

    /**
     * 获取验证码成功
     *
     * @author jyf
     */
    private void getCodeSuccess() {
        closeDialogIdentify();
        GolukUtils.showToast(this, getResources().getString(R.string.user_getidentify_success));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventLoginSuccess event) {
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
        }
        return false;
    }

    /**
     * 关闭获取验证码的loading
     */
    private void closeDialogIdentify() {
        if (null != mCustomDialogIdentify) {
            mCustomDialogIdentify.close();
            mBtnNext.setEnabled(true);
        }
    }

    /**
     * 关闭注册loading
     */
    private void closeDialogRegist() {
        if (null != mCustomDialogRegist) {
            mCustomDialogRegist.close();
            mBtnNext.setEnabled(true);
        }
    }

    /**
     * 关闭重置密码loading
     */
    private void closeDialogRepwd() {
        if (null != mCustomDialogRepwd) {
            mCustomDialogRepwd.close();
            mBtnNext.setEnabled(true);
        }
    }

    /**
     * 判断关闭哪个对话框 // 从设置页注册 it.putExtra("fromRegist", "fromSetup");
     *
     * @param b
     */
    public void justCloseDialog(boolean b) {
        if (b) {
            closeDialogRegist();
        } else {
            closeDialogRepwd();
        }
    }

    /**
     * 注册/重置密码
     *
     * @param flag
     * @param phone
     * @param password
     * @param vCode
     */
    @SuppressWarnings("static-access")
    public void toRegistAndRepwd(boolean flag, String phone, String password, String vCode) {
        if ("".equals(vCode) || null == vCode) {
            GolukUtils.showToast(mApp, this.getResources().getString(R.string.user_no_getidentify));
        } else {
            if (vCode.length() < 4) {
                GolukUtils.showToast(mApp, this.getResources()
                        .getString(R.string.user_identify_format));
            } else {
                if (mApp.mIdentifyManage.useridentifymanage_count > mApp.mIdentifyManage.IDENTIFY_COUNT) {
                    UserUtils.showDialog(mContext,
                            this.getResources().getString(R.string.count_identify_count_six_limit));
                } else {
                    mApp.mRegistAndRepwdManage.setUserRegistAndRepwd(this);
                    String zone = mZone.substring(mZone.indexOf("+") + 1);
                    boolean b = mApp.mRegistAndRepwdManage.registAndRepwd(flag, phone, password, vCode, zone);
                    if (b) {
                        if (flag) {
                            mCustomDialogRegist.show();
                        } else {
                            mCustomDialogRepwd.show();
                        }
                        mBtnNext.setEnabled(false);
                    } else {
                        justCloseDialog(flag);
                        if (flag) {
                            GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_regist_fail));
                        } else {
                            GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_repwd_fail));
                        }
                    }
                }
            }
        }
    }

    /**
     * 注册/重置密码接口回调
     */
    @Override
    public void registAndRepwdInterface() {
        justCloseDialog(justDifferent);
        switch (mApp.registStatus) {
            // 注册/重置密码中
            case 1:
                mBtnNext.setEnabled(false);
                break;
            // 注册/重置密码成功
            case 2:
                if (justDifferent) {
                    mApp.registStatus = 0;
                    Intent it = new Intent(mApp, InternationUserPwdActivity.class);
                    it.putExtra("phone", mUserPhone);
                    it.putExtra("vcode", mCodeEditText.getText().toString());
                    it.putExtra("zone", mZone.substring(mZone.indexOf("+") + 1));
                    it.putExtra("from", intentRegistInter);
                    it.putExtra("step2code", mApp.mRegistAndRepwdManage.mStep2Code);
                    startActivity(it);
                } else {
                    GolukUtils.showToast(this, this.getResources().getString(R.string.user_repwd_success));
                    registLogin();
                }
                break;
            // 注册/重置失败
            case 3:
                if (justDifferent) {
                    GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_regist_fail));
                } else {
                    GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_repwd_fail));
                }
                break;
            // code = 500
            case 4:
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_background_error));
                break;
            // code = 405
            case 5:
                if (justDifferent) {
                    GolukUtils.showToast(this, this.getResources().getString(R.string.user_already_regist));
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
                            .setMessage(this.getResources().getString(R.string.user_no_regist))
                            .setNegativeButton(this.getResources().getString(R.string.user_cancle), null)
                            .setPositiveButton(this.getResources().getString(R.string.user_immediately_regist),
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            Intent intentRepwd = new Intent(InternationUserIdentifyActivity.this,
                                                    InternationUserRegistActivity.class);
                                            intentRepwd.putExtra("intentRepassword", mUserPhone);
                                            startActivity(intentRepwd);
                                            finish();
                                        }
                                    }).create().show();
                }
                break;
            // code = 406
            case 6:
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_identify_right_hint));
                break;
            // code = 407
            case 7:
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_identify_error));
                break;
            // code = 480
            case 8:
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_getidentify_fail));
                break;
            // 超时
            case 9:
                GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_netword_outtime));
                break;
            default:
                break;
        }
    }

    /**
     * 注册完成后自动调一次登录的接口，以存储用户信息
     */
    public void registLogin() {
        userloginBean = new UserloginBeanRequest(false, IPageNotifyFn.PageType_Login, this);
        int zoneCode = mZone.indexOf("+");
        String code = mZone.substring(zoneCode + 1, mZone.length());
        userloginBean.loginByPhone(mUserPhone.replace("-", ""), code, intentPassword, "");
        mApp.loginStatus = 0;// 登录中
    }

    private void loginByEmail() {
        userloginBean = new UserloginBeanRequest(true, IPageNotifyFn.PageType_Login, this);
        userloginBean.loginByEmail(mEmail, (intentPassword), "");
        mApp.loginStatus = 0;// 登录中
    }

    /**
     * 登录的回调
     */
    public void registLoginCallBack(int success, Object obj) {
        mApp.loginStatus = 0;// 登录中
        if (1 == success) {
            try {
                String data = (String) obj;
                JSONObject json = new JSONObject(data);
                int code = Integer.valueOf(json.getString("code"));
                JSONObject jsonData = json.optJSONObject("data");
                String uid = jsonData.optString("uid");
                switch (code) {
                    case 200:
                        // 登录成功后，存储用户的登录信息
                        mSharedPreferences = getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
                        mEditor = mSharedPreferences.edit();
                        mEditor.putBoolean("FirstLogin", false);
                        mEditor.commit();
                        mSharedPreferences = mApp.getSharedPreferences("setup", Context.MODE_PRIVATE);
                        mEditor = mSharedPreferences.edit();
                        mEditor.putString("uid", uid);
                        mEditor.commit();
                        // 登录成功跳转
                        mApp.loginStatus = 1;// 登录成功
                        mApp.isUserLoginSucess = true;
                        mApp.registStatus = 2;// 注册成功的状态
                        mApp.autoLoginStatus = 2;

                        Intent it = null;
                        mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
//                        if ("fromStart".equals(intentRegistInter)) {
//                            it = new Intent(InternationUserIdentifyActivity.this, MainActivity.class);
//                            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                            startActivity(it);
//                        } else if ("fromIndexMore".equals(intentRegistInter)) {
//                            it = new Intent(InternationUserIdentifyActivity.this, MainActivity.class);
//                            it.putExtra("showMe", "showMe");
//                            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                            startActivity(it);
//                        } else if ("fromSetup".equals(intentRegistInter)) {
//                            it = new Intent(InternationUserIdentifyActivity.this, UserSetupActivity.class);
//                            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                            startActivity(it);
//                        } else if ("fromProfit".equals(intentRegistInter)) {
//                            it = new Intent(InternationUserIdentifyActivity.this, MyProfitActivity.class);
//                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(it);
//                            UserUtils.exit();
//                        }
                        finish();
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // 回调执行失败
        }
    }

    /**
     * 保存手机号
     */
    public void putPhone() {
        mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mEditor.putString("setupPhone", mUserPhone);
        mEditor.putBoolean("noPwd", true);
        mEditor.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void changeBtnColor() {
        String password = "";
        String code = mCodeEditText.getText().toString();
        if (justDifferent) {
            if (!"".equals(code.trim())) {
                mBtnNext.setTextColor(Color.parseColor("#FFFFFF"));
                mBtnNext.setEnabled(true);
            } else {
                mBtnNext.setTextColor(Color.parseColor("#7fffffff"));
                mBtnNext.setEnabled(false);
            }
        } else {
            password = mPwdEditText.getText().toString();
            if (!"".equals(password.trim()) && password.length() > 5 && password.length() <= 16 && !"".equals(code.trim())) {
                mBtnNext.setTextColor(Color.parseColor("#000000"));
                mBtnNext.setEnabled(true);
            } else {
                mBtnNext.setTextColor(Color.parseColor("#33000000"));
                mBtnNext.setEnabled(false);
            }
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == IPageNotifyFn.PageType_Login) {
            try {
                UserResult userresult = (UserResult) result;
                int code = userresult.code;
                switch (code) {
                    case 0:
                        UserData userdata = userresult.data;
                        userdata.email = mEmail;
                        userdata.phone = mUserPhone;
                        // 登录成功后，存储用户的登录信息
                        if (!TextUtils.isEmpty(mUserPhone))
                            $.config().putString("phone", mUserPhone);
                        if (!TextUtils.isEmpty(mEmail))
                            $.config().putString("email", mEmail);
                        mSharedPreferences = getSharedPreferences("firstLogin", Context.MODE_PRIVATE);
                        mEditor = mSharedPreferences.edit();
                        mEditor.putBoolean("FirstLogin", false);
                        mEditor.commit();
                        mSharedPreferences = mApp.getSharedPreferences("setup", Context.MODE_PRIVATE);
                        mEditor = mSharedPreferences.edit();
                        mEditor.putString("uid", userresult.data.uid);
                        mEditor.commit();
                        // 登录成功跳转
                        mApp.loginStatus = 1;// 登录成功
                        mApp.isUserLoginSucess = true;
                        mApp.registStatus = 2;// 注册成功的状态
                        mApp.autoLoginStatus = 2;

                        Intent it = null;
                        mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);

                        SharedPrefUtil.saveUserInfo(com.alibaba.fastjson.JSONObject.toJSONString(userresult.data));
                        SharedPrefUtil.saveUserToken(userresult.data.token);
                        JSONObject json = new JSONObject();

                        if (!"".equals(userresult.data.phone)) {
                            json.put("phone", userresult.data.phone);
                        }
                        if (!"".equals(intentPassword)) {
                            json.put("pwd", intentPassword);
                        }
                        json.put("uid", userresult.data.uid);
                        SharedPrefUtil.saveUserPwd(json.toString());

                        mApp.getInstance().parseLoginData(userresult.data);
                        EventBus.getDefault().post(new UpdateBindListEvent());
                        EventBus.getDefault().post(new EventLoginSuccess());
                        break;
                    default:
                        GolukUtils.showToast(this, userresult.msg);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (IPageNotifyFn.SEND_EMAIL_VCODE == requestType) {
            closeDialogIdentify();
            EmailVcodeRetBean retBean = (EmailVcodeRetBean) result;
            if (retBean == null) {
                return;
            }
            if (retBean.code != 0) {
                Toast.makeText(this, retBean.msg, Toast.LENGTH_SHORT).show();
                return;
            }
            mHandler.sendEmptyMessageDelayed(1, 1000);
        } else if (IPageNotifyFn.RESET_PWD_BY_EMAIL == requestType) {
            ResetPwdByEmailRetBean retBean = (ResetPwdByEmailRetBean) result;
            if (retBean == null) {
                return;
            }
            if (retBean.code == 20012) {
                Toast.makeText(this, getText(R.string.user_identify_error), Toast.LENGTH_SHORT).show();
                return;
            } else if (retBean.code == 20103) {
                Toast.makeText(this, getText(R.string.email_not_registered), Toast.LENGTH_SHORT).show();
                return;
            } else if (retBean.code == 20011) {
                Toast.makeText(this, getText(R.string.email_invalid), Toast.LENGTH_SHORT).show();
                return;
            } else if (retBean.code == 20010) {
                Toast.makeText(this, retBean.msg, Toast.LENGTH_SHORT).show();
                return;
            }
            loginByEmail();
        }
    }

    private void getVCode(String phone, String code) {
        GetVcodeRequest request = new GetVcodeRequest(1, new IRequestResultListener() {
            @Override
            public void onLoadComplete(int requestType, Object result) {
                CheckVcodeBean bean = (CheckVcodeBean) result;
                if (InternationUserIdentifyActivity.this.isDestroyed()) {
                    return;
                }
                justCloseDialog(justDifferent);
                closeDialogIdentify();
                if (null == bean) {
                    GolukUtils.showToast(mContext, InternationUserIdentifyActivity.this.getResources().getString(R.string.user_getidentify_fail));
                    return;
                }
                int code = bean.code;
                if (code == 0) {
                    getCodeSuccess();
                } else if (code == 20004) {
                    GolukUtils.showToast(mContext, InternationUserIdentifyActivity.this.getResources().getString(R.string.user_no_regist));
                } else if (code == 20103) {
                    GolukUtils.showToast(mContext, InternationUserIdentifyActivity.this.getResources().getString(R.string.user_already_regist));
                } else if (code == 12016) {
                    GolukUtils.showToast(mContext, InternationUserIdentifyActivity.this.getResources().getString(R.string.count_identify_count_six_limit));
                } else {
                    GolukUtils.showToast(mContext, InternationUserIdentifyActivity.this.getResources().getString(R.string.user_getidentify_fail));
                }
            }
        });
        request.get(phone, code, justDifferent ? GetVcodeRequest.GET_NEW_CODE : GetVcodeRequest.GET_RESET_CODE);
    }

}
