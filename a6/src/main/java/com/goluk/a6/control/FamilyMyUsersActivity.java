package com.goluk.a6.control;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.bumptech.glide.Glide;
import com.goluk.a6.http.IRequestResultListener;
import com.goluk.a6.http.request.FamilyCheckRequest;
import com.goluk.a6.http.request.FamilyDeleteRequest;
import com.goluk.a6.http.responsebean.FamilyCheckResult;
import com.goluk.a6.http.responsebean.ServerBaseResult;
import com.goluk.a6.internation.CustomLoadingDialog;
import com.goluk.a6.internation.GlideCircleTransform;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.UserUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FamilyMyUsersActivity extends BaseActivity implements IRequestResultListener {
    private Button mStart;
    public List<FamilyCheckResult.FamilyUserBean> list;

    private SwipeMenuListView mListView;
    private FamilyUserAdapter adapter;
    private FamilyCheckRequest checkRequest;
    private CustomLoadingDialog mPrograss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_user);
        setTitle(R.string.famly_users);
        showBack(true);
        initView();
    }

    private void initView() {
        mStart = (Button) findViewById(R.id.btn_start);
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mApp.isBoundIMei()) {
                    showToast(getString(R.string.please_bound_device));
                    return;
                }
                if (!UserUtils.isNetDeviceAvailable(FamilyMyUsersActivity.this)) {
                    GolukUtils.showToast(FamilyMyUsersActivity.this, getResources().getString(R.string.user_net_unavailable));
                } else {
                    FamilyMyUsersActivity.this.startActivity(new Intent(FamilyMyUsersActivity.this, FamilyMyLinkActivity.class));
                }
            }
        });
        list = new ArrayList<>();
        mPrograss = new CustomLoadingDialog(this, getString(R.string.delete_family_user));
        mListView = (SwipeMenuListView) findViewById(R.id.listView);
        checkRequest = new FamilyCheckRequest(1, this);
    }

    private void loadData() {
        checkRequest.get(mApp.getMyInfo().uid);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mApp.isUserLoginToServerSuccess()) {
            checkRequest.get(mApp.getMyInfo().uid);
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == 1) {
            list.clear();
            FamilyCheckResult checkResult = (FamilyCheckResult) result;
            if (checkResult != null && checkResult.code == 0 && checkResult.data != null && checkResult.data.size > 0) {
                list.addAll(checkResult.data.list);
                if (adapter == null) {
                    adapter = new FamilyUserAdapter(list);
                    mListView.setAdapter(adapter);
                } else {
                    adapter.list = list;
                }
                adapter.notifyDataSetChanged();
            }
        } else if (requestType == 2) {
            mPrograss.close();
            ServerBaseResult result1 = (ServerBaseResult) result;
            if (result1 != null && result1.code == 0) {
                showToast(getString(R.string.delete_family_success));
                if (list.size() > 1) {
                    loadData();
                } else {
                    finish();
                }
            } else {
                showToast(getString(R.string.delete_family_error));
            }
        }
    }

    class FamilyUserAdapter extends BaseAdapter {
        List<FamilyCheckResult.FamilyUserBean> list;

        public FamilyUserAdapter(List<FamilyCheckResult.FamilyUserBean> list) {
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
                convertView = LayoutInflater.from(FamilyMyUsersActivity.this).inflate(R.layout.family_item_user, null);
            }

            ViewHolder vh = (ViewHolder) convertView.getTag();
            if (vh == null) {
                vh = new ViewHolder();
                vh.mUserName = (TextView) convertView.findViewById(R.id.device_name);
                vh.mUserHead = (ImageView) convertView.findViewById(R.id.rename);
                vh.mStop = (TextView) convertView.findViewById(R.id.stop);
                vh.mStop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!UserUtils.isNetDeviceAvailable(FamilyMyUsersActivity.this)) {
                            GolukUtils.showToast(FamilyMyUsersActivity.this, getResources().getString(R.string.user_net_unavailable));
                        } else {
                            final String uid = (String) view.getTag();
                            AlertDialog formatDialog = new AlertDialog.Builder(FamilyMyUsersActivity.this)
                                    .setTitle(R.string.hint)
                                    .setMessage(R.string.delete_user)
                                    .setPositiveButton(R.string.delete_ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (!UserUtils.isNetDeviceAvailable(FamilyMyUsersActivity.this)) {
                                                GolukUtils.showToast(FamilyMyUsersActivity.this, getResources().getString(R.string.user_net_unavailable));
                                            } else {
                                                deleteUser(uid);
                                            }
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, null)
                                    .create();
                            formatDialog.show();
                        }
                    }
                });
                convertView.setTag(vh);
            }

            try {
                vh.mUserName.setText(URLDecoder.decode(list.get(position).name, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception ex){
                vh.mUserName.setText(String.valueOf(list.get(position).name));
            }
            if (TextUtils.isEmpty(list.get(position).avatar) || list.get(position).avatar.contains("default")) {
                Glide.with(FamilyMyUsersActivity.this)
                        .load(R.drawable.usercenter_head_default)
                        .transform(new GlideCircleTransform(FamilyMyUsersActivity.this))
                        .into(vh.mUserHead);
            } else {
                Glide.with(FamilyMyUsersActivity.this)
                        .load(list.get(position).avatar)
                        .transform(new GlideCircleTransform(FamilyMyUsersActivity.this))
                        .into(vh.mUserHead);
            }
            vh.mStop.setTag(list.get(position).uid);
            return convertView;
        }

        private class ViewHolder {
            TextView mUserName;
            TextView mStop;
            ImageView mUserHead;
        }
    }

    private void deleteUser(String uid) {
        mPrograss.show();
        FamilyDeleteRequest request = new FamilyDeleteRequest(2, this);
        request.get(mApp.getMyInfo().uid, uid);
    }
}
