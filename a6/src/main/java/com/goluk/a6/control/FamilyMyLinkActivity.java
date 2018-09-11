package com.goluk.a6.control;

import android.content.ClipboardManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.goluk.a6.common.util.H5Util;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.FamilyShareMeRequest;
import com.goluk.a6.http.responsebean.FamilyShareMeResult;


public class FamilyMyLinkActivity extends BaseActivity implements IRequestResultListener {
    private TextView mLink;
    private Button mCopy;
    private FamilyShareMeRequest request;
    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_share_link);
        setTitle(R.string.my_link);
        showBack(true);
        initView();
        request = new FamilyShareMeRequest(1, this);
        if(mApp.isUserLoginToServerSuccess()) {
            request.get(mApp.getMyInfo().uid);
        }
    }

    private void initView() {
        mLink = (TextView) findViewById(R.id.my_link);
        mCopy = (Button) findViewById(R.id.btn_copy);
        mCopy.setEnabled(false);
        mCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("", H5Util.getShareUrl(mUrl));
                clipboard.setPrimaryClip(clip);
                showToast(getString(R.string.copy));
            }
        });
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        FamilyShareMeResult meResult = (FamilyShareMeResult) result;
        if (meResult != null && meResult.code == 0 && meResult.data != null && !TextUtils.isEmpty(meResult.data.url)) {
            mUrl = meResult.data.url;
            mLink.setText(mUrl);
            mCopy.setEnabled(true);
        } else {
            showToast(getString(R.string.error_get_me));
        }
    }
}
