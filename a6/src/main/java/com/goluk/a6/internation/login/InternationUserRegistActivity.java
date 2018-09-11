package com.goluk.a6.internation.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goluk.a6.common.event.EventLoginSuccess;
import com.goluk.a6.common.util.H5Util;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.BuildConfig;
import com.goluk.a6.control.R;
import com.goluk.a6.control.WebviewActivity;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.internation.CustomLoadingDialog;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.UserUtils;
import com.goluk.a6.internation.bean.CheckVcodeBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 注册
 * <p>
 * 1、注册手机号密码 2、获取验证码 3、登陆
 *
 * @author mobnote
 */
public class InternationUserRegistActivity extends BaseActivity implements OnClickListener, OnTouchListener {

    /**
     * 选择国家界面
     */
    public static final int REG_REQUESTCODE_SELECTCTROY = 100;
    /**
     * 免责申明条款
     **/
    public static final String PRIVACY_POLICY_WEB_URL = "http://www.goluk.cn/legal.html";
    /**
     * 手机号、密码、注册按钮
     **/
    private EditText mEditTextPhone;
    private EditText mEmailEt;
    private TextView mPhoneTab;
    private TextView mEmailTab;
    private View mPhoneTabIndicator;
    private View mEmailTabIndicator;
    private LinearLayout mPhoneLL;

    private boolean mIsPhoneSelected;
    private boolean mIsPhoneEmpty;
    private boolean mIsEmailEmpty;

    private Button mBtnRegist;
    private Context mContext = null;
    /**
     * 注册
     **/
    private CustomLoadingDialog mCustomProgressDialog = null;
    /**
     * 获取验证码
     **/
    private CustomLoadingDialog mCustomProgressDialogIdentify = null;
    /**
     * 记录注册成功的状态
     **/
    private SharedPreferences mSharedPreferences = null;
    private Editor mEditor = null;
    /**
     * 注册成功跳转页面的判断标志
     */
    private String registOk = null;
    private TextView zoneTv = null;

    private ImageView mCloseBtn;

    private TextView mLoginBtn;
    private View mViewDiv;

