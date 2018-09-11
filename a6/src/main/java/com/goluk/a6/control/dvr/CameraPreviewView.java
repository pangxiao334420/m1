package com.goluk.a6.control.dvr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.a6.common.event.ConnectEvent;
import com.goluk.a6.common.event.util.Event;
import com.goluk.a6.common.event.util.EventUtil;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.CarAssistMainView;
import com.goluk.a6.control.CarControlActivity;
import com.goluk.a6.control.CarPreviewActivity;
import com.goluk.a6.control.IPagerView;
import com.goluk.a6.control.R;
import com.goluk.a6.control.browser.FileInfo;
import com.goluk.a6.control.util.Util;
import com.goluk.a6.control.util.WifiAdmin;
import com.goluk.a6.control.util.WifiHideAPI;
import com.goluk.a6.internation.GolukUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import likly.dollar.$;

public class CameraPreviewView extends IPagerView implements IShotSwitch {

    private static final String TAG = "CarSvc_CameraPrvView";

    private CameraView mCameraView;
    private LinearLayout bottomView;

    RelativeLayout mTab1, mTab2;
    boolean mUsingTab1 = true;
    private RelativeLayout mRlHowTo;
    private Button mBtnConnect;
    private ImageView mIvView;

    LinearLayout mFragmentNormal;
    FrameLayout mFragmentSetting;
    private ImageView mFullscreenBtn;
    private ImageView mExitFullscreenBtn;
    private TextView mSwitch;
    private QuickVoiceFragment mQuickVoiceFragment;
    private QuickSettingFragment mQuickSettingFragment;
    private QuickSettingFragment2 mQuickSettingFragment2;
    private QuickTrackFragment mQuickTrackFragment;
    private RadioGroup mTabRadioGroup, mTabRadioGroup2;
    private View mCurrentFragment;
    private Map<View, Integer> mFragmentMap = new HashMap<View, Integer>();
    private WifiManager mWifiManager;
    private WifiAdmin mWifiAdmin;
    private boolean isChooseWifi = false;
    private boolean mFirstWifiApState = true;
    private ProgressDialog mScanRecorderDialog;
    private Handler mHandler = new Handler();
    private boolean inSettingMode;
    private Context mContext;
//    private Menu menu;

