package com.goluk.a6.control;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.ChangeUserInfoRequest;
import com.goluk.a6.http.responsebean.ServerBaseResult;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.UserUtils;
import com.goluk.a6.internation.bean.UserInfo;

import likly.dollar.$;

/**
 * 修改邮箱页面
 */
public class ChangeMailActivity extends BaseActivity implements OnClickListener, IRequestResultListener, TextWatcher {

    private ImageView mBtnBack;
    private TextView mTvTitle;
    private TextView mTvInfo;
    private EditText mEtMail;
    private TextView mBtnSubmit;

    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_mail);

        intiView();
    }

    public void intiView() {
        mBtnBack = (ImageView) findViewById(R.id.btn_back);
        mTvTitle = (TextView) findViewById(R.id.title);
        mTvInfo = (TextView) findViewById(R.id.tv_info);
        mEtMail = (EditText) findViewById(R.id.edit_mail);
        mBtnSubmit = (TextView) findViewById(R.id.btn_submit);

        mTvTitle.setText(R.string.change_mail);
        final String info = getString(R.string.change_mail_hint, CarControlApplication.getInstance().getMyInfo().email);
        mTvInfo.setText(info);
        mBtnBack.setOnClickListener(this);
        mBtnSubmit.setOnClickListener(this);
        mEtMail.addTextChangedListener(this);
    }

    @Override
    public void onClick(View View) {
        if (View.getId() == R.id.btn_back) {
            finish();
        } else if (View.getId() == R.id.btn_submit) {
            changeEmail();
        }
    }

    private void changeEmail() {
        mEmail = mEtMail.getText().toString().trim();
        if (!UserUtils.emailValidation(mEmail)) {
            $.toast().text(R.string.email_invalid).show();
            return;
        }

        if (!GolukUtils.isNetworkConnected(this)) {
            $.toast().text(R.string.user_net_unavailable).show();
            return;
        }

        ChangeUserInfoRequest request = new ChangeUserInfoRequest(0, this);
        request.get(mEmail);
        showLoaing();
    }

    @Override
    public void onLoadComplete(int requestType, Object data) {
        closeLoading();

        ServerBaseResult result = (ServerBaseResult) data;
        if (result != null) {
            if (result.code == 0) {
                UserInfo userInfo = CarControlApplication.getInstance().getMyInfo();
                if (userInfo != null) {
                    userInfo.email = mEmail;
                    CarControlApplication.getInstance().setUserInfo(userInfo);
                }
                Intent intent = new Intent();
                intent.putExtra("email", mEmail);
                setResult(RESULT_OK, intent);
                finish();
            } else if (result.code == 20100) {
                $.toast().text(R.string.email_already_regist).show();
            } else if (result.code == 20011) {
                $.toast().text(R.string.email_invalid).show();
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        final String input = s.toString();
        mBtnSubmit.setEnabled(!TextUtils.isEmpty(input));
    }

}
