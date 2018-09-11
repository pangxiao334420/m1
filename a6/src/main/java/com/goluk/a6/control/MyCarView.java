package com.goluk.a6.control;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goluk.a6.common.event.ConnectEvent;
import com.goluk.a6.common.event.util.Event;
import com.goluk.a6.common.event.util.EventUtil;
import com.goluk.a6.common.map.MyCarMapView;
import com.goluk.a6.common.map.Point;
import com.goluk.a6.common.util.DateUtils;
import com.goluk.a6.common.util.H5Util;
import com.goluk.a6.control.dvr.DeviceStatusQueryer;
import com.goluk.a6.control.dvr.RemoteCameraConnectManager;
import com.goluk.a6.control.live.GolukLiveActivity;
import com.goluk.a6.control.live.LiveConstant;
import com.goluk.a6.control.ui.track.TrackListActivity;
import com.goluk.a6.control.util.AddressConvert;
import com.goluk.a6.control.util.DeviceUtil;
import com.goluk.a6.control.util.LocationUtil;
import com.goluk.a6.control.util.NetUtil;
import com.goluk.a6.control.util.Util;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.BindedByOtherRequest;
import com.goluk.a6.http.request.ShareLiveRequest;
import com.goluk.a6.http.responsebean.BindedByOtherResult;
import com.goluk.a6.http.responsebean.DeviceStatus;
import com.goluk.a6.http.responsebean.FamilyShareMeResult;
import com.goluk.a6.http.responsebean.TrackDetail;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.UserUtils;
import com.goluk.a6.internation.login.InternationUserLoginActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import likly.dollar.$;

import static android.content.Context.CLIPBOARD_SERVICE;
//18200257837/123456

public class MyCarView extends IPagerView implements IRequestResultListener, View.OnClickListener, DeviceStatusQueryer.DeviceStatusCallback, LocationUtil.OnLocationListener {
    private static final int REQUEST_CODE_SHARE_LINK = 1002;
    private static final int REQUEST_CODE_BINDED_BY_OTHER = 1003;

    private RelativeLayout mLayoutConnect;
    private TextView mBtnConnect;
    private Button mBtnLive;
    private ImageView mIvConnectAnima;
    private TextView mtvSim;
    private View mTitleView;
    private ImageView mBtnSetting, mBtnShare;

    //WebFragment newFragment;
    ShareLiveRequest liveRequest;
    ProgressDialog mProgressDialog;

    private CarControlApplication mApp;
    private Handler mHandler;

    // 是否点击了连接
    private boolean mIsClickConnect;
    // 当前Tab是否处于激活状态
    private boolean mIsActive;
    // 是否允许绑定
    private boolean mIsNeedBind;

    ///////
    private ProgressDialog mDialogBind;
    private View mLayoutBottom;
    private RelativeLayout mMapContainer;
    private MyCarMapView mMap;
    private ImageView mBtnLocation, mBtnHistoryTrack;
    private TextView mTvOnlineStatus, mTvLastestLocationLabel, mTvLastLocateTime, mTvDeviceLocation;
    private DeviceStatusQueryer mDeviceStatusQueryer;
    private DeviceStatus mDeviceStatus;
    private LocationUtil mLocationUtil;
    private AddressConvert mAddressConvert;

    public MyCarView(Context context) {
        super(context);
        initView();
        EventBus.getDefault().register(this);
    }