    private TextView mRegistPrivacy;
    private TextView mRegistPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.internation_user_regist);
        initData();
        initView();
        EventBus.getDefault().register(this);
        UserUtils.addActivity(InternationUserRegistActivity.this);
    }

    private void initData() {
        mContext = this;
        mIsPhoneEmpty = true;
        mIsEmailEmpty = true;
        mIsPhoneSelected = true;
        mIsPhoneSelected = getIntent().getBooleanExtra("isPhoneSelected", false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApp.setContext(mContext, "UserRegist");
        getInfo();
    }

    public void initView() {
        // 手机号、密码、注册按钮
        mEditTextPhone = (EditText) findViewById(R.id.user_regist_phonenumber);
        mEmailEt = (EditText) findViewById(R.id.et_email);
        mPhoneTab = (TextView) findViewById(R.id.tab_phone);
        mEmailTab = (TextView) findViewById(R.id.tab_email);
        mPhoneTabIndicator = findViewById(R.id.tab_phone_indicator);
        mEmailTabIndicator = findViewById(R.id.tab_email_indicator);
        mPhoneLL = (LinearLayout) findViewById(R.id.ll_phone);

        mBtnRegist = (Button) findViewById(R.id.user_regist_btn);
        zoneTv = (TextView) findViewById(R.id.user_regist_zone);
        mCloseBtn = (ImageView) findViewById(R.id.close_btn);
        mLoginBtn = (TextView) findViewById(R.id.login_user_btn);
        mViewDiv = findViewById(R.id.user_div);
        mRegistPrivacy = (TextView) findViewById(R.id.regist_privacy);
        mRegistPolicy = (TextView) findViewById(R.id.regist_policy);

        String savedZoneData = GolukUtils.getSavedCountryZone();
        if (!TextUtils.isEmpty(savedZoneData)) {
            zoneTv.setText(savedZoneData);
        } else {
            zoneTv.setText(GolukUtils.getDefaultZone());
        }
        if(BuildConfig.BRANCH_CHINA){
            zoneTv.setVisibility(View.GONE);
            mViewDiv.setVisibility(View.GONE);
        }
        if (mIsPhoneSelected) {
            phoneTabSelected();
        } else {
            emailTabSelected();
        }
        // 监听绑定
        mBtnRegist.setOnClickListener(this);
        zoneTv.setOnClickListener(this);
        mLoginBtn.setOnClickListener(this);
        mCloseBtn.setOnClickListener(this);
        mRegistPrivacy.setOnClickListener(this);
        mRegistPolicy.setOnClickListener(this);
        mPhoneTab.setOnClickListener(this);
        mEmailTab.setOnClickListener(this);

        if (null == mCustomProgressDialog) {
            mCustomProgressDialog = new CustomLoadingDialog(mContext, this.getResources().getString(
                    R.string.str_regist_loading));
        }
        if (null == mCustomProgressDialogIdentify) {
            mCustomProgressDialogIdentify = new CustomLoadingDialog(mContext, this.getResources().getString(
                    R.string.str_identify_loading));
        }

        mEditTextPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                String phone = mEditTextPhone.getText().toString();
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
//        mEmailEt.setFilters(new InputFilter[]{filter});
    }

    private String blockCharacterSet = "-";
    private InputFilter filter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            if (source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };

    /**
     * 手机号码获取
     */
    private void getInfo() {
        Intent itLoginPhone = getIntent();
        String email = itLoginPhone.getStringExtra("intentLoginEmail");
        String phone = itLoginPhone.getStringExtra("intentLogin");
        if (mIsPhoneSelected && !TextUtils.isEmpty(phone)) {
            mEditTextPhone.setText(phone);
            mEditTextPhone.setSelection(mEditTextPhone.getText().toString().length());
        }
        if (!mIsPhoneSelected && !TextUtils.isEmpty(email)) {
            mEmailEt.setText(email);
        }
        Intent itRepassword = getIntent();
        if (null != itRepassword.getStringExtra("intentRepassword")) {
            String repwdNum = itRepassword.getStringExtra("intentRepassword");
            mEditTextPhone.setText(repwdNum);
            mEditTextPhone.setSelection(mEditTextPhone.getText().toString().length());
        }

        /**
         * 判断是从哪个入口进行的注册
         */
        Intent itRegist = getIntent();
        if (null != itRegist.getStringExtra("fromRegist")) {
            registOk = itRegist.getStringExtra("fromRegist");
        }
        getPhone();
    }

    private void resetRegisterBtnState() {
        if (mIsPhoneSelected && !mIsPhoneEmpty) {
            mBtnRegist.setTextColor(Color.parseColor("#FFFFFF"));
            mBtnRegist.setEnabled(true);
        } else if (!mIsPhoneSelected && !mIsEmailEmpty) {
            mBtnRegist.setTextColor(Color.parseColor("#FFFFFF"));
            mBtnRegist.setEnabled(true);
        } else {
            mBtnRegist.setTextColor(Color.parseColor("#7fffffff"));
            mBtnRegist.setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) { // 数据发送成功
            if (REG_REQUESTCODE_SELECTCTROY == requestCode) {
                CountryBean bean = (CountryBean) data.getSerializableExtra(InternationUserLoginActivity.COUNTRY_BEAN);
                zoneTv.setText(bean.area + " +" + bean.code);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back_btn) {
            finish();
        } else if (view.getId() == R.id.user_regist_zone) {
            Intent intent = new Intent(this, UserSelectCountryActivity.class);
            startActivityForResult(intent, REG_REQUESTCODE_SELECTCTROY);
        } else if (view.getId() == R.id.user_regist_btn) {
            register();
        } else if (view.getId() == R.id.login_user_btn || view.getId() == R.id.close_btn) {
            this.finish();
        } else if (view.getId() == R.id.regist_policy) {
            Intent privacy = new Intent(this, WebviewActivity.class);
            privacy.putExtra(WebviewActivity.KEY_WEBVIEW_TITLE, this.getString(R.string.privacy));
            privacy.putExtra(WebviewActivity.KEY_WEBVIEW_URL, H5Util.getPrivacy());
            mContext.startActivity(privacy);
        } else if (view.getId() == R.id.regist_privacy) {
            Intent privacy = new Intent(this, WebviewActivity.class);
            privacy.putExtra(WebviewActivity.KEY_WEBVIEW_TITLE, this.getString(R.string.user_aggre));
            privacy.putExtra(WebviewActivity.KEY_WEBVIEW_URL, H5Util.getUser());
            mContext.startActivity(privacy);
        } else if (view.getId() == R.id.tab_email) {
            if (!mIsPhoneSelected) {
                return;
            }
            emailTabSelected();
        } else if (view.getId() == R.id.tab_phone) {
            if (mIsPhoneSelected) {
                return;
            }
            phoneTabSelected();
        }
    }

    private void phoneTabSelected() {
        mIsPhoneSelected = true;
        mEmailTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_grey));
        mPhoneTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_white));
        mEmailTab.setTextColor(getResources().getColor(R.color.tab_color_grey));
        mPhoneTab.setTextColor(getResources().getColor(R.color.tab_color_white));
        mPhoneLL.setVisibility(View.VISIBLE);
        mEmailEt.setVisibility(View.GONE);
        resetRegisterBtnState();
    }

    private void emailTabSelected() {
        mIsPhoneSelected = false;
        mEmailTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_white));
        mPhoneTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_grey));
        mEmailTab.setTextColor(getResources().getColor(R.color.tab_color_white));
        mPhoneTab.setTextColor(getResources().getColor(R.color.tab_color_grey));
        mPhoneLL.setVisibility(View.GONE);
        mEmailEt.setVisibility(View.VISIBLE);
        resetRegisterBtnState();
    }

    private void registerByPhone() {
        String zone = zoneTv.getText().toString();
        String phone = mEditTextPhone.getText().toString();
        if (TextUtils.isEmpty(zone)) {
            return;
        }
        if (BuildConfig.BRANCH_CHINA&&!GolukUtils.isMobile(phone)){
            GolukUtils.showToast(mContext,
                     this.getResources()
                            .getString(R.string.user_login_phone_show_error));
            return;
        }

        if (!"".equals(phone)) {
            mBtnRegist.setEnabled(true);
            if (!UserUtils.isNetDeviceAvailable(mContext)) {
                GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
            } else {
                if (!mApp.mTimerManage.flag) {
                    GolukUtils.showToast(this, this.getResources().getString(R.string.user_timer_count_hint));
                } else {
                    mApp.mTimerManage.timerCancel();
                    mApp.mTimerManage.timerCount();
                    // 获取验证码
                    int zoneCode = zone.indexOf("+");
                    String code = zone.substring(zoneCode + 1, zone.length());
                    getVCode(phone, code);
                    UserUtils.hideSoftMethod(this);
                    mCustomProgressDialogIdentify.show();
                    mBtnRegist.setEnabled(false);
                    mEditTextPhone.setEnabled(false);
                }
            }
        } else {
            UserUtils.hideSoftMethod(this);
            UserUtils.showDialog(mContext, this.getResources().getString(R.string.user_login_phone_show_error));
        }
    }

    private void registerByEmail() {
        String emailAddress = mEmailEt.getText().toString();
        if (TextUtils.isEmpty(emailAddress) || !UserUtils.emailValidation(emailAddress)) {
            showToast(R.string.email_invalid);
            return;
        }
        if (!UserUtils.isNetDeviceAvailable(mContext)) {
            GolukUtils.showToast(mContext, this.getResources().getString(R.string.user_net_unavailable));
            return;
        }
        Intent intent = new Intent();
        intent.setClass(this, InternationUserPwdActivity.class);
        intent.putExtra("email", emailAddress);
        intent.putExtra("from", registOk);
        startActivity(intent);
    }

    /**
     * 注册
     */
    public void register() {
        if (mIsPhoneSelected) {
            registerByPhone();
        } else {
            registerByEmail();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventLoginSuccess event) {
        finish();
    }

    // 获取验证码成功
    private void callBack_getCode_Success() {
        closeProgressDialogIdentify();
        GolukUtils.showToast(this, this.getResources().getString(R.string.user_getidentify_success));

        String phone = mEditTextPhone.getText().toString();
        String zone = zoneTv.getText().toString();

        Intent getIdentify = new Intent(InternationUserRegistActivity.this, InternationUserIdentifyActivity.class);
        getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_DIFFERENT, true);
        getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_PHONE, phone);
        getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_INTER_REGIST, registOk);

        getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_REGISTER_CODE, zone);
        startActivity(getIdentify);
    }

    /**
     * 获取手机号
     */
    public void getPhone() {
        if (mApp.loginoutStatus = true) {
            String phone = mEditTextPhone.getText().toString();
            mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
            mEditor = mSharedPreferences.edit();
            mEditor.putString("setupPhone", phone);
            mEditor.putBoolean("noPwd", false);
            mEditor.commit();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();
        if (view.getId() == R.id.user_regist_btn) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mBtnRegist.setBackgroundResource(R.drawable.icon_login_click);
                    break;
                case MotionEvent.ACTION_UP:
                    mBtnRegist.setBackgroundResource(R.drawable.icon_login);
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    /**
     * 关闭注册中获取验证码的对话框
     */
    private void closeProgressDialogIdentify() {
        if (null != mCustomProgressDialogIdentify) {
            mCustomProgressDialogIdentify.close();
            mBtnRegist.setEnabled(true);
            mEditTextPhone.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        closeProgressDialogIdentify();
        if (mCustomProgressDialog != null) {
            mCustomProgressDialog.close();
        }
        mCustomProgressDialogIdentify = null;
        mCustomProgressDialog = null;
    }

    private void getVCode(String phone, String code) {
        GetVcodeRequest request = new GetVcodeRequest(1, new IRequestResultListener() {
            @Override
            public void onLoadComplete(int requestType, Object result) {
                CheckVcodeBean bean = (CheckVcodeBean) result;
                if (InternationUserRegistActivity.this.isDestroyed()) {
                    return;
                }
                closeProgressDialogIdentify();
                if (null == bean) {
                    GolukUtils.showToast(mContext, InternationUserRegistActivity.this.getResources().getString(R.string.user_getidentify_fail));
                    return;
                }
                int code = bean.code;
                if (code == 0) {
                    callBack_getCode_Success();
                } else if (code == 20004) {//错误
                    GolukUtils.showToast(mContext, InternationUserRegistActivity.this.getResources().getString(R.string.user_no_regist));
                } else if (code == 20103) {//超时
                    GolukUtils.showToast(mContext, InternationUserRegistActivity.this.getResources().getString(R.string.user_already_regist));
                } else if (code == 12016) {//超出限制
                    GolukUtils.showToast(mContext, InternationUserRegistActivity.this.getResources().getString(R.string.count_identify_count_six_limit));
                } else {
                    GolukUtils.showToast(mContext, InternationUserRegistActivity.this.getResources().getString(R.string.user_getidentify_fail));
                }
            }
        });
        request.get(phone, code, GetVcodeRequest.GET_NEW_CODE);
    }

}
