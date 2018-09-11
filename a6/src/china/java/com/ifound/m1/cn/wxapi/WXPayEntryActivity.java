package com.goluk.ifound.m1.cn.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.goluk.a6.control.flux.FluxManagerActivity;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by goluk_lium on 2017/11/6.
 */

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler{
    private static final String TAG = "WXPayEntryActivity";
    private IWXAPI api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, WXConstants.APP_ID);
        api.handleIntent(getIntent(),this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent,this);
    }

    @Override
    public void onReq(BaseReq baseReq) {}

    @Override
    public void onResp(BaseResp baseResp) {
        int resultCode = baseResp.errCode;
        if (resultCode==0){
            Toast.makeText(this,"支付成功",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, FluxManagerActivity.class);
            startActivity(intent);
        }else if (resultCode==-2){
            Toast.makeText(this,"支付失败",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this,"支付异常",Toast.LENGTH_SHORT).show();
        }
        finish();
  }
}
