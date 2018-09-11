package com.goluk.a6.internation.login;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.a6.common.WXConstants;
import com.goluk.a6.common.event.EventLoginSuccess;
import com.goluk.a6.common.event.UpdateBindListEvent;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.BuildConfig;
import com.goluk.a6.control.R;
import com.goluk.a6.internation.CustomLoadingDialog;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.UserLoginInterface;
import com.goluk.a6.internation.UserUtils;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import likly.dollar.$;

/**
 * 登陆模块
 * <p>
 * 1、手机号码、密码的输入 2、手机号码快速注册 3、忘记密码（重置密码） 4、第三方登陆
 *
 * @author mobnote
 */
public class InternationUserLoginActivity extends BaseActivity implements OnClickListener, UserLoginInterface {

    private static final String TAG = "InternationalLogin";

    /**
     * 手机号和密码
     **/
    private EditText mEditTextPhoneNumber, mEditTextPwd;
    private Button mBtnLogin;
    /**
     * 快速注册
     **/
    private TextView mTextViewRegist, mTextViewForgetPwd;
    /**
     * application
     **/
    /**
     * context
     **/
    private String mPhone = null;
    private String mEmail = null;
    private String mPwd = null;
    /**
     * 将用户的手机号和密码保存到本地
     **/
    private SharedPreferences mSharedPreferences = null;
    private Editor mEditor = null;

    /**
     * 判断登录
     **/
    private String justLogin = "";
    private CustomLoadingDialog mCustomProgressDialog = null;

    private boolean flag = false;

    private UMShareAPI mShareAPI = null;
    private TextView mSelectCountryText, mTvDiv = null;
    public static final int REQUEST_SELECT_COUNTRY_CODE = 1000;
    public static final String COUNTRY_BEAN = "countrybean";

    public TextView mLoginByFacebookTV;
    public ImageView mLoginByFacebookIv;
    private ImageView mCloseBtn;

    private EditText mEmailEt;
    private TextView mPhoneTab;
    private TextView mEmailTab;
    private View mPhoneTabIndicator;
    private View mEmailTabIndicator;
    private LinearLayout mPhoneLL;

