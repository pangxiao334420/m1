package com.goluk.a6.internation.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.a6.common.event.EventLoginSuccess;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.BuildConfig;
import com.goluk.a6.control.R;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.internation.CustomLoadingDialog;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.IPageNotifyFn;
import com.goluk.a6.internation.UserUtils;
import com.goluk.a6.internation.bean.CheckVcodeBean;
import com.goluk.a6.internation.bean.EmailVcodeRetBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * 重置密码
 * <p>
 * 1、输入手机号、密码 2、验证码的获取和判断 3、短信验证
 *
 * @author mobnote
 */
public class InternationalResetPwdActivity extends BaseActivity implements OnClickListener, IRequestResultListener {

    /**
     * 找回密码
     */
    public static final int FIND_REQUESTCODE_SELECTCTROY = 20;
    /**
     * title
     **/
    private ImageButton mBtnBack;
    private TextView mTextViewTitle;
    /**
     * 手机号、密码、验证码
     **/
    private EditText mEditTextPhone;
    private Button mBtnOK;

    private Context mContext = null;
    /**
     * 重置密码显示进度条
     **/
    private CustomLoadingDialog mCustomProgressDialog = null;
    /**
     * 验证码获取显示进度条
     **/
    private CustomLoadingDialog mCustomProgressDialogIdentify = null;

    private SharedPreferences mSharedPreferences = null;
    private Editor mEditor = null;
    /**
     * 重置密码跳转标志
     **/
    private String repwdOk = null;
    private TextView zoneTv = null;

    private EditText mEmailEt;
    private TextView mPhoneTab;
    private TextView mEmailTab;
    private View mPhoneTabIndicator;
    private View mEmailTabIndicator;
    private LinearLayout mPhoneLL;

