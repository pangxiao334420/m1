package com.goluk.a6.control;

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
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.ChangePwdRequest;
import com.goluk.a6.http.responsebean.ServerBaseResult;
import com.goluk.a6.internation.GolukUtils;

import likly.dollar.$;

/**
 * 修改密码页面
 */
public class ChangePwdActivity extends BaseActivity implements OnClickListener, IRequestResultListener {

    private ImageView mBtnBack;
    private TextView mTvTitle;
    private EditText mEtPwdOld, mEtPwdNew;
    private TextView mBtnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pwd);

        intiView();
    }

    public void intiView() {
        mBtnBack = (ImageView) findViewById(R.id.btn_back);
        mTvTitle = (TextView) findViewById(R.id.title);
        mEtPwdOld = (EditText) findViewById(R.id.edit_pwd_old);
        mEtPwdNew = (EditText) findViewById(R.id.edit_pwd_new);
        mBtnSubmit = (TextView) findViewById(R.id.btn_submit);

        mTvTitle.setText(R.string.change_pwd);
        mBtnBack.setOnClickListener(this);
        mBtnSubmit.setOnClickListener(this);
        mEtPwdOld.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                inputChanged();
            }
        });
        mEtPwdNew.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                inputChanged();
            }
        });
    }

    private void inputChanged() {
        String inputOldPwd = mEtPwdOld.getText().toString().trim();
        String inputNewPwd = mEtPwdNew.getText().toString().trim();
        boolean enable = !TextUtils.isEmpty(inputOldPwd) && inputOldPwd.length() >= 6
                && !TextUtils.isEmpty(inputNewPwd) && inputNewPwd.length() >= 6;
        mBtnSubmit.setEnabled(enable);
    }

    @Override
    public void onClick(View View) {
        if (View.getId() == R.id.btn_back) {
            finish();
        } else if (View.getId() == R.id.btn_submit) {
            changePwd();
        }
    }

    private void changePwd() {
        String oldPwd = mEtPwdOld.getText().toString().trim();
        String newPwd = mEtPwdNew.getText().toString().trim();

        if (!GolukUtils.isNetworkConnected(this)) {
            $.toast().text(R.string.user_net_unavailable).show();
            return;
        }

        ChangePwdRequest request = new ChangePwdRequest(0, this);
        request.get(oldPwd, newPwd);
        showLoaing();
    }

    @Override
    public void onLoadComplete(int requestType, Object data) {
        closeLoading();

        ServerBaseResult result = (ServerBaseResult) data;
        if (result != null) {
            if (result.code == 0) {
                $.toast().text(R.string.change_success).show();
                finish();
            } else if (result.code == 26003) {
                // 旧密码不正确
                $.toast().text(R.string.old_pwd_incorrect).show();
            }
        }
    }

}
