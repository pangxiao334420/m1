package com.goluk.a6.internation;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.R;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.internation.bean.UpDescResult;
import com.goluk.a6.internation.bean.UserUpdateRetBean;
import com.goluk.a6.internation.login.InternationUserLoginActivity;
import com.goluk.a6.internation.login.UpdUserDescBeanRequest;
import com.goluk.a6.internation.login.UpdUserNameBeanRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 编辑签名
 *
 * @author mobnote
 */
public class UserPersonalSignActivity extends BaseActivity implements OnClickListener, IRequestResultListener {

    /** application **/
    /**
     * itle
     **/
    private ImageButton btnBack;
    private TextView mTextTitle, mTextOk;
    /**
     * body
     **/
    private EditText mEditBody;
    private String mSignText;
    private String mSignNewText;
    /**
     * 最大字数限制
     **/
    private TextView mTextCount = null;
    private TextView mTextCountAll = null;
    private static final int MAX_COUNT = 50;
    /**
     * 输入的个性签名剩余字数
     **/
    private int number = 0;

    // 保存数据的loading
    private CustomLoadingDialog mCustomProgressDialog = null;

    private UpdUserNameBeanRequest updUserDescBeanRequest = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_personal_edit_sign);

        // 获得GolukApplication对象
        /**
         * 获取从编辑界面传来的信息
         *
         */

        if (savedInstanceState == null) {
            Intent it = getIntent();
            mSignText = it.getStringExtra("intentSignText");
        } else {
            mSignText = savedInstanceState.getString("intentSignText");
        }

        initView();
        // title
        mTextTitle.setText(R.string.user_personal_sign_edit);
        mTextOk.setText(R.string.user_personal_title_right);
        //
        int count = mEditBody.getText().toString().length();
        mTextCount.setText("" + (MAX_COUNT - count));
        mTextCountAll.setText(this.getResources().getString(R.string.str_slash) + MAX_COUNT
                + this.getResources().getString(R.string.str_bracket_rigth));
    }

    // 初始化控件
    public void initView() {
        btnBack = (ImageButton) findViewById(R.id.back_btn);
        mTextOk = (TextView) findViewById(R.id.user_title_right);
        mTextTitle = (TextView) findViewById(R.id.user_title_text);
        mEditBody = (EditText) findViewById(R.id.user_personal_edit_sign_body);
        mTextCount = (TextView) findViewById(R.id.number_count);
        mTextCountAll = (TextView) findViewById(R.id.number_count_all);
        mCustomProgressDialog = new CustomLoadingDialog(this, getString(R.string.user_personal_saving));

        mEditBody.setText(mSignText);
        if (null != mSignText || "".equals(mSignText))
            mEditBody.setSelection(mSignText.length());

        /**
         * 监听
         */
        btnBack.setOnClickListener(this);
        mTextOk.setOnClickListener(this);
        mEditBody.addTextChangedListener(mTextWatcher);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mApp.setContext(this, "UserPersonalSign");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        outState.putString("intentSignText", mSignText);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        if (id == R.id.back_btn) {
            finish();
        } else if (id == R.id.user_title_right) {
            if (number < 0) {
                UserUtils.showDialog(this, this.getResources().getString(R.string.str_sign_limit));
            } else {
                String body = mEditBody.getText().toString();
                if (body.equalsIgnoreCase(mSignText)) {

                    Intent it = new Intent(UserPersonalSignActivity.this, UserPersonalInfoActivity.class);
                    it.putExtra("itSign", mSignNewText);
                    this.setResult(2, it);
                    this.finish();
                } else {
                    saveSign(body);
                }
            }
        } else {
        }
    }

    TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void afterTextChanged(Editable arg0) {
            int num = arg0.length();
            number = MAX_COUNT - num;
            if (number < 0) {
                mTextCount.setTextColor(Color.RED);
            } else {
                mTextCount.setTextColor(Color.GRAY);
            }
            mTextCount.setText("" + number);
        }
    };

    /**
     * 修改用户个性签名
     */
    private void saveSign(String sign) {

        if (!UserUtils.isNetDeviceAvailable(this)) {
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
        } else {
            // {desc：“个性签名”}
            mSignNewText = sign;
            try {
                updUserDescBeanRequest = new UpdUserNameBeanRequest(IPageNotifyFn.PageType_ModifySignature, this);
                updUserDescBeanRequest.get(mApp.getMyInfo().uid, "", "", URLEncoder.encode(sign, "UTF-8"), "", "","");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mCustomProgressDialog.show();
        }
    }

    public void startUserLogin() {
        Intent loginIntent = null;
        loginIntent = new Intent(this, InternationUserLoginActivity.class);
        startActivity(loginIntent);
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {

        if (requestType == IPageNotifyFn.PageType_ModifySignature) {
            UserUpdateRetBean upnameresult = (UserUpdateRetBean) result;

            if (mCustomProgressDialog.isShowing()) {
                mCustomProgressDialog.close();
            }
            if (upnameresult == null || upnameresult.code != 0) {
                GolukUtils.showToast(this, getString(R.string.user_personal_save_failed));
                return;
            }
            this.finish();
        }
    }
}
