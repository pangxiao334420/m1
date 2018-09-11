
package com.goluk.a6.control.dvr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.goluk.a6.common.event.ConnectEvent;
import com.goluk.a6.common.event.UpdateBindListEvent;
import com.goluk.a6.common.event.util.Event;
import com.goluk.a6.common.event.util.EventUtil;
import com.goluk.a6.control.CarControlApplication;
import com.goluk.a6.control.CarWebSocketClient;
import com.goluk.a6.control.Config;
import com.goluk.a6.control.R;
import com.goluk.a6.control.browser.FileInfo;
import com.goluk.a6.control.browser.FileScanner;
import com.goluk.a6.control.browser.RemoteFileActivity;
import com.goluk.a6.control.util.DownloadTask;
import com.goluk.a6.control.util.HttpDownloadManager;
import com.goluk.a6.control.util.HttpRequestManager;
import com.goluk.a6.control.util.NetworkListener;
import com.goluk.a6.control.util.NetworkListener.ServerInfo;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.BindAddRequest;
import com.goluk.a6.http.responsebean.BindAddResult;
import com.goluk.a6.internation.SharedPrefUtil;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RemoteCameraConnectManager implements CarWebSocketClient.CarWebSocketClientCallback, IRequestResultListener {

    private static final String TAG = "CarSvc_RemoteCameraConnectManager";

    public static final String KEY_PRESERVER_SERIALNO = "serialno";

    public static String HTTP_SERVER_IP = "";
    public static String HTTP_SERVER_PORT = "8080";
    public static String WEBSOCK_SERVER_PORT = "8081";

    private static final int STATUS_CONNECT = 1;
    private static final int STATUS_DISCONNECT = 2;
    private static final int STATUS_CONNECTTING = 3;

    private int mConntectStatus = STATUS_DISCONNECT;
    private static RemoteCameraConnectManager sIns;
    public static final int SIM_STATUS_UNKNOWN = -1;
    public static final int SIM_STATUS_NO_SIM = 0;
    public static final int SIM_STATUS_NO_NETWORK = 1;
    public static final int SIM_STATUS_NORMAL = 2;

    /**
     * -1 未知，0 未插卡 ，1 插卡 无网络 ，2插卡有网络
     */
    private int mSimStatus = -1;

    public static void create(Context ctx) {
        sIns = new RemoteCameraConnectManager(ctx);
    }

    public static RemoteCameraConnectManager instance() {
        return sIns;
    }

    public int getSimStatus() {
        return mSimStatus;
    }

    public static void destory() {
        if (sIns != null && sIns.mNetworkListener != null)
            sIns.mNetworkListener.deinit();
        CarWebSocketClient.destory();
    }

    public static boolean isOversea() {
        if (mCurrentServerInfo != null && mCurrentServerInfo.oversea)
            return true;
        return false;
    }

    public static boolean isHeadless() {
        if (mCurrentServerInfo != null && mCurrentServerInfo.headless)
            return true;
        return false;
    }

    public static boolean supportNewSetting() {
        if (mCurrentServerInfo != null && mCurrentServerInfo.newSetting)
            return true;
        return false;
    }

    public static boolean supportWebsocket() {
        if (mCurrentServerInfo != null && mCurrentServerInfo.supportWebsocket)
            return true;
        return false;
    }

    public static NetworkListener.ServerInfo getCurrentServerInfo() {
        return mCurrentServerInfo;
    }

    public Context mContext;
    private ListView mServerListView;
    private List<NetworkListener.ServerInfo> mServerList;
    private ServerListAdapter mServerListAdapter;
    private Dialog mServerDialog;
    private NetworkListener.ServerInfo mNoServer;
    private static NetworkListener.ServerInfo mCurrentServerInfo;
    private String mPreserverSerialNO = "";
    private CameraPreviewView mCameraPreviewView;
    public HashMap<String, String> historyList = SharedPrefUtil.getDevices();
    private NetworkListener mNetworkListener;
    private static final int PRESERVER_SERVER_WAIT_COUNT = 10;
    private int mSumOfServerChecked = 0;
    private List<FileInfo> mLockFileList = new ArrayList<FileInfo>();
    private List<FileInfo> mLoopFileList = new ArrayList<FileInfo>();
    private List<FileInfo> mCaptureFileList = new ArrayList<FileInfo>();
    private List<FileInfo> mDownloadingFileList = new ArrayList<FileInfo>();
    private List<OnRemoteFileListChange> mRemoteFileListeners =
            new ArrayList<OnRemoteFileListChange>();
    private String mVersion;
    private String mCurrentSSID = "";

    boolean mJustBindRequest = true;
    private boolean mSplashViewDismissed = true;
    private boolean uploadImei = false;

    public void setSplashViewDismissed() {
        mSplashViewDismissed = true;
    }

    public void showServerDialog(boolean justBindRequest) {
        mJustBindRequest = justBindRequest;
        mServerDialog.show();
    }

    public CameraPreviewView getCameraPreviewView() {
        return mCameraPreviewView;
    }

    public void setmCameraPreviewView(CameraPreviewView mCameraPreviewView) {
        this.mCameraPreviewView = mCameraPreviewView;
    }

    public void showServerDialog() {
        mJustBindRequest = false;
        showServerDialog(false);
    }

    public NetworkListener getNetworkListener() {
        return mNetworkListener;
    }

    public void release() {
        mContext = null;
        sIns = null;
        uploadImei = false;
        EventBus.getDefault().unregister(this);
    }

    public void setAutoConnectSerial(String sn) {
        mPreserverSerialNO = sn;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(KEY_PRESERVER_SERIALNO, mPreserverSerialNO);
        ed.commit();
        mHandler.sendEmptyMessage(MSG_DISCONNECT_SERVER);
        mNetworkListener.deinit();
        mNetworkListener.init(mContext.getApplicationContext(), mServerFoundCallBack);
    }

    private RemoteCameraConnectManager(Context ctx) {
        mContext = ctx;
        uploadImei = false;
        mNetworkListener = new NetworkListener();
        mNetworkListener.init(mContext.getApplicationContext(), mServerFoundCallBack);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
//        mPreserverSerialNO = sp.getString(KEY_PRESERVER_SERIALNO, "");
        mServerListView = new ListView(mContext.getApplicationContext());
        mServerList = new ArrayList<NetworkListener.ServerInfo>();
        mNoServer = new ServerInfo();
        mNoServer.ipAddr = mContext.getString(R.string.tip_no_searched_device);
        mNoServer.serialNo = mContext.getString(R.string.no_recorder);
        mNoServer.name = mContext.getString(R.string.no_recorder);
        mServerList.add(mNoServer);
        mServerListAdapter = new ServerListAdapter(mServerList, mContext.getApplicationContext());
        mServerListView.setAdapter(mServerListAdapter);
        mServerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String ip = mServerList.get(arg2).ipAddr;
                String name = mServerList.get(arg2).name;

                boolean supportWebsocket = mServerList.get(arg2).supportWebsocket;
                Log.i(TAG, "ip = " + ip);
                if (!name.equals(mContext.getString(R.string.no_recorder))) {
                    connectServer(ip, supportWebsocket);
                } else {
                    mCurrentServerInfo = null;
                }
                mServerDialog.dismiss();
            }

        });
        //mServerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.ip_setting);
        builder.setView(mServerListView);