    public MyCarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MyCarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        mApp = CarControlApplication.getInstance();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.mycar_views, this);

        mLayoutBottom = findViewById(R.id.layout_bottom);
        mLayoutConnect = (RelativeLayout) findViewById(R.id.connect_cling);
        mIvConnectAnima = (ImageView) findViewById(R.id.image);
        mBtnConnect = (TextView) findViewById(R.id.btn_connect);
        mBtnLive = (Button) findViewById(R.id.btn_live);
        mtvSim = (TextView) findViewById(R.id.tv_sim);
        mMapContainer = (RelativeLayout) findViewById(R.id.map_container);
        mMap = MyCarMapView.create(getContext());
        mMapContainer.addView(mMap);
        mTvOnlineStatus = (TextView) findViewById(R.id.tv_online_status);
        mTvLastestLocationLabel = (TextView) findViewById(R.id.tv_lastest_location);
        mTvLastLocateTime = (TextView) findViewById(R.id.tv_last_locate_time);
        mTvDeviceLocation = (TextView) findViewById(R.id.tv_device_location);
        mBtnLocation = (ImageView) findViewById(R.id.btn_location);
        mBtnHistoryTrack = (ImageView) findViewById(R.id.btn_history_track);

        mBtnConnect.setOnClickListener(this);
        mBtnLive.setOnClickListener(this);
        mBtnLocation.setOnClickListener(this);
        mBtnHistoryTrack.setOnClickListener(this);

        AnimationDrawable connectAnima = (AnimationDrawable) getResources().getDrawable(
                R.drawable.connect_anim);
        mIvConnectAnima.setBackground(connectAnima);
        connectAnima.start();

        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setMessage(getContext().getString(R.string.str_loading_text));
        mProgressDialog.setCancelable(false);

        mHandler = new Handler();

        mDeviceStatusQueryer = new DeviceStatusQueryer(this);
        mDeviceStatusQueryer.start();

        mLocationUtil = new LocationUtil(getContext());
        mLocationUtil.setListener(this);

        mAddressConvert = new AddressConvert();

        liveRequest = new ShareLiveRequest(REQUEST_CODE_SHARE_LINK, this);

    }

    public void showMenu(boolean replaceActionBar) {
        if (mTitleView == null) {
            mTitleView = LayoutInflater.from(getContext()).inflate(R.layout.home_title_live, null);
            mBtnSetting = (ImageView) mTitleView.findViewById(R.id.btn_setting);
            mBtnShare = (ImageView) mTitleView.findViewById(R.id.btn_share);
            mBtnSetting.setOnClickListener(this);
            mBtnShare.setOnClickListener(this);

        }

        // 是否需要替换Title栏
        if (replaceActionBar)
            ((BaseActivity) getContext()).getActionBar().setCustomView(mTitleView);

        boolean enable = (CarControlApplication.getInstance().isUserLoginToServerSuccess()
                && CarControlApplication.getInstance().isBoundIMei());
        mBtnSetting.setImageResource(enable ? R.drawable.nav_setup_n : R.drawable.nav_setup_d);
        mBtnShare.setImageResource(enable ? R.drawable.nav_share_n : R.drawable.nav_share_d);
        mBtnSetting.setClickable(enable);
        mBtnShare.setClickable(enable);
    }

    public void showMenu() {
        showMenu(true);
    }

    /**
     * 显示SIM卡状态
     */
    private void showConnectView(boolean changeShowState) {
        if (changeShowState) {
            mIsClickConnect = false;
            mLayoutConnect.setVisibility(VISIBLE);
        } else {
            if (mLayoutConnect.getVisibility() == View.GONE)
                return;
        }

        if (RemoteCameraConnectManager.instance().isConnected()) {
            if (RemoteCameraConnectManager.instance().getSimStatus() == RemoteCameraConnectManager.SIM_STATUS_NO_NETWORK) {
                mtvSim.setText(R.string.sim_not_available);
                mtvSim.setVisibility(VISIBLE);
            } else if (RemoteCameraConnectManager.instance().getSimStatus() == RemoteCameraConnectManager.SIM_STATUS_NO_SIM) {
                mtvSim.setText(R.string.sim_not_inserted);
                mtvSim.setVisibility(VISIBLE);
            } else {
                mtvSim.setVisibility(GONE);
            }
        } else {
            mtvSim.setVisibility(GONE);
        }
    }

    private void toSetting() {
        if (!UserUtils.isNetDeviceAvailable(null)) {
            GolukUtils.showToast(null, getContext().getResources().getString(R.string.user_net_unavailable));
            return;
        }
        Intent intent = new Intent(getContext(), FeatureSettingActivity.class);
        getContext().startActivity(intent);
    }

    private void shareLive() {
        if (!UserUtils.isNetDeviceAvailable(getContext())) {
            GolukUtils.showToast(getContext(), getContext().getResources().getString(R.string.user_net_unavailable));
            return;
        }
        if (!CarControlApplication.getInstance().isBoundIMei()) {
            return;
        }
        mProgressDialog.show();
        liveRequest.get(CarControlApplication.getInstance().getMyInfo().uid);
    }

    private void getPosition() {
        Location location = mLocationUtil.getLstknownLocation();
        if (location == null) {
            $.toast().text(R.string.can_not_get_user_location).show();
            return;
        }

        updateMapUserLocation(new Point(location.getLatitude(), location.getLongitude()), true);
    }

    public void chooseWif() {
        if (GolukUtils.isFastDoubleClick()) {
            return;
        }
        if (!CarControlApplication.getInstance().isUserLoginToServerSuccess()) {
            Intent loginIntent = null;
            loginIntent = new Intent(getContext(), InternationUserLoginActivity.class);
            getContext().startActivity(loginIntent);
            return;
        }
        Util.chooseWifi(getContext());
        mIsClickConnect = true;
    }

    @Override
    public void onActivityCreate(Bundle savedInstanceState) {
    }

    @Override
    public boolean onCreateOptionsMenu(MenuInflater mi, Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onActivate() {
        mIsActive = true;

        if (mDeviceStatusQueryer != null)
            mDeviceStatusQueryer.start();

        // 设备被绑走对话框
        if (mDialogBindedByOther != null && !mDialogBindedByOther.isShowing())
            mDialogBindedByOther.show();
    }

    @Override
    public void onDeactivate() {
        mIsActive = false;

        if (mDeviceStatusQueryer != null)
            mDeviceStatusQueryer.pause();
    }

    @Override
    public void onActivityPause() {
        mLocationUtil.pauseLocation();
    }

    @Override
    public void onAcitvityResume() {
        mLocationUtil.startLocation();

        boolean isConnectedM1Wifi = NetUtil.isConnectedM1Wifi(getContext());
        if (isConnectedM1Wifi) {
            // 开始连接
            if (mIsActive && mIsClickConnect) {
                //if (!RemoteCameraConnectManager.instance().isConnected()) {
                EventUtil.sendStartConnectEvent();
                //}
                mIsClickConnect = false;
                mIsNeedBind = true;

                // 10s超时重置允许绑定状态
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsNeedBind = false;
                        // 绑定超时提示
                        if (isBindLoadingShowing())
                            $.toast().text(R.string.bind_failed).show();

                        dismissBindLoading();
                    }
                }, 30 * 1000);

                showBindLoading();
            }
        }
    }

    @Override
    public void onActivityDestroy() {
        EventBus.getDefault().unregister(this);

        if (mLocationUtil != null)
            mLocationUtil.stopLocation();

        if (mDeviceStatusQueryer != null)
            mDeviceStatusQueryer.stop();
    }

    @Override
    public void onActivityStart() {
    }

    @Override
    public void onActivityStop() {
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void refresh() {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    /**
     * 显示绑定Loading
     */
    private void showBindLoading() {
        if (mDialogBind == null) {
            mDialogBind = new ProgressDialog(getContext());
            mDialogBind.setCancelable(false);
            mDialogBind.setMessage(getContext().getString(R.string.binding_device));
        }
        if (!mDialogBind.isShowing())
            mDialogBind.show();
    }

    private void dismissBindLoading() {
        if (mDialogBind != null && mDialogBind.isShowing())
            mDialogBind.dismiss();
    }

    private boolean isBindLoadingShowing() {
        return mDialogBind != null && mDialogBind.isShowing();
    }

    private String generateShareDialogMessage() {
        String s = getResources().getString(R.string.live_share) + "\n" + getResources().getString(R.string.str_share_validity) + ": ";
        if (CarControlApplication.getInstance().userLiveValidity == -1) {
            return s + getResources().getString(R.string.str_longtime);
        } else {
            return s + CarControlApplication.getInstance().userLiveValidity / 60 + " " + getResources().getString(R.string.str_unit_hour);
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == REQUEST_CODE_SHARE_LINK) {
            mProgressDialog.dismiss();
            final FamilyShareMeResult result1 = (FamilyShareMeResult) result;
            if (result1 != null && result1.data != null && result1.code == 0) {
                AlertDialog formatDialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.hint)
                        .setMessage(generateShareDialogMessage())
                        .setPositiveButton(getContext().getString(R.string.copylink), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                String mUrl = H5Util.getShareUrl(result1.data.url);
                                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("", mUrl);
                                clipboard.setPrimaryClip(clip);
                                GolukUtils.showToast(getContext(), getContext().getString(R.string.copy));
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();
                formatDialog.show();
            }
        } else if (requestType == REQUEST_CODE_BINDED_BY_OTHER) {
            BindedByOtherResult bindedResult = (BindedByOtherResult) result;
            if (bindedResult != null && bindedResult.data != null) {
                showBindedByOtherDialog(bindedResult.data);
            }
        }
    }

    private AlertDialog mDialogBindedByOther;

    /**
     * 显示设备被其他用户绑走提示
     */
    private void showBindedByOtherDialog(List<BindedByOtherResult.DeviceInfo> data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDialogBindedByOther = null;
            }
        });
        String msg = "";
        if (data.size() == 1) {
            final BindedByOtherResult.DeviceInfo deviceInfo = data.get(0);
            msg = getContext().getString(R.string.one_device_binded_by_other, deviceInfo.name, deviceInfo.username);
        } else if (data.size() > 1) {
            msg = getContext().getString(R.string.more_device_binded_by_other, data.size());
        }
        builder.setMessage(msg);
        builder.setCancelable(false);
        mDialogBindedByOther = builder.create();

        if (mIsActive)
            mDialogBindedByOther.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ConnectEvent event) {
        if (!mApp.haveBoundSuccess())
            showConnectView(true);
    }

    private void toLivePage() {
        if (!GolukUtils.isNetworkConnected(getContext())) {
            $.toast().text(R.string.user_net_unavailable).show();
            return;
        }
        Intent intent = new Intent(getContext(), GolukLiveActivity.class);
        intent.putExtra(LiveConstant.KEY_CAR_IMEI, CarControlApplication.getInstance().currentImei);
        if (mDeviceStatus != null) {
            intent.putExtra(LiveConstant.KEY_CAR_STATE, mDeviceStatus.state);
            intent.putExtra(LiveConstant.KEY_DEVICE_CAMERA_NUMBER, mDeviceStatus.cameraSN.size());
        }
        getContext().startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_setting:
                toSetting();
                break;
            case R.id.btn_share:
                shareLive();
                break;
            case R.id.btn_connect:
                chooseWif();
                break;
            case R.id.btn_location:
                getPosition();
                break;
            case R.id.btn_history_track:
                gotoHitoryTrack();
                break;
            case R.id.btn_live:
                if (GolukUtils.isFastDoubleClick()) {
                    return;
                }
                toLivePage();
                break;
        }
    }

    private void gotoHitoryTrack() {
        if (!GolukUtils.isNetworkConnected(getContext())) {
            $.toast().text(R.string.user_net_unavailable).show();
            return;
        }
        Intent intentTrackList = new Intent(getContext(), TrackListActivity.class);
        getContext().startActivity(intentTrackList);
    }

    @Override
    public void onLocation(Location location) {
        if (location != null)
            updateMapUserLocation(new Point(location.getLatitude(), location.getLongitude()), false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Event event) {
        if (EventUtil.isConnectedEvent(event)) {
            // 显示是否进行绑定提示
            if (mIsActive && mIsNeedBind) {
                final String imei = (String) event.data;
                EventUtil.sendBindDeviceEvent(imei);
                mIsNeedBind = false;
            }
        } else if (EventUtil.isBindedByOtherEvent(event)) {
            // 设备被用户绑走
            BindedByOtherRequest request = new BindedByOtherRequest(REQUEST_CODE_BINDED_BY_OTHER, this);
            request.request();
        } else if (EventUtil.isBindDeviceSuccessEvent(event)) {
            // 绑定成功,刷新
            if (mDeviceStatusQueryer != null)
                mDeviceStatusQueryer.start();
            // 取消绑定加载框
            dismissBindLoading();
        } else if (EventUtil.isSIMStateChangedEvent(event)) {
            showConnectView(false);
        }
    }

    /**
     * 更新地图上手机位置
     */
    private void updateMapUserLocation(Point position, boolean moveCamera) {
        if (mMap != null) {
            mMap.updateUserLocation(position, moveCamera);
        }
    }

    @Override
    public void onQueryDeviceStatus(DeviceStatus deviceStatus) {
        mDeviceStatus = deviceStatus;
        // 查询设备状态返回
        updateLayout(true);
        // 更新设备信息UI
        updateDeviceInfoUI(deviceStatus);
        // 传递给Map
        if (mMap != null)
            mMap.updateCarStatus(deviceStatus);
    }

    @Override
    public void onNoDeviceBind() {
        // 无绑定设备
        updateLayout(false);
    }

    @Override
    public void onTrackQueryed(TrackDetail trackDetail, boolean trackChanged) {
        if (mMap != null)
            mMap.onTrackQueryed(trackDetail, trackChanged);
    }

    /**
     * 根据是否绑定显示Layout
     *
     * @param hasBind 是否绑定
     */
    private void updateLayout(boolean hasBind) {
        mLayoutBottom.setVisibility(hasBind ? View.VISIBLE : View.GONE);
        mLayoutConnect.setVisibility(hasBind ? View.GONE : View.VISIBLE);
        // Title bar
        showMenu(false);
        // location
        if (hasBind)
            mLocationUtil.startLocation();
        else
            mLocationUtil.pauseLocation();
    }

    /**
     * 更新设备信息显示
     */
    private void updateDeviceInfoUI(DeviceStatus deviceStatus) {
        if (deviceStatus == null)
            return;
        // 监控按钮
        mBtnLive.setVisibility(DeviceUtil.isOffline(deviceStatus) ? View.GONE : View.VISIBLE);
        // 在线状态
        final Resources res = getContext().getResources();
        int textColorId = res.getColor(DeviceUtil.isOnline(deviceStatus) ? R.color.font_green : R.color.font_96);
        mTvOnlineStatus.setTextColor(textColorId);
        int stateStringResId = DeviceUtil.getStateStringResidByState(deviceStatus);
        if (stateStringResId != -1)
            mTvOnlineStatus.setText(stateStringResId);
        // 如果是离线或者休眠,显示最近定位时间
        if (DeviceUtil.isOnline(deviceStatus)) {
            mTvLastestLocationLabel.setText(R.string.position);
            mTvLastLocateTime.setText("");
        } else {
            mTvLastestLocationLabel.setText(R.string.most_recent_location);
            mTvLastLocateTime.setText(DateUtils.formatDateNomalString(deviceStatus.pointTime));
        }

        // 位置
        mAddressConvert.convert(deviceStatus.lastLat, deviceStatus.lastLon,
                new AddressConvert.AddressConvertCallback() {
                    @Override
                    public void onAddressConverted(String address) {
                        mTvDeviceLocation.setText(address);
                    }
                });

        // Title bar
        showMenu(false);
    }

}
