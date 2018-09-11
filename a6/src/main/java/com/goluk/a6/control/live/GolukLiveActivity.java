package com.goluk.a6.control.live;

import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goluk.a6.common.util.BlurUtil;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.control.R;
import com.goluk.a6.control.util.AddressConvert;
import com.goluk.a6.control.util.SignalUtil;
import com.goluk.a6.control.util.Util;
import com.goluk.a6.cpstomp.CPStompClient;
import com.goluk.a6.cpstomp.MessageResult;
import com.goluk.a6.cpstomp.SignalInfo;
import com.goluk.a6.cpstomp.SocketConst;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.DeviceStatusBean;
import com.goluk.a6.http.request.DeviceStatusRequest;
import com.goluk.a6.internation.GolukUtils;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.player.KSYTextureView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import likly.dollar.$;

/**
 * Created by goluk_lium on 2018/3/29.
 */

public class GolukLiveActivity extends BaseActivity
        implements CPStompClient.StompConnListener, ILiveView, View.OnClickListener, Handler.Callback, IRequestResultListener {

    private static final int REQUEST_CODE_DEVICES_STATUS = 10001;
    private static final int MSG_BEGIN_SEND_CMD_LIVE_MESSAGE = 10002;
    private static final int MSG_BEGIN_SEND_CMD_WAKE_UP_MESSAGE = 10004;
    private static final int MSG_DISCONNECT_LIVE = 10003;
    private static final int MSG_TCP_CONNECTION_CLOSED = 10005;
    private static final int MSG_RETRY_CONNECT_WEB_SOCKET = 10006;

    public static final int DEFAULT_CAMERA_NO = 0;
    private static final int SEND_DISCONNECT_MSG_DELAY_MILLS = 30 * 1000;
    private static final int DEFAULT_ANIMATION_DURATION = 300;
    private static final int UPDATE_POSITION_INTERVAL_TIME = 10 * 1000;
    private static final int TIME_OUT = 30 * 1000;
    private static final int SEND_CMD_LIVE_TIME_OUT = 30 * 1000;
    private static final int SEND_CMD_WAKE_UP_TIME_OUT = 30 * 1000;

    private static final int WEB_SOCKET_RETRY_CONNECT_TIMES = 3;

    private CPStompClient mStompClient = new CPStompClient();
    private int clientConnectRetryTimes;
    //如果是异常关闭 置false
    private boolean shouldWebSocketClosed = false;
    private RelativeLayout liveView;
    private ImageView mLoadingView;
    private ImageView mCameraSwitch;
    private ImageView mDisplayFullView, mExitFullView;
    private TextView textLiveInfo;//直播过程，如断开连接，则提示****
    private TextView textPositionState, textPositionInfo;//
    private TextView textLiveTitle;
    private RelativeLayout mLayoutTitle;
    private RelativeLayout rLayoutLivePanel;
    private RelativeLayout rLayoutLiveContent;
    private RelativeLayout rLayoutMask;
    private LinearLayout lLayoutDialog;
    private LinearLayout lLayoutRoot;
    private boolean isSwitchingCamera = false;
    private LinearLayout mLayoutSignalInfo, mLayoutSignalInfoFullscreen;
    private ImageView mIvSignalStrength, mIvSignalStrengthFullscreen;
    private TextView mTvNetSpeed, mTvNetSpeedFullscreen;

    private LinearLayout.LayoutParams mLayoutParamsDialog, mLayoutParamsVideo;
    //0 横屏播放 1 竖屏播放
    private int currentDisplayState = Configuration.ORIENTATION_PORTRAIT;

    private int mCameraNumber;
    private int mCurrentCameraNo = DEFAULT_CAMERA_NO;

    private String mCarIMEI;//当前车辆IMEI
    private int mCurrentCarState;//当前车辆状态
    private String mLastLocation;//最新位置
    private String mUserName;
    private String mUID;
    private LiveControl mLiveController;

    private Handler mHandler;
    private boolean comeBackFromRestart = false;
    private boolean shouldReloadLive = false;
    private String mDataSource;
    private boolean isTcponnectionClosed = false;

    private AddressConvert mAddressConvert;

    private DeviceStatusRequest mDeviceStatusRequest;

    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            if (iMediaPlayer == null) return;
            Log.e(TAG, "IMediaPlayer w: " + iMediaPlayer.getVideoWidth() + "  IMediaPlayer h: " + iMediaPlayer.getVideoHeight());
//            updateLayoutSize(iMediaPlayer.getVideoWidth(),iMediaPlayer.getVideoHeight());
            mHandler.removeMessages(MSG_BEGIN_SEND_CMD_LIVE_MESSAGE);
//            mHandler.removeMessages(MSG_TCP_CONNECTION_CLOSED);
            mHandler.removeMessages(MSG_BEGIN_SEND_CMD_WAKE_UP_MESSAGE);
            LivingPlayer.getInstance().getKsyTextureView().setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            comeBackFromRestart = false;
            shouldReloadLive = false;
            isSwitchingCamera = false;
            setHideAnimation(rLayoutMask, DEFAULT_ANIMATION_DURATION);
            LivingPlayer.getInstance().getKsyTextureView().start();
            LiveLoadingDialog.dismiss();
            hideLiveInfoText();
        }
    };

    private void updateLayoutSize(int videoWidth, int videoHeight) {
        KSYTextureView mediaPlayer = LivingPlayer.getInstance().getKsyTextureView();
        if (mediaPlayer == null) {
            return;
        }
        int playerWidth = mediaPlayer.getWidth();
        int heightSet = (int) (((float) videoHeight / videoWidth) * playerWidth);
        ViewGroup.LayoutParams layoutParams = mediaPlayer.getLayoutParams();
        layoutParams.height = heightSet;
        mediaPlayer.setLayoutParams(layoutParams);

        ViewGroup.LayoutParams lpControl = rLayoutLiveContent.getLayoutParams();
        lpControl.height = heightSet;
        rLayoutLiveContent.setLayoutParams(lpControl);
    }

    private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {

        }
    };
    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int errCode, int i1) {
            Log.e(TAG, "IMediaPlayer  onError: i = " + errCode + "   i1 = " + i1);
            if (isSwitchingCamera) {
                setHideAnimation(rLayoutMask, DEFAULT_ANIMATION_DURATION);
                isSwitchingCamera = false;
            }
            mHandler.removeMessages(MSG_BEGIN_SEND_CMD_LIVE_MESSAGE);
//            mHandler.removeMessages(MSG_TCP_CONNECTION_CLOSED);
            mHandler.removeMessages(MSG_BEGIN_SEND_CMD_WAKE_UP_MESSAGE);
            LiveLoadingDialog.dismiss();
            if (mLiveController != null) {
                switch (errCode) {
                    case IMediaPlayer.MEDIA_ERROR_DNS_PARSE_FAILED:
                        Log.e(TAG, "IMediaPlayer  onError: MEDIA_ERROR_DNS_PARSE_FAILED");
                        break;
                    case IMediaPlayer.MEDIA_ERROR_CONNECT_SERVER_FAILED:
                        Log.e(TAG, "IMediaPlayer  onError: MEDIA_ERROR_CONNECT_SERVER_FAILED");
                        break;
                    case IMediaPlayer.MEDIA_ERROR_IO: //网络断开
                        Log.e(TAG, "IMediaPlayer  onError: MEDIA_ERROR_IO");
                        break;
                }
                if (mStompClient.isOpend())
                    mLiveController.sendStartLiveCmd(mStompClient, mCarIMEI, mCurrentCameraNo);
                return false;
            }
            updateLiveInfoText(R.string.play_error);
            return false;
        }
    };
    private IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            switch (i) {
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    LiveLoadingDialog.show();
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    LiveLoadingDialog.dismiss();
                    break;
            }
            return false;
        }
    };

    private void initView() {
        liveView = (RelativeLayout) findViewById(R.id.media_player_view);
        mLoadingView = (ImageView) findViewById(R.id.iv_loading);
        textLiveInfo = (TextView) findViewById(R.id.player_info);
        textLiveTitle = (TextView) findViewById(R.id.mtv_title);
        textPositionInfo = (TextView) findViewById(R.id.tv_position);
        textPositionState = (TextView) findViewById(R.id.tv_p);
        mCameraSwitch = (ImageView) findViewById(R.id.iv_cam_switch);
        mDisplayFullView = (ImageView) findViewById(R.id.iv_display_fullscreen);
        mExitFullView = (ImageView) findViewById(R.id.iv_exit_fullscreen);
        mLayoutTitle = (RelativeLayout) findViewById(R.id.layout_title);
        rLayoutLivePanel = (RelativeLayout) findViewById(R.id.rlayout_live_panel);
        rLayoutLiveContent = (RelativeLayout) findViewById(R.id.live_show);
        lLayoutDialog = (LinearLayout) findViewById(R.id.ll_dialog);
        rLayoutMask = (RelativeLayout) findViewById(R.id.rLayout_mask);
        lLayoutRoot = (LinearLayout) findViewById(R.id.live_root_view);
        mLayoutSignalInfo = (LinearLayout) findViewById(R.id.layout_signal_info);
        mIvSignalStrength = (ImageView) findViewById(R.id.ic_signal_strength);
        mTvNetSpeed = (TextView) findViewById(R.id.net_speed);
        mLayoutSignalInfoFullscreen = (LinearLayout) findViewById(R.id.layout_signal_info_fullscreen);
        mIvSignalStrengthFullscreen = (ImageView) findViewById(R.id.ic_signal_strength_fullscreen);
        mTvNetSpeedFullscreen = (TextView) findViewById(R.id.net_speed_fullscreen);

        LiveLoadingDialog.init(mLoadingView);
        mCameraSwitch.setOnClickListener(this);
        mDisplayFullView.setOnClickListener(this);
        mExitFullView.setOnClickListener(this);
        lLayoutRoot.setOnClickListener(this);

        ViewTreeObserver vto = rLayoutLiveContent.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rLayoutLiveContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setVideoLayoutSize();
            }
        });
        ViewTreeObserver vto2 = lLayoutDialog.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                lLayoutDialog.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mLayoutParamsDialog = (LinearLayout.LayoutParams) lLayoutDialog.getLayoutParams();
            }
        });
    }

    /**
     * 设置播放器宽高
     */
    private void setVideoLayoutSize() {
        int width = rLayoutLiveContent.getWidth();
        if (width == -1) {
            return;
        }
        int height = width * 9 / 16; //默认视频分辨率为 960X546  也就是16:9

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) rLayoutLiveContent.getLayoutParams();
        layoutParams.height = height;
        rLayoutLiveContent.setLayoutParams(layoutParams);

        mLayoutParamsVideo = layoutParams;
    }

    private void startToPlay(String url) {
        LiveLoadingDialog.show();
        LivingPlayer.getInstance().init(this);
        liveView.addView(LivingPlayer.getInstance().getKsyTextureView());
        LivingPlayer.getInstance().getKsyTextureView().setOnPreparedListener(mOnPreparedListener);
        LivingPlayer.getInstance().getKsyTextureView().setOnErrorListener(mOnErrorListener);
        LivingPlayer.getInstance().getKsyTextureView().setOnInfoListener(mOnInfoListener);
        LivingPlayer.getInstance().getKsyTextureView().setOnCompletionListener(mOnCompletionListener);
        LivingPlayer.getInstance().getKsyTextureView().setOnTouchListener(mTouchListener);
        LivingPlayer.getInstance().getKsyTextureView().setVolume(1.0f, 1.0f);
        LivingPlayer.getInstance().getKsyTextureView().setBufferSize(15);
        LivingPlayer.getInstance().getKsyTextureView().setBufferTimeMax(2f);
        LivingPlayer.getInstance().getKsyTextureView().setTimeout(20, 60);//(prepareTimeout,readTimeout)

        try {
            LivingPlayer.getInstance().getKsyTextureView().setDataSource(url);
            LivingPlayer.getInstance().getKsyTextureView().shouldAutoPlay(true);
            LivingPlayer.getInstance().getKsyTextureView().prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LivingPlayer.getInstance().getKsyTextureView().setKeepScreenOn(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goluk_live);
        if (getActionBar() != null) getActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        mLiveController = new GolukLiveController(this);
        mHandler = new Handler(Looper.getMainLooper(), this);
        obtainIntentData();
        initView();
        LiveLoadingDialog.show();
        if (!GolukUtils.isNetworkConnected(this)) {
            updateLiveInfoText(R.string.user_net_unavailable);
            LiveLoadingDialog.dismiss();
            return;
        }
        mStompClient.connect(this);
        LivingPlayer.getInstance().init(this);
        mDeviceStatusRequest = new DeviceStatusRequest(REQUEST_CODE_DEVICES_STATUS, this);
        startTimerTask();
        mAddressConvert = new AddressConvert();
    }

    private void obtainIntentData() {
        if (getIntent() != null) {
            mUserName = getIntent().getStringExtra(LiveConstant.KEY_USER_NAME);
            mCarIMEI = getIntent().getStringExtra(LiveConstant.KEY_CAR_IMEI);
            mCurrentCarState = getIntent().getIntExtra(LiveConstant.KEY_CAR_STATE, 0);
            mLastLocation = getIntent().getStringExtra(LiveConstant.KEY_CAR_LOCATION);
            mCameraNumber = getIntent().getIntExtra(LiveConstant.KEY_DEVICE_CAMERA_NUMBER, 1);
        }
    }

    private void exitLive() {
        if (currentDisplayState == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        comeBackFromRestart = true;
        mHandler.removeMessages(MSG_DISCONNECT_LIVE);
        LiveLoadingDialog.show();
        if (mStompClient == null) mStompClient = new CPStompClient();
        if (shouldReloadLive) {
            if (mStompClient.isOpend()) {
                mLiveController.prepareStartLive(mCurrentCarState, mCurrentCameraNo, mStompClient, mCarIMEI);
            } else {
                mStompClient.connect(this);
            }
        } else {
            startToPlay(mDataSource);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (LivingPlayer.getInstance().getKsyTextureView() != null) {
            liveView.removeView(LivingPlayer.getInstance().getKsyTextureView());
            LivingPlayer.getInstance().getKsyTextureView().pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.sendEmptyMessageDelayed(MSG_DISCONNECT_LIVE, SEND_DISCONNECT_MSG_DELAY_MILLS);
    }

    @Override
    public void stompOpened() {
        clientConnectRetryTimes = 0;
        if (mStompClient != null) mStompClient.topic(mCarIMEI);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLiveController.prepareStartLive(mCurrentCarState, mCurrentCameraNo, mStompClient, mCarIMEI);
            }
        }, 500);
    }

    @Override
    public void stompClosed() {
        Log.e(TAG, "-------------- stompClosed: ----------------");
        LiveLoadingDialog.dismiss();
        if (GolukUtils.isNetworkConnected(this)) {
            updateLiveInfoText(R.string.user_net_unavailable);
            return;
        }
        clientConnectRetryTimes += 1;
        if (clientConnectRetryTimes <= WEB_SOCKET_RETRY_CONNECT_TIMES && mHandler != null) {
            mHandler.sendEmptyMessage(MSG_RETRY_CONNECT_WEB_SOCKET);
            return;
        }
        updateLiveInfoText(R.string.live_closed);
    }

    @Override
    public void stompError(Exception e) {
        Log.e(TAG, "stompError: " + e.getLocalizedMessage());
    }

    private static final String TAG = "GolukLiveActivity";

    @Override
    public void onRcvMsg(String bean) {
        Log.e(TAG, "onRcvMsg: " + bean);
        mLiveController.onLive(bean);
        handleSignalInfoMsg(bean);
    }

    private void handleSignalInfoMsg(String msg) {
        MessageResult result = $.json().toBean(msg, MessageResult.class);
        MessageResult.Data data = $.json().toBean(result.data, MessageResult.Data.class);
        if (TextUtils.equals(data.cmd, SocketConst.CMD_SIGNAL_STRANGTH_ACK)) {
            // 信号强度和网速
            if (result.code == 0) {
                if (data.code == 0) {
                    SignalInfo signalInfo = $.json().toBean(data.data, SignalInfo.class);
                    if (signalInfo != null)
                        updateSignalInfo(signalInfo);
                }
            }
        }
    }

    /**
     * 更新显示信号强度和网速
     */
    private void updateSignalInfo(SignalInfo signalInfo) {
        int orientation = getRequestedOrientation();
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mLayoutSignalInfo.setVisibility(View.GONE);
            mLayoutSignalInfoFullscreen.setVisibility(View.VISIBLE);
            mTvNetSpeedFullscreen.setText(Util.getFormatNetSpeedSize(signalInfo.speed));
            int simSignalIcon = SignalUtil.getSimStrengthBlueIconByLevel(signalInfo.level);
            if (simSignalIcon != -1)
                mIvSignalStrengthFullscreen.setImageResource(simSignalIcon);
        } else {
            mLayoutSignalInfo.setVisibility(View.VISIBLE);
            mLayoutSignalInfoFullscreen.setVisibility(View.GONE);
            mTvNetSpeed.setText(Util.getFormatNetSpeedSize(signalInfo.speed));
            int simSignalIcon = SignalUtil.getSimStrengthBlueIconByLevel(signalInfo.level);
            if (simSignalIcon != -1)
                mIvSignalStrength.setImageResource(simSignalIcon);
        }
    }

    @Override
    public void onReturn(Void aVoid) {

    }

    private void updateLiveInfoText(@StringRes int res) {
        textLiveInfo.setVisibility(View.VISIBLE);
        textLiveInfo.setText(res);
    }

    private void hideLiveInfoText() {
        textLiveInfo.setVisibility(View.GONE);
    }

    /**
     * 开始直播，显示 位置信息，摄像头,title
     */
    @Override
    public void onPrepareLive() {
        if (mCurrentCarState == LiveConstant.STATE_OFFLINE
                || mCurrentCarState == LiveConstant.STATE_SLEEP) {
            textPositionState.setText(R.string.most_recent_location);
        } else if (mCurrentCarState == LiveConstant.STATE_ONLINE) {
            textPositionState.setText(R.string.position);
        }
        if (mLastLocation != null && !TextUtils.isEmpty(mLastLocation)) {
            textPositionInfo.setText(mLastLocation);
        }
        if (mCameraNumber == 2) {
            mCameraSwitch.setVisibility(View.VISIBLE);
        } else {
            mCameraSwitch.setVisibility(View.GONE);
        }
        if (mUserName != null && !TextUtils.isEmpty(mUserName)) {
            textLiveTitle.setText(mUserName + " " + getString(R.string.live));
        }
    }

    @Override
    public void onStartLive(String url) {
        mDataSource = url;
        startToPlay(url);
    }

    @Override
    public void showDeviceOffline() {
        updateLiveInfoText(R.string.device_not_online);
        LiveLoadingDialog.dismiss();
    }

    @Override
    public void onDeviceWakeup() {
        updateLiveInfoText(R.string.wake_up_device);
        LiveLoadingDialog.dismiss();
    }

    @Override
    public void onDeviceWakeupSuccess() {
        mLiveController.prepareStartLive(mCurrentCarState, mCurrentCameraNo, mStompClient, mCarIMEI);
        hideLiveInfoText();
        LiveLoadingDialog.show();
    }

    @Override
    public void updateDeviceState(int state) {
        this.mCurrentCarState = state;
    }

    @Override
    public void onLiving() {

    }

    @Override
    public void onLiveError() {
        updateLiveInfoText(R.string.play_error);
        LiveLoadingDialog.dismiss();
    }

    @Override
    public void onLiveTimeout() {
        mHandler.sendEmptyMessageDelayed(MSG_BEGIN_SEND_CMD_LIVE_MESSAGE, SEND_CMD_LIVE_TIME_OUT);
    }

    @Override
    public void onWakeUpTimeout() {
        mHandler.sendEmptyMessageDelayed(MSG_BEGIN_SEND_CMD_WAKE_UP_MESSAGE, SEND_CMD_WAKE_UP_TIME_OUT);
    }

    @Override
    public void onLivePause() {

    }

    @Override
    public void onLiveResume() {

    }

    @Override
    public void onLiveStop() {
        updateLiveInfoText(R.string.play_finish);
        LiveLoadingDialog.dismiss();
    }

    @Override
    public void onLiveFinish() {

    }

    @Override
    public void onTcpConnectionClosed() {
//        updateLiveInfoText(R.string.play_finish);
//        LiveLoadingDialog.dismiss();
        updateLiveInfoText(R.string.network_maybe_unreachable);
        Log.e(TAG, "onTcpConnectionClosed--" + getString(R.string.network_maybe_unreachable));
        LiveLoadingDialog.dismiss();
        isTcponnectionClosed = true;
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onServerReceivedWakeupCmd() {
        mHandler.removeMessages(MSG_BEGIN_SEND_CMD_LIVE_MESSAGE);
    }

    @Override
    public void onServerReceivedLiveCmd() {
        mHandler.removeMessages(MSG_BEGIN_SEND_CMD_LIVE_MESSAGE);
    }

    @Override
    protected void onDestroy() {
        if (mLiveController != null) {
            mLiveController.closeLiveConnection(mStompClient);
        }
        LivingPlayer.getInstance().destroy();
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);
        destroyTimerTask();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_cam_switch:
                mCurrentCameraNo = mCurrentCameraNo == 0 ? 1 : 0;
                mLiveController.sendStartLiveCmd(mStompClient, mCarIMEI, mCurrentCameraNo);
                isSwitchingCamera = true;
                KSYTextureView ksyTextureView = LivingPlayer.getInstance().getKsyTextureView();
                if (ksyTextureView.getMediaPlayer() != null && ksyTextureView.getMediaPlayer().getScreenShot() != null) {
                    Bitmap bitmap = BlurUtil.doBlur(ksyTextureView.getMediaPlayer().getScreenShot(), 10, true);
                    if (android.os.Build.VERSION.SDK_INT >= 16) {
                        setBackgroundV16Plus(rLayoutMask, bitmap);
                    } else {
                        setBackgroundV16Minus(rLayoutMask, bitmap);
                    }
                }
                setShowAnimation(rLayoutMask, DEFAULT_ANIMATION_DURATION);
                break;
            case R.id.iv_display_fullscreen:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case R.id.iv_exit_fullscreen:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case R.id.live_root_view:
                onBackPressed();
                break;
        }
    }

    @TargetApi(16)
    private void setBackgroundV16Plus(View view, Bitmap bitmap) {
        view.setBackground(new BitmapDrawable(getResources(), bitmap));

    }

    @SuppressWarnings("deprecation")
    private void setBackgroundV16Minus(View view, Bitmap bitmap) {
        view.setBackgroundDrawable(new BitmapDrawable(bitmap));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        currentDisplayState = newConfig.orientation;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            rLayoutLivePanel.setVisibility(View.GONE);
            mLayoutTitle.setVisibility(View.GONE);
            mLayoutSignalInfoFullscreen.setVisibility(View.VISIBLE);
            mDisplayFullView.setVisibility(View.GONE);
            mExitFullView.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(mTvNetSpeed.getText().toString())) {
                mTvNetSpeedFullscreen.setVisibility(View.VISIBLE);
                mTvNetSpeedFullscreen.setText(mTvNetSpeed.getText().toString());
            }
            mTvNetSpeed.setVisibility(View.INVISIBLE);
            LinearLayout.LayoutParams outLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            outLayoutParams.setMargins(0, 0, 0, 0);
            lLayoutDialog.setLayoutParams(outLayoutParams);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(displayMetrics.widthPixels, displayMetrics.heightPixels);
            rLayoutLiveContent.setLayoutParams(layoutParams);

            params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(params);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            //showStatusBar();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(params);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            rLayoutLivePanel.setVisibility(View.VISIBLE);
            mLayoutTitle.setVisibility(View.VISIBLE);
            mLayoutSignalInfoFullscreen.setVisibility(View.GONE);
            mDisplayFullView.setVisibility(View.VISIBLE);
            mExitFullView.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(mTvNetSpeedFullscreen.getText().toString())) {
                mTvNetSpeed.setVisibility(View.VISIBLE);
                mTvNetSpeed.setText(mTvNetSpeedFullscreen.getText().toString());
            }
            mTvNetSpeedFullscreen.setVisibility(View.INVISIBLE);
            lLayoutDialog.setLayoutParams(mLayoutParamsDialog);
            rLayoutLiveContent.setLayoutParams(mLayoutParamsVideo);
        }
    }

    private boolean mTouching;
    private boolean isShowStatusBar = false;
    //事件监听
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            KSYTextureView mVideoView = LivingPlayer.getInstance().getKsyTextureView();
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mTouching = false;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    mTouching = true;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
                case MotionEvent.ACTION_UP:
                    if (!mTouching) {
                        dealTouchEvent();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private void dealTouchEvent() {
    }

    private void hideStatusBar() {
        isShowStatusBar = false;
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(params);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void showStatusBar() {
        isShowStatusBar = true;
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setAttributes(params);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_DISCONNECT_LIVE:
                if (mLiveController != null) {
                    mLiveController.closeLiveConnection(mStompClient);
                }
                shouldReloadLive = true;
                break;
            case MSG_BEGIN_SEND_CMD_LIVE_MESSAGE:
                if (isSwitchingCamera) {
                    setHideAnimation(rLayoutMask, DEFAULT_ANIMATION_DURATION);
                    isSwitchingCamera = false;
                }
                updateLiveInfoText(R.string.get_live_error);
                Log.e(TAG, "handleMessage: MSG_BEGIN_SEND_CMD_LIVE_MESSAGE--直播指令发送超时");
                LiveLoadingDialog.dismiss();
                break;
            case MSG_BEGIN_SEND_CMD_WAKE_UP_MESSAGE:
                updateLiveInfoText(R.string.waking_up_device_failed);
                Log.e(TAG, "handleMessage: MSG_BEGIN_SEND_CMD_WAKE_UP_MESSAGE--唤醒指令发送超时");
                LiveLoadingDialog.dismiss();
                break;
            case MSG_TCP_CONNECTION_CLOSED:
                break;
            case MSG_RETRY_CONNECT_WEB_SOCKET:
                mStompClient.connect(this);
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (currentDisplayState == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
//            mLiveController.sendStopLiveCmd(mStompClient,mCarIMEI);
            super.onBackPressed();
        }
    }

    private Animation mHideAnimation;
    private Animation mShowAnimation;

    /**
     * View渐隐动画效果
     */
    public void setHideAnimation(final View view, int duration) {
        if (null == view || duration < 0) {
            return;
        }
        view.setClickable(false);
        if (null != mHideAnimation) {
            mHideAnimation.cancel();
        }
        // 监听动画结束的操作
        mHideAnimation = new AlphaAnimation(1.0f, 0.0f);
        mHideAnimation.setDuration(duration);
        mHideAnimation.setFillAfter(true);
        mHideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                view.setVisibility(View.GONE);
            }
        });
        view.startAnimation(mHideAnimation);
    }

    /**
     * View渐现动画效果
     */
    public void setShowAnimation(final View view, int duration) {
        if (null == view || duration < 0) {
            return;
        }
        if (null != mShowAnimation) {
            mShowAnimation.cancel();
        }
        view.setClickable(true);
        mShowAnimation = new AlphaAnimation(0.0f, 1.0f);
        mShowAnimation.setDuration(duration);
        mShowAnimation.setFillAfter(true);
        mShowAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
            }
        });
        view.startAnimation(mShowAnimation);
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        switch (requestType) {
            case REQUEST_CODE_DEVICES_STATUS:
                final DeviceStatusBean bean = (DeviceStatusBean) result;
                if (bean != null && bean.code == 0 && bean.data != null) {
                    int stateTmp = bean.data.state;
                    if (stateTmp == LiveConstant.STATE_ONLINE) {
                        Log.e(TAG, "Looping-----设备在线");
                    } else if (stateTmp == LiveConstant.STATE_SLEEP) {
                        Log.e(TAG, "Looping-----设备休眠");
                    } else {
                        if (mCurrentCarState == LiveConstant.STATE_SLEEP) {
                        } else {
                            showDeviceOffline();
                            mHandler.removeCallbacksAndMessages(null);
                            Log.e(TAG, "设备离线，移除所有超时");
                        }
                    }
                    if (bean.data.cameraSN != null) {
                        mCameraNumber = bean.data.cameraSN.size();
                    }
                    if (mCameraNumber == 2) {
                        mCameraSwitch.setVisibility(View.VISIBLE);
                    } else {
                        mCameraSwitch.setVisibility(View.GONE);
                    }

                    mAddressConvert.convert(bean.data.lastLat, bean.data.lastLon,
                            new AddressConvert.AddressConvertCallback() {
                                @Override
                                public void onAddressConverted(String address) {
                                    textPositionInfo.setText(address);
                                }
                            });

                }
                break;
        }

    }

    private Timer mTimer = new Timer(true);

    private void startTimerTask() {
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                requestDeviceStatus();
            }
        }, 0, UPDATE_POSITION_INTERVAL_TIME);
    }

    private void requestDeviceStatus() {
        if (CarControlApplication.getInstance().getMyInfo() != null)
            mDeviceStatusRequest.get(CarControlApplication.getInstance().getMyInfo().uid, CarControlApplication.getInstance().serverImei);
    }

    private void destroyTimerTask() {
        mTimer.cancel();
    }

    private void hideSystemNavigationBar() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View view = this.getWindow().getDecorView();
            view.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
