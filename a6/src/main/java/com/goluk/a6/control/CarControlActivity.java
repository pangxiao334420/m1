package com.goluk.a6.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.goluk.a6.common.event.EventLoginSuccess;
import com.goluk.a6.common.event.ShowMoreNewEvent;
import com.goluk.a6.control.dvr.RemoteCameraConnectManager;
import com.goluk.a6.control.util.PermissionUtils;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.PushTokenRequest;
import com.goluk.a6.http.responsebean.ServerBaseResult;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import likly.dollar.$;
import pub.devrel.easypermissions.EasyPermissions;

public class CarControlActivity extends BaseActivity implements SplashView.SplashViewListener, IRequestResultListener, XGIOperateCallback,
        EasyPermissions.PermissionCallbacks {

    private static final String TAG = "CarSvc_CarCtrllActivity";

    public static final String ACTION_FINISH_CARCONTROL = "action_finish_carcontrol";

    private CarAssistMainView mCarAssistMainView = null;
    private RelativeLayout mMainContainer;
    private Handler mHandler = new Handler();
    private Bundle mSavedInstanceState;
    PushTokenRequest pushTokenRequest;

    public void setPaperViewEnable(boolean enable) {
        if (mCarAssistMainView != null)
            mCarAssistMainView.setPaperViewEnable(enable);
    }

    public Cling initCling(int clingId, int[] positionData, boolean animate, int delay) {
        if (mCarAssistMainView != null)
            return mCarAssistMainView.initCling(clingId, positionData, animate, delay);
        return null;
    }

    public void dismissPreviewCling(View v) {
        if (mCarAssistMainView != null)
            mCarAssistMainView.dismissPreviewCling(v);
    }

    public void dismissCarCling(View v) {
        if (mCarAssistMainView != null)
            mCarAssistMainView.dismissCarCling(v);
    }

    public void dismissPhoneCling(View v) {
        if (mCarAssistMainView != null)
            mCarAssistMainView.dismissPhoneCling(v);
    }

    public void dismissCloudCling(View v) {
        if (mCarAssistMainView != null)
            mCarAssistMainView.dismissCloudCling(v);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mSavedInstanceState = savedInstanceState;
        RemoteCameraConnectManager.create(this);
        setContentView(R.layout.activity_car_control);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMainContainer = (RelativeLayout) findViewById(R.id.main_container);
        mCarAssistMainView = (CarAssistMainView) findViewById(R.id.main);
        setPaperViewEnable(false);
        IntentFilter filter = new IntentFilter(ACTION_FINISH_CARCONTROL);
        registerReceiver(mBroadcastReceiver, filter);
        if (mApp.isUserLoginToServerSuccess()) {
            registXG();
        }

        // 申请权限
        PermissionUtils.requestLocationAndStoragePermission(this);

        // 重置绑定设备提示Flag
        $.config().putBoolean("hasShowBindConfirm", false);
    }

    private void registXG() {
        XGPushConfig.enableDebug(this, BuildConfig.DEBUG);
        XGPushManager.registerPush(getApplicationContext(), this);
        pushTokenRequest = new PushTokenRequest(2, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        if (mCarAssistMainView != null)
            mCarAssistMainView.onActivityDestroy();
        Log.d(TAG, "onDestroy");
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApp.checkUpgrade(this);
        RemoteCameraConnectManager.instance().mContext = this;
        if (mCarAssistMainView != null)
            mCarAssistMainView.onAcitvityResume();
        mApp.appStarted = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCarAssistMainView != null)
            mCarAssistMainView.onActivityPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCarAssistMainView != null)
            mCarAssistMainView.onActivityStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCarAssistMainView != null)
            mCarAssistMainView.onActivityStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");

        if (mCarAssistMainView != null) {
            mCarAssistMainView.onCreateOptionsMenu(getMenuInflater(), menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mCarAssistMainView != null && mCarAssistMainView.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mCarAssistMainView != null && mCarAssistMainView.onBackPressed())
            return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mCarAssistMainView != null)
            mCarAssistMainView.onActivityResult(requestCode, resultCode, data);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_FINISH_CARCONTROL)) {
                Log.d(TAG, "CarControlActivity finish");
                CarControlActivity.this.finish();
            }
        }

    };

    @Override
    public void onSplashViewDismissed() {
        //WindowManager.LayoutParams params = getWindow().getAttributes();
        //params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().setAttributes(params);
        RemoteCameraConnectManager.instance().setSplashViewDismissed();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ShowMoreNewEvent event) {
        mCarAssistMainView.showMoreNew(event.value);
    }


    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == 1) {
        } else if (requestType == 2) {
            ServerBaseResult result1 = (ServerBaseResult) result;
            if (result1 != null && result1.code == 0) {
                //Log.d("TPush", "服务器Token绑定成功");
            } else {
                //Log.e("TPush", "服务器绑定失败" + result1 == null ? "" : result1.msg);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventLoginSuccess event) {
        registXG();
        mApp.getShareValidity();
    }


    @Override
    public void onSuccess(Object data, int i) {
        String tag = (String) data;
        Log.d("TPush", "注册成功，设备token为：" + tag);
        pushTokenRequest.get(tag, mApp.getMyInfo().uid);
    }

    @Override
    public void onFail(Object o, int errCode, String msg) {
        Log.d("TPush", "注册失败，错误码：" + errCode + ",错误信息：" + msg);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        System.out.print("");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        System.out.print("");
    }

}