    private boolean mIsPhoneSelected;
    private boolean mIsPhoneEmpty;
    private boolean mIsEmailEmpty;
    private boolean mIsPwdEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.internation_user_login);
        initView();
        adapterLoginUI();
        initData();
        UserUtils.addActivity(InternationUserLoginActivity.this);
        EventBus.getDefault().register(this);
    }

    private void adapterLoginUI() {
        if (BuildConfig.BRANCH_CHINA) {
            PlatformConfig.setWeixin(WXConstants.APP_ID, WXConstants.APP_SECRET_KEY);
            mLoginByFacebookIv.setImageResource(R.drawable.wexin);
            mLoginByFacebookTV.setText(R.string.str_weixin_login);
            if (BuildConfig.DEBUG) {
                Config.DEBUG = true;
            }
            mTvDiv.setVisibility(View.GONE);
            mSelectCountryText.setVisibility(View.GONE);
        }
    }

    private void initData() {
        mShareAPI = UMShareAPI.get(this);
        // 获得GolukApplication对象
        mIsPhoneEmpty = true;
        mIsEmailEmpty = true;
        mIsPwdEmpty = true;
        mIsPhoneSelected = true;

        // 填写之前登录的用户名
        String phone = $.config().getString("phone");
        if (!TextUtils.isEmpty(phone)) {
            mEditTextPhoneNumber.setText(phone);
            mEditTextPhoneNumber.setSelection(phone.length());
        }
        String email = $.config().getString("email");
        if (!TextUtils.isEmpty(email)) {
            mEmailEt.setText(email);
            mEmailEt.setSelection(email.length());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mShareAPI == null) mShareAPI = UMShareAPI.get(this);
        mShareAPI.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SELECT_COUNTRY_CODE:
                if (RESULT_OK != resultCode) {
                    return;
                }
                if (null != data) {
                    CountryBean bean = (CountryBean) data.getSerializableExtra(COUNTRY_BEAN);
                    mSelectCountryText.setText(bean.area + " +" + bean.code);
                }
                break;

            default:
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventLoginSuccess event) {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mApp.setContext(this, "UserLogin");

        getInfo();
    }

    /**
     * auth callback interface
     **/
    private UMAuthListener umAuthListener = new UMAuthListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {

        }

        @Override
        public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
            if (action == 0) {
                if (BuildConfig.BRANCH_CHINA) {
                    mShareAPI.getPlatformInfo(InternationUserLoginActivity.this, SHARE_MEDIA.WEIXIN, umAuthListener);
                } else {
                    mShareAPI.getPlatformInfo(InternationUserLoginActivity.this, SHARE_MEDIA.FACEBOOK, umAuthListener);
                }
            } else if (action == 2) {
                HashMap<String, String> result = new HashMap<String, String>();
                HashMap<String, String> userinfo = new HashMap<String, String>();
                userinfo.put("openid", data.get("uid"));
                userinfo.put("opentoken", data.get("accessToken"));
                if (BuildConfig.BRANCH_CHINA) {
                    userinfo.put("sex", data.get("gender"));
                }
                userinfo.put("avatar", data.get("iconurl"));
                String name = data.get("name");
                userinfo.put("name", name);
                result.put("userinfo", new JSONObject(userinfo).toString());
                mApp.mLoginManage.setUserLoginInterface(InternationUserLoginActivity.this);
                mApp.mLoginManage.loginBy3rdPlatform(userinfo);
                //Toast.makeText(InternationUserLoginActivity.this, R.string.authorize_suss, Toast.LENGTH_SHORT).show();
                if (mActivityDestroyed) {
                    return;
                }
                mCustomProgressDialog.show();
            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, int action, Throwable t) {
            Toast.makeText(InternationUserLoginActivity.this, R.string.authorize_failed, Toast.LENGTH_SHORT).show();
            Log.e("", t.getStackTrace().toString());
            Log.e("", t.getMessage());
        }

        @Override
        public void onCancel(SHARE_MEDIA platform, int action) {
            Toast.makeText(InternationUserLoginActivity.this, R.string.autiorize_cancel, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!flag) {
            mSharedPreferences = getSharedPreferences("setup", Context.MODE_PRIVATE);
            if (null != mEditTextPhoneNumber.getText().toString()
                    && mEditTextPhoneNumber.getText().toString().length() == 11) {
                String phone = mEditTextPhoneNumber.getText().toString();
                mEditor = mSharedPreferences.edit();
                mEditor.putString("setupPhone", phone);
                mEditor.putBoolean("noPwd", false);
                // 提交
                mEditor.commit();
            }
        }
    }

    public void initView() {
        // 手机号和密码、登录按钮
        mEditTextPhoneNumber = (EditText) findViewById(R.id.user_login_phonenumber);
        mEditTextPwd = (EditText) findViewById(R.id.user_login_pwd);
        mBtnLogin = (Button) findViewById(R.id.user_login_layout_btn);
        // 快速注册
        mTextViewRegist = (TextView) findViewById(R.id.insert_user_btn);
        mTextViewForgetPwd = (TextView) findViewById(R.id.user_login_forgetpwd);
        // select country
        mSelectCountryText = (TextView) findViewById(R.id.tv_user_login_select_country);
        mTvDiv = (TextView) findViewById(R.id.tv_user_login_div);
        mLoginByFacebookTV = (TextView) findViewById(R.id.login_facebook_btn_txt);
        mLoginByFacebookIv = (ImageView) findViewById(R.id.login_facebook_btn_img);
        mCloseBtn = (ImageView) findViewById(R.id.close_btn);

        mEmailEt = (EditText) findViewById(R.id.et_email);
        mPhoneTab = (TextView) findViewById(R.id.tab_phone);
        mEmailTab = (TextView) findViewById(R.id.tab_email);
        mPhoneTabIndicator = findViewById(R.id.tab_phone_indicator);
        mEmailTabIndicator = findViewById(R.id.tab_email_indicator);
        mPhoneLL = (LinearLayout) findViewById(R.id.ll_login_by_phone);

        mCloseBtn.setOnClickListener(this);
        mSelectCountryText.setOnClickListener(this);

        String savedZoneData = GolukUtils.getSavedCountryZone();
        if (!TextUtils.isEmpty(savedZoneData)) {
            mSelectCountryText.setText(savedZoneData);
        } else {
            mSelectCountryText.setText(GolukUtils.getDefaultZone());
        }
        TextView text = (TextView) findViewById(R.id.user_login_phoneRegist);
        text.setText(this.getString(R.string.user_login_phone));

        // 登录按钮
        mBtnLogin.setOnClickListener(this);
        mLoginByFacebookTV.setOnClickListener(this);
        mLoginByFacebookIv.setOnClickListener(this);
        // 快速注册
        mTextViewRegist.setOnClickListener(this);
        mTextViewForgetPwd.setOnClickListener(this);

        mPhoneTab.setOnClickListener(this);
        mEmailTab.setOnClickListener(this);

        if (null == mCustomProgressDialog) {
            mCustomProgressDialog = new CustomLoadingDialog(InternationUserLoginActivity.this, this.getResources().getString(
                    R.string.str_loginning));
        }

        if (null != mApp && null != mApp.mLoginManage) {
            mApp.mLoginManage.initData();
        }

        mEditTextPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                String phone = mEditTextPhoneNumber.getText().toString();
                if (!TextUtils.isEmpty(phone)) {
                    mIsPhoneEmpty = false;
                } else {
                    mIsPhoneEmpty = true;
                }
                resetRegisterBtnState();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        mEmailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                String email = mEmailEt.getText().toString();
                if (!TextUtils.isEmpty(email)) {
                    mIsEmailEmpty = false;
                } else {
                    mIsEmailEmpty = true;
                }
                resetRegisterBtnState();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        // 密码监听
        mEditTextPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                mPwd = mEditTextPwd.getText().toString();
                if (TextUtils.isEmpty(mPwd)) {
                    mIsPwdEmpty = true;
                } else {
                    mIsPwdEmpty = false;
                }
                resetRegisterBtnState();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {

            }
        });
    }

    private void resetRegisterBtnState() {
        if (mIsPwdEmpty) {
            mBtnLogin.setTextColor(Color.parseColor("#7fffffff"));
            mBtnLogin.setEnabled(false);
            return;
        }
        if (mIsPhoneSelected && !mIsPhoneEmpty) {
            mBtnLogin.setTextColor(Color.parseColor("#FFFFFF"));
            mBtnLogin.setEnabled(true);
        } else if (!mIsPhoneSelected && !mIsEmailEmpty) {
            mBtnLogin.setTextColor(Color.parseColor("#FFFFFF"));
            mBtnLogin.setEnabled(true);
        } else {
            mBtnLogin.setTextColor(Color.parseColor("#7fffffff"));
            mBtnLogin.setEnabled(false);
        }
    }

    public void getInfo() {
        Intent intentStart = getIntent();
        // 登录页面返回
        if (null != intentStart.getStringExtra("isInfo")) {
            justLogin = intentStart.getStringExtra("isInfo").toString();
        }

        /**
         * 填写手机号
         */
//        mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
//        if (!"".equals(mSharedPreferences.getString("setupPhone", ""))) {
//            String phone = mSharedPreferences.getString("setupPhone", "");
//            mEditTextPhoneNumber.setText(phone.replace("-", ""));
//            mEditTextPhoneNumber.setSelection(mEditTextPhoneNumber.getText().toString().length());
//        }

//        boolean b = mSharedPreferences.getBoolean("noPwd", false);
//        if (b) {
//            mEditTextPwd.setText("");
//        }

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back_btn) {
            mApp.mLoginManage.setUserLoginInterface(null);
            UserUtils.hideSoftMethod(this);
            setResult(Activity.RESULT_CANCELED);
            this.finish();
        } else if (view.getId() == R.id.user_login_layout_btn) {
            loginManage();
        } else if (view.getId() == R.id.tab_email) {
            if (!mIsPhoneSelected) {
                return;
            }
            mIsPhoneSelected = false;
            mEmailTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_white));
            mPhoneTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_grey));
            mEmailTab.setTextColor(getResources().getColor(R.color.tab_color_white));
            mPhoneTab.setTextColor(getResources().getColor(R.color.tab_color_grey));
            mPhoneLL.setVisibility(View.GONE);
            mEmailEt.setVisibility(View.VISIBLE);
            mEmailEt.requestFocus();
            resetRegisterBtnState();
        } else if (view.getId() == R.id.tab_phone) {
            if (mIsPhoneSelected) {
                return;
            }
            mIsPhoneSelected = true;
            mEmailTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_grey));
            mPhoneTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_white));
            mEmailTab.setTextColor(getResources().getColor(R.color.tab_color_grey));
            mPhoneTab.setTextColor(getResources().getColor(R.color.tab_color_white));
            mPhoneLL.setVisibility(View.VISIBLE);
            mEmailEt.setVisibility(View.GONE);
            resetRegisterBtnState();
        } else if (view.getId() == R.id.insert_user_btn) {
            mApp.mLoginManage.setUserLoginInterface(null);
            UserUtils.hideSoftMethod(this);
            Intent itRegist = new Intent(InternationUserLoginActivity.this, InternationUserRegistActivity.class);
            if (justLogin.equals("main") || justLogin.equals("back")) {// 从起始页注册
                itRegist.putExtra("fromRegist", "fromStart");
            } else if (justLogin.equals("indexmore")) {// 从更多页个人中心注册
                itRegist.putExtra("fromRegist", "fromIndexMore");
            } else if (justLogin.equals("setup")) {// 从设置页注册
                itRegist.putExtra("fromRegist", "fromSetup");
            } else if (justLogin.equals("profit")) {
                itRegist.putExtra("fromRegist", "fromProfit");
            }
            itRegist.putExtra("isPhoneSelected", mIsPhoneSelected);
            startActivity(itRegist);
        } else if (view.getId() == R.id.user_login_forgetpwd) {
            mApp.mLoginManage.setUserLoginInterface(null);
            UserUtils.hideSoftMethod(this);
            Intent itForget = new Intent(InternationUserLoginActivity.this, InternationalResetPwdActivity.class);
            if (justLogin.equals("main") || justLogin.equals("back")) {// 从起始页注册
                itForget.putExtra("fromRegist", "fromStart");
            } else if (justLogin.equals("indexmore")) {// 从更多页个人中心注册
                itForget.putExtra("fromRegist", "fromIndexMore");
            } else if (justLogin.equals("setup")) {// 从设置页注册
                itForget.putExtra("fromRegist", "fromSetup");
            } else if (justLogin.equals("profit")) {
                itForget.putExtra("fromRegist", "fromProfit");
            }
            itForget.putExtra("isPhoneSelected", mIsPhoneSelected);
            startActivity(itForget);
        } else if (view.getId() == R.id.tv_user_login_select_country) {
            mApp.mLoginManage.setUserLoginInterface(null);
            UserUtils.hideSoftMethod(this);
            Intent itSelectCountry = new Intent(this, UserSelectCountryActivity.class);
            startActivityForResult(itSelectCountry, REQUEST_SELECT_COUNTRY_CODE);
        } else if (view.getId() == R.id.login_facebook_btn_img || view.getId() == R.id.login_facebook_btn_txt) {
            if (GolukUtils.isNetworkConnected(this)) {
                if (BuildConfig.BRANCH_CHINA) {
                    if (mShareAPI.isInstall(this, SHARE_MEDIA.WEIXIN)) {
                        mShareAPI.doOauthVerify(this, SHARE_MEDIA.WEIXIN, umAuthListener);
                    } else {
                        GolukUtils.showToast(this, getResources().getString(R.string.str_weixin_no_install));
                    }
                } else {
                    if (mShareAPI.isInstall(this, SHARE_MEDIA.FACEBOOK)) {
                        mShareAPI.doOauthVerify(this, SHARE_MEDIA.FACEBOOK, umAuthListener);
                    } else {
                        GolukUtils.showToast(this, getResources().getString(R.string.str_facebook_no_install));
                    }
                }
            } else {
                GolukUtils.showToast(this, getResources().getString(R.string.user_net_unavailable));
            }
        } else if (view.getId() == R.id.close_btn) {
            this.finish();
        }
    }

    /**
     * 登录管理类
     */
    public void loginManage() {
        if (mIsPhoneSelected) {
            loginByPhone();
        } else {
            loginByEmail();
        }
    }

    private void loginByEmail() {
        mEmail = mEmailEt.getText().toString();
        mPwd = mEditTextPwd.getText().toString();
        if (TextUtils.isEmpty(mEmail) || !UserUtils.emailValidation(mEmail)) {
            showToast(R.string.email_invalid);
            return;
        }
        if (TextUtils.isEmpty(mPwd) || mPwd.length() < 6 || mPwd.length() > 16) {
            UserUtils.hideSoftMethod(this);
            GolukUtils.showToast(this,
                    this.getResources().getString(R.string.user_login_password_show_error));
            return;
        }

        if (!UserUtils.isNetDeviceAvailable(this)) {
            UserUtils.hideSoftMethod(this);
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
        } else {
            mApp.mLoginManage.setUserLoginInterface(this);
            mApp.mLoginManage.loginByEmail(mEmail, mPwd, "");
            mApp.loginStatus = 0;
            UserUtils.hideSoftMethod(this);
            mCustomProgressDialog.show();
            mEditTextPhoneNumber.setEnabled(false);
            mEditTextPwd.setEnabled(false);
            mTextViewRegist.setEnabled(false);
            mTextViewForgetPwd.setEnabled(false);
            mBtnLogin.setEnabled(false);
        }
    }

    private void loginByPhone() {
        mPhone = mEditTextPhoneNumber.getText().toString().replace("-", "");
        mPwd = mEditTextPwd.getText().toString();
        String zone = mSelectCountryText.getText().toString();
        int zoneCode = zone.indexOf("+");
        String code = zone.substring(zoneCode + 1, zone.length());
        if (!"".equals(mPhone)) {
            if (!"".equals(mPwd)) {
                if (mPwd.length() >= 6 && mPwd.length() <= 16) {
                    if (!UserUtils.isNetDeviceAvailable(this)) {
                        UserUtils.hideSoftMethod(this);
                        GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
                    } else {
                        mApp.mLoginManage.setUserLoginInterface(this);
                        mApp.mLoginManage.loginByPhone(mPhone, code, mPwd, "");
                        mApp.loginStatus = 0;
                        UserUtils.hideSoftMethod(this);
                        mCustomProgressDialog.show();
                        mEditTextPhoneNumber.setEnabled(false);
                        mEditTextPwd.setEnabled(false);
                        mTextViewRegist.setEnabled(false);
                        mTextViewForgetPwd.setEnabled(false);
                        mBtnLogin.setEnabled(false);
                    }
                } else {
                    UserUtils.hideSoftMethod(this);
                    GolukUtils.showToast(this,
                            this.getResources().getString(R.string.user_login_password_show_error));
                }
            }
        }
    }

    /**
     * 登录管理类回调返回的状态 0登录中 1登录成功 2登录失败 3用户未注册 4登录超时
     */
    @Override
    public void loginCallbackStatus() {
        switch (mApp.loginStatus) {
            case 0:
                break;
            case 1:
                // 登录成功后关闭个人中心启动模块页面
                mApp.isUserLoginSucess = true;
                closeProgressDialog();
                mEditTextPhoneNumber.setEnabled(true);
                mEditTextPwd.setEnabled(true);
                mTextViewRegist.setEnabled(true);
                mTextViewForgetPwd.setEnabled(true);
                mBtnLogin.setEnabled(true);
                Intent it = new Intent();
                if ("profit".equals(justLogin)) {
                    startActivity(it);
                }
                EventBus.getDefault().post(new EventLoginSuccess());
                EventBus.getDefault().post(new UpdateBindListEvent());
                //用户登录成功后应获取默认设备，否则默认设备为Splash中获取的那个，
                mApp.getImei();
                this.finish();
                break;
            case 2:
                mApp.isUserLoginSucess = false;
                closeProgressDialog();
                mEditTextPhoneNumber.setEnabled(true);
                mEditTextPwd.setEnabled(true);
                mTextViewRegist.setEnabled(true);
                mTextViewForgetPwd.setEnabled(true);
                mBtnLogin.setEnabled(true);
                break;
            case 3:
                mApp.isUserLoginSucess = false;
                closeProgressDialog();
                mEditTextPhoneNumber.setEnabled(true);
                mEditTextPwd.setEnabled(true);
                mTextViewRegist.setEnabled(true);
                mTextViewForgetPwd.setEnabled(true);
                mBtnLogin.setEnabled(true);
                String notRegistered = "";
                if (mIsPhoneSelected) {
                    notRegistered = this.getResources().getString(R.string.user_no_regist);
                } else {
                    notRegistered = this.getResources().getString(R.string.email_not_registered);
                }
                new AlertDialog.Builder(this)
                        .setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
                        .setMessage(notRegistered)
                        .setNegativeButton(this.getResources().getString(R.string.user_cancle), null)
                        .setPositiveButton(this.getResources().getString(R.string.user_regist),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        mApp.mLoginManage.setUserLoginInterface(null);
                                        Intent it = new Intent(InternationUserLoginActivity.this, InternationUserRegistActivity.class);
                                        if (mIsPhoneSelected) {
                                            it.putExtra("intentLogin", mEditTextPhoneNumber.getText().toString());
                                        } else {
                                            it.putExtra("intentLoginEmail", mEmailEt.getText().toString());
                                        }
                                        it.putExtra("isPhoneSelected", mIsPhoneSelected);
                                        if (justLogin.equals("main") || justLogin.equals("back")) {// 从起始页注册
                                            it.putExtra("fromRegist", "fromStart");
                                        } else if (justLogin.equals("indexmore")) {// 从更多页个人中心注册
                                            it.putExtra("fromRegist", "fromIndexMore");
                                        } else if (justLogin.equals("setup")) {// 从设置页注册
                                            it.putExtra("fromRegist", "fromSetup");
                                        } else if (justLogin.equals("profit")) {// 从我的收益注册
                                            it.putExtra("fromRegist", "fromProfit");
                                        }

                                        startActivity(it);
                                    }
                                }).create().show();
                break;
            case 4:
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_netword_outtime));
                mApp.isUserLoginSucess = false;
                closeProgressDialog();
                mEditTextPhoneNumber.setEnabled(true);
                mEditTextPwd.setEnabled(true);
                mTextViewRegist.setEnabled(true);
                mTextViewForgetPwd.setEnabled(true);
                mBtnLogin.setEnabled(true);
                break;
            case 5:
                mApp.isUserLoginSucess = false;
                closeProgressDialog();
                mEditTextPhoneNumber.setEnabled(true);
                mEditTextPwd.setEnabled(true);
                mTextViewRegist.setEnabled(true);
                mTextViewForgetPwd.setEnabled(true);
                mBtnLogin.setEnabled(true);
                new AlertDialog.Builder(this)
                        .setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
                        .setMessage(this.getResources().getString(R.string.user_login_password_limit_top_hint))
                        .setPositiveButton(this.getResources().getString(R.string.user_repwd_ok),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        mApp.mLoginManage.setUserLoginInterface(null);
                                        Intent it = new Intent(InternationUserLoginActivity.this, InternationalResetPwdActivity.class);
                                        it.putExtra("errorPwdOver", mEditTextPhoneNumber.getText().toString());
                                        if (justLogin.equals("main") || justLogin.equals("back")) {// 从起始页注册
                                            it.putExtra("fromRegist", "fromStart");
                                        } else if (justLogin.equals("indexmore")) {// 从更多页个人中心注册
                                            it.putExtra("fromRegist", "fromIndexMore");
                                        } else if (justLogin.equals("setup")) {// 从设置页注册
                                            it.putExtra("fromRegist", "fromSetup");
                                        } else if (justLogin.equals("profit")) {// 从我的收益注册
                                            it.putExtra("fromRegist", "fromProfit");
                                        }
                                        startActivity(it);
                                    }
                                }).create().show();
                break;
            // 密码错误
            case 6:
                closeProgressDialog();
                mEditTextPwd.setText("");
                break;
            default:

                break;
        }
    }

    /**
     * 关闭加载中对话框
     */
    private void closeProgressDialog() {
        if (null != mCustomProgressDialog) {
            mCustomProgressDialog.close();
            mEditTextPhoneNumber.setEnabled(true);
            mEditTextPwd.setEnabled(true);
            mTextViewRegist.setEnabled(true);
            mTextViewForgetPwd.setEnabled(true);
            mBtnLogin.setEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        boolean isCurrentRunningForeground = isRunningForeground();
        flag = isCurrentRunningForeground;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (mCustomProgressDialog != null) {
            if (mCustomProgressDialog.isShowing()) {
                mCustomProgressDialog.close();
                mCustomProgressDialog = null;
            }
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public boolean isRunningForeground() {
        String packageName = getPackageName(this);
        String topActivityClassName = getTopActivityName(this);
        if (packageName != null && topActivityClassName != null && topActivityClassName.startsWith(packageName)) {
            return true;
        } else {
            return false;
        }
    }

    public String getTopActivityName(Context context) {
        String topActivityClassName = null;
        ActivityManager activityManager = (ActivityManager) (context
                .getSystemService(Context.ACTIVITY_SERVICE));
        // 即最多取得的运行中的任务信息(RunningTaskInfo)数量
        List<RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        if (runningTaskInfos != null && runningTaskInfos.size() > 0) {
            ComponentName f = runningTaskInfos.get(0).topActivity;
            topActivityClassName = f.getClassName();

        }
        // 按下Home键盘后 topActivityClassName
        return topActivityClassName;
    }

    public String getPackageName(Context context) {
        String packageName = context.getPackageName();
        return packageName;
    }

}
