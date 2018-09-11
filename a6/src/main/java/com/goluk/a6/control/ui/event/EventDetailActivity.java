package com.goluk.a6.control.ui.event;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.goluk.a6.api.ApiUtil;
import com.goluk.a6.api.Callback;
import com.goluk.a6.common.map.MapStateListener;
import com.goluk.a6.common.map.MyCarMapView;
import com.goluk.a6.common.map.Point;
import com.goluk.a6.common.util.DateUtils;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.Config;
import com.goluk.a6.control.R;
import com.goluk.a6.control.R2;
import com.goluk.a6.control.download.SimpleDownloadListener;
import com.goluk.a6.control.live.LiveLoadingDialog;
import com.goluk.a6.control.ui.dialog.DownloadDialogUtil;
import com.goluk.a6.control.util.AddressConvert;
import com.goluk.a6.control.util.DeviceUtil;
import com.goluk.a6.control.util.EventVideoUtil;
import com.goluk.a6.control.util.SignalUtil;
import com.goluk.a6.control.util.Util;
import com.goluk.a6.cpstomp.CPStompClient;
import com.goluk.a6.cpstomp.DataSender;
import com.goluk.a6.cpstomp.EventVideoInfo;
import com.goluk.a6.cpstomp.MessageResult;
import com.goluk.a6.cpstomp.SignalInfo;
import com.goluk.a6.cpstomp.SocketConst;
import com.goluk.a6.cpstomp.UploadVideoProgress;
import com.goluk.a6.http.responsebean.DeviceStatus;
import com.goluk.a6.http.responsebean.EventCollectInfo;
import com.goluk.a6.http.responsebean.EventVideoList.EventVideo;
import com.goluk.a6.internation.GolukUtils;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYTextureView;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import likly.dollar.$;

/**
 * 事件视频详情页面
 */
public class EventDetailActivity extends BaseActivity implements MapStateListener, SeekBar.OnSeekBarChangeListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener, CPStompClient.StompConnListener, DownloadDialogUtil.OnDownloadCancelListener {

    /* 唤醒超时30s */
    private final static int TIMEOUT_WAKE_UP = 30 * 1000;
    /* 提取超时180s */
    private final static int TIMEOUT_EXTRA_VIDEO = 180 * 1000;

    @BindView(R2.id.title)
    TextView mTvTitle;
    @BindView(R2.id.tv_adress)
    TextView mTvAdress;
    @BindView(R2.id.map_container)
    RelativeLayout mMapContainer;

    @BindView(R2.id.ic_not_collected)
    TextView mIcNotCollected;
    @BindView(R2.id.ic_collected)
    TextView mIcHasCollected;

    @BindView(R2.id.player)
    KSYTextureView mMediaPlayer;
    @BindView(R2.id.view_video_bg)
    View mViewVideoBg;
    @BindView(R2.id.video_seekbar)
    SeekBar mSeekBar;
    @BindView(R2.id.tv_msg)
    TextView mTvMsg;
    @BindView(R2.id.ic_loading)
    ImageView icLoading;
    @BindView(R2.id.current_time)
    TextView mTimeCurrent;
    @BindView(R2.id.total_time)
    TextView mTimeTotal;
    @BindView(R2.id.ic_video_pause)
    ImageView mIcVideoPause;
    @BindView(R2.id.ic_switch_camera)
    ImageView mSwitchCamera;
    @BindView(R2.id.layout_download)
    RelativeLayout mLayoutDownload;
    @BindView(R2.id.layout_bottom_options)
    LinearLayout mLayoutBottomOptions;
    @BindView(R2.id.btn_download)
    TextView mBtnDownload;
    @BindView(R2.id.layout_signal_info)
    LinearLayout mLayoutSignalInfo;
    @BindView(R2.id.ic_signal_strength)
    ImageView mIvSignalStrength;
    @BindView(R2.id.net_speed)
    TextView mTvNetSpeed;

    private MyCarMapView mMap;

