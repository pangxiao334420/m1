package com.goluk.a6.control.dvr;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.R;
import com.goluk.a6.control.dvr.model.ApnBean;
import com.goluk.a6.internation.GolukUtils;

/**
 * Created by goluk_lium on 2018/1/30.
 */

public class ApnManagerActivity extends BaseActivity {

    public static ApnManagerActivity instance = null;

    private EditText etApnName,etApn,etApnMcc,etApnMnc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_apn);
        showBack(true);
        setTitle(getString(R.string.create_apn));
        initView();
        instance = this;
    }

    private void initView(){
        etApnName = (EditText) findViewById(R.id.et_apn_name);
        etApn = (EditText) findViewById(R.id.et_apn);
        etApnMcc = (EditText) findViewById(R.id.et_apn_mcc);
        etApnMnc = (EditText) findViewById(R.id.et_apn_mnc);

        TextView addApn = (TextView) getActionBar().getCustomView().findViewById(R.id.tv_right);
        addApn.setVisibility(View.VISIBLE);
        addApn.setText(R.string.apn_add);
        addApn.setOnClickListener(addApnClickListener);

    }

    private View.OnClickListener addApnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ApnBean apnBean = getApnValues();
            if (TextUtils.isEmpty(apnBean.name)){
                GolukUtils.showToast(ApnManagerActivity.this,getString(R.string.str_input_apn_name));
                return;
            }
            if (TextUtils.isEmpty(apnBean.apn)){
                GolukUtils.showToast(ApnManagerActivity.this,getString(R.string.str_input_apn));
                return;
            }
            if (TextUtils.isEmpty(apnBean.mcc)){
                GolukUtils.showToast(ApnManagerActivity.this,getString(R.string.str_input_mcc));
                return;
            }
            if (TextUtils.isEmpty(apnBean.mnc)){
                GolukUtils.showToast(ApnManagerActivity.this,getString(R.string.str_input_mnc));
                return;
            }
            Intent intent = new Intent();
            intent.putExtra("apn",getApnValues());
            setResult(RESULT_OK,intent);
            finish();
        }
    };

    public ApnBean getApnValues(){

        ApnBean apnBean = new ApnBean();
        apnBean.apn = etApn.getText().toString();
        apnBean.name = etApnName.getText().toString();
        apnBean.mcc = etApnMcc.getText().toString();
        apnBean.mnc = etApnMnc.getText().toString();
        return apnBean;
    }

}
