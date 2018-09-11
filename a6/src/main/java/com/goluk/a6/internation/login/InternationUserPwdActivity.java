package com.goluk.a6.internation.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.goluk.a6.common.event.EventLoginSuccess;
import com.goluk.a6.common.event.UpdateBindListEvent;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.R;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.internation.CustomLoadingDialog;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.IPageNotifyFn;
import com.goluk.a6.internation.SharedPrefUtil;
import com.goluk.a6.internation.UserLoginInterface;
import com.goluk.a6.internation.UserUtils;
import com.goluk.a6.internation.bean.RegistBean;
import com.goluk.a6.internation.bean.UserData;
import com.goluk.a6.internation.bean.UserResult;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import likly.dollar.$;

/**
 * 国际版注册时设置密码页面
 */
public class InternationUserPwdActivity extends BaseActivity implements View.OnClickListener, UserLoginInterface,
        IRequestResultListener {

    private ImageButton mImageBtnBack = null;
    private EditText mPwdEditText = null;
    private EditText mNameEditText = null;
    private Button mNextBtn = null;
    /**
     * 从上个页面传来的phone
     **/
    private String mPhone = "";
    private String mName = "";
    private String mEmail = "";
    /**
     * 从上个页面传来的vcode
     **/
    private String mVcode = "";
    /**
     * 从上个页面传来的zone
     **/
    private String mZone = "";
    private String mFrom = "";
    /**
     * 2次验证码
     **/
    private String mStep2code = "";
    private CustomLoadingDialog mLoadingDialog = null;
    private SharedPreferences mSharedPreferences = null;
    private SharedPreferences.Editor mEditor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internation_user_pwd);

        getDataInfo();

        initView();

    }

    private void getDataInfo() {
        Intent it = getIntent();
        mPhone = it.getStringExtra("phone");
        mEmail = it.getStringExtra("email");
        mVcode = it.getStringExtra("vcode");
        mZone = it.getStringExtra("zone");
        mFrom = it.getStringExtra("from");
        mStep2code = it.getStringExtra("step2code");
    }

    private void initView() {
        mImageBtnBack = (ImageButton) findViewById(R.id.ib_internation_pwd_back);
        mPwdEditText = (EditText) findViewById(R.id.et_internation_pwd);
        mNameEditText = (EditText) findViewById(R.id.et_internation_name);
        mNextBtn = (Button) findViewById(R.id.btn_interantion_pwd_next);
        mNextBtn.setEnabled(false);

        mImageBtnBack.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);

        if (null == mLoadingDialog) {
            mLoadingDialog = new CustomLoadingDialog(this, this.getResources().getString(
                    R.string.str_regist_loading));
        }
        if (!TextUtils.isEmpty(mEmail)) {
            mNextBtn.setText(getString(R.string.user_regist));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPwdEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password = mPwdEditText.getText().toString();
                if (!TextUtils.isEmpty(password.trim())) {
                    mNextBtn.setTextColor(Color.parseColor("#FFFFFF"));
                    mNextBtn.setEnabled(true);
                } else {
                    mNextBtn.setTextColor(Color.parseColor("#7fffffff"));
                    mNextBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ib_internation_pwd_back) {
            finish();
        } else if (id == R.id.btn_interantion_pwd_next) {
            clickToRegist();
        }
    }

    private void clickToRegist() {
        final String pwd = mPwdEditText.getText().toString();
        mName = mNameEditText.getText().toString().trim();
        if (!UserUtils.isNetDeviceAvailable(this)) {
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
        } else {
            if (pwd.length() < 6 || pwd.length() > 16) {
                GolukUtils.showToast(this,
                        this.getResources().getString(R.string.user_login_password_show_error));
                return;
            }
            if (TextUtils.isEmpty(mName) || mName.length() < 3 || mName.length() > 12) {
                GolukUtils.showToast(this,
                        this.getResources().getString(R.string.str_user_name_limit));
                return;
            }
            if (!TextUtils.isEmpty(mPhone)) {
                registerByPhone(pwd);
            } else {
                registerByEmail(pwd);
            }
        }
    }

    private void registerByPhone(String pwd) {
        InternationalPhoneRegisterRequest request = new InternationalPhoneRegisterRequest(IPageNotifyFn.PageType_InternationalRegister, this);
        boolean b = request.get(mName, mPhone, pwd, mVcode, mZone, mStep2code);
        if (b) {
            mLoadingDialog.show();
            mNextBtn.setEnabled(false);
        } else {
            closeLoadingDialog();
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_regist_fail));
        }
    }

    private void registerByEmail(String pwd) {
        InternationalEmailRegisterRequest registerRequest = new InternationalEmailRegisterRequest(IPageNotifyFn.REGISTER_BY_EMAIL, this);
        boolean b = registerRequest.get(mEmail, pwd, mName);
        if (b) {
            mLoadingDialog.show();
            mNextBtn.setEnabled(false);
        } else {
            closeLoadingDialog();
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_regist_fail));
        }
    }

    private void closeLoadingDialog() {
        if (null != mLoadingDialog) {
            mLoadingDialog.close();
            mNextBtn.setEnabled(true);
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == IPageNotifyFn.PageType_InternationalRegister) {
            closeLoadingDialog();
            RegistBean bean = (RegistBean) result;
            if (null == bean) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_getidentify_fail));
                return;
            }
            int code = bean.code;
            if (code == 0) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_regist_success));
                final String pwd = mPwdEditText.getText().toString();
                mApp.mLoginManage.setUserLoginInterface(this);

                UserloginBeanRequest userloginBean = new UserloginBeanRequest(false, IPageNotifyFn.PageType_Login, this);
                userloginBean.loginByPhone(mPhone, mZone, pwd, "");

                mApp.loginStatus = 0;// 登录中
            } else if (code == 20103 || code == 20100) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_already_regist));
            } else if (code == 22001) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_regist_fail));
            } else {
                GolukUtils.showToast(this, bean.msg);
            }
        } else if (requestType == IPageNotifyFn.REGISTER_BY_EMAIL) {
            closeLoadingDialog();
            RegistBean bean = (RegistBean) result;
            if (null == bean) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_getidentify_fail));
                return;
            }
            if (bean.code == 20103 || bean.code == 20100) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.email_already_regist));
                return;
            } else if (bean.code != 0) {
                GolukUtils.showToast(this, bean.msg);
                return;
            }
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_regist_success));
            final String pwd = mPwdEditText.getText().toString();
            mApp.mLoginManage.setUserLoginInterface(this);

            UserloginBeanRequest userloginBean = new UserloginBeanRequest(true, IPageNotifyFn.PageType_Login, this);
            userloginBean.loginByEmail(mEmail, pwd, "");
            mApp.loginStatus = 0;// 登录中
        } else if (requestType == IPageNotifyFn.PageType_Login) {
            try {
                UserResult userresult = (UserResult) result;
                int code = userresult.code;
                switch (code) {
                    case 0:
                        UserData userdata = userresult.data;
                        userdata.email = mEmail;
                        userdata.phone = mPhone;
                        // 登录成功后，存储用户的登录信息
                        if (!TextUtils.isEmpty(mPhone))
                            $.config().putString("phone", mPhone);
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
                        if (!"".equals(mPwdEditText.getText().toString())) {
                            json.put("pwd", mPwdEditText.getText().toString());
                        }
                        json.put("uid", userresult.data.uid);
                        SharedPrefUtil.saveUserPwd(json.toString());

                        mApp.getInstance().parseLoginData(userresult.data);
                        EventBus.getDefault().post(new EventLoginSuccess());
                        EventBus.getDefault().post(new UpdateBindListEvent());
                        finish();
                        break;
                    default:
                        GolukUtils.showToast(this, mApp.getResources().getString(R.string.hint_msg_user_error));
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void loginCallbackStatus() {
        mPwdEditText.setEnabled(true);
        mNextBtn.setEnabled(true);
        switch (mApp.loginStatus) {
            case 0:
                break;
            case 1:
                // 登录成功后关闭个人中心启动模块页面
                mApp.isUserLoginSucess = true;
                mApp.autoLoginStatus = 2;
                Intent it = new Intent();
//                if ("fromStart".equals(mFrom)) {
//                    it = new Intent(InternationUserPwdActivity.this, MainActivity.class);
//                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    startActivity(it);
//                } else if ("fromIndexMore".equals(mFrom)) {
//                    it = new Intent(InternationUserPwdActivity.this, MainActivity.class);
//                    it.putExtra("showMe", "showMe");
//                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    startActivity(it);
//                } else if ("fromSetup".equals(mFrom)) {
//                    it = new Intent(InternationUserPwdActivity.this, UserSetupActivity.class);
//                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    startActivity(it);
//                } else if ("fromProfit".equals(mFrom)) {
//                    it = new Intent(InternationUserPwdActivity.this, MyProfitActivity.class);
//                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(it);
//                    UserUtils.exit();
//                }
                this.finish();
                break;
            case 2:
                mApp.isUserLoginSucess = false;
                break;
            case 3:
                mApp.isUserLoginSucess = false;
                new AlertDialog.Builder(this)
                        .setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
                        .setMessage(this.getResources().getString(R.string.user_no_regist))
                        .setNegativeButton(this.getResources().getString(R.string.user_cancle), null)
                        .setPositiveButton(this.getResources().getString(R.string.user_regist),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        mApp.mLoginManage.setUserLoginInterface(null);
                                        Intent it = new Intent(InternationUserPwdActivity.this, InternationUserRegistActivity.class);
                                        it.putExtra("intentLogin", mPhone);
                                        if (mFrom.equals("main") || mFrom.equals("back")) {// 从起始页注册
                                            it.putExtra("fromRegist", "fromStart");
                                        } else if (mFrom.equals("indexmore")) {// 从更多页个人中心注册
                                            it.putExtra("fromRegist", "fromIndexMore");
                                        } else if (mFrom.equals("setup")) {// 从设置页注册
                                            it.putExtra("fromRegist", "fromSetup");
                                        } else if (mFrom.equals("profit")) {// 从我的收益注册
                                            it.putExtra("fromRegist", "fromProfit");
                                        }

                                        startActivity(it);
                                    }
                                }).create().show();
                break;
            case 4:
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_netword_outtime));
                mApp.isUserLoginSucess = false;
                break;
            case 5:
                mApp.isUserLoginSucess = false;
                new AlertDialog.Builder(this)
                        .setTitle(this.getResources().getString(R.string.user_dialog_hint_title))
                        .setMessage(this.getResources().getString(R.string.user_login_password_limit_top_hint))
                        .setPositiveButton(this.getResources().getString(R.string.user_repwd_ok),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        mApp.mLoginManage.setUserLoginInterface(null);
                                        Intent it = new Intent(InternationUserPwdActivity.this, InternationalResetPwdActivity.class);
                                        it.putExtra("errorPwdOver", mPhone);
                                        if (mFrom.equals("main") || mFrom.equals("back")) {// 从起始页注册
                                            it.putExtra("fromRegist", "fromStart");
                                        } else if (mFrom.equals("indexmore")) {// 从更多页个人中心注册
                                            it.putExtra("fromRegist", "fromIndexMore");
                                        } else if (mFrom.equals("setup")) {// 从设置页注册
                                            it.putExtra("fromRegist", "fromSetup");
                                        } else if (mFrom.equals("profit")) {// 从我的收益注册
                                            it.putExtra("fromRegist", "fromProfit");
                                        }
                                        startActivity(it);
                                    }
                                }).create().show();
                break;
            // 密码错误
            case 6:
                mPwdEditText.setText("");
                break;
            default:

                break;
        }
    }
}