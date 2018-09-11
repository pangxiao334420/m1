package com.goluk.a6.control;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.goluk.a6.common.event.ImeiUpdateEvent;
import com.goluk.a6.common.event.util.Event;
import com.goluk.a6.common.event.util.EventUtil;
import com.goluk.a6.control.ui.event.EventCollectionsActivity;
import com.goluk.a6.control.ui.event.FragmentEventList;
import com.goluk.a6.internation.GolukUtils;
import com.goluk.a6.internation.UserUtils;
import com.goluk.a6.internation.login.InternationUserLoginActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import likly.dollar.$;

public class MessageView extends IPagerView implements View.OnClickListener {

    private FragmentEventList mFragmentEventList;

    public MessageView(Context context) {
        super(context);
        initView();
    }

    public MessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.messages_views, this);
        mFragmentEventList = (FragmentEventList) ((FragmentActivity) getContext()).getSupportFragmentManager().findFragmentById(R.id.message_content);

        EventBus.getDefault().register(this);
    }

    @Override
    public void onActivityCreate(Bundle savedInstanceState) {
    }

    @Override
    public boolean onCreateOptionsMenu(MenuInflater mi, Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    private void goSettings() {
        if (!CarControlApplication.getInstance().isUserLoginToServerSuccess()) {
            startLogin();
            return;
        }
        if (!UserUtils.isNetDeviceAvailable(getContext())) {
            GolukUtils.showToast(getContext(), getContext().getResources().getString(R.string.user_net_unavailable));
            return;
        }
        if (!CarControlApplication.getInstance().isBoundIMei()) {
            GolukUtils.showToast(getContext(), getContext().getResources().getString(R.string.please_bound_device));
            return;
        }
        Intent intent = new Intent(getContext(), MessageActivity.class);
        getContext().startActivity(intent);
    }

    private void myCollections() {
        if (!CarControlApplication.getInstance().isUserLoginToServerSuccess()) {
            startLogin();
            return;
        }
        if (!GolukUtils.isNetworkConnected(getContext())) {
            $.toast().text(R.string.user_net_unavailable).show();
            return;
        }
        Intent intent = new Intent(getContext(), EventCollectionsActivity.class);
        getContext().startActivity(intent);
    }

    public void startLogin() {
        Intent loginIntent = null;
        loginIntent = new Intent(getContext(), InternationUserLoginActivity.class);
        getContext().startActivity(loginIntent);
    }

    @Override
    public void onActivate() {

    }

    @Override
    public void onDeactivate() {

    }

    @Override
    public void onActivityPause() {

    }

    @Override
    public void onAcitvityResume() {
    }

    @Override
    public void onActivityDestroy() {
        EventBus.getDefault().unregister(this);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ImeiUpdateEvent event) {
        if (mFragmentEventList != null)
            mFragmentEventList.onRefreshing();
    }

    private View mTitleView;
    private ImageView mBtnSetting, mBtnCollection;

    public void showMenu() {
        showMenu(true);
    }

    public void showMenu(boolean replace) {
        if (mTitleView == null) {
            mTitleView = LayoutInflater.from(getContext()).inflate(R.layout.home_title_message, null);
            mBtnCollection = (ImageView) mTitleView.findViewById(R.id.btn_star);
            mBtnSetting = (ImageView) mTitleView.findViewById(R.id.btn_setting);

            mBtnCollection.setOnClickListener(this);
            mBtnSetting.setOnClickListener(this);

        }

        String imei = CarControlApplication.getInstance().serverImei;
        boolean hasBind = !TextUtils.isEmpty(imei);
        mBtnSetting.setImageResource(hasBind ? R.drawable.nav_setup_n : R.drawable.nav_setup_d);
        mBtnCollection.setImageResource(hasBind ? R.drawable.collection : R.drawable.collection_disable);
        mBtnSetting.setEnabled(hasBind);
        mBtnCollection.setEnabled(hasBind);

        if (replace)
            ((BaseActivity) getContext()).getActionBar().setCustomView(mTitleView);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Event event) {
        if (EventUtil.isDefaultDeviceChangedEvent(event)) {
            showMenu(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_star:
                myCollections();
                break;
            case R.id.btn_setting:
                goSettings();
                break;
        }
    }

}
