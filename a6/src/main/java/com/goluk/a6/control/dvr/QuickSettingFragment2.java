
package com.goluk.a6.control.dvr;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.a6.control.BuildConfig;
import com.goluk.a6.control.CarPreviewActivity;
import com.goluk.a6.control.CarWebSocketClient;
import com.goluk.a6.control.Config;
import com.goluk.a6.control.PasswordActivity;
import com.goluk.a6.control.R;
import com.goluk.a6.control.SdActivity;
import com.goluk.a6.control.WiFiNameActivity;
import com.goluk.a6.control.browser.FileInfo;
import com.goluk.a6.control.dvr.model.ApnBean;
import com.goluk.a6.control.util.HttpRequestManager;
import com.goluk.a6.internation.GolukUtils;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.goluk.a6.control.CarWebSocketClient.KEY_WATERMARK;

public class QuickSettingFragment2 extends RelativeLayout implements CarWebSocketClient.CarWebSocketClientCallback {

    private static final String TAG = "TAG_QuickSettingFrag2";
    final int MSG_GWAKEUP_SET = 100;
    final int MSG_GLOCK_SET = 101;
    final int MSG_MUTE_SET = 102;
    final int MSG_GPS_SET = 103;
    final int MSG_WATER_SET = 104;
    final int MSG_EDOG_SET = 105;

    AlertDialog mAutoSleepAlertDialog, mSensityDialog, mEDogDialog, mSaveTimeDialog, mQualityDialog, mVolumeDialog, mLanguageDialog;
    SettingListAdapter mAutoSleepAdapter, mSensityAdapter, mEdogAdapter, mSaveTimeAdapter, mQualityAdapter, mVolumeAdapter, mLanAdapter;
    TextView mMobileTitle, mMobileDetail, mAutoSleepDetail, mSensityDetail, mEdogDetail, mEdogHint, mSaveTimeDetail, mTvVolume, mTvLanguage, mQualityDetail, mVersionTitle, mVersionDetail;
    Switch mMobileSwitch, mGWakeupSwitch, mGLockSwitch, mMuteSwitch, mGpsSwitch;
    LinearLayout mSoftapLayout, mAutoSleepLayout, mSensityLayout, mLLEDogLayout, mSaveTimeLayout, mQualityLayout, mLLVolumeLayout, mLLLanguage, mDvrRestartLayout;
    LinearLayout mApnLayout;
    RelativeLayout mBrightnessLayout, mUpdateLayout;
    private SeekBar mVolumeSeekBar;
    private SeekBar mBrightnessSeekBar;
    private ProgressDialog mScanRecorderDialog;

    ImageView mUpdateNotify;
    private long mLastClickTime = 0;
    TextView mBondTitle;
    TextView mTvDataUsed;
    LinearLayout mSdcardExist;
    LinearLayout mWiFiName;
    LinearLayout mReset;
    TextView mSdcardTitle;
    TextView mSdcardSize;
    TextView mAutoSleep;
    TextView mSoftApConfig;
    EditText mSsidName, mPwd;
    String mSsid, mPassword;

    int mDvrSaveTime = -1;
    int mDvrMute = -1;
    int mDvrGps = -1;
    int mWaterMark = -1;
    int volume = 0;
    String mDvrMode = "";
    long mTotalSize = 0;
    boolean mAdasReport, mAdasReport2, mAdasReport3;
    AlertDialog mInstallDialog = null;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GWAKEUP_SET: {
                    try {
                        JSONObject jso = new JSONObject();
                        jso.put("f", "set");
                        JSONObject item = new JSONObject();
                        item.put(CarWebSocketClient.GSENSOR_ENABLE, mGWakeup);
                        jso.put("generic", item);
                        HttpRequestManager.instance().requestWebSocket(jso.toString());
                    } catch (JSONException e) {

                        e.printStackTrace();
                    }
                }
                break;
                case MSG_EDOG_SET: {
                    try {
                        JSONObject jso = new JSONObject();
                        jso.put("f", "set");
                        JSONObject item = new JSONObject();
                        item.put(CarWebSocketClient.EDOG, mEdog);
                        jso.put("generic", item);
                        HttpRequestManager.instance().requestWebSocket(jso.toString());
                    } catch (JSONException e) {

                        e.printStackTrace();
                    }
                }

