package com.goluk.a6.control;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.goluk.a6.common.util.DateUtils;
import com.goluk.a6.common.util.H5Util;
import com.goluk.a6.control.live.GolukLiveActivity;
import com.goluk.a6.control.live.LiveConstant;
import com.goluk.a6.control.ui.event.EventDetailActivity;
import com.goluk.a6.control.util.AddressConvert;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.DeviceStatusBean;
import com.goluk.a6.http.request.DeviceStatusRequest;
import com.goluk.a6.http.request.FamilyCheckRequest;
import com.goluk.a6.http.request.FamilyEventRequest;
import com.goluk.a6.http.responsebean.FamilyCheckResult;
import com.goluk.a6.http.responsebean.FamilyEventResult;
import com.goluk.a6.internation.GlideCircleTransform;
import com.goluk.a6.internation.GlideUtils;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.UserUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FamilyActivity extends BaseActivity implements IRequestResultListener, Handler.Callback {

    private static final int REQUEST_CODE_DEVICES_STATUS = 1001;

    private LinearLayout mLlNone;
    private LinearLayout mLlContent;
    private FamilyCheckRequest checkRequest;
    private TextView mStart;
    private LinearLayout mlinear;
    private PullToRefreshListView mListView;
    private int maxSize;
    List<FamilyEventResult.FamilyEventDetailBean> list;
    private FamilyEventAdapter adapter;
    private FamilyEventRequest request = new FamilyEventRequest(2, this);
    private DeviceStatusRequest deviceStatusRequest = new DeviceStatusRequest(REQUEST_CODE_DEVICES_STATUS, this);
    private boolean isPullDown;
    private Handler mHandler;
    private boolean havemore;
    private LinearLayout mBlankPageLL;
    private long mCurrentIndex = 0;
    private ProgressDialog mProgressDialog;
    private String mCurrentLocationString;
    private int mCameraNumber = 1;
    private String mClickUserName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family);
        setTitle(R.string.family);
        showBack(true);
        initView();
    }

    private void initView() {
        mHandler = new Handler(Looper.getMainLooper(),this);
        mProgressDialog = new ProgressDialog(this);
        list = new ArrayList<>();
        mLlNone = (LinearLayout) findViewById(R.id.nll_none);
        mLlContent = (LinearLayout) findViewById(R.id.ll_content);
        mlinear = (LinearLayout) findViewById(R.id.linear);
        mStart = (TextView) findViewById(R.id.btn_start);
        mListView = (PullToRefreshListView) findViewById(R.id.listView);
        checkRequest = new FamilyCheckRequest(1, this);
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mApp.isBoundIMei()) {
                    showToast(getString(R.string.please_bound_device));
                    return;
                }
                if (!UserUtils.isNetDeviceAvailable(FamilyActivity.this)) {
                    GolukUtils.showToast(FamilyActivity.this, getResources().getString(R.string.user_net_unavailable));
                } else {
                    FamilyActivity.this.startActivity(new Intent(FamilyActivity.this, FamilyMyLinkActivity.class));
                }
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > list.size() && i == 0) {
                    return;
                }
                FamilyEventResult.FamilyEventDetailBean bean = list.get(i - 1);
                // 轨迹详情已移除,只有事件详情
                if (bean.type == 1) {
                    Intent intent = new Intent(FamilyActivity.this, WebviewActivity.class);
                    intent.putExtra(WebviewActivity.KEY_WEBVIEW_TITLE, FamilyActivity.this.getString(R.string.track_detail));
                    intent.putExtra(WebviewActivity.KEY_WEBVIEW_URL, H5Util.getTrackDetailUrl(bean.track.trackId, bean.user.uid, bean.track.imei));
                    startActivity(intent);
                } else {
//                    Intent intent = new Intent(FamilyActivity.this, WebviewActivity.class);
//                    intent.putExtra(WebviewActivity.KEY_WEBVIEW_TITLE, FamilyActivity.this.getString(R.string.event_detail));
////                    intent.putExtra(WebviewActivity.KEY_BUTTON, true);
//                    intent.putExtra(WebviewActivity.KEY_EXTRAL, bean.event.eventId);
//                    intent.putExtra(WebviewActivity.KEY_WEBVIEW_URL, H5Util.getMessageDetailUrl(bean));
//                    startActivity(intent);

                    Intent intent = new Intent(FamilyActivity.this, EventDetailActivity.class);
                    intent.putExtra("eventId", bean.event.eventId);
                    intent.putExtra("fromFamily", true);
                    startActivity(intent);
                }
            }
        });
        mListView.setMode(PullToRefreshBase.Mode.BOTH);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                isPullDown = false;
                havemore = false;
                loadData();
