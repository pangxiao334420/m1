package com.goluk.a6.control;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.a6.common.event.util.EventUtil;
import com.goluk.a6.control.dvr.CameraPreviewView;
import com.goluk.a6.control.dvr.CameraView;
import com.goluk.a6.control.dvr.DeviceSettingActivity;
import com.goluk.a6.control.dvr.RemoteCameraConnectManager;

public class CarPreviewActivity extends BaseActivity implements SplashView.SplashViewListener {

    private static final String TAG = "CarSvc_CarCtrllActivity";

    public static final String ACTION_FINISH_CARCONTROL = "action_finish_carcontrol";
    public static final int ACTION_ADD_APN = 10004;
    private CameraPreviewView mCarAssistMainView = null;
    public boolean showRemote;
    private boolean isFullScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_preview);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCarAssistMainView = (CameraPreviewView) findViewById(R.id.main);
        mCarAssistMainView.getFullScreenBtn().setOnClickListener(click2FullScreen);
        mCarAssistMainView.getExitFullScreenBtn().setOnClickListener(clickExitFullScreen);
        IntentFilter filter = new IntentFilter(ACTION_FINISH_CARCONTROL);
        registerReceiver(mBroadcastReceiver, filter);
        RemoteCameraConnectManager.instance().setmCameraPreviewView(mCarAssistMainView);
        showBack(true);
        setTitle(R.string.device);
    }
    public void fullScreenState(){
        mCarAssistMainView.getFullScreenBtn().setVisibility(View.GONE);
        mCarAssistMainView.getExitFullScreenBtn().setVisibility(View.VISIBLE);
    }
    public void notFullScreenState(){
        mCarAssistMainView.getFullScreenBtn().setVisibility(View.VISIBLE);
        mCarAssistMainView.getExitFullScreenBtn().setVisibility(View.GONE);
    }
    private View.OnClickListener click2FullScreen = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setFullScreen();
        }
    };

    private View.OnClickListener clickExitFullScreen = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setExitFullScreen();
        }

    };

    private void setExitFullScreen() {
        isFullScreen = false;
        ActionBar a = getActionBar();
        if (a != null) {
            a.show();
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        CameraView cameraView = mCarAssistMainView.getCameraView();
        cameraView.setKeepScreenOn(false);
        mCarAssistMainView.visiableBottomView();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        notFullScreenState();
    }

    public void setFullScreen() {
        isFullScreen = true;
        ActionBar a = getActionBar();
        if (a != null) {
            a.hide();
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        CameraView cameraView = mCarAssistMainView.getCameraView();
        cameraView.setKeepScreenOn(true);
        mCarAssistMainView.invisiableBottomView();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        fullScreenState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        if (mCarAssistMainView != null)
            mCarAssistMainView.onActivityDestroy();
        // 防止内存泄漏
//        RemoteCameraConnectManager.instance().setmCameraPreviewView(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showRemote = false;
        if (mCarAssistMainView != null)
            mCarAssistMainView.onAcitvityResume();

        if (!RemoteCameraConnectManager.instance().isConnected()) {
            EventUtil.sendStartConnectEvent();
        }
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
        if (isFullScreen) {
            setExitFullScreen();
            return;
        }

        if (mCarAssistMainView != null && mCarAssistMainView.onBackPressed())
            return;
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mCarAssistMainView != null) {
            mCarAssistMainView.onActivityResult(requestCode, resultCode, data);
            if (mCarAssistMainView.getQuickSettingFragment2() != null) {
                mCarAssistMainView.getQuickSettingFragment2().handlerActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_FINISH_CARCONTROL)) {
                Log.d(TAG, "CarControlActivity finish");
                CarPreviewActivity.this.finish();
            }
        }

    };

    @Override
    public void onSplashViewDismissed() {
        RemoteCameraConnectManager.instance().setSplashViewDismissed();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mCarAssistMainView != null)
            mCarAssistMainView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mCarAssistMainView != null)
            mCarAssistMainView.onRestoreInstanceState(savedInstanceState);
    }

    public void showSetting(boolean value) {
        if (noActionBar) {
            return;
        }
        if (value) {
            getActionBar().getCustomView().findViewById(R.id.tv_right).setVisibility(View.VISIBLE);
            ((TextView) getActionBar().getCustomView().findViewById(R.id.tv_right)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.device_add, 0);
            getActionBar().getCustomView().findViewById(R.id.tv_right).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (RemoteCameraConnectManager.getCurrentServerInfo() == null) {
                        Toast.makeText(CarPreviewActivity.this, R.string.no_connect, Toast.LENGTH_SHORT).show();
                    }
                    //mCarAssistMainView.showDvrSetting(true);
                    Intent intent = new Intent(CarPreviewActivity.this, DeviceSettingActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            getActionBar().getCustomView().findViewById(R.id.tv_right).setVisibility(View.GONE);
            ((TextView) getActionBar().getCustomView().findViewById(R.id.tv_right)).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            getActionBar().getCustomView().findViewById(R.id.tv_right).setOnClickListener(null);
        }
    }


}
