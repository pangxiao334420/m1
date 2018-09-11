
package com.goluk.a6.control.dvr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.a6.control.CarPreviewActivity;
import com.goluk.a6.control.Config;
import com.goluk.a6.control.R;
import com.goluk.a6.control.browser.FileInfo;
import com.goluk.a6.control.browser.RemoteFileActivity;
import com.goluk.a6.control.util.DownloadTask;
import com.goluk.a6.control.util.HttpRequestManager;
import com.goluk.a6.control.util.HttpDownloadManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class QuickVoiceFragment extends RelativeLayout implements IShot {

    private static final String TAG = "CarSvc_QuickVoiceFrag";

    private View mStartRecordView;
    private View mMessageView;
    private View mLivingButton;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_LOOP_FOR_MOBILE_SIGNAL) {
                obtainMobileSignal();
                loopGetMobileSignal();
            }
        }
    };
    private AudioManager mAudioManager;
    private PowerManager.WakeLock mWakeLock;
    private String mInputAddress;
    LinearLayout mVoiceLayout, mMapLayout;
    private TextView mTvShow;
    private TextView mTvCameType;
    AnimationDrawable shoting;
    private RelativeLayout mRlTop;

    private View mViewBg;
    private View mViewFront;
    Animation connectingAnimation;
    Animation mRecordingStatusAnimation;
    ImageView mRecordingView, mSimReady, mSignalView;
    TextView mNetworkType, mDvrFile, mDvrSetting, mSatellite, mRecordingStatus, mTvGpsStrength;

    public IShot getmShotListener() {
        return mShotListener;
    }

    public void setmShotListener(IShot mShotListener) {
        this.mShotListener = mShotListener;
    }

    private IShot mShotListener;

    public QuickVoiceFragment(Context context) {
        super(context);
        initView();
    }

    public QuickVoiceFragment(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public QuickVoiceFragment(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public void setHeadless(boolean headless) {
        if (headless) {
            mVoiceLayout.setVisibility(View.GONE);
            mMapLayout.setVisibility(View.GONE);
        } else {
            mVoiceLayout.setVisibility(View.VISIBLE);
            mMapLayout.setVisibility(View.VISIBLE);
        }
    }

    public void onDestroy() {
    }

    public void onPause() {

    }

    public void onResume() {

    }

    public void refresh() {
        syncFile();
    }

    public void setSyncFile(final String path, final String type, final List<FileInfo> list) {
        Log.i(TAG, "setSyncFile : path = " + path + ",list = " + list);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean syncFileAuto = true; //sp.getBoolean(SettingView.KEY_SYNC_CAPTURE, true);
        if (type.equals("new")) {
            if (syncFileAuto) {
                for (FileInfo fi : list) {
                    if (!fi.isDirectory)
                        downloadFile(fi);
                }
            }
        } else if (type.equals("all")) {
            if (syncFileAuto) {
                for (FileInfo fi : list) {
                    String filePath = fi.path + fi.name;
                    String pathName = null;
                    pathName = Config.CARDVR_PATH + filePath;
                    if (fi.name.endsWith(".ts"))
                        pathName = pathName.substring(0, pathName.lastIndexOf(".ts")) + ".mp4";
                    Log.i(TAG, "pathName = " + pathName);
                    File file = new File(pathName);
                    if (!file.exists()) {
                        if (!fi.isDirectory)
                            downloadFile(fi);
                    }
                }
            }


        }

    }

    public boolean onBackPressed() {
        Log.i(TAG, "onBackPressed()");
        return false;
    }

    public void startNavi(double latitude, double longitude, String name) {
        if (RemoteCameraConnectManager.getCurrentServerInfo() != null) {
            if (RemoteCameraConnectManager.supportWebsocket()) {
                try {
                    JSONObject jso = new JSONObject();
                    jso.put("action", Config.ACTION_NAVI);
                    jso.put("latitude", latitude);
                    jso.put("longitude", longitude);
                    jso.put("name", name);
                    jso.toString();
                    Log.i(TAG, "jso.toString() = " + jso.toString());
                    HttpRequestManager.instance().requestWebSocket(jso.toString());
                } catch (JSONException e) {

                    e.printStackTrace();
                }
            } else {
                String url = "http://" + RemoteCameraConnectManager.HTTP_SERVER_IP + ":" + RemoteCameraConnectManager.HTTP_SERVER_PORT +
                        "/cgi-bin/Config.cgi?action=navi&property=latitude&value=" + latitude + "&property=longitude&value=" + longitude +
                        "&property=name&value=" + name;
                Log.i(TAG, "url = " + url);
                HttpRequestManager.instance().requestHttp(url, new HttpRequestManager.OnHttpResponseListener() {

                    @Override
                    public void onHttpResponse(String result) {
                        Log.i(TAG, "result = " + result);
                    }

                });
            }
        } else {
            Toast.makeText(getContext(), R.string.no_connect, Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.quick_voice_fragment, this);
        connectingAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.alpha_scale_animation);
        mRecordingStatusAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.alpha_animation);
        mTvShow = (TextView) findViewById(R.id.start_record);
        mTvCameType = (TextView) findViewById(R.id.recording_cam);
        mVoiceLayout = (LinearLayout) findViewById(R.id.start_record_container);
        mRlTop = (RelativeLayout) findViewById(R.id.rl_top);
        mMessageView = findViewById(R.id.input_text);
        mMessageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (RemoteCameraConnectManager.getCurrentServerInfo() == null) {
                    Toast.makeText(getContext(), R.string.no_connect, Toast.LENGTH_SHORT).show();
                    return;
                }

                int type = RemoteFileActivity.TYPE_REMOTE_FILE_CAPTURE;
                Intent intent = new Intent(getContext(), RemoteFileActivity.class);
                intent.putExtra(RemoteFileActivity.KEY_TYPE_REMOTE_FILE, type);
                getContext().startActivity(intent);
            }

        });

        mLivingButton = findViewById(R.id.camera_living);
        mLivingButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                int type = RemoteFileActivity.TYPE_REMOTE_FILE_CAPTURE;
                Intent intent = new Intent(getContext(), RemoteFileActivity.class);
                intent.putExtra(RemoteFileActivity.KEY_TYPE_REMOTE_FILE, type);
                if (!RemoteCameraConnectManager.instance().isConnected()) {
                    intent.putExtra(RemoteFileActivity.KEY_TYPE_REMOTE_FILE_FROM, true);
                    return; //沒有鏈接設備时，不做任何操作
                }
                ((CarPreviewActivity) getContext()).showRemote = true;
                getContext().startActivity(intent);
            }
        });
        mViewBg = findViewById(R.id.start_record_bg);
        mViewFront = findViewById(R.id.start_record_animation);

        shoting = (AnimationDrawable) getResources().getDrawable(
                R.drawable.shot_anim);
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());
        mTvShow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDisConnected();
                if (mShotListener != null) {
                    mShotListener.startShot();
                    startAnim();
                }
            }
        });

        mRecordingStatus = (TextView) findViewById(R.id.recording_status);
        mRecordingStatus.setText(R.string.record_stop);

        mSimReady = (ImageView) findViewById(R.id.mobile_signal_view);
        mRecordingView = (ImageView) findViewById(R.id.recording_view);
        mNetworkType = (TextView) findViewById(R.id.mobile_4g_view);
