package com.goluk.a6.control.browser;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TimingLogger;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.a6.common.event.ConnectEvent;
import com.goluk.a6.common.util.FileMediaType;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.BuildConfig;
import com.goluk.a6.control.CarWebSocketClient;
import com.goluk.a6.control.CarWebSocketClient.CarWebSocketClientCallback;
import com.goluk.a6.control.Config;
import com.goluk.a6.control.R;
import com.goluk.a6.control.dvr.RemoteCameraConnectManager;
import com.goluk.a6.control.dvr.UserItem;
import com.goluk.a6.control.util.DownloadTask;
import com.goluk.a6.control.util.HttpDownloadManager;
import com.goluk.a6.control.util.HttpRequestManager;
import com.goluk.a6.internation.GolukUtils;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import likly.dollar.$;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class RemoteFileActivity extends BaseActivity implements CarWebSocketClientCallback,
        RemoteCameraConnectManager.OnRemoteFileListChange {

    private static final String TAG = "RemoteFileActivity";

    public static final String KEY_TYPE_REMOTE_FILE = "key_type_remote_file";
    public static final String KEY_TYPE_REMOTE_FILE_FROM = "from";
    public static final int TYPE_REMOTE_FILE_LOCK = 1;
    public static final int TYPE_REMOTE_FILE_CAPTURE = 2;
    public static final int TYPE_REMOTE_FILE_LOOP = 3;
    public static final int TYPE_REMOTE_FILE_DOWNLOADING = 4;

    public static final String LOCK_PATH = Config.REMOTE_LOCK_PATH;
    public static final String CAPTURE_PATH = Config.REMOTE_CAPTURE_PATH;
    public static final String LOOP_PATH = Config.REMOTE_LOOP_PATH;
    //虚构
    public static final String DOWNLOADING_PATH = "/downloading";

    private FileListAdapter mAdapter;
    private StickyGridHeadersGridView mGridView;
    //未加锁，所有对mFileList, mDownloadInfos的操作需在UI线程
    private List<FileInfo> mFileList = new ArrayList<FileInfo>();
    private List<FileInfo> mSelectFileList = new ArrayList<FileInfo>();
    private Map<DownloadTask, FileInfo> mDownloadInfos = new HashMap<DownloadTask, FileInfo>();
    private ProgressBar mProgressBar;
    private TextView mTVDownload;
    private TextView mTVDelete;
    private TextView mTVSelect;
    private FileInfo mClickedFileInfo;

    private String mCurrentPath = "";

    private boolean mSelectMode = false;
    private RelativeLayout mNoFile;
    private int mType;
    private RadioGroup mTabRadioGroup;
    private SwipeRefreshLayout mswLayout;
    private LinearLayout mLLOperation;
    private ImageView mIvView;
    private LinearLayout mLLNotCon;
    private boolean isChooseWifi;
    private boolean deleteActived = false;
    private TimingLogger timingLogger = new TimingLogger("RemoteFile", "RefreshTime");
    private boolean fromPreview = false;


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle arg0) {
        if (BuildConfig.DEBUG) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
        super.onCreate(arg0);
        Intent data = getIntent();
        if (data != null) {
            fromPreview = data.getBooleanExtra(KEY_TYPE_REMOTE_FILE_FROM, false);
        }
        setContentView(R.layout.activity_remote_file);
        showBack(true);
        setTitle(R.string.preview_cling_title);
        mswLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mNoFile = (RelativeLayout) findViewById(R.id.rl_empty);
        mGridView = (StickyGridHeadersGridView) findViewById(R.id.remote_file_gridview);
        mLLOperation = (LinearLayout) findViewById(R.id.ll_operation_bar);
        mGridView.setOnItemClickListener(new ItemClickListener());
//        mGridView.setOnItemSelectedListener(new ItemSelectedListener());
//        mGridView.setOnItemLongClickListener(new ItemLongClickListener());
        mLLNotCon = (LinearLayout) findViewById(R.id.not_connect_cling);
        mLLNotCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToWifi();
            }
        });
        findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToWifi();
            }
        });
        mAdapter = new FileListAdapter(this, mFileList, true);
        mGridView.setAdapter(mAdapter);

        mTabRadioGroup = (RadioGroup) findViewById(R.id.remote_file_fragmen_tab);
        mTabRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.remote_file_lock:
                        mType = TYPE_REMOTE_FILE_LOCK;
                        runFileList(LOCK_PATH);
                        break;
                    case R.id.remote_file_capture:
                        mType = TYPE_REMOTE_FILE_CAPTURE;
                        runFileList(CAPTURE_PATH);
                        break;
                    case R.id.remote_file_loop:
                        mType = TYPE_REMOTE_FILE_LOOP;
                        runFileList(LOOP_PATH);
                        break;
                    case R.id.remote_file_downloading:
                        mType = TYPE_REMOTE_FILE_DOWNLOADING;
                        runFileList(DOWNLOADING_PATH);
                        break;
                }
            }
        });

        mTVDelete = (TextView) findViewById(R.id.delete);
        mTVSelect = (TextView) findViewById(R.id.phone_select);
        mTVDownload = (TextView) findViewById(R.id.tv_down);
        mTVDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (FileInfo info : mSelectFileList) {
                    if (info.isDirectory)
                        downloadFileInDir(info);
                    else
                        downloadFile(info);
                }
                RemoteCameraConnectManager.instance().refreshDownloadingFileList();
                toggleEditMode();
            }
        });
        mTVDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSelectFileList.size() == 0) {
                    return;
                }
                String msg = getString(R.string.delete_video_count, mSelectFileList.size());
                AlertDialog formatDialog = new AlertDialog.Builder(RemoteFileActivity.this)
                        .setTitle(R.string.hint)
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (RemoteCameraConnectManager.supportWebsocket()) {
                                    JSONObject jso = new JSONObject();
                                    try {
                                        jso.put("action", "delete");
                                        JSONArray array = new JSONArray();
                                        for (FileInfo fi : mSelectFileList) {
                                            JSONObject file = new JSONObject();
                                            file.put("name", fi.name);
                                            file.put("path", fi.path);
                                            file.put("size", fi.lsize);
                                            file.put("dir", fi.isDirectory);
                                            file.put("time", fi.modifytime);
                                            file.put("sub", fi.sub);
                                            array.put(file);
                                        }
                                        jso.put("list", array);
                                        Log.i(TAG, "jso.toString() = " + jso.toString());
                                        deleteActived = true;
                                        HttpRequestManager.instance().requestWebSocket(jso.toString());
                                        showDeletingDialog();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    for (FileInfo info : mSelectFileList) {
                                        String path = info.path + info.name;
                                        doDeleteFile(path);
                                    }
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();
                formatDialog.show();
            }
        });
        mProgressBar = (ProgressBar) findViewById(R.id.remote_file_progressbar);
        mType = 1;
        switch (mType) {
            case TYPE_REMOTE_FILE_LOCK:
                runFileList(LOCK_PATH);
                ((RadioButton) findViewById(R.id.remote_file_lock)).setChecked(true);
                break;
            case TYPE_REMOTE_FILE_CAPTURE:
                runFileList(CAPTURE_PATH);
                ((RadioButton) findViewById(R.id.remote_file_capture)).setChecked(true);
                break;
            case TYPE_REMOTE_FILE_LOOP:
                runFileList(LOOP_PATH);
                ((RadioButton) findViewById(R.id.remote_file_loop)).setChecked(true);
                break;
            case TYPE_REMOTE_FILE_DOWNLOADING:
                runFileList(DOWNLOADING_PATH);
                ((RadioButton) findViewById(R.id.remote_file_downloading)).setChecked(true);
            default:
                Log.e(TAG, "wrong type, finish activity");
                finish();
        }
        if (RemoteCameraConnectManager.supportWebsocket())
            CarWebSocketClient.instance().registerCallback(this);
        if (RemoteCameraConnectManager.instance() != null)
            RemoteCameraConnectManager.instance().addOnRemoteFileListChange(this);
        mswLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mswLayout.setRefreshing(false);
                refresh();
            }
        });
        mTVSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSelectAll()) {
                    for (FileInfo info : mFileList)
                        info.selected = false;
                    mSelectFileList.clear();
                    mTVDelete.setEnabled(false);
                    showAllView(true);
                    mAdapter.setSelectMode(mSelectMode);
                } else {
                    mSelectFileList.clear();
                    for (FileInfo info : mFileList) {
                        if (info.name.equals(".."))
                            continue;
                        if (isInDownloadList(info)) {
                            continue;
                        }
                        info.selected = true;
                        mSelectFileList.add(info);
                    }
                    showAllView(false);
                    if (mSelectFileList.size() != 0 && mSelectMode) {
                        mTVDelete.setEnabled(true);
                    }
                    mAdapter.setSelectMode(mSelectMode);
                }
            }
        });
        showEdit();
        mIvView = (ImageView) findViewById(R.id.image);
        AnimationDrawable ad = (AnimationDrawable) getResources().getDrawable(
                R.drawable.connect_anim);
        mIvView.setBackgroundDrawable(ad);
        ad.start();
    }

    private ProgressDialog mDialogDeleting;

    private void showDeletingDialog() {
        mDialogDeleting = new ProgressDialog(this);
        mDialogDeleting.setTitle(R.string.hint);
        mDialogDeleting.setMessage(getString(R.string.tip_deleting_file));
        mDialogDeleting.setCanceledOnTouchOutside(false);
        mDialogDeleting.setCancelable(false);
        mDialogDeleting.show();
    }

    private void dismissDeletingDialog() {
        if (mDialogDeleting != null && mDialogDeleting.isShowing()) {
            mDialogDeleting.dismiss();
            mDialogDeleting = null;
        }
    }

    private void goToWifi() {
        isChooseWifi = true;
        com.goluk.a6.control.util.Util.chooseWifi(RemoteFileActivity.this);
    }


    public void showAllView(boolean value) {
        if (value) {
            ((TextView) findViewById(R.id.phone_select)).setText(R.string.select_all);
            ((TextView) findViewById(R.id.phone_select)).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.select_all, 0, 0);
        } else {
            ((TextView) findViewById(R.id.phone_select)).setText(R.string.unselect_all);
            ((TextView) findViewById(R.id.phone_select)).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.unselect_all, 0, 0);
        }
    }


    private void refresh() {
        if (RemoteCameraConnectManager.instance().isConnected()) {
            mHandler.removeMessages(SHOW_PROGRESSBAR);
            mHandler.sendEmptyMessage(SHOW_PROGRESSBAR);
            RemoteCameraConnectManager.instance().refreshRemoteFileList(mCurrentPath);
            timingLogger.addSplit("refresh " + mCurrentPath);
        } else {
            mLLNotCon.setVisibility(VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (CarWebSocketClient.instance() != null)
            CarWebSocketClient.instance().unregisterCallback(this);
        RemoteCameraConnectManager.instance().removeOnRemoteFileListChange(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (fromPreview) {
            fromPreview = false;
            goToWifi();
        }

        if (isChooseWifi && RemoteCameraConnectManager.instance().isDisconnected()) {
            isChooseWifi = false;
            finish();
            return;
        }
        if (isChooseWifi && RemoteCameraConnectManager.instance().isConnecting()) {
            isChooseWifi = false;
            mLLNotCon.setVisibility(GONE);
            return;
        }
        if (RemoteCameraConnectManager.instance().isConnected()) {
            mLLNotCon.setVisibility(GONE);
        }
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed()");
        if (!mSelectMode) {
            finish();
        } else {
            toggleEditMode();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");
//        if (!mSelectMode) {
//            getMenuInflater().inflate(R.menu.car_files, menu);
//        } else {
//            if (mType != TYPE_REMOTE_FILE_DOWNLOADING) {
//                getMenuInflater().inflate(R.menu.car_files_multiple, menu);
//                MenuItem item = menu.findItem(R.id.car_file_select);
//                if (isSelectAll())
//                    item.setIcon(R.drawable.unselect_all);
//                else
//                    item.setIcon(R.drawable.select_all);
//            } else {
//                getMenuInflater().inflate(R.menu.car_files_multiple_download, menu);
//                MenuItem item = menu.findItem(R.id.car_file_download_select);
//                if (isSelectAll())
//                    item.setIcon(R.drawable.unselect_all);
//                else
//                    item.setIcon(R.drawable.select_all);
//            }
//        }
        return true;
    }

    public void showEdit() {
        getActionBar().getCustomView().findViewById(R.id.tv_right).setVisibility(VISIBLE);
        ((TextView) getActionBar().getCustomView().findViewById(R.id.tv_right)).setText(R.string.multiple);
        ((TextView) getActionBar().getCustomView().findViewById(R.id.tv_right)).setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        getActionBar().getCustomView().findViewById(R.id.tv_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFileList == null || mFileList.size() == 0) {
                    //Toast.makeText(RemoteFileActivity.this, "没有视频文件", Toast.LENGTH_SHORT).show();
                    if (mSelectMode) {
                        toggleEditMode();
                    }
                    return;
                }
                if (!RemoteCameraConnectManager.instance().isConnected()) {
                    return;
                }
                toggleEditMode();
            }
        });
    }

    public void toggleEditMode() {
        if (!mSelectMode) {
            mSelectMode = true;
            mSelectFileList.clear();
            setTitle(R.string.choose_video);
            for (FileInfo info : mFileList)
                info.selected = false;
            mAdapter.setSelectMode(mSelectMode);
            ((TextView) getActionBar().getCustomView().findViewById(R.id.tv_right)).setText(R.string.cancel);
            showOperation(true);
            for (int i = 0; i < mTabRadioGroup.getChildCount(); i++) {
                mTabRadioGroup.getChildAt(i).setEnabled(false);
            }
            adapterView();
            mswLayout.setEnabled(false);
            mTVDelete.setEnabled(false);
        } else {
            mSelectMode = false;
            setTitle(R.string.preview_cling_title);
            mSelectFileList.clear();
            for (FileInfo info : mFileList)
                info.selected = false;
            mAdapter.setSelectMode(mSelectMode);
            ((TextView) getActionBar().getCustomView().findViewById(R.id.tv_right)).setText(R.string.multiple);
            showOperation(false);
            for (int i = 0; i < mTabRadioGroup.getChildCount(); i++) {
                mTabRadioGroup.getChildAt(i).setEnabled(true);
            }
            mswLayout.setEnabled(true);
        }
    }

    public void adapterView() {
        mTVSelect.setText(R.string.select_all);
        mTVSelect.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.select_all, 0, 0);
    }

    private void showOperation(boolean b) {
        if (b) {
            mLLOperation.setVisibility(VISIBLE);
        } else {
            mLLOperation.setVisibility(GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.multiple) {
//            mSelectMode = true;
//            mSelectFileList.clear();
//            for (FileInfo info : mFileList)
//                info.selected = false;
//            mAdapter.setSelectMode(mSelectMode);
//            invalidateOptionsMenu();
//            return true;
//        } else if (item.getItemId() == R.id.refresh) {
//
//            return true;
//        } else if (item.getItemId() == R.id.car_file_delete) {
//
//            return true;
//        } else if (item.getItemId() == R.id.car_file_download) {
//
//            return true;
//        } else if (item.getItemId() == R.id.car_file_select) {
//            if (isSelectAll()) {
//                for (FileInfo info : mFileList)
//                    info.selected = false;
//                mSelectFileList.clear();
//                mAdapter.setSelectMode(mSelectMode);
//            } else {
//                mSelectFileList.clear();
//                for (FileInfo info : mFileList) {
//                    if (info.name.equals(".."))
//                        continue;
//                    info.selected = true;
//                    mSelectFileList.add(info);
//                }
//                mAdapter.setSelectMode(mSelectMode);
//            }
//            if (Build.VERSION.SDK_INT >= 14)
//                invalidateOptionsMenu();
//            return true;
//        } else if (item.getItemId() == R.id.car_file_download_select) {
//            if (isSelectAll()) {
//                for (FileInfo info : mFileList)
//                    info.selected = false;
//                mSelectFileList.clear();
//                mAdapter.setSelectMode(mSelectMode);
//            } else {
//                mSelectFileList.clear();
//                for (FileInfo info : mFileList) {
//                    if (info.name.equals(".."))
//                        continue;
//                    info.selected = true;
//                    mSelectFileList.add(info);
//                }
//                mAdapter.setSelectMode(mSelectMode);
//            }
//            if (Build.VERSION.SDK_INT >= 14)
//                invalidateOptionsMenu();
//            return true;
//        } else if (item.getItemId() == R.id.car_file_download_cancel) {
//            for (FileInfo info : mSelectFileList) {
//                String path = info.path + info.name;
//                DownloadTask task = HttpDownloadManager.instance().getDownloadTask(path);
//                if (task != null) {
//                    HttpDownloadManager.instance().cancelDownload(task);
//                }
//            }
//            mSelectMode = false;
//            mSelectFileList.clear();
//            for (FileInfo info : mFileList)
//                info.selected = false;
//            mAdapter.setSelectMode(mSelectMode);
//            RemoteFileActivity.this.invalidateOptionsMenu();
//            RemoteCameraConnectManager.instance().refreshDownloadingFileList();
//        } else if (item.getItemId() == android.R.id.home) {
//            onBackPressed();
//        }

        return false;
    }

    //更新文件列表
    private boolean runFileList(final String filePath) {
        mHandler.removeMessages(SHOW_PROGRESSBAR);
        mHandler.sendEmptyMessage(SHOW_PROGRESSBAR);

		/*String url = "/";
        try {
			url = "http://" + RemoteCameraConnectManager.HTTP_SERVER_IP + ":" + RemoteCameraConnectManager.HTTP_SERVER_PORT +
					"/cgi-bin/Config.cgi?action=dir&property=path&value=" + URLEncoder.encode(filePath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Log.i(TAG,"url = " + url);
		HttpRequestManager.instance().requestHttp(url, new HttpRequestManager.OnHttpResponseListener(){

			@Override
			public void onHttpResponse(String result) {
				Log.i(TAG, "result = " + result);
				if(result == null)
					return;
				List<FileInfo> list = FileScanner.readStringXML(result, false);
				mCurrentPath = filePath;
				mHandler.removeMessages(SCAN_FINISHED);
				Message msg = mHandler.obtainMessage(SCAN_FINISHED, list);
				mHandler.sendMessage(msg);
			}

		});*/
        if (RemoteCameraConnectManager.instance() != null) {
            List<FileInfo> list = RemoteCameraConnectManager.instance().getRemoteFileList(filePath);
            mCurrentPath = filePath;
            mHandler.removeMessages(SCAN_FINISHED);
            Message msg = mHandler.obtainMessage(SCAN_FINISHED, list);
            mHandler.sendMessage(msg);
        }
        return true;
    }

    private List<FileInfo> getCurrentTabFiles() {
        return RemoteCameraConnectManager.instance().getRemoteFileList(mCurrentPath);
    }

    //下载文件
    private void downloadFile(FileInfo info) {
        if (!info.isDirectory) {
            String filePath = info.path + info.name;

            //如果需要下载的文件已经正在下载，先取消下载
            DownloadTask old = HttpDownloadManager.instance().getDownloadTask(filePath);
            if (old != null) {
                HttpDownloadManager.instance().cancelDownload(old);
                FileInfo finfo = mDownloadInfos.get(old);
                if (finfo != null) {
                    finfo.downloading = false;
                    finfo.downloadProgress = 0;
                    mAdapter.notifyDataSetChanged();
                }
            }

            DownloadTask task = new DownloadTask(filePath, mOnDownloadListener);
            mDownloadInfos.put(task, info);
            info.downloading = true;
            info.downloadProgress = task.getProgress();
            mAdapter.notifyDataSetChanged();
            HttpDownloadManager.instance().requestDownload(task);
        }
    }

    //下载文件夹下的所有文件
    private void downloadFileInDir(FileInfo info) {
        if (info.isDirectory) {
            String filePath = info.path + info.name;
            String url = null;
            try {
                url = "http://" + RemoteCameraConnectManager.HTTP_SERVER_IP + ":" + RemoteCameraConnectManager.HTTP_SERVER_PORT +
                        "/cgi-bin/Config.cgi?action=dir&property=path&value=" + URLEncoder.encode(filePath, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "url = " + url);
            HttpRequestManager.instance().requestHttp(url, new HttpRequestManager.OnHttpResponseListener() {

                @Override
                public void onHttpResponse(String result) {
                    Log.i(TAG, "result = " + result);
                    if (result == null)
                        return;
                    List<FileInfo> list = FileScanner.readStringXML(result, false);
                    Message msg = mHandler.obtainMessage(DOWNLOAD_FILE_IN_DIR, list);
                    mHandler.sendMessage(msg);
                }

            });
        }
    }
    public static final  int REQUEST_CODE_DELETE_FILE = 10001;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==this.RESULT_OK){
           if (requestCode==REQUEST_CODE_DELETE_FILE){

            String filePath = data.getStringExtra("DELETE_FILE");
            if (filePath!=null&&filePath.equals(mClickedFileInfo.path)){
                mFileList.remove(mClickedFileInfo);
                mAdapter.notifyDataSetChanged();
            }else {
                GolukUtils.showToast(null,getString(R.string.tip_delete_fail));
            }
           }
        }
    }

    //打开文件
    private void openFile(FileInfo info) {
        Intent intent = new Intent();
        if (info.fileType == FileMediaType.IMAGE_TYPE) {
            intent = new Intent(this, PhotoActivity.class);
            intent.putExtra(PhotoActivity.KEY_REMOTE, true);
            intent.putExtra(PhotoActivity.KEY_PHOTO_CURRENT, info.name);
            Bundle bundle = new Bundle();
            bundle.putString(PhotoActivity.KEY_JSON_STRING, getJsonStringForFileList(mFileList));
            intent.putExtras(bundle);
        } else if (info.fileType == FileMediaType.VIDEO_TYPE) {
            intent = new Intent(this, VideoActivity.class);
            String strUrl = "";
            try {
                strUrl = "http://" + RemoteCameraConnectManager.HTTP_SERVER_IP + ":" + RemoteCameraConnectManager.HTTP_SERVER_PORT +
                        "/cgi-bin/Config.cgi?action=download&property=path&value=" + URLEncoder.encode(info.getFullPath(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            int type = FileMediaType.getMediaType(info.getFullPath());
            intent.setDataAndType(Uri.parse(strUrl), FileMediaType.getOpenMIMEType(type));
            intent.putExtra(VideoActivity.KEY_FILE_NAME, info);
            startActivityForResult(intent,REQUEST_CODE_DELETE_FILE);
            return;
        } else {
            String strUrl = "";
            try {
                strUrl = "http://" + RemoteCameraConnectManager.HTTP_SERVER_IP + ":" + RemoteCameraConnectManager.HTTP_SERVER_PORT +
                        "/cgi-bin/Config.cgi?action=download&property=path&value=" + URLEncoder.encode(info.getFullPath(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            int type = FileMediaType.getMediaType(info.getFullPath());
            intent.setDataAndType(Uri.parse(strUrl), FileMediaType.getOpenMIMEType(type));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //删除文件
    private void doDeleteFile(String filePath) {
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
                            Toast.makeText(RemoteFileActivity.this, R.string.tip_delete_success, Toast.LENGTH_SHORT).show();
                            finish();
                        } else
                            Toast.makeText(RemoteFileActivity.this, R.string.tip_delete_fail, Toast.LENGTH_SHORT).show();

                    }

                });
            }

        });
    }

    private String getJsonStringForFileList(List<FileInfo> list) {
        Log.i(TAG, "===================");
        Log.i(TAG, "list size = " + list.size());
        for (FileInfo fi : list)
            Log.i(TAG, "" + fi.name);
        Log.i(TAG, "===================");

        try {
            JSONArray array = new JSONArray();
            for (FileInfo fi : list) {
                if (fi.fileType == FileMediaType.IMAGE_TYPE) {
                    JSONObject file = new JSONObject();
                    file.put("name", fi.name);
                    file.put("path", fi.path);
                    file.put("size", fi.lsize);
                    file.put("dir", fi.isDirectory);
                    file.put("time", fi.modifytime);
                    file.put("sub", fi.sub);
                    array.put(file);
                }
            }
            return array.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean isSelectAll() {
        return (mFileList.size() != 0 && mFileList.size() == mSelectFileList.size())
                || (mFileList.size() != 0 && mFileList.size() == mSelectFileList.size() + mDownloadInfos.keySet().size());
    }

    private boolean isUnSelectAll() {
        return mSelectFileList.size() == 0;
    }

    private static final int SCAN_FINISHED = 998;
    private static final int SHOW_PROGRESSBAR = 999;
    private static final int DISMISS_PROGRESSBAR = 1000;
    private static final int DOWNLOAD_FILE_IN_DIR = 1001;
    private static final int DELETE_SUCCESS = 1002;
    private static final int DELETE_FAILED = 1003;

    final Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_FINISHED:
                    timingLogger.dumpToLog();
                    mFileList.clear();
                    mDownloadInfos.clear();
                    @SuppressWarnings("unchecked")
                    List<FileInfo> list = (List<FileInfo>) msg.obj;
                    if (list.size() > 0) {
                        mNoFile.setVisibility(GONE);
                    } else {
                        mNoFile.setVisibility(VISIBLE);
                    }
                    mFileList.addAll(list);
                    Log.i(TAG, "==========================");
                    for (FileInfo info : mFileList) {
                        Log.i(TAG, "" + info.name);
                        info.downloading = false;
                        info.downloadProgress = 0;
                        String path = info.path + info.name;
                        DownloadTask task = HttpDownloadManager.instance().getDownloadTask(path);
                        if (task != null&&!task.isCanceled()) {
                            task.setListener(mOnDownloadListener);
                            mDownloadInfos.put(task, info);
                            info.downloading = true;
                            info.downloadProgress = task.getProgress();
                        }
                    }
                    Log.i(TAG, "==========================");
                    mAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    break;
                case SHOW_PROGRESSBAR:
                    mGridView.smoothScrollToPosition(0);
                    mNoFile.setVisibility(GONE);
                    mFileList.clear();
                    mDownloadInfos.clear();
                    mAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(VISIBLE);
                    break;
                case DISMISS_PROGRESSBAR:
                    mProgressBar.setVisibility(View.INVISIBLE);
                    break;
                case DOWNLOAD_FILE_IN_DIR:
                    @SuppressWarnings("unchecked")
                    List<FileInfo> downlist = (List<FileInfo>) msg.obj;
                    for (FileInfo info : downlist) {
                        if (info.isDirectory)
                            downloadFileInDir(info);
                        else
                            downloadFile(info);
                    }
                    break;
                case DELETE_SUCCESS:
                    List<FileInfo> remoteFiles = getCurrentTabFiles();
                    for (FileInfo fileInfo:mSelectFileList){
                        if (mFileList.contains(fileInfo)){
                            mFileList.remove(fileInfo);
                        }
                        if (remoteFiles.contains(fileInfo)) {
                            remoteFiles.remove(fileInfo);
                        }
                    }
                    toggleEditMode();
                    $.toast().text(R.string.tip_delete_success).show();
                    dismissDeletingDialog();
                    if (remoteFiles.isEmpty()) {
                        mNoFile.setVisibility(VISIBLE);
                    } else {
                        mNoFile.setVisibility(GONE);
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case DELETE_FAILED:
                    toggleEditMode();
                    GolukUtils.showToast(null,getString(R.string.tip_delete_fail));
                    break;
            }
        }
    };

    private class ItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if (position < 0 || position >= mFileList.size())
                return;
            mClickedFileInfo = mFileList.get(position);
            FileInfo finfo = mFileList.get(position);
            if (isInDownloadList(finfo)) {
                GolukUtils.showToast(RemoteFileActivity.this, getString(R.string.videodownloading));
                return;
            }
            if (!mSelectMode) {
                if (finfo.isDirectory) {
                    runFileList(finfo.path + finfo.name);
                } else {
                    openFile(finfo);
                    mAdapter.setCurrentPosition(position);
                }
            } else {
                if (finfo.name.equals("..")) {
                    return;
                }
                if (isInDownloadList(finfo)) {
                    GolukUtils.showToast(RemoteFileActivity.this, getString(R.string.videodownloading));
                    return;
                }
                if (mSelectFileList.contains(finfo)) {
                    finfo.selected = false;
                    mSelectFileList.remove(finfo);
                    mAdapter.notifyDataSetChanged();
                } else {
                    finfo.selected = true;
                    mSelectFileList.add(finfo);
                    mAdapter.notifyDataSetChanged();
                }
                boolean now = isSelectAll();
                showAllView(!now);
            }
            mTVDelete.setEnabled(mSelectFileList.size() != 0);
        }
    }


    public boolean isInDownloadList(FileInfo fileInfo) {
        for (FileInfo value : mDownloadInfos.values()) {
            if (fileInfo.name.equals(value.name)) {
                return true;
            }
        }
        return false;
    }

    private HttpDownloadManager.OnDownloadListener mOnDownloadListener = new HttpDownloadManager.OnDownloadListener() {

        @Override
        public void onDownloadStart(final DownloadTask task) {
            Log.i(TAG, "onDownloadStart()");
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    //Toast.makeText(getContext(), "开始下载", Toast.LENGTH_SHORT).show();
                    FileInfo finfo = mDownloadInfos.get(task);
                    if (finfo != null) {
                        finfo.downloading = true;
                        finfo.downloadProgress = task.getProgress();
                        mAdapter.notifyDataSetChanged();
                    }
                }

            });
        }

        @Override
        public void onDownloadEnd(final DownloadTask task, final boolean succeed) {
            Log.i(TAG, "onDownloadEnd:succeed = " + succeed);
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    FileInfo finfo = mDownloadInfos.get(task);
                    if (finfo != null) {
                        finfo.downloading = false;
                        finfo.downloadProgress = 0;
                        mAdapter.notifyDataSetChanged();
                    }
                    mDownloadInfos.remove(task);
                    if (succeed) {
                        showToast(getString(R.string.tip_download_success));
                    } else {
                        if (!RemoteCameraConnectManager.instance().isConnected()) {
                            showToast(getString(R.string.device_offline));
                        } else {
                            showToast(getString(R.string.tip_download_fail));
                        }
                    }
                    HttpDownloadManager.instance().cancelDownload(task);
                    RemoteCameraConnectManager.instance().refreshDownloadingFileList();
                }
            });
        }

        @Override
        public void onDownloadProgress(final DownloadTask task, final int progress) {
            Log.i(TAG, "onDownloadProgress:progress = " + progress);
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    FileInfo finfo = mDownloadInfos.get(task);
                    if (finfo != null) {
                        finfo.downloading = true;
                        finfo.downloadProgress = progress;
                        mAdapter.notifyDataSetChanged();
                    }
                }

            });
        }

    };

    private class ItemSelectedListener implements OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private class ItemLongClickListener implements OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            if (position < 0 || position >= mFileList.size())
                return true;
            FileInfo finfo = mFileList.get(position);
            if (mSelectMode) {
                mSelectMode = false;
                mSelectFileList.clear();
                for (FileInfo info : mFileList)
                    info.selected = false;
                mAdapter.setSelectMode(mSelectMode);
                if (Build.VERSION.SDK_INT >= 14)
                    invalidateOptionsMenu();
            } else {
                mSelectMode = true;
                mSelectFileList.clear();
                for (FileInfo info : mFileList)
                    info.selected = false;
                mAdapter.setSelectMode(mSelectMode);
                finfo.selected = true;
                mSelectFileList.add(finfo);
                mAdapter.notifyDataSetChanged();
                if (Build.VERSION.SDK_INT >= 14)
                    invalidateOptionsMenu();
            }
            return true;
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                finish();
//                showToast(getString(R.string.device_offline));
//                mLLNotCon.setVisibility(VISIBLE);
            }
        });
    }

    @Override
    public void onError(Exception ex) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                finish();
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
        if (!deleteActived) {
            return;
        }
        deleteActived = false;
        if (success){
            mHandler.sendEmptyMessage(DELETE_SUCCESS);
        }else {
            mHandler.sendEmptyMessage(DELETE_FAILED);
        }

    }

    @Override
    public void onSyncFile(String path, String type, List<FileInfo> list) {


    }

    @Override
    public void onRemoteFileListChange(String filePath, List<FileInfo> list) {
        if (filePath.equals(mCurrentPath)) {
            mHandler.removeMessages(SCAN_FINISHED);
            Message msg = mHandler.obtainMessage(SCAN_FINISHED, list);
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void onSetAutoSleepTime(int time) {


    }

    @Override
    public void onGsensorSensity(int sensity) {


    }

    @Override
    public void onSetBrightnessPercent(int percent) {


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
    public void onMobileStatus(String imei, boolean ready, int dBm, boolean enable, boolean connected, int type,
                               long usage, boolean registered, String flag) {


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
    public void onGsensor(final float x, final float y, final float z, final boolean passed) {
    }

    @Override
    public void onAdas(String key, boolean value) {


    }

    @Override
    public void onEDog(int value) {

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ConnectEvent event) {
        if (event.connect) {
            mLLNotCon.setVisibility(GONE);
            refresh();
        } else {
            mLLNotCon.setVisibility(VISIBLE);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

}
