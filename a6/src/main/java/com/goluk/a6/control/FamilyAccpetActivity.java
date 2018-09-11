package com.goluk.a6.control;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.FamilyAccpetRequest;
import com.goluk.a6.http.request.FamilyShareMeInfoRequest;
import com.goluk.a6.http.responsebean.FamilyShareMeInfoResult;
import com.goluk.a6.http.responsebean.ServerBaseResult;
import com.goluk.a6.internation.GlideCircleTransform;
import com.goluk.a6.internation.GlideUtils;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.UserUtils;
import com.goluk.a6.internation.login.InternationUserLoginActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class FamilyAccpetActivity extends BaseActivity implements IRequestResultListener {
    private ImageView mHead;
    private TextView mTvName;
    private TextView mTvEail;
    private TextView mTvDesc;
    private String code = "";
    private String name = "";
    private String head = "";
    private String contact = "";
    private FamilyShareMeInfoRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_accpect);
        setTitle(R.string.invite_famly);
        showBack(true);
        initView();
    }

    private void initView() {
        if (mApp.getMyInfo() != null) {
            mApp.mUser.initAutoLogin();
        }
        request = new FamilyShareMeInfoRequest(0, this);
        try {
            code = getIntent().getData().getQueryParameter("code");
        } catch (Exception ex) {
        }
        mHead = (ImageView) findViewById(R.id.iv_head);
        mTvName = (TextView) findViewById(R.id.name);
        mTvEail = (TextView) findViewById(R.id.email);
        mTvDesc = (TextView) findViewById(R.id.desc);
        findViewById(R.id.my_accpect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!CarControlApplication.getInstance().isUserLoginToServerSuccess()) {
                    startActivity(new Intent(FamilyAccpetActivity.this, InternationUserLoginActivity.class));
                    return;
                }
                final FamilyAccpetRequest accpetRequest = new FamilyAccpetRequest(1, FamilyAccpetActivity.this);
                accpetRequest.get(mApp.getMyInfo().uid, code);
            }
        });
        findViewById(R.id.my_reject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!CarControlApplication.getInstance().isUserLoginToServerSuccess()) {
                    startActivity(new Intent(FamilyAccpetActivity.this, InternationUserLoginActivity.class));
                    return;
                }
                close();
            }
        });
        if (!UserUtils.isNetDeviceAvailable(this)) {
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
        } else {
            request.get(code);
        }
    }

    private void close() {
        startActivity(new Intent(FamilyAccpetActivity.this, SplashActivity.class));
        finish();
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == 0) {
            FamilyShareMeInfoResult result1 = (FamilyShareMeInfoResult) result;
            if (result1 != null && result1.code == 0 && result1.data != null) {
                try {
                    name =  URLDecoder.decode(result1.data.name, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (Exception ex){
                    name = String.valueOf(result1.data.name);
                }
                head = result1.data.avatar;
                if (TextUtils.isEmpty(result1.data.account.phone)) {
                    contact = result1.data.account.email;
                } else {
                    contact = result1.data.account.phone;
                }
                if (TextUtils.isEmpty(head) || head.contains("default")) {
                    Glide.with(this).load(R.drawable.usercenter_head_default).transform(new GlideCircleTransform(this)).into(mHead);
                } else {
                    GlideUtils.loadNetHead(this, mHead, head, R.drawable.usercenter_head_default);
                }
                mTvName.setText(name);
                mTvEail.setText(contact);
                mTvDesc.setText(getString(R.string.matt_demen, name));
            } else {
                showToast(getString(R.string.family_link_err));
            }
        } else if (requestType == 1) {
            ServerBaseResult result1 = (ServerBaseResult) result;
            if (result1 != null && result1.code == 0) {
                showToast(getString(R.string.add_family_success));
                close();
            } else {
                showToast(getString(R.string.add_family_err));
            }
        }
    }
}
