package com.goluk.a6.control;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.goluk.a6.control.util.SimpleTextWatcher;
import com.goluk.a6.control.util.TimerCountUtil;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.ChangeUserInfoRequest;
import com.goluk.a6.http.responsebean.ServerBaseResult;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.UserUtils;
import com.goluk.a6.internation.bean.CheckVcodeBean;
import com.goluk.a6.internation.bean.UserInfo;
import com.goluk.a6.internation.login.CountryBean;
import com.goluk.a6.internation.login.GetVcodeRequest;
import com.goluk.a6.internation.login.InternationUserLoginActivity;
import com.goluk.a6.internation.login.UserSelectCountryActivity;

import likly.dollar.$;

/**
 * 修改手机号页面
 */
public class ChangePhoneActivity extends BaseActivity implements OnClickListener, IRequestResultListener, TimerCountUtil.TimerCallback {

    public static final int REQUEST_CODE_ZONE = 20;

    private ImageView mBtnBack;
    private TextView mTvTitle;
    private TextView mTvInfo;
    private EditText mEtPhone, mEtCode;
    private TextView mBtnZone, mbtnGetCode, mBtnSubmit;

    private boolean mIsAcceptMsgcode = true;
    private boolean mHasGetCode;

    private TimerCountUtil mTimerCount;