//                request.get(mApp.getMyInfo().uid, "0", "");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (!UserUtils.isNetDeviceAvailable(FamilyActivity.this)) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            GolukUtils.showToast(FamilyActivity.this, FamilyActivity.this.getResources().getString(R.string.user_net_unavailable));
                            mListView.onRefreshComplete();
                        }
                    }, 1000);
                    return;
                }
                if (list != null && !havemore) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showToast(getString(R.string.no_more));
                            mListView.onRefreshComplete();
                        }
                    }, 1000);
                    return;
                }
                isPullDown = true;
                if (list != null) {
                    request.get(mApp.getMyInfo().uid, "2", String.valueOf(mCurrentIndex));
                    return;
                }
                mListView.onRefreshComplete();
            }
        });
        mBlankPageLL = (LinearLayout) findViewById(R.id.ll_blank_page);
        mBlankPageLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!GolukUtils.isNetworkConnected(FamilyActivity.this)) {
                    GolukUtils.showToast(FamilyActivity.this, getResources().getString(R.string.user_net_unavailable));
                    return;
                }
                loadData();
            }
        });
    }

    public void showFamilyUser(boolean value) {
        if (value) {
            getActionBar().getCustomView().findViewById(R.id.tv_right).setVisibility(View.VISIBLE);
            ((TextView) getActionBar().getCustomView().findViewById(R.id.tv_right)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.nav_family_member_pre, 0);
            ((TextView) getActionBar().getCustomView().findViewById(R.id.tv_right)).setText("");
            getActionBar().getCustomView().findViewById(R.id.tv_right).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FamilyActivity.this.startActivity(new Intent(FamilyActivity.this, FamilyMyUsersActivity.class));
                }
            });
            mLlNone.setVisibility(View.GONE);
            mLlContent.setVisibility(View.VISIBLE);
        } else {
            mLlNone.setVisibility(View.VISIBLE);
            mLlContent.setVisibility(View.GONE);
            getActionBar().getCustomView().findViewById(R.id.tv_right).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        mlinear.removeAllViews();
        list.clear();
        if (mApp.isUserLoginToServerSuccess()) {
            if (!UserUtils.isNetDeviceAvailable(this)) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListView.onRefreshComplete();
                    }
                }, 1000);
                mBlankPageLL.setVisibility(View.VISIBLE);
            } else {
                mBlankPageLL.setVisibility(View.GONE);
                checkRequest.get(mApp.getMyInfo().uid);
            }
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == 1) {
            FamilyCheckResult checkResult = (FamilyCheckResult) result;
            if (checkResult != null && checkResult.code == 0 && checkResult.data != null && checkResult.data.size > 0) {
                showDetailView(checkResult.data);
                showFamilyUser(true);
                return;
            }
            showFamilyUser(false);
        } else if (requestType == 2) {
            FamilyEventResult eventResult = (FamilyEventResult) result;
            mCurrentIndex = 0;
            mListView.onRefreshComplete();
            if (eventResult != null && eventResult.code == 0 && eventResult.data != null && eventResult.data.size > 0) {
                mCurrentIndex = eventResult.data.index;
                havemore = eventResult.data.size >= 20;
                showEvent(eventResult.data);
            } else {
                //mListView.setVisibility(View.GONE);
            }
        } else if (requestType == REQUEST_CODE_DEVICES_STATUS) {
            Message message = mHandler.obtainMessage();
            message.what = REQUEST_CODE_DEVICES_STATUS;
            message.obj = result;
            mHandler.sendMessage(message);
        }
    }

    private void showEvent(FamilyEventResult.FamilyEventBean data) {
        if (!isPullDown) {
            list.clear();
        }
        mListView.setVisibility(View.VISIBLE);
        list.addAll(data.events);
        if (adapter == null) {
            adapter = new FamilyEventAdapter(list);
            mListView.setAdapter(adapter);
        } else {
            adapter.list = list;
        }
        adapter.notifyDataSetChanged();
    }

    private void showDetailView(FamilyCheckResult.FamilyBean data) {
        showFamilyUser(true);
        for (int i = 0; i < data.size; i++) {
            LinearLayout lllayout = new LinearLayout(this);
            lllayout.setClickable(true);
            lllayout.setId(i);
//            lllayout.setBackgroundColor(Color.RED);
            lllayout.setOrientation(LinearLayout.VERTICAL);
            lllayout.setPadding(0, 5, 10, 5);
            lllayout.setGravity(Gravity.LEFT);
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            TextView textView = new TextView(this);
            textView.setSingleLine(true);
            textView.setGravity(Gravity.LEFT);
//            textView.setBackgroundColor(Color.GREEN);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            try {
                textView.setText(URLDecoder.decode(data.list.get(i).name, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                textView.setText(String.valueOf(data.list.get(i).name));
            }
//            imageView.setBackgroundColor(Color.BLUE);
            lllayout.addView(imageView, (int) (50 * getResources().getDisplayMetrics().density), (int) (50 * getResources().getDisplayMetrics().density));
            lllayout.addView(textView, new LinearLayout.LayoutParams((int) (70 * getResources().getDisplayMetrics().density), ViewGroup.LayoutParams.WRAP_CONTENT));

            lllayout.setTag(data.list.get(i));
            lllayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FamilyCheckResult.FamilyUserBean data = (FamilyCheckResult.FamilyUserBean) view.getTag();
                    if (TextUtils.isEmpty(data.imei)) {
                        showToast(getString(R.string.user_not_bound));
                        return;
                    }
                    if (!UserUtils.isNetDeviceAvailable(FamilyActivity.this)) {
                        GolukUtils.showToast(FamilyActivity.this, getResources().getString(R.string.user_net_unavailable));
                    } else {
                        mClickUserName = data.name;
                        //开始准备直播，获取设备状态
                        deviceStatusRequest.get(data.uid,data.imei);
                        mProgressDialog.show();

                    }
                }
            });
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            llp.setMargins(0, 0, 0, 0);
            mlinear.addView(lllayout, llp);
            if (TextUtils.isEmpty(data.list.get(i).avatar) || data.list.get(i).avatar.contains("default")) {
                Glide.with(this).load(R.drawable.usercenter_head_default).transform(new GlideCircleTransform(this)).into(imageView);
            } else {
                GlideUtils.loadNetHead(this, imageView, data.list.get(i).avatar, R.drawable.usercenter_head_default);
            }
        }
        request.get(mApp.getMyInfo().uid, "0", "");
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == REQUEST_CODE_DEVICES_STATUS) {
            mProgressDialog.dismiss();
            final DeviceStatusBean bean = (DeviceStatusBean) msg.obj;
            if (bean != null && bean.code == 0 && bean.data != null) {
                if (bean.data.cameraSN != null) {
                    mCameraNumber = bean.data.cameraSN.size();
                }

                //mCurrentLocationString=GolukUtils.getAddress(bean.data.lastLat,bean.data.lastLon);

                AddressConvert addressConvert = new AddressConvert();
                addressConvert.convert(bean.data.lastLat, bean.data.lastLon,
                        new AddressConvert.AddressConvertCallback() {
                            @Override
                            public void onAddressConverted(String address) {
                                mCurrentLocationString = address;
                                toLivePage(bean.data.state, bean.data.imei);
                            }
                        });

                //toLivePage(bean.data.state,bean.data.imei);
            }
        }
        return false;
    }

    private void toLivePage(int state,String imei){
        Intent intent = new Intent(this, GolukLiveActivity.class);
        intent.putExtra(LiveConstant.KEY_CAR_IMEI,imei);
        intent.putExtra(LiveConstant.KEY_CAR_STATE,state);
        intent.putExtra(LiveConstant.KEY_CAR_LOCATION,mCurrentLocationString);
        intent.putExtra(LiveConstant.KEY_DEVICE_CAMERA_NUMBER,mCameraNumber);
        intent.putExtra(LiveConstant.KEY_USER_NAME,mClickUserName);
        this.startActivity(intent);
    }


    class FamilyEventAdapter extends BaseAdapter {
        List<FamilyEventResult.FamilyEventDetailBean> list;

        public FamilyEventAdapter(List<FamilyEventResult.FamilyEventDetailBean> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(FamilyActivity.this).inflate(R.layout.family_item_event, null);
            }
            ViewHolder vh = (ViewHolder) convertView.getTag();
            if (vh == null) {
                vh = new ViewHolder();
                vh.mUserName = (TextView) convertView.findViewById(R.id.user_name);
                //vh.mDeviceName = (TextView) convertView.findViewById(R.id.device_name);
                vh.mTime = (TextView) convertView.findViewById(R.id.time);
                vh.mTrackTimeDesc = (TextView) convertView.findViewById(R.id.track_time);
                vh.mMail = (TextView) convertView.findViewById(R.id.tv_km);
                vh.mTrackTime = (TextView) convertView.findViewById(R.id.tv_time);
                vh.mSpeed = (TextView) convertView.findViewById(R.id.tv_speed);
                vh.mUserHead = (ImageView) convertView.findViewById(R.id.rename);
                vh.mLLInfo = (LinearLayout) convertView.findViewById(R.id.ll_info);
                convertView.setTag(vh);
            }
            if (list.get(position).type == 1) {
                vh.mLLInfo.setVisibility(View.VISIBLE);
                if (list.get(position).track != null) {
                    vh.mMail.setText(String.format("%.1f", list.get(position).track.mileage));
                    vh.mTime.setText(DateUtils.formatDateAndTime(list.get(position).track.starttime));
                    vh.mTrackTime.setText(DateUtils.getTime(getmint(list.get(position).track.starttime, list.get(position).track.endtime)));
                    vh.mSpeed.setText(String.format("%.1f", list.get(position).track.speed));
                    vh.mTrackTimeDesc.setText(getString(R.string.track, DateUtils.formatDateTime(list.get(position).track.starttime), DateUtils.formatDateTime(list.get(position).track.endtime)));
                }
            } else {
                vh.mLLInfo.setVisibility(View.GONE);
                if (list.get(position).event != null) {
                    //vh.mDeviceName.setText(list.get(position).event.deviceName);
                    vh.mTime.setText(DateUtils.getDateAndTime(list.get(position).event.time));
                    int res = list.get(position).event.getEventStringRes();
                    String event = "";
                    if (res != 0) {
                        event = getString(res);
                    }
                    vh.mTrackTimeDesc.setText(getString(R.string.event_detail_info, event));
                    vh.mTrackTimeDesc.setText(event);
                }
            }
            if (list.get(position).user != null) {
                try {
                    vh.mUserName.setText(URLDecoder.decode(list.get(position).user.name, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (Exception ex) {
                    vh.mUserName.setText(String.valueOf(list.get(position).user.name));
                }

                Glide.with(FamilyActivity.this)
                        .load(list.get(position).user.avatar)
                        .transform(new GlideCircleTransform(FamilyActivity.this))
                        .into(vh.mUserHead);
            }
            return convertView;
        }

        public int getmint(Date start, Date end) {
            if (start == null || end == null) {
                return 0;
            }
            return (int) ((end.getTime() / 1000) - (start.getTime() / 1000));
        }

        private class ViewHolder {
            TextView mUserName;
            TextView mDeviceName;
            LinearLayout mLLInfo;
            TextView mTime;
            TextView mMail;
            TextView mTrackTime;
            TextView mTrackTimeDesc;
            TextView mSpeed;
            ImageView mUserHead;
        }
    }
}
