package com.goluk.a6.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.goluk.a6.control.R;
import com.goluk.a6.control.ui.event.EventDetailActivity;
import com.goluk.a6.http.responsebean.XgLocalServerCustomBean;
import com.goluk.a6.http.responsebean.XgServerCustomBean;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageReceiver extends XGPushBaseReceiver {
    public static final String NOTIFICATION_BROADCAST = "com.goluk.broadcast.m1";
    public static final String NOTIFICATION_BROADCAST_CANCEL = "com.goluk.broadcast.m1.notification_cancelled";
    public static final String NOTIFICATION_KEY_FROM = "from";
    public static final String NOTIFICATION_KEY_JSON = "json";

    // 通知展示
    @Override
    public void onNotifactionShowedResult(Context context,
                                          XGPushShowedResult notifiShowedRlt) {

    }

    @Override
    public void onUnregisterResult(Context context, int errorCode) {

    }

    @Override
    public void onSetTagResult(Context context, int errorCode, String tagName) {

    }

    @Override
    public void onDeleteTagResult(Context context, int errorCode, String tagName) {

    }

    // 通知点击回调 actionType=1为该消息被清除，actionType=0为该消息被点击
    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult message) {
        if (context == null || message == null) {
            return;
        }
        if (message.getActionType() != 0) {
            return;
        }
        String customContent = message.getCustomContent();
        handleCustomLogic(context, customContent);
    }

    private static void handleCustomLogic(Context context, String customContent) {
        XgServerCustomBean bean;
        if (customContent.contains("local")) {
            XgLocalServerCustomBean localServerCustomBean = JSON.parseObject(customContent, XgLocalServerCustomBean.class);
            if (localServerCustomBean != null) {
                customContent = localServerCustomBean.local;
            }
        }
        bean = JSON.parseObject(customContent, XgServerCustomBean.class);
        if (bean == null || bean.p == null) {
            return;
        }
//        Intent intent = new Intent(context, WebviewActivity.class);
//        intent.putExtra(WebviewActivity.KEY_WEBVIEW_TITLE, context.getString(R.string.event_detail));
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra(WebviewActivity.KEY_WEBVIEW_URL, H5Util.getMessageDetailUrl(bean.p.m, bean.p.i));
//        context.startActivity(intent);

        if (!TextUtils.isEmpty(bean.p.i)) {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("eventId", bean.p.i);
            context.startActivity(intent);
        }
    }

    @Override
    public void onRegisterResult(Context context, int errorCode,
                                 XGPushRegisterResult message) {
    }

    // 消息透传
    @Override
    public void onTextMessage(Context context, XGPushTextMessage message) {
        if (context == null || message == null) {
            return;
        }
//        XGLocalMessage localMessage = new XGLocalMessage();
//        localMessage.setTitle(message.getTitle());
//        localMessage.setContent(message.getContent());
        HashMap<String, String> temp = new HashMap<>();
        temp.put("local", message.getCustomContent());
//        localMessage.setCustomContent(temp);
//        XGPushManager.addLocalNotification(context, localMessage);
        showNotify(context, message, JSON.toJSONString(temp));
    }

    public void showNotify(Context context, XGPushTextMessage msgBean, String json) {
        NotificationManager mNotiManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotiManager == null) {
            return;
        }
        mNotiManager.notify(getID(), createNotification(context, json, msgBean.getTitle(), msgBean.getContent()));
    }


    private Notification createNotification(Context startActivity, String json, String title, String content) {
        PendingIntent contentIntent = getPendingIntent(startActivity, json);
        Notification.Builder builder = new Notification.Builder(startActivity)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDeleteIntent(getDeleteIntent(startActivity));
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        return notification;
    }

    private PendingIntent getPendingIntent(Context startActivity, String json) {
        PendingIntent pendingIntent;
        Intent intent = getBroadCastIntent(json);
        // 注意最后一个参数必须 写 PendingIntent.FLAG_UPDATE_CURRENT,
        // 否则下个Activity无法接受到消息
        pendingIntent = PendingIntent.getBroadcast(startActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private PendingIntent getDeleteIntent(Context context) {
        Intent intent = new Intent(context, GolukCancelAllNotificationReceiver.class);
        intent.setAction(NOTIFICATION_BROADCAST_CANCEL);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private Intent getBroadCastIntent(String json) {
        Intent intent = new Intent(NOTIFICATION_BROADCAST);
        intent.putExtra(NOTIFICATION_KEY_FROM, "notication");
        intent.putExtra(NOTIFICATION_KEY_JSON, json);
        return intent;
    }

    private final static AtomicInteger c = new AtomicInteger(0);

    public static int getID() {
        return c.incrementAndGet();
    }


    public static class GolukClickNotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) {
                return;
            }
            final String action = intent.getAction();
            if (NOTIFICATION_BROADCAST.equals(action)) {
                String json = intent.getStringExtra(NOTIFICATION_KEY_JSON);
                handleCustomLogic(context, json);
            }
        }
    }


    public static class GolukCancelAllNotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) {
                return;
            }
            final String action = intent.getAction();
            if (NOTIFICATION_BROADCAST_CANCEL.equals(action)) {
                NotificationManager mNotiManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (mNotiManager != null) {
                    mNotiManager.cancelAll();
                }
            }
        }
    }
}
