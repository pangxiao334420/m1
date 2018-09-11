package com.goluk.a6.internation;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.BuildConfig;
import com.goluk.a6.control.R;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.internation.bean.UserInfoBean;
import com.goluk.a6.internation.bean.UserUpdateRetBean;
import com.goluk.a6.internation.login.CountryBean;
import com.goluk.a6.internation.login.UpdUserNameBeanRequest;
import com.goluk.a6.internation.login.UserInfoRequest;
import com.goluk.a6.internation.login.UserSelectCountryActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import static com.goluk.a6.internation.login.InternationUserLoginActivity.COUNTRY_BEAN;
import static com.goluk.a6.internation.login.InternationUserLoginActivity.REQUEST_SELECT_COUNTRY_CODE;


/**
 * 编辑昵称
 *
 * @author mobnote
 */
public class UserSosActivity extends BaseActivity implements OnClickListener, IRequestResultListener {

    /** application **/
    /**
     * title
     **/
    private ImageButton btnBack;
    private TextView mTextTitle, mTextOk;
    private TextView mSelectCountryText = null;
    /**
     * body
     **/
    private EditText mEditName;
    private EditText mEditPhone;


    // 保存数据的loading
    private CustomLoadingDialog mCustomProgressDialog = null;
    private UpdUserNameBeanRequest mUpUserNameBeanRequest = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_personal_edit_sos);
        initView();
        // title
        mTextTitle.setText(R.string.sos);
        mTextOk.setText(R.string.user_personal_title_right);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    // 初始化控件
    public void initView() {
        btnBack = (ImageButton) findViewById(R.id.back_btn);
        mTextOk = (TextView) findViewById(R.id.user_title_right);
        mTextTitle = (TextView) findViewById(R.id.user_title_text);
        mSelectCountryText = (TextView) findViewById(R.id.tv_user_login_select_country);
        mEditName = (EditText) findViewById(R.id.user_personal_name_edit);
        mEditPhone = (EditText) findViewById(R.id.user_login_phonenumber);
        mCustomProgressDialog = new CustomLoadingDialog(this, getString(R.string.str_save_fail));

        /**
         * 监听
         */
        btnBack.setOnClickListener(this);
        mTextOk.setOnClickListener(this);
        mTextOk.setVisibility(View.GONE);
        findViewById(R.id.button).setOnClickListener(this);
        mSelectCountryText.setOnClickListener(this);

        String savedZoneData = GolukUtils.getSavedCountryZone();
        if (!TextUtils.isEmpty(savedZoneData)) {
            mSelectCountryText.setText(savedZoneData);
        } else {
            mSelectCountryText.setText(GolukUtils.getDefaultZone());
        }
        if(BuildConfig.BRANCH_CHINA){
            mSelectCountryText.setVisibility(View.GONE);
            findViewById(R.id.view_div).setVisibility(View.GONE);
        }
        getData();
    }

    public void getData() {
        UserInfoRequest request = new UserInfoRequest(1, this);
        if (!UserUtils.isNetDeviceAvailable(UserSosActivity.this)) {
            GolukUtils.showToast(UserSosActivity.this, this.getResources().getString(R.string.user_net_unavailable));
            return;
        }
        request.get(mApp.getMyInfo().uid);
        mCustomProgressDialog.setCurrentMessage(getString(R.string.str_loading_text));
    }

    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        if (id == R.id.back_btn) {
            UserUtils.hideSoftMethod(this);
            finish();
        } else if (id == R.id.button) {
            String name = mEditName.getText().toString().trim();
            if (name.isEmpty()) {
                GolukUtils.showToast(this, getString(R.string.input_sos_name));
                return;
            }
            String phone = mEditPhone.getText().toString().trim();
            if (phone.isEmpty()) {
                GolukUtils.showToast(this, getString(R.string.input_sos_phone));
                return;
            }
            UserUtils.hideSoftMethod(this);
            saveName(name, phone);
        } else if (id == R.id.user_personal_name_image) {
            // 点击清空
            mEditName.setText("");
        } else if (arg0.getId() == R.id.tv_user_login_select_country) {
            UserUtils.hideSoftMethod(this);
            Intent itSelectCountry = new Intent(this, UserSelectCountryActivity.class);
            startActivityForResult(itSelectCountry, REQUEST_SELECT_COUNTRY_CODE);
        }
    }


    /**
     * 修改用户名称
     */
    private void saveName(String name, String phone) {
        if (!UserUtils.isNetDeviceAvailable(this)) {
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
        } else {
            // {NickName：“昵称”}
            try {
                String zone = mSelectCountryText.getText().toString();
                int zoneCode = zone.indexOf("+");
                String code = zone.substring(zoneCode + 1, zone.length());
                mCustomProgressDialog.setCurrentMessage(getString(R.string.str_save_fail));
                mUpUserNameBeanRequest = new UpdUserNameBeanRequest(IPageNotifyFn.PageType_ModifyNickName, this);
                mUpUserNameBeanRequest.get(mApp.getMyInfo().uid, "", "", "", name, URLEncoder.encode(phone, "UTF-8"), code);
                // 保存中
                mCustomProgressDialog.show();
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == IPageNotifyFn.PageType_ModifyNickName) {
            UserUpdateRetBean upnameresult = (UserUpdateRetBean) result;
            if (mCustomProgressDialog.isShowing()) {
                mCustomProgressDialog.close();
            }
            if (upnameresult == null || upnameresult.code != 0) {
                GolukUtils.showToast(this, getString(R.string.user_personal_save_failed));
                return;
            }
            SharedPrefUtil.saveUserSos(true);
            this.finish();
        } else if (requestType == 1) {
            UserInfoBean userInfoBean = (UserInfoBean) result;
            if (userInfoBean == null || userInfoBean.code != 0 || userInfoBean.data == null) {
                return;
            }
            String name = "";
            try {
                name = URLDecoder.decode(userInfoBean.data.emgContactName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception ex){
                name = String.valueOf(userInfoBean.data.emgContactName);
            }
            mEditName.setText(name);
            mEditName.setSelection(name.length());
            mEditPhone.setText(userInfoBean.data.emgContactPhone);
            if (!TextUtils.isEmpty(userInfoBean.data.emgContactCode)) {
                mSelectCountryText.setText(" +" + userInfoBean.data.emgContactCode);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

}
