package com.goluk.a6.control;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Switch;

import com.alibaba.fastjson.JSON;
import com.goluk.a6.cpstomp.CPStompClient;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.DeviceSleepSettingGetRequest;
import com.goluk.a6.http.request.DeviceSleepSettingPostRequest;
import com.goluk.a6.http.request.MessageGetRequest;
import com.goluk.a6.http.request.MessageSetRequest;
import com.goluk.a6.http.request.QueryAutoSyncVideoRequest;
import com.goluk.a6.http.responsebean.AutoSyncVideoResult;
import com.goluk.a6.http.responsebean.AutoUploadBean;
import com.goluk.a6.http.responsebean.DeviceSleepSettingResult;
import com.goluk.a6.http.responsebean.MessageResult;
import com.goluk.a6.http.responsebean.ProtocolMessage;
import com.goluk.a6.http.responsebean.ServerBaseResult;
import com.goluk.a6.internation.CustomLoadingDialog;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.IPageNotifyFn;
import com.goluk.a6.internation.UserUtils;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 编辑昵称
 *
 * @author mobnote
 */
public class MessageActivity extends BaseActivity implements IRequestResultListener, View.OnClickListener {
    // 保存数据的loading
    private CustomLoadingDialog mCustomProgressDialog = null;
    private MessageSetRequest messageSetRequest = null;
    private Switch mUrgent;
    private Switch mCatch;
    private Switch mException;
    private Switch mSyncVideo;
    private View.OnTouchListener forbiddenSwipeListener;
    private CPStompClient cpStompClient;
    private boolean isSyncVideo;
    private DeviceSleepSettingPostRequest uploadVideoSetRequest;
    private DeviceSleepSettingGetRequest uploadVideoGetRequest;
    private final int UPLOAD_VIDEO_GET_SETTING = 10001;
    private final int UPLOAD_VIDEO_SET_SETTING = 10002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_activitiy);
        showBack(true);
        setTitle(getString(R.string.message_setting));
        initView();
    }

    // 初始化控件
    public void initView() {
        mUrgent = (Switch) findViewById(R.id.switch_argent);
        mCatch = (Switch) findViewById(R.id.catch_exception);
        mException = (Switch) findViewById(R.id.switch_exception);
        mSyncVideo = (Switch) findViewById(R.id.switch_sync_video);
        mCustomProgressDialog = new CustomLoadingDialog(this, getString(R.string.str_save_fail));
        mUrgent.setOnClickListener(this);
        mCatch.setOnClickListener(this);
        mException.setOnClickListener(this);
        mSyncVideo.setOnClickListener(syncSwitchClickListener);
        forbiddenSwipeListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return event.getActionMasked() == MotionEvent.ACTION_MOVE;
            }
        };
        mUrgent.setOnTouchListener(forbiddenSwipeListener);
        mException.setOnTouchListener(forbiddenSwipeListener);
        mCatch.setOnTouchListener(forbiddenSwipeListener);
        getData();
        uploadVideoGetRequest = new DeviceSleepSettingGetRequest(UPLOAD_VIDEO_GET_SETTING,this);
        uploadVideoSetRequest = new DeviceSleepSettingPostRequest(UPLOAD_VIDEO_SET_SETTING,this);
        uploadVideoGetRequest.get(mApp.serverImei);
    }

    private View.OnClickListener syncSwitchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean isSync2Cloud = ((Switch) v).isChecked();
            mCustomProgressDialog.show();
            uploadVideoSetRequest.post(mApp.serverImei,"autoUploadEventVideo",isSync2Cloud?1:0);
        }
    };



    public void getData() {
        MessageGetRequest request = new MessageGetRequest(1, this);
        request.get(mApp.getMyInfo().uid);
    }

    /**
     * 修改用户名称
     */
    private void save(String name, int value) {
        if (!UserUtils.isNetDeviceAvailable(this)) {
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
        } else {
            messageSetRequest = new MessageSetRequest(0, this);
            messageSetRequest.get(mApp.getMyInfo().uid, name, value);
            mCustomProgressDialog.show();
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        hideProgressDialog();
        if (requestType == 0) {
            ServerBaseResult baseResult = (ServerBaseResult) result;
            if (baseResult == null || baseResult.code != 0) {
                GolukUtils.showToast(this, getString(R.string.user_personal_save_failed));
                return;
            }
            GolukUtils.showToast(null, getString(R.string.setting_ok));
            getData();
        } else if (requestType == 1) {
            MessageResult bean = (MessageResult) result;
            if (bean == null || bean.code != 0 || bean.data == null) {
                return;
            }
            fillData(bean.data);
        } else if (requestType == 2) {
            AutoSyncVideoResult autoSyncVideoResult = (AutoSyncVideoResult) result;
            if (autoSyncVideoResult == null || autoSyncVideoResult.code != 0 || autoSyncVideoResult.data == null) {
                return;
            }
            if (autoSyncVideoResult.data.size() == 0) {
                GolukUtils.showToast(null, getString(R.string.please_bound_device));
                return;
            }
            if (autoSyncVideoResult.data.get(0).flag == 1) {
                isSyncVideo = true;
                mSyncVideo.setChecked(true);
            } else {
                isSyncVideo = false;
                mSyncVideo.setChecked(false);
            }
        }else if (requestType == UPLOAD_VIDEO_GET_SETTING){
            DeviceSleepSettingResult settingResult = (DeviceSleepSettingResult) result;
            if (settingResult == null || settingResult.code != 0 || settingResult.data == null) {
                return;
            }

            if (settingResult.data.autoUploadEventVideo == 1) {
                isSyncVideo = true;
                mSyncVideo.setChecked(true);
            } else {
                isSyncVideo = false;
                mSyncVideo.setChecked(false);
            }
        }else if (requestType == UPLOAD_VIDEO_SET_SETTING){
            ServerBaseResult result2= (ServerBaseResult) result;
            if (result2!=null&&result2.code==0){
                GolukUtils.showToast(null,getString(R.string.setting_ok));
            }else {
                GolukUtils.showToast(null,getString(R.string.setting_failed));
            }
        }
    }

    private void fillData(MessageResult.MessageBean bean) {
        mUrgent.setChecked(bean.emergencyAlert == 1);
        mException.setChecked(bean.exceptionAlert == 1);
        mCatch.setChecked(bean.snapAlert == 1);
    }

    @Override
    public void onClick(View view) {
        String name = "";
        int value = ((Switch) view).isChecked() ? 1 : 0;
        if (view.getId() == R.id.switch_argent) {
            name = "emergencyAlert";
        } else if (view.getId() == R.id.switch_exception) {
            name = "exceptionAlert";
        } else if (view.getId() == R.id.catch_exception) {
            name = "snapAlert";
        }
        save(name, value);
    }

    private void hideProgressDialog() {
        if (mCustomProgressDialog.isShowing()) {
            mCustomProgressDialog.close();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
