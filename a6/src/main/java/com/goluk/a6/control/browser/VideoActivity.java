package com.goluk.a6.control.browser;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.a6.common.OrientationManager;
import com.goluk.a6.common.map.MapTrackView;
import com.goluk.a6.common.util.DateUtils;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.CarWebSocketClient;
import com.goluk.a6.control.Config;
import com.goluk.a6.control.R;
import com.goluk.a6.control.dvr.RemoteCameraConnectManager;
import com.goluk.a6.control.dvr.UserItem;
import com.goluk.a6.control.util.CollectionUtils;
import com.goluk.a6.control.util.DownloadTask;
import com.goluk.a6.control.util.GPSFile;
import com.goluk.a6.control.util.HttpDownloadManager;
import com.goluk.a6.control.util.HttpRequestManager;
import com.media.tool.GPSData;
import com.media.tool.MediaPlayer;
import com.media.tool.MediaPlayer.OnBufferListener;
import com.media.tool.MediaPlayer.OnInfoListener;
import com.media.tool.MediaPlayer.onVideoLocationListener;
import com.media.tool.MediaProcess;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class VideoActivity extends BaseActivity implements OnSeekBarChangeListener, OnTouchListener, SurfaceTextureListener,
        OnInfoListener, OnBufferListener, onVideoLocationListener, MapTrackView.MapListener, CarWebSocketClient.CarWebSocketClientCallback, OrientationManager.IOrientationFn {

    private static final String TAG = "CarSvc_VideoActivity";
    public static final String KEY_FILE_TIME = "key_file_time";
    public static final String KEY_FILE_NAME = "key_file_name";
    public static final String KEY_LIVING_FLAG = "key_living";
    public static final String KEY_LIVING_SN = "key_living_sn";
    public static final String KEY_LIVING_JSON = "key_living_json";

    private static final int START_LIVING = 1;
    private static final int STOP_LIVING = 0;
    public static final int CAMERA_FRONT = 0;
    public static final int HIDE_CONTROL_DELAY_MILLIS = 2000;
    public static final int UPDATE_PROGRESS_DELAY_MILLIS = 300;

    private Uri mIntentUri;
    private TextView mDuration;
    private TextView mTime;
    private TextView mTvSize;
    private TextView mTvKM;
    private TextView mTvTimeUnit;
    private TextView mTvTime;
    private TextView mTvSpeed;
    private TextView mTvRes;
    private TextView mTvGpsErr;
    private SeekBar mPlayerSeekBar;
    private ImageView mStartPlayer;
    private ImageView play;
    private ImageView delete;
    private ImageView download;
    private ImageView mReturn;
    private RelativeLayout mRlTop;
    ProgressBar mProgressBar;
    TextView mTipPrompt;
    boolean mIsPlaying = false;
    private boolean isDownLoad = false;

    private Toast mToast;
    private TextView mToastView;
    private TextView mTvMapHint;
    private Drawable mBrightnessIcon;
    private Drawable mVolumnIcon;
    private Drawable mSeekIconBackward;
    private Drawable mSeekIconforward;
    private Point mTouchStart = new Point();
    private AudioManager mAudioManager;
    private int mMaxVolume;
    private int mCurrentVolume;
    private View mBottomView;
    private View mMapContainer;
    private MapTrackView mMapTrackView;
    private MediaPlayer mMediaPlayer;
    private TextureView mTextureView;
    private RelativeLayout mVideoPreviewContainer;
    private boolean mRemoteFile = false;
    private boolean mLiving = false;
    private String mActionbarTitle = "";
    private ImageView mStartImage;
    private ImageView mFullScreen;
    private LinearLayout mLLInfo;
    private LinearLayout mLLOperation;
    private Map<Integer, GPSData> mGPSDataMap;
    private int mFirstGPSDataTime;

    private boolean mIsVideoFullScreenMode = false;
    private int mMarginBottom = 0;
    private Surface mSurface;

    private View mVideoMainLayout, mVideoShareLayout;
    private String mSerialNum = null;
    private AliyunOSSDataSource mAliyunOSSDataSource = null;
    private String mAliyunOSSBufferFileName = Config.CARDVR_PATH + "/live_streaimg_file";
    boolean mPauseFromBack = false;
    ArrayList<GPSData> mGPSDataList = new ArrayList<GPSData>();
    private List<GPSData> mAllGps;
    private ImageView mSaveImage;
    ImageView mVoiceVolume;
    private int mLivingRetryCount = 0;    //try about 5 seconds
    RelativeLayout mVolume_container;
    private ProgressBar mProgressBarDownload;
    private int mCameraNumber = 0;
    private int mCameraDir = 'F';
    private List<String> mCameraLists = new ArrayList<String>(4);
    private TextView mSwitchButton;
    private TextView mTvCurrentSpeed;
    private JSONObject mIntentJson;
    private String filePath;
    private String mCurrentPath;
    private FileInfo finfo;
    private ProgressDialog mDialog;
    private FrameLayout mFlDownload;
    private boolean isRemoteMp4 = false;
    private int lastPlayPosition = 0;
    private boolean showingControlViews = false;
    private Runnable seekPositionRunable;
    private Runnable updateViewRunalbe;
    private OrientationManager mOrignManager = null;
    private boolean isCanRotate = true;
    private boolean mClick = false;
    private boolean mIsLand = false;
    /**
     * 点击进入横屏
     */
    private boolean mClickLand = true;
    /**
     * 点击进入竖屏
     */
    private boolean mClickPort = true;

    /**
     * 当前下载任务
     */
    private DownloadTask mCurrentDownloadTask = null;
    private ProgressDialog mDownloadProgressView;

    private void lockRotate() {
        isCanRotate = false;
        mHandler.sendEmptyMessageDelayed(100, 1000);
    }

    private String getHumanTime(long sendTime) {
        String formatTimeString;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatTimeString = formatter.format(new Date(sendTime));
        return formatTimeString;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        mRlTop = (RelativeLayout) findViewById(R.id.rl_top);
        mLLInfo = (LinearLayout) findViewById(R.id.ll_info);
        mLLOperation = (LinearLayout) findViewById(R.id.ll_operation);
        mProgressBarDownload = (ProgressBar) findViewById(R.id.progressBar);
        mTvRes = (TextView) findViewById(R.id.res);
        mFlDownload = (FrameLayout) findViewById(R.id.fl_download);
        mVolume_container = (RelativeLayout) findViewById(R.id.volume_container);
        mVolume_container.setVisibility(View.INVISIBLE);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        mProgressBar.setVisibility(View.INVISIBLE);
        mTipPrompt = (TextView) findViewById(R.id.tip_prompt);
        mTvKM = (TextView) findViewById(R.id.tv_km);
        mTvTime = (TextView) findViewById(R.id.tv_time);
        mTvTimeUnit = (TextView) findViewById(R.id.time_unit);
        mTvSpeed = (TextView) findViewById(R.id.tv_speed);
        mTvMapHint = (TextView) findViewById(R.id.tv_map_not_installed);
        mTvCurrentSpeed = (TextView) findViewById(R.id.tv_speed_in_time);
        mTipPrompt.setVisibility(View.INVISIBLE);
        play = (ImageView) findViewById(R.id.play);
        delete = (ImageView) findViewById(R.id.delete);
        mReturn = (ImageView) findViewById(R.id.video_activity_fullscreen_exit);
        mTvGpsErr = (TextView) findViewById(R.id.text_gps_data_error);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPauseResume();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(filePath)) {
                    AlertDialog formatDialog = new AlertDialog.Builder(VideoActivity.this)
                            .setTitle(R.string.hint)
                            .setMessage(R.string.delete_video)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deletefile();
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .create();
                    formatDialog.show();
                }
            }
        });

        mVideoMainLayout = findViewById(R.id.video_main_layout);
        mVideoShareLayout = findViewById(R.id.video_share_layout);

        mBottomView = findViewById(R.id.video_bar_bottom);
        mDuration = (TextView) findViewById(R.id.video_duration);
        mTime = (TextView) findViewById(R.id.video_time);
        mPlayerSeekBar = (SeekBar) findViewById(R.id.player_seekbar);
        mPlayerSeekBar.setOnSeekBarChangeListener(this);
        mStartPlayer = (ImageView) findViewById(R.id.video_play);
        mStartPlayer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mLiving)
                    doPauseResume();
                if (mIsVideoFullScreenMode) {
                    mHandler.removeMessages(MSG_HIDE_CONTROL_BOTTOMBAR);
                    mHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROL_BOTTOMBAR, HIDE_CONTROL_DELAY_MILLIS);
                }
            }

        });
        mStartImage = (ImageView) findViewById(R.id.video_activity_start);
        mStartImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mHandler.removeMessages(MSG_HIDE_CONTROL_BOTTOMBAR);
                }
                if (mPlaybackCompleted == 1) {
                    hideControlBar();
                }
                doPauseResume();
            }

        });
        mStartImage.setVisibility(View.INVISIBLE);

        mSaveImage = (ImageView) findViewById(R.id.video_save);
        mSaveImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);
                builder.setTitle(R.string.video_save);
                builder.setMessage(R.string.video_view);
                builder.setPositiveButton(R.string.ok, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        storeLiving();
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.create().show();
            }

        });

        mVoiceVolume = (ImageView) findViewById(R.id.voice_vol);

        mTextureView = (TextureView) findViewById(R.id.video_textureview);
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setOnTouchListener(this);

        mVideoPreviewContainer = (RelativeLayout) findViewById(R.id.video_preview_container);

        mFullScreen = (ImageView) findViewById(R.id.video_activity_fullscreen);
        mFullScreen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mStartImage.setVisibility(View.GONE);
                mHandler.removeMessages(MSG_HIDE_CONTROL_BOTTOMBAR);
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    fullScreen();
                else
                    exitFullScreen();
            }

        });

        mReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClick = true;
                lockRotate();
                exitFullScreen();
            }
        });
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mFullScreen.setImageResource(R.drawable.fullscreen_video);
        } else {
            mFullScreen.setImageResource(R.drawable.small_screen_black);
        }

        mToastView = (TextView) getLayoutInflater().inflate(R.layout.toast_note, (ViewGroup) mTextureView.getParent(),
                false);
        mBrightnessIcon = this.getResources().getDrawable(R.drawable.icon_toast_brightness);
        mVolumnIcon = this.getResources().getDrawable(R.drawable.icon_toast_volume);
        mSeekIconBackward = this.getResources().getDrawable(R.drawable.icon_toast_seekbackward);
        mSeekIconforward = this.getResources().getDrawable(R.drawable.icon_toast_seekforward);
        mBrightnessIcon.setBounds(0, 0, mBrightnessIcon.getIntrinsicWidth(), mBrightnessIcon.getIntrinsicHeight());
        mVolumnIcon.setBounds(0, 0, mBrightnessIcon.getIntrinsicWidth(), mBrightnessIcon.getIntrinsicHeight());
        mSeekIconBackward.setBounds(0, 0, mBrightnessIcon.getIntrinsicWidth(), mBrightnessIcon.getIntrinsicHeight());
        mSeekIconforward.setBounds(0, 0, mBrightnessIcon.getIntrinsicWidth(), mBrightnessIcon.getIntrinsicHeight());
        mTvSize = (TextView) findViewById(R.id.size);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100;
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 100;

        mMapContainer = findViewById(R.id.video_map_container);
        mMapTrackView = MapTrackView.create(this);
        if (!mMapTrackView.isMapAvailable()) {
//            AlertDialog formatDialog = new AlertDialog.Builder(VideoActivity.this)
//                    .setTitle(R.string.hint)
//                    .setMessage(R.string.install_google)
//                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    })
//                    .create();
//            formatDialog.show();
            mTvMapHint.setVisibility(View.VISIBLE);
        } else {
            mMapTrackView.onCreate(savedInstanceState);
            mMapTrackView.setMapListener(this);
            RelativeLayout mapParent = (RelativeLayout) findViewById(R.id.tarck_map_parent_view);
            mapParent.addView(mMapTrackView);
        }
        mSwitchButton = (TextView) findViewById(R.id.switch_camera);
        mSwitchButton.setVisibility(View.GONE);
        mSwitchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchLivingCamera();
            }
        });

        handleIntent(getIntent());

        if (mLiving) {
//            mMapTrackView.setShowCarInfoTime(false);
            mStartImage.setVisibility(View.INVISIBLE);
            mStartPlayer.setVisibility(View.INVISIBLE);
            mPlayerSeekBar.setVisibility(View.INVISIBLE);
            mTime.setVisibility(View.INVISIBLE);
            mSaveImage.setVisibility(View.VISIBLE);
            mSaveImage.setEnabled(false);
            mVoiceVolume.setImageResource(R.drawable.v1);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        if (mIntentUri.toString().startsWith("http")) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mTipPrompt.setText(R.string.wait_for_buffer);
                    mTipPrompt.setVisibility(View.VISIBLE);
                }
            });
        }

        mMapContainer = findViewById(R.id.video_map_container);
        download = (ImageView) findViewById(R.id.download);
        if (!mRemoteFile) {
            download.setVisibility(View.GONE);
        }
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer!=null&&mMediaPlayer.isPlaying())doPauseResume();
                downloadFile(finfo.path + finfo.name);
            }
        });
        mVoiceToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mVoiceToast.setDuration(Toast.LENGTH_SHORT);
        mVoiceToast.setGravity(Gravity.TOP, 0, 100);
        mDialog = new ProgressDialog(this);
        mDialog.setTitle(R.string.hint);
        mDialog.setMessage(getString(R.string.tip_deleting_file));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mOrignManager = new OrientationManager(this, this);
        if (android.provider.Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 0) {
            mOrignManager.clearListener();
        }
        if (RemoteCameraConnectManager.supportWebsocket())
            CarWebSocketClient.instance().registerCallback(this);
        initProgressView();
    }

    private void initProgressView(){
        mDownloadProgressView = new ProgressDialog(this);
        mDownloadProgressView.setMessage(getString(R.string.str_downloading));
        mDownloadProgressView.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0
                        &&event.getAction()==KeyEvent.ACTION_DOWN){
                    requestCancelDownload();
                    return true;
                }
                return false;
            }
        });
        mDownloadProgressView.setCancelable(false);
    }

    private AlertDialog mCancelTaskDialog;
    private void requestCancelDownload(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        mCancelTaskDialog = builder.setTitle(R.string.hint).setMessage(R.string.str_hint_quit_download).setPositiveButton(R.string.cling_dismiss, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDownloadProgressView.dismiss();
                if (mCurrentDownloadTask!=null&&HttpDownloadManager.instance()!=null){
                    HttpDownloadManager.instance().cancelDownload(mCurrentDownloadTask);
                    HttpDownloadManager.instance().clear();}

                finish();

            }
        }).setNegativeButton(R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create();
        mCancelTaskDialog.show();
    }

    private void deletefile() {
        if (mRemoteFile) {
            if (RemoteCameraConnectManager.supportWebsocket()) {
                JSONObject jso = new JSONObject();
                try {
                    jso.put("action", "delete");
                    JSONArray array = new JSONArray();
                    JSONObject file = new JSONObject();
                    file.put("name", finfo.name);
                    file.put("path", finfo.path);
                    file.put("size", finfo.lsize);
                    file.put("dir", finfo.isDirectory);
                    file.put("time", finfo.modifytime);
                    file.put("sub", finfo.sub);
                    array.put(file);
                    jso.put("list", array);
                    Log.i(TAG, "jso.toString() = " + jso.toString());
                    mDialog.show();
                    HttpRequestManager.instance().requestWebSocket(jso.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                String path = finfo.path + finfo.name;
                doDeleteFile(path);
            }
        } else {
            if (Util.deleteFile(filePath)) {
                Toast.makeText(VideoActivity.this, R.string.tip_delete_success, Toast.LENGTH_SHORT).show();
                setResult(100);
                VideoActivity.this.finish();
            } else {
                Toast.makeText(VideoActivity.this, R.string.delete_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsVideoFullScreenMode) {
            int newUiOptions = getWindow().getDecorView().getSystemUiVisibility();
            if (Build.VERSION.SDK_INT >= 16) {
                newUiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }
            if (Build.VERSION.SDK_INT >= 18) {
                newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        }
        isCanRotate = true;
        mPauseFromBack = false;
        if (mMapTrackView.isMapAvailable()) {
            mMapTrackView.onResume();
            mMapTrackView.setLocationEnabled(true);
        }
        if (mMediaPlayer != null) {
            mHandler.removeMessages(MSG_PROGRESS);
            mHandler.sendEmptyMessage(MSG_PROGRESS);
            mStartImage.setVisibility(View.GONE);
            refreshStartImage();
            mMediaPlayer.resume();
            if (isDownloading())
                doPauseResume();
        }
    }

    private boolean isDownloading() {
        return mDownloadProgressView != null && mDownloadProgressView.isShowing();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    //删除文件
    private void doDeleteFile(String filePath) {
        mCurrentPath = filePath;
        String url = "";
        try {
            url = "http://" + RemoteCameraConnectManager.HTTP_SERVER_IP + ":" + RemoteCameraConnectManager.HTTP_SERVER_PORT +
                    "/cgi-bin/Config.cgi?action=delete&property=path&value=" + URLEncoder.encode(filePath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "url = " + url);
        HttpRequestManager.instance().requestHttp(url, new HttpRequestManager.OnHttpResponseListener() {

            @Override
            public void onHttpResponse(final String result) {
                Log.i(TAG, "result = " + result);
                if (result == null)
                    return;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (result.contains("OK")) {
                            Toast.makeText(VideoActivity.this, R.string.tip_delete_success, Toast.LENGTH_SHORT).show();
                            setResult(100);
                            finish();
                        } else
                            Toast.makeText(VideoActivity.this, R.string.tip_delete_fail, Toast.LENGTH_SHORT).show();

                    }

                });
            }

        });

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isDownLoad) {
//            AlertDialog isExit = new AlertDialog.Builder(this).create();
//            // 设置对话框标题
//            isExit.setTitle("系统提示");
//            // 设置对话框消息
//            isExit.setMessage("确定要退出吗");
//            // 添加选择按钮并注册监听
//            isExit.setButton("确定", listener);
//            isExit.setButton2("取消", listener);
//            // 显示对话框
//            isExit.show();
                Toast.makeText(this, R.string.downloading, Toast.LENGTH_SHORT);
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }
        return false;
    }


    //下载文件
    private void downloadFile(String filePath) {
        //如果需要下载的文件已经正在下载，先取消下载
        Log.e(TAG, "downloadFile: "+filePath );

        // 先判断本地是否已经存在
        String savePath = Config.CARDVR_PATH + filePath;
        if (savePath.contains(".")) {
            savePath = savePath.substring(0, savePath.lastIndexOf(".")) + ".mp4";
        }
        File saveFile = new File(savePath);
        if (saveFile.exists()) {
            showToast(R.string.tip_download_success);
            return;
        }

        DownloadTask old = HttpDownloadManager.instance().getDownloadTask(filePath);
        if (old != null) {
            VideoActivity.this.showToast(R.string.downloading);
            mCurrentDownloadTask = old;
            return;
            //HttpDownloadManager.instance().cancelDownload(old);
        }
        finfo.downloading = false;
        finfo.downloadProgress = 0;
        DownloadTask task = new DownloadTask(filePath, new HttpDownloadManager.OnDownloadListener() {
            @Override
            public void onDownloadStart(DownloadTask task) {
                isDownLoad = true;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        delete.setEnabled(false);
                        mProgressBarDownload.setVisibility(View.VISIBLE);
                        mDownloadProgressView.show();
                    }
                });
            }

            @Override
            public void onDownloadEnd(DownloadTask task, final boolean succeed) {
                if (finfo != null) {
                    finfo.downloading = false;
                    finfo.downloadProgress = 0;
                }
                isDownLoad = false;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mCancelTaskDialog != null && mCancelTaskDialog.isShowing())
                            mCancelTaskDialog.dismiss();

                        mProgressBarDownload.setVisibility(View.GONE);
                        delete.setEnabled(true);
                        if (!succeed) {
                            if (!RemoteCameraConnectManager.instance().isConnected()) {
                                showToast(getString(R.string.device_offline));
                            } else {
                                showToast(getString(R.string.tip_download_fail));
                            }
                        } else {
                            showToast(getString(R.string.tip_download_success));
                        }
                    }
                });
                mDownloadProgressView.dismiss();
                HttpDownloadManager.instance().cancelDownload(task);
            }

            @Override
            public void onDownloadProgress(DownloadTask task, final int progress) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        finfo.downloading = true;
                        finfo.downloadProgress = progress;
                        mProgressBarDownload.setProgress(progress);
                        mDownloadProgressView.setMessage(getString(R.string.str_downloading)+"  "+progress+"%");
                    }
                });
            }
        });
        finfo.downloading = true;
        finfo.downloadProgress = task.getProgress();
        mCurrentDownloadTask = task;
        HttpDownloadManager.instance().requestDownload(task);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMapTrackView != null) {
            mMapTrackView.onDestroy();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mGPSDataMap = null;
        }
        mOrignManager.clearListener();
        //stopLiving();
        HttpDownloadManager.clear();
        if(mAllGps != null)
            mAllGps.clear();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isCanRotate = false;
        if (mMapTrackView.isMapAvailable()) {
            mMapTrackView.onPause();
            mMapTrackView.setLocationEnabled(false);
        }
        if (mMediaPlayer != null) {
            mHandler.removeMessages(MSG_PROGRESS);
            mMediaPlayer.pause();
            lastPlayPosition = mMediaPlayer.getCurrentPosition();
        }

        if (mLiving) {
            if (!mPauseFromBack && mIsPlaying) {
                //save default
                storeLiving();
                Toast.makeText(this, getResources().getString(R.string.video_save_default), Toast.LENGTH_LONG).show();
            }
        }
        mHandler.removeCallbacks(seekPositionRunable);
        mHandler.removeCallbacks(updateViewRunalbe);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent");
        super.onNewIntent(intent);
        handleIntent(intent);
        if (mIntentUri != null && mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer = null;
            mGPSDataMap = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mRemoteFile) {
            getMenuInflater().inflate(R.menu.video_activity_remote, menu);
        } else {
            if (mVideoShareLayout.getVisibility() == View.VISIBLE) {
                getMenuInflater().inflate(R.menu.video_activity_remote, menu);
            } else {
                getMenuInflater().inflate(R.menu.video_activity_local, menu);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    void sendLiveHeartbeat() {
        // : Send Living heart packet
        mHandler.sendEmptyMessageDelayed(MSG_LIVE_HEARTBEAT, 10 * 1000);
    }

    private void startLiving(JSONObject jso) {
        int ret = -1;
        String accessKey = null, secretKey = null, endpoint = null, bucket = null, streamingFile = null;
        try {
            ret = jso.getInt("ret");
            accessKey = jso.getString("access");
            secretKey = jso.getString("secret");
            endpoint = jso.getString("ep");
            bucket = jso.getString("bk");
            streamingFile = jso.getString("sf");

        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        String cameralist = jso.optString("cl", null);
        if (cameralist != null) {
            Log.d(TAG, "mCameraDir = " + mCameraDir + " cameralist = " + cameralist);
            parseLivingCameraList(cameralist);
        }

        if (ret == 0) {
            if (mProgressBar.getVisibility() == View.VISIBLE) {
                mTipPrompt.setText(R.string.video_ok);
                mTipPrompt.setVisibility(View.VISIBLE);
            }
            startMediaPlayer();
            mAliyunOSSDataSource = new AliyunOSSDataSource(getApplicationContext(), accessKey, secretKey, endpoint, bucket, streamingFile);
            mAliyunOSSDataSource.setMediaPlayer(mMediaPlayer);
            mAliyunOSSDataSource.setBufferFilename(mAliyunOSSBufferFileName);
            mAliyunOSSDataSource.start();
            sendLiveHeartbeat();
            mHandler.removeMessages(MSG_LIVE_TIMEOUT);
            mHandler.sendEmptyMessageDelayed(MSG_LIVE_TIMEOUT, 200 * 1000);
            mGPSDataList.clear();
            mLastStreamSize = -1;
            mLastDuration = -1;
        }
    }

    private void startLiving() {
        if (mSerialNum == null) {
            Log.e(TAG, "mSerialNum is null, error...");
            return;
        }

        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mTipPrompt.setText(R.string.video_request);
            mTipPrompt.setVisibility(View.VISIBLE);
        }

        // : send start living command to device side
        mHandler.sendEmptyMessageDelayed(MSG_REQUEST_TIMEOUT, 10000);
        if (mIntentJson != null) {
            startLiving(mIntentJson);
        }
        Log.d(TAG, "Send start Living command to device, and wait..., count=" + mLivingRetryCount + " mCameraDir = " + mCameraDir);
    }

    protected void parseLivingCameraList(String list) {
        mCameraLists.clear();
        for (int i = 0; i < list.length(); i++) {
            mCameraLists.add(list.substring(i, i + 1));
        }

        Log.d(TAG, "mCameraLists.size() = " + mCameraLists.size());
        if (mCameraLists.size() > 1) {
            mSwitchButton.setText(String.format("%c", mCameraDir));
            mSwitchButton.setVisibility(View.VISIBLE);
        } else {
            mSwitchButton.setVisibility(View.GONE);
        }
    }

    private void switchLivingCamera() {
        stopLiving();

        int i = 0;
        int number = mCameraLists.size();
        for (i = 0; i < number; i++) {
            if (mCameraDir == (mCameraLists.get(i).charAt(0))) {
                i = (i + 1) % number;
                mCameraDir = mCameraLists.get(i).charAt(0);
                break;
            }
        }

        startLiving();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMediaPlayer != null) {
            outState.putInt("current", mMediaPlayer.getCurrentPosition());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            int pos = savedInstanceState.getInt("current");
            if (pos != 0 && mMediaPlayer != null) {
                mMediaPlayer.seekTo(pos);
            }
        }
    }

    private void stopLiving() {
        mIsPlaying = false;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer = null;
            mGPSDataMap = null;
        }

        if (mAliyunOSSDataSource != null) {
            mAliyunOSSDataSource.stop();
            mAliyunOSSDataSource = null;
            // : send stop living command to device side
            mHandler.removeMessages(MSG_LIVE_HEARTBEAT);
            mHandler.removeMessages(MSG_LIVE_TIMEOUT);
            mHandler.removeMessages(MSG_STREAM_CHECK);
            mGPSDataList.clear();
            Log.d(TAG, "Send stop Living command to device");
        }
        mSaveImage.setEnabled(false);
        mProgressBar.setVisibility(View.INVISIBLE);
        mTipPrompt.setVisibility(View.INVISIBLE);

        mHandler.removeMessages(MSG_REQUEST_TIMEOUT);
        mHandler.removeMessages(MSG_BUFFER_TIMEOUT);
        mHandler.removeMessages(MSG_START_LIVE);
        //onRecordState(VoiceRecordImage.STATE_RECORD_FINISH);
    }

    private void storeLiving() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                String saveFile = Config.CARDVR_CAPTURE_PATH + "/livestream-" + getHumanTime(System.currentTimeMillis()) + ".mp4";
                if (mLiving) {
                    MediaProcess mp = new MediaProcess(MediaProcess.CONVERT);
                    mp.setInputFile(mAliyunOSSBufferFileName);
                    mp.setOutFile(saveFile);
                    //mp.setListener(this);
                    //native block here
                    mp.start();

                    mp.destroy();
                    mp = null;
                }
            }
        }).start();
    }

    private void startMediaPlayer() {
        try {
            mMediaPlayer = new MediaPlayer(mSurface);
            mMediaPlayer.setInfoListener(this);
            mMediaPlayer.setBufferingListener(this);
            mMediaPlayer.setLocationListener(this);
            if (mLiving) {
                mMediaPlayer.setLiveStreamingFlag();
                mMediaPlayer.setDataSource("living://" + mSerialNum);
            } else {
                mMediaPlayer.setDataSource(mIntentUri.toString());
            }
            mMediaPlayer.start();
            if (lastPlayPosition != 0) {
                seekPositionRunable = new Runnable() {
                    @Override
                    public void run() {
                        if (mMediaPlayer != null) {
                            mMediaPlayer.seekTo(lastPlayPosition);
                        }
                    }
                };
                mHandler.postDelayed(seekPositionRunable, 1000);
            }
            mHandler.removeMessages(MSG_PROGRESS);
            mHandler.sendEmptyMessage(MSG_PROGRESS);
            updatePausePlay();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable");

        mSurface = new Surface(surface);
        if (mLiving) {
            mLivingRetryCount = 0;
            startLiving();
        } else {
            startMediaPlayer();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureDestroyed");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.setLocationListener(null);
            mMediaPlayer = null;
        }

        stopLiving();
        mSurface = null;
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureSizeChanged to width = " + width + " height = " + height);
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //Log.i(TAG, "onSurfaceTextureUpdated");

        mHandler.removeMessages(MSG_BUFFER_TIMEOUT);
        mHandler.removeMessages(MSG_BUFFERING_START);
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mTipPrompt.setVisibility(View.INVISIBLE);
        }
    }

    private void handleIntent(Intent intent) {
        mIntentUri = intent.getData();
        Log.i(TAG, "mIntentUri = " + mIntentUri);
        if (mIntentUri == null) {
            finish();
            return;
        }
        showBack(true);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        if (mIntentUri.getScheme().compareTo("file") == 0) {
            mRemoteFile = false;
            String path = mIntentUri.toString().replace("file://", "");
            File file = new File(path);
            filePath = path;
            mActionbarTitle = Util.name2DateString(file.getName());
//            mTvSize.setText(Util.fileSizeMsg(path));
//            mTvRes.setText(Util.getResolution(path));
            setTitle(mActionbarTitle);
            if (mActionbarTitle == null)
                mActionbarTitle = sdf.format(new Date(file.lastModified()));
        } else {
            mRemoteFile = true;
            mFlDownload.setVisibility(View.VISIBLE);
            finfo = (FileInfo) intent.getSerializableExtra(KEY_FILE_NAME);
            filePath = finfo.name;
            if (filePath != null)
                mActionbarTitle = Util.name2DateString(filePath);
            if (mActionbarTitle == null) {
                long time = intent.getLongExtra(KEY_FILE_TIME, 0);
                if (time > 0) {
                    mActionbarTitle = sdf.format(new Date(intent.getLongExtra(KEY_FILE_TIME, 0)));
                } else if (filePath != null) {
                    mActionbarTitle = filePath;
                }
            }
            if (filePath != null) {
                if (filePath.contains(".ts")) {
                    if (mMapTrackView.isMapAvailable()) {
                        mMapTrackView.setGPSDataFromType(MapTrackView.GPS_REMOTE_MP4_TS);
                    }
                } else {
                    isRemoteMp4 = true;
                    if (mMapTrackView.isMapAvailable()) {
                        mMapTrackView.setGPSDataFromType(MapTrackView.GPS_REMOTE_MP4);
                    }
                }
                filePath = filePath.replace(".ts", ".gps");
                String path = "";
                if (isRemoteMp4) {
                    path = finfo.path + filePath;
                } else {
                    path = "/~cache/" + filePath;
                }
                Log.i(TAG, "gps url path = " + path);
                if (mMapTrackView.isMapAvailable()) {
                    mMapTrackView.drawRemoteTrackLine(path);
                }
            }
            setTitle(mActionbarTitle);
            mLiving = intent.getBooleanExtra(KEY_LIVING_FLAG, false);
            mSerialNum = intent.getStringExtra(KEY_LIVING_SN);
            String livingJson = intent.getStringExtra(KEY_LIVING_JSON);
            if (livingJson != null) {
                try {
                    mIntentJson = new JSONObject(livingJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        invalidateOptionsMenu();
    }

    private String stringForTime(long millis) {
        int totalSeconds = (int) millis / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return String.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private long mLastPosition = -2;
    private void updateProgress() {
        if (mMediaPlayer != null) {
            if (!mLiving) {
                int postion = mMediaPlayer.getCurrentPosition();
                int duration = mMediaPlayer.getDuration();

                //Log.e("VideoPlay", "Time:" + (duration - postion) + ", " + mMediaPlayer.isPlaying());
                // 设置500ms的阈值
                if ((mLastPosition == postion)
                        && (duration != 0)  && (postion != 0) && mMediaPlayer.isPlaying()) {
                    mLastPosition = -2;
                    mPlaybackCompleted = 1;
                    //mStartImage.setImageResource(R.drawable.play_video);
                    //mStartImage.setVisibility(View.VISIBLE);
                    mPlayerSeekBar.setProgress(0);
                    mTime.setText(stringForTime(0));
                    mMediaPlayer.seekTo(duration);
                    mMediaPlayer.seekTo(0);
                    mMediaPlayer.pause();
                    mMediaPlayer.resume();
//                    mMediaPlayer.start();
                    //mMediaPlayer.pause();
                } else {
                    mLastPosition = postion;
                    mPlaybackCompleted = 0;
                    mTime.setText(stringForTime(postion));
                    mPlayerSeekBar.setProgress(postion);
                    Log.e("VideoPlay", "postion:" + postion);
                }
                //mLastPosition = postion;
                GPSData data = findBestPointGPSData();
                if (data != null) {
                    drawVideoLocation(data);
                }
            } else {
                updatePausePlay();
                mDuration.setText(stringForTime(mMediaPlayer.getPastDurationFromLastPlayback()));
            }
        }
    }

    private GPSData findBestPointGPSData() {
        if (mGPSDataMap == null)
            return null;
        int postion = mMediaPlayer.getCurrentPosition();
        int time = postion / 1000;
        return mGPSDataMap.get(mFirstGPSDataTime + time);
    }

    private void doPauseResume() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                if (mProgressBar.getVisibility()==View.VISIBLE){
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mTipPrompt.setVisibility(View.INVISIBLE);
                }
                if (mPlaybackCompleted == 1) {
                    mMediaPlayer.seekTo(0);
                    mPlaybackCompleted = 0;
                    mHandler.removeMessages(MSG_PROGRESS);
                    mHandler.sendEmptyMessage(MSG_PROGRESS);
                } else {
                    mMediaPlayer.pause();
                    updatePausePlay();
                }
            } else {
//                if (mMediaPlayer.getCurrentPosition() == 0 && mGPSDataMap != null) {
//                    mGPSDataMap.clear();
//                }
                mMediaPlayer.resume();
                updatePausePlay();
            }
        }
    }

    private void updatePausePlay() {
        if (mMediaPlayer.isPlaying()) {
            mStartImage.setVisibility(View.GONE);
            mStartImage.setImageResource(R.drawable.pause_video);
        } else {
            mStartImage.setImageResource(R.drawable.play_video);
            mStartImage.setVisibility(View.VISIBLE);
        }
    }

    private void showTouchMsg(Drawable icon, String title) {
        if (mToast == null) {
            mToast = new Toast(this);
            mToast.setView(mToastView);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.CENTER, 0, 0);
        }
        mToastView.setCompoundDrawables(icon, null, null, null);
        mToastView.setText(title);
        mToast.show();
    }

    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    protected static final int MSG_PROGRESS = 0x110;
    protected static final int MSG_DURATION = 0x111;
    protected static final int MSG_BUFFERING_START = 0x112;
    protected static final int MSG_BUFFERING_END = 0x113;
    protected static final int MSG_HIDE_CONTROL_BOTTOMBAR = 0x114;
    protected static final int MSG_LIVE_HEARTBEAT = 0x115;
    protected static final int MSG_LIVE_TIMEOUT = 0x116;
    protected static final int MSG_STREAM_CHECK = 0x117;
    protected static final int MSG_VOICE_HANDLING = 0x118;
    protected static final int MSG_VOICE_VOL = 0x119;
    protected static final int MSG_VOICE_RECORD = 0x11a;
    protected static final int MSG_REQUEST_TIMEOUT = 0x11b;
    protected static final int MSG_BUFFER_TIMEOUT = 0x11c;
    protected static final int MSG_START_LIVE = 0x11d;

    private int mPlaybackCompleted = 0;
    long mLastStreamSize = 0;
    long mLastDuration = 0;

    static int[] sVolDrawables = {
            R.drawable.v1,
            R.drawable.v2,
            R.drawable.v3,
            R.drawable.v4,
            R.drawable.v5,
            R.drawable.v6,
            R.drawable.v7,
    };

    String mOSSAccessKeyID = "LTAIKe1Jrhpitzc4";
    String mOSSAccessKeySecret = "aEhBl5nkj0CvY4Wz1c5XalOa5Xpi9Q";
    private static final float BASE_NUMBER = 32768;


    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100: {
                    isCanRotate = true;
                }
                case MSG_BUFFER_TIMEOUT: {
//                    Toast.makeText(VideoActivity.this, R.string.video_buffer_failed, Toast.LENGTH_LONG).show();
//                    finish();
                    return;
                }
                case MSG_REQUEST_TIMEOUT: {
                    if (mLivingRetryCount < 6) {
                        //retry after 2 seconds, try 3 times about half a minute
                        mHandler.removeMessages(MSG_REQUEST_TIMEOUT);
                        mHandler.removeMessages(MSG_START_LIVE);
                        mHandler.sendEmptyMessageDelayed(MSG_START_LIVE, 2000);
                        mLivingRetryCount += 2;
                    } else {
                        Toast.makeText(VideoActivity.this, R.string.video_failed, Toast.LENGTH_LONG).show();
                        finish();
                    }
                    return;
                }
                case MSG_VOICE_RECORD: {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 100};
                    vibrator.vibrate(pattern, -1);
                    mIsRecording = true;
                    mLastVoiceFile = Config.CARDVR_PATH + "/myvoice-" + Build.SERIAL + getHumanTime(System.currentTimeMillis()) + ".aac";
                    doVoiceRecord2(mLastVoiceFile);
                    mVolume_container.setVisibility(View.VISIBLE);
                    //mVoiceVolume.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessage(MSG_VOICE_VOL);
                    mVoiceToast.setText(getResources().getString(R.string.voice_cancel));
                    mVoiceToast.show();
                }
                break;
                case MSG_VOICE_VOL: {
                    if (mVolume_container.getVisibility() == View.VISIBLE && mIsRecording && mMediaRecorder != null) {
                        int step = 0;
                        step = (int) ((float) 7 * mMediaRecorder.getMaxAmplitude() / BASE_NUMBER);
                        if (step >= 6) step = 6;
                        else if (step <= 0) step = 0;

                        mVoiceVolume.setImageResource(sVolDrawables[step]);
                        this.sendEmptyMessageDelayed(MSG_VOICE_VOL, 100);
                    }
                }
                break;
                case MSG_VOICE_HANDLING: {
                }
                break;
                case MSG_STREAM_CHECK: {
                    if (mAliyunOSSDataSource != null) {
                        this.sendEmptyMessageDelayed(MSG_STREAM_CHECK, 15 * 1000);
                        long curSize = mAliyunOSSDataSource.getFileSize();
                        long duration = mMediaPlayer.getPastDurationFromLastPlayback();
                        if (curSize == mLastStreamSize && duration == mLastDuration) {
                            Log.d(TAG, "File size in server not changed, stop living now");
                            AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);
                            builder.setTitle(R.string.live_size);
                            builder.setMessage(R.string.video_view);
                            builder.setPositiveButton(R.string.ok, new OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    storeLiving();
                                    onBackPressed();
                                }
                            });
                            builder.setNegativeButton(R.string.cancel, new OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onBackPressed();
                                }
                            });
                            builder.create().show();
                            stopLiving();
                        } else {
                            mLastStreamSize = curSize;
                            mLastDuration = duration;
                        }
                    }
                }
                break;
                case MSG_PROGRESS:
                    if (mNeedUpdateSeekBar)
                        updateProgress();
                    sendEmptyMessageDelayed(MSG_PROGRESS, UPDATE_PROGRESS_DELAY_MILLIS);
                    break;
                case MSG_DURATION:
                    mPlayerSeekBar.setMax(msg.arg1);
                    mDuration.setText(stringForTime(msg.arg1));
                    mTvTime.setText(stringForTime(msg.arg1));
                    // 计算平均速度
                    updateView(mAllGps);
                    break;
                case MSG_BUFFERING_START:
                    if (mIntentUri.toString().startsWith("http")) {
                        if (mMediaPlayer != null && mMediaPlayer.isEOF() != 1) {
                            mProgressBar.setVisibility(View.VISIBLE);
                            mTipPrompt.setVisibility(View.VISIBLE);
                            mHandler.removeMessages(MSG_PROGRESS);
                        }
                    } else if (mLiving) {
                        int percent = msg.arg1;
                        mProgressBar.setVisibility(View.VISIBLE);
                        mTipPrompt.setText(getResources().getString(R.string.video_buffering) + " " + percent + "%");
                        mTipPrompt.setVisibility(View.VISIBLE);
                        if (!mIsPlaying && mAliyunOSSDataSource != null) {
                            if (!this.hasMessages(MSG_BUFFER_TIMEOUT))
                                this.sendEmptyMessageDelayed(MSG_BUFFER_TIMEOUT, 15 * 1000);
                        }
                    }
                    break;
                case MSG_BUFFERING_END:
                    if (mIntentUri.toString().startsWith("http")) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mTipPrompt.setVisibility(View.INVISIBLE);
                        mHandler.sendEmptyMessage(MSG_PROGRESS);
                    } else if (mLiving) {
                        mIsPlaying = true;
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mTipPrompt.setVisibility(View.INVISIBLE);
                        mSaveImage.setEnabled(true);
                        this.removeMessages(MSG_STREAM_CHECK);
                        this.sendEmptyMessageDelayed(MSG_STREAM_CHECK, 15 * 1000);
                        this.removeMessages(MSG_BUFFER_TIMEOUT);
                    }
                    break;
                case MSG_HIDE_CONTROL_BOTTOMBAR:
                    hideControlBar();
                    break;
                case MSG_LIVE_HEARTBEAT:
                    sendLiveHeartbeat();
                    break;
                case MSG_LIVE_TIMEOUT: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);
                    builder.setTitle(R.string.live_timeout);
                    builder.setMessage(R.string.video_view);
                    builder.setPositiveButton(R.string.ok, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            storeLiving();
                            onBackPressed();
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onBackPressed();
                        }
                    });
                    builder.create().show();
                    stopLiving();
                }
                break;
                case MSG_START_LIVE:
                    startLiving();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
        mTime.setText(stringForTime(arg1));
    }

    private boolean mNeedUpdateSeekBar = true;
    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        mNeedUpdateSeekBar = false;
        mHandler.removeMessages(MSG_PROGRESS);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (mMediaPlayer != null) {
            mHandler.sendEmptyMessageDelayed(MSG_PROGRESS, UPDATE_PROGRESS_DELAY_MILLIS);
            mMediaPlayer.seekTo(progress);
            refreshStartImage();
        }
        mNeedUpdateSeekBar = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }


    MODE mMode;

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                VideoActivity.this.finish();
            }
        });
    }

    @Override
    public void onError(Exception ex) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                VideoActivity.this.finish();
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
    public void onDeleteDVRFile(final boolean success) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();
                if (success) {
                    Toast.makeText(VideoActivity.this, R.string.tip_delete_success, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("DELETE_FILE",finfo==null?"":finfo.path);
                    setResult(RESULT_OK,intent);
                    finish();
                } else
                    Toast.makeText(VideoActivity.this, R.string.tip_delete_fail, Toast.LENGTH_SHORT).show();
            }
        });
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

    @Override
    public void landscape() {
        if (!isCanRotate) {
            return;
        }
        // 重力感应设置横屏
        if (mClick) {
            if (!mIsLand && !mClickPort) {
                return;
            } else {
                mClickLand = true;
                mClick = false;
                mIsLand = true;
            }
        } else {
            if (!mIsLand) {
                lockRotate();
                fullScreen();
                mIsLand = true;
                mClick = false;
            }
        }
    }

    @Override
    public void landscape_left() {
        if (!isCanRotate) {
            return;
        }
        if (mClick) {
            if (!mIsLand && !mClickPort) {
                return;
            } else {
                mClickLand = true;
                mClick = false;
                mIsLand = true;
            }
        } else {
            if (!mIsLand) {
                lockRotate();
                fullScreen(true);
                mIsLand = true;
                mClick = false;
            }
        }
    }

    @Override
    public void portrait() {
        if (!isCanRotate) {
            return;
        }
        // 重力感应竖屏
        if (mClick) {
            if (mIsLand && !mClickLand) {
                return;
            } else {
                mClickPort = true;
                mClick = false;
                mIsLand = false;
            }
        } else {
            if (mIsLand) {
                lockRotate();
                exitFullScreen();
                mIsLand = false;
                mClick = false;
            }
        }
    }

    enum MODE {
        NUL, BRIGHTNESS, VOLUMN, SEEK
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStart.x = rawX;
                mTouchStart.y = rawY;
                mMode = MODE.NUL;
                break;
//            case MotionEvent.ACTION_MOVE:
//                int xAxisOffset = rawX - mTouchStart.x;
//                int yAxisOffset = rawY - mTouchStart.y;
//                if (mMode == MODE.NUL) {
//                    if (Math.abs(xAxisOffset) > dip2px(20)) {
//                        mMode = MODE.SEEK;
//                    } else if (Math.abs(yAxisOffset) > dip2px(20)) {
//                        if (mTouchStart.x < v.getWidth() / 2)
//                            mMode = MODE.BRIGHTNESS;
//                        else
//                            mMode = MODE.VOLUMN;
//                    }
//                } else if (Math.abs(xAxisOffset) > 0 || Math.abs(yAxisOffset) > 0) {
//                    if (mMode == MODE.BRIGHTNESS) {
//                        WindowManager.LayoutParams lp = getWindow().getAttributes();
//                        float value = lp.screenBrightness * 100f;
//                        value += yAxisOffset < 0 ? 1 : -1;
//                        if (value > 100)
//                            value = 100;
//                        else if (value < 0)
//                            value = 0;
//
//                        lp.screenBrightness = value / 100;
//                        getWindow().setAttributes(lp);
//                        showTouchMsg(mBrightnessIcon, String.valueOf((int) value));
//                    } else if (mMode == MODE.VOLUMN) {
//
//                        int per = mMaxVolume / 45;
//                        mCurrentVolume += yAxisOffset < 0 ? per : -per;
//                        if (mCurrentVolume > mMaxVolume)
//                            mCurrentVolume = mMaxVolume;
//                        else if (mCurrentVolume < 0)
//                            mCurrentVolume = 0;
//
//                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume / 100, 0);
//                        showTouchMsg(mVolumnIcon, String.valueOf(mCurrentVolume / 100));
//                    } else {
//                        if (!mLiving) {
//                            SeekBar seekbar = mPlayerSeekBar;
//                            int value = seekbar.getProgress();
//                            int per = seekbar.getMax() / 100;
//                            value += xAxisOffset > 0 ? per : -per;
//                            if (value > seekbar.getMax())
//                                value = seekbar.getMax();
//                            else if (value < per)
//                                value = 0;
//                            if (mMediaPlayer != null) {
//                                mMediaPlayer.seekTo(value);
//                            }
//                            seekbar.setProgress(value);
//                            if (xAxisOffset > 0)
//                                showTouchMsg(mSeekIconforward, stringForTime(value));
//                            else
//                                showTouchMsg(mSeekIconBackward, stringForTime(value));
//                        }
//                    }
//
//                    mTouchStart.x = rawX;
//                    mTouchStart.y = rawY;
//                }
//                break;
            case MotionEvent.ACTION_UP:
                if (mMode == MODE.NUL) {
                    doHideShowControlBar();
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra1, int extra2) {
        switch (what) {
            case MediaPlayer.MEDIA_DURATION_UPDATE:
                Message msg = mHandler.obtainMessage(MSG_DURATION, extra1, 0);
                mHandler.sendMessage(msg);
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public int onVideoLocationChange(final GPSData data) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                if (mMapTrackView.isMapAvailable()) {
                    mMapTrackView.drawTrackCar(data);
                }
                mTvCurrentSpeed.setText(String.valueOf(GPSFile.currentSpeed(data)) + getString(R.string.kmh));
            }
        });
        return 0;
    }

    private void drawVideoLocation(final GPSData data) {
        mGPSDataList.add(data);
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                if (mMapTrackView.isMapAvailable()) {
                    mMapTrackView.drawTrackCar(data);
                }
            }
        });
    }
    // TODO: add when gps data invalid

    private void handlerGpsDataError(List<GPSData> list){
        if (CollectionUtils.isEmpty(list)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvGpsErr.setVisibility(View.VISIBLE);
                }
            });
            return;
        }
    }


    @Override
    public boolean onBuffeing(int what, int extra1, int extra2) {
        Log.d(TAG, "onBuffeing:" + what + " extra1 = " + extra1);
        if (what == MediaPlayer.MEDIAPLAYER_BUFFERING_START) {
            Message msg = new Message();
            msg.what = MSG_BUFFERING_START;
            msg.arg1 = extra1;
            mHandler.sendMessage(msg);
        } else if (what == MediaPlayer.MEDIAPLAYER_BUFFERING_END) {
            mHandler.sendEmptyMessage(MSG_BUFFERING_END);
        }
        return true;
    }

    @Override
    public int onVideoLocationDataBuffer(final ByteBuffer buffer) {
        final List<GPSData> list = GPSFile.parseGPSList(buffer.array(), false, true, false);
        mAllGps = list;
        updateViewRunalbe = new Runnable() {

            @Override
            public void run() {
                updateView(list);
                if (mMapTrackView.isMapAvailable()) {
                    mMapTrackView.setGPSDataFromType(MapTrackView.GPS_LOCAL_MP4);
                    mMapTrackView.drawTrackLine(list);
                }
            }
        };
        mHandler.postDelayed(updateViewRunalbe, 4000);
        return 0;
    }

    private void updateView(List<GPSData> list) {
        if (CollectionUtils.isEmpty(list))
            return;
        mTvKM.setText(String.valueOf(GPSFile.totalMails(list)));
        int second = 0;
        if (mMediaPlayer != null) {
            second = mMediaPlayer.getDuration() / 1000;
            mTvTime.setText(DateUtils.getTime(second));
        }
        String avgSpeed = String.format("%.01f", GPSFile.avgSpeed(list, second));
        mTvSpeed.setText(avgSpeed);
    }

    @Override
    public void onPreDrawLineTrack() {

    }

    @SuppressLint("UseSparseArrays")
    @Override
    public void onAfterDrawLineTrack(List<GPSData> list) {
        if (list != null && list.size() > 0) {
            mAllGps = list;
            mGPSDataMap = new HashMap<Integer, GPSData>();
            boolean finded = false;
            for (GPSData data : list) {
                if (!finded && data.time != 0) {
                    mFirstGPSDataTime = data.time;
                    finded = true;
                }
                mGPSDataMap.put(data.time, data);
            }
            if (mRemoteFile) {
                updateView(list);
            }
        }
        handlerGpsDataError(list);
    }

    @Override
    public void onBackPressed() {
        mPauseFromBack = true;
        if (mVideoShareLayout.getVisibility() == View.VISIBLE) {
            if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                mMediaPlayer.resume();
            }
            return;
        }
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mHandler.removeMessages(MSG_HIDE_CONTROL_BOTTOMBAR);
            if (!isCanRotate) {
                return;
            }
            mClick = true;
            lockRotate();
            exitFullScreen();
            return;
        } else {
            super.onBackPressed();
        }
    }

    public void exitFullScreen() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        int newUiOptions = getWindow().getDecorView().getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        getActionBar().show();
        setVideoFullScreenMode(false);
        mMapContainer.setVisibility(View.VISIBLE);
        showControlBar();
        mFullScreen.setImageResource(R.drawable.fullscreen_video);
    }

    private void fullScreen() {
        fullScreen(false);
    }

    private void fullScreen(boolean left) {
        if (left) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        getActionBar().hide();

        int newUiOptions = getWindow().getDecorView().getSystemUiVisibility();
        // Navigation bar hiding: Backwards compatible to ICS.
        // if (Build.VERSION.SDK_INT >= 14) {
        // newUiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        // }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setVideoFullScreenMode(true);
        mMapContainer.setVisibility(View.GONE);
        mFullScreen.setImageResource(R.drawable.small_screen_black);
    }

    private void setVideoFullScreenMode(boolean bFull) {
        mIsVideoFullScreenMode = bFull;
//        mRlTop.setVisibility(View.VISIBLE);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mVideoPreviewContainer.getLayoutParams();
        if (bFull) {
            mRlTop.setVisibility(View.GONE);
            mMarginBottom = lp.bottomMargin;
            lp.bottomMargin = 0;
            mBottomView.setBackgroundColor(Color.TRANSPARENT);
            mBottomView.setVisibility(View.GONE);
            mFullScreen.setVisibility(View.GONE);
            mReturn.setVisibility(View.VISIBLE);
            mLLInfo.setVisibility(View.GONE);
            mLLOperation.setVisibility(View.GONE);
            mTime.setTextColor(Color.WHITE);
            mDuration.setTextColor(Color.WHITE);
        } else {
            //mRlTop.setVisibility(View.VISIBLE);
            lp.bottomMargin = mMarginBottom;
            mReturn.setVisibility(View.GONE);
            mBottomView.setVisibility(View.VISIBLE);
            mFullScreen.setVisibility(View.VISIBLE);
            mBottomView.setBackgroundColor(Color.WHITE);
            mLLInfo.setVisibility(View.VISIBLE);
            mLLOperation.setVisibility(View.VISIBLE);
            mTime.setTextColor(getResources().getColor(R.color.background));
            mDuration.setTextColor(getResources().getColor(R.color.background));
        }
        mVideoPreviewContainer.setLayoutParams(lp);
    }

    private void doHideShowControlBar() {
        //if (!mIsVideoFullScreenMode) {
        //doPauseResume();
        //} else {
        if (mMediaPlayer == null || mPlaybackCompleted == 1) {
            return;
        }
        if (!showingControlViews) {
            showControlBar();
        } else {
            hideControlBar();
        }
    }

    public void hideControlBar() {
        mHandler.removeMessages(MSG_HIDE_CONTROL_BOTTOMBAR);
        showingControlViews = false;
        mStartImage.setVisibility(View.GONE);
        if (mIsVideoFullScreenMode) {
            mBottomView.setVisibility(View.INVISIBLE);
        }
    }

    public void showControlBar() {
        showingControlViews = true;
        mHandler.removeMessages(MSG_HIDE_CONTROL_BOTTOMBAR);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROL_BOTTOMBAR, HIDE_CONTROL_DELAY_MILLIS);
        refreshStartImage();
        mStartImage.setVisibility(View.VISIBLE);
        mBottomView.setVisibility(View.VISIBLE);
    }

    private void refreshStartImage() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mStartImage.setImageResource(R.drawable.pause_video);
        } else {
            mStartImage.setImageResource(R.drawable.play_video);
        }
    }

    boolean mIsRecording = false;
    String mLastVoiceFile = null;
    long mLastVolTime = 0;
    int mUpIndex = 0;
    Toast mVoiceToast;
    MediaRecorder mMediaRecorder;

    void doVoiceRecord2(final String voiceFile) {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setAudioChannels(2);
        mMediaRecorder.setAudioSamplingRate(48000);
        mMediaRecorder.setAudioEncodingBitRate(128000);
        mMediaRecorder.setOutputFile(voiceFile);
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        } catch (IllegalStateException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private OnAudioFocusChangeListener mOnAudioFocusChangeListener = new OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.d(TAG, "onAudioFocusChange:focusChange = " + focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:        //-1
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:        //-2
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:    //-3
                    break;
            }
        }
    };
}