//        mNetworkType.setVisibility(View.INVISIBLE);
//        mRecordingView.setVisibility(View.INVISIBLE);
        mSatellite = (TextView) findViewById(R.id.satellite_num);
        mSignalView = (ImageView) findViewById(R.id.mobile_signal_strength);
        mSignalView.setVisibility(View.GONE);
        mTvGpsStrength = (TextView) findViewById(R.id.gps_strength);
        mTvGpsStrength.setVisibility(View.GONE);
    }

    public void showConnected() {
        shoting.stop();
        mRlTop.setVisibility(VISIBLE);
//        mTvShow.setBackground(getContext().getResources().getDrawable(R.drawable.btn_start_record));
        mTvShow.setEnabled(true);
    }

    public void showDisConnected() {
        mRlTop.setVisibility(GONE);
        mTvShow.setEnabled(false);
        stopAnim();
        stopRecordStatusUI();
//        mTvShow.setBackground(shoting);
//        shoting.start();
    }

    //下载文件
    private void downloadFile(FileInfo info) {
        if (!info.isDirectory) {
            String filePath = info.path + info.name;

            //如果需要下载的文件已经正在下载，先取消下载
            DownloadTask old = HttpDownloadManager.instance().getDownloadTask(filePath);
            if (old != null) {
                HttpDownloadManager.instance().cancelDownload(old);
            }
            DownloadTask task = new DownloadTask(filePath, null);
            task.setDeleteAfterDownload(true);
            HttpDownloadManager.instance().requestDownload(task);
        }
    }

    private void syncFile() {
        if (RemoteCameraConnectManager.supportWebsocket()) {
            try {
                JSONObject jso = new JSONObject();
                jso.put("action", Config.ACTION_SYNC_FILE);
                Log.i(TAG, "jso.toString() = " + jso.toString());
                HttpRequestManager.instance().requestWebSocket(jso.toString());
            } catch (JSONException e) {

                e.printStackTrace();
            }
        }
    }

    @Override
    public void startShot() {
    }

    private void startAnim() {
        mViewBg.setVisibility(VISIBLE);
        mViewFront.setVisibility(VISIBLE);
        mViewFront.startAnimation(connectingAnimation);
    }

    private void stopAnim() {
        mViewFront.clearAnimation();
        connectingAnimation.cancel();
        connectingAnimation.reset();
        mViewBg.setVisibility(GONE);
        mViewFront.setVisibility(GONE);
    }

    @Override
    public void shotFinish() {
        showConnected();
        stopAnim();
    }


    public static String formatUnit(int i) {
        return String.format("%02d", i);
    }

    public static String secondsToTime(int seconds) {
        if (seconds < 1)
            return "00:00:00";
        if (seconds >= 360000)
            return "99:59:59";

        StringBuilder sb = new StringBuilder();
        int hour = seconds / 3600;
        int min = (seconds % 3600) / 60;
        int sec = (seconds % 3600) % 60;

        return formatUnit(hour) + ":" + formatUnit(min) + ":" + formatUnit(sec);
    }

    public void setRecordingStatus(final boolean started, final int num, final int time) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                if (started) {
                    mRecordingView.setImageResource(R.drawable.shape_dot);
                    int resId = R.string.record_front;
                    if (num == 1) resId = R.string.record_rear;
                    else if (num == 2) resId = R.string.record_both;
                    mTvCameType.setText(getResources().getString(resId));
                    mRecordingStatus.setText(secondsToTime(time));
                    mRecordingView.startAnimation(mRecordingStatusAnimation);
                } else {
                    stopRecordStatusUI();
                }
            }
        });
    }

    private void stopRecordStatusUI() {
        mRecordingView.setImageResource(R.drawable.shape_dot_gray);
        mRecordingStatus.setText(secondsToTime(0));
        mRecordingStatusAnimation.cancel();
        mRecordingStatusAnimation.reset();
        mRecordingView.clearAnimation();
    }

    public void setNetworkType(final boolean ready, final boolean connected, final int dbm) {
        mHandler.post(new Runnable() {

            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if (!ready) {
                    mSimReady.setImageResource(R.drawable.nosim);
                    mNetworkType.setText(R.string.no_sim_card);
                    mNetworkType.setVisibility(VISIBLE);
//                    mSignalView.setVisibility(View.GONE);
                } else {
                    if (connected) {
                        mSimReady.setImageResource(R.drawable.sim_connected);
//                        mSignalView.setVisibility(View.VISIBLE);
                        mNetworkType.setVisibility(GONE);
//                        showSignalStrength(dbm);
                    } else {
                        mSimReady.setImageResource(R.drawable.mobile_signal);
                        mNetworkType.setText(R.string.no_connection);
//                        mSignalView.setVisibility(View.GONE);
                        mNetworkType.setVisibility(VISIBLE);
                    }
                }
//                loopGetMobileSignal();
            }
        });
    }

    public void setSatellites(final int num, final String nmea) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mSatellite.setText("" + num);