    public CameraPreviewView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public CameraPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public CameraPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initView();
    }

    public void setQuickSetting2() {
        mFragmentSetting = (FrameLayout) findViewById(R.id.fragment_setting);
        mQuickSettingFragment2 = (QuickSettingFragment2) findViewById(R.id.quick_setting_fragment2);
        mFragmentMap.put(mQuickSettingFragment2, 2);
    }

    public QuickSettingFragment2 getQuickSettingFragment2() {
        return mQuickSettingFragment2;
    }

    public void showContect() {
        if (((CarPreviewActivity) getContext()).backGround && ((CarPreviewActivity) getContext()).showRemote) {
            return;
        }
        mRlHowTo.setVisibility(GONE);
        mCameraView.showContect();
        mQuickVoiceFragment.showConnected();
        if (!inSettingMode) {
            ((CarPreviewActivity) getContext()).showSetting(true);
        }
        boolean needReset = false;
        if (needReset) {
            showDvrSetting(false);
            mCurrentFragment.setVisibility(View.INVISIBLE);
            if (RemoteCameraConnectManager.isHeadless() || RemoteCameraConnectManager.isOversea()) {
                mTab1.setVisibility(View.GONE);
//	   			mTab2.setVisibility(View.VISIBLE);
                mUsingTab1 = false;
                getContext().getSharedPreferences("newsetting", 0).edit().putBoolean("newtab", true).commit();
                ((RadioButton) findViewById(R.id.about_button)).setChecked(true);
                mCurrentFragment = mQuickVoiceFragment;
            } else {
                mTab2.setVisibility(View.GONE);
//	   			mTab1.setVisibility(View.VISIBLE);
                mUsingTab1 = true;
                getContext().getSharedPreferences("newsetting", 0).edit().putBoolean("newtab", false).commit();
                ((RadioButton) findViewById(R.id.voice_button)).setChecked(true);
                mCurrentFragment = mQuickVoiceFragment;
            }
            mCurrentFragment.setVisibility(View.VISIBLE);
        }
//   		if (RemoteCameraConnectManager.supportNewSetting()) {
//   			mQuickSettingFragment.showDvrMore(true);
//   		} else {
//   			mQuickSettingFragment.showDvrMore(false);
//   		}

//   		mQuickSettingFragment.setAbilityStatue(null);
    }

    public void showDvrSetting(boolean show) {
        if (show) {
            ((BaseActivity) getContext()).setTitle(R.string.dvrset);
            ((BaseActivity) getContext()).showBack(true);
            ((CarPreviewActivity) getContext()).showSetting(false);
            mFragmentNormal.setAnimation(AnimationUtils.loadAnimation(getContext(),
                    R.anim.fragment_exit_left));
            mFragmentSetting.setAnimation(AnimationUtils.loadAnimation(getContext(),
                    R.anim.fragment_enter_left));
            mFragmentSetting.setVisibility(View.VISIBLE);
            mFragmentNormal.setVisibility(View.GONE);
            inSettingMode = true;
        } else {
            if (mFragmentSetting.getVisibility() == View.VISIBLE) {
                mFragmentSetting.setAnimation(AnimationUtils.loadAnimation(getContext(),
                        R.anim.fragment_exit_right));
                mFragmentNormal.setAnimation(AnimationUtils.loadAnimation(getContext(),
                        R.anim.fragment_enter_right));
                mFragmentSetting.setVisibility(View.GONE);
                mFragmentNormal.setVisibility(View.VISIBLE);
            }
            mQuickSettingFragment2.showDisconnect();
            ((CarPreviewActivity) getContext()).showSetting(true);
            ((BaseActivity) getContext()).setTitle(R.string.tab_preview);
            ((BaseActivity) getContext()).showBack(true);
            inSettingMode = false;
        }
    }

    public void showDiscontect() {
        mCameraView.showDiscontect();
        mQuickVoiceFragment.showDisConnected();
        showDvrSetting(false);
        ((CarPreviewActivity) getContext()).showSetting(false);
        GolukUtils.showToast(getContext(), getContext().getString(R.string.device_offline));
        //((RadioButton)findViewById(R.id.voice_button)).setChecked(true);
    }

    public void showContectting() {
        mCameraView.showContectting();
        mQuickVoiceFragment.showDisConnected();
        ((CarPreviewActivity) getContext()).showSetting(false);
    }

    public void setDVRSDcardStatus(boolean mount) {
        mCameraView.setDVRSDcardStatus(mount);
    }

    public void setRecordingButton(final boolean recording) {
        mCameraView.setRecordingButton(recording);
    }

    public void setDvrSaveTime(int time) {
        //mQuickSettingFragment2.setDvrSaveTime(time);
    }

    public void setDvrMode(String mode) {
        //mQuickSettingFragment2.setDvrMode(mode);
    }

    public void setDvrMute(boolean mute) {
        //mQuickSettingFragment2.setDvrMute(mute);
    }

    public void setUpdate(int percent, String version) {
        //mQuickSettingFragment2.setUpdate(percent, version);
    }

    public void setSatellites(int num,String nmea) {
        mQuickVoiceFragment.setSatellites(num,nmea);
    }

    public void setMobileStatus(boolean ready, boolean enable, boolean connected, int type,
                                long usage, int dbm) {
        mQuickVoiceFragment.setNetworkType(ready, connected, dbm);
        //mQuickSettingFragment2.setMobileEnabled(ready, enable, connected, type, usage);
    }

    public void setRecordStatus(boolean start, int num, int time) {
        mQuickVoiceFragment.setRecordingStatus(start, num, time);
    }

    public void setUserList(ArrayList<UserItem> list) {
        //mQuickSettingFragment2.setUserList(list);
    }

    public void setSdcardSize(long total, long left, long dvrdir) {
        //mQuickSettingFragment2.setSdcardSize(total, left, dvrdir);
    }

    public void setDvrGps(String show) {
        //mQuickSettingFragment2.setDvrGps(show);
    }

    public void setSoftApConfig(String ssid, String pwd) {
        //mQuickSettingFragment2.setSoftApConfig(ssid, pwd);
    }

    public void setGsensorLock(int enable) {
        //mQuickSettingFragment2.setGsensorLock(enable);
    }

    public void setGsensorWakeup(int enable) {
        //mQuickSettingFragment2.setGsensorWakeup(enable);
    }

    public void setAutoSleepTime(int time) {
        //mQuickSettingFragment2.setAutoSleepTime(time);
    }

    public void setGsensorSensity(int val) {
        //mQuickSettingFragment2.setGsensorSensitive(val);
    }

    //根据属性显示或者隐藏控件
    public void setAbilityStatue(String ability) {
        mQuickSettingFragment.setAbilityStatue(ability);
    }

    //设置声音
    public void setVolumeStatue(final int min, final int max, final int current) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mQuickSettingFragment.setVolumeStatue(min, max, current);
                //if (RemoteCameraConnectManager.supportNewSetting())
                    //mQuickSettingFragment2.setVolumeState(max, current);
            }
        });
    }

    public void setBrightnessPercent(int current) {
        //mQuickSettingFragment2.setBrightnessPercent(current);
    }

    //设置亮度
    public void setBrightnessStatue(int min, int max, int current) {
        mQuickSettingFragment.setBrightnessStatue(min, max, current);
    }

    //设置唤醒灵敏度
    public void setWakeUpStatue(final int value) {
        mQuickSettingFragment.setWakeUpStatue(value);
    }

    public void onSyncFile(String path, String type, List<FileInfo> list) {
        mQuickVoiceFragment.setSyncFile(path, type, list);
    }

    @Override
    public boolean onCreateOptionsMenu(MenuInflater mi, Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.ip_setting) {
            //RemoteCameraConnectManager.instance().showServerDialog();

            return true;
        }/*else if(item.getItemId() == R.id.softap){
            if(mSoftAPState == WifiHideAPI.WIFI_AP_STATE_ENABLED)
				RemoteCameraConnectManager.instance().getNetworkListener().setWifiApEnable(false);
			else if(mSoftAPState == WifiHideAPI.WIFI_AP_STATE_DISABLED)
				RemoteCameraConnectManager.instance().getNetworkListener().setWifiApEnable(true);
			return true;
		}*/
        return false;
    }

    @Override
    public void onActivate() {
        Log.i(TAG, "onActivate()");
        if (mQuickSettingFragment2 == null || mQuickSettingFragment == null) {
            return;
        }
        if (RemoteCameraConnectManager.supportNewSetting()) {
            //mQuickSettingFragment2.refreshSetting();
        } else
            mQuickSettingFragment.refreshSetting();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean show = sp.getBoolean(CarAssistMainView.KEY_PREVIEW_CLING, false);
        if (show)
            ((CarControlActivity) getContext()).initCling(R.id.preview_cling, null, false, 0);
        mQuickTrackFragment.onResume(true);
        mCameraView.refreshMiddleText();
        if (isChooseWifi && RemoteCameraConnectManager.instance().isConnecting()) {
            isChooseWifi = false;
            showContectting();
        }
        if (RemoteCameraConnectManager.instance().isConnected()) {
            showContect();
            mRlHowTo.setVisibility(GONE);
        }
    }

    @Override
    public void onDeactivate() {
        Log.i(TAG, "onDeactivate()");
//        mCameraView.stopPreview();
        mQuickTrackFragment.onPause();
    }

    @Override
    public void onActivityCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreate()");
        mQuickTrackFragment.onCreate(savedInstanceState);
    }

    public CameraView getCameraView() {
        return mCameraView;
    }

    private AboutFragment aboutFragment;

    public View getFullScreenBtn() {
        return mFullscreenBtn;
    }

    public View getExitFullScreenBtn() {
        return mExitFullscreenBtn;
    }

    public void invisiableBottomView() {
        bottomView.setVisibility(GONE);
    }

    public void visiableBottomView() {
        bottomView.setVisibility(VISIBLE);
    }

    @Override
    public void onActivityPause() {
        Log.i(TAG, "onActivityPause()");
        mQuickTrackFragment.onPause();
        mQuickVoiceFragment.onPause();
        onDeactivate();
        mCameraView.onPause();
    }

    @Override
    public void onAcitvityResume() {
        mCameraView.onStart();
        Log.i(TAG, "onAcitvityResume()");
        mQuickTrackFragment.onResume(true);
        mQuickVoiceFragment.onResume();
        onActivate();
    }

    @Override
    public void onActivityStart() {
        Log.i(TAG, "onActivityStart()");
    }

    @Override
    public void onActivityStop() {
        Log.i(TAG, "onActivityStop()");
    }

    @Override
    public void onActivityDestroy() {
        Log.i(TAG, "onActivityDestroy()");
        mQuickTrackFragment.onDestroy();
        mQuickVoiceFragment.onDestroy();
        getContext().unregisterReceiver(mBroadcastReceiver);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onBackPressed() {
        Log.i(TAG, "onBackPressed()");
        if (mFragmentNormal.getVisibility() == View.GONE) {
            showDvrSetting(false);
            return true;
        }
        return mQuickVoiceFragment.onBackPressed();
    }

    @Override
    public void refresh() {
        if (mQuickSettingFragment2 == null || mQuickSettingFragment == null) {
            return;
        }
        onActivate();
        mCameraView.requestDVRSDcardStatus();
        mCameraView.requestDVRRecordStatus();
        mQuickVoiceFragment.refresh();
        if (RemoteCameraConnectManager.supportNewSetting()) {
            //mQuickSettingFragment2.refreshSetting();
        } else
            mQuickSettingFragment.refreshSetting();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    double latitude = data.getDoubleExtra(MapSelectActivity.KEY_LATITUDE, 0);
                    double longitude = data.getDoubleExtra(MapSelectActivity.KEY_LONGITUDE, 0);
                    String name = data.getStringExtra(MapSelectActivity.KEY_NAME);
                    mQuickVoiceFragment.startNavi(latitude, longitude, name);
                }
                break;
            case CarAssistMainView.SCANNIN_GREQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    Log.i(TAG, bundle.getString("result"));
                    String scanResult = bundle.getString("result");
                    if (scanResult.startsWith("http")) {
                        final String sn;
                        if (scanResult.indexOf("?sn=") != -1 && scanResult.indexOf("&online") != -1)
                            sn = scanResult.substring(
                                    scanResult.indexOf("?sn=") + "?sn=".length(), scanResult.indexOf("&"));
                        else if (scanResult.indexOf("?sn=") != -1 && scanResult.indexOf("&online") == -1)
                            sn = scanResult.substring(
                                    scanResult.indexOf("?sn=") + "?sn=".length());
                        else
                            sn = null;
                        //check ssid & pwd
                        String ssidAp = "";
                        String pwdAp = "";
                        Map<String, String> mapRequest = Util.URLRequest(scanResult);
                        for (String strRequestKey : mapRequest.keySet()) {
                            String strRequestValue = mapRequest.get(strRequestKey);
                            if (strRequestKey.equals("ssid"))
                                ssidAp = strRequestValue;
                            if (strRequestKey.equals("pwd"))
                                pwdAp = strRequestValue;
                        }

                        Log.d(TAG, "ssid:" + ssidAp + " pwdAp:" + pwdAp);
                        if (ssidAp.length() != 0) {
                            //connect_anim softap
                            mWifiAdmin = new WifiAdmin(getContext()) {

                                @Override
                                public Intent myRegisterReceiver(BroadcastReceiver receiver, IntentFilter filter) {
                                    getContext().registerReceiver(receiver, filter);
                                    return null;
                                }

                                @Override
                                public void myUnregisterReceiver(BroadcastReceiver receiver) {

                                    getContext().unregisterReceiver(receiver);
                                }

                                @Override
                                public void onNotifyWifiConnected() {

                                    mHandler.post(new Runnable() {

                                        @Override
                                        public void run() {

                                            Log.v(TAG, "have connected success!");
                                            mScanRecorderDialog.dismiss();
                                            Toast.makeText(getContext(), R.string.connect_ap_success,
                                                    Toast.LENGTH_SHORT).show();
                                            if (sn != null) {
                                                RemoteCameraConnectManager.instance().setAutoConnectSerial(sn);
                                            }
                                        }
                                    });

                                }

                                @Override
                                public void onNotifyWifiConnectFailed() {

                                    mHandler.post(new Runnable() {

                                        @Override
                                        public void run() {

                                            Log.v(TAG, "have connected failed!");
                                            mScanRecorderDialog.dismiss();
                                            Toast.makeText(getContext(), R.string.connect_ap_fail,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            };
                            //mWifiAdmin.openWifi();
                            if (mWifiManager.isWifiEnabled() == false) {
                                Toast.makeText(getContext(), getContext().getString(R.string.msg_bond_device_wifi_isoff), Toast.LENGTH_SHORT).show();
                                return;
                            } else {
                                Log.d(TAG, "start addNetwork");
                                mScanRecorderDialog.show();
                                if (pwdAp.length() != 0)
                                    mWifiAdmin.addNetwork(mWifiAdmin.createWifiInfo(ssidAp, pwdAp, WifiAdmin.TYPE_WPA));
                                else
                                    mWifiAdmin.addNetwork(mWifiAdmin.createWifiInfo(ssidAp, "", WifiAdmin.TYPE_NO_PASSWD));
                            }
                        } else {
                            Toast.makeText(getContext(), R.string.open_recorder_ap_tip,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.camera_preview_view, this);
        EventBus.getDefault().register(this);
        mSwitch = (TextView) findViewById(R.id.tv_switch);
        mCameraView = (CameraView) findViewById(R.id.camera_view);
        bottomView = (LinearLayout) findViewById(R.id.layout_bottom_view);
        mFragmentNormal = (LinearLayout) findViewById(R.id.fragment_normal);
        setQuickSetting2();
        mQuickVoiceFragment = (QuickVoiceFragment) findViewById(R.id.quick_voice_fragment);
        mQuickVoiceFragment.setmShotListener(mCameraView);
        mCameraView.setmShotListener(mQuickVoiceFragment);
        mCameraView.setmShowSwitchListener(this);
        mQuickSettingFragment = (QuickSettingFragment) findViewById(R.id.quick_setting_fragment);
        mQuickSettingFragment.setCameraPreviewView(this);

        mQuickTrackFragment = (QuickTrackFragment) findViewById(R.id.quick_track_fragment);
        mFragmentMap.put(mQuickVoiceFragment, 0);
        mFragmentMap.put(mQuickSettingFragment, 1);
        //mFragmentMap.put(mQuickSettingFragment2, 2);
//        mFragmentMap.put(mAboutFragment, 3);
        mFragmentMap.put(mQuickTrackFragment, 4);

        mCameraView.setQuickTrackFragment(mQuickTrackFragment);
        mFullscreenBtn = (ImageView) findViewById(R.id.iv_fullscreen);
        mExitFullscreenBtn = (ImageView) findViewById(R.id.iv_exit_fullscreen);

        mTab1 = (RelativeLayout) findViewById(R.id.tab1);
        mTab2 = (RelativeLayout) findViewById(R.id.tab2);
        mSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!RemoteCameraConnectManager.instance().isConnected()) {
                    return;
                }
                mCameraView.switchCamera(true);
                if (mCameraView.isFrontCAM()) {
                } else {
                }
            }
        });
        mTabRadioGroup2 = (RadioGroup) findViewById(R.id.fragmen_tab2);
        ((RadioButton) findViewById(R.id.about_button)).setChecked(true);
        mTabRadioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View oldFragment = mCurrentFragment;
                switch (checkedId) {
                    case R.id.about_button:
                        //mCurrentFragment = mAboutFragment;
                        break;
                    case R.id.track2_button:
                        mCurrentFragment = mQuickTrackFragment;
                        break;
                }
                if (mFragmentMap.get(oldFragment) < mFragmentMap.get(mCurrentFragment)) {
                    oldFragment.setAnimation(AnimationUtils.loadAnimation(getContext(),
                            R.anim.fragment_exit_left));
                    mCurrentFragment.setAnimation(AnimationUtils.loadAnimation(getContext(),
                            R.anim.fragment_enter_left));
                } else {
                    oldFragment.setAnimation(AnimationUtils.loadAnimation(getContext(),
                            R.anim.fragment_exit_right));
                    mCurrentFragment.setAnimation(AnimationUtils.loadAnimation(getContext(),
                            R.anim.fragment_enter_right));
                }
                oldFragment.setVisibility(View.GONE);
                mCurrentFragment.setVisibility(View.VISIBLE);
            }
        });

        mTabRadioGroup = (RadioGroup) findViewById(R.id.fragmen_tab);
        ((RadioButton) findViewById(R.id.voice_button)).setChecked(true);
        mTabRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View oldFragment = mCurrentFragment;
                switch (checkedId) {
                    case R.id.voice_button:
                        mCurrentFragment = mQuickVoiceFragment;
                        break;
                    case R.id.setting_button: {
                        mCurrentFragment = mQuickSettingFragment;
                    }
                    break;
                    case R.id.track_button:
                        mCurrentFragment = mQuickTrackFragment;
                        break;
                }
                if (mFragmentMap.get(oldFragment) < mFragmentMap.get(mCurrentFragment)) {
                    oldFragment.setAnimation(AnimationUtils.loadAnimation(getContext(),
                            R.anim.fragment_exit_left));
                    mCurrentFragment.setAnimation(AnimationUtils.loadAnimation(getContext(),
                            R.anim.fragment_enter_left));
                } else {
                    oldFragment.setAnimation(AnimationUtils.loadAnimation(getContext(),
                            R.anim.fragment_exit_right));
                    mCurrentFragment.setAnimation(AnimationUtils.loadAnimation(getContext(),
                            R.anim.fragment_enter_right));
                }
                oldFragment.setVisibility(View.GONE);
                mCurrentFragment.setVisibility(View.VISIBLE);
            }
        });

        mCurrentFragment = mQuickVoiceFragment;

        IntentFilter mIntentFilter = new IntentFilter(WifiHideAPI.WIFI_AP_STATE_CHANGED_ACTION);
        getContext().registerReceiver(mBroadcastReceiver, mIntentFilter);

        mWifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mScanRecorderDialog = new ProgressDialog(getContext());
        mScanRecorderDialog.setMessage(getContext().getString(R.string.connecting_ap));

        mRlHowTo = (RelativeLayout) findViewById(R.id.connect_cling);
