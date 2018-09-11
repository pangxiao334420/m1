package com.goluk.a6.control.dvr;

import android.content.Intent;
import android.os.Bundle;

import com.goluk.a6.common.event.ConnectEvent;
import com.goluk.a6.control.BaseActivity;
import com.goluk.a6.control.CarWebSocketClient;
import com.goluk.a6.control.R;
import com.goluk.a6.internation.GolukUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 设备设置页面
 */
public class DeviceSettingActivity extends BaseActivity {

    private QuickSettingFragment2 mQuickSettingFragment2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_setting);
        showBack(true);
        setTitle(getString(R.string.dvrset));
        initView();

        CarWebSocketClient.instance().registerCallback(mQuickSettingFragment2);
        EventBus.getDefault().register(this);
    }

    private void initView() {
        mQuickSettingFragment2 = (QuickSettingFragment2) findViewById(R.id.quick_setting_fragment2);
        mQuickSettingFragment2.refreshSetting();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CarWebSocketClient.instance().unregisterCallback(mQuickSettingFragment2);
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mQuickSettingFragment2.handlerActivityResult(requestCode, resultCode, data);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ConnectEvent event) {
        if (!event.connect) {
            GolukUtils.showToast(this, getString(R.string.device_offline));
            finish();
        }
    }

}