//                if (nmea.startsWith(FORMAT_GPGSV)) {
//                    mTvGpsStrength.setText(parseNmea(nmea) + "%");
//                }
            }
        });
    }

    private static final int MSG_LOOP_FOR_MOBILE_SIGNAL = 20001;
    private static final int MSG_LOOP_FOR_MOBILE_MILLS = 3 * 1000;

    private void loopGetMobileSignal() {
        mHandler.removeMessages(MSG_LOOP_FOR_MOBILE_SIGNAL);
        mHandler.sendEmptyMessageDelayed(MSG_LOOP_FOR_MOBILE_SIGNAL, MSG_LOOP_FOR_MOBILE_MILLS);
    }

    public void releaseLooper() {
        mHandler.removeCallbacksAndMessages(null);
    }

    private void obtainMobileSignal() {
        if (RemoteCameraConnectManager.supportWebsocket()) {
            if (RemoteCameraConnectManager.supportNewSetting()) {
                try {
                    JSONObject jso = new JSONObject();
                    jso.put("f", "get");
                    JSONArray items = new JSONArray();
                    items.put("mobile");
                    jso.put("what", items);
                    HttpRequestManager.instance().requestWebSocket(jso.toString());
                    Log.e(TAG, "obtainMobileSignal: " + jso.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 5格：≥ -91 dBm
     * 4格：≥ -101 dBm
     * 3格：≥ -103 dBm
     * 2格：≥ -107 dBm
     * 1格：≥ -113 dBm
     * 0格：＜ -113 dBm
     *
     * @param dbm
     */
    private void showSignalStrength(int dbm) {
        int drawableRes = -1;
        if (dbm < -107) {
            drawableRes = R.drawable.icon_network_00;
        } else if (dbm >= -107 && dbm < -103) {
            drawableRes = R.drawable.icon_network_01;
        } else if (dbm >= -103 && dbm < -101) {
            drawableRes = R.drawable.icon_network_02;
        } else if (dbm >= -101 && dbm < -91) {
            drawableRes = R.drawable.icon_network_03;
        } else if (dbm >= -91) {
            drawableRes = R.drawable.icon_network_04;
        }
        if (drawableRes != -1) {
            mSignalView.setImageResource(drawableRes);
        } else {
            mSignalView.setVisibility(View.GONE);
        }
    }

    private static final String FORMAT_GPGSV = "$GPGSV";

    /**
     * $GPGSV,4,2,14,50,35,140,42,16,72,018,26,06,09,040,36,07,17,043,36*78
     * <p>
     * 4,2,14,
     * 50,35,140,42,
     * 16,72,018,26,
     * 06,09,040,36,
     * 07,17,043,36
     * 78
     *
     * @param nmea
     */
    private int parseNmea(String nmea) {
        String data = nmea.substring(FORMAT_GPGSV.length() + 1);
        String[] arr = data.split(",");
        if (arr.length < 3) {
            return 0;
        }
        int number = Integer.parseInt(arr[0]);
        int words = Integer.parseInt(arr[1]);
        String visibleStr = arr[2];
        int visible = 0;
        if (visibleStr.contains("*")) {
            if (visibleStr.indexOf("*") == 0) {
                return 0;
            }
            visible = Integer.parseInt(visibleStr.substring(0, visibleStr.indexOf("*")));
        } else {
            visible = Integer.parseInt(visibleStr);
        }
        Log.e(TAG, "parseNmea: " + visibleStr + "----" + visible);
        if (visible <= 0) {
            return 0;
        }
        int num = (arr.length - 3) / 4;
        int sum = 0;
        if (num > 0) {
            for (int index = 6; index < arr.length; index += 4) {
                if (arr[index].trim().length() <= 0)
                    continue;
                try {
                    if (index != arr.length - 1) {
                        sum += Float.parseFloat(arr[index]);
                    } else {
                        sum += Float.parseFloat(arr[index].substring(0, arr[index].indexOf("*")));
                    }
                } catch (NumberFormatException e) {
                    return 0;
                }

            }
            return sum / num;
        }
        return 0;

    }


}