//		builder.setPositiveButton(R.string.scan_recorder, new DialogInterface.OnClickListener(){
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				Intent intent = new Intent(mContext, MipcaActivityCapture.class);
//				intent.putExtra(MipcaActivityCapture.SHOW_SCAN_RECORDER_TIP, true);
//				Activity a = (Activity)mContext;
//				a.startActivityForResult(intent, CarAssistMainView.SCANNIN_GREQUEST_CODE);
//			}
//		});
        mServerDialog = builder.create();

        EventBus.getDefault().register(this);
    }

    private void connectServer(String ip, boolean supprotWebsocket) {
        setConnectStatus(STATUS_CONNECTTING);
        mHandler.removeMessages(MSG_CONNECT_TIMEOUT);
        needUploadImei();
        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        if (mWifiInfo != null) {
            mCurrentSSID = mWifiInfo.getSSID();
            // 删除系统获取SSID前后带"
            if (mCurrentSSID.startsWith("\""))
                mCurrentSSID = mCurrentSSID.substring(1);
            if (mCurrentSSID.endsWith("\""))
                mCurrentSSID = mCurrentSSID.substring(0, mCurrentSSID.length() - 1);
        }
        mHandler.sendEmptyMessageDelayed(MSG_CONNECT_TIMEOUT, 10000);
        if (supprotWebsocket) {
            final String uri = "ws://" + ip + ":" + WEBSOCK_SERVER_PORT;
            Log.i(TAG, "uri = " + uri);
            try {
                CarWebSocketClient.create(new URI(uri));
                CarWebSocketClient.instance().registerCallback(this);
                Logger.d("connect_anim 2 connect_anim  server", ip);
                CarWebSocketClient.instance().connect();
            } catch (Exception e) {
                Log.i(TAG, "Exception:" + e);
            }
        } else {
            String url = "http://" + ip + ":" + HTTP_SERVER_PORT +
                    "/cgi-bin/Config.cgi?action=get&property=CarDvr.Status.*";
            Log.i(TAG, "url = " + url);
            HttpRequestManager.instance().requestHttp(url, new HttpRequestManager.OnHttpResponseListener() {

                @Override
                public void onHttpResponse(String result) {
                    Log.i(TAG, "result = " + result);
                    if (result == null)
                        return;
                    String params[] = result.split("\n");
                    for (String str : params) {
                        try {
                            if (str.startsWith(Config.PROPERTY_CARDVR_STATUS_SERIALNO)) {
                                String serialNo = str.split("=")[1];
                                Message msg = mHandler.obtainMessage(MSG_CONNECT_SERVER, serialNo);
                                mHandler.removeMessages(MSG_CONNECT_SERVER);
                                mHandler.sendMessage(msg);
                            }
                        } catch (Exception e) {
                            Log.i(TAG, "Exception", e);
                        }
                    }
                }

            });
        }
    }

    private static final int MSG_UPDATE_SERVER_LIST = 1;
    private static final int MSG_CONNECT_SERVER = 2;
    private static final int MSG_DISCONNECT_SERVER = 3;
    private static final int MSG_CONNECT_TIMEOUT = 4;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_SERVER_LIST:
                    Log.i(TAG, "MSG_UPDATE_SERVER_LIST");
                    @SuppressWarnings("unchecked")
                    ArrayList<NetworkListener.ServerInfo> list = (ArrayList<NetworkListener.ServerInfo>) msg.obj;
                    mServerList.clear();
                    mServerList.addAll(list);
                    if (mCurrentServerInfo != null) {
                        NetworkListener.ServerInfo info;
                        int i = 0;
                        for (i = 0; i < mServerList.size(); i++) {
                            info = mServerList.get(i);
                            if (info.serialNo.equals(mCurrentServerInfo.serialNo)) {
                                mServerListAdapter.setCurrentSelect(info.serialNo);
                                HTTP_SERVER_IP = info.ipAddr;
                                if (!isConnected()) {
                                    setConnectStatus(STATUS_CONNECT);
                                }
                                mHandler.removeMessages(MSG_CONNECT_TIMEOUT);
                                break;
                            }
                        }
//                        if (i >= mServerList.size()) {
//                            mServerListAdapter.setCurrentSelect("0000");
//                            mCurrentServerInfo = null;
//                            setConnectStatus(STATUS_DISCONNECT);
//                        }
                        mSumOfServerChecked = 0;
                        if (mServerList.size() == 0) {
                            connectServer(mCurrentServerInfo.ipAddr, mCurrentServerInfo.supportWebsocket);
                            break;
                        }
//                    } else if (mPreserverSerialNO.equals("")) {
//                        mServerListAdapter.setCurrentSelect("0000");
//                        mCurrentServerInfo = null;
//                        setConnectStatus(STATUS_DISCONNECT);
//                        boolean match = false;
//                        for (NetworkListener.ServerInfo info : mServerList) {
//                            match = true;
//                            connectServer(info.ipAddr, info.supportWebsocket);
//                            break;
//                        }
//                        if (!match && mServerList.size() != 0 && mSplashViewDismissed) {
//                            if (mSumOfServerChecked > PRESERVER_SERVER_WAIT_COUNT) {
//                                mServerDialog.show();
//                                mSumOfServerChecked = 0;
//                            } else
//                                mSumOfServerChecked++;
//                        }
                    } else {
//                        mServerListAdapter.setCurrentSelect("0000");
//                        setConnectStatus(STATUS_DISCONNECT);
//                        if (mServerList.size() != 0 && mSplashViewDismissed)
//                            mServerDialog.show();
                        for (NetworkListener.ServerInfo info : mServerList) {
                            connectServer(info.ipAddr, info.supportWebsocket);
                            break;
                        }
                        mSumOfServerChecked = 0;
                    }