    private EventVideo mEventVideo;
    private String mImei;
    // 前后摄像头标志
    private boolean mFrontCamera = true;
    private boolean mIsCollected;
    private String mVideoUrl;
    private DeviceStatus mDviceStatus;
    private String mEventId;

    private DataSender mDataSender;
    private CPStompClient mWebSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        ButterKnife.bind(this);

        intiView();
        initData(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        intiView();
        initData(intent);
    }

    public void intiView() {
        mTvTitle.setText(R.string.event_detail);

        mMap = MyCarMapView.create(this);
        mMapContainer.addView(mMap);
        mMap.setStateListener(this);

        mSeekBar.setOnSeekBarChangeListener(this);
        setDownloadState(false);

        LiveLoadingDialog.init(icLoading);

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setBufferTimeMax(2.0f);
        mMediaPlayer.setTimeout(5, 30);
        mMediaPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                if (i == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    LiveLoadingDialog.show();
                } else if (i == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    LiveLoadingDialog.dismiss();
                } else if (i == IMediaPlayer.MEDIA_INFO_RELOADED) {
                }
                return false;
            }
        });

        // 视频默认宽高比19:6
        updateLayoutSize(16, 9);
    }

    private void initData(Intent intent) {
        boolean fromFamily = intent.getBooleanExtra("fromFamily", false);
        if (fromFamily)
            mLayoutBottomOptions.setVisibility(View.GONE);

        mEventId = intent.getStringExtra("eventId");
        queryEventDetail(mEventId);
    }

    private void convertAddress() {
        if (mEventVideo == null)
            return;

        AddressConvert addressConvert = new AddressConvert();
        addressConvert.convert(mEventVideo.lat, mEventVideo.lon,
                new AddressConvert.AddressConvertCallback() {
                    @Override
                    public void onAddressConverted(String address) {
                        mTvAdress.setText(address);
                    }
                });
    }

    /**
     * 查询事件详情
     */
    private void queryEventDetail(String eventId) {
        ApiUtil.apiService().eventDetail(eventId, new Callback<EventVideo>() {
            @Override
            protected void onError(int code, String msg) {
            }

            @Override
            public void onResponse(EventVideo eventVideo) {
                mEventVideo = eventVideo;
                mImei = mEventVideo.imei;

                onEventDetailQueryed();
            }
        });
    }

    /**
     * 查询到事件详情
     */
    private void onEventDetailQueryed() {
        if (mEventVideo == null)
            return;

        convertAddress();
        queryCollectInfo();
        drawCarPosition();

        // 切换按钮
        showSwitchButton();

        // 当前视频是否已经在云端
        boolean isCloud = EventVideoUtil.isCloud(mEventVideo, mFrontCamera);
        if (isCloud) {
            // 直接播放
            startPlay(mEventVideo.foreVideo);
        } else {
            // 查询设备状态
            queryDeviceStatus();
        }
    }

    /**
     * 查询设备状态返回
     */
    private void onDeviceStatus(DeviceStatus deviceStatus) {
        if (deviceStatus == null)
            return;

        mDviceStatus = deviceStatus;

        if (DeviceUtil.isOffline(deviceStatus)) {
            // 离线
            showHintMsg(R.string.device_not_online);
        } else {
            // 建立连接
            connectToDevice();
        }
    }

    /**
     * 切换按钮
     */
    private void showSwitchButton() {
        boolean hasBackEventVideo = EventVideoUtil.hasBackEventVideo(mEventVideo);
        mSwitchCamera.setVisibility(hasBackEventVideo ? View.VISIBLE : View.GONE);
    }

    @OnClick({R2.id.btn_back, R2.id.btn_collect, R2.id.layout_download, R2.id.layout_player_content, R2.id.ic_switch_camera})
    void onViewClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btn_back) {
            finish();
        } else if (viewId == R.id.btn_collect) {
            collectEvent();
        } else if (viewId == R.id.layout_download) {
            downloadVideo();
        } else if (viewId == R.id.layout_player_content) {
            pauseOrResume();
        } else if (viewId == R.id.ic_switch_camera) {
            switchCamera();
        }
    }

    // 更新播放进度
    private static final int MSG_SEEK_UPDATE = 1;
    // 唤醒休眠超时
    private static final int MSG_WAKE_UP_TIMEOUT = 2;
    // 提取视频超时
    private static final int MSG_EXTRA_TIMEOUT = 3;
    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEEK_UPDATE:
                    updateSeekInfo();
                    break;
                case MSG_WAKE_UP_TIMEOUT:
                    showHintMsg(R.string.wake_device_failed);
                    break;
                case MSG_EXTRA_TIMEOUT:
                    showHintMsg(R.string.extra_video_failed);
                    break;
            }
        }
    };

    private void updateSeekInfo() {
        int current = (int) mMediaPlayer.getCurrentPosition();
        mTimeCurrent.setText(DateUtils.parseToMinite(current));
        mSeekBar.setProgress(current);

        mUiHandler.sendEmptyMessageDelayed(MSG_SEEK_UPDATE, 1000);
    }

    /**
     * 查询收藏状态
     */
    private void queryCollectInfo() {
        ApiUtil.apiService().eventCollectInfo(mEventVideo.eventId, new Callback<EventCollectInfo>() {
            @Override
            protected void onError(int code, String msg) {
            }

            @Override
            public void onResponse(EventCollectInfo response) {
                super.onResponse(response);
                mIsCollected = true;
                updateCollectIcon();
            }
        });
    }

    /**
     * 收藏/取消收藏
     */
    private void collectEvent() {
        if (!GolukUtils.isNetworkConnected(this)) {
            $.toast().text(R.string.user_net_unavailable).show();
            return;
        }

        int type = mIsCollected ? 0 : 1;
        ApiUtil.apiService().collectEvent(mEventVideo.eventId, type, new Callback<String>() {
            @Override
            protected void onError(int code, String msg) {
            }

            @Override
            public void onResponse(String response) {
                super.onResponse(response);
                mIsCollected = !mIsCollected;
                $.toast().text(mIsCollected ? R.string.collect_success : R.string.cancel_collect_success).show();
                updateCollectIcon();
            }
        });
    }

    /**
     * 查询设备状态
     */
    private void queryDeviceStatus() {
        if (TextUtils.isEmpty(mImei))
            return;

        mViewVideoBg.setVisibility(View.VISIBLE);
        mSwitchCamera.setVisibility(View.GONE);
        showHintMsg(R.string.query_device_status);

        ApiUtil.apiService().queryDeviceStatus(mImei, 0, new Callback<DeviceStatus>() {
            @Override
            protected void onError(int code, String msg) {
                if (code == 20001) {
                    // 无对应的设备
                    showHintMsg(R.string.device_not_online);
                }
            }

            @Override
            public void onResponse(DeviceStatus response) {
                onDeviceStatus(response);
            }
        });
    }

    /**
     * 更新收藏按钮显示
     */
    private void updateCollectIcon() {
        mIcNotCollected.setVisibility(mIsCollected ? View.GONE : View.VISIBLE);
        mIcHasCollected.setVisibility(mIsCollected ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onMapReady() {
        drawCarPosition();
    }

    private void drawCarPosition() {
        if (mMap != null && mEventVideo != null)
            mMap.drawCarPosition(new Point(mEventVideo.lat, mEventVideo.lon), true);
    }

    private void play() {
        if (mMediaPlayer.isPlayable())
            mMediaPlayer.stop();

        mViewVideoBg.setVisibility(View.GONE);
        mIcVideoPause.setVisibility(View.GONE);

        try {
            mMediaPlayer.setDataSource(mVideoUrl);
            mMediaPlayer.prepareAsync();
            LiveLoadingDialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startPlay(String videoUrl) {
        if (TextUtils.isEmpty(videoUrl)) {
            return;
        }

        mViewVideoBg.setVisibility(View.GONE);
        mTvMsg.setVisibility(View.GONE);
        mLayoutSignalInfo.setVisibility(View.GONE);
        videoAlreadyExistLocal();
        showSwitchButton();

        mVideoUrl = videoUrl;

        if (mFrontCamera)
            mEventVideo.foreVideo = videoUrl;
        else
            mEventVideo.backVideo = videoUrl;

        play();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mTimeCurrent.setText(DateUtils.parseToMinite(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (mMediaPlayer != null)
            mMediaPlayer.seekTo(progress);
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        if (mMediaPlayer == null)
            return;
        LiveLoadingDialog.dismiss();
        final int duration = (int) mMediaPlayer.getDuration();
        // 更新View
        updateViewByVideoDuration(duration);
        // updateLayoutSize(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight());
        // 开始播放
        mMediaPlayer.start();
        mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        updateSeekInfo();
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        toVideoStartState();
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    private void updateViewByVideoDuration(int videoDuration) {
        mSeekBar.setMax(videoDuration);
        mTimeTotal.setText(DateUtils.parseToMinite(videoDuration));
    }

    private void showHintMsg(@StringRes int msgId) {
        showHintMsg(getString(msgId));
    }

    /**
     * 显示提示信息
     */
    private void showHintMsg(String msg) {
        mTvMsg.setVisibility(View.VISIBLE);
        mTvMsg.setText(msg);
    }

    /**
     * 设置下载按钮状态
     *
     * @param enable 可用
     */
    private void setDownloadState(boolean enable) {
        mLayoutDownload.setEnabled(enable);
        mLayoutDownload.setClickable(enable);
        mBtnDownload.setEnabled(enable);
    }

    /**
     * 当前视频本地是否已经存在
     */
    private void videoAlreadyExistLocal() {
        File localVideo = new File(getLocalVideoPath());
        boolean isExists = localVideo.exists();
        setDownloadState(!isExists);
        mBtnDownload.setText(isExists ? R.string.already_downloaded : R.string.download_file);
    }

    /**
     * 到视频开始位置并暂停状态
     */
    private void toVideoStartState() {
        if (mMediaPlayer == null)
            return;
        mUiHandler.removeMessages(MSG_SEEK_UPDATE);
        mMediaPlayer.pause();
        mMediaPlayer.seekTo(0);
        mSeekBar.setProgress(0);

        mIcVideoPause.setVisibility(View.VISIBLE);
        //mSwitchCamera.setVisibility(View.VISIBLE);
        keepScreenOn(false);
    }

    /**
     * 根据视频实际大小来设置视频控件大小
     */
    private void updateLayoutSize(int videoWidth, int videoHeight) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int playerWidth = dm.widthPixels;
        int heightSet = (int) (((float) videoHeight / videoWidth) * playerWidth);
        ViewGroup.LayoutParams layoutParams = mMediaPlayer.getLayoutParams();
        layoutParams.height = heightSet;
        mMediaPlayer.setLayoutParams(layoutParams);

        mViewVideoBg.setLayoutParams(layoutParams);
    }

    private void keepScreenOn(boolean alwaysOn) {
        if (alwaysOn)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 暂停/恢复
     */
    private void pauseOrResume() {
        // 如果没有提取,则为提取操作,否则播放/暂停操作
        boolean isCloud = EventVideoUtil.isCloud(mEventVideo, mFrontCamera);
        if (!isCloud) {
            if (DeviceUtil.isOnline(mDviceStatus)) {
                // 在线
                extraVideo();
            } else if (DeviceUtil.isDormant(mDviceStatus)) {
                // 休眠
                wakeup();
            }
            // 控件隐藏
            mIcVideoPause.setVisibility(View.GONE);
            mSwitchCamera.setVisibility(View.GONE);
            return;
        }

        // 播放/暂停操作
        if (!mMediaPlayer.isPlayable())
            return;
        mUiHandler.removeMessages(MSG_SEEK_UPDATE);
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mIcVideoPause.setVisibility(View.VISIBLE);
            keepScreenOn(false);
        } else {
            mMediaPlayer.start();
            mIcVideoPause.setVisibility(View.GONE);
            mUiHandler.sendEmptyMessageDelayed(MSG_SEEK_UPDATE, 1000);
            keepScreenOn(true);
        }
    }

    /**
     * 唤醒设备
     */
    private void wakeup() {
        showHintMsg(R.string.wakeuping);
        mDataSender.wakeup();
    }

    /**
     * 切换摄像头
     */
    private void switchCamera() {
        // 先判断是否已经在云上,没有则发送提取视频指令
        mFrontCamera = !mFrontCamera;
        boolean isCloud = EventVideoUtil.isCloud(mEventVideo, mFrontCamera);
        if (isCloud) {
            // 直接播放
            String videoUrl = mFrontCamera ? mEventVideo.foreVideo : mEventVideo.backVideo;
            startPlay(videoUrl);
        } else {
            // 需要提取视频
            if (mMediaPlayer != null)
                mMediaPlayer.stop();
            mUiHandler.removeMessages(MSG_SEEK_UPDATE);
            mIcVideoPause.setVisibility(View.GONE);

            videoAlreadyExistLocal();
            queryDeviceStatus();
        }
    }

    /**
     * 建立WebSocket连接
     */
    private void connectToDevice() {
        if (mWebSocket != null)
            mWebSocket.disconnect();

        mWebSocket = new CPStompClient();
        mDataSender = new DataSender(mWebSocket);
        mDataSender.connect(this);
    }

    /**
     * 提取事件视频
     */
    private void extraVideo() {
        showHintMsg(R.string.getting_video);
        setDownloadState(false);
        String videoName = mFrontCamera ? mEventVideo.foreVideoName : mEventVideo.backVideoName;
        if (mDataSender != null)
            mDataSender.extractEventVideo(mEventVideo.eventId, videoName, mFrontCamera);
        // 开始提取超时,90s
        mUiHandler.sendEmptyMessageDelayed(MSG_EXTRA_TIMEOUT, TIMEOUT_EXTRA_VIDEO);
    }

    private void showExtraProgress(int progress) {
        final String progressMsg = getString(R.string.getting_video) + "(" + String.valueOf(progress) + "%)";
        showHintMsg(progressMsg);
    }

    @Override
    public void stompOpened() {
        mDataSender.topic(mImei);
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (DeviceUtil.isOnline(mDviceStatus)) {
                    // 在线
                    extraVideo();
                } else if (DeviceUtil.isDormant(mDviceStatus)) {
                    // 休眠
                    wakeup();
                }
            }
        }, 1000);
    }

    @Override
    public void stompClosed() {
    }

    @Override
    public void stompError(Exception e) {
    }

    @Override
    public void onRcvMsg(String msg) {
        MessageResult result = $.json().toBean(msg, MessageResult.class);
        MessageResult.Data data = $.json().toBean(result.data, MessageResult.Data.class);
        if (TextUtils.equals(data.cmd, SocketConst.CMD_EXTRACT_EVENT_VIDEO_ACK)) {
            mUiHandler.removeMessages(MSG_EXTRA_TIMEOUT);
            if (result.code == 0) {
                if (data.code == 0) {
                    // 上传进度
                    UploadVideoProgress progressInfo = $.json().toBean(data.data, UploadVideoProgress.class);
                    if (progressInfo != null && isCurrentUploadVideo(progressInfo.eventId, progressInfo.cameraNo))
                        showExtraProgress(progressInfo.progress);
                    //播放url
                    EventVideoInfo videoInfo = $.json().toBean(data.data, EventVideoInfo.class);
                    if (videoInfo != null && isCurrentUploadVideo(videoInfo.eventId, videoInfo.cameraNo)) {
                        startPlay(videoInfo.videoUrl);
                    }
                } else if (data.code == SocketConst.ERROR_CODE_FILENOTEXIST) {
                    // 视频文件不存在
                    showHintMsg(R.string.video_deleted);
                } else if (data.code == SocketConst.ERROR_CODE_UPLOAD_FAILED
                        || data.code == SocketConst.ERROR_CODE_SIGN_FAILED) {
                    // 上传视频失败
                    showHintMsg(R.string.extra_video_failed);
                } else if (data.code == -1) {
                    // 上传进度
                    UploadVideoProgress progressInfo = $.json().toBean(data.data, UploadVideoProgress.class);
                    if (progressInfo != null && isCurrentUploadVideo(progressInfo.eventId, progressInfo.cameraNo))
                        showExtraProgress(progressInfo.progress);
                }
            }
        } else if (TextUtils.equals(data.cmd, SocketConst.CMD_WAKEUP_ACK)) {
            // 开始唤醒超时,30s
            mUiHandler.sendEmptyMessageDelayed(MSG_WAKE_UP_TIMEOUT, TIMEOUT_WAKE_UP);
        } else if (TextUtils.equals(data.cmd, SocketConst.ONLINE)) {
            // 唤醒成功,且设备状态已经切换为在线
            mUiHandler.removeMessages(MSG_WAKE_UP_TIMEOUT);
            extraVideo();
        } else if (TextUtils.equals(data.cmd, SocketConst.CMD_SIGNAL_STRANGTH_ACK)) {
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
        mLayoutSignalInfo.setVisibility(View.VISIBLE);
        mTvNetSpeed.setText(Util.getFormatNetSpeedSize(signalInfo.speed));
        int simSignalIcon = SignalUtil.getSimStrengthWhiteIconByLevel(signalInfo.level);
        if (simSignalIcon != -1)
            mIvSignalStrength.setImageResource(simSignalIcon);
    }

    /**
     * 是否是当前上传的视频进度
     */
    private boolean isCurrentUploadVideo(String eventId, int cameraNo) {
        return TextUtils.equals(mEventId, eventId)
                && ((mFrontCamera && cameraNo == 0) || (!mFrontCamera && cameraNo == 1));
    }

    @Override
    public void onReturn(Void aVoid) {
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        videoAlreadyExistLocal();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        if (mDataSender != null) {
            mDataSender.destory();
            mDataSender = null;
        }
    }

    /// 下载视频 ///
    private DownloadDialogUtil mDownloadDialogUtil;
    private BaseDownloadTask mDownloadTask;

    private void downloadVideo() {
        if (!GolukUtils.isNetworkConnected(this)) {
            $.toast().text(R.string.user_net_unavailable).show();
            return;
        }

        String videoDownloadUrl = mFrontCamera ? mEventVideo.foreVideo : mEventVideo.backVideo;
        if (TextUtils.isEmpty(videoDownloadUrl))
            return;

        mDownloadDialogUtil = new DownloadDialogUtil(this, this);

        FileDownloader.setup(this);
        final String savePath = getLocalVideoPath();
        mDownloadTask = FileDownloader.getImpl().create(videoDownloadUrl).setPath(savePath).setListener(new SimpleDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                if (mDownloadDialogUtil != null)
                    mDownloadDialogUtil.showDownloadDialog();
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                int percent = Math.round((float) soFarBytes / (float) totalBytes * 100);
                if (mDownloadDialogUtil != null)
                    mDownloadDialogUtil.onProgressUpdate(percent);
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                if (mDownloadDialogUtil != null)
                    mDownloadDialogUtil.dismiss();
                $.toast().text(R.string.tip_download_success).show();
                videoAlreadyExistLocal();
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                if (mDownloadDialogUtil != null)
                    mDownloadDialogUtil.dismiss();
                $.toast().text(R.string.tip_download_fail).show();
            }
        });
        mDownloadTask.start();
    }

    /**
     * 获取当前摄像头对应的本地视频下载保存路径
     */
    private String getLocalVideoPath() {
        String videoName = mFrontCamera ? mEventVideo.foreVideoName : mEventVideo.backVideoName;
        return Config.CARDVR_LOCK_PATH + File.separator + videoName;
    }

    @Override
    public void onDownloadCanceled() {
        mDownloadDialogUtil = null;
        if (mDownloadTask != null)
            mDownloadTask.pause();
        finish();
    }

}