    private String mPhone, mZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_phone);

        intiView();
    }

    public void intiView() {
        mBtnBack = (ImageView) findViewById(R.id.btn_back);
        mTvTitle = (TextView) findViewById(R.id.title);
        mTvInfo = (TextView) findViewById(R.id.tv_info);
        mEtPhone = (EditText) findViewById(R.id.et_phone);
        mEtCode = (EditText) findViewById(R.id.et_code);
        mBtnZone = (TextView) findViewById(R.id.btn_zone);
        mbtnGetCode = (TextView) findViewById(R.id.btn_code);
        mBtnSubmit = (TextView) findViewById(R.id.btn_submit);

        mTvTitle.setText(R.string.change_phone);
        final String info = getString(R.string.change_phone_hint, CarControlApplication.getInstance().getMyInfo().phone);
        mTvInfo.setText(info);

        String savedZoneData = GolukUtils.getSavedCountryZone();
        if (!TextUtils.isEmpty(savedZoneData)) {
            mBtnZone.setText(savedZoneData);
        } else {
            mBtnZone.setText(GolukUtils.getDefaultZone());
        }
        if (BuildConfig.BRANCH_CHINA) {
            mBtnZone.setVisibility(View.GONE);
        }

        mBtnBack.setOnClickListener(this);
        mBtnZone.setOnClickListener(this);
        mbtnGetCode.setOnClickListener(this);
        mBtnSubmit.setOnClickListener(this);
        mEtPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateBtnState();
            }
        });
        mEtCode.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateBtnState();
            }
        });

        mTimerCount = new TimerCountUtil(this);
    }

    private void updateBtnState() {
        String phone = mEtPhone.getText().toString().trim();
        String code = mEtCode.getText().toString().trim();
        boolean enable = !TextUtils.isEmpty(phone);
        mbtnGetCode.setEnabled(!TextUtils.isEmpty(phone));
        mBtnSubmit.setEnabled(!TextUtils.isEmpty(code) && mHasGetCode);
    }

    @Override
    public void onClick(View View) {
        if (View.getId() == R.id.btn_back) {
            finish();
        } else if (View.getId() == R.id.btn_submit) {
            changePhone();
        } else if (View.getId() == R.id.btn_zone) {
            Intent intent = new Intent(this, UserSelectCountryActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ZONE);
        } else if (View.getId() == R.id.btn_code) {
            getCode();
        }
    }

    /**
     * 修改手机号
     */
    private void changePhone() {
        mPhone = mEtPhone.getText().toString().trim();
        mZone = mBtnZone.getText().toString();
        String code = mEtCode.getText().toString().trim();
        mZone = mZone.substring(mZone.indexOf("+") + 1);

        if (!GolukUtils.isNetworkConnected(this)) {
            $.toast().text(R.string.user_net_unavailable).show();
            return;
        }

        ChangeUserInfoRequest request = new ChangeUserInfoRequest(1, this);
        request.get(mPhone, mZone, code);
        showLoaing();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode && REQUEST_CODE_ZONE == requestCode) {
            CountryBean bean = (CountryBean) data.getSerializableExtra(InternationUserLoginActivity.COUNTRY_BEAN);
            mBtnZone.setText(bean.area + " +" + bean.code);
        }
    }

    /**
     * 获取验证码
     */
    public void getCode() {
        String phone = mEtPhone.getText().toString();
        String zone = mBtnZone.getText().toString();
        if (TextUtils.isEmpty(zone)) {
            return;
        }

        if (!UserUtils.isNetDeviceAvailable(this)) {
            UserUtils.hideSoftMethod(this);
            $.toast().text(R.string.user_net_unavailable).show();
            return;
        }

        if (!mApp.mTimerManage.flag) {
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_timer_count_hint));
            return;
        }

        mApp.mTimerManage.timerCancel();
        int zoneCode = zone.indexOf("+");
        String code = zone.substring(zoneCode + 1, zone.length());
        UserUtils.hideSoftMethod(this);

        getVCode(phone, code);
        showLoaing();

    }

    private void callBack_getCode_Success() {
        mHasGetCode = true;
        mIsAcceptMsgcode = false;
        closeLoading();
        $.toast().text(R.string.user_getidentify_success).show();

        mTimerCount.startTimer();
    }

    @Override
    public void onLoadComplete(int requestType, Object data) {
        closeLoading();
        if (requestType == 1) {
            ServerBaseResult result = (ServerBaseResult) data;
            if (result != null) {
                if (result.code == 0) {
                    $.toast().text(R.string.change_success).show();
                    UserInfo userInfo = CarControlApplication.getInstance().getMyInfo();
                    if (userInfo != null) {
                        userInfo.phone = "+" + mZone + " " + mPhone;
                        CarControlApplication.getInstance().setUserInfo(userInfo);
                    }
                    Intent intent = new Intent();
                    intent.putExtra("phone", mPhone);
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (result.code == 20103) {
                    $.toast().text(R.string.user_already_regist).show();
                } else if (result.code == 20010) {
                    $.toast().text(R.string.user_identify_error).show();
                }
            }
        }
    }

    private void getVCode(String phone, String code) {
        GetVcodeRequest request = new GetVcodeRequest(2, new IRequestResultListener() {
            @Override
            public void onLoadComplete(int requestType, Object result) {
                closeLoading();
                CheckVcodeBean bean = (CheckVcodeBean) result;
                if (null == bean) {
                    $.toast().text(R.string.user_getidentify_fail).show();
                    return;
                }
                int code = bean.code;
                if (code == 0) {
                    callBack_getCode_Success();
                } else if (code == 20004) {
                    $.toast().text(R.string.user_no_regist).show();
                } else if (code == 20103) {
                    $.toast().text(R.string.user_already_regist).show();
                } else if (code == 12016) {
                    $.toast().text(R.string.count_identify_count_six_limit).show();
                } else {
                    $.toast().text(R.string.user_getidentify_fail).show();
                }
            }
        });
        request.get(phone, code, GetVcodeRequest.GET_NEW_CODE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimerCount.stopTimer();
    }

    @Override
    public void onTimerStart() {
        mbtnGetCode.setEnabled(false);
        mEtPhone.setEnabled(false);
    }

    @Override
    public void onTimerCount(int count) {
        mbtnGetCode.setText(count + "s");
    }

    @Override
    public void onTimeCountEnd() {
        mbtnGetCode.setEnabled(true);
        mEtPhone.setEnabled(true);
        mbtnGetCode.setText(R.string.get_verify_code);
    }

}