//                    if (mServerList.size() == 0) {
//                        mServerList.add(mNoServer);
//                    }
//                    mServerListAdapter.notifyDataSetChanged();
                    break;
                case MSG_CONNECT_SERVER:
                    Log.i(TAG, "MSG_CONNECT_SERVER");
                    String serialNo = (String) msg.obj;
                    for (NetworkListener.ServerInfo info : mServerList) {
                        if (info.serialNo.equals(serialNo)) {
                            mCurrentServerInfo = info;
                            Logger.d("connect_anim 5 connect_anim", info);
                            mServerListAdapter.setCurrentSelect(mCurrentServerInfo.serialNo);
                            setConnectStatus(STATUS_CONNECT);
                            mHandler.removeMessages(MSG_CONNECT_TIMEOUT);
                            HTTP_SERVER_IP = mCurrentServerInfo.ipAddr;
                            mPreserverSerialNO = mCurrentServerInfo.serialNo;
//                            historyList.put(mPreserverSerialNO, "");
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                            SharedPreferences.Editor ed = sp.edit();
                            ed.putString(KEY_PRESERVER_SERIALNO, mPreserverSerialNO);
                            ed.commit();
                            if (mServerDialog.isShowing())
                                mServerDialog.dismiss();
                            //UpgradeManager.instance().checkVersion(false);
                            if (mCameraPreviewView != null) {
                                mCameraPreviewView.refresh();
                            }
                            refreshall();
                        }
                    }
                    break;
                case MSG_DISCONNECT_SERVER:
                    Log.i(TAG, "MSG_DISCONNECT_SERVER");
                    mServerListAdapter.setCurrentSelect("0000");
                    mCurrentServerInfo = null;
                    setConnectStatus(STATUS_DISCONNECT);
                    if (mCameraPreviewView != null) {
                        mCameraPreviewView.refresh();
                    }
                    needUploadImei();
                    mLockFileList.clear();
                    mLoopFileList.clear();
                    mCaptureFileList.clear();
                    mDownloadingFileList.clear();
                    break;
                case MSG_CONNECT_TIMEOUT:
                    Log.i(TAG, "MSG_CONNECT_TIMEOUT");
                    mServerListAdapter.setCurrentSelect("0000");
                    mCurrentServerInfo = null;
                    setConnectStatus(STATUS_DISCONNECT);
                    if (mCameraPreviewView != null) {
                        mCameraPreviewView.refresh();
                    }
                    needUploadImei();
                    mLockFileList.clear();
                    mLoopFileList.clear();
                    mCaptureFileList.clear();
                    mDownloadingFileList.clear();
                    break;
            }
        }
    };

    public void refreshall() {
        refreshRemoteFileList(RemoteFileActivity.CAPTURE_PATH);
        refreshRemoteFileList(RemoteFileActivity.LOCK_PATH);
        refreshRemoteFileList(RemoteFileActivity.LOOP_PATH);
    }

    public boolean isConnected() {
        return mConntectStatus == STATUS_CONNECT;
    }

    public boolean isDisconnected() {
        return mConntectStatus == STATUS_DISCONNECT;
    }

    public boolean isConnecting() {
        return mConntectStatus == STATUS_CONNECTTING;
    }

    private void setConnectStatus(final int status) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mConntectStatus = status;
                if (mConntectStatus == STATUS_CONNECT) {
                    uploadImei = false;
                    EventBus.getDefault().post(new ConnectEvent(true));
                    if (mCameraPreviewView != null) {
                        mCameraPreviewView.showContect();
                    }
                } else if (mConntectStatus == STATUS_DISCONNECT) {
                    EventBus.getDefault().post(new ConnectEvent(false));
                    uploadImei = false;
                    if (mCameraPreviewView != null) {
                        mCameraPreviewView.showDiscontect();
                    }
                } else if (mConntectStatus == STATUS_CONNECTTING) {
                    if (mServerDialog.isShowing())
                        mServerDialog.dismiss();
                    if (mCameraPreviewView != null) {
                        mCameraPreviewView.showContectting();
                    }
                }
            }

        });
    }

    public boolean refreshRemoteFileList(final String filePath) {
        if (filePath.equals(RemoteFileActivity.LOCK_PATH))
            mLockFileList.clear();
        else if (filePath.equals(RemoteFileActivity.LOOP_PATH))
            mLoopFileList.clear();
        else if (filePath.equals(RemoteFileActivity.CAPTURE_PATH))
            mCaptureFileList.clear();
        else if (filePath.equals(RemoteFileActivity.DOWNLOADING_PATH)) {
            mDownloadingFileList.clear();
            refreshDownloadingFileList();
            return true;
        }
        String url = "/";
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
                if (filePath.equals(RemoteFileActivity.LOCK_PATH))
                    mLockFileList.addAll(list);
                else if (filePath.equals(RemoteFileActivity.LOOP_PATH))
                    mLoopFileList.addAll(list);
                else if (filePath.equals(RemoteFileActivity.CAPTURE_PATH))
                    mCaptureFileList.addAll(list);
                synchronized (mRemoteFileListeners) {
                    for (OnRemoteFileListChange l : mRemoteFileListeners) {
                        l.onRemoteFileListChange(filePath, list);
                    }
                }
            }

        });

        return true;
    }

    public List<FileInfo> getRemoteFileList(final String filePath) {
        if (filePath.equals(RemoteFileActivity.LOCK_PATH))
            return mLockFileList;
        else if (filePath.equals(RemoteFileActivity.LOOP_PATH))
            return mLoopFileList;
        else if (filePath.equals(RemoteFileActivity.CAPTURE_PATH))
            return mCaptureFileList;
        else if (filePath.equals(RemoteFileActivity.DOWNLOADING_PATH))
            return mDownloadingFileList;
        return new ArrayList<FileInfo>();
    }

    public void addOnRemoteFileListChange(OnRemoteFileListChange l) {
        synchronized (mRemoteFileListeners) {
            mRemoteFileListeners.add(l);
        }
    }

    public void removeOnRemoteFileListChange(OnRemoteFileListChange l) {
        synchronized (mRemoteFileListeners) {
            mRemoteFileListeners.remove(l);
        }
    }

    public void refreshDownloadingFileList() {
        mDownloadingFileList.clear();
        List<FileInfo> all = new ArrayList<FileInfo>();
        all.addAll(mCaptureFileList);
        all.addAll(mLockFileList);
        all.addAll(mLoopFileList);
        for (FileInfo info : all) {
            String path = info.path + info.name;
            DownloadTask task = HttpDownloadManager.instance().getDownloadTask(path);
            if (task != null) {
                mDownloadingFileList.add(info);
            }
        }

        synchronized (mRemoteFileListeners) {
            for (OnRemoteFileListChange l : mRemoteFileListeners) {
                l.onRemoteFileListChange(RemoteFileActivity.DOWNLOADING_PATH, mDownloadingFileList);
            }
        }
    }

    private NetworkListener.ServerFoundCallBack mServerFoundCallBack = new NetworkListener.ServerFoundCallBack() {

        @Override
        public void serverNotify(ArrayList<ServerInfo> list, boolean change) {
            if (list != null && mConntectStatus != STATUS_CONNECTTING) {
                Log.i(TAG, "list = " + list);
                Logger.d("connect_anim 1", list);
                Log.i(TAG, "change = " + change);
                mHandler.removeMessages(MSG_UPDATE_SERVER_LIST);
                Message msg = mHandler.obtainMessage(MSG_UPDATE_SERVER_LIST, list);
                mHandler.sendMessage(msg);
            }
        }

    };

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.e("Connect", "onOpen");
        try {
            JSONObject jso = new JSONObject();
            jso.put("action", "get");
            JSONArray items = new JSONArray();
            items.put(Config.PROPERTY_CARDVR_STATUS_SERIALNO);
            jso.put("list", items);
            jso.toString();
            Log.i(TAG, "jso.toString() = " + jso.toString());
            Logger.d("connect_anim 3 Server open websocket", jso.toString());
            HttpRequestManager.instance().requestWebSocket(jso.toString());

            sendGetImeiData();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送获取IMEI信息指令
     */
    private void sendGetImeiData() {
        try {
            JSONObject jso = new JSONObject();
            jso.put("f", "get");
            JSONArray items = new JSONArray();
            items.put("mobile");
            jso.put("what", items);
            HttpRequestManager.instance().requestWebSocket(jso.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i(TAG, "onClose");
        Logger.d("connect_anim onClose");
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mServerListAdapter.setCurrentSelect("0000");
                mCurrentServerInfo = null;
                setConnectStatus(STATUS_DISCONNECT);
            }

        });
    }

    @Override
    public void onError(Exception ex) {
        Logger.d("connect_anim ERROR");
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mServerListAdapter.setCurrentSelect("0000");
                mCurrentServerInfo = null;
                setConnectStatus(STATUS_DISCONNECT);
            }

        });
    }

    @Override
    public void onSetSerialNo(String serial) {
        Logger.d("connect_anim 4 SerialNo :" + serial);
        Message msg = mHandler.obtainMessage(MSG_CONNECT_SERVER, serial);
        mHandler.removeMessages(MSG_CONNECT_SERVER);
        mHandler.sendMessage(msg);
    }

    @Override
    public void onSetAbilityStatue(String ability) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setAbilityStatue(ability);
        }
    }

    @Override
    public void onSetVolumeStatue(int min, int max, int current) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setVolumeStatue(min, max, current);
        }
    }

    @Override
    public void onSetBrightnessStatue(int min, int max, int current) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setBrightnessStatue(min, max, current);
        }
    }

    @Override
    public void onSetWakeUpStatue(int value) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setWakeUpStatue(value);
        }
    }

    @Override
    public void onSetVoicePromptStatue(boolean enable) {
        if (mCameraPreviewView != null) {
//            mCameraPreviewView.setVoicePromptStatue(enable);
        }
    }

    @Override
    public void onSetDVRRecordStatus(boolean recording) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setRecordingButton(recording);
        }
    }

    @Override
    public void onSetDVRSDcardStatus(boolean mount) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setDVRSDcardStatus(mount);
        }
    }

    @Override
    public void onDirDVRFiles(String path, JSONArray array) {

    }

    @Override
    public void onDeleteDVRFile(boolean succes) {

    }

    @Override
    public void onSyncFile(String path, String type, List<FileInfo> list) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.onSyncFile(path, type, list);
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        BindAddResult bean = (BindAddResult) result;
        // 发送绑定成功Event
        EventUtil.sendBindDeviceSuccessEvent();

        if (uploadImei) {
            return;
        }
        if (bean != null && bean.code == 0 && bean.data != null) {
            uploadImei = true;
            historyList.put(mPreserverSerialNO, CarControlApplication.getInstance().currentImei);
            SharedPrefUtil.saveDevices(historyList);
            EventBus.getDefault().post(new UpdateBindListEvent());
        }
    }

    public interface OnRemoteFileListChange {
        public void onRemoteFileListChange(String filePath, List<FileInfo> list);
    }

    @Override
    public void onSetAutoSleepTime(int time) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setAutoSleepTime(time);
        }
    }

    @Override
    public void onGsensorSensity(int sensity) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setGsensorSensity(sensity);
        }
    }

    @Override
    public void onSetBrightnessPercent(int percent) {
        if (mCameraPreviewView != null) {

            mCameraPreviewView.setBrightnessPercent(percent);
        }
    }

    @Override
    public void onGsensorWakeup(int enable) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setGsensorWakeup(enable);
        }
    }

    @Override
    public void onGsensorLock(int enable) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setGsensorLock(enable);
        }
    }

    @Override
    public void onSoftApConfig(String ssid, String pwd) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setSoftApConfig(ssid, pwd);
        }
    }

    @Override
    public void onDvrSaveTime(int time) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setDvrSaveTime(time);
        }
    }

    @Override
    public void onDvrMode(String mode) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setDvrMode(mode);
        }
    }

    @Override
    public void onDvrLanguage(String lan) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.onDvrLanguage(lan);
        }
    }

    @Override
    public void onDvrMute(boolean mute) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setDvrMute(mute);
        }
    }

    @Override
    public void onDvrGps(String show) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setDvrGps(show);
        }
    }

    @Override
    public void onSdcardSize(long total, long left, long dvrdir) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setSdcardSize(total, left, dvrdir);
        }
    }

    @Override
    public void onUserList(String serialNum, int cloudID, ArrayList<UserItem> list) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setUserList(list);
        }
    }

    @Override
    public void onRecordStatus(boolean start, int num, int time) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setRecordStatus(start, num, time);
        }
    }

    public void needUploadImei() {
        uploadImei = false;
    }

    @Override
    public void onMobileStatus(String imei, boolean ready, int dBm, boolean enable,
                               boolean connected, int type,
                               long usage, boolean registered, String flag) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setMobileStatus(ready, enable, connected, type, usage,dBm);
        }
        if (ready) {
            if (connected) {
                mSimStatus = SIM_STATUS_NORMAL;
            } else {
                mSimStatus = SIM_STATUS_NO_NETWORK;
            }
        } else {
            mSimStatus = SIM_STATUS_NO_SIM;
        }

        // 发送Event
        EventUtil.sendSIMStateChangedEvent();

        Logger.v("onMobileStatus imei " + imei);
        if (!uploadImei) {
            if (!CarControlApplication.getInstance().isUserLoginToServerSuccess()) {
                return;
            }
            CarControlApplication.getInstance().currentImei = imei;
            boolean haveBind = CarControlApplication.getInstance().haveBound(imei);
            if (haveBind) {
                uploadImei = true;
                return;
            }

            // 如果在预览页面,显示绑定视图,让用户选择是否进行绑定
            if (mCameraPreviewView != null) {
                EventUtil.sendConnectedEvent(imei);
                return;
            }

            //bindDevice(imei);
            EventUtil.sendConnectedEvent(imei);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Event event) {
        // 开始绑定设备
        if (EventUtil.isBindDeviceEvent(event)) {
            final String imei = (String) event.data;
            bindDevice(imei);
        }
    }

    /**
     * 绑定设备
     *
     * @param imei
     */
    private void bindDevice(String imei) {
        if (TextUtils.isEmpty(imei) || TextUtils.isEmpty(mCurrentSSID) || TextUtils.isEmpty(mPreserverSerialNO))
            return;
        BindAddRequest request = new BindAddRequest(1, this);
        request.get(CarControlApplication.getInstance().getMyInfo().uid, "", mCurrentSSID, imei, mPreserverSerialNO);
    }

    @Override
    public void onSatellites(boolean enabled, int num, long timestamp, String nmea) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setSatellites(num,nmea);
        }
    }

    @Override
    public void onUpdate(int percent, String version) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setUpdate(percent, version);
        }
        mVersion = version;
    }

    @Override
    public void onCpuInfo(double cpuTemp, double pmuTemp, int core, int freq, boolean isFull,
                          boolean isAccOn) {
    }

    @Override
    public void onGsensor(final float x, final float y, final float z, final boolean passed) {
    }

    @Override
    public void onAdas(String key, boolean value) {
    }

    @Override
    public void onEDog(int value) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.setEDog(value);
        }
    }

    public String getSoftwareVersion() {
        return mVersion;
    }

    public boolean isSimConnected() {
        return mSimStatus == SIM_STATUS_NORMAL;
    }
}
