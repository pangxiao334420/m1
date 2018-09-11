package com.goluk.a6.control;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gelitenight.waveview.library.WaveView;
import com.goluk.a6.control.browser.FileInfo;
import com.goluk.a6.control.dvr.RemoteCameraConnectManager;
import com.goluk.a6.control.dvr.UserItem;
import com.goluk.a6.control.util.HttpRequestManager;
import com.goluk.a6.control.util.WaveHelper;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SdActivity extends BaseActivity implements CarWebSocketClient.CarWebSocketClientCallback {
    public static final String TOTAL = "total";
    public static final String LEFT = "left";
    public static final String DIR = "dir";

    WaveView waveView;
    private WaveHelper mWaveHelper;
    Button mBtnFormat;
    TextView mSdcardSize;
    TextView mSdAvaSize;
    private Handler mHandler;
    private ProgressDialog mDialog;
    private boolean isFormating;
    private AlertDialog formatDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sd);
        setTitle(R.string.sdcard);
        showBack(true);
        mHandler = new Handler(Looper.getMainLooper());
        mBtnFormat = (Button) findViewById(R.id.btn_format);
        waveView = (WaveView) findViewById(R.id.wave);
        mSdAvaSize = (TextView) findViewById(R.id.tv_userable);
        mSdcardSize = (TextView) findViewById(R.id.tv_app_version_desc);
        setSdcardSize(getIntent().getLongExtra(TOTAL, 0), getIntent().getLongExtra(LEFT, 0), getIntent().getLongExtra(DIR, 0));
        waveView.setBorder(10, Color.parseColor("#ecf2f9"));
        waveView.setWaveColor(Color.parseColor("#8ab7ef"), Color.parseColor("#1f77e5"));
        waveView.setShapeType(WaveView.ShapeType.CIRCLE);
        initView();
        mDialog = new ProgressDialog(SdActivity.this);
        mDialog.setTitle(R.string.hint);
        mDialog.setMessage(getString(R.string.formating));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        if (RemoteCameraConnectManager.supportWebsocket())
            CarWebSocketClient.instance().registerCallback(this);
        formatDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.format_sdcard)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            isFormating = true;
                            JSONObject jso = new JSONObject();
                            jso.put("f", "set");
                            JSONObject items = new JSONObject();
                            items.put("format", true);
                            jso.put("sdcard", items);
                            HttpRequestManager.instance().requestWebSocket(jso.toString());
                            mDialog.show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (CarWebSocketClient.instance() != null)
            CarWebSocketClient.instance().unregisterCallback(this);
    }


    protected void initView() {
        mBtnFormat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                formatSD();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWaveHelper.start();
    }

    public void setSdcardSize(final long total, final long left, final long dvrdir) {
        String tips = String.format(
                getResources().getString(R.string.sdcard_storage_info),
                Formatter.formatFileSize(this, total),
                Formatter.formatFileSize(this, total - left));
        mSdcardSize.setText(tips);
        mSdAvaSize.setText(Formatter.formatFileSize(this, left));
        float level = 0f;
        if (total != 0) {
            level = left / (float) total;
        }
        mWaveHelper = new WaveHelper(waveView, level);
    }

    private void formatSD() {
        formatDialog.show();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showToast(getString(R.string.no_connect));
                finish();
            }
        });

    }

    @Override
    public void onError(Exception ex) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();
            }
        });
    }

    @Override
    public void onSetSerialNo(String serial) {

    }

    @Override
    public void onSetAbilityStatue(String ability) {

    }

    @Override
    public void onSetVolumeStatue(int min, int max, int current) {

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
    public void onSetDVRSDcardStatus(final boolean mount) {
        if (isFormating) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!mount) {
                    if (formatDialog.isShowing()) {
                        formatDialog.dismiss();
                    }
                    showToast(getString(R.string.sd_unmount));
                    mBtnFormat.setEnabled(false);
                    mBtnFormat.setTextColor(getResources().getColor(R.color.gray));
                } else {
                    mBtnFormat.setEnabled(true);
                    mBtnFormat.setTextColor(getResources().getColor(R.color.volume_stroke));
                }
            }
        });
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
    public void onSetBrightnessPercent(int percent) {

    }

    @Override
    public void onSetAutoSleepTime(int time) {

    }

    @Override
    public void onGsensorSensity(int sensity) {

    }

    @Override
    public void onGsensorWakeup(int enable) {

    }

    @Override
    public void onGsensorLock(int enable) {

    }

    @Override
    public void onSoftApConfig(String ssid, String pwd) {

    }

    @Override
    public void onDvrSaveTime(int time) {

    }

    @Override
    public void onDvrMode(String mode) {

    }

    @Override
    public void onDvrLanguage(String lan) {

    }

    @Override
    public void onDvrMute(boolean mute) {

    }

    @Override
    public void onDvrGps(String show) {

    }

    @Override
    public void onSdcardSize(long total, long left, long dvrdir) {
        if (!isFormating) {
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();
                Toast.makeText(SdActivity.this, R.string.sd_format_ok, Toast.LENGTH_SHORT).show();
                RemoteCameraConnectManager.instance().refreshall();
                finish();
            }
        });
    }

    @Override
    public void onUserList(String serialNum, int cloudID, ArrayList<UserItem> list) {

    }

    @Override
    public void onRecordStatus(boolean start, int num, int time) {

    }

    @Override
    public void onMobileStatus(String imei, boolean ready, int dBm, boolean enable, boolean connected, int type, long usage, boolean registered, String flag) {

    }

    @Override
    public void onSatellites(boolean enabled, int num, long timestamp, String nmea) {

    }

    @Override
    public void onUpdate(int percent, String version) {

    }

    @Override
    public void onCpuInfo(double cpuTemp, double pmuTemp, int core, int freq, boolean isFull, boolean isAccOn) {

    }

    @Override
    public void onGsensor(float x, float y, float z, boolean passed) {

    }

    @Override
    public void onAdas(String key, boolean value) {

    }

    @Override
    public void onEDog(int value) {

    }
}