                break;
                case MSG_GLOCK_SET: {
                    try {
                        JSONObject jso = new JSONObject();
                        jso.put("f", "set");
                        JSONObject item = new JSONObject();
                        item.put(CarWebSocketClient.VIDEO_LOCK_ENABLE, mGLock);
                        jso.put("generic", item);
                        HttpRequestManager.instance().requestWebSocket(jso.toString());
                    } catch (JSONException e) {

                        e.printStackTrace();
                    }
                }
                break;
                case MSG_MUTE_SET: {
                    try {
                        mScanRecorderDialog.show();
                        JSONObject jso = new JSONObject();
                        jso.put("f", "set");
                        JSONObject item = new JSONObject();
                        item.put(CarWebSocketClient.KEY_MUTE_RECORD, mDvrMute == 1);
                        jso.put("dvr", item);
                        HttpRequestManager.instance().requestWebSocket(jso.toString());
                    } catch (JSONException e) {
                        mScanRecorderDialog.dismiss();
                        e.printStackTrace();
                    }
                }
                break;
                case MSG_GPS_SET: {
                    try {
                        JSONObject jso = new JSONObject();
                        jso.put("f", "set");
                        JSONObject item = new JSONObject();
                        item.put(CarWebSocketClient.KEY_GPS_WATERMARK, mDvrGps == 1);
                        jso.put("dvr", item);
                        HttpRequestManager.instance().requestWebSocket(jso.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                case MSG_WATER_SET: {
                    try {
                        JSONObject jso = new JSONObject();
                        jso.put("f", "set");
                        JSONObject item = new JSONObject();
                        item.put("key", KEY_WATERMARK);
                        item.put("value", mWaterMark);
                        jso.put("property", item);
                        HttpRequestManager.instance().requestWebSocket(jso.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }

    };

    int mGsensorVal = -1, mAutoSleepTtime = -1, mVol = -1, mBrightness = -1, mGWakeup = -1, mGLock = -1, mEdog = -1;
    private long mLeft;
    private long mDvrDir;
    private boolean inSetting = false;

    public QuickSettingFragment2(Context context) {
        super(context);
        initView();
    }

    public QuickSettingFragment2(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public QuickSettingFragment2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public void setBrightnessVisible(boolean visible) {
        mBrightnessLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void showDisconnect() {
        if (mVolumeDialog.isShowing()) {
            mVolumeDialog.dismiss();
        }
        if (mQualityDialog.isShowing()) {
            mQualityDialog.dismiss();
        }
        if (mLanguageDialog.isShowing()) {
            mLanguageDialog.dismiss();
        }
        if (mScanRecorderDialog.isShowing()) {
            mScanRecorderDialog.dismiss();
        }
        if (ApnManagerActivity.instance != null) {
            ApnManagerActivity.instance.finish();
        }
    }

    public void refreshSetting() {
        if (RemoteCameraConnectManager.supportWebsocket()) {
            if (RemoteCameraConnectManager.supportNewSetting()) {
                try {
                    JSONObject jso = new JSONObject();
                    jso.put("f", "get");
                    JSONArray items = new JSONArray();
                    items.put("generic");
                    items.put("mobile");
                    items.put("softap");
                    items.put("dvr");
                    items.put("sdcard");
                    items.put("bondlist");
                    items.put("update");
                    items.put("lang");
                    items.put("adas");
                    jso.put("what", items);
                    HttpRequestManager.instance().requestWebSocket(jso.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    JSONObject jso = new JSONObject();
                    jso.put("action", "get");
                    JSONArray items = new JSONArray();
                    items.put(Config.PROPERTY_SETTING_STATUS_BRIGHTNESS);
                    items.put(Config.PROPERTY_SETTING_STATUS_VOLUME);
                    items.put(Config.PROPERTY_SETTING_STATUS_WAKE_UP);
                    items.put(Config.PROPERTY_SETTING_STATUS_VOICE_PROMPT);
                    items.put(Config.PROPERTY_CARDVR_STATUS_ABILITY);
                    jso.put("list", items);
                    jso.toString();
                    Log.i(TAG, "jso.toString() = " + jso.toString());
                    HttpRequestManager.instance().requestWebSocket(jso.toString());
                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }
        }
        getWaterMark();
    }

    public void getWaterMark() {
        try {
            JSONObject jso = new JSONObject();
            jso.put("f", "get");
            JSONObject items = new JSONObject();
            items.put("key", KEY_WATERMARK);
            jso.put("property", items);
            HttpRequestManager.instance().requestWebSocket(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addApn(ApnBean apnBean) {
        try {
            JSONObject jso = new JSONObject();
            jso.put("f", "broadcast");
            JSONObject items = new JSONObject();
            items.put("action", "com.car.useapn");
            items.put("name", apnBean.name);
            items.put("apn", apnBean.apn);
            items.put("mcc", apnBean.mcc);
            items.put("mnc", apnBean.mnc);
            jso.put("intent", items);
            String content = jso.toString();
            HttpRequestManager.instance().requestWebSocket(content);
//            GolukUtils.showToast(getContext(),""); 提示添加成功
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getApn() {
        try {
            JSONObject jso = new JSONObject();
            jso.put("action", "get");
            JSONArray items = new JSONArray();
            items.put("com.car.useapn");
            jso.put("list", items);
            HttpRequestManager.instance().requestWebSocket(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    public void setUpdate(final int percent, final String version) {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                mVersionTitle.setText(version);
//                handlePercent(percent);
//                // TODO: 通过判断版本号，确定是否显示 -- APN，电子狗
//                boolean show = GolukUtils.compareVersion("V1.1.5", version);
//                showEdog(show);
//                showApnController(show);
//            }
//        });
//    }

    private void showApnController(boolean show) {
        if (show) return;
        mApnLayout.setVisibility(View.GONE);
    }

    private void showEdog(boolean show) {
        if (show) return;
        if (!BuildConfig.BRANCH_CHINA) {
            mLLEDogLayout.setVisibility(View.GONE);
            findViewById(R.id.text_edog_desc).setVisibility(View.GONE);
        }
    }

    void handlePercent(int percent) {
        Log.d(TAG, "percent=" + percent);
        if (percent == -1 || percent == -2) {
            mVersionDetail.setText(R.string.version_latest);
            mUpdateNotify.setVisibility(View.INVISIBLE);
        } else if (percent == 101) {
//            mUpdateNotify.setVisibility(View.VISIBLE);
            mVersionDetail.setText(R.string.version_available);
            if (mInstallDialog == null) {
                mInstallDialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.version_install)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    JSONObject jso = new JSONObject();
                                    jso.put("f", "set");
                                    JSONObject items = new JSONObject();
                                    items.put("install", true);
                                    jso.put("update", items);
                                    HttpRequestManager.instance().requestWebSocket(jso.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();
            }
            if (!mInstallDialog.isShowing())
                mInstallDialog.show();
        } else if (percent >= 0) {
//            mUpdateNotify.setVisibility(View.VISIBLE);
            mVersionDetail.setText(String.format(getResources().getString(R.string.version_downloading), percent));
        }
    }

//    public void setMobileEnabled(final boolean ready, final boolean enabled, final boolean connected, final int type, final long usage) {
//        mHandler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                if (ready) {
//                    mMobileSwitch.setEnabled(true);
//                    mMobileSwitch.setChecked(enabled);
//                    String tip = getResources().getString(R.string.mobile_disconnect);
//                    if (connected) {
//                        if (type == TelephonyManager.NETWORK_TYPE_LTE) tip = "4G";
//                        else if (type >= TelephonyManager.NETWORK_TYPE_HSDPA) tip = "3G";
//                        else tip = "Unknown";
//                    }
//                    mMobileTitle.setText(String.format(getResources().getString(R.string.mobile_title), tip));
//                    mMobileDetail.setText(String.format(getResources().getString(R.string.mobile_config), Formatter.formatFileSize(getContext(), usage)));
//                    mTvDataUsed.setText(String.format(getResources().getString(R.string.mobile_data_used), Formatter.formatFileSize(getContext(), usage)));
//                } else {
//                    mMobileSwitch.setEnabled(false);
//                    String tip = getResources().getString(R.string.nosim);
//                    mMobileTitle.setText(String.format(getResources().getString(R.string.mobile_title), tip));
//                    mMobileDetail.setText(String.format(getResources().getString(R.string.mobile_config), Formatter.formatFileSize(getContext(), 0)));
//                    mTvDataUsed.setText(String.format(getResources().getString(R.string.mobile_data_used), Formatter.formatFileSize(getContext(), 0)));
//                }
//            }
//        });
//    }

//    public void setGsensorWakeup(final int enable) {
//        mHandler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                switch (enable) {
//                    case 0:
//                        if (mGWakeupSwitch.isChecked()) {
//                            mGWakeupSwitch.setChecked(false);
//                            mGWakeup = 0;
//                        }
//                        break;
//                    case 1:
//                        if (!mGWakeupSwitch.isChecked()) {
//                            mGWakeupSwitch.setChecked(true);
//                            mGWakeup = 1;
//                        }
//                        break;
//                }
//            }
//        });
//    }

//    public void setDvrSaveTime(final int time) {
//        mHandler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                int value = 0;
//                switch (time) {
//                    case 60:
//                        value = 0;
//                        mSaveTimeDetail.setText(R.string.auto_save_m1);
//                        break;
//                    case 120:
//                        value = 1;
//                        mSaveTimeDetail.setText(R.string.auto_save_m2);
//                        break;
//                    case 180:
//                        value = 2;
//                        mSaveTimeDetail.setText(R.string.auto_save_m3);
//                        break;
//                }
//                mSaveTimeAdapter.setSelected(value);
//            }
//        });
//    }

//    public void setDvrMode(final String mode) {
//        if (inSetting) {
//            inSetting = false;
//            restart();
//        }
//        mHandler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                if (mode.equals("high")) {
//                    mQualityAdapter.setSelected(0);
//                    mQualityDetail.setText(R.string.high);
//                } else {
//                    mQualityAdapter.setSelected(1);
//                    mQualityDetail.setText(R.string.normal);
//                }
//            }
//        });
//    }

//    public void setUserList(final ArrayList<UserItem> list) {
//    }

    public void setSdcardSize(final long total, final long left, final long dvrdir) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mTotalSize = total;
                mLeft = left;
                mDvrDir = dvrdir;
                String tips = String.format(getResources().getString(R.string.sdcard_storage_info), Formatter.formatFileSize(getContext(), total)
                        , Formatter.formatFileSize(getContext(), left), Formatter.formatFileSize(getContext(), dvrdir)
                        , Formatter.formatFileSize(getContext(), total - left - dvrdir));
                mSdcardSize.setText(tips);
                if (mTotalSize == 0) {
                    mSdcardTitle.setText(R.string.nosdcard);
                } else {
                    mSdcardTitle.setText(R.string.sdcard);
                }
            }
        });
    }

//    public void setDvrMute(final boolean mute) {
//        if (mScanRecorderDialog.isShowing()) {
//            mScanRecorderDialog.dismiss();
//            restart();
//        }
//        mHandler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                mDvrMute = mute ? 1 : 0;
//                mMuteSwitch.setChecked(!mute);
//            }
//        });
//    }

//    public void setDvrGps(final String show) {
//        mHandler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                int value = -1;
//                if (TextUtils.isEmpty(show)) {
//                    value = 1;
//                } else {
//                    value = Integer.parseInt(show);
//                }
//                mGpsSwitch.setChecked(value == 1);
//                mWaterMark = value;
//            }
//        });
//    }

//    public void setSoftApConfig(String ssid, String pwd) {
//        mSoftApConfig.setText(String.format(getResources().getString(R.string.softap_config), ssid));
//        mSsid = ssid;
//        mPassword = pwd;
//    }

//    public void setGsensorLock(final int enable) {
//        mHandler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                switch (enable) {
//                    case 0:
//                        if (mGLockSwitch.isChecked()) {
//                            mGLockSwitch.setChecked(false);
//                            mGLock = 0;
//                        }
//                        break;
//                    case 1:
//                        if (!mGLockSwitch.isChecked()) {
//                            mGLockSwitch.setChecked(true);
//                            mGLock = 1;
//                        }
//                        break;
//                }
//            }
//        });
//    }

//    public void setGsensorSensitive(final int val) {
//        mHandler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                switch (val) {
//                    case 0:
//                        mSensityDetail.setText(R.string.setting_wake_up_low);
//                        break;
//                    case 1:
//                        mSensityDetail.setText(R.string.setting_wake_up_mid);
//                        break;
//                    case 2:
//                        mSensityDetail.setText(R.string.setting_wake_up_high);
//                        break;
//                }
//                mSensityAdapter.setSelected(val);
//            }
//        });
//    }

//    public void setEDog(final int value) {
////        if(!BuildConfig.BRANCH_CHINA){
////            return;
////        }
//        if (value < 0) {
//            return;
//        }
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                switch (value) {
//                    case 0:
//                        mEdogDetail.setText(R.string.e_dog_close);
//                        break;
//                    case 1:
//                        mEdogDetail.setText(R.string.e_dog_report_road_cam);
//                        break;
//                    case 2:
//                        mEdogDetail.setText(R.string.e_dog_report_traffic_con);
//                        break;
//                    case 3:
//                        mEdogDetail.setText(R.string.e_dog_cam_traffic);
//                        break;
//                }
//                mEdogAdapter.setSelected(value);
//            }
//        });
//    }

//    public void setAutoSleepTime(final int time) {
//        mHandler.post(new Runnable() {
//
//            @Override
//            public void run() {
//                int value = 0;
//                switch (time) {
//                    case 15:
//                        value = 0;
//                        mAutoSleepDetail.setText(R.string.auto_sleep_15minutes);
//                        break;
//                    case 30:
//                        value = 1;
//                        mAutoSleepDetail.setText(R.string.auto_sleep_30minutes);
//                        break;
//                    case 60:
//                        value = 2;
//                        mAutoSleepDetail.setText(R.string.auto_sleep_60minutes);
//                        break;
//                    case 0:
//                        value = 3;
//                        mAutoSleepDetail.setText(R.string.auto_sleep_forbidden);
//                        break;
//                }
//                mAutoSleepAdapter.setSelected(value);
//            }
//        });
//    }

    //设置声音
//    public void setVolumeState(int max, int current) {
//        mVolumeSeekBar.setMax(max);
//        mVolumeSeekBar.setProgress(current);
//        volume = current;
//        mTvVolume.setText(getVolumeString(current));
//        mVolumeAdapter.setCurrent(getContext().getString(getVolumeString(current)));
//    }

    public void setLanguage(String language) {
        mTvLanguage.setText(getLanguage(language));
        mLanAdapter.setCurrent(getContext().getString(getLanguage(language)));
    }

    private int getLanguage(String language) {
        if (TextUtils.isEmpty(language)) {
            return R.string.language_eng;
        } else if (language.equals("en") || language.equals("en-US")) {
            return R.string.language_eng;
        } else if (language.contains("ru")) {
            return R.string.language_ru;
        } else if (language.contains("zh")) {
            return R.string.language_cn;
        }
        return 0;
    }

    public int getVolumeString(int value) {
        if (value == 0) {
            return R.string.muting;
        } else if (value <= 5) {
            return R.string.volume_low;
        } else if (value <= 10) {
            return R.string.volume_center;
        } else {
            return R.string.volume_high;
        }
    }

    //设置亮度
//    public void setBrightnessPercent(int current) {
//        mBrightnessSeekBar.setMax(100);
//        mBrightnessSeekBar.setProgress(current);
//    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition  
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.quick_setting_fragment2, this);

        mUpdateNotify = (ImageView) findViewById(R.id.update_notify);
        mVersionTitle = (TextView) findViewById(R.id.version_title);
        mVersionDetail = (TextView) findViewById(R.id.version_detail);
        mUpdateLayout = (RelativeLayout) findViewById(R.id.version_layout);
//        mUpdateLayout.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                mVersionDetail.setText(R.string.version_check);
//                try {
//                    JSONObject jso = new JSONObject();
//                    jso.put("f", "set");
//                    JSONObject items = new JSONObject();
//                    items.put("check", true);
//                    jso.put("update", items);
//                    HttpRequestManager.instance().requestWebSocket(jso.toString());
//                } catch (JSONException e) {
//
//                    e.printStackTrace();
//                }
//            }
//        });
        mScanRecorderDialog = new ProgressDialog(getContext());
        mScanRecorderDialog.setMessage(getContext().getString(R.string.setting));

        mWiFiName = (LinearLayout) findViewById(R.id.ll_wifi_name);
        mDvrRestartLayout = (LinearLayout) findViewById(R.id.dvrrestart_layout);
        mTvDataUsed = (TextView) findViewById(R.id.tv_data_usage);
        mDvrRestartLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//                AlertDialog formatDialog = new AlertDialog.Builder(getContext())
//                        .setTitle(R.string.dvr_restart)
//                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
                restart();
            }
//                        })
//                        .setNegativeButton(R.string.cancel, null)
//                        .create();
//                formatDialog.show();
//            }

        });

        mMobileSwitch = (Switch) findViewById(R.id.switch_mobile);
        mMobileSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    JSONObject jso = new JSONObject();
                    jso.put("f", "set");
                    JSONObject items = new JSONObject();
                    items.put("enable", isChecked);
                    jso.put("mobile", items);
                    HttpRequestManager.instance().requestWebSocket(jso.toString());
                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }
        });

        mWiFiName.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), WiFiNameActivity.class);
                intent.putExtra("ssidName", mSsid);
                intent.putExtra("ssidPassword", mPassword);
                getContext().startActivity(intent);
            }
        });

        mMobileTitle = (TextView) findViewById(R.id.mobile_title);
        mMobileDetail = (TextView) findViewById(R.id.mobile_config);

        mBrightnessLayout = (RelativeLayout) findViewById(R.id.brightnesslayout);

        mBondTitle = (TextView) findViewById(R.id.bond_title);

        mSdcardTitle = (TextView) findViewById(R.id.sdcard_title);
        mSdcardExist = (LinearLayout) findViewById(R.id.sdcard_exist);
        mReset = (LinearLayout) findViewById(R.id.reset);
        mReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.hint)
                        .setMessage(R.string.reset_factory)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    mScanRecorderDialog.show();
                                    JSONObject jso = new JSONObject();
                                    jso.put("reset", "1");
                                    JSONObject items = new JSONObject();
                                    items.put("factory", jso);
                                    items.put("f", "set");
                                    HttpRequestManager.instance().requestWebSocket(items.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();
                dialog.show();
            }
        });
        mSdcardSize = (TextView) findViewById(R.id.sdcard_size);
        mSdcardExist.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mTotalSize == 0) {
                    Toast.makeText(getContext(), R.string.nosdcard, Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getContext(), SdActivity.class);
                    intent.putExtra(SdActivity.TOTAL, mTotalSize);
                    intent.putExtra(SdActivity.LEFT, mLeft);
                    intent.putExtra(SdActivity.DIR, mDvrDir);
                    getContext().startActivity(intent);
                }
            }
        });

        mVolumeSeekBar = (SeekBar) findViewById(R.id.volume);
        mVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "mVolumeSeekBar:" + seekBar.getProgress());
                if (RemoteCameraConnectManager.supportWebsocket()) {
                    try {
                        JSONObject jso = new JSONObject();
                        jso.put("f", "set");
                        JSONObject items = new JSONObject();
                        items.put(CarWebSocketClient.SYSTEM_VOLUME, seekBar.getProgress());
                        jso.put("generic", items);
                        HttpRequestManager.instance().requestWebSocket(jso.toString());
                    } catch (JSONException e) {

                        e.printStackTrace();
                    }
                }
            }
        });

        mBrightnessSeekBar = (SeekBar) findViewById(R.id.brightness);
        mBrightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.i(TAG, "mBrightnessSeekBar:" + seekBar.getProgress());
                if (RemoteCameraConnectManager.supportWebsocket()) {
                    try {
                        JSONObject jso = new JSONObject();
                        jso.put("f", "set");
                        JSONObject items = new JSONObject();
                        items.put(CarWebSocketClient.SCREEN_BRIGHTNESS, seekBar.getProgress());
                        jso.put("generic", items);
                        HttpRequestManager.instance().requestWebSocket(jso.toString());
                    } catch (JSONException e) {

                        e.printStackTrace();
                    }
                }
            }

        });

        initAllReports();
        initSoftAp();
        initApnView();
        initAutoSleep();
        initSensity();
        initGwakeup();
        initGLock();
        initSaveTime();
        initQuality();
        initDeviceVolume();
        initDeviceLanguage();
        initRecordMute();
        initGpsWatermark();
        setSdcardSize(0, 0, 0);
        //国际版本 添加电子狗
        initEDog();
    }

    private void restart() {
        try {
            JSONObject jso = new JSONObject();
            jso.put("f", "set");
            JSONObject items = new JSONObject();
            items.put("restart", true);
            jso.put("dvr", items);
            HttpRequestManager.instance().requestWebSocket(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void initAllReports() {

    }

    void initSoftAp() {
        mSoftApConfig = (TextView) findViewById(R.id.softap_config);
        mSoftapLayout = (LinearLayout) findViewById(R.id.softap_layout);
        mSoftapLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PasswordActivity.class);
                intent.putExtra("ssidName", mSsid);
                intent.putExtra("ssidPassword", mPassword);
                getContext().startActivity(intent);
            }
        });
    }

    void initApnView() {
        mApnLayout = (LinearLayout) findViewById(R.id.apn_setting_layout);
        mApnLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ApnManagerActivity.class);
                ((DeviceSettingActivity) getContext()).startActivityForResult(intent, CarPreviewActivity.ACTION_ADD_APN);
            }
        });
        if (BuildConfig.BRANCH_CHINA) {
            mApnLayout.setVisibility(View.GONE);
        }
    }

    public void handlerActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CarPreviewActivity.ACTION_ADD_APN) {
            if (resultCode == CarPreviewActivity.RESULT_OK) {
                ApnBean apnBean = data.getParcelableExtra("apn");
                addApn(apnBean);
                Log.e(TAG, "handlerActivityResult: " + apnBean.apn);
            }
        }
    }

    void initGpsWatermark() {
        mGpsSwitch = (Switch) findViewById(R.id.switch_gps);
        mGpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                    return;
                }
                if (mWaterMark < 0) {
                    int val = isChecked ? 1 : 0;
                    mWaterMark = val;
                    return;
                }
                int val = isChecked ? 1 : 0;
                mWaterMark = val;
                mHandler.removeMessages(MSG_WATER_SET);
                mHandler.sendEmptyMessageDelayed(MSG_WATER_SET, 500);
                mLastClickTime = SystemClock.elapsedRealtime();
            }
        });
    }

    void initRecordMute() {
        mMuteSwitch = (Switch) findViewById(R.id.switch_mute);
        mMuteSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                    return;
                }
                int val = !mMuteSwitch.isChecked() ? 1 : 0;
                if (mDvrMute == val) return;
                mDvrMute = val;
                mHandler.removeMessages(MSG_MUTE_SET);
                mHandler.sendEmptyMessageDelayed(MSG_MUTE_SET, 500);
            }
        });
    }

    void initGLock() {
        mGLockSwitch = (Switch) findViewById(R.id.switch_glock);
        mGLockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int val = isChecked ? 1 : 0;
                if (mGLock == val) return;
                mGLock = val;
                mHandler.removeMessages(MSG_GLOCK_SET);
                mHandler.sendEmptyMessageDelayed(MSG_GLOCK_SET, 500);
            }
        });
    }

    void initGwakeup() {
        mGWakeupSwitch = (Switch) findViewById(R.id.switch_gsensorwakeup);
        mGWakeupSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int val = isChecked ? 1 : 0;
                if (mGWakeup == val) return;
                mGWakeup = val;
                mHandler.removeMessages(MSG_GWAKEUP_SET);
                mHandler.sendEmptyMessageDelayed(MSG_GWAKEUP_SET, 500);
            }
        });
    }

    void initQuality() {
        mQualityDetail = (TextView) findViewById(R.id.quality_config);
        ListView listView = new ListView(getContext());
        ArrayList<String> list = new ArrayList<String>();
        list.add(getResources().getString(R.string.high));
        list.add(getResources().getString(R.string.normal));

        mQualityAdapter = new SettingListAdapter(list, getContext(), getResources().getString(R.string.high));
        listView.setAdapter(mQualityAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int id, long arg3) {
                mQualityDialog.dismiss();
                String mode = "";
                switch (id) {
                    case 0:
                        mode = "high";
                        mQualityDetail.setText(R.string.high);
                        break;
                    case 1:
                        mode = "normal";
                        mQualityDetail.setText(R.string.normal);
                        break;
                }

                if (mDvrMode.equals(mode)) return;
                mDvrMode = mode;
                inSetting = true;
                try {
                    JSONObject jso = new JSONObject();
                    jso.put("f", "set");
                    JSONObject item = new JSONObject();
                    item.put(CarWebSocketClient.KEY_FRONT_CAMERA, mode);
                    jso.put("dvr", item);
                    HttpRequestManager.instance().requestWebSocket(jso.toString());
                } catch (JSONException e) {

                    e.printStackTrace();
                }

            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.front_cam_qua);
        builder.setView(listView);
        mQualityDialog = builder.create();

        mQualityLayout = (LinearLayout) findViewById(R.id.quality_layout);
        mQualityLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mQualityDialog.show();
            }
        });
    }

    void initDeviceVolume() {
        mTvVolume = (TextView) findViewById(R.id.tv_device_volume);
        ListView listView = new ListView(getContext());
        ArrayList<String> list = new ArrayList<String>();
        list.add(getResources().getString(R.string.volume_high));
        list.add(getResources().getString(R.string.volume_center));
        list.add(getResources().getString(R.string.volume_low));
        list.add(getResources().getString(R.string.muting));

        mVolumeAdapter = new SettingListAdapter(list, getContext(), getContext().getString(getVolumeString(volume)));
        listView.setAdapter(mVolumeAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int id, long arg3) {
                mVolumeDialog.dismiss();
                int value = 0;
                if (id == 0) {
                    value = 15;
                } else if (id == 1) {
                    value = 10;
                } else if (id == 2) {
                    value = 5;
                } else if (id == 3) {
                    value = 0;
                }
                volume = value;
                if (RemoteCameraConnectManager.supportWebsocket()) {
                    try {
                        JSONObject jso = new JSONObject();
                        jso.put("f", "set");
                        JSONObject items = new JSONObject();
                        items.put(CarWebSocketClient.SYSTEM_VOLUME, value);
                        jso.put("generic", items);
                        HttpRequestManager.instance().requestWebSocket(jso.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.device_volume);
        builder.setView(listView);
        mVolumeDialog = builder.create();
        mLLVolumeLayout = (LinearLayout) findViewById(R.id.ll_volume);
        mLLVolumeLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mVolumeDialog.show();
            }
        });
    }

    void initDeviceLanguage() {
        mTvLanguage = (TextView) findViewById(R.id.tv_lanuage);
        ListView listView = new ListView(getContext());
        ArrayList<String> list = new ArrayList<String>();
        list.add(getResources().getString(R.string.language_eng));
        list.add(getResources().getString(R.string.language_ru));
        list.add(getResources().getString(R.string.language_cn));

        mLanAdapter = new SettingListAdapter(list, getContext(), getContext().getString(R.string.language_eng));
        listView.setAdapter(mLanAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int id, long arg3) {
                mLanguageDialog.dismiss();
                String value = "";
                if (id == 0) {
                    value = "en";
                } else if (id == 1) {
                    value = "ru-RU";
                } else if (id == 2) {
                    value = "zh-CN";
                }
                setLanguage(value);
                if (RemoteCameraConnectManager.supportWebsocket()) {
                    try {
                        JSONObject jso = new JSONObject();
                        jso.put("locale", value);
                        JSONObject items = new JSONObject();
                        items.put("lang", jso);
                        items.put("f", "set");
                        HttpRequestManager.instance().requestWebSocket(items.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.language);
        builder.setView(listView);
        mLanguageDialog = builder.create();
        mLLLanguage = (LinearLayout) findViewById(R.id.ll_lan);
        mLLLanguage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mLanguageDialog.show();
            }
        });
    }

    void initSaveTime() {
        mSaveTimeDetail = (TextView) findViewById(R.id.savetime_config);
        ListView listView = new ListView(getContext());
        ArrayList<String> list = new ArrayList<String>();
        list.add(getResources().getString(R.string.auto_save_m1));
        list.add(getResources().getString(R.string.auto_save_m2));
        list.add(getResources().getString(R.string.auto_save_m3));

        mSaveTimeAdapter = new SettingListAdapter(list, getContext(), getResources().getString(R.string.auto_save_m2));
        listView.setAdapter(mSaveTimeAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int id, long arg3) {
                mSaveTimeDialog.dismiss();
                int value = 0;
                switch (id) {
                    case 0:
                        value = 60;
                        mSaveTimeDetail.setText(R.string.auto_save_m1);
                        break;
                    case 1:
                        value = 120;
                        mSaveTimeDetail.setText(R.string.auto_save_m2);
                        break;
                    case 2:
                        value = 180;
                        mSaveTimeDetail.setText(R.string.auto_save_m3);
                        break;
                }

                if (mDvrSaveTime == value) return;
                mDvrSaveTime = value;

                try {
                    JSONObject jso = new JSONObject();
                    jso.put("f", "set");
                    JSONObject item = new JSONObject();
                    item.put(CarWebSocketClient.KEY_AUTO_SAVE_TIME, value);
                    jso.put("dvr", item);
                    HttpRequestManager.instance().requestWebSocket(jso.toString());
                } catch (JSONException e) {

                    e.printStackTrace();
                }

            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.autosave_time_title);
        builder.setView(listView);
        mSaveTimeDialog = builder.create();

        mSaveTimeLayout = (LinearLayout) findViewById(R.id.savetime_layout);
        mSaveTimeLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mSaveTimeDialog.show();
            }
        });
    }

    void initSensity() {
        mSensityDetail = (TextView) findViewById(R.id.sensity_config);
        ListView listView = new ListView(getContext());
        ArrayList<String> list = new ArrayList<String>();
        list.add(getResources().getString(R.string.setting_wake_up_low));
        list.add(getResources().getString(R.string.setting_wake_up_mid));
        list.add(getResources().getString(R.string.setting_wake_up_high));

        mSensityAdapter = new SettingListAdapter(list, getContext(), getResources().getString(R.string.setting_wake_up_mid));
        listView.setAdapter(mSensityAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int id, long arg3) {
                mSensityDialog.dismiss();
                switch (id) {
                    case 0:
                        mSensityDetail.setText(R.string.setting_wake_up_low);
                        break;
                    case 1:
                        mSensityDetail.setText(R.string.setting_wake_up_mid);
                        break;
                    case 2:
                        mSensityDetail.setText(R.string.setting_wake_up_high);
                        break;
                }

                if (mGsensorVal == id) return;
                mGsensorVal = id;

                try {
                    JSONObject jso = new JSONObject();
                    jso.put("f", "set");
                    JSONObject items = new JSONObject();
                    items.put(CarWebSocketClient.GSENSOR_SENSITIVE, id);
                    jso.put("generic", items);
                    HttpRequestManager.instance().requestWebSocket(jso.toString());
                } catch (JSONException e) {

                    e.printStackTrace();
                }

            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.sensitive_title);
        builder.setView(listView);
        mSensityDialog = builder.create();

        mSensityLayout = (LinearLayout) findViewById(R.id.sensity_layout);
        mSensityLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mSensityDialog.show();
            }
        });
    }

    void initEDog() {
        mEdogHint = (TextView) findViewById(R.id.text_edog_desc);
        if (BuildConfig.BRANCH_CHINA) {
            mEdogHint.setVisibility(GONE);
        }
        mEdogDetail = (TextView) findViewById(R.id.edog_config);
        ListView listView = new ListView(getContext());
        ArrayList<String> list = new ArrayList<>();
        list.add(getResources().getString(R.string.e_dog_close));
        list.add(getResources().getString(R.string.e_dog_report_road_cam));
        list.add(getResources().getString(R.string.e_dog_report_traffic_con));
        list.add(getResources().getString(R.string.e_dog_cam_traffic));

        mEdogAdapter = new SettingListAdapter(list, getContext(), getResources().getString(R.string.e_dog_close));
        listView.setAdapter(mEdogAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int id, long arg3) {
                mEDogDialog.dismiss();
                switch (id) {
                    case 0:
                        mEdogDetail.setText(R.string.e_dog_close);
                        break;
                    case 1:
                        mEdogDetail.setText(R.string.e_dog_report_road_cam);
                        break;
                    case 2:
                        mEdogDetail.setText(R.string.e_dog_report_traffic_con);
                        break;
                    case 3:
                        mEdogDetail.setText(R.string.e_dog_cam_traffic);
                        break;
                }

                if (mEdog == id) return;
                mEdog = id;

                mHandler.removeMessages(MSG_EDOG_SET);
                mHandler.sendEmptyMessageDelayed(MSG_EDOG_SET, 500);
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.edog);
        builder.setView(listView);
        mEDogDialog = builder.create();
        mLLEDogLayout = (LinearLayout) findViewById(R.id.ll_edog_layout);
        mLLEDogLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mEDogDialog.show();
            }
        });
    }

    void initAutoSleep() {
        mAutoSleepDetail = (TextView) findViewById(R.id.autosleep_config);
        mAutoSleep = (TextView) findViewById(R.id.autosleep);
        ListView listView = new ListView(getContext());
        ArrayList<String> list = new ArrayList<String>();
        list.add(getResources().getString(R.string.auto_sleep_m15));
        list.add(getResources().getString(R.string.auto_sleep_m30));
        list.add(getResources().getString(R.string.auto_sleep_m60));
        list.add(getResources().getString(R.string.auto_sleep_mf));

        mAutoSleepAdapter = new SettingListAdapter(list, getContext(), getResources().getString(R.string.auto_sleep_m15));
        listView.setAdapter(mAutoSleepAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int id, long arg3) {
                mAutoSleepAlertDialog.dismiss();
                int value = 0;
                switch (id) {
                    case 0:
                        value = 15;
                        mAutoSleepDetail.setText(R.string.auto_sleep_15minutes);
                        break;
                    case 1:
                        value = 30;
                        mAutoSleepDetail.setText(R.string.auto_sleep_30minutes);
                        break;
                    case 2:
                        value = 60;
                        mAutoSleepDetail.setText(R.string.auto_sleep_60minutes);
                        break;
                    case 3:
                        value = 0;
                        mAutoSleepDetail.setText(R.string.auto_sleep_forbidden);
                        break;
                }
                if (mAutoSleepTtime == value) return;

                mAutoSleepTtime = value;

                try {
                    JSONObject jso = new JSONObject();
                    jso.put("f", "set");
                    JSONObject item = new JSONObject();
                    item.put(CarWebSocketClient.AUTOSLEEP_TIME, value);
                    jso.put("generic", item);
                    HttpRequestManager.instance().requestWebSocket(jso.toString());
                } catch (JSONException e) {

                    e.printStackTrace();
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.auto_sleep);
        builder.setView(listView);
        mAutoSleepAlertDialog = builder.create();

        mAutoSleepLayout = (LinearLayout) findViewById(R.id.autosleep_layout);
        mAutoSleepLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mAutoSleepAlertDialog.show();
            }
        });
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }

    @Override
    public void onSetSerialNo(String serial) {

    }

    @Override
    public void onSetAbilityStatue(String ability) {

    }

    @Override
    public void onSetVolumeStatue(int min, final int max, final int current) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mVolumeSeekBar.setMax(max);
                mVolumeSeekBar.setProgress(current);
                volume = current;
                mTvVolume.setText(getVolumeString(current));
                mVolumeAdapter.setCurrent(getContext().getString(getVolumeString(current)));
            }
        });
    }

    @Override
    public void onSetBrightnessStatue(int min, int max, int current) {

    }

    @Override
    public void onSetWakeUpStatue(int value) {

    }

    @Override
    public void onSetVoicePromptStatue(boolean enable) {

    }

    @Override
    public void onSetDVRRecordStatus(boolean recording) {

    }

    @Override
    public void onSetDVRSDcardStatus(boolean mount) {

    }

    @Override
    public void onDirDVRFiles(String path, JSONArray array) {

    }

    @Override
    public void onDeleteDVRFile(boolean succes) {

    }

    @Override
    public void onSyncFile(String path, String type, List<FileInfo> list) {

    }

    @Override
    public void onSetBrightnessPercent(final int percent) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mBrightnessSeekBar.setMax(100);
                mBrightnessSeekBar.setProgress(percent);
            }
        });
    }

    @Override
    public void onSetAutoSleepTime(final int time) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                int value = 0;
                switch (time) {
                    case 15:
                        value = 0;
                        mAutoSleepDetail.setText(R.string.auto_sleep_15minutes);
                        break;
                    case 30:
                        value = 1;
                        mAutoSleepDetail.setText(R.string.auto_sleep_30minutes);
                        break;
                    case 60:
                        value = 2;
                        mAutoSleepDetail.setText(R.string.auto_sleep_60minutes);
                        break;
                    case 0:
                        value = 3;
                        mAutoSleepDetail.setText(R.string.auto_sleep_forbidden);
                        break;
                }
                mAutoSleepAdapter.setSelected(value);
            }
        });
    }

    @Override
    public void onGsensorSensity(final int sensity) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                switch (sensity) {
                    case 0:
                        mSensityDetail.setText(R.string.setting_wake_up_low);
                        break;
                    case 1:
                        mSensityDetail.setText(R.string.setting_wake_up_mid);
                        break;
                    case 2:
                        mSensityDetail.setText(R.string.setting_wake_up_high);
                        break;
                }
                mSensityAdapter.setSelected(sensity);
            }
        });
    }

    @Override
    public void onGsensorWakeup(final int enable) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                switch (enable) {
                    case 0:
                        if (mGWakeupSwitch.isChecked()) {
                            mGWakeupSwitch.setChecked(false);
                            mGWakeup = 0;
                        }
                        break;
                    case 1:
                        if (!mGWakeupSwitch.isChecked()) {
                            mGWakeupSwitch.setChecked(true);
                            mGWakeup = 1;
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void onGsensorLock(final int enable) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                switch (enable) {
                    case 0:
                        if (mGLockSwitch.isChecked()) {
                            mGLockSwitch.setChecked(false);
                            mGLock = 0;
                        }
                        break;
                    case 1:
                        if (!mGLockSwitch.isChecked()) {
                            mGLockSwitch.setChecked(true);
                            mGLock = 1;
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void onSoftApConfig(final String ssid, final String pwd) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mSoftApConfig.setText(getResources().getString(R.string.softap_config, ssid));
                mSsid = ssid;
                mPassword = pwd;
            }
        });
    }

    @Override
    public void onDvrSaveTime(final int time) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                int value = 0;
                switch (time) {
                    case 60:
                        value = 0;
                        mSaveTimeDetail.setText(R.string.auto_save_m1);
                        break;
                    case 120:
                        value = 1;
                        mSaveTimeDetail.setText(R.string.auto_save_m2);
                        break;
                    case 180:
                        value = 2;
                        mSaveTimeDetail.setText(R.string.auto_save_m3);
                        break;
                }
                mSaveTimeAdapter.setSelected(value);
            }
        });
    }

    @Override
    public void onDvrMode(final String mode) {
        if (inSetting) {
            inSetting = false;
            restart();
        }
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                if (mode.equals("high")) {
                    mQualityAdapter.setSelected(0);
                    mQualityDetail.setText(R.string.high);
                } else {
                    mQualityAdapter.setSelected(1);
                    mQualityDetail.setText(R.string.normal);
                }
            }
        });
    }

    @Override
    public void onDvrLanguage(final String lan) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                setLanguage(lan);
            }
        });
    }

    @Override
    public void onDvrMute(final boolean mute) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                if (mScanRecorderDialog.isShowing()) {
                    mScanRecorderDialog.dismiss();
                    restart();
                }

                mDvrMute = mute ? 1 : 0;
                mMuteSwitch.setChecked(!mute);
            }
        });
    }

    @Override
    public void onDvrGps(final String show) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                int value = -1;
                if (TextUtils.isEmpty(show)) {
                    value = 1;
                } else {
                    value = Integer.parseInt(show);
                }
                mGpsSwitch.setChecked(value == 1);
                mWaterMark = value;
            }
        });
    }

    @Override
    public void onSdcardSize(final long total, final long left, final long dvrdir) {
        setSdcardSize(total, left, dvrdir);
    }

    @Override
    public void onUserList(String serialNum, int cloudID, ArrayList<UserItem> list) {

    }

    @Override
    public void onRecordStatus(boolean start, int num, int time) {

    }

    @Override
    public void onMobileStatus(String imei, final boolean ready, int dBm, final boolean enable, final boolean connected, final int type, final long usage, boolean registered, String flag) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                if (ready) {
                    mMobileSwitch.setEnabled(true);
                    mMobileSwitch.setChecked(enable);
                    String tip = getResources().getString(R.string.mobile_disconnect);
                    if (connected) {
                        if (type == TelephonyManager.NETWORK_TYPE_LTE) tip = "4G";
                        else if (type >= TelephonyManager.NETWORK_TYPE_HSDPA) tip = "3G";
                        else tip = "Unknown";
                    }
                    mMobileTitle.setText(String.format(getResources().getString(R.string.mobile_title), tip));
                    mMobileDetail.setText(String.format(getResources().getString(R.string.mobile_config), Formatter.formatFileSize(getContext(), usage)));
                    mTvDataUsed.setText(String.format(getResources().getString(R.string.mobile_data_used), Formatter.formatFileSize(getContext(), usage)));
                } else {
                    mMobileSwitch.setEnabled(false);
                    String tip = getResources().getString(R.string.nosim);
                    mMobileTitle.setText(String.format(getResources().getString(R.string.mobile_title), tip));
                    mMobileDetail.setText(String.format(getResources().getString(R.string.mobile_config), Formatter.formatFileSize(getContext(), 0)));
                    mTvDataUsed.setText(String.format(getResources().getString(R.string.mobile_data_used), Formatter.formatFileSize(getContext(), 0)));
                }
            }
        });
    }

    @Override
    public void onSatellites(boolean enabled, int num, long timestamp, String nmea) {

    }

    @Override
    public void onUpdate(final int percent, final String version) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mVersionTitle.setText(version);
                handlePercent(percent);
                // TODO: 通过判断版本号，确定是否显示 -- APN，电子狗
                boolean show = GolukUtils.compareVersion("V1.1.5", version);
                showEdog(show);
                showApnController(show);
            }
        });
    }

    @Override
    public void onCpuInfo(double cpuTemp, double pmuTemp, int core, int freq, boolean isFull, boolean isAccOn) {

    }

    @Override
    public void onGsensor(float x, float y, float z, boolean passed) {
        Log.e("onGsensor", "x:" + y +",y:" + y + ",z:" + z);
    }

    @Override
    public void onAdas(String key, boolean value) {

    }

    @Override
    public void onEDog(final int value) {
//        if(!BuildConfig.BRANCH_CHINA){
//            return;
//        }
        if (value < 0) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (value) {
                    case 0:
                        mEdogDetail.setText(R.string.e_dog_close);
                        break;
                    case 1:
                        mEdogDetail.setText(R.string.e_dog_report_road_cam);
                        break;
                    case 2:
                        mEdogDetail.setText(R.string.e_dog_report_traffic_con);
                        break;
                    case 3:
                        mEdogDetail.setText(R.string.e_dog_cam_traffic);
                        break;
                }
                mEdogAdapter.setSelected(value);
            }
        });
    }

    class SettingListAdapter extends BaseAdapter {

        private List<String> mList;
        private Context mContext;
        private LayoutInflater mInflater;
        private String mCurrent;

        SettingListAdapter(List<String> list, Context context, String current) {
            mList = list;
            mContext = context;
            mCurrent = current;
            mInflater = LayoutInflater.from(mContext);
        }

        public void setCurrent(String current) {
            mCurrent = current;
            notifyDataSetChanged();
        }

        public void setSelected(int index) {
            // Fix bugly #3142
            if (index >= mList.size())
                return;
            mCurrent = mList.get(index);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            String item = mList.get(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.setting_item, null);
            }

            ViewHolder vh = (ViewHolder) convertView.getTag();
            if (vh == null) {
                vh = new ViewHolder();
                vh.mServerName = (TextView) convertView.findViewById(R.id.setting_name);
                vh.mServerCheckBox = (CheckBox) convertView.findViewById(R.id.setting_checkbox);
                convertView.setTag(vh);
            }

            vh.mServerName.setText(item);
            vh.mServerCheckBox.setChecked(item.equals(mCurrent));

            return convertView;
        }
    }

    private class ViewHolder {
        TextView mServerName;
        CheckBox mServerCheckBox;
    }
}