//        mRlHowTo.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                chooseWif();
//            }
//        });
        mIvView = (ImageView) findViewById(R.id.image);
        AnimationDrawable ad = (AnimationDrawable) getResources().getDrawable(
                R.drawable.connect_anim);
        mIvView.setBackgroundDrawable(ad);
        //Start animation
        ad.start();
        mBtnConnect = (Button) findViewById(R.id.btn_connect);
        mBtnConnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (!CarControlApplication.getInstance().isUserLoginToServerSuccess()) {
//                    getContext().startActivity(new Intent(getContext(), InternationUserLoginActivity.class));
//                    return;
//                }
                if (GolukUtils.isFastDoubleClick()) {
                    return;
                }
                chooseWif();
            }
        });
        if (RemoteCameraConnectManager.instance() == null) {
            RemoteCameraConnectManager.create(getContext());
        } else {
            RemoteCameraConnectManager.instance().mContext = this.getContext();
        }
    }

    public void chooseWif() {
        isChooseWifi = true;
        Util.chooseWifi(getContext());
    }

    private void handleWifiApStateChanged(int state) {
        switch (state) {
            case WifiHideAPI.WIFI_AP_STATE_ENABLING:
                if (Build.VERSION.SDK_INT >= 14)
                    ((Activity) getContext()).invalidateOptionsMenu();
                break;
            case WifiHideAPI.WIFI_AP_STATE_ENABLED:
                if (!mFirstWifiApState)
                    Toast.makeText(getContext(), R.string.tip_softap_open, Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= 14)
                    ((Activity) getContext()).invalidateOptionsMenu();
                break;
            case WifiHideAPI.WIFI_AP_STATE_DISABLING:
                if (Build.VERSION.SDK_INT >= 14)
                    ((Activity) getContext()).invalidateOptionsMenu();
                break;
            case WifiHideAPI.WIFI_AP_STATE_DISABLED:
                if (!mFirstWifiApState)
                    Toast.makeText(getContext(), R.string.tip_softap_close, Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= 14)
                    ((Activity) getContext()).invalidateOptionsMenu();
                break;
            default:
        }
        mFirstWifiApState = false;
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String action = arg1.getAction();
            if (WifiHideAPI.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiApStateChanged(arg1.getIntExtra(WifiHideAPI.EXTRA_WIFI_AP_STATE,
                        WifiHideAPI.WIFI_AP_STATE_FAILED));
            }
        }

    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ConnectEvent event) {
        if (event.connect) {
            showContect();
        } else {
            showDiscontect();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        mCameraView.onSaveInstanceState(outState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mCameraView.onRestoreInstanceState(savedInstanceState);
    }

    public void onDvrLanguage(String lan) {
        //mQuickSettingFragment2.onDvrLanguage(lan);
    }

    @Override
    public void showSwitch(boolean value) {
        mSwitch.setVisibility(value ? VISIBLE : GONE);
    }

    public void setEDog(int value) {
        //mQuickSettingFragment2.setEDog(value);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Event event) {
        if (EventUtil.isConnectedEvent(event)) {
            // 显示是否进行绑定提示
            final String imei = (String) event.data;
            showBindConfirmDialog(imei);
        } else if (EventUtil.isBindDeviceSuccessEvent(event)) {
            // 绑定成功
            mIsBindSuccess = true;
        }
    }

    private boolean mIsBindSuccess;
    private AlertDialog mBindConfirmDialog;

    /**
     * 显示确认是否绑定对话框
     */
    private void showBindConfirmDialog(final String imei) {
        // 先判断设备是否联网正常
        if (!RemoteCameraConnectManager.instance().isSimConnected())
            return;

        if (mBindConfirmDialog != null && mBindConfirmDialog.isShowing())
            return;

        if (getConfigHasShowBindConfirm())
            return;

        mBindConfirmDialog = new AlertDialog.Builder(mContext)
                .setMessage(R.string.bind_prompt)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveShowConfirmDialogFlag(true);
                    }
                })
                .setPositiveButton(R.string.bind, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventUtil.sendBindDeviceEvent(imei);

                        mIsBindSuccess = false;
                        // 绑定超时设置
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!mIsBindSuccess)
                                    $.toast().text(R.string.bind_failed).show();
                            }
                        }, 30 * 1000);
                    }
                })
                .setCancelable(false)
                .create();
        mBindConfirmDialog.show();
    }

    private void saveShowConfirmDialogFlag(boolean hasShow) {
        $.config().putBoolean("hasShowBindConfirm", hasShow);
    }

    private boolean getConfigHasShowBindConfirm() {
        return $.config().getBoolean("hasShowBindConfirm");
    }

}