    private boolean mIsPhoneSelected;
    private boolean mIsPhoneEmpty;
    private boolean mIsEmailEmpty;

    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.internation_user_repwd);

        initData();
        initView();

        mTextViewTitle.setText(this.getResources().getString(R.string.user_login_forgetpwd));
        UserUtils.addActivity(InternationalResetPwdActivity.this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initData() {
        mContext = this;
        mIsPhoneEmpty = true;
        mIsEmailEmpty = true;
        mIsPhoneSelected = getIntent().getBooleanExtra("isPhoneSelected", false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getInfo();
    }

    public void initView() {

        if (null == mCustomProgressDialog) {
            mCustomProgressDialog = new CustomLoadingDialog(mContext, this.getResources().getString(
                    R.string.str_repwd_loading));
        }
        if (null == mCustomProgressDialogIdentify) {
            mCustomProgressDialogIdentify = new CustomLoadingDialog(mContext, this.getResources().getString(
                    R.string.str_identify_loading));
        }

        mBtnBack = (ImageButton) findViewById(R.id.back_btn);
        mTextViewTitle = (TextView) findViewById(R.id.user_title_text);
        mEditTextPhone = (EditText) findViewById(R.id.user_repwd_phonenumber);
        mBtnOK = (Button) findViewById(R.id.user_repwd_ok_btn);
        zoneTv = (TextView) findViewById(R.id.repwd_zone);
        mEmailEt = (EditText) findViewById(R.id.et_email);
        mPhoneTab = (TextView) findViewById(R.id.tab_phone);
        mEmailTab = (TextView) findViewById(R.id.tab_email);
        mPhoneTabIndicator = findViewById(R.id.tab_phone_indicator);
        mEmailTabIndicator = findViewById(R.id.tab_email_indicator);
        mPhoneLL = (LinearLayout) findViewById(R.id.ll_phone);

        String savedZoneData = GolukUtils.getSavedCountryZone();
        if (!TextUtils.isEmpty(savedZoneData)) {
            zoneTv.setText(savedZoneData);
        } else {
            zoneTv.setText(GolukUtils.getDefaultZone());
        }
        if(BuildConfig.BRANCH_CHINA){
            zoneTv.setVisibility(View.GONE);
        }
        if (mIsPhoneSelected) {
            phoneTabSelected();
        } else {
            emailTabSelected();
        }

        // 绑定监听
        mBtnBack.setOnClickListener(this);
        mBtnOK.setOnClickListener(this);
        zoneTv.setOnClickListener(this);
        mPhoneTab.setOnClickListener(this);
        mEmailTab.setOnClickListener(this);

        mEditTextPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                String phone = mEditTextPhone.getText().toString();
                if (!TextUtils.isEmpty(phone)) {
                    mIsPhoneEmpty = false;
                } else {
                    mIsPhoneEmpty = true;
                }
                resetBtnState();
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
                mEmail = mEmailEt.getText().toString();
                if (!TextUtils.isEmpty(mEmail)) {
                    mIsEmailEmpty = false;
                } else {
                    mIsEmailEmpty = true;
                }
                resetBtnState();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
    }

    private void resetBtnState() {
        if (mIsPhoneSelected && !mIsPhoneEmpty) {
            mBtnOK.setTextColor(Color.parseColor("#000000"));
            mBtnOK.setEnabled(true);
        } else if (!mIsPhoneSelected && !mIsEmailEmpty) {
            mBtnOK.setTextColor(Color.parseColor("#000000"));
            mBtnOK.setEnabled(true);
        } else {
            mBtnOK.setTextColor(Color.parseColor("#60000000"));
            mBtnOK.setEnabled(false);
        }
    }

    /**
     * 获取信息
     */
    public void getInfo() {
        /**
         * 登录页密码输入错误超过五次，跳转到重置密码也，并且填入手机号
         */
        Intent it = getIntent();
        if (null != it.getStringExtra("errorPwdOver")) {
            String phone = it.getStringExtra("errorPwdOver");
            mEditTextPhone.setText(phone);
            mBtnOK.setTextColor(Color.parseColor("#000000"));
            mBtnOK.setEnabled(true);
            mEditTextPhone.setSelection(mEditTextPhone.getText().toString().length());
        }

        /**
         * 判断是从哪个入口进行的注册
         */
        Intent itRepwd = getIntent();
        if (null != itRepwd.getStringExtra("fromRegist")) {
            repwdOk = itRepwd.getStringExtra("fromRegist");
        }

        putPhones();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back_btn) {
            finish();
        } else if (view.getId() == R.id.repwd_zone) {
            Intent intent = new Intent(this, UserSelectCountryActivity.class);
            startActivityForResult(intent, FIND_REQUESTCODE_SELECTCTROY);
        } else if (view.getId() == R.id.user_repwd_ok_btn) {
            repwd();
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
        mEmailTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_lighter));
        mPhoneTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_darker));
        mEmailTab.setTextColor(getResources().getColor(R.color.tab_color_lighter));
        mPhoneTab.setTextColor(getResources().getColor(R.color.tab_color_darker));
        mPhoneLL.setVisibility(View.VISIBLE);
        mEmailEt.setVisibility(View.GONE);
        resetBtnState();
    }

    private void emailTabSelected() {
        mIsPhoneSelected = false;
        mEmailTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_darker));
        mPhoneTabIndicator.setBackgroundColor(getResources().getColor(R.color.tab_color_lighter));
        mEmailTab.setTextColor(getResources().getColor(R.color.tab_color_darker));
        mPhoneTab.setTextColor(getResources().getColor(R.color.tab_color_lighter));
        mPhoneLL.setVisibility(View.GONE);
        mEmailEt.setVisibility(View.VISIBLE);
        resetBtnState();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventLoginSuccess event) {
        finish();
    }

    /**
     * 重置密码
     */
    public void repwd() {

        if (!UserUtils.isNetDeviceAvailable(this)) {
            UserUtils.hideSoftMethod(this);
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
            return;
        }

        if (mIsPhoneSelected) {
            requestPhoneVcode();
        } else {
            requestEmailVcode();
        }
    }

    private void requestEmailVcode() {
        mEmail = mEmailEt.getText().toString();
        if (TextUtils.isEmpty(mEmail) || !UserUtils.emailValidation(mEmail)) {
            showToast(R.string.email_invalid);
            return;
        }
        UserUtils.hideSoftMethod(this);
        mCustomProgressDialogIdentify.show();
        mBtnOK.setEnabled(false);
        mEditTextPhone.setEnabled(false);
        mBtnBack.setEnabled(false);

        new EmailVcodeRequest(IPageNotifyFn.SEND_EMAIL_VCODE, this).send(mEmail, "2");
    }

    private void requestPhoneVcode() {
        String phone = mEditTextPhone.getText().toString();
        String zone = zoneTv.getText().toString();
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
            mBtnOK.setFocusable(true);
            if (!mApp.mTimerManage.flag) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_timer_count_hint));
            } else {
                mApp.mTimerManage.timerCancel();
                int zoneCode = zone.indexOf("+");
                String code = zone.substring(zoneCode + 1, zone.length());
                getVCode(phone,code);
                UserUtils.hideSoftMethod(this);
                mCustomProgressDialogIdentify.show();
                mBtnOK.setEnabled(false);
                mEditTextPhone.setEnabled(false);
                mBtnBack.setEnabled(false);

            }
        }
    }

    private void callBack_getCode_Success() {
        closeProgressDialogIdentify();
        GolukUtils.showToast(this, this.getResources().getString(R.string.user_getidentify_success));
        String phone = mEditTextPhone.getText().toString();
        String zone = zoneTv.getText().toString();

        Intent getIdentify = new Intent(InternationalResetPwdActivity.this, InternationUserIdentifyActivity.class);
        getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_DIFFERENT, false);
        getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_PHONE, phone);
        getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_INTER_REGIST, repwdOk);

        getIdentify.putExtra(InternationUserIdentifyActivity.IDENTIFY_REGISTER_CODE, zone);
        startActivity(getIdentify);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) { // 数据发送成功
            if (FIND_REQUESTCODE_SELECTCTROY == requestCode) {
                CountryBean bean = (CountryBean) data.getSerializableExtra(InternationUserLoginActivity.COUNTRY_BEAN);
                zoneTv.setText(bean.area + " +" + bean.code);
            }
        }
    }

    public void putPhones() {
        String phone = mEditTextPhone.getText().toString();
        mSharedPreferences = getSharedPreferences("setup", MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mEditor.putString("setupPhone", phone);
        mEditor.putBoolean("noPwd", false);
        mEditor.commit();
    }

    /**
     * 关闭重置中获取验证码的对话框
     */
    private void closeProgressDialogIdentify() {
        if (null != mCustomProgressDialogIdentify) {
            mCustomProgressDialogIdentify.close();
            mBtnOK.setEnabled(true);
            mEditTextPhone.setEnabled(true);
            mBtnBack.setEnabled(true);
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        closeProgressDialogIdentify();
        if (IPageNotifyFn.SEND_EMAIL_VCODE == requestType) {
            EmailVcodeRetBean retBean = (EmailVcodeRetBean) result;
            if (retBean == null) {
                return;
            }
            if (retBean.code == 20004 || retBean.code == 20001) {
                Toast.makeText(this, getString(R.string.email_not_registered), Toast.LENGTH_SHORT).show();
                return;
            } else if (retBean.code == 25010) {
                Toast.makeText(this, getString(R.string.email_send_failed), Toast.LENGTH_SHORT).show();
                return;
            } else if (retBean.code == 0) {
                Intent intent = new Intent(InternationalResetPwdActivity.this, InternationUserIdentifyActivity.class);
                intent.putExtra(InternationUserIdentifyActivity.KEY_EMAIL_ADDRESS, mEmail);
                intent.putExtra(InternationUserIdentifyActivity.IDENTIFY_INTER_REGIST, repwdOk);
                startActivity(intent);
            }
        }
    }

    private void getVCode(String phone, String code) {
        GetVcodeRequest request = new GetVcodeRequest(1, new IRequestResultListener() {
            @Override
            public void onLoadComplete(int requestType, Object result) {
                closeProgressDialogIdentify();
                CheckVcodeBean bean = (CheckVcodeBean) result;
                if (InternationalResetPwdActivity.this.isDestroyed()) {
                    return;
                }
                if (null == bean) {
                    GolukUtils.showToast(mContext, InternationalResetPwdActivity.this.getResources().getString(R.string.user_getidentify_fail));
                    return;
                }
                int code = bean.code;
                if (code == 0) {
                    callBack_getCode_Success();
                } else if (code == 20004) {
                    GolukUtils.showToast(mContext, InternationalResetPwdActivity.this.getResources().getString(R.string.user_no_regist));
                } else if (code == 20103) {
                    GolukUtils.showToast(mContext, InternationalResetPwdActivity.this.getResources().getString(R.string.user_already_regist));
                } else if (code == 12016) {
                    GolukUtils.showToast(mContext, InternationalResetPwdActivity.this.getResources().getString(R.string.count_identify_count_six_limit));
                } else {
                    GolukUtils.showToast(mContext, InternationalResetPwdActivity.this.getResources().getString(R.string.user_getidentify_fail));
                }
            }
        });
        request.get(phone, code, GetVcodeRequest.GET_RESET_CODE);
    }

}
