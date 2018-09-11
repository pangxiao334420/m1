package com.goluk.a6.control;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.a6.control.browser.FileInfo;
import com.goluk.a6.control.dvr.RemoteCameraConnectManager;
import com.goluk.a6.control.dvr.UserItem;
import com.goluk.a6.control.util.HttpRequestManager;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PasswordActivity extends BaseActivity implements CarWebSocketClient.CarWebSocketClientCallback {

    private EditText mSsidName;
    private EditText mPwd;
    private boolean CheckOk = false;
    private String msg = "";
    private ProgressDialog mDialog;
    private Handler mHandler;
    private String ssName;
    private CheckTextWatcher watcher = new CheckTextWatcher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.softap_dialog);
        mHandler = new Handler(Looper.getMainLooper());
        initView();
        ssName = getIntent().getStringExtra("ssidName");
        mDialog = new ProgressDialog(this);
        mDialog.setTitle(R.string.hint);
        mDialog.setMessage(getString(R.string.setting));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        if (RemoteCameraConnectManager.supportWebsocket())
            CarWebSocketClient.instance().registerCallback(this);
    }

    protected void initView() {
        setTitle(getString(R.string.modify_pass));
        showBack(true);
        getActionBar().getCustomView().findViewById(R.id.tv_right).setVisibility(View.VISIBLE);
        getActionBar().getCustomView().findViewById(R.id.tv_right).setEnabled(false);
        ((TextView) getActionBar().getCustomView().findViewById(R.id.tv_right)).setTextColor(Color.GRAY);
        ((TextView) getActionBar().getCustomView().findViewById(R.id.tv_right)).setText(R.string.finish);
        getActionBar().getCustomView().findViewById(R.id.tv_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });
        //获取布局中的控件
        mSsidName = (EditText) findViewById(R.id.edit_username);
        mPwd = (EditText) findViewById(R.id.edit_password);
        mSsidName.setText("");
        mSsidName.setSelectAllOnFocus(true);
        mPwd.setText("");
        mPwd.setSelectAllOnFocus(true);
        mSsidName.addTextChangedListener(watcher);
        mPwd.addTextChangedListener(watcher);
    }

    public void changePassword() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        if (!CheckOk) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return;
        }
        String ssid = mSsidName.getText().toString();
        String pwd = mPwd.getText().toString();
        mDialog.show();
        try {
            JSONObject jso = new JSONObject();
            jso.put("f", "set");
            JSONObject items = new JSONObject();
            items.put("pwd", pwd);
            items.put("ssid", ssName);
            jso.put("softap", items);
            HttpRequestManager.instance().requestWebSocket(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (CarWebSocketClient.instance() != null)
            CarWebSocketClient.instance().unregisterCallback(this);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mDialog.isShowing()) {
                    Toast.makeText(PasswordActivity.this, R.string.setting_ok, Toast.LENGTH_SHORT).show();
                } else {
                    showToast(getString(R.string.no_connect));
                }
                mDialog.dismiss();
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
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();
                Toast.makeText(PasswordActivity.this, R.string.setting_ok, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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


    private class CheckTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String pass = mSsidName.getText().toString();
            String pass2 = mPwd.getText().toString();
            CheckOk = false;
            if (TextUtils.isEmpty(pass)) {
                setPass(false);
                return;
            }
            if (TextUtils.isEmpty(pass2)) {
                setPass(false);
                return;
            }

            if (pass.length() < 8 || pass.length() > 15) {
                setPass(false);
                msg = getString(R.string.input_password);
                return;
            }

            if (pass2.length() < 8 || pass2.length() > 15) {
                setPass(false);
                msg = getString(R.string.input_password);
                return;
            }

            if (!pass.equals(pass2)) {
                setPass(true);
                msg = getString(R.string.password_diff);
                return;
            }
            CheckOk = true;
            msg = "";
            setPass(true);
        }
    }


    private void setPass(boolean value) {
        if (value) {
            ((TextView) getActionBar().getCustomView().findViewById(R.id.tv_right)).setTextColor(Color.BLUE);
        } else {
            ((TextView) getActionBar().getCustomView().findViewById(R.id.tv_right)).setTextColor(Color.GRAY);
        }
        getActionBar().getCustomView().findViewById(R.id.tv_right).setEnabled(value);
    }
}
