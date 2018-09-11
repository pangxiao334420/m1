package com.goluk.a6.control;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.goluk.a6.common.event.UpdateBindListEvent;
import com.goluk.a6.control.dvr.RemoteCameraConnectManager;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.BindDefaultRequest;
import com.goluk.a6.http.request.BindDeleteRequest;
import com.goluk.a6.http.request.BindListRequest;
import com.goluk.a6.http.responsebean.BindAddResult;
import com.goluk.a6.http.responsebean.BindListResult;
import com.goluk.a6.http.responsebean.ServerBaseResult;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.UserUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DevicesActivity extends BaseActivity implements IRequestResultListener {
    private SwipeMenuListView mListView;
    List<BindAddResult.BindBean> imeis = new ArrayList<>();
    private AppAdapter adapter;
    private FrameLayout mEmpty;
    private SwipeRefreshLayout mswLayout;
    BindListRequest request;
    private LinearLayout mBlankPageLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        setTitle(R.string.device_manage);
        showBack(true);
        mswLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mListView = (SwipeMenuListView) findViewById(R.id.listView);

        mEmpty = (FrameLayout) findViewById(R.id.iv_empty);
//        mListView.setMenuCreator(creator);
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                delete(position);
                return false;
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                setDeault(imeis.get(i).bindId);
            }
        });

        mListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        mswLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mswLayout.setRefreshing(false);
                loadData();
            }
        });
        mBlankPageLL = (LinearLayout) findViewById(R.id.ll_blank_page);
        mBlankPageLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!GolukUtils.isNetworkConnected(DevicesActivity.this)) {
                    GolukUtils.showToast(DevicesActivity.this, getResources().getString(R.string.user_net_unavailable));
                    return;
                }
                loadData();
            }
        });
        loadData();
    }

    private void delete(final int position) {
        AlertDialog isExit = new AlertDialog.Builder(DevicesActivity.this).create();
        // 设置对话框标题
        isExit.setTitle(R.string.hint);
        // 设置对话框消息
        isExit.setMessage(getString(R.string.delete_device));
        // 添加选择按钮并注册监听
        isExit.setButton(getString(R.string.delete_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                delete(imeis.get(position).bindId);
            }
        });
        isExit.setButton2(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        // 显示对话框
        isExit.show();
    }

    private void loadData() {
        request = new BindListRequest(100, this);
        if (!GolukUtils.isNetworkConnected(this)) {
            GolukUtils.showToast(this, getResources().getString(R.string.user_net_unavailable));
            mBlankPageLL.setVisibility(View.VISIBLE);
            return;
        }
        mBlankPageLL.setVisibility(View.GONE);
        request.get(mApp.getMyInfo().uid);
    }

    private void adaptViewdw() {
        if (imeis.size() == 0) {
            mEmpty.setVisibility(View.VISIBLE);
        } else {
            mEmpty.setVisibility(View.GONE);
        }
    }


    SwipeMenuCreator creator = new SwipeMenuCreator() {

        @Override
        public void create(SwipeMenu menu) {
            SwipeMenuItem deleteItem = new SwipeMenuItem(
                    getApplicationContext());
            deleteItem.setBackground(new ColorDrawable(Color.rgb(0xFf,
                    0xfF, 0xff)));
            deleteItem.setWidth(dp2px(60));
            deleteItem.setIcon(R.drawable.ic_delete);
            menu.addMenuItem(deleteItem);
        }
    };

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }


    class AppAdapter extends BaseAdapter {
        List<BindAddResult.BindBean> imeis;

        public AppAdapter(List<BindAddResult.BindBean> list) {
            imeis = list;
        }

        @Override
        public int getCount() {
            return imeis.size();
        }

        @Override
        public Object getItem(int i) {
            return imeis.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(DevicesActivity.this).inflate(R.layout.device_item_swipe, null);
            }

            ViewHolder vh = (ViewHolder) convertView.getTag();
            if (vh == null) {
                vh = new ViewHolder();
                vh.mServerIme = (TextView) convertView.findViewById(R.id.device_name);
                vh.mServerImageCur = (ImageView) convertView.findViewById(R.id.device_select);
                vh.mDelete = (ImageView) convertView.findViewById(R.id.device_delete);
                vh.mDeviceImei = (TextView) convertView.findViewById(R.id.device_imei);
                convertView.setTag(vh);
            }
            String name = imeis.get(position).name;
            String imei = imeis.get(position).imei;
            if(!TextUtils.isEmpty(name)){
                name = name.replace("\"", "");
            }
            vh.mServerIme.setText(name);
            vh.mDeviceImei.setText("IMEI: "+imei);
            if (mApp.bindListBean != null) {
                if (imeis.get(position).bindId.equals(mApp.bindListBean.defaultId))
                    vh.mServerImageCur.setVisibility(View.VISIBLE);
                else
                    vh.mServerImageCur.setVisibility(View.GONE);
            }
            vh.mDelete.setTag(position);
            vh.mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    delete((int) view.getTag());
                }
            });
            return convertView;
        }

        private class ViewHolder {
            TextView mServerIme;
            TextView mDeviceImei;
            ImageView mServerImageCur;
            ImageView mDelete;
        }
    }

    private void setDeault(String bindId) {
        final BindDefaultRequest request = new BindDefaultRequest(2, this);
        if (!UserUtils.isNetDeviceAvailable(this)) {
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
            return;
        }
        request.get(mApp.getMyInfo().uid, bindId);
    }

    private void delete(String s) {
        final BindDeleteRequest request = new BindDeleteRequest(1, this);
        if (!UserUtils.isNetDeviceAvailable(this)) {
            GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
            return;
        }
        request.get(mApp.getMyInfo().uid, s);
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == 1) {
            ServerBaseResult result1 = (ServerBaseResult) result;
            if (result1 != null && result1.code == 0) {
                EventBus.getDefault().post(new UpdateBindListEvent());
                RemoteCameraConnectManager.instance().needUploadImei();
                loadData();

                mApp.getImei();
            } else {
                showToast(getString(R.string.delete_bindid_error));
            }
        } else if (requestType == 2) {
            ServerBaseResult result1 = (ServerBaseResult) result;
            if (result1 != null && result1.code == 0) {
                EventBus.getDefault().post(new UpdateBindListEvent());
                loadData();

                mApp.getImei();
            } else {
                showToast(getString(R.string.set_defualt_bind_error));
            }
        } else if (requestType == 100) {
            imeis.clear();
            BindListResult bean = (BindListResult) result;
            if (bean != null && bean.code == 0 && bean.data != null && bean.data != null) {
                mApp.bindListBean = bean.data;
                if (mApp.bindListBean != null && mApp.bindListBean.list != null) {
                    imeis.addAll(mApp.bindListBean.list);
                }
                // Fix bugly #3252
                if (imeis != null)
                    Collections.reverse(imeis);
                if (adapter == null) {
                    adapter = new AppAdapter(imeis);
                    mListView.setAdapter(adapter);
                } else {
                    adapter.imeis = imeis;
                }
                //始终已服务器段的代码来同步本地
                List<String> keyset = new ArrayList<>();
                for (String key : RemoteCameraConnectManager.instance().historyList.keySet()) {
                    boolean find = false;
                    for (BindAddResult.BindBean temp : imeis) {
                        if (temp.sn.equals(key)) {
                            find = true;
                            break;
                        }
                    }
                    if (!find) {
                        keyset.add(key);
                    }
                }
                for (String key : keyset) {
                    RemoteCameraConnectManager.instance().historyList.remove(key);
                }
                adapter.notifyDataSetChanged();
            }
            adaptViewdw();
        }
    }
}
